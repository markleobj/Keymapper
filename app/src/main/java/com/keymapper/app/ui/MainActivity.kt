package com.keymapper.app.ui

import android.provider.Settings
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keymapper.app.AppContainer
import com.keymapper.app.bluetooth.ConnectionState
import com.keymapper.app.mapping.MappingAdapter
import com.keymapper.app.model.HidButtonEvent
import com.keymapper.app.service.KeyMapperAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), KeyMapperAccessibilityService.KeyListener {

    private lateinit var devicesRecycler: RecyclerView
    private lateinit var tvDevicesStatus: TextView
    private lateinit var tvDebugLog: TextView
    private lateinit var tvDebugTitle: TextView
    private lateinit var deviceAdapter: DeviceAdapter
    private var app: AppContainer? = null

    private var PERMISSION_REQUEST_CODE = 100
    private val debugLog = StringBuilder()
    private var motionCount = 0
    private var motionAxisCount = 0
    private var keyCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildProgrammaticUI())

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val container = AppContainer.getOrCreate(this@MainActivity)
                app = container
                requestPermissions()
                withContext(Dispatchers.Main) {
                    setupDeviceTab()
                }
                startEventCollectors()
                refreshDeviceList()
            } catch (e: Throwable) {
                Log.e(TAG, "init failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        KeyMapperAccessibilityService.addKeyListener(this)
        refreshDiagnosticPanel()
    }

    override fun onPause() {
        super.onPause()
        KeyMapperAccessibilityService.removeKeyListener(this)
    }

    override fun onKeyCaptured(event: HidButtonEvent, source: String, deviceName: String?, rawKeyCode: Int) {
        runOnUiThread {
            keyCount++
            val device = deviceName ?: source
            appendDebug("[A11yKEY#$keyCount] keyCode=$rawKeyCode action=${if (event.isPressed) "DOWN" else "UP"} src=$source dev=$device -> ${event.buttonName}/${event.buttonId}")
        }
    }

    override fun onGenericMotionEvent(ev: MotionEvent?): Boolean {
        ev ?: return super.onGenericMotionEvent(null)
        val act = when (ev.action) {
            MotionEvent.ACTION_DOWN -> "DOWN"
            MotionEvent.ACTION_UP -> "UP"
            MotionEvent.ACTION_MOVE -> "MOVE"
            else -> "ACT${ev.action}"
        }
        val axes = mutableListOf<String>()
        for (axis in intArrayOf(MotionEvent.AXIS_X, MotionEvent.AXIS_Y, MotionEvent.AXIS_Z,
                                MotionEvent.AXIS_RX, MotionEvent.AXIS_RY, MotionEvent.AXIS_RZ,
                                MotionEvent.AXIS_LTRIGGER, MotionEvent.AXIS_RTRIGGER,
                                MotionEvent.AXIS_HAT_X, MotionEvent.AXIS_HAT_Y)) {
            try {
                val v = ev.getAxisValue(axis)
                if (v != 0f) axes.add("ax$axis=${"%.2f".format(v)}")
            } catch (_: Exception) {}
        }
        if (axes.isNotEmpty() || ev.action != MotionEvent.ACTION_MOVE) {
            motionAxisCount++
            appendDebug("[MOTION#$motionAxisCount] $act source=${ev.source} ${axes.joinToString(" ")}")
        }
        return super.onGenericMotionEvent(ev)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        event ?: return super.dispatchKeyEvent(null)
        if (event.repeatCount > 0) return super.dispatchKeyEvent(event)
        keyCount++
        val btn = KeyMapperAccessibilityService.keyEventToButton(event)
        val action = if (event.action == KeyEvent.ACTION_DOWN) "DOWN" else "UP  "
        val source = KeyMapperAccessibilityService.sourceToString(event.source)
        val mapping = btn?.let { "-> ${it.buttonName}/${it.buttonId}" } ?: ""
        appendDebug("[KEY#$keyCount] $action keyCode=${event.keyCode} src=$source flags=${event.flags} $mapping")
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev ?: return super.dispatchTouchEvent(null)
        if (ev.action == MotionEvent.ACTION_MOVE) return super.dispatchTouchEvent(ev)
        motionCount++
        val act = when (ev.action) {
            MotionEvent.ACTION_DOWN -> "DOWN"
            MotionEvent.ACTION_UP -> "UP"
            MotionEvent.ACTION_CANCEL -> "CANCEL"
            else -> "OTHER(${ev.action})"
        }
        appendDebug("[TOUCH#$motionCount] $act x=${ev.x.toInt()},y=${ev.y.toInt()} source=${ev.source} pointerCount=${ev.pointerCount}")
        return super.dispatchTouchEvent(ev)
    }

    private fun appendDebug(line: String) {
        debugLog.insert(0, line + "\n")
        if (debugLog.length > 4000) debugLog.setLength(4000)
        val log = debugLog.toString()
        runOnUiThread {
            try {
                tvDebugLog.text = log
                val a11yCount = KeyMapperAccessibilityService.getA11yKeyCount()
                tvDebugTitle.text = "🔍 诊断面板 (SDK=${Build.VERSION.SDK_INT}) [A11yKEY=$a11yCount ACTIVITY_KEY=$keyCount TOUCH=$motionCount MOTION=$motionAxisCount]"
            } catch (_: Exception) {}
        }
        Log.i(TAG, line)
    }

    private fun refreshDiagnosticPanel() {
        val a11yRunning = KeyMapperAccessibilityService.isRunning()
        val a11yCount = KeyMapperAccessibilityService.getA11yKeyCount()
        val info = "📱 Android SDK: ${Build.VERSION.SDK_INT}\n" +
                   "♿ 无障碍服务: ${if (a11yRunning) "运行中 ✅" else "未运行 ❌"} (已捕获按键: $a11yCount)\n"
        appendDebug("=== 诊断 ===")
        appendDebug(info)
    }

    private fun dumpDevices() {
        val info = KeyMapperAccessibilityService.enumerateInputDevices()
        appendDebug(info)
        Toast.makeText(this, "已刷新输入设备列表，看调试面板", Toast.LENGTH_SHORT).show()
    }

    private fun startEventCollectors() {
        val container = app ?: return

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                container.bluetoothController.buttonEvents.collect { event ->
                    try { container.mappingEngine.onButtonEvent(event) }
                    catch (e: Exception) { Log.e(TAG, "button dispatch failed", e) }
                }
            } catch (e: Exception) { Log.e(TAG, "buttonEvents collect failed", e) }
        }

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                container.mappingRepository.mappings.collect { list ->
                    container.mappingEngine.updateActiveMappings(list)
                }
            } catch (e: Exception) { Log.e(TAG, "mappings collect failed", e) }
        }

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                container.bluetoothController.connectionState.collect { state ->
                    withContext(Dispatchers.Main) {
                        val dev = container.bluetoothController.connectedDevice.value
                        when (state) {
                            ConnectionState.CONNECTED -> {
                                tvDevicesStatus.text = "✅ 已选中并连接：${dev?.name ?: "?"}"
                                tvDevicesStatus.setTextColor(Color.parseColor("#FF4CAF50"))
                            }
                            ConnectionState.CONNECTING -> {
                                tvDevicesStatus.text = "⏳ 连接中…"
                                tvDevicesStatus.setTextColor(Color.parseColor("#FFFF9800"))
                            }
                            ConnectionState.DISCONNECTED -> {
                                tvDevicesStatus.text = "未选中设备"
                                tvDevicesStatus.setTextColor(Color.parseColor("#FF9E9E9E"))
                            }
                        }
                        deviceAdapter.updateConnectionState(state)
                    }
                }
            } catch (e: Exception) { Log.e(TAG, "connectionState collect failed", e) }
        }

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                container.bluetoothController.connectedDevice.collect {
                    withContext(Dispatchers.Main) { deviceAdapter.notifyDataSetChanged() }
                }
            } catch (e: Exception) { Log.e(TAG, "connectedDevice collect failed", e) }
        }
    }

    private fun setupDeviceTab() {
        val container = app ?: return
        deviceAdapter = DeviceAdapter(
            selectedAddressProvider = {
                container.bluetoothController.connectedDevice.value?.address
            },
            onSelect = { address ->
                container.bluetoothController.selectDevice(address)
                deviceAdapter.notifyDataSetChanged()
                Toast.makeText(this, "已选中手柄，按手柄键看调试面板", Toast.LENGTH_LONG).show()
            },
            onUnselect = {
                container.bluetoothController.unselectDevice()
                deviceAdapter.notifyDataSetChanged()
            }
        )
        devicesRecycler.adapter = deviceAdapter
    }

    private fun refreshDeviceList() {
        val container = app ?: return
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val devices = container.bluetoothController.getCurrentlyConnectedDevices()
                withContext(Dispatchers.Main) {
                    deviceAdapter.submitList(devices)
                    if (devices.isEmpty()) {
                        tvDevicesStatus.text = "没有发现已配对的蓝牙设备\n请先在系统蓝牙设置里配对手柄"
                        tvDevicesStatus.setTextColor(Color.parseColor("#FFFF9800"))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "refresh failed", e)
            }
        }
    }

    private fun requestPermissions() {
        val perms = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms += Manifest.permission.BLUETOOTH_SCAN
            perms += Manifest.permission.BLUETOOTH_CONNECT
        }
        perms += Manifest.permission.ACCESS_FINE_LOCATION
        perms += Manifest.permission.ACCESS_COARSE_LOCATION

        val need = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (need.isNotEmpty()) {
            runOnUiThread {
                ActivityCompat.requestPermissions(this, need.toTypedArray(), PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun buildProgrammaticUI(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FFF5F5F5"))
        }

        val toolbar = Toolbar(this).apply {
            setBackgroundColor(Color.parseColor("#FF3F51B5"))
            setTitleTextColor(Color.WHITE)
            title = "KeyMapper"
        }
        root.addView(toolbar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)))
        setSupportActionBar(toolbar)

        val statusBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setGravity(android.view.Gravity.CENTER_VERTICAL)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            setBackgroundColor(Color.parseColor("#FFFDE7"))
        }

        val tvA11yStatus = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.parseColor("#FF6F00"))
        }
        val btnGoA11y = AppCompatButton(this).apply {
            text = "去开启"
            setOnClickListener {
                try { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
                catch (_: Exception) { Toast.makeText(this@MainActivity, "手动打开系统设置 → 无障碍", Toast.LENGTH_LONG).show() }
            }
        }

        val refreshStatusBar = Runnable {
            val running = KeyMapperAccessibilityService.isRunning()
            val a11yCount = KeyMapperAccessibilityService.getA11yKeyCount()
            if (running) {
                statusBar.setBackgroundColor(Color.parseColor("#FFF1F8E9"))
                tvA11yStatus.text = "✅ 无障碍服务已开启 (A11yKEY=$a11yCount)"
                tvA11yStatus.setTextColor(Color.parseColor("#FF2E7D32"))
                btnGoA11y.visibility = View.GONE
            } else {
                statusBar.setBackgroundColor(Color.parseColor("#FFFFEBEE"))
                tvA11yStatus.text = "⚠️ 无障碍服务未开启！这是捕获手柄按键的关键"
                tvA11yStatus.setTextColor(Color.parseColor("#FFC62828"))
                btnGoA11y.visibility = View.VISIBLE
            }
        }
        statusBar.addView(tvA11yStatus, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        statusBar.addView(btnGoA11y)

        root.addView(statusBar)
        root.post(object : Runnable {
            override fun run() {
                refreshStatusBar.run()
                root.postDelayed(this, 1500)
            }
        })

        val scrollView = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        val diagTitle = TextView(this).apply {
            text = "🔧 诊断：SDK ${Build.VERSION.SDK_INT}"
            textSize = 13f
            setTextColor(Color.parseColor("#FF1565C0"))
            setPadding(0, dp(4), 0, dp(4))
        }
        content.addView(diagTitle)

        val deviceBtnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val btnDumpDev = AppCompatButton(this).apply {
            text = "🔌 列出输入设备"
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { dumpDevices() }
        }
        val btnRefreshDiag = AppCompatButton(this).apply {
            text = "🔄 刷新诊断"
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(8) }
            setOnClickListener { refreshDiagnosticPanel() }
        }
        deviceBtnRow.addView(btnDumpDev)
        deviceBtnRow.addView(btnRefreshDiag)
        content.addView(deviceBtnRow)

        val devicesTitle = TextView(this).apply {
            text = "已配对的蓝牙设备（点『选中』指定）"
            textSize = 16f
            setTextColor(Color.parseColor("#FF212121"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(12), 0, dp(4))
        }
        content.addView(devicesTitle)

        tvDevicesStatus = TextView(this).apply {
            text = "未选中设备"
            textSize = 13f
            setTextColor(Color.parseColor("#FF9E9E9E"))
            setPadding(0, dp(2), 0, dp(6))
        }
        content.addView(tvDevicesStatus)

        devicesRecycler = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        content.addView(devicesRecycler, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(180)))

        val refreshBtn = AppCompatButton(this).apply {
            text = "🔄 刷新设备列表"
            setOnClickListener { refreshDeviceList() }
        }
        content.addView(refreshBtn)

        val addBtn = AppCompatButton(this).apply {
            text = "+ 添加新映射"
            setOnClickListener { startActivity(Intent(this@MainActivity, MappingConfigActivity::class.java)) }
        }
        content.addView(addBtn, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        tvDebugTitle = TextView(this).apply {
            text = "🔍 诊断面板（加载中...）"
            textSize = 14f
            setTextColor(Color.parseColor("#FFD32F2F"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(16), 0, dp(4))
        }
        content.addView(tvDebugTitle)

        tvDebugLog = TextView(this).apply {
            text = "加载中... 点上面的『列出输入设备』和『刷新诊断』按钮\n然后按手柄按键看这里有什么变化\n"
            textSize = 11f
            setTextColor(Color.parseColor("#FF212121"))
            setBackgroundColor(Color.WHITE)
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        content.addView(tvDebugLog, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(220)))

        val clearDebugBtn = AppCompatButton(this).apply {
            text = "清除调试面板"
            setOnClickListener {
                debugLog.clear()
                tvDebugLog.text = ""
                keyCount = 0; motionCount = 0; motionAxisCount = 0
            }
        }
        content.addView(clearDebugBtn)

        scrollView.addView(content)
        root.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        return root
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    companion object { private const val TAG = "MainActivity" }
}
