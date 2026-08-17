package com.keymapper.app.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CoordinatePickerActivity : AppCompatActivity() {

    private var pickedX: Float = 0f
    private var pickedY: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#66000000"))
        }

        val tip = TextView(this).apply {
            text = "👆 点击屏幕任意位置拾取坐标\n按 BACK 取消"
            textSize = 16f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
            setPadding(32, 64, 32, 32)
        }
        val tipLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL }
        root.addView(tip, tipLp)

        val coordDisplay = TextView(this).apply {
            text = "坐标: (0, 0)"
            textSize = 18f; setTextColor(Color.YELLOW); gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }
        val cdLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.CENTER }
        root.addView(coordDisplay, cdLp)

        val cancel = Button(this).apply {
            text = "取消"
        }
        val cancelLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL; bottomMargin = 64 }
        cancel.setOnClickListener { finish() }
        root.addView(cancel, cancelLp)

        val pickOverlay = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setOnTouchListener { _, e ->
                if (e.action == MotionEvent.ACTION_DOWN) {
                    pickedX = e.x; pickedY = e.y
                    coordDisplay.text = "坐标: (${pickedX.toInt()}, ${pickedY.toInt()})"
                } else if (e.action == MotionEvent.ACTION_UP) {
                    val result = Intent().apply {
                        putExtra("X", pickedX); putExtra("Y", pickedY)
                    }
                    setResult(RESULT_OK, result)
                    finish()
                }
                true
            }
        }
        root.addView(pickOverlay, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        ))

        setContentView(root)
    }
}
