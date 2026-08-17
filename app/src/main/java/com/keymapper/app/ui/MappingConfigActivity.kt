package com.keymapper.app.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.keymapper.app.AppContainer
import com.keymapper.app.R
import com.keymapper.app.model.ActionType
import com.keymapper.app.model.Mapping
import com.keymapper.app.floating.FloatingCoordinatePicker
import com.keymapper.app.service.InputMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MappingConfigActivity : AppCompatActivity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var targetButton: String = ""
    private var targetX: Float = 0f
    private var targetY: Float = 0f
    private var pkgName: String = ""
    private var sceneId: String? = null
    private var editMappingId: String? = null
    private var pendingX: Float = 0f
    private var pendingY: Float = 0f

    private lateinit var etName: EditText
    private lateinit var tvButton: TextView
    private lateinit var tvCoord: TextView
    private lateinit var spinnerType: Spinner
    private lateinit var etDuration: EditText
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.getOrCreate(this)

        pkgName = intent.getStringExtra("PKG") ?: ""
        sceneId = intent.getStringExtra("SCENE_ID")
        editMappingId = intent.getStringExtra("MAPPING_ID")

        if (pkgName.isBlank()) {
            pkgName = InputMonitor.currentPackageName ?: ""
        }
        if (pkgName.isBlank()) {
            promptSelectApp()
            return
        }
        if (sceneId == null) {
            val app = AppContainer.getOrCreate(this).mappingRepository.getOrCreateApp(pkgName)
            sceneId = app.activeSceneId ?: app.scenes.firstOrNull()?.id
        }

        buildUI()
        if (editMappingId != null) loadExisting()
        intent.getStringExtra("BTN")?.let {
            targetButton = it
            tvButton?.text = it
        }
        if (intent.hasExtra("X")) {
            val x = intent.getFloatExtra("X", 0f)
            val y = intent.getFloatExtra("Y", 0f)
            targetX = x; targetY = y
            tvCoord?.text = "(${x.toInt()}, ${y.toInt()})"
        }
    }

    private fun promptSelectApp() {
        val apps = queryInstalledApps()
        val names = apps.map { "${it.second}\n${it.first}" }
        AlertDialog.Builder(this)
            .setTitle("选择目标 APP")
            .setItems(names.toTypedArray()) { _, i ->
                pkgName = apps[i].first
                AppContainer.getOrCreate(this).mappingRepository.getOrCreateApp(pkgName, apps[i].second)
                val app = AppContainer.getOrCreate(this).mappingRepository.getOrCreateApp(pkgName)
                sceneId = app.scenes.firstOrNull()?.id
                buildUI()
            }
            .setNegativeButton("取消") { _, _ -> finish() }
            .show()
    }

    private fun queryInstalledApps(): List<Pair<String, String>> {
        val pm = packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .mapNotNull { ai ->
                val pkg = ai.packageName
                if (pkg == packageName) return@mapNotNull null
                if (pm.getLaunchIntentForPackage(pkg) == null) return@mapNotNull null
                pkg to pm.getApplicationLabel(ai).toString()
            }
            .sortedBy { it.second }
    }

    @Suppress("SetTextI18n")
    private fun buildUI() {
        val pm = packageManager
        val appName = runCatching { pm.getApplicationLabel(pm.getApplicationInfo(pkgName, 0)).toString() }
            .getOrDefault(pkgName.substringAfterLast('.'))

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF5F5F5.toInt())
        }

        tvTitle = TextView(this).apply {
            text = if (editMappingId != null) "编辑映射" else "新建映射"
            textSize = 18f; setTextColor(0xFF212121.toInt())
            setPadding(24, 24, 24, 8)
        }
        root.addView(tvTitle)

        val tvSub = TextView(this).apply {
            text = "📱 $appName ($pkgName)"
            textSize = 13f; setTextColor(0xFF616161.toInt())
            setPadding(24, 0, 24, 16)
        }
        root.addView(tvSub)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(16, 16, 16, 16)
        }

        // 按键
        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }
        row1.addView(TextView(this).apply {
            text = "🎮 手柄按键"; textSize = 14f; width = 120; setTextColor(0xFF424242.toInt())
        })
        tvButton = TextView(this).apply {
            text = "未选择"; textSize = 14f; setTextColor(0xFF1976D2.toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { pickButton() }
        }
        row1.addView(tvButton)
        card.addView(row1)

        // 坐标
        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }
        row2.addView(TextView(this).apply {
            text = "📍 坐标"; textSize = 14f; width = 120; setTextColor(0xFF424242.toInt())
        })
        tvCoord = TextView(this).apply {
            text = "未选择"; textSize = 14f; setTextColor(0xFF1976D2.toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { pickCoordinate() }
        }
        row2.addView(tvCoord)
        card.addView(row2)

        // 动作类型
        val row3 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }
        row3.addView(TextView(this).apply {
            text = "⚡ 动作类型"; textSize = 14f; width = 120; setTextColor(0xFF424242.toInt())
        })
        spinnerType = Spinner(this).apply {
            adapter = ArrayAdapter(this@MappingConfigActivity, android.R.layout.simple_spinner_dropdown_item,
                ActionType.values().map { it.zh })
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        row3.addView(spinnerType)
        card.addView(row3)

        // 长按/滑动时长
        val row4 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }
        row4.addView(TextView(this).apply {
            text = "⏱ 时长(ms)"; textSize = 14f; width = 120; setTextColor(0xFF424242.toInt())
        })
        etDuration = EditText(this).apply {
            hint = "300"; inputType = android.text.InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        row4.addView(etDuration)
        card.addView(row4)

        // 名称
        val row0 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }
        row0.addView(TextView(this).apply {
            text = "🏷 名称"; textSize = 14f; width = 120; setTextColor(0xFF424242.toInt())
        })
        etName = EditText(this).apply {
            hint = "可选，方便识别"; inputType = android.text.InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        row0.addView(etName)
        card.addView(row0)

        root.addView(card)

        // 操作按钮
        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 24, 16, 16)
        }
        val btnSave = Button(this).apply {
            text = "💾 保存"; textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 8 }
            setOnClickListener { save() }
        }
        val btnCancel = Button(this).apply {
            text = "取消"; textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { finish() }
        }
        actionRow.addView(btnSave); actionRow.addView(btnCancel)
        root.addView(actionRow)

        setContentView(root)
    }

    private fun pickButton() {
        startActivityForResult(Intent(this, ButtonPickerActivity::class.java), REQ_BUTTON)
    }

    private fun pickCoordinate() {
        FloatingCoordinatePicker.showAndLaunch(this, pkgName) { x, y ->
            pendingX = x; pendingY = y
            targetX = x; targetY = y
            tvCoord.text = "(${x.toInt()}, ${y.toInt()})"
            android.widget.Toast.makeText(this@MappingConfigActivity,
                "✅ 已拾取 (${x.toInt()}, ${y.toInt()})", android.widget.Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@MappingConfigActivity, MappingConfigActivity::class.java).apply {
                putExtra("PKG", pkgName)
                sceneId?.let { putExtra("SCENE_ID", it) }
                editMappingId?.let { putExtra("MAPPING_ID", it) }
                putExtra("BTN", targetButton)
                putExtra("X", x); putExtra("Y", y)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
            finish()
        }
    }

    private fun loadExisting() {
        val app = AppContainer.getOrCreate(this).mappingRepository.getApp(pkgName) ?: return
        val scene = app.scenes.firstOrNull { it.id == sceneId } ?: return
        val m = scene.mappings.firstOrNull { it.id == editMappingId } ?: return
        targetButton = m.button
        targetX = m.targetX; targetY = m.targetY
        tvButton.text = m.button
        tvCoord.text = "(${m.targetX.toInt()}, ${m.targetY.toInt()})"
        spinnerType.setSelection(ActionType.values().indexOf(m.actionType))
        etDuration.setText(m.durationMs.toString())
        etName.setText(m.name)
    }

    private fun save() {
        if (targetButton.isBlank()) { Toast.makeText(this, "请选择按键", Toast.LENGTH_SHORT).show(); return }
        if (targetX == 0f && targetY == 0f) { Toast.makeText(this, "请选择坐标", Toast.LENGTH_SHORT).show(); return }

        val type = ActionType.values()[spinnerType.selectedItemPosition]
        val duration = etDuration.text.toString().toLongOrNull() ?: 300L

        val mapping = Mapping(
            id = editMappingId ?: UUID.randomUUID().toString(),
            name = etName.text.toString(),
            button = targetButton,
            actionType = type,
            targetX = targetX,
            targetY = targetY,
            durationMs = duration,
            enabled = true
        )

        scope.launch {
            val repo = AppContainer.getOrCreate(this@MappingConfigActivity).mappingRepository
            val sid = sceneId ?: repo.getOrCreateApp(pkgName).scenes.firstOrNull()?.id ?: return@launch
            if (editMappingId != null) {
                repo.updateMapping(pkgName, sid, mapping)
            } else {
                repo.addMapping(pkgName, sid, mapping)
            }
            Toast.makeText(this@MappingConfigActivity, "✅ 已保存", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) return
        when (requestCode) {
            REQ_BUTTON -> {
                targetButton = data.getStringExtra("BUTTON") ?: return
                tvButton.text = targetButton
            }
            REQ_COORD -> {
                targetX = data.getFloatExtra("X", 0f)
                targetY = data.getFloatExtra("Y", 0f)
                tvCoord.text = "(${targetX.toInt()}, ${targetY.toInt()})"
            }
        }
    }

    companion object {
        private const val REQ_BUTTON = 1
        private const val REQ_COORD = 2
    }
}
