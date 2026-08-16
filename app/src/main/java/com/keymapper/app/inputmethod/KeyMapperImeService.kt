package com.keymapper.app.inputmethod

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import com.keymapper.app.AppContainer
import com.keymapper.app.service.KeyMapperAccessibilityService

class KeyMapperImeService : InputMethodService() {

    companion object {
        private const val TAG = "K2ER-IME"
        private val HANDLED_KEYS = setOf(
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_BUTTON_L1,
            KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_BUTTON_L2,
            KeyEvent.KEYCODE_BUTTON_R2,
            KeyEvent.KEYCODE_BUTTON_SELECT,
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_BUTTON_THUMBL,
            KeyEvent.KEYCODE_BUTTON_THUMBR,
            KeyEvent.KEYCODE_BUTTON_MODE,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
        )

        @Volatile private var _imeKeyCount: Int = 0
        fun getImeKeyCount() = _imeKeyCount
        fun resetImeKeyCount() { _imeKeyCount = 0 }

        @Volatile private var active: Boolean = false
        fun isActive() = active
    }

    override fun onCreate() {
        super.onCreate()
        active = true
        Log.i(TAG, "✅ InputMethodService onCreate — K2ER 作为输入法运行中")
    }

    override fun onCreateInputView(): View {
        Log.i(TAG, "🔤 onCreateInputView — 返回透明占位 View")
        return FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        Log.i(TAG, "▶️ onStartInputView field=${info?.fieldName} pkg=${info?.packageName}")
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        Log.i(TAG, "⏹ onFinishInputView")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return forwardKey(event, isDown = true) || super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return forwardKey(event, isDown = false) || super.onKeyUp(keyCode, event)
    }

    private fun forwardKey(event: KeyEvent?, isDown: Boolean): Boolean {
        if (event == null) return false
        val code = event.keyCode
        if (code !in HANDLED_KEYS) return false

        _imeKeyCount++
        val btn = KeyMapperAccessibilityService.keyEventToButton(event)

        try {
            val container = AppContainer.getOrCreate(this)
            val engine = container.mappingEngine
            if (isDown) engine.onButtonEvent(btn)
            val blocked = engine.isEventBlocked(btn)
            Log.i(TAG, "IME forward ${if (isDown) "↓" else "↑"} ${btn.buttonName} kc=$code blocked=$blocked")
            return blocked
        } catch (e: Throwable) {
            Log.e(TAG, "forwardKey error", e)
            return false
        }
    }

    override fun onDestroy() {
        active = false
        Log.i(TAG, "❌ InputMethodService onDestroy")
        super.onDestroy()
    }
}
