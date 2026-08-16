package com.keymapper.app.inputmethod

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.widget.FrameLayout

class KeyMapperImeService : InputMethodService() {

    companion object {
        private const val TAG = "K2ER-IME"
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

    override fun onDestroy() {
        active = false
        Log.i(TAG, "❌ InputMethodService onDestroy")
        super.onDestroy()
    }
}
