package com.keymapper.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.keymapper.app.AppContainer
import com.keymapper.app.R
import com.keymapper.app.mapping.ShizukuShell
import com.keymapper.app.model.ActionType
import com.keymapper.app.model.AppConfig
import com.keymapper.app.model.Mapping
import com.keymapper.app.service.InputMonitor
import com.keymapper.app.service.MappingForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var tvStatus: TextView
    private lateinit var container: LinearLayout
    private lateinit var sv: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF5F5F5.toInt())
        }

        tvStatus = TextView(this).apply {
            text = "⏳ 初始化中..."
            textSize = 12f
            setTextColor(0xFF616161.toInt())
            setPadding(24, 16, 24, 8)
        }
        root.addView(tvStatus)

        val btnBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 8, 16, 8)
        }
        val btnActivation = Button(this).apply {
            text = "🔑 激活"
            setOnClickListener { activate() }
        }
        val btnNew = Button(this).apply {
            text = "➕ 新建"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, MappingConfigActivity::class.java))
            }
        }
        btnBar.addView(btnActivation)
        btnBar.addView(btnNew)
        root.addView(btnBar)

        sv = ScrollView(this)
        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 8, 16, 8)
        }
        sv.addView(container)
        root.addView(sv)

        setContentView(root)
        initContainer()
    }

    override fun onResume() {
        super.onResume()
        refreshAll()
    }

    private fun initContainer() {
        AppContainer.getOrCreate(this)
        scope.launch(Dispatchers.Default) {
            AppContainer.getOrCreate(this@MainActivity).mappingRepository.apps.collectLatest {
                withContext(Dispatchers.Main) { refreshAll() }
            }
        }
        scope.launch {
            while (true) { refreshStatusBar(); delay(1000) }
        }
    }

    private fun refreshAll() {
        renderApps()
        refreshStatusBar()
    }

    @Suppress("SetTextI18n")
    private fun refreshStatusBar() {
        val shizukuOk = ShizukuShell.isPermissionGranted()
        val floatOn = MappingForegroundService.isRunning()
        val pkg = InputMonitor.currentPackageName
        val label = InputMonitor.currentPackageLabel ?: pkg?.substringAfterLast('.') ?: "?"
        val text = when {
            !shizukuOk -> "⚠️ Shizuku 未激活 — 点『激活』按引导"
            !floatOn -> "⚠️ 后台服务未启动 — 点『激活』"
            else -> "✅ K2ER 运行中 | $label"
        }
        tvStatus.text = text
        tvStatus.setTextColor(
            if (shizukuOk && floatOn) Color.parseColor("#FF2E7D32")
            else Color.parseColor("#FFE65100")
        )
    }

    @Suppress("SetTextI18n")
    private fun renderApps() {
        container.removeAllViews()
        val repo = AppContainer.getOrCreate(this).mappingRepository
        val apps = repo.apps.value

        container.addView(TextView(this).apply {
            text = "📋 已配置的 APP (${apps.size})"
            textSize = 14f; setTextColor(0xFF212121.toInt())
            setPadding(0, 8, 0, 8)
        })

        if (apps.isEmpty()) {
            container.addView(TextView(this).apply {
                text = "暂无配置。\n点『➕ 新建』选择一个 APP 开始配置映射。"
                textSize = 13f; setTextColor(0xFF9E9E9E.toInt())
                setPadding(0, 24, 0, 24)
            })
            return
        }

        apps.sortedBy { !it.enabled }.forEach { app ->
            container.addView(buildAppCard(app))
        }
    }

    @Suppress("SetTextI18n")
    private fun buildAppCard(app: AppConfig): LinearLayout {
        val repo = AppContainer.getOrCreate(this).mappingRepository
        val active = repo.getActiveMappingsForApp(app.packageName).size
        val totalMappings = app.scenes.sumOf { it.mappings.size }

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
            setBackgroundColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 12 }

            val header = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            }
            val ind = TextView(this@MainActivity).apply {
                text = when { !app.enabled -> "⚫"; active > 0 -> "🟢"; else -> "⚪" }
                textSize = 20f; width = 48
            }
            val title = TextView(this@MainActivity).apply {
                text = "${app.appName}\n${app.packageName}"
                textSize = 14f; setTextColor(0xFF212121.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val btnToggle = Button(this@MainActivity).apply {
                text = if (app.enabled) "停用" else "启用"; textSize = 11f
                setOnClickListener { repo.toggleAppEnabled(app.packageName) }
            }
            header.addView(ind); header.addView(title); header.addView(btnToggle)
            addView(header)

            val summary = TextView(this@MainActivity).apply {
                text = "🟢 $active 激活 / $totalMappings 总计  |  场景 ${app.scenes.size} 个"
                textSize = 12f; setTextColor(0xFF757575.toInt())
                setPadding(0, 4, 0, 4)
            }
            addView(summary)

            app.scenes.forEach { scene ->
                val sceneRow = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 4, 0, 4)
                }
                val sceneLabel = TextView(this@MainActivity).apply {
                    text = " 🎬 ${scene.name} (${scene.mappings.size})"
                    textSize = 12f
                    setTextColor(if (scene.enabled) 0xFF424242.toInt() else 0xFF9E9E9E.toInt())
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                val btnScene = TextView(this@MainActivity).apply {
                    text = if (scene.id == app.activeSceneId) "▶ 活动" else "切换"
                    textSize = 11f
                    setTextColor(if (scene.id == app.activeSceneId) 0xFF2E7D32.toInt() else 0xFF1976D2.toInt())
                    setPadding(16, 4, 16, 4)
                    setOnClickListener { repo.setActiveScene(app.packageName, scene.id) }
                }
                sceneRow.addView(sceneLabel); sceneRow.addView(btnScene)
                addView(sceneRow)

                scene.mappings.forEach { m ->
                    val mapRow = LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                        setPadding(8, 2, 8, 2)
                    }
                    val mi = TextView(this@MainActivity).apply {
                        text = if (m.enabled) "  • " else "    "
                        textSize = 12f; width = 24
                    }
                    val ml = TextView(this@MainActivity).apply {
                        text = "${m.button} → ${m.actionType.zh}@(${m.targetX.toInt()},${m.targetY.toInt()})${if (m.name.isNotBlank()) " [${m.name}]" else ""}"
                        textSize = 12f; setTextColor(if (m.enabled) 0xFF424242.toInt() else 0xFFBDBDBD.toInt())
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        setOnClickListener {
                            startActivity(Intent(this@MainActivity, MappingConfigActivity::class.java).apply {
                                putExtra("PKG", app.packageName)
                                putExtra("SCENE_ID", scene.id)
                                putExtra("MAPPING_ID", m.id)
                            })
                        }
                    }
                    val mbtn = TextView(this@MainActivity).apply {
                        text = if (m.enabled) "停" else "启"
                        textSize = 11f; setTextColor(0xFF1976D2.toInt())
                        setPadding(12, 2, 12, 2)
                        setOnClickListener {
                            repo.toggleMappingEnabled(app.packageName, scene.id, m.id)
                        }
                    }
                    mapRow.addView(mi); mapRow.addView(ml); mapRow.addView(mbtn)
                    addView(mapRow)
                }
            }

            val actionRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 0)
            }
            val btnNewMap = TextView(this@MainActivity).apply {
                text = "➕ 新增映射"; textSize = 12f; setTextColor(0xFF1976D2.toInt())
                setPadding(16, 6, 16, 6)
                setOnClickListener {
                    startActivity(Intent(this@MainActivity, MappingConfigActivity::class.java).apply {
                        putExtra("PKG", app.packageName)
                    })
                }
            }
            val btnDel = TextView(this@MainActivity).apply {
                text = "🗑 删除APP"
                textSize = 12f; setTextColor(0xFFE53935.toInt())
                setPadding(16, 6, 16, 6)
                setOnClickListener {
                    AlertDialog.Builder(this@MainActivity).apply {
                        setTitle("删除确认")
                        setMessage("确定删除 ${app.appName} 的所有映射配置？")
                        setPositiveButton("删除") { _, _ -> repo.deleteApp(app.packageName) }
                        setNegativeButton("取消", null)
                    }.show()
                }
            }
            actionRow.addView(btnNewMap); actionRow.addView(btnDel)
            addView(actionRow)
        }
    }


    private fun activate() {
        AlertDialog.Builder(this)
            .setTitle("🔑 激活 KeyMapper (K2ER)")
            .setMessage("K2ER 技术路线（3 个条件）：\n\n" +
                "① Shizuku server 启动\n" +
                "   (WiFi 配对 / ADB / Shizuku Manager)\n\n" +
                "② KeyMapper 获得 Shizuku shell 权限\n\n" +
                "③ 允许悬浮窗显示\n\n" +
                "✅ 无无障碍  ✅ 无输入法  ✅ 无 root\n\n" +
                "首次启动请准备 Shizuku Manager")
            .setPositiveButton("开始检查") { _, _ -> doActivate() }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun doActivate() {
        if (!ShizukuShell.isBinderAvailable()) {
            AlertDialog.Builder(this)
                .setTitle("⚠️ Shizuku server 未启动")
                .setMessage("Shizuku server 没跑起来。有 3 种启动方式：\n\n" +
                    "方式 A — WiFi 配对（推荐，和 K2ER 一样）：\n" +
                    "  1. 开发者选项 → 无线调试 → 打开\n" +
                    "  2. 点『使用配对码配对设备』记下 IP:端口 和 配对码\n" +
                    "  3. 电脑：adb pair <ip>:<port>\n" +
                    "  4. 电脑：adb connect <ip>:<port>\n" +
                    "  5. 打开 Shizuku Manager 点『启动』\n\n" +
                    "方式 B — USB ADB：\n" +
                    "  adb shell sh /data/user_de/0/moe.shizuku.privileged.api/start.sh\n\n" +
                    "方式 C — Shizuku Manager 直接启动：\n" +
                    "  某些机型 Shizuku Manager 可直接点『启动』\n\n" +
                    "启动 server 后回到本应用")
                .setPositiveButton("下载 Shizuku") { _, _ ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/download/")))
                }
                .setNeutralButton("我已启动") { _, _ -> requestShizukuPermission() }
                .setNegativeButton("取消", null)
                .show()
            return
        }
        requestShizukuPermission()
    }

    private fun requestShizukuPermission() {
        if (ShizukuShell.isPermissionGranted()) {
            AlertDialog.Builder(this)
                .setTitle("✅ Shizuku 已授权")
                .setMessage("KeyMapper 已经有 Shizuku shell 权限了。继续检查悬浮窗？")
                .setPositiveButton("继续") { _, _ -> checkOverlay() }
                .setNegativeButton("取消", null)
                .show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("📱 申请 Shizuku 权限")
            .setMessage("即将弹出 Shizuku 系统授权对话框。\n\n请在对话框里点『允许』或『Grant』。")
            .setPositiveButton("下一步") { _, _ ->
                ShizukuShell.requestPermission(this) { granted ->
                    runOnUiThread {
                        if (granted) {
                            Toast.makeText(this@MainActivity, "✅ Shizuku 权限已授予", Toast.LENGTH_LONG).show()
                            checkOverlay()
                        } else {
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle("❌ 授权被拒绝")
                                .setMessage("Shizuku 授权被拒绝了。\n\n请确认 Shizuku Manager 里 server 正在运行，然后重新点『激活』。")
                                .setPositiveButton("好的", null)
                                .show()
                        }
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun checkOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle("🖼️ 悬浮窗权限")
                .setMessage("KeyMapper 需要悬浮窗来显示状态球。\n\n即将跳转到系统设置页，请在列表里找到 KeyMapper 并打开开关。\n\n⚠️ 授权后点返回键回到本应用。")
                .setPositiveButton("去设置") { _, _ ->
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }
                .setNegativeButton("取消", null)
                .show()
            return
        }
        startK2er()
    }

    private fun startK2er() {
        if (!ShizukuShell.isPermissionGranted()) {
            Toast.makeText(this, "⚠️ Shizuku 权限丢了，请重新激活", Toast.LENGTH_LONG).show()
            return
        }
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "⚠️ 悬浮窗权限丢了，请重新激活", Toast.LENGTH_LONG).show()
            return
        }
        MappingForegroundService.start(this)
        Toast.makeText(this, "🎉 K2ER 已启动！退出到其他 APP 即可看到悬浮球", Toast.LENGTH_LONG).show()
    }

    private fun startService() {
        startK2er()
    }
}
