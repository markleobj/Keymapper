package com.keymapper.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keymapper.app.AppContainer
import com.keymapper.app.bluetooth.ConnectionState
import com.keymapper.app.mapping.MappingAdapter
import com.keymapper.app.service.KeyMapperAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private var app: AppContainer? = null

    private lateinit var toolbar: Toolbar
    private lateinit var accessibilityCard: LinearLayout
    private lateinit var devicesRecycler: RecyclerView
    private lateinit var mappingsRecycler: RecyclerView
    private lateinit var emptyMappingsText: TextView
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var mappingAdapter: MappingAdapter

    private var PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 用编程式布局 —— 完全绕过 XML Inflate
        val root = buildProgrammaticUI()
        setContentView(root)

        // 2. 后台初始化
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val container = AppContainer.getOrCreate(this@MainActivity)
                app = container

                requestPermissions()

                withContext(Dispatchers.Main) {
                    setupAccessibilityCheck()
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

    private fun buildProgrammaticUI(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FFF5F5F5"))
        }

        // Toolbar
        toolbar = Toolbar(this).apply {
            setBackgroundColor(Color.parseColor("#FF3F51B5"))
            setTitleTextColor(Color.WHITE)
            title = "KeyMapper"
        }
        root.addView(toolbar, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(56)
        ))
        setSupportActionBar(toolbar)

        // ScrollView 内容
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        // 无障碍服务提示卡片
        accessibilityCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        val accTitle = TextView(this).apply {
            text = "请先开启无障碍服务"
            textSize = 16f
            setTextColor(Color.parseColor("#FF212121"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        val accDesc = TextView(this).apply {
            text = "KeyMapper 需要无障碍服务来模拟点击"
            textSize = 14f
            setTextColor(Color.parseColor("#FF757575"))
            setPadding(0, dp(4), 0, dp(8))
        }
        val accBtn = Button(this).apply {
            text = "打开无障碍设置"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        accessibilityCard.addView(accTitle)
        accessibilityCard.addView(accDesc)
        accessibilityCard.addView(accBtn)
        content.addView(accessibilityCard, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, dp(12))
        })

        // 已配对手柄标题
        val devicesTitle = TextView(this).apply {
            text = "已配对的手柄"
            textSize = 16f
            setTextColor(Color.parseColor("#FF212121"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(8), 0, dp(4))
        }
        content.addView(devicesTitle)

        // 刷新按钮
        val refreshBtn = AppCompatButton(this).apply {
            text = "🔄 刷新列表"
            textSize = 14f
            setOnClickListener {
                lifecycleScope.launch(Dispatchers.Default) {
                    try {
                        val devices = app!!.bluetoothController.getPairedDevices()
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
                    }
                }
            }
        }
        content.addView(refreshBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, dp(8))
        })

        // 设备 RecyclerView
        devicesRecycler = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        content.addView(devicesRecycler, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(160)
        ).apply {
            setMargins(0, 0, 0, dp(8))
        })

        // 添加映射按钮
        val addMappingBtn = AppCompatButton(this).apply {
            text = "+ 添加新映射"
            textSize = 15f
            setOnClickListener {
                startActivity(Intent(this@MainActivity, MappingConfigActivity::class.java))
            }
        }
        content.addView(addMappingBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, dp(8), 0, dp(8))
        })

        // 映射标题
        val mappingsTitle = TextView(this).apply {
            text = "按键映射"
            textSize = 16f
            setTextColor(Color.parseColor("#FF212121"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(8), 0, dp(4))
        }
        content.addView(mappingsTitle)

        // 空状态
        emptyMappingsText = TextView(this).apply {
            text = "暂无映射配置"
            textSize = 14f
            setTextColor(Color.parseColor("#FF9E9E9E"))
            gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, dp(16))
            visibility = View.GONE
        }
        content.addView(emptyMappingsText)

        // 映射 RecyclerView
        mappingsRecycler = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        content.addView(mappingsRecycler, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(200)
        ))

        scrollView.addView(content)
        root.addView(scrollView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))

        return root
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onResume() {
        super.onResume()
        try {
            setupAccessibilityCheck()
        } catch (e: Exception) {
            Log.w(TAG, "accessibility check failed", e)
        }
    }

    private fun setupAccessibilityCheck() {
        try {
            if (KeyMapperAccessibilityService.isRunning()) {
                accessibilityCard.visibility = View.GONE
            } else {
                accessibilityCard.visibility = View.VISIBLE
            }
            val serviceRunning = KeyMapperAccessibilityService.isRunning()
            val connState = app?.bluetoothController?.connectionState?.value
            app?.mappingEngine?.setEnabled(serviceRunning && connState == ConnectionState.CONNECTED)
        } catch (e: Exception) {
            Log.w(TAG, "accessibility check failed", e)
        }
    }

    private fun setupDeviceTab() {
        val container = app ?: return

        deviceAdapter = DeviceAdapter(
            onConnect = { address ->
                lifecycleScope.launch(Dispatchers.Default) {
                    try {
                        if (container.bluetoothController.connect(address)) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "正在连接…",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "连接失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
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
                        Log.e(TAG, "connect failed", e)
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
        devicesRecycler.adapter = deviceAdapter

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
    }

    private fun setupMappingTab() {
        val container = app ?: return

        mappingAdapter = MappingAdapter(
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
                AlertDialog.Builder(this)
                    .setTitle("删除映射")
                    .setMessage("确定要删除这个映射吗？")
                    .setPositiveButton("删除") { _, _ ->
                        lifecycleScope.launch(Dispatchers.Default) {
                            try {
                                container.mappingRepository.remove(config.id)
                            } catch (e: Exception) {
                                Log.e(TAG, "delete mapping failed", e)
                            }
                        }
                    }
                    .setNegativeButton("取消", null)
                    .show()
            },
            onEdit = { config ->
                startActivity(Intent(this, MappingConfigActivity::class.java).apply {
                    putExtra(MappingConfigActivity.EXTRA_MAPPING_ID, config.id)
                })
            }
        )
        mappingsRecycler.adapter = mappingAdapter

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                container.mappingRepository.mappings.collect { list ->
                    withContext(Dispatchers.Main) {
                        mappingAdapter.submitList(list)
                        emptyMappingsText.visibility =
                            if (list.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "mappings collect failed", e)
            }
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
