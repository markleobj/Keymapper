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

class KeyInterceptorOverlay(private val context: Context) {

    companion object {
        private const val TAG = "KeyInterceptor"

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

    private var overlayView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var isRunning = false

    @SuppressLint("ClickableViewAccessibility")
    fun start() {
        if (isRunning) {
            Log.w(TAG, "already running")
            return
        }
        if (!FloatingWindowManager.canDrawOverlay(context)) {
            Log.e(TAG, "no overlay permission")
            return
        }

        val dm = context.resources.displayMetrics
        val view = InterceptView(context)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = Gravity.TOP or Gravity.START
            this.x = 0
            this.y = 0
            this.width = dm.widthPixels
            this.height = dm.heightPixels
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        try {
            windowManager.addView(view, lp)
            overlayView = view
            params = lp
            isRunning = true
            Log.i(TAG, "✅ K2er-style fullscreen key interceptor started (${dm.widthPixels}x${dm.heightPixels})")
        } catch (e: Throwable) {
            Log.e(TAG, "❌ Failed to add overlay view", e)
        }
    }

    fun stop() {
        try {
            overlayView?.let { windowManager.removeViewImmediate(it) }
        } catch (_: Throwable) {}
        overlayView = null
        params = null
        isRunning = false
        Log.i(TAG, "Key interceptor stopped")
    }

    fun isRunning() = isRunning

    @SuppressLint("ViewConstructor", "ClickableViewAccessibility")
    private inner class InterceptView(context: Context) : View(context) {

        init {
            isClickable = true
            isFocusable = true
            isFocusableInTouchMode = true
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            val handled = handleKey(event)
            if (handled) return true
            return super.dispatchKeyEvent(event)
        }

        override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
            event ?: return false
            return handleKey(event) || super.onKeyDown(keyCode, event)
        }

        override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
            event ?: return false
            return handleKey(event) || super.onKeyUp(keyCode, event)
        }

        private fun handleKey(event: KeyEvent): Boolean {
            val source = event.source
            val isGamepad = (source and android.view.InputDevice.SOURCE_GAMEPAD) != 0 ||
                (source and android.view.InputDevice.SOURCE_JOYSTICK) != 0 ||
                (source and android.view.InputDevice.SOURCE_DPAD) != 0

            if (!isGamepad) return false

            val buttonId = KeyMapperAccessibilityService.keyEventToButton(event)
            if (buttonId == "UNKNOWN") return false
            if (event.repeatCount > 0) return false

            val container = runCatching { AppContainer.getOrCreate(context) }.getOrNull()
            val engine = container?.mappingEngine
            if (engine == null) return false

            KeyMapperAccessibilityService.refreshForegroundPackage()
            val currentPkg = KeyMapperAccessibilityService.currentPackageName

            val btnEvent = HidButtonEvent(
                buttonId = buttonId,
                buttonName = buttonId,
                isPressed = event.action == KeyEvent.ACTION_DOWN,
                timestamp = System.currentTimeMillis(),
                deviceName = event.device?.name
            )

            if (event.action == KeyEvent.ACTION_DOWN) {
                Log.i(TAG, "[KEY] $buttonId DOWN → pkg=$currentPkg shizuku=${ShizukuShell.isPermissionGranted()}")
                runCatching { engine.onButtonEvent(btnEvent, currentPkg) }
                return true
            }
            if (event.action == KeyEvent.ACTION_UP) {
                runCatching { engine.onButtonEvent(btnEvent, currentPkg) }
                return true
            }
            return false
        }

        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            event.offsetLocation(-x, -y)
            return false
        }

        override fun onTouchEvent(event: MotionEvent) = false

        override fun onGenericMotionEvent(event: MotionEvent): Boolean {
            val source = event.source
            if ((source and android.view.InputDevice.SOURCE_JOYSTICK) != 0 &&
                event.action == MotionEvent.ACTION_MOVE) {
                handleJoystick(event)
                return true
            }
            return false
        }

        private fun handleJoystick(event: MotionEvent) {
            val container = runCatching { AppContainer.getOrCreate(context) }.getOrNull()
            val engine = container?.mappingEngine ?: return
            val axisX = event.getAxisValue(MotionEvent.AXIS_X)
            val axisY = event.getAxisValue(MotionEvent.AXIS_Y)
            KeyMapperAccessibilityService.refreshForegroundPackage()
            val currentPkg = KeyMapperAccessibilityService.currentPackageName
            runCatching { engine.onJoystickMove(axisX, axisY, currentPkg) }
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            requestFocus()
            post { requestFocus() }
        }
    }
}
