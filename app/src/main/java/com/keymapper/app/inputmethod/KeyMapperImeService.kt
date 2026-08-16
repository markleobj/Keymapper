package com.keymapper.app.inputmethod

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.FrameLayout

class KeyMapperImeService : InputMethodService() {

    companion object {
        @Volatile private var _imeKeyCount: Int = 0
        fun getImeKeyCount() = _imeKeyCount
        fun resetImeKeyCount() { _imeKeyCount = 0 }
    }

    override fun onCreateInputView(): View {
        return FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
    }
}
