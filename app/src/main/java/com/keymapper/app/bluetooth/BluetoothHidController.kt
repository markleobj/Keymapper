package com.keymapper.app.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.keymapper.app.model.DeviceInfo
import com.keymapper.app.model.HidButtonEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

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

    private val _buttonEvents = MutableSharedFlow<HidButtonEvent>(extraBufferCapacity = 512)
    val buttonEvents = _buttonEvents.asSharedFlow()

    private var selectedAddress: String? = null
    private val prefs by lazy { context.getSharedPreferences("keymapper", Context.MODE_PRIVATE) }

    private val aclReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
            val addr = device.address ?: return
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    Log.i(TAG, "📡 ACL_CONNECTED: ${device.name} ($addr) selected=$selectedAddress")
                    if (selectedAddress == addr) {
                        _connectedDevice.value = DeviceInfo(device.name ?: "未知手柄", addr)
                        _connectionState.value = ConnectionState.CONNECTED
                    }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    Log.i(TAG, "📡 ACL_DISCONNECTED: ${device.name} ($addr)")
                    if (selectedAddress == addr) {
                        _connectedDevice.value = null
                        _connectionState.value = ConnectionState.DISCONNECTED
                    }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                        Log.i(TAG, "📡 Bluetooth turned off")
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
            val info = DeviceInfo(name, selectedAddress!!)
            _connectedDevice.value = info
            _connectionState.value = ConnectionState.CONNECTED
            Log.i(TAG, "🔄 恢复已选手柄: $name (${selectedAddress})")

            Handler(Looper.getMainLooper()).postDelayed({
                verifyAclConnection()
            }, 500)
        }
    }

    private fun verifyAclConnection() {
        val addr = selectedAddress ?: return
        val adapter = bluetoothAdapter ?: return
        try {
            val all = adapter.bondedDevices ?: emptySet()
            val target = all.firstOrNull { it.address == addr }
            if (target == null) {
                Log.w(TAG, "⚠️ 已选设备 $addr 不在已配对列表中，可能已被取消配对")
                return
            }
            Log.i(TAG, "🔍 verifyAcl: 设备在已配对列表中，蓝牙开关=${adapter.isEnabled}")
        } catch (e: Exception) {
            Log.e(TAG, "verifyAclConnection error: ${e.message}")
        }
    }

    fun release() {
        try { context.unregisterReceiver(aclReceiver) } catch (_: Exception) {}
    }

    fun dispatchAccessibilityKey(event: HidButtonEvent) {
        _buttonEvents.tryEmit(event)
    }

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
        _connectionState.value = ConnectionState.DISCONNECTED
        _connectedDevice.value = null
        Log.i(TAG, "❌ 已取消选中并清除保存")
    }

    suspend fun getCurrentlyConnectedDevices(): List<DeviceInfo> {
        val adapter = bluetoothAdapter ?: return emptyList()
        val bonded = adapter.bondedDevices ?: emptySet()
        return bonded.map { dev ->
            DeviceInfo(dev.name ?: "未知设备", dev.address)
        }
    }

    fun connect(deviceAddress: String): Boolean { selectDevice(deviceAddress); return true }
    fun disconnect() { unselectDevice() }

    companion object {
        private const val TAG = "BluetoothHid"
    }
}
