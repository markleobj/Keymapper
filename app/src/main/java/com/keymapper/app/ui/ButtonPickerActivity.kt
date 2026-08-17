package com.keymapper.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.keymapper.app.service.InputMonitor

class ButtonPickerActivity : AppCompatActivity() {

    private var capturedButton: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF5F5F5.toInt())
        }

        val title = TextView(this).apply {
            text = "🎮 请按下手柄按键"
            textSize = 20f; gravity = Gravity.CENTER
            setPadding(32, 48, 32, 16)
        }
        root.addView(title)

        val sub = TextView(this).apply {
            text = "按一下手柄上要绑定的按键，或者按 BACK 取消"
            textSize = 14f; gravity = Gravity.CENTER; setTextColor(0xFF616161.toInt())
            setPadding(32, 0, 32, 16)
        }
        root.addView(sub)

        val status = TextView(this).apply {
            text = "⌛ 等待按键..."
            textSize = 18f; gravity = Gravity.CENTER
            setTextColor(0xFF1976D2.toInt())
            setPadding(32, 48, 32, 32)
        }
        root.addView(status)

        val cancel = Button(this).apply {
            text = "取消"
            setOnClickListener { finish() }
        }
        root.addView(cancel)

        setContentView(root)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event ?: return false
        val btn = InputMonitor.keyEventToButton(event)
        if (btn == "UNKNOWN") return super.onKeyDown(keyCode, event)

        capturedButton = btn
        val result = Intent().putExtra("BUTTON", btn)
        setResult(RESULT_OK, result)
        finish()
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        event ?: return false
        return capturedButton != null || super.onKeyUp(keyCode, event)
    }
}
