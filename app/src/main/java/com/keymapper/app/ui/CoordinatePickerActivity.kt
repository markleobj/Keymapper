package com.keymapper.app.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CoordinatePickerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CoordPicker"
        const val EXTRA_PICKED_X = "picked_x"
        const val EXTRA_PICKED_Y = "picked_y"
    }

    private lateinit var tvHint: TextView
    private lateinit var tvCoord: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#66000000"))
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(48), dp(16), dp(16))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        tvHint = TextView(this).apply {
            text = "👆 点屏幕任意位置拾取坐标"
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#CC000000"))
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        content.addView(tvHint)

        tvCoord = TextView(this).apply {
            text = "X=0.00  Y=0.00"
            textSize = 22f
            setTextColor(Color.parseColor("#00E676"))
            gravity = Gravity.CENTER
            setPadding(0, dp(20), 0, dp(8))
        }
        content.addView(tvCoord)

        val tvGuide = TextView(this).apply {
            text = "X=0 最左  X=1 最右  |  Y=0 最上  Y=1 最下\n点屏幕 → 返回映射页自动填入  |  按返回键取消"
            textSize = 12f
            setTextColor(Color.parseColor("#BDBDBD"))
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(8), dp(16), dp(16))
        }
        content.addView(tvGuide)

        val cancelBtn = TextView(this).apply {
            text = "✕ 取消"
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#CC000000"))
            setPadding(dp(24), dp(12), dp(24), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.END; topMargin = dp(24) }
            setOnClickListener { finish() }
        }
        content.addView(cancelBtn)

        root.addView(content)
        setContentView(root)

        Log.i(TAG, "CoordinatePicker shown")
        Toast.makeText(this, "请在屏幕上点一下目标位置", Toast.LENGTH_LONG).show()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val rawX = event.rawX
            val rawY = event.rawY
            val displayMetrics = resources.displayMetrics
            val screenW = displayMetrics.widthPixels.toFloat()
            val screenH = displayMetrics.heightPixels.toFloat()
            val nx = (rawX / screenW).coerceIn(0f, 1f)
            val ny = (rawY / screenH).coerceIn(0f, 1f)

            val label = String.format("X=%.2f  Y=%.2f", nx, ny)
            tvCoord.text = label
            Log.i(TAG, "picked: raw=($rawX,$rawY) screen=${screenW}x${screenH} norm=($nx,$ny)")

            runOnUiThread {
                Toast.makeText(this, "已拾取 $label，返回映射页", Toast.LENGTH_SHORT).show()
            }

            val intent = Intent().apply {
                putExtra(EXTRA_PICKED_X, nx)
                putExtra(EXTRA_PICKED_Y, ny)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
            return true
        }
        return true
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
