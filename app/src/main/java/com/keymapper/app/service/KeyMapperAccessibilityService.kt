package com.keymapper.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.WindowManager
import com.keymapper.app.AppContainer
import com.keymapper.app.model.HidButtonEvent
import kotlin.math.max
import kotlin.math.min

class KeyMapperAccessibilityService : AccessibilityService() {

    interface KeyListener {
        fun onKeyCaptured(event: HidButtonEvent, source: String, deviceName: String?)
    }

    companion object {
        private const val TAG = "AccessibilityService"
        @Volatile
        var instance: KeyMapperAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null

        @Volatile
        private var lastKeyTime: Long = 0
        fun getLastKeyTime(): Long = lastKeyTime

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
            188 to Pair("BTN_1",  "1"),
            189 to Pair("BTN_2",  "2"),
            190 to Pair("BTN_3",  "3"),
            191 to Pair("BTN_4",  "4"),
            192 to Pair("BTN_5",  "5"),
            193 to Pair("BTN_6",  "6"),
            194 to Pair("BTN_7",  "7"),
            195 to Pair("BTN_8",  "8"),
            196 to Pair("BTN_9",  "9"),
            197 to Pair("BTN_10", "10"),
            198 to Pair("BTN_11", "11"),
            199 to Pair("BTN_12", "12"),
            200 to Pair("BTN_13", "13"),
            201 to Pair("BTN_14", "14"),
            202 to Pair("BTN_15", "15"),
            203 to Pair("BTN_16", "16"),
        )

        fun keyEventToButton(event: KeyEvent): HidButtonEvent {
            val pair = KEYCODE_TO_BUTTON[event.keyCode]
                ?: return HidButtonEvent("RAW_${event.keyCode}", "键#${event.keyCode}", event.action == KeyEvent.ACTION_DOWN)
            return HidButtonEvent(pair.first, pair.second, event.action == KeyEvent.ACTION_DOWN)
        }

        fun keyEventToButton(keyCode: Int): HidButtonEvent {
            val pair = KEYCODE_TO_BUTTON[keyCode]
                ?: return HidButtonEvent("RAW_$keyCode", "键#$keyCode", true)
            return HidButtonEvent(pair.first, pair.second, true)
        }

        fun sourceToString(source: Int): String = when {
            source and InputDevice.SOURCE_GAMEPAD != 0 -> "GAMEPAD"
            source and InputDevice.SOURCE_JOYSTICK != 0 -> "JOYSTICK"
            source and InputDevice.SOURCE_KEYBOARD != 0 -> "KEYBOARD"
            source and InputDevice.SOURCE_DPAD != 0 -> "DPAD"
            source and InputDevice.SOURCE_MOUSE != 0 -> "MOUSE"
            source and InputDevice.SOURCE_TOUCHPAD != 0 -> "TOUCHPAD"
            else -> "SRC_$source"
        }
    }

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false
        if (event.repeatCount > 0) return false

        lastKeyTime = System.currentTimeMillis()
        val btn = keyEventToButton(event)
        val source = sourceToString(event.source)
        val deviceName = try { event.device?.name } catch (_: Exception) { null }

        runCatching { AppContainer.require() }
            .getOrNull()
            ?.bluetoothController
            ?.dispatchAccessibilityKey(btn)

        synchronized(keyListeners) {
            for (l in keyListeners.toList()) {
                runCatching { l.onKeyCaptured(btn, source, deviceName) }
            }
        }

        Log.i(TAG, "onKeyEvent: keyCode=${event.keyCode} action=${event.action} src=$source device=$deviceName -> ${btn.buttonName}/${btn.buttonId}")
        return false
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.bounds
        } else {
            val dm = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getMetrics(dm)
            android.graphics.Rect(0, 0, dm.widthPixels, dm.heightPixels)
        }
        screenWidth = metrics.width()
        screenHeight = metrics.height()
        Log.i(TAG, "✅ AccessibilityService connected ${screenWidth}x${screenHeight}, onKeyEvent will receive gamepad keys")
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    fun performTap(normX: Float, normY: Float): Boolean {
        val x = clampX(normX); val y = clampY(normY)
        val path = Path().apply { moveTo(x, y); lineTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 80L))
            .build()
        return dispatchGesture(gesture, null, null).also { Log.d(TAG, "tap -> $it") }
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
