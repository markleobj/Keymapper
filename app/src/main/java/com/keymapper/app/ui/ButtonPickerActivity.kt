package com.keymapper.app.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import com.keymapper.app.AppContainer
import com.keymapper.app.model.HidButtonEvent
import com.keymapper.app.service.KeyMapperAccessibilityService

class ButtonPickerActivity : AppCompatActivity(), KeyMapperAccessibilityService.KeyListener {

    private lateinit var tvStatus: TextView
    private lateinit var tvA11yState: TextView
    private lateinit var tvDetected: TextView
    private lateinit var tvLog: TextView
    private lateinit var btnConfirm: AppCompatButton
    private lateinit var btnCancel: AppCompatButton
    private lateinit var btnClear: AppCompatButton

    private var app: AppContainer? = null
    private var lastButton: HidButtonEvent? = null
    private val logLines = mutableListOf<String>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val statusRunnable = object : Runnable {
        override fun run() {
            updateA11yState()
            mainHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildProgrammaticUI())

        try {
            app = AppContainer.getOrCreate(this)
        } catch (e: Throwable) {
            Toast.makeText(this, "初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
            finish(); return
        }

        btnCancel.setOnClickListener { finish() }
        btnClear.setOnClickListener {
            logLines.clear()
            tvLog.text = ""
        }
        btnConfirm.setOnClickListener {
            val btn = lastButton
            if (btn != null) {
                setResult(RESULT_OK, Intent().apply {
                    putExtra(MappingConfigActivity.EXTRA_PICKED_BUTTON, btn.buttonId)
                })
                finish()
            } else {
                Toast.makeText(this, "还没有捕获到按键", Toast.LENGTH_SHORT).show()
            }
        }

        appendLog("✅ ButtonPickerActivity 已启动")
        appendLog("ℹ️ Android SDK = ${android.os.Build.VERSION.SDK_INT}")
    }

    override fun onResume() {
        super.onResume()
        KeyMapperAccessibilityService.addKeyListener(this)
        mainHandler.post(statusRunnable)
        appendLog("🔔 已向无障碍服务注册按键监听器")
    }

    override fun onPause() {
        super.onPause()
        KeyMapperAccessibilityService.removeKeyListener(this)
        mainHandler.removeCallbacks(statusRunnable)
    }

    override fun onKeyCaptured(event: HidButtonEvent, source: String, deviceName: String?, rawKeyCode: Int) {
        runOnUiThread {
            val device = deviceName ?: source
            appendLog("♿ 无障碍服务 -> keyCode=$rawKeyCode key=${event.buttonName}/${event.buttonId} pressed=${event.isPressed} src=$source dev=$device")
            if (event.isPressed) {
                lastButton = event
                tvDetected.text = "已捕获：${event.buttonName} (${event.buttonId})\n原始keyCode=$rawKeyCode\n设备: $device\n来源: 无障碍服务"
                btnConfirm.isEnabled = true
            }
        }
    }

    override fun onMotionCaptured(button: String, source: String, deviceName: String?) {
        runOnUiThread {
            val device = deviceName ?: source
            appendLog("🎮 onMotionCaptured: button=$button src=$source dev=$device")
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        event ?: return super.dispatchKeyEvent(null)
        if (event.repeatCount > 0) return super.dispatchKeyEvent(event)

        val source = KeyMapperAccessibilityService.sourceToString(event.source)
        appendLog("📱 dispatchKeyEvent: keyCode=${event.keyCode} action=${event.action} src=$source")

        if (event.action == KeyEvent.ACTION_DOWN) {
            val btn = KeyMapperAccessibilityService.keyEventToButton(event)
            lastButton = btn
            tvDetected.text = "已捕获：${btn.buttonName} (${btn.buttonId})\n设备: ${event.device?.name ?: source}\n来源: Activity"
            btnConfirm.isEnabled = true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event ?: return super.onKeyDown(keyCode, null)
        if (event.repeatCount > 0) return super.onKeyDown(keyCode, event)
        val source = KeyMapperAccessibilityService.sourceToString(event.source)
        appendLog("📱 onKeyDown: keyCode=$keyCode src=$source")
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        event ?: return super.onKeyUp(keyCode, null)
        appendLog("📱 onKeyUp: keyCode=$keyCode")
        return super.onKeyUp(keyCode, event)
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        event ?: return super.onGenericMotionEvent(null)
        val source = KeyMapperAccessibilityService.sourceToString(event.source)
        val sb = StringBuilder("🎮 Motion src=$source ")
        for (axis in arrayOf(
            MotionEvent.AXIS_X, MotionEvent.AXIS_Y, MotionEvent.AXIS_Z,
            MotionEvent.AXIS_RX, MotionEvent.AXIS_RY, MotionEvent.AXIS_RZ,
            MotionEvent.AXIS_LTRIGGER, MotionEvent.AXIS_RTRIGGER,
            MotionEvent.AXIS_HAT_X, MotionEvent.AXIS_HAT_Y
        )) {
            val v = event.getAxisValue(axis)
            if (kotlin.math.abs(v) > 0.1f) sb.append(axisName(axis)).append("=${"%.2f".format(v)} ")
        }
        val msg = sb.toString()
        if (msg.length > 20) appendLog(msg)
        return super.onGenericMotionEvent(event)
    }

    private fun updateA11yState() {
        val running = KeyMapperAccessibilityService.isRunning()
        val a11yKeyCount = KeyMapperAccessibilityService.getA11yKeyCount()

        if (running) {
            tvA11yState.text = "♿ 无障碍服务: 运行中 ✅ (已捕获 $a11yKeyCount 个按键)"
            tvA11yState.setTextColor(Color.parseColor("#FF2E7D32"))
        } else {
            tvA11yState.text = "⚠️ 无障碍服务未运行 — 按键可能无法捕获！"
            tvA11yState.setTextColor(Color.parseColor("#FFC62828"))
        }
    }

    private fun appendLog(line: String) {
        logLines.add(line)
        if (logLines.size > 150) logLines.removeAt(0)
        tvLog?.text = logLines.joinToString("\n")
        Log.d("ButtonPicker", line)
    }

    private fun axisName(axis: Int): String = when (axis) {
        MotionEvent.AXIS_X -> "X"; MotionEvent.AXIS_Y -> "Y"
        MotionEvent.AXIS_Z -> "Z"; MotionEvent.AXIS_RX -> "RX"
        MotionEvent.AXIS_RY -> "RY"; MotionEvent.AXIS_RZ -> "RZ"
        MotionEvent.AXIS_LTRIGGER -> "L2"; MotionEvent.AXIS_RTRIGGER -> "R2"
        MotionEvent.AXIS_HAT_X -> "HX"; MotionEvent.AXIS_HAT_Y -> "HY"
        else -> "A$axis"
    }

    private fun buildProgrammaticUI(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FFF5F5F5"))
        }

        val toolbar = Toolbar(this).apply {
            setBackgroundColor(Color.parseColor("#FF3F51B5"))
            setTitleTextColor(Color.WHITE)
            title = "录制按键"
        }
        root.addView(toolbar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)))
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val scrollView = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(16))
        }

        tvA11yState = TextView(this).apply {
            text = "检查中..."
            textSize = 13f
            setPadding(dp(8), dp(6), dp(8), dp(6))
            setBackgroundColor(Color.parseColor("#FFE3F2FD"))
        }
        content.addView(tvA11yState)

        val tvStatus = TextView(this).apply {
            text = "按下手柄任意按键…"
            textSize = 16f
            setTextColor(Color.parseColor("#FF212121"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, dp(16), 0, dp(8))
        }
        content.addView(tvStatus)

        tvDetected = TextView(this).apply {
            text = "（等待按键）"
            textSize = 15f
            setTextColor(Color.parseColor("#FF3F51B5"))
            gravity = android.view.Gravity.CENTER
            setPadding(dp(8), dp(12), dp(8), dp(12))
            setBackgroundColor(Color.parseColor("#FFE8EAF6"))
        }
        content.addView(tvDetected)

        btnConfirm = AppCompatButton(this).apply { text = "✅ 确认此按键"; isEnabled = false }
        content.addView(btnConfirm, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(12) })

        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, 0)
        }
        btnClear = AppCompatButton(this).apply {
            text = "清空日志"
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        btnCancel = AppCompatButton(this).apply {
            text = "取消"
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(8) }
        }
        btnRow.addView(btnClear)
        btnRow.addView(btnCancel)
        content.addView(btnRow)

        content.addView(TextView(this).apply {
            text = "—— 事件日志 ——"
            textSize = 12f
            setTextColor(Color.parseColor("#FF9E9E9E"))
            setPadding(0, dp(20), 0, dp(4))
        })

        tvLog = TextView(this).apply {
            text = ""
            textSize = 11f
            setBackgroundColor(Color.parseColor("#FF1B1B1B"))
            setTextColor(Color.parseColor("#FF00E676"))
            setPadding(dp(8), dp(8), dp(8), dp(8))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(4) }
        }
        content.addView(tvLog)

        scrollView.addView(content)
        root.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        return root
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
