package com.keymapper.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.WindowManager
import kotlin.math.max
import kotlin.math.min

class KeyMapperAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AccessibilityService"
        var instance: KeyMapperAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null
    }

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

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
        Log.i(TAG, "service connected: ${screenWidth}x${screenHeight}")
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    /**
     * Perform a tap at normalized coordinates (0..1 range relative to screen).
     */
    fun performTap(normX: Float, normY: Float): Boolean {
        val x = clampX(normX)
        val y = clampY(normY)
        val path = Path().apply { moveTo(x, y); lineTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 80L)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null).also {
            Log.d(TAG, "tap ($x,$y) -> $it")
        }
    }

    fun performLongPress(normX: Float, normY: Float, durationMs: Long): Boolean {
        val x = clampX(normX)
        val y = clampY(normY)
        val path = Path().apply { moveTo(x, y); lineTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0L, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null).also {
            Log.d(TAG, "longPress ($x,$y,${durationMs}ms) -> $it")
        }
    }

    fun performSwipe(fromNormX: Float, fromNormY: Float, toNormX: Float, toNormY: Float, durationMs: Long): Boolean {
        val fx = clampX(fromNormX)
        val fy = clampY(fromNormY)
        val tx = clampX(toNormX)
        val ty = clampY(toNormY)
        val path = Path().apply { moveTo(fx, fy); lineTo(tx, ty) }
        val stroke = GestureDescription.StrokeDescription(path, 0L, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null).also {
            Log.d(TAG, "swipe ($fx,$fy)->($tx,$ty) -> $it")
        }
    }

    private fun clampX(normX: Float): Float {
        val v = if (normX <= 1f) normX * screenWidth else normX
        return max(10f, min(screenWidth - 10f, v))
    }

    private fun clampY(normY: Float): Float {
        val v = if (normY <= 1f) normY * screenHeight else normY
        return max(10f, min(screenHeight - 10f, v))
    }
}
