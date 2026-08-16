package com.keymapper.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.keymapper.app.AppContainer
import com.keymapper.app.model.HidButtonEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class KeyMapperAccessibilityService : AccessibilityService() {

    interface KeyListener {
        fun onKeyCaptured(event: HidButtonEvent, source: String, deviceName: String?, rawKeyCode: Int)
        fun onMotionCaptured(button: String, source: String, deviceName: String?)
    }

    companion object {
        fun getTouchKeyCount(): Int = instance?.gamepadTracker?.touchKeyCount ?: 0
        private const val TAG = "K2ER-Service"
        @Volatile
        var instance: KeyMapperAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null

        @Volatile
        private var a11yKeyCount: Int = 0
        fun getA11yKeyCount(): Int = a11yKeyCount

        @Volatile
        private var a11yMotionCount: Int = 0
        fun getA11yMotionCount(): Int = a11yMotionCount

        @Volatile
        private var flagsSummary: String = ""
        fun getFlagsSummary() = flagsSummary

        @Volatile
        private var inputDeviceSummary: String = ""
        fun getInputDeviceSummary() = inputDeviceSummary

        @Volatile
        private var lastKeyLog: String = ""
        fun getLastKeyLog() = lastKeyLog

        @Volatile
        @JvmStatic
        var currentPackageName: String? = null
            private set

        @Volatile
        @JvmStatic
        var currentPackageLabel: String? = null
            private set

        @JvmStatic
        fun refreshForegroundPackage() {
            val svc = instance ?: return
            runCatching {
                val windows = svc.windows
                if (windows.isNullOrEmpty()) return@runCatching
                val top = windows
                    .sortedByDescending { it.layer }
                    .firstOrNull { w -> w.isActive && w.isFocused }
                    ?: windows.sortedByDescending { it.layer }.firstOrNull()
                val root = top?.root
                val pkg = root?.packageName?.toString()
                if (!pkg.isNullOrBlank() && pkg != "com.keymapper.app") {
                    if (pkg != currentPackageName) {
                        currentPackageName = pkg
                        currentPackageLabel = runCatching {
                            val info = svc.packageManager.getApplicationInfo(pkg, 0)
                            svc.packageManager.getApplicationLabel(info).toString()
                        }.getOrNull()
                        Log.i(TAG, "📱 getWindows 刷新前台: $pkg (${currentPackageLabel ?: "?"})")
                    }
                }
            }
        }

        private val keyListeners = mutableListOf<KeyListener>()

        fun addKeyListener(listener: KeyListener) {
            synchronized(keyListeners) {
                if (!keyListeners.contains(listener)) keyListeners.add(listener)
            }
        }

        fun removeKeyListener(listener: KeyListener) {
            synchronized(keyListeners) {
                keyListeners.remove(listener)
            }
        }

        private val KEYCODE_TO_BUTTON = mapOf(
            KeyEvent.KEYCODE_BUTTON_A      to Pair("BTN_A",      "A"),
            KeyEvent.KEYCODE_BUTTON_B      to Pair("BTN_B",      "B"),
            KeyEvent.KEYCODE_BUTTON_X      to Pair("BTN_X",      "X"),
            KeyEvent.KEYCODE_BUTTON_Y      to Pair("BTN_Y",      "Y"),
            KeyEvent.KEYCODE_BUTTON_L1     to Pair("BTN_L1",     "L1"),
            KeyEvent.KEYCODE_BUTTON_R1     to Pair("BTN_R1",     "R1"),
            KeyEvent.KEYCODE_BUTTON_L2     to Pair("BTN_L2",     "L2"),
            KeyEvent.KEYCODE_BUTTON_R2     to Pair("BTN_R2",     "R2"),
            KeyEvent.KEYCODE_BUTTON_SELECT to Pair("BTN_SELECT", "SELECT"),
            KeyEvent.KEYCODE_BUTTON_START  to Pair("BTN_START",  "START"),
            KeyEvent.KEYCODE_BUTTON_THUMBL to Pair("BTN_L3",     "L3"),
            KeyEvent.KEYCODE_BUTTON_THUMBR to Pair("BTN_R3",     "R3"),
            KeyEvent.KEYCODE_BUTTON_MODE   to Pair("BTN_HOME",   "HOME"),
            KeyEvent.KEYCODE_DPAD_UP       to Pair("DPAD_UP",    "上"),
            KeyEvent.KEYCODE_DPAD_DOWN     to Pair("DPAD_DOWN",  "下"),
            KeyEvent.KEYCODE_DPAD_LEFT     to Pair("DPAD_LEFT",  "左"),
            KeyEvent.KEYCODE_DPAD_RIGHT    to Pair("DPAD_RIGHT", "右"),
        )

        fun keyEventToButton(event: KeyEvent): HidButtonEvent {
            val devName = try { event.device?.name } catch (_: Exception) { null }
            val pair = KEYCODE_TO_BUTTON[event.keyCode]
                ?: return HidButtonEvent("RAW_${event.keyCode}", "键#${event.keyCode}", event.action == KeyEvent.ACTION_DOWN, deviceName = devName)
            return HidButtonEvent(pair.first, pair.second, event.action == KeyEvent.ACTION_DOWN, deviceName = devName)
        }

        fun sourceToString(source: Int): String {
            val parts = mutableListOf<String>()
            if (source and InputDevice.SOURCE_GAMEPAD != 0) parts.add("GAMEPAD")
            if (source and InputDevice.SOURCE_JOYSTICK != 0) parts.add("JOYSTICK")
            if (source and InputDevice.SOURCE_KEYBOARD != 0) parts.add("KEYBOARD")
            if (source and InputDevice.SOURCE_DPAD != 0) parts.add("DPAD")
            if (source and InputDevice.SOURCE_MOUSE != 0) parts.add("MOUSE")
            if (source and InputDevice.SOURCE_TOUCHPAD != 0) parts.add("TOUCHPAD")
            if (source and InputDevice.SOURCE_TOUCHSCREEN != 0) parts.add("TOUCHSCREEN")
            if (parts.isEmpty()) parts.add("SRC_$source")
            return parts.joinToString("|")
        }
    }

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val MY_PACKAGE = "com.keymapper.app"

    private var keyCaptureView: android.view.View? = null
    private var keyCaptureParams: WindowManager.LayoutParams? = null
    private var keyCaptureAttached = false

    private var touchStartX: Float = 0f
    private var touchStartY: Float = 0f
    private var touchDevName: String = ""
    private var touchMoved: Boolean = false

    private val gamepadTracker = object {
        private val _touchKeyCount = java.util.concurrent.atomic.AtomicInteger(0)
        val touchKeyCount get() = _touchKeyCount.get()
        fun inc() { _touchKeyCount.incrementAndGet() }

        fun processTouch(ev: MotionEvent): Boolean {
            val dev = ev.device ?: return false
            val devName = dev.name ?: ""
            val source = ev.source
            val devNameLower = devName.lowercase()
            val isGP = devNameLower.contains("r1s") || devNameLower.contains("gamepad")
                    || devNameLower.contains("controller")
                    || source and InputDevice.SOURCE_GAMEPAD != 0
            if (!isGP) return false

            val rawX = ev.rawX; val rawY = ev.rawY
            val container = runCatching { AppContainer.getOrCreate(this@KeyMapperAccessibilityService) }.getOrNull()

            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    touchStartX = rawX; touchStartY = rawY
                    touchDevName = devName; touchMoved = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (kotlin.math.abs(rawX - touchStartX) > 30f || kotlin.math.abs(rawY - touchStartY) > 30f) {
                        touchMoved = true
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val endX = rawX; val endY = rawY
                    val button = inferButtonFromTouch(touchStartX, touchStartY, endX, endY, touchMoved)
                    val btn = HidButtonEvent(button, button, true, deviceName = devName)
                    val container2 = container
                    if (container2 != null) {
                        container2.mappingEngine.onButtonEvent(btn)
                        _touchKeyCount.incrementAndGet()
                        synchronized(keyListeners) {
                            for (l in keyListeners.toList()) {
                                runCatching { l.onKeyCaptured(btn, sourceToString(source), devName, -1) }
                            }
                        }
                    }
                    Log.i(TAG, "🎮 Window触摸判定按键: $button dev=\"$devName\" start=(${touchStartX.toInt()},${touchStartY.toInt()}) end=(${endX.toInt()},${endY.toInt()}) moved=$touchMoved")
                }
            }
            return false
        }
    }
    private fun inferButtonFromTouch(startX: Float, startY: Float, endX: Float, endY: Float, moved: Boolean): String {
        val dx = endX - startX; val dy = endY - startY
        if (moved || kotlin.math.abs(dx) > 30f || kotlin.math.abs(dy) > 30f) {
            return when {
                kotlin.math.abs(dx) > kotlin.math.abs(dy) -> if (dx > 0) "DPAD_RIGHT" else "DPAD_LEFT"
                kotlin.math.abs(dy) > 30f -> if (dy > 0) "DPAD_DOWN" else "DPAD_UP"
                else -> inferStaticButton(startX, startY)
            }
        }
        return inferStaticButton(startX, startY)
    }

    private fun inferStaticButton(x: Float, y: Float): String {
        val rng = 1000f; val cx = rng / 2f; val cy = rng / 2f
        val nx = (x - cx) / cx; val ny = (y - cy) / cy
        return when {
            nx in -0.2..0.2 && ny in -0.2..0.2 -> "BTN_A"
            nx > 0.2 && ny in -0.3..0.3 -> "BTN_B"
            nx in -0.3..0.3 && ny > 0.2 -> "BTN_X"
            nx in -0.3..0.3 && ny < -0.2 -> "BTN_Y"
            nx < -0.2 && ny in -0.3..0.3 -> "BTN_START"
            else -> "BTN_${x.toInt()}_${y.toInt()}"
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.bounds
        } else {
            val dm = android.util.DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getMetrics(dm)
            android.graphics.Rect(0, 0, dm.widthPixels, dm.heightPixels)
        }
        screenWidth = metrics.width()
        screenHeight = metrics.height()

        // ✅ 关键：运行时重新设置所有 flags
        // 某些 Android 版本 / OEM ROM 会忽略 XML 里的 flagRequestFilterKeyEvents，
        // 必须在 onServiceConnected 里主动设置才能激活按键过滤。
        val info = serviceInfo
        val FLAG_DEFAULT = 0x00000001
        info.flags = (info.flags
                or FLAG_DEFAULT
                or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
                or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS)
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        info.notificationTimeout = 100
        serviceInfo = info

        val flags = serviceInfo.flags
        val hasFilterKey = flags and AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS != 0
        val hasRetrieveWin = flags and AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS != 0

        flagsSummary = "flags=0x${flags.toString(16)} " +
                "[filterKey=$hasFilterKey retrieveWin=$hasRetrieveWin]"
        Log.i(TAG, "✅ K2ER Service connected ${screenWidth}x${screenHeight}, SDK=${Build.VERSION.SDK_INT}")
        Log.i(TAG, "📋 $flagsSummary")

        // 列出所有输入设备（诊断用）
        enumerateInputDevices()

        try {
            val container = AppContainer.getOrCreate(this)

            serviceScope.launch {
                container.mappingRepository.migrateIfNeeded()
                try {
                    container.mappingRepository.mappings.collectLatest {
                        refreshEngineForCurrentApp(container)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "mappings flow collect 异常", e)
                }
            }

            serviceScope.launch {
                try {
                    container.bluetoothController.connectedDevice.collect { info ->
                        container.mappingEngine.setRequiredDevice(info?.name)
                        Log.i(TAG, "🎮 手柄连接状态: ${info?.name ?: "未连接"}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "device flow collect 异常", e)
                }
            }

            // 初始加载
            serviceScope.launch {
                refreshForegroundPackage()
                refreshEngineForCurrentApp(container)
            }

            Log.i(TAG, "🚀 引擎已挂载，APP级Profile模式")
        } catch (e: Throwable) {
            Log.e(TAG, "AppContainer 初始化失败", e)
        }

        attachKeyCaptureWindow()
    }

    @Suppress("DEPRECATION")
    private fun attachKeyCaptureWindow() {
        if (keyCaptureAttached) return
        try {
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val view = object : android.view.View(this) {
                init {
                    isFocusable = true
                    isFocusableInTouchMode = true
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setOnKeyListener { _, _, event ->
                        if (event == null) return@setOnKeyListener false
                        onKeyEvent(event)
                        true
                    }
                }

                override fun onTouchEvent(event: MotionEvent): Boolean {
                    val dev = event.device
                    val devName = dev?.name ?: "?"
                    val srcLabel = sourceToString(event.source)
                    Log.i(TAG, "🪟 Window.onTouchEvent act=${event.actionMasked} dev=\"$devName\" src=$srcLabel raw=(${event.rawX.toInt()},${event.rawY.toInt()})")
                    gamepadTracker.processTouch(event)
                    return false
                }

                override fun onGenericMotionEvent(event: MotionEvent): Boolean {
                    val dev = event.device
                    val devName = dev?.name ?: "?"
                    val srcLabel = sourceToString(event.source)
                    Log.i(TAG, "🪟 Window.onGenericMotionEvent act=${event.actionMasked} dev=\"$devName\" src=$srcLabel")
                    if (gamepadTracker.processTouch(event)) return true
                    return false
                }
            }
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            val flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                flags,
                android.graphics.PixelFormat.TRANSLUCENT
            ).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.START
            }
            wm.addView(view, params)
            keyCaptureView = view
            keyCaptureParams = params
            keyCaptureAttached = true
            view.post {
                view.requestFocus()
                view.requestFocusFromTouch()
                Log.i(TAG, "🔍 Window isFocused=${view.isFocused} isWindowFocused=${view.hasWindowFocus()}")
            }
            Log.i(TAG, "✅ 按键+触摸捕获 Window 已挂载 (全屏透明，flags=0x${flags.toString(16)})")
        } catch (e: Throwable) {
            Log.e(TAG, "❌ attachKeyCaptureWindow 失败", e)
        }
    }

    private fun detachKeyCaptureWindow() {
        if (!keyCaptureAttached) return
        try {
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            keyCaptureView?.let { wm.removeViewImmediate(it) }
        } catch (e: Throwable) {
            Log.w(TAG, "detachKeyCaptureWindow error", e)
        } finally {
            keyCaptureView = null
            keyCaptureParams = null
            keyCaptureAttached = false
        }
    }

    private fun enumerateInputDevices() {
        val ids = InputDevice.getDeviceIds()
        val sb = StringBuilder()
        sb.append("共 ${ids.size} 个输入设备:\n")
        var gamepadCount = 0
        var keyboardCount = 0
        for (id in ids) {
            val dev = InputDevice.getDevice(id) ?: continue
            val name = dev.name
            val sources = dev.sources
            val srcStr = sourceToString(sources)
            if (sources and InputDevice.SOURCE_GAMEPAD != 0) gamepadCount++
            if (sources and InputDevice.SOURCE_KEYBOARD != 0) keyboardCount++
            sb.append("  [id=$id] name=\"$name\" sources=0x${sources.toString(16)} [$srcStr]\n")
        }
        sb.append("→ Gamepad数=$gamepadCount Keyboard数=$keyboardCount")
        inputDeviceSummary = sb.toString()
        Log.i(TAG, "🔌 $inputDeviceSummary")
    }

    private suspend fun refreshEngineForCurrentApp(container: com.keymapper.app.AppContainer) {
        val pkg = currentPackageName
        val profile = pkg?.let { container.mappingRepository.currentProfileFor(it) }
        val list = container.mappingRepository.getActiveMappingsForApp(pkg)
        container.mappingEngine.updateActiveMappings(list)
        Log.i(TAG, "🔄 引擎加载: pkg=${pkg ?: "?"} profile=${profile ?: "?"} 共${list.size}条（${list.count { it.enabled }}启用）")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val type = event.eventType
        if (type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == MY_PACKAGE) return

        val oldPkg = currentPackageName
        if (pkg != oldPkg) {
            currentPackageName = pkg
            currentPackageLabel = runCatching {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
            }.getOrNull()
            Log.i(TAG, "📱 前台APP切换: $oldPkg → $pkg (${currentPackageLabel ?: "?"})")
            runCatching {
                val container = com.keymapper.app.AppContainer.getOrCreate(this)
                serviceScope.launch { refreshEngineForCurrentApp(container) }
            }
        }
    }

    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false
        if (event.repeatCount > 0) return false

        a11yKeyCount++
        val btn = keyEventToButton(event)
        val source = sourceToString(event.source)
        val deviceName = try { event.device?.name } catch (_: Exception) { null }

        val container = runCatching { AppContainer.getOrCreate(this) }.getOrNull()
        if (container == null) {
            Log.w(TAG, "AppContainer 未就绪，跳过按键处理")
        } else {
            val engine = container.mappingEngine
            if (engine.isEventBlocked(btn)) {
                Log.i(TAG, "🚫 拦截按键 ${btn.buttonName} (blocked=true)")
                if (event.action == KeyEvent.ACTION_DOWN) engine.onButtonEvent(btn)
                return true
            }
            if (event.action == KeyEvent.ACTION_DOWN) {
                engine.onButtonEvent(btn)
            }
        }

        synchronized(keyListeners) {
            for (l in keyListeners.toList()) {
                runCatching { l.onKeyCaptured(btn, source, deviceName, event.keyCode) }
            }
        }

        lastKeyLog = "KEY#$a11yKeyCount kc=${event.keyCode} act=${event.action} src=$source dev=${deviceName ?: "?"} -> ${btn.buttonName}/${btn.buttonId}"
        if (a11yKeyCount <= 10 || event.action == KeyEvent.ACTION_DOWN) {
            Log.i(TAG, "🔑 $lastKeyLog")
        }
        return false
    }

    override fun onDestroy() {
        instance = null
        serviceScope.cancel("service destroyed")
        super.onDestroy()
    }

    fun bringK2erTaskToFront() {
        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            val task = activityManager?.appTasks?.firstOrNull()
            task?.moveToFront()
            Log.i(TAG, "🔙 K2ER task 已 moveToFront")
        } catch (e: Throwable) {
            Log.w(TAG, "moveTaskToFront 失败，用 fallback", e)
            try {
                val intent = android.content.Intent(this, com.keymapper.app.ui.MainActivity::class.java).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            } catch (e2: Throwable) {
                Log.e(TAG, "fallback 也失败", e2)
            }
        }
    }

    fun performTap(normX: Float, normY: Float): Boolean {
        val x = clampX(normX); val y = clampY(normY)
        val path = Path().apply { moveTo(x, y); lineTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 80L))
            .build()
        return dispatchGesture(gesture, null, null).also { Log.d(TAG, "tap($normX,$normY) -> $it") }
    }

    fun performLongPress(normX: Float, normY: Float, durationMs: Long): Boolean {
        val x = clampX(normX); val y = clampY(normY)
        val path = Path().apply { moveTo(x, y); lineTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, durationMs))
            .build()
        return dispatchGesture(gesture, null, null).also { Log.d(TAG, "longPress -> $it") }
    }

    fun performSwipe(fx: Float, fy: Float, tx: Float, ty: Float, durationMs: Long): Boolean {
        val x1 = clampX(fx); val y1 = clampY(fy)
        val x2 = clampX(tx); val y2 = clampY(ty)
        val path = Path().apply { moveTo(x1, y1); lineTo(x2, y2) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, durationMs))
            .build()
        return dispatchGesture(gesture, null, null).also { Log.d(TAG, "swipe -> $it") }
    }

    private fun clampX(normX: Float): Float {
        val v = if (normX <= 1f) normX * screenWidth else normX
        return max(10f, min(screenWidth - 10f, v))
    }

    private fun clampY(normX: Float): Float {
        val v = if (normX <= 1f) normX * screenHeight else normX
        return max(10f, min(screenHeight - 10f, v))
    }
}
