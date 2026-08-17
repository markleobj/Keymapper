package com.keymapper.app.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.keymapper.app.AppContainer
import com.keymapper.app.mapping.ShizukuShell
import com.keymapper.app.model.HidButtonEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlin.math.abs

object InputMonitor {
    private const val TAG = "InputMonitor-K2ER"

    @Volatile var currentPackageName: String? = null
        private set
    @Volatile var currentPackageLabel: String? = null
    @Volatile var deviceCount: Int = 0
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var refreshJob: Job? = null
    private var geteventJob: Job? = null

    @Volatile private var running = false
    @Volatile private var captureWindowActive = false

    @Volatile private var buttonEventListener: ((HidButtonEvent) -> Unit)? = null

    private var appContext: Context? = null
    private var captureView: View? = null
    private var wmRef: WindowManager? = null

    fun setButtonEventListener(listener: ((HidButtonEvent) -> Unit)?) {
        buttonEventListener = listener
    }

    @SuppressLint("ClickableViewAccessibility")
    fun start(context: Context) {
        if (running) { ensureCaptureWindow(); return }
        appContext = context.applicationContext
        running = true
        startPackageMonitor()
        startGeteventListener()
        ensureCaptureWindow()
        Log.i(TAG, "✅ InputMonitor started (K2er mode, captureWindow=$captureWindowActive)")
    }

    fun stop() {
        running = false
        refreshJob?.cancel(); refreshJob = null
        geteventJob?.cancel(); geteventJob = null
        detachCaptureWindow()
        buttonEventListener = null
        Log.i(TAG, "InputMonitor stopped")
    }

    @SuppressLint("ClickableViewAccessibility")
    fun ensureCaptureWindow() {
        if (captureWindowActive) return
        val ctx = appContext ?: return
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
        val dm = ctx.resources.displayMetrics
        updateScreenSize(dm.widthPixels, dm.heightPixels)

        val view = object : View(ctx) {
            override fun onTouchEvent(event: MotionEvent): Boolean {
                onMotionEvent(event); return false
            }
            override fun onGenericMotionEvent(event: MotionEvent): Boolean {
                onMotionEvent(event); return false
            }
        }
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 0; y = 0 }
        try {
            wm.addView(view, params)
            captureView = view; wmRef = wm; captureWindowActive = true
            Log.i(TAG, "✅ MotionEvent 捕获 Window 已挂载 (${dm.widthPixels}x${dm.heightPixels})")
        } catch (e: Throwable) {
            Log.w(TAG, "ensureCaptureWindow 失败（可能缺悬浮窗权限）: ${e.message}")
        }
    }

    fun detachCaptureWindow() {
        try { captureView?.let { wmRef?.removeViewImmediate(it) } } catch (_: Throwable) {}
        captureView = null; wmRef = null; captureWindowActive = false
    }

    private fun startPackageMonitor() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            while (running) {
                refreshForegroundPackage()
                delay(500)
            }
        }
    }

    fun refreshForegroundPackage() {
        val pkg = ShizukuShell.execSync("dumpsys activity top 2>/dev/null | head -5")
            .lineSequence()
            .firstOrNull { it.contains("ACTIVITY") }
            ?.let { line ->
                val m = Regex("ACTIVITY\\s+([a-zA-Z0-9_.]+)/").find(line)
                m?.groupValues?.getOrNull(1)
            }
            ?: ShizukuShell.execSync("dumpsys window windows 2>/dev/null | grep -E 'mCurrentFocus|mFocusedApp' | head -1")
                .let { s ->
                    val m = Regex("([a-zA-Z0-9_.]+)/").find(s)
                    m?.groupValues?.getOrNull(1)
                }

        if (pkg != null && pkg != currentPackageName) {
            currentPackageName = pkg
            currentPackageLabel = getAppLabel(com.keymapper.app.AppContainer.ctx, pkg)
            runCatching {
                val engine = com.keymapper.app.AppContainer.require().mappingEngine
                engine.refreshForPackage(pkg)
            }
            Log.d(TAG, "📱 前台 APP: $pkg (${currentPackageLabel})")
        }
    }

    private fun getAppLabel(ctx: Context, pkg: String): String? {
        return try {
            ctx.packageManager.getApplicationLabel(ctx.packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (_: Throwable) { null }
    }

    private fun startGeteventListener() {
        geteventJob?.cancel()
        geteventJob = scope.launch {
            while (running) {
                if (!ShizukuShell.isPermissionGranted()) {
                    delay(2000); continue
                }
                val paths = findInputDevices()
                if (paths.isEmpty()) {
                    delay(2000); continue
                }
                Log.i(TAG, "🎮 getevent 监听 ${paths.size} 个设备: ${paths.joinToString()}")
                val ok = runGeteventLoop(paths)
                if (!ok) delay(1000)
            }
        }
    }

    private suspend fun findInputDevices(): List<String> {
        val devices = mutableSetOf<String>()
        val lsOut = ShizukuShell.execSync("ls /dev/input/event* 2>/dev/null")
        lsOut.trim().split("\n").filter { it.isNotBlank() }.forEach { devices.add(it.trim()) }

        val dumpsysOut = ShizukuShell.execSync("dumpsys input 2>/dev/null")
        dumpsysOut.lineSequence().forEach { line ->
            val m = Regex("(\\/dev\\/input\\/event\\d+)").find(line)
            if (m != null) devices.add(m.groupValues[1])
        }

        val found = devices.toList()
        deviceCount = found.size
        Log.d(TAG, "🔍 发现输入设备: ${found.joinToString()}")
        return found
    }

    private suspend fun runGeteventLoop(paths: List<String>): Boolean {
        val process = ShizukuShell.execProcess("getevent -l ${paths.joinToString(" ")}") ?: return false
        try {
            val reader = process.inputStream.bufferedReader()
            var lastBtn = ""; var lastTime = 0L
            while (running) {
                val line: String? = try {
                    runInterruptible(Dispatchers.IO) { reader.readLine() }
                } catch (_: java.io.IOException) {
                    if (Thread.currentThread().isInterrupted) break
                    null
                }
                line ?: break
                val parsed = parseGeteventLine(line) ?: continue
                val now = System.currentTimeMillis()
                if (parsed.buttonId == lastBtn && now - lastTime < 40L && parsed.isDown) continue
                lastBtn = parsed.buttonId; lastTime = now

                val btnEvent = HidButtonEvent(
                    buttonId = parsed.buttonId,
                    buttonName = parsed.buttonId,
                    isPressed = parsed.isDown,
                    timestamp = now,
                    deviceName = parsed.devicePath
                )

                buttonEventListener?.invoke(btnEvent)
                if (parsed.isDown && shouldDispatch(parsed.buttonId)) {
                    val engine = runCatching { com.keymapper.app.AppContainer.require().mappingEngine }.getOrNull()
                    runCatching { engine?.onButtonEvent(btnEvent, currentPackageName) }
                }
            }
            process.waitFor(); return true
        } catch (e: CancellationException) {
            Log.i(TAG, "getevent cancelled"); return false
        } catch (e: Throwable) {
            Log.e(TAG, "getevent error", e); return false
        } finally {
            runCatching { process.destroy() }
        }
    }

    private data class ParsedKey(val buttonId: String, val isDown: Boolean, val devicePath: String)

    private fun parseGeteventLine(line: String): ParsedKey? {
        if (!line.contains("EV_KEY")) return null

        val parts = line.trim().split("\\s+".toRegex())
        val valueStr = parts.lastOrNull() ?: return null
        val value = valueStr.toIntOrNull(16) ?: valueStr.toIntOrNull() ?: return null
        val isDown = value != 0

        val path = parts.firstOrNull { it.startsWith("/dev/input/") } ?: "/dev/input/event0"

        val btnTouchMatch = Regex("BTN_TOUCH").find(line)
        if (btnTouchMatch != null) {
            return ParsedKey("BTN_TOUCH", isDown, path)
        }

        val btnToolMatch = Regex("BTN_TOOL_(\\w+)").find(line)
        if (btnToolMatch != null) {
            val tool = btnToolMatch.groupValues[1]
            return ParsedKey("TOOL_${tool}", isDown, path)
        }

        val keyMatch = Regex("KEY_(\\w+)").find(line)
        if (keyMatch != null) {
            val keyName = keyMatch.groupValues[1]
            val buttonId = mapKey(keyName)
            if (buttonId == "UNKNOWN") {
                return ParsedKey("KEY_$keyName", isDown, path)
            }
            return ParsedKey(buttonId, isDown, path)
        }

        val btnMatch = Regex("BTN_(\\w+)").find(line)
        if (btnMatch != null) {
            val btnName = btnMatch.groupValues[1]
            val buttonId = mapKey("BTN_$btnName")
            if (buttonId == "UNKNOWN") {
                return ParsedKey("BTN_$btnName", isDown, path)
            }
            return ParsedKey(buttonId, isDown, path)
        }

        return null
    }

    private fun mapKey(name: String): String = when (name) {
        "BTN_SOUTH", "BTN_A" -> "A"
        "BTN_EAST", "BTN_B" -> "B"
        "BTN_NORTH", "BTN_X" -> "X"
        "BTN_WEST", "BTN_Y" -> "Y"
        "BTN_TL" -> "L1"; "BTN_TR" -> "R1"
        "BTN_TL2" -> "L2"; "BTN_TR2" -> "R2"
        "BTN_SELECT" -> "SELECT"; "BTN_START" -> "START"; "BTN_MODE" -> "MODE"
        "BTN_THUMBL" -> "L3"; "BTN_THUMBR" -> "R3"
        "DPAD_UP" -> "DPAD_UP"; "DPAD_DOWN" -> "DPAD_DOWN"
        "DPAD_LEFT" -> "DPAD_LEFT"; "DPAD_RIGHT" -> "DPAD_RIGHT"
        "BTN_GAMEPAD" -> "GAMEPAD"
        else -> "UNKNOWN"
    }

    fun keyEventToButton(event: KeyEvent): String {
        val kc = event.keyCode
        return when (kc) {
            KeyEvent.KEYCODE_DPAD_UP -> "DPAD_UP"
            KeyEvent.KEYCODE_DPAD_DOWN -> "DPAD_DOWN"
            KeyEvent.KEYCODE_DPAD_LEFT -> "DPAD_LEFT"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "DPAD_RIGHT"
            KeyEvent.KEYCODE_BUTTON_A -> "A"
            KeyEvent.KEYCODE_BUTTON_B -> "B"
            KeyEvent.KEYCODE_BUTTON_X -> "X"
            KeyEvent.KEYCODE_BUTTON_Y -> "Y"
            KeyEvent.KEYCODE_BUTTON_L1 -> "L1"
            KeyEvent.KEYCODE_BUTTON_L2 -> "L2"
            KeyEvent.KEYCODE_BUTTON_R1 -> "R1"
            KeyEvent.KEYCODE_BUTTON_R2 -> "R2"
            KeyEvent.KEYCODE_BUTTON_START -> "START"
            KeyEvent.KEYCODE_BUTTON_SELECT -> "SELECT"
            KeyEvent.KEYCODE_BUTTON_THUMBL -> "L3"
            KeyEvent.KEYCODE_BUTTON_THUMBR -> "R3"
            KeyEvent.KEYCODE_ENTER -> "ENTER"
            KeyEvent.KEYCODE_ESCAPE -> "ESC"
            else -> "UNKNOWN"
        }
    }

    @Volatile private var screenWidth: Int = 1080
    @Volatile private var screenHeight: Int = 1920

    fun updateScreenSize(w: Int, h: Int) {
        screenWidth = w; screenHeight = h
    }

    @Volatile private var lastDispatchKey = ""
    @Volatile private var lastDispatchTime = 0L

    private fun shouldDispatch(btnId: String): Boolean {
        val now = System.currentTimeMillis()
        if (btnId == lastDispatchKey && now - lastDispatchTime < 250L) return false
        lastDispatchKey = btnId; lastDispatchTime = now
        return true
    }

    private val gamepadTouchTracker = object {
        private var touchStartX: Float = 0f
        private var touchStartY: Float = 0f
        private var touchDevName: String = ""
        private var touchMoved: Boolean = false
        private var touchActive: Boolean = false

        private val GP_HINTS = listOf(
            "gamepad", "controller", "r1s", "hid", "bluetooth", "joystick",
            "betop", "nacon", "8bitdo", "snes", "nes", "ps3", "ps4", "ps5",
            "xbox", "switch", "switchpro", "joycon", "mi", "beibitong"
        )

        fun isGamepadDevice(dev: InputDevice): Boolean {
            val name = (dev.name ?: "").lowercase()
            if (GP_HINTS.any { name.contains(it) }) return true
            val src = dev.sources
            if (src and InputDevice.SOURCE_GAMEPAD != 0) return true
            if (src and InputDevice.SOURCE_JOYSTICK != 0) return true
            if (src and InputDevice.SOURCE_DPAD != 0) return true
            return false
        }

        fun process(ev: MotionEvent) {
            val dev = ev.device ?: return
            if (!isGamepadDevice(dev)) return
            val devName = dev.name ?: ""
            val rawX = ev.rawX; val rawY = ev.rawY

            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    touchStartX = rawX; touchStartY = rawY
                    touchDevName = devName; touchMoved = false; touchActive = true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (abs(rawX - touchStartX) > 30f || abs(rawY - touchStartY) > 30f) {
                        touchMoved = true
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (!touchActive) return
                    touchActive = false
                    val endX = rawX; val endY = rawY
                    val button = inferButtonFromTouch(
                        touchStartX, touchStartY, endX, endY, touchMoved
                    )
                    val btnEvent = HidButtonEvent(
                        buttonId = button, buttonName = button,
                        isPressed = true, deviceName = devName
                    )
                    if (shouldDispatch(button)) dispatchButtonEvent(btnEvent)
                    Log.i(TAG, "🎮 MotionEvent→按键: $button dev=\"$devName\" " +
                            "start=(${touchStartX.toInt()},${touchStartY.toInt()}) " +
                            "end=(${endX.toInt()},${endY.toInt()}) moved=$touchMoved")
                }
            }
        }
    }

    private fun inferButtonFromTouch(
        startX: Float, startY: Float, endX: Float, endY: Float, moved: Boolean
    ): String {
        val dx = endX - startX; val dy = endY - startY
        if (moved || abs(dx) > 30f || abs(dy) > 30f) {
            return when {
                abs(dx) > abs(dy) -> if (dx > 0) "DPAD_RIGHT" else "DPAD_LEFT"
                abs(dy) > 30f -> if (dy > 0) "DPAD_DOWN" else "DPAD_UP"
                else -> inferStaticButton(startX, startY)
            }
        }
        return inferStaticButton(startX, startY)
    }

    private fun inferStaticButton(x: Float, y: Float): String {
        val w = screenWidth.toFloat(); val h = screenHeight.toFloat()
        val nx = (x / w).coerceIn(0f, 1f)
        val ny = (y / h).coerceIn(0f, 1f)

        val centerX = 0.25f..0.75f
        val centerY = 0.25f..0.75f
        if (nx in centerX && ny in centerY) return "JOYSTICK_CENTER"

        return when {
            ny < 0.25f -> {
                when {
                    nx < 0.33f -> "L2"
                    nx > 0.66f -> "R2"
                    else -> "START_SELECT"
                }
            }
            ny < 0.5f -> {
                when {
                    nx < 0.25f -> "DPAD_LEFT"
                    nx > 0.75f -> "R1"
                    nx < 0.5f -> "L1"
                    else -> "Y"
                }
            }
            ny < 0.75f -> {
                when {
                    nx < 0.25f -> "DPAD_UP"
                    nx > 0.75f -> "X"
                    else -> "SELECT"
                }
            }
            else -> {
                when {
                    nx < 0.25f -> "DPAD_DOWN"
                    nx > 0.75f -> "B"
                    nx < 0.5f -> "A"
                    else -> "BTN_HOME"
                }
            }
        }
    }

    fun onMotionEvent(ev: MotionEvent) {
        gamepadTouchTracker.process(ev)
    }

    private fun dispatchButtonEvent(btnEvent: HidButtonEvent) {
        buttonEventListener?.invoke(btnEvent)
        val engine = runCatching { AppContainer.require().mappingEngine }.getOrNull()
        runCatching { engine?.onButtonEvent(btnEvent, currentPackageName) }
    }

    fun bringK2erTaskToFront() {
        runCatching {
            val pkg = com.keymapper.app.AppContainer.ctx.packageName
            val intent = com.keymapper.app.AppContainer.ctx.packageManager.getLaunchIntentForPackage(pkg)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            com.keymapper.app.AppContainer.ctx.startActivity(intent)
        }
    }
}
