package com.keymapper.app.floating

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView

class FloatingCoordinatePicker(private val context: Context) {

    companion object {
        @Volatile private var instance: FloatingCoordinatePicker? = null
        fun getInstance(c: Context): FloatingCoordinatePicker {
            return instance ?: synchronized(this) {
                instance ?: FloatingCoordinatePicker(c.applicationContext).also { instance = it }
            }
        }

        fun isShowing() = instance?.showing == false

        fun showAndLaunch(context: Context, targetPkg: String, onPicked: (x: Float, y: Float) -> Unit) {
            val picker = getInstance(context)
            picker.launchTargetAndShow(targetPkg, onPicked)
        }
    }

    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var showing = false
    private var overlayView: View? = null
    private var currentPickedX = 0f
    private var currentPickedY = 0f

    @SuppressLint("SetTextI18n")
    fun launchTargetAndShow(targetPkg: String, onPicked: (x: Float, y: Float) -> Unit) {
        val pm = context.packageManager
        val targetIntent = pm.getLaunchIntentForPackage(targetPkg)
        if (targetIntent != null) {
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(targetIntent)
        }
        Thread.sleep(400)
        showPicker(onPicked)
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun showPicker(onPicked: (x: Float, y: Float) -> Unit) {
        if (showing) return
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }

        val root = FrameLayout(context).apply {
            setBackgroundColor(Color.parseColor("#22000000"))
        }

        val tipView = TextView(context).apply {
            text = "👆 点屏幕拾取坐标（按 BACK 取消）\n位置: (0, 0)"
            textSize = 16f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
            setShadowLayer(4f, 0f, 2f, Color.BLACK)
            setPadding(64, 128, 64, 64)
        }
        val tipLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL }
        root.addView(tipView, tipLp)

        val cancelHint = TextView(context).apply {
            text = "两指同时点击 / 下拉通知栏关闭"
            textSize = 12f; setTextColor(Color.parseColor("#88FFFFFF")); gravity = Gravity.CENTER
            setPadding(32, 32, 32, 64)
        }
        val chLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL }
        root.addView(cancelHint, chLp)

        var fingerCount = 0
        val touchLayer = View(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setOnTouchListener { _, e ->
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> fingerCount = 1
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        fingerCount++
                        if (fingerCount >= 2) { hidePicker(); return@setOnTouchListener true }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        currentPickedX = e.rawX
                        currentPickedY = e.rawY
                        tipView.text = "👆 点屏幕拾取坐标\n位置: (${currentPickedX.toInt()}, ${currentPickedY.toInt()})"
                    }
                    MotionEvent.ACTION_UP -> {
                        if (fingerCount == 1) {
                            currentPickedX = e.rawX
                            currentPickedY = e.rawY
                            tipView.text = "✅ 已拾取 (${currentPickedX.toInt()}, ${currentPickedY.toInt()})"
                            hidePicker()
                            onPicked(currentPickedX, currentPickedY)
                        }
                    }
                    MotionEvent.ACTION_POINTER_UP -> fingerCount--
                }
                true
            }
        }
        root.addView(touchLayer, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        ))

        try {
            wm.addView(root, params)
            overlayView = root
            showing = true
        } catch (e: Throwable) {
            android.util.Log.e("CoordPicker", "show overlay failed", e)
        }
    }

    private fun hidePicker() {
        val v = overlayView ?: return
        try { wm.removeViewImmediate(v) } catch (_: Throwable) {}
        overlayView = null; showing = false
    }
}
