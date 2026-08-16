package com.keymapper.app.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.InputDevice
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
import androidx.lifecycle.lifecycleScope
import com.keymapper.app.AppContainer
import com.keymapper.app.model.HidButtonEvent
import com.keymapper.app.service.KeyMapperAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ButtonPickerActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvDetected: TextView
    private lateinit var tvLog: TextView
    private lateinit var btnConfirm: AppCompatButton
    private lateinit var btnCancel: AppCompatButton
    private lateinit var btnClear: AppCompatButton

    private var app: AppContainer? = null
    private var captureJob: Job? = null
    private var lastButton: HidButtonEvent? = null
    private val logLines = mutableListOf<String>()

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

        tvStatus.text = "按下手柄任意按键…"
        appendLog("ButtonPickerActivity 已启动，等待按键…")
        startCapturing()
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        event ?: return super.dispatchKeyEvent(null)
        if (event.repeatCount > 0) return super.dispatchKeyEvent(event)

        val actionName = when (event.action) {
            KeyEvent.ACTION_DOWN -> "DOWN"
            KeyEvent.ACTION_UP   -> "UP"
            else -> "MISC"
        }
        val srcName = sourceToString(event.source)
        appendLog("dispatchKeyEvent: keyCode=${event.keyCode} $actionName src=$srcName device=${event.device?.name}")

        if (event.action == KeyEvent.ACTION_DOWN) {
            handleKeyDown(event.keyCode, event.source, event.device?.name)
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event ?: return super.onKeyDown(keyCode, null)
        if (event.repeatCount > 0) return super.onKeyDown(keyCode, event)
        appendLog("onKeyDown: keyCode=$keyCode src=${sourceToString(event.source)}")
        handleKeyDown(keyCode, event.source, event.device?.name)
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        event ?: return super.onKeyUp(keyCode, null)
        appendLog("onKeyUp: keyCode=$keyCode src=${sourceToString(event.source)}")
        return super.onKeyUp(keyCode, event)
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        event ?: return super.onGenericMotionEvent(null)
        if (event.action == MotionEvent.ACTION_MOVE) {
            val sb = StringBuilder("onGenericMotion: src=${sourceToString(event.source)} ")
            for (axis in arrayOf(MotionEvent.AXIS_X, MotionEvent.AXIS_Y, MotionEvent.AXIS_Z,
                MotionEvent.AXIS_RX, MotionEvent.AXIS_RY, MotionEvent.AXIS_RZ,
                MotionEvent.AXIS_LTRIGGER, MotionEvent.AXIS_RTRIGGER,
                MotionEvent.AXIS_HAT_X, MotionEvent.AXIS_HAT_Y)) {
                val v = event.getAxisValue(axis)
                if (v != 0f) sb.append(axisName(axis)).append("=").append("%.2f".format(v)).append(" ")
            }
            val msg = sb.toString()
            if (msg.length > 40) appendLog(msg)
        }
        return super.onGenericMotionEvent(event)
    }

    private fun handleKeyDown(keyCode: Int, source: Int, deviceName: String?) {
        val btn = KeyMapperAccessibilityService.keyEventToButton(keyCode)
        val finalBtn = btn ?: run {
            val rawId = "RAW_$keyCode"
            val rawName = "键#$keyCode"
            HidButtonEvent(rawId, rawName, true)
        }
        lastButton = finalBtn
        val device = deviceName ?: sourceToString(source)
        tvDetected.text = "已捕获：${finalBtn.buttonName} (${finalBtn.buttonId})\n设备: $device"
        btnConfirm.isEnabled = true
        Log.i("ButtonPicker", "捕获按键: keyCode=$keyCode -> ${finalBtn.buttonName}")
    }

    /** 无障碍服务按键事件流（优先级更高，因为能捕获全局事件）。 */
    private fun startCapturing() {
        captureJob?.cancel()
        captureJob = lifecycleScope.launch(Dispatchers.Default) {
            try {
                app!!.bluetoothController.buttonEvents.collect { event ->
                    if (event.isPressed) {
                        withContext(Dispatchers.Main) {
                            lastButton = event
                            tvDetected.text = "已捕获：${event.buttonName} (${event.buttonId})\n来源: 无障碍服务"
                            btnConfirm.isEnabled = true
                            appendLog("无障碍服务事件: ${event.buttonName}")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvStatus.text = "捕获出错: ${e.message}"
                }
            }
        }
    }

    private fun appendLog(line: String) {
        logLines.add(line)
        if (logLines.size > 100) logLines.removeAt(0)
        tvLog?.let { tv ->
            val text = logLines.joinToString("\n")
            tv.text = text
            tv.post { tv.parent.requestChildFocus(tv, tv) }
        }
        Log.d("ButtonPicker", line)
    }

    override fun onDestroy() {
        captureJob?.cancel()
        super.onDestroy()
    }

    private fun sourceToString(source: Int): String {
        return when {
            source and InputDevice.SOURCE_GAMEPAD != 0 -> "GAMEPAD"
            source and InputDevice.SOURCE_JOYSTICK != 0 -> "JOYSTICK"
            source and InputDevice.SOURCE_KEYBOARD != 0 -> "KEYBOARD"
            source and InputDevice.SOURCE_DPAD != 0 -> "DPAD"
            source and InputDevice.SOURCE_TOUCHSCREEN != 0 -> "TOUCHSCREEN"
            source and InputDevice.SOURCE_MOUSE != 0 -> "MOUSE"
            source and InputDevice.SOURCE_TOUCHPAD != 0 -> "TOUCHPAD"
            else -> "SOURCE_$source"
        }
    }

    private fun axisName(axis: Int): String = when (axis) {
        MotionEvent.AXIS_X -> "X"; MotionEvent.AXIS_Y -> "Y"
        MotionEvent.AXIS_Z -> "Z"; MotionEvent.AXIS_RX -> "RX"
        MotionEvent.AXIS_RY -> "RY"; MotionEvent.AXIS_RZ -> "RZ"
        MotionEvent.AXIS_LTRIGGER -> "L2"; MotionEvent.AXIS_RTRIGGER -> "R2"
        MotionEvent.AXIS_HAT_X -> "HX"; MotionEvent.AXIS_HAT_Y -> "HY"
        else -> "AXIS_$axis"
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
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        tvStatus = TextView(this).apply {
            text = "按下手柄任意按键…"
            textSize = 18f
            setTextColor(Color.parseColor("#FF212121"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, dp(8), 0, dp(8))
        }
        content.addView(tvStatus)

        tvDetected = TextView(this).apply {
            text = "（等待按键）"
            textSize = 16f
            setTextColor(Color.parseColor("#FF3F51B5"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, dp(8), 0, dp(12))
            setBackgroundColor(Color.parseColor("#FFE8EAF6"))
        }
        content.addView(tvDetected)

        btnConfirm = AppCompatButton(this).apply { text = "✅ 确认此按键"; isEnabled = false }
        content.addView(btnConfirm)

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
            setPadding(0, dp(16), 0, dp(4))
        })

        tvLog = TextView(this).apply {
            text = ""
            textSize = 11f
            setTextColor(Color.parseColor("#FF424242"))
            setBackgroundColor(Color.parseColor("#FF212121"))
            setTextColor(Color.parseColor("#FF8BC34A"))
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
