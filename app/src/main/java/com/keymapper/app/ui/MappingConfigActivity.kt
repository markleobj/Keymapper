package com.keymapper.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.keymapper.app.KeyMapperApp
import com.keymapper.app.R
import com.keymapper.app.databinding.ActivityMappingConfigBinding
import com.keymapper.app.model.ActionType
import com.keymapper.app.model.MappingConfig
import kotlinx.coroutines.launch
import java.util.UUID

class MappingConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMappingConfigBinding
    private lateinit var app: KeyMapperApp

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
        binding = ActivityMappingConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = application as KeyMapperApp

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_mapping_config)
        binding.toolbar.setNavigationOnClickListener { finish() }

        editingId = intent.getStringExtra(EXTRA_MAPPING_ID)

        setupActionTypeSpinner()
        setupFields()
        setupListeners()

        editingId?.let { id ->
            lifecycleScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                app.mappingRepository.mappings.collect { list ->
                    list.firstOrNull { it.id == id }?.let { config ->
                        fillFromConfig(config)
                    }
                }
            }
        }
    }

    private fun setupActionTypeSpinner() {
        val items = ActionType.values().map { it.name }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAction.adapter = spinnerAdapter
    }

    private fun setupFields() {
        binding.etTargetX.setText("0.5")
        binding.etTargetY.setText("0.5")
        binding.etDuration.setText("500")
    }

    private fun setupListeners() {
        binding.btnPickButton.setOnClickListener {
            startActivityForResult(Intent(this, ButtonPickerActivity::class.java), REQUEST_BUTTON_PICKER)
        }
        binding.spinnerAction.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedActionType = ActionType.values()[position]
                binding.durationGroup.visibility = if (selectedActionType == ActionType.LONG_PRESS) {
                    android.view.View.VISIBLE
                } else android.view.View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.btnSave.setOnClickListener { save() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun fillFromConfig(config: MappingConfig) {
        selectedButton = config.button
        selectedTargetX = config.targetX
        selectedTargetY = config.targetY
        selectedActionType = config.actionType
        longPressDuration = config.duration
        binding.tvPickedButton.text = selectedButton
        binding.etTargetX.setText(config.targetX.toString())
        binding.etTargetY.setText(config.targetY.toString())
        binding.etDuration.setText(config.duration.toString())
        val idx = ActionType.values().indexOf(config.actionType)
        if (idx >= 0) binding.spinnerAction.setSelection(idx)
    }

    private fun save() {
        selectedButton = binding.tvPickedButton.text.toString().ifBlank { selectedButton }
        if (selectedButton.isBlank()) {
            Toast.makeText(this, "请先选择手柄按键", Toast.LENGTH_SHORT).show()
            return
        }
        val x = binding.etTargetX.text.toString().toFloatOrNull()
        val y = binding.etTargetY.text.toString().toFloatOrNull()
        if (x == null || y == null || x !in 0f..1f || y !in 0f..1f) {
            Toast.makeText(this, "坐标需在 0-1 范围（屏幕比例）", Toast.LENGTH_SHORT).show()
            return
        }
        selectedTargetX = x
        selectedTargetY = y
        longPressDuration = binding.etDuration.text.toString().toLongOrNull() ?: 500L

        val config = MappingConfig(
            id = editingId ?: UUID.randomUUID().toString(),
            button = selectedButton,
            actionType = selectedActionType,
            targetX = selectedTargetX,
            targetY = selectedTargetY,
            duration = longPressDuration,
            enabled = true
        )
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            app.mappingRepository.add(config)
            Toast.makeText(this@MappingConfigActivity, "已保存", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BUTTON_PICKER && resultCode == RESULT_OK) {
            val btn = data?.getStringExtra(EXTRA_PICKED_BUTTON) ?: return
            selectedButton = btn
            binding.tvPickedButton.text = btn
        }
    }
}
