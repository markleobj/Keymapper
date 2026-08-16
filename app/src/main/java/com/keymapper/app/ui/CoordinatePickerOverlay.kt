package com.keymapper.app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.keymapper.app.R
import com.keymapper.app.service.KeyMapperAccessibilityService

class CoordinatePickerOverlay(private val context: Context) {

    companion object {
        private const val TAG = "CoordPickerOverlay"
        const val ACTION_PICK_RESULT = "com.keymapper.app.COORD_PICK_RESULT"
        const val EXTRA_X = "x"
        const val EXTRA_Y = "y"
        const val ACTION_PICK_CANCEL = "com.keymapper.app.COORD_PICK_CANCEL"

        @Volatile
        private var current: CoordinatePickerOverlay? = null

        fun isShowing() = current != null

        fun dismiss() {
            current?.remove()
            current = null
        }
    }

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null

    private fun bringK2erToFront() {
        KeyMapperAccessibilityService.instance?.bringK2erTaskToFront()
            ?: run {
                Log.w(TAG, "AccessibilityService 未启动，fallback startActivity")
                try {
                    context.startActivity(Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                } catch (e: Throwable) {
                    Log.e(TAG, "fallback startActivity 也失败", e)
                }
            }
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    fun show() {
        if (current != null) {
            Log.w(TAG, "already showing, dismiss old one first")
            dismiss()
        }

        val view = LayoutInflater.from(context).inflate(R.layout.view_coord_picker_overlay, null, false)
        val tvHint = view.findViewById<TextView>(R.id.picker_hint)
        val tvCoord = view.findViewById<TextView>(R.id.picker_coord)
        val tvCancel = view.findViewById<TextView>(R.id.picker_cancel)

        val dm = context.resources.displayMetrics
        val screenW = dm.widthPixels
        val screenH = dm.heightPixels

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            screenW, screenH, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0; y = 0
        }

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val rawX = event.rawX
                    val rawY = event.rawY
                    val nx = (rawX / screenW).coerceIn(0f, 1f)
                    val ny = (rawY / screenH).coerceIn(0f, 1f)
                    tvCoord.text = String.format("X=%.2f  Y=%.2f", nx, ny)
                    tvHint.text = "✅ 已拾取！松开手指返回"
                }
                MotionEvent.ACTION_UP -> {
                    val rawX = event.rawX
                    val rawY = event.rawY
                    val nx = (rawX / screenW).coerceIn(0f, 1f)
                    val ny = (rawY / screenH).coerceIn(0f, 1f)
                    Log.i(TAG, "picked: raw=($rawX,$rawY) norm=($nx,$ny)")

                    val intent = Intent(ACTION_PICK_RESULT).apply {
                        putExtra(EXTRA_X, nx)
                        putExtra(EXTRA_Y, ny)
                    }
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    sendGlobalBroadcast(intent)

                    Toast.makeText(context, String.format("已拾取 X=%.2f Y=%.2f", nx, ny), Toast.LENGTH_SHORT).show()
                    remove()
                    current = null
                    bringK2erToFront()
                }
                MotionEvent.ACTION_MOVE -> {
                    val rawX = event.rawX
                    val rawY = event.rawY
                    val nx = (rawX / screenW).coerceIn(0f, 1f)
                    val ny = (rawY / screenH).coerceIn(0f, 1f)
                    tvCoord.text = String.format("X=%.2f  Y=%.2f", nx, ny)
                }
            }
            true
        }

        tvCancel.setOnClickListener {
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(ACTION_PICK_CANCEL))
            sendGlobalBroadcast(Intent(ACTION_PICK_CANCEL))
            remove()
            current = null
            bringK2erToFront()
        }

        try {
            windowManager.addView(view, params)
            overlayView = view
            current = this
            Log.i(TAG, "overlay shown ${screenW}x${screenH}")
        } catch (e: Throwable) {
            Log.e(TAG, "show overlay failed", e)
        }
    }

    private fun sendGlobalBroadcast(intent: Intent) {
        try {
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
        } catch (_: Exception) {}
    }

    private fun remove() {
        try {
            overlayView?.let { windowManager.removeViewImmediate(it) }
        } catch (e: Throwable) {
            Log.e(TAG, "remove overlay failed", e)
        }
        overlayView = null
    }
}
