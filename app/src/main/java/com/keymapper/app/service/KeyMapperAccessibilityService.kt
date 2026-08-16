package com.keymapper.app.service

import android.content.Context
import android.util.Log
import android.view.InputDevice
import android.view.KeyEvent
import com.keymapper.app.mapping.ShizukuShell
import com.keymapper.app.model.HidButtonEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object KeyMapperAccessibilityService {
    private const val TAG = "K2ER-State"

    interface KeyListener {
        fun onKeyCaptured(event: HidButtonEvent, source: String, deviceName: String?, rawKeyCode: Int)
        fun onMotionCaptured(button: String, source: String, deviceName: String?)
    }

    private val listeners = mutableListOf<KeyListener>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile
    var gamepadTracker: GamepadTracker? = null

    @JvmStatic
    @Volatile
    var currentPackageName: String? = null
        private set

    @JvmStatic
    @Volatile
    var currentPackageLabel: String? = null
        private set

    fun isRunning(): Boolean = ShizukuShell.isPermissionGranted()

    fun getTouchKeyCount(): Int = gamepadTracker?.touchKeyCount ?: 0

    @Volatile private var a11yKeyCount: Int = 0
    @JvmStatic fun getA11yKeyCount(): Int = a11yKeyCount

    @Volatile private var lastKeyLog: String = ""
    @JvmStatic fun getLastKeyLog() = lastKeyLog

    @Volatile private var flagsSummary: String = ""
    @JvmStatic fun getFlagsSummary() = flagsSummary

    @Volatile private var inputDeviceSummary: String = ""
    @JvmStatic fun getInputDeviceSummary() = inputDeviceSummary

    @Volatile var appContext: android.content.Context? = null

    fun refreshForegroundPackage() {
        scope.launch {
            val pkg = ShizukuShell.tryGetForegroundPackage()
            if (pkg != null) {
                currentPackageName = pkg
                currentPackageLabel = try {
                    val ctx = appContext
                    val pm = ctx?.packageManager
                    pm?.getApplicationLabel(pm.getApplicationInfo(pkg, 0))?.toString()
                } catch (_: Throwable) { null }
                Log.d(TAG, "前台 APP: $pkg ($currentPackageLabel)")
            }
        }
    }

    fun bringK2erTaskToFront() {
        try {
            val ctx = appContext ?: return
            val intent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(intent)
            }
        } catch (_: Throwable) {}
    }

    class GamepadTracker {
        var touchKeyCount: Int = 0
    }

    fun addKeyListener(l: KeyListener) {
        if (!listeners.contains(l)) listeners.add(l)
    }

    fun removeKeyListener(l: KeyListener) { listeners.remove(l) }

    fun dispatchKeyCaptured(event: HidButtonEvent, source: String, deviceName: String?, rawKeyCode: Int) {
        a11yKeyCount++
        lastKeyLog = "${if (event.isPressed) "↓" else "↑"} ${event.buttonId} (source=$source, device=$deviceName)"
        listeners.forEach { it.onKeyCaptured(event, source, deviceName, rawKeyCode) }
    }

    fun dispatchMotionCaptured(button: String, source: String, deviceName: String?) {
        lastKeyLog = "🎯 Motion $button (source=$source, device=$deviceName)"
        listeners.forEach { it.onMotionCaptured(button, source, deviceName) }
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
            KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_L2 -> "L${if (kc == KeyEvent.KEYCODE_BUTTON_L1) "1" else "2"}"
            KeyEvent.KEYCODE_BUTTON_R1, KeyEvent.KEYCODE_BUTTON_R2 -> "R${if (kc == KeyEvent.KEYCODE_BUTTON_R1) "1" else "2"}"
            KeyEvent.KEYCODE_BUTTON_START -> "START"
            KeyEvent.KEYCODE_BUTTON_SELECT -> "SELECT"
            KeyEvent.KEYCODE_BUTTON_THUMBL -> "L3"
            KeyEvent.KEYCODE_BUTTON_THUMBR -> "R3"
            KeyEvent.KEYCODE_ENTER -> "ENTER"
            KeyEvent.KEYCODE_ESCAPE -> "ESC"
            else -> "KEY_$kc"
        }
    }

    fun sourceToString(source: Int): String {
        val parts = mutableListOf<String>()
        if (source and InputDevice.SOURCE_GAMEPAD != 0) parts.add("GAMEPAD")
        if (source and InputDevice.SOURCE_KEYBOARD != 0) parts.add("KEYBOARD")
        if (source and InputDevice.SOURCE_MOUSE != 0) parts.add("MOUSE")
        if (source and InputDevice.SOURCE_TOUCHSCREEN != 0) parts.add("TOUCH")
        if (source and InputDevice.SOURCE_JOYSTICK != 0) parts.add("JOYSTICK")
        if (source and InputDevice.SOURCE_DPAD != 0) parts.add("DPAD")
        return if (parts.isEmpty()) "UNKNOWN($source)" else parts.joinToString("+")
    }
}
