package com.keymapper.app.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.keymapper.app.model.DeviceInfo
import com.keymapper.app.model.HidButtonEvent
import com.keymapper.app.model.JoystickEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

class BluetoothHidController(private val context: Context) {

    private val bluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val bluetoothAdapter get() = bluetoothManager.adapter

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _connectedDevice = MutableStateFlow<DeviceInfo?>(null)
    val connectedDevice: StateFlow<DeviceInfo?> = _connectedDevice.asStateFlow()

    private val _buttonEvents = MutableSharedFlow<HidButtonEvent>(extraBufferCapacity = 64)
    val buttonEvents = _buttonEvents.asSharedFlow()

    private val _joystickEvents = MutableSharedFlow<JoystickEvent>(extraBufferCapacity = 32)
    val joystickEvents = _joystickEvents.asSharedFlow()

    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var listenerThread: Thread? = null
    @Volatile private var running = false

    // Classic Bluetooth Serial UUID - fallback for non-HID devices
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    companion object {
        private const val TAG = "BluetoothHid"

        // Standard HID gamepad button IDs (based on Android key codes / HID usage table)
        val BUTTON_MAP = mapOf(
            0x01 to "A",
            0x02 to "B",
            0x03 to "X",
            0x04 to "Y",
            0x05 to "L1",
            0x06 to "R1",
            0x07 to "L2",
            0x08 to "R2",
            0x09 to "SELECT",
            0x0A to "START",
            0x0B to "L3",
            0x0C to "R3",
            0x0D to "HOME",
        )

        val DPAD_MAP = mapOf(
            0x01 to "DPAD_UP",
            0x02 to "DPAD_DOWN",
            0x04 to "DPAD_LEFT",
            0x08 to "DPAD_RIGHT",
        )
    }

    suspend fun getPairedDevices(): List<DeviceInfo> = withContext(Dispatchers.IO) {
        bluetoothAdapter?.bondedDevices
            ?.filter { it.bluetoothClass?.majorDeviceClass == 0x0005 || it.name != null }
            ?.map { DeviceInfo(it.name, it.address) }
            ?: emptyList()
    }

    fun connect(deviceAddress: String): Boolean {
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress) ?: return false
        _connectionState.value = ConnectionState.CONNECTING

        return try {
            // Try HidDevice profile first (skipped - requires system permissions)
            // Fallback to SPP
            if (connectViaSpp(device)) return true
            false
        } catch (e: Exception) {
            Log.e(TAG, "connect failed", e)
            _connectionState.value = ConnectionState.DISCONNECTED
            false
        }
    }

    private fun connectViaSpp(device: BluetoothDevice): Boolean {
        return try {
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothAdapter?.cancelDiscovery()
            socket.connect()
            inputStream = socket.inputStream
            outputStream = socket.outputStream
            _connectionState.value = ConnectionState.CONNECTED
            _connectedDevice.value = DeviceInfo(device.name, device.address)
            startListener()
            true
        } catch (e: Exception) {
            Log.w(TAG, "SPP failed, trying fallback method", e)
            // Try fallback RFCOMM
            try {
                val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                val socket = method.invoke(device, 1) as android.bluetooth.BluetoothSocket
                socket.connect()
                inputStream = socket.inputStream
                outputStream = socket.outputStream
                _connectionState.value = ConnectionState.CONNECTED
                _connectedDevice.value = DeviceInfo(device.name, device.address)
                startListener()
                true
            } catch (e2: Exception) {
                Log.e(TAG, "fallback SPP also failed", e2)
                false
            }
        }
    }

    private fun startListener() {
        running = true
        listenerThread = Thread {
            val buffer = ByteArray(1024)
            while (running) {
                try {
                    val n = inputStream?.read(buffer) ?: break
                    if (n > 0) {
                        val data = buffer.copyOf(n)
                        parseReportBytes(data)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "listener stopped", e)
                    break
                }
            }
            if (_connectionState.value == ConnectionState.CONNECTED) {
                _connectionState.value = ConnectionState.DISCONNECTED
                _connectedDevice.value = null
            }
        }.also { it.isDaemon = true; it.name = "HID-Listener"; it.start() }
    }

    private fun stopListener() {
        running = false
        try { inputStream?.close() } catch (_: Exception) {}
        try { outputStream?.close() } catch (_: Exception) {}
        inputStream = null; outputStream = null
        listenerThread?.interrupt()
        listenerThread = null
    }

    fun disconnect() {
        stopListener()
        _connectionState.value = ConnectionState.DISCONNECTED
        _connectedDevice.value = null
    }

    /**
     * Parse raw bytes from HID interrupt or SPP stream.
     * Gamepad HID reports are typically 64-byte, with first bytes as buttons/D-pad,
     * and later bytes as axis (little-endian 16-bit).
     */
    private fun parseReportBytes(data: ByteArray) {
        if (data.isEmpty()) return

        // Try Xbox/PS style: byte[0] = buttons, byte[1] = D-pad, bytes[2..7] = axes
        if (data.size >= 6) {
            parseStandardGamepadReport(data)
            return
        }
        // Fallback: try single-byte HID keyboard style
        parseHidReport(data)
    }

    private fun parseStandardGamepadReport(data: ByteArray) {
        val buttonByte = data[0].toInt() and 0xFF
        val dpadByte = data[1].toInt() and 0xFF

        // Buttons (bits)
        for ((bit, name) in BUTTON_MAP) {
            val pressed = (buttonByte and (1 shl (bit - 1))) != 0
            _buttonEvents.tryEmit(HidButtonEvent(
                buttonId = "BTN_$bit",
                buttonName = name,
                isPressed = pressed
            ))
        }

        // D-pad
        for ((mask, name) in DPAD_MAP) {
            val pressed = (dpadByte and mask) != 0
            _buttonEvents.tryEmit(HidButtonEvent(
                buttonId = name,
                buttonName = name,
                isPressed = pressed
            ))
        }

        // Axes (first 4 axis channels: LX, LY, RX, RY)
        if (data.size >= 10) {
            val lx = readAxis(data, 2)
            val ly = readAxis(data, 4)
            val rx = readAxis(data, 6)
            val ry = readAxis(data, 8)
            if (lx != 0f || ly != 0f) {
                _joystickEvents.tryEmit(JoystickEvent("LEFT", lx, ly))
            }
            if (rx != 0f || ry != 0f) {
                _joystickEvents.tryEmit(JoystickEvent("RIGHT", rx, ry))
            }
        }
    }

    private fun readAxis(data: ByteArray, offset: Int): Float {
        if (offset + 1 >= data.size) return 0f
        val raw = (data[offset + 1].toInt() shl 8) or (data[offset].toInt() and 0xFF)
        val signed = if (raw > 0x7FFF) raw - 0x10000 else raw
        return signed / 32768f
    }

    private fun parseHidReport(data: ByteArray) {
        for (b in data) {
            val v = b.toInt() and 0xFF
            if (v in 0x04..0xD1) {
                _buttonEvents.tryEmit(HidButtonEvent(
                    buttonId = "KEY_$v",
                    buttonName = "KEY_$v",
                    isPressed = v != 0
                ))
            }
        }
    }
}
