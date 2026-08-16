package com.keymapper.app.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.keymapper.app.AppContainer
import com.keymapper.app.mapping.MappingRepository
import com.keymapper.app.model.MappingConfig
import com.keymapper.app.service.KeyMapperAccessibilityService
import com.keymapper.app.service.MappingForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var tvStatusBar: TextView
    private lateinit var appListContainer: LinearLayout
    private lateinit var tvEmptyHint: TextView
    private var app: AppContainer? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() { refreshStatusBar(); handler.postDelayed(this, 2000) }
    }

    private data class AppCard(
        val pkg: String,
        val label: String,
        val icon: android.graphics.drawable.Drawable?,
        var profiles: List<String>,
        var currentProfile: String,
        var mappings: List<MappingConfig>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildProgrammaticUI())

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val container = AppContainer.getOrCreate(this@MainActivity)
                app = container
                container.mappingRepository.migrateIfNeeded()
                requestPermissions()
                setupMappingsFlow(container)
                refreshAll()
            } catch (e: Throwable) {
                Log.e(TAG, "init failed", e)
                withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "初始化失败: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }
    }

    private fun setupMappingsFlow(container: AppContainer) {
        lifecycleScope.launch(Dispatchers.Default) {
            container.mappingRepository.mappings.collect {
                withContext(Dispatchers.Main) { rebuildAppList() }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshAll()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun refreshStatusBar() {
        val shizukuOk = com.keymapper.app.mapping.ShizukuShell.isPermissionGranted()
        val floatOn = com.keymapper.app.service.MappingForegroundService.isRunning()
        val currentPkg = KeyMapperAccessibilityService.currentPackageName
        val currentLabel = KeyMapperAccessibilityService.currentPackageLabel
        val pkgDisplay = if (currentLabel != null) "$currentLabel" else (currentPkg ?: "未知")
        runOnUiThread {
            tvStatusBar.text = when {
                !shizukuOk -> "⚠️ Shizuku 未激活 — 点『激活』按引导操作"
                !floatOn -> "⚠️ 悬浮未开 | 当前: $pkgDisplay"
                else -> "✅ 运行中 | $pkgDisplay"
            }
            tvStatusBar.setTextColor(
                if (shizukuOk && floatOn) Color.parseColor("#FF2E7D32")
                else Color.parseColor("#FFE65100")
            )
        }
    }

    private fun refreshAll() {
        lifecycleScope.launch(Dispatchers.Default) {
            rebuildAppList()
        }
    }

    private suspend fun collectAppCards(): List<AppCard> {
        val repo = app?.mappingRepository ?: return emptyList()
        val cards = mutableListOf<AppCard>()

        val currentPkg = KeyMapperAccessibilityService.currentPackageName

        val configured = runCatching { repo.listConfiguredApps() }.getOrDefault(emptyList())
        // 加上当前前台 APP（即使没配置过也要显示，让用户可以加）
        val allPkgs = (configured + listOfNotNull(currentPkg, MappingRepository.GLOBAL_PKG)).distinct()

        for (pkg in allPkgs) {
            val profiles = runCatching { repo.listProfilesFor(pkg) }.getOrDefault(listOf(MappingRepository.DEFAULT_PROFILE))
            val currentProf = runCatching { repo.currentProfileFor(pkg) }.getOrDefault(MappingRepository.DEFAULT_PROFILE)
            val mappings = runCatching { repo.getCurrentMappingsFor(pkg) }.getOrDefault(emptyList())
            val label = when {
                pkg == MappingRepository.GLOBAL_PKG -> "🌐 全局映射（所有APP生效）"
                pkg == currentPkg -> "📍 ${appNameFor(pkg)}"
                else -> appNameFor(pkg)
            }
            val icon = when {
                pkg == MappingRepository.GLOBAL_PKG -> resources.getDrawable(android.R.drawable.ic_menu_myplaces, null)
                else -> runCatching { packageManager.getApplicationIcon(pkg) }.getOrNull()
            }
            cards.add(AppCard(pkg, label, icon, profiles, currentProf, mappings))
        }

        // 排序：当前APP置顶，然后其他APP，全局放最后
        val sorted = cards.sortedWith(compareByDescending<AppCard> { it.pkg == currentPkg }.thenBy {
            if (it.pkg == MappingRepository.GLOBAL_PKG) 1 else 0
        })
        return sorted
    }

    private suspend fun rebuildAppList() {
        val cards = collectAppCards()
        withContext(Dispatchers.Main) {
            appListContainer.removeAllViews()
            cards.forEach { appListContainer.addView(makeAppCardView(it)) }
            tvEmptyHint.visibility = if (cards.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun makeAppCardView(card: AppCard): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            val borderColor = when {
                card.pkg == KeyMapperAccessibilityService.currentPackageName && card.mappings.any { it.enabled }
                    -> Color.parseColor("#FF4CAF50")
                card.mappings.any { it.enabled } -> Color.parseColor("#FF1976D2")
                else -> Color.parseColor("#FFE0E0E0")
            }
            val bgColor = when {
                card.pkg == KeyMapperAccessibilityService.currentPackageName -> Color.parseColor("#FFF1F8E9")
                else -> Color.WHITE
            }
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                setStroke(2, borderColor)
                setColor(bgColor)
            }
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                .apply { bottomMargin = dp(8) }

            // ---- 第一行：图标 + 名称 + 状态 ----
            val row1 = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val icon = ImageView(this@MainActivity).apply {
                setImageDrawable(card.icon)
                val size = dp(40)
                layoutParams = LinearLayout.LayoutParams(size, size)
            }
            row1.addView(icon)

            val nameCol = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    .apply { marginStart = dp(10) }
            }
            nameCol.addView(TextView(this@MainActivity).apply {
                text = card.label
                textSize = 15f
                setTextColor(Color.parseColor("#FF212121"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            val statusText = when {
                card.mappings.isEmpty() -> "尚未配置映射"
                card.mappings.any { it.enabled } && card.pkg == KeyMapperAccessibilityService.currentPackageName
                    -> "✅ 当前APP激活中 · ${card.mappings.size} 条"
                card.mappings.any { it.enabled } -> "✅ 已激活 · ${card.mappings.size} 条"
                else -> "⏸ 未激活 · ${card.mappings.size} 条"
            }
            nameCol.addView(TextView(this@MainActivity).apply {
                text = statusText
                textSize = 12f
                setTextColor(when {
                    card.mappings.any { it.enabled } -> Color.parseColor("#FF2E7D32")
                    else -> Color.parseColor("#FF9E9E9E")
                })
            })
            row1.addView(nameCol)
            addView(row1)

            // ---- 第二行：方案下拉 + 按钮 ----
            val row2 = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(8), 0, 0)
            }
            row2.addView(TextView(this@MainActivity).apply {
                text = "方案:"
                textSize = 12f
                setTextColor(Color.parseColor("#FF616161"))
            })
            val profileSpinner = Spinner(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    .apply { marginStart = dp(6) }
                adapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_item,
                    card.profiles
                ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                setSelection(card.profiles.indexOf(card.currentProfile).coerceAtLeast(0))
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selected = card.profiles[position]
                        if (selected != card.currentProfile) {
                            switchProfile(card.pkg, selected)
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
            row2.addView(profileSpinner)

            val btnNew = AppCompatButton(this@MainActivity).apply {
                text = "➕方案"
                textSize = 11f
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    .apply { marginStart = dp(4) }
                setOnClickListener { promptNewProfile(card.pkg) }
            }
            row2.addView(btnNew)

            val btnMap = AppCompatButton(this@MainActivity).apply {
                text = "➕映射"
                textSize = 11f
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    .apply { marginStart = dp(4) }
                setOnClickListener { openNewMappingFor(card.pkg) }
            }
            row2.addView(btnMap)

            addView(row2)

            // ---- 第三行：该方案下的映射列表（紧凑显示） ----
            if (card.mappings.isNotEmpty()) {
                val row3 = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, dp(6), 0, 0)
                }
                card.mappings.take(5).forEach { cfg ->
                    val mappingRow = LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(dp(4), dp(4), dp(4), dp(4))
                    }
                    mappingRow.addView(TextView(this@MainActivity).apply {
                        text = if (cfg.enabled) "✅" else "❌"
                        textSize = 12f
                        width = dp(24)
                    })
                    mappingRow.addView(TextView(this@MainActivity).apply {
                        text = "${cfg.button} → ${cfg.actionType.name}" + if (cfg.name.isNotBlank()) "（${cfg.name}）" else ""
                        textSize = 12f
                        setTextColor(Color.parseColor("#FF424242"))
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    })
                    val toggleBtn = AppCompatButton(this@MainActivity).apply {
                        text = if (cfg.enabled) "停" else "启"
                        textSize = 10f
                        setOnClickListener { toggleMapping(card.pkg, cfg) }
                    }
                    mappingRow.addView(toggleBtn)
                    val editBtn = AppCompatButton(this@MainActivity).apply {
                        text = "改"
                        textSize = 10f
                        setOnClickListener { editMapping(cfg) }
                    }
                    mappingRow.addView(editBtn)
                    val delBtn = AppCompatButton(this@MainActivity).apply {
                        text = "删"
                        textSize = 10f
                        setOnClickListener { deleteMapping(card.pkg, cfg) }
                    }
                    mappingRow.addView(delBtn)
                    row3.addView(mappingRow)
                }
                if (card.mappings.size > 5) {
                    row3.addView(TextView(this@MainActivity).apply {
                        text = "...还有 ${card.mappings.size - 5} 条"
                        textSize = 11f
                        setTextColor(Color.parseColor("#FF9E9E9E"))
                    })
                }
                addView(row3)
            } else {
                addView(TextView(this@MainActivity).apply {
                    text = "  （当前方案还没有映射，点『➕映射』添加）"
                    textSize = 11f
                    setTextColor(Color.parseColor("#FFBDBDBD"))
                    setPadding(0, dp(4), 0, 0)
                })
            }
        }
    }

    private fun switchProfile(pkg: String, profile: String) {
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                app?.mappingRepository?.switchProfileFor(pkg, profile)
                Log.i(TAG, "🔄 切换 profile: pkg=$pkg → $profile")
            } catch (e: Exception) {
                Log.e(TAG, "switchProfile 失败", e)
            }
        }
    }

    private fun promptNewProfile(pkg: String) {
        val input = EditText(this).apply {
            hint = "方案名称（例如：游戏模式 / 浏览模式）"
        }
        AlertDialog.Builder(this)
            .setTitle("为 ${appNameFor(pkg)} 创建新方案")
            .setView(input)
            .setPositiveButton("创建并切换") { _, _ ->
                val name = input.text?.toString()?.trim().orEmpty()
                if (name.isBlank()) {
                    Toast.makeText(this, "请输入方案名", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch(Dispatchers.Default) {
                    try {
                        app?.mappingRepository?.addProfileFor(pkg, name)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "✅ 已创建并切换到『$name』", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "addProfileFor 失败", e)
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun toggleMapping(pkg: String, cfg: MappingConfig) {
        lifecycleScope.launch(Dispatchers.Default) {
            val newCfg = cfg.copy(enabled = !cfg.enabled)
            val repoPkg = if (pkg == MappingRepository.GLOBAL_PKG) MappingRepository.GLOBAL_PKG else pkg
            app?.mappingRepository?.addMappingFor(repoPkg, newCfg)
        }
    }

    private fun editMapping(cfg: MappingConfig) {
        startActivity(Intent(this, MappingConfigActivity::class.java).apply {
            putExtra(MappingConfigActivity.EXTRA_MAPPING_ID, cfg.id)
        })
    }

    private fun deleteMapping(pkg: String, cfg: MappingConfig) {
        AlertDialog.Builder(this)
            .setTitle("删除这条映射？")
            .setMessage("『${cfg.button} → ${cfg.actionType.name}』\n（来自方案内的当前APP）")
            .setPositiveButton("删除") { _, _ ->
                lifecycleScope.launch(Dispatchers.Default) {
                    val repoPkg = if (pkg == MappingRepository.GLOBAL_PKG) MappingRepository.GLOBAL_PKG else pkg
                    app?.mappingRepository?.removeMappingFor(repoPkg, cfg.id)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun openNewMappingFor(pkg: String) {
        val intent = Intent(this, MappingConfigActivity::class.java)
        if (pkg != MappingRepository.GLOBAL_PKG) {
            intent.putExtra("target_package_preselect", pkg)
        }
        startActivity(intent)
    }

    private fun addNewAppAndCreate() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        val items = resolveInfoList.map { it.activityInfo.packageName to loadAppName(it.activityInfo.packageName) }
            .distinct().sortedBy { it.second }

        val options = items.map { "${it.second} (${it.first})" }.toTypedArray()
        val pkgs = items.map { it.first }
        AlertDialog.Builder(this)
            .setTitle("选择要添加的 APP")
            .setItems(options) { _, which ->
                openNewMappingFor(pkgs[which])
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun appNameFor(pkg: String): String = runCatching {
        val info = packageManager.getApplicationInfo(pkg, 0)
        packageManager.getApplicationLabel(info).toString()
    }.getOrDefault(pkg)

    private fun loadAppName(pkg: String): String = try {
        val info = packageManager.getApplicationInfo(pkg, 0)
        packageManager.getApplicationLabel(info).toString()
    } catch (_: Exception) { pkg }

    private fun requestPermissions() {
        val perms = mutableListOf<String>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            perms += android.Manifest.permission.BLUETOOTH_SCAN
            perms += android.Manifest.permission.BLUETOOTH_CONNECT
        }
        perms += android.Manifest.permission.ACCESS_FINE_LOCATION
        perms += android.Manifest.permission.ACCESS_COARSE_LOCATION
        val need = mutableListOf<String>()
        for (perm in perms) {
            val result = androidx.core.content.ContextCompat.checkSelfPermission(this@MainActivity, perm)
            if (result != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                need.add(perm)
            }
        }
        if (need.isNotEmpty()) runOnUiThread {
            androidx.core.app.ActivityCompat.requestPermissions(this, need.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun requestOverlayAndStart() {
        if (MappingForegroundService.isRunning()) {
            MappingForegroundService.stop(this)
            Toast.makeText(this, "⏹ 悬浮窗已停止", Toast.LENGTH_SHORT).show()
            refreshAll(); return
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
            && !android.provider.Settings.canDrawOverlays(this)
        ) {
            Toast.makeText(this, "请允许『显示在其他应用上层』", Toast.LENGTH_LONG).show()
            startActivity(
                Intent(
                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                )
            )
            return
        }
        MappingForegroundService.start(this)
        Toast.makeText(this, "🎮 悬浮窗已开启", Toast.LENGTH_LONG).show()
        refreshAll()
    }

    private fun stepWiseActivation() {
        val shizukuOk = com.keymapper.app.mapping.ShizukuShell.isPermissionGranted()
        val binderOk = com.keymapper.app.mapping.ShizukuShell.isBinderAvailable()

        if (!shizukuOk) {
            val msg = buildString {
                appendLine("【第 1 步：激活 Shizuku】")
                appendLine()
                appendLine("KeyMapper 基于 K2er 技术路线，使用 Shizuku 获得 shell 权限来模拟触摸。")
                appendLine()
                appendLine("✅ 无需无障碍服务")
                appendLine("✅ 无需 root")
                appendLine()
                appendLine("操作步骤：")
                appendLine("1️⃣ 下载安装 Shizuku Manager")
                appendLine("   https://shizuku.rikka.app/download/")
                appendLine("2️⃣ 打开 Shizuku Manager，按引导用 ADB 或 Wireless Debugging 启动服务")
                appendLine("3️⃣ 回到 KeyMapper，点击『授权』按钮授予权限")
            }
            AlertDialog.Builder(this)
                .setTitle("🔑 激活 KeyMapper")
                .setMessage(msg)
                .setPositiveButton("去下载 Shizuku") { _, _ ->
                    startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://shizuku.rikka.app/download/")))
                }
                .setNeutralButton("授权") { _, _ ->
                    com.keymapper.app.mapping.ShizukuShell.requestPermission(this) { granted, _ ->
                        if (granted) {
                            Toast.makeText(this, "✅ Shizuku 权限已授予！", Toast.LENGTH_LONG).show()
                            if (!android.provider.Settings.canDrawOverlays(this)) {
                                requestOverlayAndStart()
                            } else {
                                MappingForegroundService.start(this)
                                Toast.makeText(this, "🎉 激活完成！", Toast.LENGTH_LONG).show()
                            }
                            refreshAll()
                        } else {
                            Toast.makeText(this, "❌ 权限被拒绝，请先在 Shizuku Manager 中启动服务", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("关闭", null)
                .show()
            return
        }

        if (!android.provider.Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle("第 2 步：允许显示悬浮窗")
                .setMessage("KeyMapper 需要悬浮窗权限来在任何 APP 上方显示控制面板。")
                .setPositiveButton("去开启") { _, _ -> requestOverlayAndStart() }
                .setNegativeButton("取消", null)
                .show()
            return
        }
        MappingForegroundService.start(this)
        Toast.makeText(this, "✅ 已激活！悬浮窗已开启", Toast.LENGTH_LONG).show()
        refreshAll()
    }

    private fun buildProgrammaticUI(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FFF5F5F5"))
        }

        val toolbar = Toolbar(this).apply {
            setBackgroundColor(Color.parseColor("#FF3F51B5"))
            setTitleTextColor(Color.WHITE)
            title = "KeyMapper · 按键映射"
        }
        root.addView(toolbar, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(56)))
        setSupportActionBar(toolbar)

        val statusBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
            setBackgroundColor(Color.parseColor("#FFFDE7"))
        }
        tvStatusBar = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.parseColor("#FF6F00"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val btnA11y = AppCompatButton(this).apply {
            text = "激活"
            textSize = 11f
            setOnClickListener { stepWiseActivation() }
        }
        val btnFloat = AppCompatButton(this).apply {
            text = "悬浮"
            textSize = 11f
            setOnClickListener { requestOverlayAndStart() }
        }
        statusBar.addView(tvStatusBar)
        statusBar.addView(btnA11y)
        statusBar.addView(btnFloat)
        root.addView(statusBar)

        val scrollView = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        content.addView(TextView(this).apply {
            text = "📱 需要映射的 Apps（每个 APP 可有多个方案）"
            textSize = 15f
            setTextColor(Color.parseColor("#FF212121"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(4), 0, dp(8))
        })

        appListContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(appListContainer)

        tvEmptyHint = TextView(this).apply {
            text = "👇 还没有配置任何 APP\n点下方按钮选择一个 APP 并开始创建映射"
            textSize = 13f
            setTextColor(Color.parseColor("#FF9E9E9E"))
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(32), dp(8), dp(32))
            visibility = View.GONE
        }
        content.addView(tvEmptyHint)

        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, dp(8))
        }
        val addAppBtn = AppCompatButton(this).apply {
            text = "➕ 添加APP"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { addNewAppAndCreate() }
        }
        val floatBtn = AppCompatButton(this).apply {
            text = "🎮 悬浮开关"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                .apply { marginStart = dp(8) }
            setOnClickListener { requestOverlayAndStart() }
        }
        btnRow.addView(addAppBtn)
        btnRow.addView(floatBtn)
        content.addView(btnRow)

        scrollView.addView(content)
        root.addView(
            scrollView,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        )
        return root
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
