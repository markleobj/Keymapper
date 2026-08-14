package com.keymapper.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.keymapper.app.KeyMapperApp
import com.keymapper.app.R
import com.keymapper.app.bluetooth.ConnectionState
import com.keymapper.app.databinding.ActivityButtonPickerBinding
import com.keymapper.app.model.HidButtonEvent
import com.keymapper.app.mapping.MappingEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ButtonPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityButtonPickerBinding
    private lateinit var app: KeyMapperApp

    private var captureJob: Job? = null
    private var lastButton: HidButtonEvent? = null
    private var waitingForRelease = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityButtonPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = application as KeyMapperApp

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_button_picker)
        binding.toolbar.setNavigationOnClickListener { finish() }

        if (app.bluetoothController.connectionState.value != ConnectionState.CONNECTED) {
            binding.tvStatus.text = "请先连接手柄"
            binding.btnConfirm.isEnabled = false
        } else {
            startCapturing()
        }

        binding.btnConfirm.setOnClickListener {
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
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun startCapturing() {
        binding.tvStatus.text = "按下手柄按键…"
        captureJob?.cancel()
        captureJob = lifecycleScope.launch {
            app.bluetoothController.buttonEvents.collect { event ->
                if (event.isPressed) {
                    lastButton = event
                    binding.tvDetected.text = "已捕获：${event.buttonName} (${event.buttonId})"
                    binding.btnConfirm.isEnabled = true
                    waitingForRelease = true
                }
            }
        }
    }

    override fun onDestroy() {
        captureJob?.cancel()
        super.onDestroy()
    }
}
