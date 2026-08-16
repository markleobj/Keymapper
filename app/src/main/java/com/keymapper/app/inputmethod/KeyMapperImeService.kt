package com.keymapper.app.inputmethod

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.keymapper.app.service.KeyMapperAccessibilityService
import com.keymapper.app.service.KeyMapperAccessibilityService.Companion.keyEventToButton
import com.keymapper.app.service.KeyMapperAccessibilityService.Companion.sourceToString

class KeyMapperImeService : InputMethodService() {

    companion object {
        private const val TAG = "KeyMapperIME"
        @Volatile var lastKeyInfo: String? = null
        @Volatile private var _imeKeyCount: Int = 0
        fun getImeKeyCount() = _imeKeyCount
        fun resetImeKeyCount() { _imeKeyCount = 0; lastKeyInfo = null }
    }

    override fun onCreateInputView(): View {
        val v = FrameLayout(this)
        v.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        return v
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        _imeKeyCount++
        val btn = keyEventToButton(event)
        val src = sourceToString(event.source)
        val dev = try { event.device?.name } catch (_: Exception) { null }
        val info = "IME#$_imeKeyCount: keyCode=$keyCode src=$src dev=$dev -> ${btn.buttonName}/${btn.buttonId}"
        lastKeyInfo = info
        Log.i(TAG, "⌨️ $info")
        broadcastKey(btn, src, dev, keyCode, true)
        return false
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        _imeKeyCount++
        val btn = keyEventToButton(event)
        val src = sourceToString(event.source)
        val dev = try { event.device?.name } catch (_: Exception) { null }
        val info = "IME#$_imeKeyCount: UP keyCode=$keyCode src=$src dev=$dev -> ${btn.buttonName}"
        lastKeyInfo = info
        Log.i(TAG, "⌨️ $info")
        broadcastKey(btn, src, dev, keyCode, false)
        return false
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        event ?: return false
        if (event.source and android.view.InputDevice.SOURCE_GAMEPAD != 0 ||
            event.source and android.view.InputDevice.SOURCE_JOYSTICK != 0) {
            _imeKeyCount++
            val src = sourceToString(event.source)
            val dev = try { event.device?.name } catch (_: Exception) { null }
            val ax = try { event.getAxisValue(MotionEvent.AXIS_X) } catch (_: Exception) { 0f }
            val ay = try { event.getAxisValue(MotionEvent.AXIS_Y) } catch (_: Exception) { 0f }
            val info = "IME#$_imeKeyCount: MOTION src=$src dev=$dev axisX=$ax axisY=$ay btnState=${event.buttonState}"
            lastKeyInfo = info
            Log.i(TAG, "🎮 $info")
            val listeners = KeyMapperAccessibilityService.getKeyListeners()
            synchronized(listeners) {
                for (l in listeners.toList()) {
                    runCatching { l.onMotionCaptured("MOTION_${event.buttonState}", src, dev) }
                }
            }
        }
        return false
    }

    private fun broadcastKey(btn: com.keymapper.app.model.HidButtonEvent, source: String, deviceName: String?, keyCode: Int, isPressed: Boolean) {
        val listeners = KeyMapperAccessibilityService.getKeyListeners()
        synchronized(listeners) {
            for (l in listeners.toList()) {
                runCatching {
                    val finalBtn = btn.copy(isPressed = isPressed)
                    l.onKeyCaptured(finalBtn, "IME:$source", deviceName, keyCode)
                }
            }
        }
    }
}
