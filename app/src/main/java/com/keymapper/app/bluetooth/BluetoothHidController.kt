package com.keymapper.app.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.keymapper.app.model.DeviceInfo
import com.keymapper.app.model.HidButtonEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

class BluetoothHidController(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val bluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val bluetoothAdapter get() = bluetoothManager.adapter

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _connectedDevice = MutableStateFlow<DeviceInfo?>(null)
    val connectedDevice: StateFlow<DeviceInfo?> = _connectedDevice.asStateFlow()

    private val _buttonEvents = MutableSharedFlow<HidButtonEvent>(extraBufferCapacity = 256)
    val buttonEvents = _buttonEvents.asSharedFlow()

    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var listenerThread: Thread? = null
    @Volatile private var running = false

    private var selectedAddress: String? = null
    private val prefs by lazy { context.getSharedPreferences("keymapper", Context.MODE_PRIVATE) }

    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val aclReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
            val addr = device.address ?: return
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    if (selectedAddress != null && selectedAddress == addr) {
                        _connectedDevice.value = DeviceInfo(device.name, addr)
                        _connectionState.value = ConnectionState.CONNECTED
                    }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    if (selectedAddress == addr) {
                        _connectedDevice.value = null
                        _connectionState.value = ConnectionState.DISCONNECTED
                    }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                        _connectedDevice.value = null
                        _connectionState.value = ConnectionState.DISCONNECTED
                    }
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(aclReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(aclReceiver, filter)
        }
        selectedAddress = prefs.getString("selected_address", null)
        if (selectedAddress != null) {
            val adapter = bluetoothAdapter
            val name = try { adapter?.getRemoteDevice(selectedAddress)?.name ?: "未知手柄" } catch (e: Exception) { "未知手柄" }
            _connectedDevice.value = DeviceInfo(name, selectedAddress!!)
            _connectionState.value = ConnectionState.CONNECTED
            Log.i(TAG, "🔄 恢复已选手柄: $name (${selectedAddress})")
        }
    }

    fun release() {
        try { context.unregisterReceiver(aclReceiver) } catch (_: Exception) {}
        disconnect()
    }

    /** 无障碍服务捕获到的按键事件 -> 转发给监听者。 */
    fun dispatchAccessibilityKey(event: HidButtonEvent) {
        _buttonEvents.tryEmit(event)
    }

    /** 用户选中一个设备。完全不做任何阻塞调用，防止 ANR。 */
    fun selectDevice(address: String) {
        selectedAddress = address
        prefs.edit().putString("selected_address", address).apply()
        val adapter = bluetoothAdapter
        val name = try { adapter?.getRemoteDevice(address)?.name ?: "未知手柄" } catch (e: Exception) { "未知手柄" }
        val info = DeviceInfo(name, address)
        _connectedDevice.value = info
        _connectionState.value = ConnectionState.CONNECTED
        Log.i(TAG, "✅ 用户选中手柄: $name ($address) 已保存")
    }

    fun unselectDevice() {
        selectedAddress = null
        prefs.edit().remove("selected_address").apply()
        stopListener()
        _connectionState.value = ConnectionState.DISCONNECTED
        _connectedDevice.value = null
        Log.i(TAG, "❌ 已取消选中并清除保存")
    }

    private suspend fun tryConnectSpp(device: BluetoothDevice) = withContext(Dispatchers.IO) {
        try {
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothAdapter?.cancelDiscovery()
            socket.connect() // 这里会阻塞几秒，但我们在 IO 线程
            inputStream = socket.inputStream
            outputStream = socket.outputStream
            startListener()
            Log.i(TAG, "SPP 数据通道已建立")
        } catch (e: Exception) {
            Log.d(TAG, "SPP 不可用: ${device.name}")
        }
    }

    suspend fun getCurrentlyConnectedDevices(): List<DeviceInfo> = withContext(Dispatchers.IO) {
        val adapter = bluetoothAdapter
        if (adapter == null) return@withContext emptyList()
        adapter.bondedDevices?.map { DeviceInfo(it.name, it.address) } ?: emptyList()
    }

    fun connect(deviceAddress: String): Boolean { selectDevice(deviceAddress); return true }
    fun disconnect() { unselectDevice() }

    private fun startListener() {
        running = true
        listenerThread = Thread {
            val buffer = ByteArray(1024)
            while (running) {
                try {
                    val n = inputStream?.read(buffer) ?: break
                    if (n > 0) parseReportBytes(buffer.copyOf(n))
                } catch (e: Exception) { break }
            }
        }.also { it.isDaemon = true; it.name = "HID-Listener"; it.start() }
    }

    private fun stopListener() {
        running = false
        try { inputStream?.close() } catch (_: Exception) {}
        try { outputStream?.close() } catch (_: Exception) {}
        inputStream = null; outputStream = null
        listenerThread?.interrupt(); listenerThread = null
    }

    private fun parseReportBytes(data: ByteArray) {
        if (data.isEmpty()) return
        val buttonByte = data[0].toInt() and 0xFF
        for ((bit, name) in BUTTON_MAP) {
            val pressed = (buttonByte and (1 shl (bit - 1))) != 0
            _buttonEvents.tryEmit(HidButtonEvent(
                buttonId = "BTN_$bit", buttonName = name, isPressed = pressed
            ))
        }
    }

    companion object {
        private const val TAG = "BluetoothHid"
        val BUTTON_MAP = mapOf(
            0x01 to "A", 0x02 to "B", 0x03 to "X", 0x04 to "Y",
            0x05 to "L1", 0x06 to "R1", 0x07 to "L2", 0x08 to "R2",
            0x09 to "SELECT", 0x0A to "START",
            0x0B to "L3", 0x0C to "R3", 0x0D to "HOME",
        )
    }
}
