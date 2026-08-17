package com.keymapper.app.floating

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.keymapper.app.AppContainer
import com.keymapper.app.mapping.ShizukuShell
import com.keymapper.app.model.HidButtonEvent
import com.keymapper.app.service.KeyMapperAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KeyInterceptorOverlay(private val context: Context) {

    companion object {
        private const val TAG = "KeyInterceptor-K2ER"

        @Volatile
        private var instance: KeyInterceptorOverlay? = null

        fun getInstance(context: Context): KeyInterceptorOverlay {
            return instance ?: synchronized(this) {
                instance ?: KeyInterceptorOverlay(context.applicationContext).also { instance = it }
            }
        }
    }

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var overlayView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var isRunning = false
    private var geteventJob: Job? = null

    @SuppressLint("ClickableViewAccessibility")
    fun start() {
        if (isRunning) return
        if (!FloatingWindowManager.canDrawOverlay(context)) {
            Log.e(TAG, "no overlay permission")
            return
        }

        val dm = context.resources.displayMetrics
        val view = TransparentOverlayView(context)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = Gravity.TOP or Gravity.START
            this.x = 0
            this.y = 0
            this.width = dm.widthPixels
            this.height = dm.heightPixels
        }

        try {
            windowManager.addView(view, lp)
            overlayView = view
            params = lp
            isRunning = true

            startGeteventListener()

            Log.i(TAG, "✅ K2er transparent overlay + getevent listener started")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to add overlay", e)
        }
    }

    fun stop() {
        geteventJob?.cancel()
        geteventJob = null
        try {
            overlayView?.let { windowManager.removeViewImmediate(it) }
        } catch (_: Throwable) {}
        overlayView = null
        params = null
        isRunning = false
        Log.i(TAG, "Key interceptor stopped")
    }

    fun isRunning() = isRunning

    private fun startGeteventListener() {
        geteventJob?.cancel()
        geteventJob = scope.launch(Dispatchers.IO) {
            val shizuku = ShizukuShell
            if (!shizuku.isPermissionGranted()) {
                Log.w(TAG, "Shizuku not granted, skip getevent")
                return@launch
            }

            val eventPaths = findInputDevices()
            if (eventPaths.isEmpty()) {
                Log.w(TAG, "No input devices found")
                return@launch
            }
            Log.i(TAG, "🎮 getevent listening on: ${eventPaths.joinToString()}")

            while (true) {
                val ok = runGeteventLoop(shizuku, eventPaths)
                if (!ok) {
                    Log.w(TAG, "getevent exited, retry in 1s...")
                    delay(1000)
                }
            }
        }
    }

    private suspend fun findInputDevices(): List<String> {
        val devices = mutableListOf<String>()
        runCatching {
            val ls = ShizukuShell.execSync("ls /dev/input/")
            Log.d(TAG, "input devices: $ls")
        }
        runCatching {
            val dump = ShizukuShell.execSync("dumpsys input")
            dump.lineSequence().forEach { line ->
                if (line.contains("Keyboard") || line.contains("Gamepad") ||
                    line.contains("Joystick") || line.contains("DPad") ||
                    line.contains("BUTTON_")) {
                    val m = Regex("(\\/dev\\/input\\/event\\d+)").find(line)
                    m?.groupValues?.getOrNull(1)?.let { devices.add(it) }
                }
            }
        }
        if (devices.isEmpty()) {
            runCatching {
                val ls = ShizukuShell.execSync("ls /dev/input/event* 2>/dev/null")
                ls.trim().split("\n").filter { it.isNotBlank() }.forEach { devices.add(it.trim()) }
            }
        }
        return devices.distinct()
    }

    private suspend fun runGeteventLoop(shizuku: ShizukuShell, paths: List<String>): Boolean {
        val cmd = "getevent -l ${paths.joinToString(" ")}"
        val process = shizuku.execProcess(cmd) ?: return false
        return try {
            val reader = process.inputStream.bufferedReader()
            var lastDispatchTime = 0L
            var lastButton = ""

            while (true) {
                val line = reader.readLine() ?: break
                parseGeteventLine(line)?.let { parsed ->
                    val now = System.currentTimeMillis()
                    val isDown = parsed.isDown
                    val isRepeat = parsed.buttonId == lastButton && (now - lastDispatchTime) < 50L && isDown
                    if (isRepeat) return@let

                    lastButton = parsed.buttonId
                    lastDispatchTime = now

                    KeyMapperAccessibilityService.refreshForegroundPackage()
                    val currentPkg = KeyMapperAccessibilityService.currentPackageName

                    val btnEvent = HidButtonEvent(
                        buttonId = parsed.buttonId,
                        buttonName = parsed.buttonId,
                        isPressed = isDown,
                        timestamp = now,
                        deviceName = parsed.devicePath
                    )

                    Log.d(TAG, "[GETEVENT] ${if (isDown) "↓" else "↑"} ${parsed.buttonId} @$currentPkg")

                    val container = runCatching { AppContainer.getOrCreate(context) }.getOrNull()
                    runCatching { container?.mappingEngine?.onButtonEvent(btnEvent, currentPkg) }
                }
            }
            process.waitFor()
            true
        } catch (e: Throwable) {
            Log.e(TAG, "getevent loop error", e)
            false
        } finally {
            runCatching { process.destroy() }
        }
    }

    private data class ParsedKey(val buttonId: String, val isDown: Boolean, val devicePath: String)

    private fun parseGeteventLine(line: String): ParsedKey? {
        val trimmed = line.trim()
        if (!trimmed.contains("EV_KEY")) return null

        val path = trimmed.substringBefore("EV_KEY").trim()
            .substringBeforeLast(":").trim()
            .ifBlank { "/dev/input/event0" }

        val keyMatch = Regex("KEY_(\\w+)").find(trimmed) ?: return null
        val keyName = keyMatch.groupValues[1]

        val parts = trimmed.split("\\s+".toRegex())
        val valueStr = parts.lastOrNull() ?: return null
        val value = valueStr.toIntOrNull(16) ?: valueStr.toIntOrNull() ?: return null
        val isDown = value != 0

        val buttonId = mapKeyName(keyName)
        if (buttonId == "UNKNOWN") return null

        return ParsedKey(buttonId, isDown, path)
    }

    private fun mapKeyName(name: String): String = when (name) {
        "BTN_SOUTH", "BTN_A" -> "A"
        "BTN_EAST", "BTN_B" -> "B"
        "BTN_NORTH", "BTN_X" -> "X"
        "BTN_WEST", "BTN_Y" -> "Y"
        "BTN_TL" -> "L1"
        "BTN_TR" -> "R1"
        "BTN_TL2" -> "L2"
        "BTN_TR2" -> "R2"
        "BTN_SELECT" -> "SELECT"
        "BTN_START" -> "START"
        "BTN_MODE" -> "MODE"
        "BTN_THUMBL" -> "L3"
        "BTN_THUMBR" -> "R3"
        "DPAD_UP" -> "DPAD_UP"
        "DPAD_DOWN" -> "DPAD_DOWN"
        "DPAD_LEFT" -> "DPAD_LEFT"
        "DPAD_RIGHT" -> "DPAD_RIGHT"
        "ENTER" -> "ENTER"
        "ESC" -> "ESC"
        else -> "UNKNOWN"
    }

    @SuppressLint("ViewConstructor", "ClickableViewAccessibility")
    private inner class TransparentOverlayView(context: Context) : View(context) {
        init {
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        override fun dispatchKeyEvent(event: KeyEvent) = false
        override fun onKeyDown(keyCode: Int, event: KeyEvent?) = false
        override fun onKeyUp(keyCode: Int, event: KeyEvent?) = false
        override fun dispatchTouchEvent(event: MotionEvent) = false
        override fun onTouchEvent(event: MotionEvent) = false
        override fun onGenericMotionEvent(event: MotionEvent) = false
    }
}
