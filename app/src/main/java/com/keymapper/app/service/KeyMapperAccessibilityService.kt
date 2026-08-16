package com.keymapper.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.InputMethod
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.keymapper.app.AppContainer
import com.keymapper.app.model.HidButtonEvent
import kotlin.math.max
import kotlin.math.min

class KeyMapperAccessibilityService : AccessibilityService() {

    interface KeyListener {
        fun onKeyCaptured(event: HidButtonEvent, source: String, deviceName: String?, rawKeyCode: Int)
        fun onMotionCaptured(button: String, source: String, deviceName: String?)
    }

    companion object {
        private const val TAG = "K2ER-Service"
        @Volatile
        var instance: KeyMapperAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null

        @Volatile
        private var a11yKeyCount: Int = 0
        fun getA11yKeyCount(): Int = a11yKeyCount

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
            val pair = KEYCODE_TO_BUTTON[event.keyCode]
                ?: return HidButtonEvent("RAW_${event.keyCode}", "键#${event.keyCode}", event.action == KeyEvent.ACTION_DOWN)
            return HidButtonEvent(pair.first, pair.second, event.action == KeyEvent.ACTION_DOWN)
        }

        fun sourceToString(source: Int): String = when {
            source and InputDevice.SOURCE_GAMEPAD != 0 -> "GAMEPAD"
            source and InputDevice.SOURCE_JOYSTICK != 0 -> "JOYSTICK"
            source and InputDevice.SOURCE_KEYBOARD != 0 -> "KEYBOARD"
            source and InputDevice.SOURCE_DPAD != 0 -> "DPAD"
            source and InputDevice.SOURCE_MOUSE != 0 -> "MOUSE"
            source and InputDevice.SOURCE_TOUCHPAD != 0 -> "TOUCHPAD"
            source and InputDevice.SOURCE_TOUCHSCREEN != 0 -> "TOUCHSCREEN"
            else -> "SRC_$source"
        }
    }

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var imeBridge: InputMethod? = null

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

        val flags = serviceInfo.flags
        val hasFlagFilterKey = flags and AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS != 0
        val hasFlagImeEditor = flags and 0x00010000 != 0
        Log.i(TAG, "✅ K2ER Service connected ${screenWidth}x${screenHeight}, SDK=${Build.VERSION.SDK_INT}")
        Log.i(TAG, "📋 flags=0x${flags.toString(16)} filterKey=$hasFlagFilterKey imeEditor=$hasFlagImeEditor")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    /**
     * K2ER 核心机制：Android 13+ 通过 flagInputMethodEditor 让 AccessibilityService 内部
     * 持有一个 InputMethod 通道，从而可以在不切换输入法的情况下接收按键事件。
     * 必须调用 super.onCreateInputMethod() 让系统正确初始化，绝不能用反射自己构造！
     */
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateInputMethod(): InputMethod {
        val result = super.onCreateInputMethod()
        imeBridge = result
        Log.i(TAG, "✅ onCreateInputMethod 通过 super() 成功")
        return result
    }

    /**
     * K2ER 按键拦截入口。只要 XML 配置了 flagRequestFilterKeyEvents，
     * 且 onCreateInputMethod 正确返回了非 null 实例，系统就会把所有按键事件
     * 通过这里回调给我们 —— 不需要 Activity 有焦点，也不需要切输入法。
     *
     * 返回 false = 不消费事件，让手柄按键继续透传到游戏/系统。
     */
    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false
        if (event.repeatCount > 0) return false

        a11yKeyCount++
        val btn = keyEventToButton(event)
        val source = sourceToString(event.source)
        val deviceName = try { event.device?.name } catch (_: Exception) { null }

        runCatching { AppContainer.require() }.getOrNull()?.let {
            runCatching { it.bluetoothController.dispatchAccessibilityKey(btn) }
        }

        synchronized(keyListeners) {
            for (l in keyListeners.toList()) {
                runCatching { l.onKeyCaptured(btn, source, deviceName, event.keyCode) }
            }
        }

        if (a11yKeyCount <= 5 || event.action == KeyEvent.ACTION_DOWN) {
            Log.i(TAG, "🔑 KEY#$a11yKeyCount: kc=${event.keyCode} act=${event.action} src=$source dev=$deviceName -> ${btn.buttonName}/${btn.buttonId}")
        }
        return false
    }

    override fun onDestroy() {
        instance = null
        imeBridge = null
        super.onDestroy()
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
