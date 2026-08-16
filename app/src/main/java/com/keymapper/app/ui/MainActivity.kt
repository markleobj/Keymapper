package com.keymapper.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.keymapper.app.KeyMapperApp
import com.keymapper.app.R
import com.keymapper.app.bluetooth.ConnectionState
import com.keymapper.app.databinding.ActivityMainBinding
import com.keymapper.app.mapping.MappingAdapter
import com.keymapper.app.service.KeyMapperAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var app: KeyMapperApp
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var mappingAdapter: MappingAdapter

    private var PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = application as KeyMapperApp

        setupToolbar()
        requestPermissions()
        setupDeviceTab()
        setupMappingTab()
        setupAccessibilityCheck()
        startEventCollectors()
    }

    override fun onResume() {
        super.onResume()
        try {
            setupAccessibilityCheck()
        } catch (e: Exception) {
            Log.w(TAG, "accessibility check failed", e)
        }
    }

    private fun startEventCollectors() {
        lifecycleScope.launch(Dispatchers.Default) {
            app.bluetoothController.buttonEvents.collect { event ->
                try {
                    app.mappingEngine.onButtonEvent(event)
                } catch (e: Exception) {
                    Log.e(TAG, "button event dispatch failed", e)
                }
            }
        }

        lifecycleScope.launch(Dispatchers.Default) {
            app.mappingRepository.mappings.collect { list ->
                withContext(Dispatchers.Main) {
                    app.mappingEngine.updateActiveMappings(list)
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupAccessibilityCheck() {
        try {
            if (!KeyMapperAccessibilityService.isRunning()) {
                binding.accessibilityCard.visibility = android.view.View.VISIBLE
                binding.btnOpenSettings.setOnClickListener {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            } else {
                binding.accessibilityCard.visibility = android.view.View.GONE
            }
            val serviceRunning = KeyMapperAccessibilityService.isRunning()
            app.mappingEngine.setEnabled(
                serviceRunning &&
                    app.bluetoothController.connectionState.value == ConnectionState.CONNECTED
            )
        } catch (e: Exception) {
            Log.w(TAG, "accessibility check failed", e)
        }
    }

    private fun setupDeviceTab() {
        deviceAdapter = DeviceAdapter(
            onConnect = { address ->
                lifecycleScope.launch {
                    try {
                        if (app.bluetoothController.connect(address)) {
                            Toast.makeText(
                                this@MainActivity,
                                "正在连接…",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "连接失败",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: SecurityException) {
                        Toast.makeText(
                            this@MainActivity,
                            "请先授予蓝牙权限",
                            Toast.LENGTH_SHORT
                        ).show()
                        requestPermissions()
                    } catch (e: Exception) {
                        Log.e(TAG, "connect failed", e)
                        Toast.makeText(
                            this@MainActivity,
                            "连接出错: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            onDisconnect = {
                try {
                    app.bluetoothController.disconnect()
                } catch (e: Exception) {
                    Log.e(TAG, "disconnect failed", e)
                }
            }
        )
        binding.devicesRecycler.layoutManager = LinearLayoutManager(this)
        binding.devicesRecycler.adapter = deviceAdapter

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                app.bluetoothController.connectionState.collect { state ->
                    withContext(Dispatchers.Main) {
                        deviceAdapter.updateConnectionState(state)
                        setupAccessibilityCheck()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "connectionState collect failed", e)
            }
        }
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                app.bluetoothController.connectedDevice.collect { device ->
                    withContext(Dispatchers.Main) {
                        deviceAdapter.updateConnectedDevice(device?.address)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "connectedDevice collect failed", e)
            }
        }

        binding.btnRefreshDevices.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val devices = app.bluetoothController.getPairedDevices()
                    deviceAdapter.submitList(devices)
                } catch (e: SecurityException) {
                    Toast.makeText(
                        this@MainActivity,
                        "请先授予蓝牙权限",
                        Toast.LENGTH_SHORT
                    ).show()
                    requestPermissions()
                } catch (e: Exception) {
                    Log.e(TAG, "refresh devices failed", e)
                    Toast.makeText(
                        this@MainActivity,
                        "获取设备失败: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupMappingTab() {
        mappingAdapter = MappingAdapter(
            onToggle = { config, enabled ->
                lifecycleScope.launch(Dispatchers.Default) {
                    try {
                        app.mappingRepository.update(config.copy(enabled = enabled))
                    } catch (e: Exception) {
                        Log.e(TAG, "toggle mapping failed", e)
                    }
                }
            },
            onDelete = { config ->
                lifecycleScope.launch(Dispatchers.Default) {
                    try {
                        app.mappingRepository.remove(config.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "delete mapping failed", e)
                    }
                }
            },
            onEdit = { config ->
                startActivity(Intent(this, MappingConfigActivity::class.java).apply {
                    putExtra(MappingConfigActivity.EXTRA_MAPPING_ID, config.id)
                })
            }
        )
        binding.mappingsRecycler.layoutManager = LinearLayoutManager(this)
        binding.mappingsRecycler.adapter = mappingAdapter

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                app.mappingRepository.mappings.collect { list ->
                    withContext(Dispatchers.Main) {
                        mappingAdapter.submitList(list)
                        binding.emptyMappings.visibility =
                            if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "mappings collect failed", e)
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

        val need = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (need.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, need.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
