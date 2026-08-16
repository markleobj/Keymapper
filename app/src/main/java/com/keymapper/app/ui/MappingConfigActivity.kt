package com.keymapper.app.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.keymapper.app.AppContainer
import com.keymapper.app.model.ActionType
import com.keymapper.app.model.MappingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MappingConfigActivity : AppCompatActivity() {

    private lateinit var tvPickedButton: TextView
    private lateinit var spinnerAction: Spinner
    private lateinit var etTargetX: EditText
    private lateinit var etTargetY: EditText
    private lateinit var etDuration: EditText
    private lateinit var durationGroup: LinearLayout
    private lateinit var btnPickButton: AppCompatButton

    private var app: AppContainer? = null
    private var editingId: String? = null
    private var selectedButton: String = ""
    private var selectedTargetX: Float = 0.5f
    private var selectedTargetY: Float = 0.5f
    private var selectedActionType: ActionType = ActionType.TAP
    private var longPressDuration: Long = 500L

    companion object {
        const val EXTRA_MAPPING_ID = "mapping_id"
        const val REQUEST_BUTTON_PICKER = 200
        const val EXTRA_PICKED_BUTTON = "picked_button"
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

        editingId = intent.getStringExtra(EXTRA_MAPPING_ID)
        setupActionTypeSpinner()
        setupFields()
        setupListeners()

        editingId?.let { id ->
            lifecycleScope.launch(Dispatchers.Default) {
                try {
                    app!!.mappingRepository.mappings.collect { list ->
                        list.firstOrNull { it.id == id }?.let { config ->
                            withContext(Dispatchers.Main) { fillFromConfig(config) }
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun setupActionTypeSpinner() {
        val items = listOf("点击", "长按", "滑动")
        spinnerAction.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun setupFields() {
        etTargetX.setText("0.5")
        etTargetY.setText("0.5")
        etDuration.setText("500")
        durationGroup.visibility = View.GONE
    }

    private fun setupListeners() {
        btnPickButton.setOnClickListener {
            startActivityForResult(Intent(this, ButtonPickerActivity::class.java), REQUEST_BUTTON_PICKER)
        }
        spinnerAction.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedActionType = ActionType.values()[position]
                durationGroup.visibility = if (selectedActionType == ActionType.LONG_PRESS) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        findViewById<AppCompatButton>(1001).setOnClickListener { save() }
        findViewById<AppCompatButton>(1002).setOnClickListener { finish() }
    }

    private fun fillFromConfig(config: MappingConfig) {
        selectedButton = config.button
        selectedTargetX = config.targetX
        selectedTargetY = config.targetY
        selectedActionType = config.actionType
        longPressDuration = config.duration
        tvPickedButton.text = selectedButton
        etTargetX.setText(config.targetX.toString())
        etTargetY.setText(config.targetY.toString())
        etDuration.setText(config.duration.toString())
        val idx = ActionType.values().indexOf(config.actionType)
        if (idx >= 0) spinnerAction.setSelection(idx)
    }

    private fun save() {
        selectedButton = tvPickedButton.text.toString().ifBlank { selectedButton }
        if (selectedButton.isBlank()) {
            Toast.makeText(this, "请先选择手柄按键", Toast.LENGTH_SHORT).show()
            return
        }
        val x = etTargetX.text.toString().toFloatOrNull()
        val y = etTargetY.text.toString().toFloatOrNull()
        if (x == null || y == null || x !in 0f..1f || y !in 0f..1f) {
            Toast.makeText(this, "坐标需在 0-1 范围（屏幕比例）", Toast.LENGTH_SHORT).show()
            return
        }
        selectedTargetX = x
        selectedTargetY = y
        longPressDuration = etDuration.text.toString().toLongOrNull() ?: 500L

        val config = MappingConfig(
            id = editingId ?: UUID.randomUUID().toString(),
            button = selectedButton,
            actionType = selectedActionType,
            targetX = selectedTargetX,
            targetY = selectedTargetY,
            duration = longPressDuration,
            enabled = true
        )
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                app!!.mappingRepository.add(config)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MappingConfigActivity, "已保存", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MappingConfigActivity, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BUTTON_PICKER && resultCode == RESULT_OK) {
            val btn = data?.getStringExtra(EXTRA_PICKED_BUTTON) ?: return
            selectedButton = btn
            tvPickedButton.text = btn
        }
    }

    private fun buildProgrammaticUI(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FFF5F5F5"))
        }

        val toolbar = Toolbar(this).apply {
            setBackgroundColor(Color.parseColor("#FF3F51B5"))
            setTitleTextColor(Color.WHITE)
            title = "配置映射"
        }
        root.addView(toolbar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)))
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val scrollView = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        content.addView(textLabel("手柄按键"))
        tvPickedButton = TextView(this).apply {
            text = "未选择"
            textSize = 16f
            setTextColor(Color.parseColor("#FF3F51B5"))
            setPadding(0, dp(4), 0, dp(8))
        }
        content.addView(tvPickedButton)

        btnPickButton = AppCompatButton(this).apply { text = "🎮 录制按键" }
        content.addView(btnPickButton)

        content.addView(textLabel("操作类型"))
        spinnerAction = Spinner(this)
        content.addView(spinnerAction)

        content.addView(textLabel("点击位置 (0~1)"))
        val coordRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        etTargetX = EditText(this).apply {
            hint = "X"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        etTargetY = EditText(this).apply {
            hint = "Y"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(8) }
        }
        coordRow.addView(etTargetX)
        coordRow.addView(etTargetY)
        content.addView(coordRow)

        durationGroup = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        durationGroup.addView(textLabel("长按时长 (ms)"))
        etDuration = EditText(this).apply {
            hint = "500"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        durationGroup.addView(etDuration)
        content.addView(durationGroup)

        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(16), 0, 0)
        }
        val saveBtn = AppCompatButton(this).apply {
            text = "💾 保存"
            id = 1001
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val cancelBtn = AppCompatButton(this).apply {
            text = "取消"
            id = 1002
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(8) }
        }
        btnRow.addView(saveBtn)
        btnRow.addView(cancelBtn)
        content.addView(btnRow)

        scrollView.addView(content)
        root.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        return root
    }

    private fun textLabel(text: String) = TextView(this).apply {
        this.text = text
        textSize = 14f
        setTextColor(Color.parseColor("#FF666666"))
        setPadding(0, dp(12), 0, dp(2))
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
