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
import com.keymapper.app.AppContainer
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
    private var app: AppContainer? = null

    private var PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 先把 UI 画出来 —— 必须在 100ms 内返回
        setupToolbar()
        setupAccessibilityCheck()

        // 2. 后台线程里初始化所有业务对象 + 请求权限
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val container = AppContainer.getOrCreate(this@MainActivity)
                app = container

                requestPermissions()

                withContext(Dispatchers.Main) {
                    setupDeviceTab()
                    setupMappingTab()
                }

                startEventCollectors()
            } catch (e: Throwable) {
                Log.e(TAG, "init failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "初始化失败: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            setupAccessibilityCheck()
        } catch (e: Exception) {
            Log.w(TAG, "accessibility check failed", e)
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
        } catch (e: Exception) {
            Log.w(TAG, "accessibility check failed", e)
        }
    }

    private fun startEventCollectors() {
        val container = app ?: return

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                container.bluetoothController.buttonEvents.collect { event ->
                    try {
                        container.mappingEngine.onButtonEvent(event)
                    } catch (e: Exception) {
                        Log.e(TAG, "button event dispatch failed", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "buttonEvents collect failed", e)
            }
        }

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                container.mappingRepository.mappings.collect { list ->
                    container.mappingEngine.updateActiveMappings(list)
                }
            } catch (e: Exception) {
                Log.e(TAG, "mappings collect failed", e)
            }
        }
    }

    private fun setupDeviceTab() {
        val container = app ?: return

        val deviceAdapter = DeviceAdapter(
            onConnect = { address ->
                lifecycleScope.launch(Dispatchers.Default) {
                    try {
                        if (container.bluetoothController.connect(address)) {
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
                    container.bluetoothController.disconnect()
                } catch (e: Exception) {
                    Log.e(TAG, "disconnect failed", e)
                }
            }
        )
        binding.devicesRecycler.layoutManager = LinearLayoutManager(this)
        binding.devicesRecycler.adapter = deviceAdapter

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                container.bluetoothController.connectionState.collect { state ->
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
                container.bluetoothController.connectedDevice.collect { device ->
                    withContext(Dispatchers.Main) {
                        deviceAdapter.updateConnectedDevice(device?.address)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "connectedDevice collect failed", e)
            }
        }

        binding.btnRefreshDevices.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Default) {
                try {
                    val devices = container.bluetoothController.getPairedDevices()
                    withContext(Dispatchers.Main) {
                        deviceAdapter.submitList(devices)
                    }
                } catch (e: SecurityException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "请先授予蓝牙权限",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    requestPermissions()
                } catch (e: Exception) {
                    Log.e(TAG, "refresh devices failed", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "获取设备失败: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun setupMappingTab() {
        val container = app ?: return

        val mappingAdapter = MappingAdapter(
            onToggle = { config, enabled ->
                lifecycleScope.launch(Dispatchers.Default) {
                    try {
                        container.mappingRepository.update(config.copy(enabled = enabled))
                    } catch (e: Exception) {
                        Log.e(TAG, "toggle mapping failed", e)
                    }
                }
            },
            onDelete = { config ->
                lifecycleScope.launch(Dispatchers.Default) {
                    try {
                        container.mappingRepository.remove(config.id)
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
                container.mappingRepository.mappings.collect { list ->
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
            runOnUiThread {
                ActivityCompat.requestPermissions(
                    this,
                    need.toTypedArray(),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
