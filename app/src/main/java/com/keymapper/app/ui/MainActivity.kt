package com.keymapper.app.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keymapper.app.AppContainer
import com.keymapper.app.R
import com.keymapper.app.mapping.ShizukuShell
import com.keymapper.app.model.AppConfig
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
    private lateinit var repo: com.keymapper.app.mapping.MappingRepository
    private lateinit var appsRecycler: RecyclerView
    private lateinit var emptyApps: TextView
    private lateinit var activIcon: ImageView
    private lateinit var activText: TextView
    private lateinit var deviceIcon: ImageView
    private lateinit var deviceText: TextView
    private lateinit var appAdapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repo = AppContainer.getOrCreate(this).mappingRepository

        activIcon = findViewById(R.id.activIcon)
        activText = findViewById(R.id.activText)
        deviceIcon = findViewById(R.id.deviceIcon)
        deviceText = findViewById(R.id.deviceText)
        emptyApps = findViewById(R.id.emptyApps)

        appsRecycler = findViewById(R.id.appsRecycler)
        appsRecycler.layoutManager = LinearLayoutManager(this)
        appAdapter = AppAdapter()
        appsRecycler.adapter = appAdapter

        findViewById<View>(R.id.btnAddApp).setOnClickListener { showAddAppDialog() }
        findViewById<View>(R.id.btnSettings).setOnClickListener { showSettings() }
        findViewById<View>(R.id.btnProfile).setOnClickListener { Toast.makeText(this, "个人中心（开发中）", Toast.LENGTH_SHORT).show() }
        findViewById<View>(R.id.rowActivation).setOnClickListener {
            if (MappingForegroundService.isRunning()) showServiceStatus() else activate()
        }
        findViewById<View>(R.id.rowDevice).setOnClickListener { showDeviceStatus() }
        findViewById<View>(R.id.bannerSection).setOnClickListener { showTutorial() }

        initObservers()
    }

    override fun onResume() {
        super.onResume()
        refreshAll()
    }

    private fun initObservers() {
        scope.launch {
            repo.apps.collectLatest { withContext(Dispatchers.Main) { refreshAll() } }
        }
        scope.launch {
            while (true) { refreshStatus(); delay(1500) }
        }
    }

    private fun refreshAll() {
        appAdapter.data = repo.apps.value.sortedByDescending { it.enabled }
        appAdapter.notifyDataSetChanged()
        appsRecycler.visibility = if (appAdapter.data.isEmpty()) View.GONE else View.VISIBLE
        emptyApps.visibility = if (appAdapter.data.isEmpty()) View.VISIBLE else View.GONE
        refreshStatus()
    }

    private fun refreshStatus() {
        val shizukuOk = ShizukuShell.isPermissionGranted()
        val serviceOn = MappingForegroundService.isRunning()

        if (shizukuOk && serviceOn) {
            activIcon.setImageResource(R.drawable.ic_dot_green)
            activText.text = "K2er 已激活"
        } else if (shizukuOk) {
            activIcon.setImageResource(R.drawable.ic_dot_red)
            activText.text = "K2er 已授权（点启动）"
        } else {
            activIcon.setImageResource(R.drawable.ic_dot_red)
            activText.text = "K2er 未激活"
        }

        val count = InputMonitor.deviceCount
        deviceText.text = "设备管理 - ${count}个连接"
        deviceIcon.setImageResource(
            if (count > 0) R.drawable.ic_dot_green else R.drawable.ic_dot_red
        )
    }

    private fun showAddAppDialog() {
        val apps = queryInstalledApps()
        if (apps.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("没找到 APP")
                .setMessage("可能缺少『允许所有 APP』权限。\n\n请先激活（点顶部 K2er 未激活），激活流程最后一步会引导你开启这个权限。\n\n或者你手机上安装的 APP 比较少。")
                .setPositiveButton("好的", null)
                .show()
            return
        }

        val labels = apps.map { "${it.second}\n${it.first}" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("选择目标 APP")
            .setItems(labels) { _, idx ->
                val (pkg, label) = apps[idx]
                repo.upsertApp(AppConfig(packageName = pkg, appName = label))
                Toast.makeText(this, "✅ 已添加 $label", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private val EXCLUDED_PREFIXES = listOf(
        "moe.shizuku.", "com.hihonor.", "com.huawei.", "com.miui.",
        "com.xiaomi.", "com.oppo.", "com.coloros.", "com.vivo.",
        "com.samsung.", "com.android.settings", "com.android.systemui",
        "com.android.shell", "com.android.inputmethod",
        "com.google.android.inputmethod", "com.baidu.input", "com.android.vending"
    )

    private fun queryInstalledApps(): List<Pair<String, String>> {
        val pm = packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA or PackageManager.GET_ACTIVITIES)
            .filter { ai ->
                val pkg = ai.packageName
                if (pkg == packageName) return@filter false
                if (pm.getLaunchIntentForPackage(pkg) == null) return@filter false
                if (EXCLUDED_PREFIXES.any { pkg.startsWith(it) }) return@filter false
                true
            }
            .map { it.packageName to pm.getApplicationLabel(it).toString() }
            .sortedBy { it.second }
    }

    private fun launchAppAndMinimize(pkg: String) {
        val intent = packageManager.getLaunchIntentForPackage(pkg)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "⚠️ 找不到启动入口", Toast.LENGTH_SHORT).show()
        }
    }

    // ========== K2ER 激活流程 ==========

    private fun activate() {
        AlertDialog.Builder(this)
            .setTitle("🔑 激活 KeyMapper")
            .setMessage("K2ER 技术路线：Shizuku + 悬浮窗（无 A11y / 无输入法 / 无 root）\n\n3 步条件：\n① Shizuku server 启动\n② KeyMapper 获得 shell 权限\n③ 允许悬浮窗显示")
            .setPositiveButton("开始激活") { _, _ -> doActivate() }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun doActivate() {
        if (!ShizukuShell.isBinderAvailable()) {
            AlertDialog.Builder(this)
                .setTitle("⚠️ Shizuku server 未启动")
                .setMessage("方式 A — WiFi 配对（推荐）：\n  开发者选项 → 无线调试 → 配对 → adb pair → adb connect → Shizuku Manager 点启动\n\n方式 B — USB ADB：\n  adb shell sh /data/user_de/0/moe.shizuku.privileged.api/start.sh\n\n方式 C — Shizuku Manager 直接启动")
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
            checkOverlay()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("📱 申请 Shizuku 权限")
            .setMessage("即将弹出 Shizuku 系统授权对话框，请点『允许』")
            .setPositiveButton("下一步") { _, _ ->
                ShizukuShell.requestPermission(this) { granted ->
                    runOnUiThread {
                        if (granted) checkOverlay()
                        else Toast.makeText(this, "❌ 授权被拒绝", Toast.LENGTH_SHORT).show()
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
                .setMessage("即将跳转到系统设置页，找到 KeyMapper 打开开关")
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
        if (!ShizukuShell.isPermissionGranted() || !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "⚠️ 权限丢了，请重新激活", Toast.LENGTH_SHORT).show()
            return
        }
        startK2erActually()
    }

    private fun startK2erActually() {
        MappingForegroundService.start(this)
        Toast.makeText(this, "🎉 K2ER 已启动！退出到其他 APP 可见悬浮球", Toast.LENGTH_LONG).show()
        refreshStatus()
    }

    // ========== 其它对话框 ==========

    private fun showServiceStatus() {
        val shizukuOk = ShizukuShell.isPermissionGranted()
        val floatOn = Settings.canDrawOverlays(this)
        val running = MappingForegroundService.isRunning()
        AlertDialog.Builder(this)
            .setTitle("激活状态")
            .setMessage("Shizuku server: ${if (ShizukuShell.isBinderAvailable()) "✅" else "❌"}\nShell 权限: ${if (shizukuOk) "✅" else "❌"}\n悬浮窗权限: ${if (floatOn) "✅" else "❌"}\n后台服务: ${if (running) "✅ 运行中" else "❌ 未启动"}")
            .setPositiveButton(if (running) "停止" else "启动") { _, _ ->
                if (running) {
                    MappingForegroundService.stop(this)
                    Toast.makeText(this, "已停止", Toast.LENGTH_SHORT).show()
                } else {
                    startK2er()
                }
                refreshStatus()
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    private fun showDeviceStatus() {
        val count = InputMonitor.deviceCount
        AlertDialog.Builder(this)
            .setTitle("设备管理")
            .setMessage("当前连接的手柄/设备数量：${count}\n\n（开发中：后续将显示具体设备列表）")
            .setPositiveButton("好的", null)
            .show()
    }

    private fun showTutorial() {
        AlertDialog.Builder(this)
            .setTitle("使用教程")
            .setMessage("1️⃣ 激活：点顶部『K2er 未激活』按向导走（Shizuku + 悬浮窗）\n\n2️⃣ 添加 APP：点右上 ➕ 选择你要映射的 APP\n\n3️⃣ 开始：APP 卡片上点『开始』→ 自动缩小到悬浮球\n\n4️⃣ 配置映射：点悬浮球 → ➕ 新建 → 选按键 → 选坐标 → 保存\n\n5️⃣ 回到游戏：按手柄按键 → 自动在对应坐标点击")
            .setPositiveButton("好的", null)
            .show()
    }

    private fun showSettings() {
        AlertDialog.Builder(this)
            .setTitle("设置")
            .setMessage("KeyMapper v2.1.3 (K2ER 架构)\n\n技术路线：Shizuku + getevent + input tap\n无无障碍 · 无输入法 · 无 root")
            .setPositiveButton("好的", null)
            .show()
    }

    // ========== APP 适配器 ==========

    inner class AppAdapter : RecyclerView.Adapter<AppAdapter.VH>() {
        var data: List<AppConfig> = emptyList()

        inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.appIcon)
            val name: TextView = itemView.findViewById(R.id.appName)
            val summary: TextView = itemView.findViewById(R.id.appSummary)
            val dot: ImageView = itemView.findViewById(R.id.appDot)
            val startBtn: TextView = itemView.findViewById(R.id.btnStart)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val app = data[position]
            val active = repo.getActiveMappingsForApp(app.packageName).size
            val total = app.scenes.sumOf { it.mappings.size }

            val icon: Drawable? = try {
                packageManager.getApplicationIcon(app.packageName)
            } catch (_: Exception) { null }
            if (icon != null) holder.icon.setImageDrawable(icon)
            else holder.icon.setImageResource(R.drawable.ic_person)

            holder.name.text = app.appName.ifBlank { app.packageName.substringAfterLast('.') }
            holder.name.alpha = if (app.enabled) 1f else 0.4f
            holder.summary.text = buildString {
                append(if (app.enabled) "🟢" else "⚪")
                append(" $active 激活 / $total 总计")
                app.activeSceneId?.let { sid ->
                    app.scenes.firstOrNull { it.id == sid }?.let { append(" · 场景: ${it.name}") }
                }
            }
            holder.dot.visibility = if (total == 0) View.VISIBLE else View.GONE
            holder.startBtn.text = if (MappingForegroundService.isRunning()) "开始" else "启动"

            holder.startBtn.setOnClickListener { launchAppAndMinimize(app.packageName) }
            holder.itemView.setOnClickListener {
                startActivity(Intent(this@MainActivity, MappingConfigActivity::class.java).apply {
                    putExtra("PKG", app.packageName)
                })
            }
            holder.itemView.setOnLongClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(app.appName)
                    .setMessage("启用: ${if (app.enabled) "是" else "否"}\n激活映射: $active\n总计映射: $total\n\n想做什么？")
                    .setPositiveButton("编辑映射") { _, _ ->
                        startActivity(Intent(this@MainActivity, MappingConfigActivity::class.java).apply {
                            putExtra("PKG", app.packageName)
                        })
                    }
                    .setNeutralButton(if (app.enabled) "停用" else "启用") { _, _ ->
                        repo.toggleAppEnabled(app.packageName)
                    }
                    .setNegativeButton("删除") { _, _ ->
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("确认删除")
                            .setMessage("删除 ${app.appName} 的所有映射配置？")
                            .setPositiveButton("删除") { _, _ -> repo.deleteApp(app.packageName) }
                            .setNegativeButton("取消", null)
                            .show()
                    }
                    .show()
                true
            }
        }

        override fun getItemCount() = data.size
    }
}
