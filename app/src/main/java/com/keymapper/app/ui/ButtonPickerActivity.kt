package com.keymapper.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.keymapper.app.model.HidButtonEvent
import com.keymapper.app.service.InputMonitor

class ButtonPickerActivity : AppCompatActivity() {

    private var capturedButton: String? = null
    private lateinit var statusView: TextView
    private lateinit var subView: TextView

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

        subView = TextView(this).apply {
            text = "通过 Shizuku 实时监听手柄按键\n请按下要绑定的按键，或按 BACK 取消"
            textSize = 13f; gravity = Gravity.CENTER; setTextColor(0xFF616161.toInt())
            setPadding(32, 0, 32, 16)
        }
        root.addView(subView)

        statusView = TextView(this).apply {
            text = "⌛ 等待按键..."
            textSize = 22f; gravity = Gravity.CENTER
            setTextColor(0xFF1976D2.toInt())
            setPadding(32, 48, 32, 32)
        }
        root.addView(statusView)

        val cancel = Button(this).apply {
            text = "取消"
            setOnClickListener { finish() }
        }
        root.addView(cancel)

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        InputMonitor.start(this)
        InputMonitor.setButtonEventListener { event ->
            runOnUiThread {
                handleHidEvent(event)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        InputMonitor.setButtonEventListener(null)
    }

    private fun handleHidEvent(event: HidButtonEvent) {
        if (!event.isPressed) return

        val btn = event.buttonId
        if (btn.isBlank()) return

        statusView.text = "✅ 捕获到: $btn"
        statusView.setTextColor(0xFF4CAF50.toInt())
        subView.text = "来源: ${event.deviceName ?: "unknown"}\n按键已锁定，正在返回..."

        capturedButton = btn
        val result = Intent().putExtra("BUTTON", btn)
        setResult(RESULT_OK, result)

        android.os.Handler(mainLooper).postDelayed({ finish() }, 400)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event ?: return false
        val btn = InputMonitor.keyEventToButton(event)
        if (btn == "UNKNOWN") return super.onKeyDown(keyCode, event)

        if (capturedButton == null) {
            statusView.text = "✅ 捕获到(KeyEvent): $btn"
            statusView.setTextColor(0xFF4CAF50.toInt())
            capturedButton = btn
            val result = Intent().putExtra("BUTTON", btn)
            setResult(RESULT_OK, result)
            android.os.Handler(mainLooper).postDelayed({ finish() }, 400)
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        event ?: return false
        return capturedButton != null || super.onKeyUp(keyCode, event)
    }

    override fun onDestroy() {
        InputMonitor.setButtonEventListener(null)
        super.onDestroy()
    }
}
