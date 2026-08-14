package com.keymapper.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.keymapper.app.KeyMapperApp
import com.keymapper.app.R
import com.keymapper.app.bluetooth.ConnectionState
import com.keymapper.app.databinding.ActivityMainBinding
import com.keymapper.app.mapping.MappingAdapter
import com.keymapper.app.service.KeyMapperAccessibilityService
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var app: KeyMapperApp

    private var PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = application as KeyMapperApp

        setupToolbar()
        setupDeviceTab()
        setupMappingTab()
        setupAccessibilityCheck()
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        setupAccessibilityCheck()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupAccessibilityCheck() {
        if (!KeyMapperAccessibilityService.isRunning()) {
            binding.accessibilityCard.visibility = android.view.View.VISIBLE
            binding.btnOpenSettings.setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        } else {
            binding.accessibilityCard.visibility = android.view.View.GONE
        }
        val serviceRunning = KeyMapperAccessibilityService.isRunning()
        app.mappingEngine.setEnabled(serviceRunning && app.bluetoothController.connectionState.value == ConnectionState.CONNECTED)
    }

    private fun setupDeviceTab() {
        val adapter = DeviceAdapter(
            onConnect = { address ->
                lifecycleScope.launch {
                    if (app.bluetoothController.connect(address)) {
                        Toast.makeText(this@MainActivity, "正在连接…", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "连接失败", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDisconnect = {
                app.bluetoothController.disconnect()
            }
        )
        binding.devicesRecycler.layoutManager = LinearLayoutManager(this)
        binding.devicesRecycler.adapter = adapter

        lifecycleScope.launch {
            // Collect connection state + devices
            app.bluetoothController.connectionState.collect { state ->
                adapter.updateConnectionState(state)
                setupAccessibilityCheck()
            }
        }
        lifecycleScope.launch {
            app.bluetoothController.connectedDevice.collect { device ->
                adapter.updateConnectedDevice(device?.address)
            }
        }
        lifecycleScope.launch {
            val devices = app.bluetoothController.getPairedDevices()
            adapter.submitList(devices)
        }

        binding.btnRefreshDevices.setOnClickListener {
            lifecycleScope.launch {
                val devices = app.bluetoothController.getPairedDevices()
                adapter.submitList(devices)
            }
        }
    }

    private fun setupMappingTab() {
        val adapter = MappingAdapter(
            onToggle = { config, enabled ->
                lifecycleScope.launch {
                    app.mappingRepository.update(config.copy(enabled = enabled))
                }
            },
            onDelete = { config ->
                lifecycleScope.launch {
                    app.mappingRepository.remove(config.id)
                }
            },
            onEdit = { config ->
                startActivity(Intent(this, MappingConfigActivity::class.java).apply {
                    putExtra(MappingConfigActivity.EXTRA_MAPPING_ID, config.id)
                })
            }
        )
        binding.mappingsRecycler.layoutManager = LinearLayoutManager(this)
        binding.mappingsRecycler.adapter = adapter

        lifecycleScope.launch {
            app.mappingRepository.mappings.collect { list ->
                adapter.submitList(list)
                binding.emptyMappings.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }

        binding.btnAddMapping.setOnClickListener {
            startActivity(Intent(this, MappingConfigActivity::class.java))
        }
    }

    private fun requestPermissions() {
        val perms = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms += Manifest.permission.BLUETOOTH_SCAN
            perms += Manifest.permission.BLUETOOTH_CONNECT
        }
        perms += Manifest.permission.ACCESS_FINE_LOCATION
        perms += Manifest.permission.ACCESS_COARSE_LOCATION

        val need = perms.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (need.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, need.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }
}
