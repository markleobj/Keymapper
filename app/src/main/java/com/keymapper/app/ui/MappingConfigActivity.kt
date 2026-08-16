package com.keymapper.app.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.keymapper.app.AppContainer
import com.keymapper.app.model.ActionStep
import com.keymapper.app.model.ActionType
import com.keymapper.app.model.MappingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MappingConfigActivity : AppCompatActivity() {

    private lateinit var tvPickedButton: TextView
    private lateinit var spinnerAction: Spinner
    private lateinit var etName: EditText
    private lateinit var etTargetX: EditText
    private lateinit var etTargetY: EditText
    private lateinit var etDuration: EditText
    private lateinit var durationGroup: LinearLayout
    private lateinit var btnPickButton: AppCompatButton
    private lateinit var swBlocked: Switch
    private lateinit var comboStepsContainer: LinearLayout
    private lateinit var comboGroup: LinearLayout

    private var app: AppContainer? = null
    private var editingId: String? = null
    private var selectedButton: String = ""
    private var selectedTargetX: Float = 0.5f
    private var selectedTargetY: Float = 0.5f
    private var selectedActionType: ActionType = ActionType.TAP
    private var longPressDuration: Long = 500L
    private var blocked: Boolean = true
    private var configName: String = ""
    private val comboSteps = mutableListOf<ActionStep>()

    companion object {
        const val EXTRA_MAPPING_ID = "mapping_id"
        const val REQUEST_BUTTON_PICKER = 200
        const val EXTRA_PICKED_BUTTON = "picked_button"
    }

    private val actionChinese = mapOf(
        ActionType.TAP to "点击",
        ActionType.LONG_PRESS to "长按",
        ActionType.SWIPE to "滑动",
        ActionType.MOUSE_MOVE to "鼠标模拟移动",
        ActionType.COMBO to "组合动作",
        ActionType.DO_NOTHING to "只屏蔽（不执行）"
    )

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
        val items = ActionType.values().map { actionChinese[it] ?: it.name }
        spinnerAction.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun setupFields() {
        etTargetX.setText("0.5")
        etTargetY.setText("0.5")
        etDuration.setText("500")
        durationGroup.visibility = View.GONE
        comboGroup.visibility = View.GONE
    }

    private fun setupListeners() {
        btnPickButton.setOnClickListener {
            startActivityForResult(Intent(this, ButtonPickerActivity::class.java), REQUEST_BUTTON_PICKER)
        }
        spinnerAction.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedActionType = ActionType.values()[position]
                durationGroup.visibility = if (selectedActionType == ActionType.LONG_PRESS
                        || selectedActionType == ActionType.MOUSE_MOVE) View.VISIBLE else View.GONE
                comboGroup.visibility = if (selectedActionType == ActionType.COMBO) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        findViewById<AppCompatButton>(1001).setOnClickListener { save() }
        findViewById<AppCompatButton>(1002).setOnClickListener { finish() }
    }

    private fun fillFromConfig(config: MappingConfig) {
        configName = config.name
        selectedButton = config.button
        selectedTargetX = config.targetX
        selectedTargetY = config.targetY
        selectedActionType = config.actionType
        longPressDuration = config.duration
        blocked = config.blocked
        comboSteps.clear()
        comboSteps.addAll(config.steps)
        renderComboSteps()

        etName.setText(config.name)
        tvPickedButton.text = selectedButton
        etTargetX.setText(config.targetX.toString())
        etTargetY.setText(config.targetY.toString())
        etDuration.setText(config.duration.toString())
        swBlocked.isChecked = blocked
        val idx = ActionType.values().indexOf(config.actionType)
        if (idx >= 0) spinnerAction.setSelection(idx)
    }

    private fun save() {
        configName = etName.text?.toString()?.trim().orEmpty()
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
        blocked = swBlocked.isChecked

        val config = MappingConfig(
            id = editingId ?: UUID.randomUUID().toString(),
            name = configName,
            button = selectedButton,
            actionType = selectedActionType,
            targetX = selectedTargetX,
            targetY = selectedTargetY,
            duration = longPressDuration,
            enabled = true,
            blocked = blocked,
            steps = if (selectedActionType == ActionType.COMBO) comboSteps.toList() else emptyList()
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

    private fun renderComboSteps() {
        comboStepsContainer.removeAllViews()
        comboSteps.forEachIndexed { i, step ->
            comboStepsContainer.addView(buildStepRow(i, step))
        }
        if (comboSteps.isEmpty()) {
            comboStepsContainer.addView(TextView(this).apply {
                text = "还没有步骤，点下方『添加一步』"
                textSize = 12f
                setTextColor(Color.parseColor("#FF9E9E9E"))
                setPadding(dp(4), dp(4), dp(4), dp(4))
            })
        }
    }

    private fun buildStepRow(index: Int, step: ActionStep): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(4), 0, dp(4))
        }
        row.addView(TextView(this).apply {
            text = "#${index + 1}"
            textSize = 12f
            setTextColor(Color.parseColor("#FF616161"))
            width = dp(28)
        })
        val typeSpinner = Spinner(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            adapter = ArrayAdapter(this@MappingConfigActivity, android.R.layout.simple_spinner_item,
                ActionType.values().filter { it != ActionType.COMBO }.map { actionChinese[it]!! }
            ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            val realTypes = ActionType.values().filter { it != ActionType.COMBO }
            setSelection(realTypes.indexOf(step.type).coerceAtLeast(0))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    comboSteps[index] = comboSteps[index].copy(type = realTypes[pos])
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
        row.addView(typeSpinner)
        val etX = EditText(this).apply {
            hint = "X"
            setText(step.targetX.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            layoutParams = LinearLayout.LayoutParams(dp(48), ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginStart = dp(2) }
            setOnFocusChangeListener { _, _ ->
                val v = text.toString().toFloatOrNull() ?: 0f
                comboSteps[index] = comboSteps[index].copy(targetX = v)
            }
        }
        row.addView(etX)
        val etY = EditText(this).apply {
            hint = "Y"
            setText(step.targetY.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            layoutParams = LinearLayout.LayoutParams(dp(48), ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginStart = dp(2) }
            setOnFocusChangeListener { _, _ ->
                val v = text.toString().toFloatOrNull() ?: 0f
                comboSteps[index] = comboSteps[index].copy(targetY = v)
            }
        }
        row.addView(etY)
        val etDelay = EditText(this).apply {
            hint = "延时ms"
            setText(step.delayMs.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(dp(70), ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginStart = dp(2) }
            setOnFocusChangeListener { _, _ ->
                val v = text.toString().toLongOrNull() ?: 0L
                comboSteps[index] = comboSteps[index].copy(delayMs = v)
            }
        }
        row.addView(etDelay)
        val btnDel = AppCompatButton(this).apply {
            text = "✕"
            textSize = 10f
            layoutParams = LinearLayout.LayoutParams(dp(36), ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginStart = dp(2) }
            setOnClickListener {
                comboSteps.removeAt(index)
                renderComboSteps()
            }
        }
        row.addView(btnDel)
        return row
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

        content.addView(textLabel("📝 映射名称（可选）"))
        etName = EditText(this).apply {
            hint = "例如：抖音点赞 / 游戏技能1"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }
        content.addView(etName)

        content.addView(textLabel("🎮 手柄按键"))
        tvPickedButton = TextView(this).apply {
            text = "未选择"
            textSize = 16f
            setTextColor(Color.parseColor("#FF3F51B5"))
            setPadding(0, dp(4), 0, dp(4))
        }
        content.addView(tvPickedButton)
        btnPickButton = AppCompatButton(this).apply { text = "🎬 录制按键" }
        content.addView(btnPickButton)

        // ---- 屏蔽原事件 ----
        val blockRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(4))
        }
        blockRow.addView(TextView(this).apply {
            text = "🚫 屏蔽原按键"
            textSize = 14f
            setTextColor(Color.parseColor("#FF212121"))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        swBlocked = Switch(this).apply { isChecked = true }
        blockRow.addView(swBlocked)
        content.addView(blockRow)
        content.addView(TextView(this).apply {
            text = "（开启后该手柄键不会触发系统默认行为，只执行你的映射）"
            textSize = 11f; setTextColor(Color.parseColor("#FF9E9E9E"))
        })

        content.addView(textLabel("⚡ 操作类型"))
        spinnerAction = Spinner(this)
        content.addView(spinnerAction)

        content.addView(textLabel("📍 目标坐标 (0~1)"))
        content.addView(TextView(this).apply {
            text = "X=0 屏幕最左 / X=1 最右  ·  Y=0 最上 / Y=1 最下"
            textSize = 10f; setTextColor(Color.parseColor("#FF9E9E9E"))
        })
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
        durationGroup.addView(textLabel("⏱ 时长 (ms)"))
        etDuration = EditText(this).apply {
            hint = "500"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        durationGroup.addView(etDuration)
        content.addView(durationGroup)

        // ---- COMBO 步骤编辑 ----
        comboGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, dp(4))
        }
        comboGroup.addView(textLabel("🎬 组合动作步骤"))
        comboGroup.addView(TextView(this).apply {
            text = "每个步骤有：动作类型 + 坐标 + 之前延时"
            textSize = 11f; setTextColor(Color.parseColor("#FF9E9E9E"))
        })
        comboStepsContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        comboGroup.addView(comboStepsContainer)
        val btnAddStep = AppCompatButton(this).apply {
            text = "➕ 添加一步"
            setOnClickListener {
                comboSteps.add(ActionStep(type = ActionType.TAP, targetX = 0.5f, targetY = 0.5f, delayMs = 100))
                renderComboSteps()
            }
        }
        comboGroup.addView(btnAddStep)
        content.addView(comboGroup)

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
