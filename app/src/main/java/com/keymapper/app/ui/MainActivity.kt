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
import com.keymapper.app.floating.FloatingWindowManager
import com.keymapper.app.mapping.MappingAdapter
import com.keymapper.app.model.HidButtonEvent
import com.keymapper.app.service.KeyMapperAccessibilityService
import com.keymapper.app.service.MappingForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), KeyMapperAccessibilityService.KeyListener {

    private lateinit var devicesRecycler: RecyclerView
    private lateinit var tvDevicesStatus: TextView
    private lateinit var tvDebugLog: TextView
    private lateinit var tvDebugTitle: TextView
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var tvProfileLabel: TextView
    private lateinit var tvProfileName: TextView
    private lateinit var btnPrevP: AppCompatButton
    private lateinit var btnNextP: AppCompatButton
    private lateinit var btnNewP: AppCompatButton
    private lateinit var btnDelP: AppCompatButton
    private lateinit var tvMappingCount: TextView
    private lateinit var tvEmptyHint: TextView
    private lateinit var mappingAdapter: MappingAdapter
    private var app: AppContainer? = null

    private var PERMISSION_REQUEST_CODE = 100
    private val debugLog = StringBuilder()
    private var keyCount = 0

    private var floatBtn: AppCompatButton? = null

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

                // ---- 监听 Repository Flow 自动更新（必须在 app 赋值之后！）----
                launch {
                    app!!.mappingRepository.mappings.collect { list ->
                        withContext(Dispatchers.Main) {
                            Log.i(TAG, "mappings flow update: ${list.size} items")
                            mappingAdapter.submitList(list)
                            tvMappingCount.text = "  本方案共 ${list.size} 条映射"
                            tvEmptyHint.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }
                }

                launch {
                    try {
                        app!!.mappingRepository.currentProfileFlow.collect {
                            Log.i(TAG, "profile changed to: $it")
                            refreshAll()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "profiles flow collect failed", e)
                    }
                }

                refreshAll()
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

    private val refreshRunnable = object : Runnable {
        override fun run() {
            refreshDiagnosticPanel()
            handler.postDelayed(this, 2000)
        }
    }
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    override fun onResume() {
        super.onResume()
        KeyMapperAccessibilityService.addKeyListener(this)
        refreshDiagnosticPanel()
        refreshAll()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        KeyMapperAccessibilityService.removeKeyListener(this)
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onKeyCaptured(event: HidButtonEvent, source: String, deviceName: String?, rawKeyCode: Int) {
        runOnUiThread {
            keyCount++
            val device = deviceName ?: source
            appendDebug("[A11yKEY#$keyCount] keyCode=$rawKeyCode action=${if (event.isPressed) "DOWN" else "UP"} src=$source dev=$device -> ${event.buttonName}/${event.buttonId}")
        }
    }

    override fun onMotionCaptured(button: String, source: String, deviceName: String?) {
        runOnUiThread {
            appendDebug("[A11yMOTION] btn=$button src=$source dev=$deviceName")
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
            appendDebug("[MOTION] $act source=${ev.source} ${axes.joinToString(" ")}")
        }
        return super.onGenericMotionEvent(ev)
    }

    private var controllerTouchCount = 0

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev ?: return super.dispatchTouchEvent(null)
        val dev = ev.device
        val devName = dev?.name ?: "(无设备)"
        val source = KeyMapperAccessibilityService.sourceToString(ev.source)
        val isControllerTouch = devName.contains("R1S", true)
                || devName.contains("Gamepad", true)
                || devName.contains("Controller", true)
                || source.contains("GAMEPAD")

        if (isControllerTouch) {
            controllerTouchCount++
            if (ev.action == MotionEvent.ACTION_DOWN) {
                val rx = dev?.getMotionRange(MotionEvent.AXIS_X)
                val ry = dev?.getMotionRange(MotionEvent.AXIS_Y)
                val rngX = rx?.let { it.max - it.min } ?: 4095f
                val rngY = ry?.let { it.max - it.min } ?: 4095f
                val cx = rx?.let { (it.min + it.max) / 2f } ?: (rngX / 2f)
                val cy = ry?.let { (it.min + it.max) / 2f } ?: (rngY / 2f)
                val nx = (ev.rawX - cx) / (rngX / 2f)
                val ny = (ev.rawY - cy) / (rngY / 2f)
                appendDebug("[TOUCH#$controllerTouchCount] dev=\"$devName\" src=$source act=DOWN raw=(${ev.rawX.toInt()},${ev.rawY.toInt()}) norm=(${nx.toInt()},${ny.toInt()}) ranges:X=[${rx?.min}..${rx?.max}] Y=[${ry?.min}..${ry?.max}]")
            }
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        event ?: return super.dispatchKeyEvent(null)
        if (event.repeatCount > 0) return super.dispatchKeyEvent(event)
        keyCount++
        val btn = KeyMapperAccessibilityService.keyEventToButton(event)
        val action = if (event.action == KeyEvent.ACTION_DOWN) "DOWN" else "UP  "
        val source = KeyMapperAccessibilityService.sourceToString(event.source)
        val mapping = btn?.let { "-> ${it.buttonName}/${it.buttonId}" } ?: ""
        appendDebug("[KEY#$keyCount] $action keyCode=${event.keyCode} src=$source $mapping")
        return super.dispatchKeyEvent(event)
    }

    private fun appendDebug(line: String) {
        debugLog.insert(0, line + "\n")
        if (debugLog.length > 4000) debugLog.setLength(4000)
        val log = debugLog.toString()
        runOnUiThread {
            try {
                tvDebugLog.text = log
                val a11yKey = KeyMapperAccessibilityService.getA11yKeyCount()
                tvDebugTitle.text = "🔍 诊断面板 (SDK=${Build.VERSION.SDK_INT}) [A11yKEY=$a11yKey ACT_KEY=$keyCount]"
            } catch (_: Exception) {}
        }
        Log.i(TAG, line)
    }

    private fun refreshDiagnosticPanel() {
        val running = KeyMapperAccessibilityService.isRunning()
        val a11yKey = KeyMapperAccessibilityService.getA11yKeyCount()
        val a11yMotion = KeyMapperAccessibilityService.getA11yMotionCount()
        val flagsInfo = KeyMapperAccessibilityService.getFlagsSummary()
        val imeInfo = KeyMapperAccessibilityService.getImeStatus()
        val devices = KeyMapperAccessibilityService.getInputDeviceSummary()
        val lastKey = KeyMapperAccessibilityService.getLastKeyLog()

        val sb = StringBuilder()
        sb.appendLine("=== 📊 完整诊断 v1.0.21 ===")
        sb.appendLine("📱 Android SDK: ${Build.VERSION.SDK_INT}  (API ${Build.VERSION.SDK_INT})")
        sb.appendLine("♿ 无障碍服务: ${if (running) "✅ 运行中" else "❌ 未运行"}")
        sb.appendLine("🔢 AccessibilityService 收到按键: $a11yKey  摇杆: $a11yMotion")
        sb.appendLine("🏷️ Activity dispatchKeyEvent 收到按键: $keyCount")
        sb.appendLine()
        sb.appendLine("📋 无障碍 flags:")
        sb.appendLine("   $flagsInfo")
        sb.appendLine()
        if (imeInfo.isNotEmpty()) {
            sb.appendLine("🔑 IME通道: $imeInfo")
            sb.appendLine()
        }
        sb.appendLine("🔌 输入设备列表:")
        for (line in devices.lines()) sb.appendLine("   $line")
        sb.appendLine()
        if (lastKey.isNotEmpty()) {
            sb.appendLine("⏱ 最后收到的按键: $lastKey")
        }

        runOnUiThread {
            tvDebugLog.text = sb.toString()
            tvDebugTitle.text = "🔍 实时诊断面板 (v1.0.21)"
        }
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
                                tvDevicesStatus.text = "✅ 已选中手柄：${dev?.name ?: "?"}"
                                tvDevicesStatus.setTextColor(Color.parseColor("#FF4CAF50"))
                            }
                            ConnectionState.CONNECTING -> {
                                tvDevicesStatus.text = "⏳ 连接中…"
                                tvDevicesStatus.setTextColor(Color.parseColor("#FFFF9800"))
                            }
                            ConnectionState.DISCONNECTED -> {
                                tvDevicesStatus.text = "未选中手柄"
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

    private fun requestOverlayAndStart() {
        if (MappingForegroundService.isRunning()) {
            MappingForegroundService.stop(this)
            Toast.makeText(this, "⏹ 悬浮窗已停止", Toast.LENGTH_SHORT).show()
            refreshAll()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请在弹出的设置页中允许『显示在其他应用上层』", Toast.LENGTH_LONG).show()
            startActivity(Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")
            ))
            return
        }
        MappingForegroundService.start(this)
        Toast.makeText(this, "🎮 悬浮窗已开启，切换到目标 app 试试", Toast.LENGTH_LONG).show()
        refreshAll()
    }

    private fun buildProgrammaticUI(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FFF5F5F5"))
        }

        val toolbar = Toolbar(this).apply {
            setBackgroundColor(Color.parseColor("#FF3F51B5"))
            setTitleTextColor(Color.WHITE)
            title = "K2ER 手柄映射"
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

        val refreshStatusBar = object : Runnable {
            override fun run() {
                val running = KeyMapperAccessibilityService.isRunning()
                val a11yKey = KeyMapperAccessibilityService.getA11yKeyCount()
                if (running) {
                    statusBar.setBackgroundColor(Color.parseColor("#FFF1F8E9"))
                    tvA11yStatus.text = "✅ 无障碍服务已开启 | 按键捕获: $a11yKey"
                    tvA11yStatus.setTextColor(Color.parseColor("#FF2E7D32"))
                    btnGoA11y.visibility = View.GONE
                } else {
                    statusBar.setBackgroundColor(Color.parseColor("#FFFFEBEE"))
                    tvA11yStatus.text = "⚠️ 请先开启无障碍服务（这是按键捕获的关键）"
                    tvA11yStatus.setTextColor(Color.parseColor("#FFC62828"))
                    btnGoA11y.visibility = View.VISIBLE
                }
                root.postDelayed(this, 2000)
            }
        }
        refreshStatusBar.run()

        statusBar.addView(tvA11yStatus, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        statusBar.addView(btnGoA11y)
        root.addView(statusBar)

        val scrollView = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        content.addView(TextView(this).apply {
            text = "已配对的蓝牙手柄（点『选中』指定）"
            textSize = 16f
            setTextColor(Color.parseColor("#FF212121"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(12), 0, dp(4))
        })

        tvDevicesStatus = TextView(this).apply {
            text = "未选中手柄"
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

        // ---- Profile 管理 + 映射列表 ----
        tvProfileLabel = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.parseColor("#FF1976D2"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(16), 0, dp(4))
        }
        content.addView(tvProfileLabel)

        val profileRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        btnPrevP = AppCompatButton(this).apply {
            text = "◀"
            layoutParams = LinearLayout.LayoutParams(dp(48), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        tvProfileName = TextView(this).apply {
            textSize = 15f; setTextColor(Color.parseColor("#FF1976D2"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            text = "加载中…"
            setBackgroundColor(Color.parseColor("#FFF0F4FF"))
            setPadding(dp(8), dp(8), dp(8), dp(8))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(4); marginEnd = dp(4) }
        }
        btnNextP = AppCompatButton(this).apply {
            text = "▶"
            layoutParams = LinearLayout.LayoutParams(dp(48), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        profileRow.addView(btnPrevP); profileRow.addView(tvProfileName); profileRow.addView(btnNextP)
        content.addView(profileRow)

        val profileActions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(4), 0, dp(4))
        }
        btnNewP = AppCompatButton(this).apply {
            text = "➕新建"
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        btnDelP = AppCompatButton(this).apply {
            text = "🗑删除"
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(8) }
        }
        profileActions.addView(btnNewP); profileActions.addView(btnDelP)
        content.addView(profileActions)

        tvMappingCount = TextView(this).apply {
            textSize = 12f; setTextColor(Color.parseColor("#FF616161"))
            setPadding(0, dp(4), 0, dp(4))
        }
        content.addView(tvMappingCount)

        val mappingListFrame = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }
        val mappingListRecycler = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        tvEmptyHint = TextView(this).apply {
            text = "👇 还没有映射，点下方『➕添加新映射』开始配置"
            textSize = 12f
            setTextColor(Color.parseColor("#FF9E9E9E"))
            gravity = android.view.Gravity.CENTER
            setPadding(dp(8), dp(24), dp(8), dp(24))
            visibility = View.VISIBLE
        }
        mappingAdapter = MappingAdapter(
            onToggle = { cfg, enabled ->
                lifecycleScope.launch(Dispatchers.Default) {
                    try {
                        app?.mappingRepository?.update(cfg.copy(enabled = enabled))
                    } catch (e: Exception) {
                        Log.e(TAG, "toggle failed", e)
                    }
                }
            },
            onDelete = { cfg ->
                lifecycleScope.launch(Dispatchers.Default) {
                    try {
                        app?.mappingRepository?.remove(cfg.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "delete failed", e)
                    }
                }
            },
            onEdit = { cfg ->
                startActivity(Intent(this@MainActivity, MappingConfigActivity::class.java).apply {
                    putExtra(MappingConfigActivity.EXTRA_MAPPING_ID, cfg.id)
                })
            }
        )
        mappingListRecycler.adapter = mappingAdapter
        mappingListFrame.addView(tvEmptyHint)
        mappingListFrame.addView(mappingListRecycler)
        content.addView(mappingListFrame, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        btnPrevP.setOnClickListener {
            val repo = app?.mappingRepository
            if (repo == null) { Toast.makeText(this, "尚未初始化，请稍等", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            lifecycleScope.launch(Dispatchers.Default) {
                try {
                    val profiles = repo.listProfiles()
                    val current = repo.currentProfile()
                    val idx = profiles.indexOf(current).coerceAtLeast(0)
                    if (idx > 0) {
                        repo.switchProfile(profiles[idx - 1])
                        Log.i(TAG, "switched to ${profiles[idx - 1]}")
                    }
                    refreshAll()
                } catch (e: Exception) {
                    Log.e(TAG, "prev profile failed", e)
                    withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "切换失败: ${e.message}", Toast.LENGTH_SHORT).show() }
                }
            }
        }
        btnNextP.setOnClickListener {
            val repo = app?.mappingRepository
            if (repo == null) { Toast.makeText(this, "尚未初始化，请稍等", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            lifecycleScope.launch(Dispatchers.Default) {
                try {
                    val profiles = repo.listProfiles()
                    val current = repo.currentProfile()
                    val idx = profiles.indexOf(current).coerceAtLeast(0)
                    if (idx < profiles.size - 1) {
                        repo.switchProfile(profiles[idx + 1])
                        Log.i(TAG, "switched to ${profiles[idx + 1]}")
                    }
                    refreshAll()
                } catch (e: Exception) {
                    Log.e(TAG, "next profile failed", e)
                    withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "切换失败: ${e.message}", Toast.LENGTH_SHORT).show() }
                }
            }
        }
        btnNewP.setOnClickListener {
            val repo = app?.mappingRepository
            if (repo == null) { Toast.makeText(this, "尚未初始化，请稍等", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val edit = android.widget.EditText(this).apply { hint = "方案名，如：抖音/原神/浏览器" }
            android.app.AlertDialog.Builder(this)
                .setTitle("新建配置方案")
                .setView(edit)
                .setPositiveButton("创建") { _, _ ->
                    val name = edit.text?.toString()?.trim().orEmpty()
                    if (name.isNotEmpty()) {
                        lifecycleScope.launch(Dispatchers.Default) {
                            try {
                                repo.addProfile(name)
                                repo.switchProfile(name)
                                Log.i(TAG, "created and switched to $name")
                                refreshAll()
                            } catch (e: Exception) {
                                Log.e(TAG, "new profile failed", e)
                                withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "创建失败: ${e.message}", Toast.LENGTH_SHORT).show() }
                            }
                        }
                    }
                }
                .setNegativeButton("取消", null).show()
        }
        btnDelP.setOnClickListener {
            val repo = app?.mappingRepository
            if (repo == null) { Toast.makeText(this, "尚未初始化，请稍等", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            lifecycleScope.launch(Dispatchers.Default) {
                try {
                    val name = repo.currentProfile()
                    if (name == "默认") {
                        withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "不能删除『默认』方案", Toast.LENGTH_SHORT).show() }
                        return@launch
                    }
                    withContext(Dispatchers.Main) {
                        android.app.AlertDialog.Builder(this@MainActivity)
                            .setTitle("删除方案")
                            .setMessage("确定删除『$name』？")
                            .setPositiveButton("删除") { _, _ ->
                                lifecycleScope.launch(Dispatchers.Default) {
                                    try {
                                        repo.deleteProfile(name)
                                        repo.switchProfile("默认")
                                        refreshAll()
                                    } catch (e: Exception) {
                                        Log.e(TAG, "delete profile failed", e)
                                        withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "删除失败: ${e.message}", Toast.LENGTH_SHORT).show() }
                                    }
                                }
                            }
                            .setNegativeButton("取消", null).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "del profile failed", e)
                    withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "删除失败: ${e.message}", Toast.LENGTH_SHORT).show() }
                }
            }
        }

        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val addBtn = AppCompatButton(this).apply {
            text = "+ 添加新映射"
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { startActivity(Intent(this@MainActivity, MappingConfigActivity::class.java)) }
        }
        floatBtn = AppCompatButton(this).apply {
            text = "🎮 开启悬浮"
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(8) }
            setOnClickListener { requestOverlayAndStart() }
        }
        btnRow.addView(addBtn)
        btnRow.addView(floatBtn)
        content.addView(btnRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        tvDebugTitle = TextView(this).apply {
            text = "🔍 诊断面板"
            textSize = 14f
            setTextColor(Color.parseColor("#FFD32F2F"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(16), 0, dp(4))
        }
        content.addView(tvDebugTitle)

        tvDebugLog = TextView(this).apply {
            text = "按手柄按键，看这里有没有事件输出...\n"
            textSize = 11f
            setTextColor(Color.parseColor("#FF212121"))
            setBackgroundColor(Color.WHITE)
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        content.addView(tvDebugLog, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(220)))

        val clearDebugBtn = AppCompatButton(this).apply {
            text = "🔄 重置并刷新诊断"
            setOnClickListener {
                debugLog.clear()
                keyCount = 0
                refreshDiagnosticPanel()
            }
        }
        content.addView(clearDebugBtn)

        scrollView.addView(content)
        root.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        return root
    }

    private fun refreshAll() {
        val repo = app?.mappingRepository ?: run {
            Log.w(TAG, "app not ready, skip refreshAll")
            return
        }
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val profiles = repo.listProfiles()
                val current = repo.currentProfile()
                val idx = profiles.indexOf(current).coerceAtLeast(0)
                val mappings = repo.getCurrent()
                Log.i(TAG, "refreshAll: profiles=$profiles current=$current idx=$idx mappings=${mappings.size}")
                withContext(Dispatchers.Main) {
                    tvProfileLabel.text = "📋 配置方案（${profiles.size}套）"
                    tvProfileName.text = "  ${profiles[idx]}  "
                    btnPrevP.isEnabled = idx > 0
                    btnNextP.isEnabled = idx < profiles.size - 1
                    tvMappingCount.text = "  本方案共 ${mappings.size} 条映射"
                    mappingAdapter.submitList(mappings)
                    tvEmptyHint.visibility = if (mappings.isEmpty()) View.VISIBLE else View.GONE
                    floatBtn?.text = if (MappingForegroundService.isRunning()) "⏹ 停止悬浮" else "🎮 开启悬浮"
                }
            } catch (e: Exception) {
                Log.e(TAG, "refreshAll failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "刷新方案失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    companion object { private const val TAG = "MainActivity" }
}
