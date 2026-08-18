package io.github.sds100.keymapper.base.detection

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.sds100.keymapper.base.input.InputEventDetectionSource
import io.github.sds100.keymapper.base.input.InputEventHub
import io.github.sds100.keymapper.common.utils.InputDeviceInfo
import io.github.sds100.keymapper.common.utils.InputDeviceUtils
import io.github.sds100.keymapper.system.inputevents.KMKeyEvent
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class FullscreenOverlayKeyDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val inputEventHub: InputEventHub,
) {

    companion object {
        private const val TAG = "FullscreenOverlayKeyDetector"

        private val GAMEPAD_HINTS = listOf(
            "gamepad", "controller", "r1s", "hid", "bluetooth", "joystick",
            "betop", "nacon", "8bitdo", "snes", "nes", "ps3", "ps4", "ps5",
            "xbox", "switch", "switchpro", "joycon", "mi", "beibitong",
            "touchpad", "touch", "simulate", "simulated", "virtual", "emulator",
            "octopus", "panda", "koplayer", "ldplayer", "nox", "mumu", "memu",
        )

        fun isGamepadOrTouchpadDevice(device: InputDevice): Boolean {
            val name = (device.name ?: "").lowercase()
            if (GAMEPAD_HINTS.any { name.contains(it) }) return true
            val src = device.sources
            if (src and InputDevice.SOURCE_GAMEPAD != 0) return true
            if (src and InputDevice.SOURCE_JOYSTICK != 0) return true
            if (src and InputDevice.SOURCE_DPAD != 0) return true
            if (src and InputDevice.SOURCE_MOUSE != 0) return false
            return src and InputDevice.SOURCE_TOUCHPAD != 0
        }

        fun androidKeyCodeForInferredButton(buttonId: String): Int? = when (buttonId) {
            "DPAD_UP" -> KeyEvent.KEYCODE_DPAD_UP
            "DPAD_DOWN" -> KeyEvent.KEYCODE_DPAD_DOWN
            "DPAD_LEFT" -> KeyEvent.KEYCODE_DPAD_LEFT
            "DPAD_RIGHT" -> KeyEvent.KEYCODE_DPAD_RIGHT
            "A" -> KeyEvent.KEYCODE_BUTTON_A
            "B" -> KeyEvent.KEYCODE_BUTTON_B
            "X" -> KeyEvent.KEYCODE_BUTTON_X
            "Y" -> KeyEvent.KEYCODE_BUTTON_Y
            "L1" -> KeyEvent.KEYCODE_BUTTON_L1
            "L2" -> KeyEvent.KEYCODE_BUTTON_L2
            "R1" -> KeyEvent.KEYCODE_BUTTON_R1
            "R2" -> KeyEvent.KEYCODE_BUTTON_R2
            "SELECT" -> KeyEvent.KEYCODE_BUTTON_SELECT
            "START" -> KeyEvent.KEYCODE_BUTTON_START
            "L3" -> KeyEvent.KEYCODE_BUTTON_THUMBL
            "R3" -> KeyEvent.KEYCODE_BUTTON_THUMBR
            else -> null
        }
    }

    @Volatile private var running = false
    @Volatile private var captureWindowActive = false
    @Volatile private var screenWidth: Int = 1080
    @Volatile private var screenHeight: Int = 1920

    private var captureView: View? = null
    private var wmRef: WindowManager? = null

    private val touchTracker = TouchTracker()

    @SuppressLint("ClickableViewAccessibility")
    @Synchronized
    fun start() {
        if (running) {
            if (!captureWindowActive) ensureCaptureWindow()
            return
        }
        running = true
        ensureCaptureWindow()
        Log.i(TAG, "start: captureWindowActive=$captureWindowActive")
    }

    @Synchronized
    fun stop() {
        running = false
        detachCaptureWindow()
        touchTracker.reset()
        Log.i(TAG, "stop")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun ensureCaptureWindow() {
        if (captureWindowActive) return
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: run {
            Log.w(TAG, "WindowManager is null")
            return
        }
        val dm = context.resources.displayMetrics
        screenWidth = dm.widthPixels
        screenHeight = dm.heightPixels

        val view = object : View(context) {
            override fun onTouchEvent(event: MotionEvent): Boolean {
                handleMotionEvent(event)
                return false
            }

            override fun onGenericMotionEvent(event: MotionEvent): Boolean {
                handleMotionEvent(event)
                return false
            }
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val flags = (
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        try {
            wm.addView(view, params)
            captureView = view
            wmRef = wm
            captureWindowActive = true
            Log.i(TAG, "Capture window added (${screenWidth}x${screenHeight})")
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to add capture window (missing SYSTEM_ALERT_WINDOW?): ${e.message}")
        }
    }

    @Synchronized
    private fun detachCaptureWindow() {
        try {
            captureView?.let { wmRef?.removeViewImmediate(it) }
        } catch (_: Throwable) {
        }
        captureView = null
        wmRef = null
        captureWindowActive = false
    }

    private fun handleMotionEvent(event: MotionEvent) {
        val device = event.device ?: return
        if (!isGamepadOrTouchpadDevice(device)) return

        val deviceInfo = InputDeviceUtils.createInputDeviceInfo(device)
        touchTracker.process(event, deviceInfo)
    }

    private fun dispatchInferredKey(
        buttonId: String,
        action: Int,
        deviceInfo: InputDeviceInfo,
        eventTime: Long,
    ) {
        val keyCode = androidKeyCodeForInferredButton(buttonId) ?: run {
            Log.d(TAG, "No keyCode mapping for inferred button: $buttonId")
            return
        }

        val source = InputDevice.SOURCE_GAMEPAD

        val keyEvent = KMKeyEvent(
            keyCode = keyCode,
            action = action,
            metaState = 0,
            scanCode = 0,
            device = deviceInfo,
            repeatCount = 0,
            source = source,
            eventTime = eventTime,
        )

        val consumed = inputEventHub.onInputEvent(
            keyEvent,
            InputEventDetectionSource.FULLSCREEN_OVERLAY,
        )

        Log.i(
            TAG,
            "Inferred key ${KeyEvent.keyCodeToString(keyCode)} " +
                "action=${if (action == KeyEvent.ACTION_DOWN) "DOWN" else "UP"} " +
                "consumed=$consumed device=${deviceInfo.name}",
        )
    }

    private inner class TouchTracker {
        private var startX: Float = 0f
        private var startY: Float = 0f
        private var activePointerId: Int = -1
        private var moved: Boolean = false
        private var pressedButton: String? = null
        private var pressedDevice: InputDeviceInfo? = null
        private var pressedTime: Long = 0L

        fun reset() {
            startX = 0f; startY = 0f; activePointerId = -1
            moved = false; pressedButton = null; pressedDevice = null; pressedTime = 0L
        }

        fun process(event: MotionEvent, deviceInfo: InputDeviceInfo) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    val pointerIndex = event.actionIndex
                    activePointerId = event.getPointerId(pointerIndex)
                    startX = event.getRawX(pointerIndex)
                    startY = event.getRawY(pointerIndex)
                    moved = false
                    pressedDevice = deviceInfo
                    pressedTime = event.eventTime

                    val button = inferButtonFromTouch(startX, startY, startX, startY, moved = false)
                    pressedButton = button
                    dispatchInferredKey(
                        button,
                        KeyEvent.ACTION_DOWN,
                        deviceInfo,
                        event.eventTime,
                    )
                }

                MotionEvent.ACTION_MOVE -> {
                    val idx = event.findPointerIndex(activePointerId)
                    if (idx < 0) return
                    val rawX = event.getRawX(idx)
                    val rawY = event.getRawY(idx)
                    if (abs(rawX - startX) > 30f || abs(rawY - startY) > 30f) {
                        moved = true
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val button = pressedButton ?: return
                    val dev = pressedDevice ?: return
                    dispatchInferredKey(
                        button,
                        KeyEvent.ACTION_UP,
                        dev,
                        event.eventTime,
                    )
                    reset()
                }
            }
        }
    }

    private fun inferButtonFromTouch(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        moved: Boolean,
    ): String {
        val dx = endX - startX
        val dy = endY - startY
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
        val w = screenWidth.toFloat()
        val h = screenHeight.toFloat()
        val nx = (x / w).coerceIn(0f, 1f)
        val ny = (y / h).coerceIn(0f, 1f)

        return when {
            ny < 0.25f -> {
                when {
                    nx < 0.33f -> "L2"
                    nx > 0.66f -> "R2"
                    else -> "START"
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
                    else -> "START"
                }
            }
        }
    }
}
