package com.keymapper.app.floating

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.keymapper.app.AppContainer
import com.keymapper.app.R
import com.keymapper.app.mapping.MappingRepository
import com.keymapper.app.model.ActionType
import com.keymapper.app.model.MappingConfig
import com.keymapper.app.service.KeyMapperAccessibilityService
import com.keymapper.app.ui.MappingConfigActivity
import com.keymapper.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class FloatingWindowManager(private val context: Context) {

    companion object {
        private const val TAG = "FloatMgr"

        @Volatile
        private var instance: FloatingWindowManager? = null

        fun getInstance(context: Context): FloatingWindowManager {
            return instance ?: synchronized(this) {
                instance ?: FloatingWindowManager(context.applicationContext).also { instance = it }
            }
        }

        fun canDrawOverlay(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        }
    }

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observeJob: Job? = null
    private var debugJob: Job? = null

    private var ballView: View? = null
    private var panelView: View? = null
    private var ballParams: WindowManager.LayoutParams? = null
    private var panelParams: WindowManager.LayoutParams? = null
    private var isRunning = false

    @SuppressLint("ClickableViewAccessibility")
    fun show() {
        if (isRunning) {
            Log.w(TAG, "already showing")
            return
        }
        if (!canDrawOverlay(context)) {
            Log.e(TAG, "no overlay permission")
            return
        }

        val ball = createBallView()
        val params = buildLayoutParams(
            w = dp(48), h = dp(48),
            gravity = Gravity.TOP or Gravity.START,
            x = dp(8), y = dp(200)
        )
        try {
            windowManager.addView(ball, params)
            ballView = ball
            ballParams = params
            isRunning = true

            ball.setOnTouchListener(createDragTouchListener(isBall = true))
            ball.setOnClickListener { togglePanel() }

            observeMappings()
            Log.i(TAG, "ball shown")
        } catch (e: Throwable) {
            Log.e(TAG, "show ball failed", e)
        }
    }

    fun hide() {
        try {
            panelView?.let { windowManager.removeViewImmediate(it) }
            ballView?.let { windowManager.removeViewImmediate(it) }
        } catch (_: Throwable) {}
        panelView = null
        ballView = null
        panelParams = null
        ballParams = null
        isRunning = false
        observeJob?.cancel()
        observeJob = null
        Log.i(TAG, "hidden")
    }

    private fun togglePanel() {
        if (panelView != null) {
            hidePanel()
        } else {
            showPanel()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showPanel() {
        val ball = ballView ?: return
        val panel = createPanelView()
        val ballLoc = IntArray(2).also { ball.getLocationOnScreen(it) }

        val panelW = dp(260)
        val panelH = WindowManager.LayoutParams.WRAP_CONTENT

        val dm = context.resources.displayMetrics
        val screenW = dm.widthPixels
        val ballSize = dp(48)
        val gap = dp(8)

        val ballCenterX = ballLoc[0] + ballSize / 2
        val x = if (ballCenterX > screenW / 2) {
            (ballLoc[0] - panelW - gap).coerceAtLeast(0)
        } else {
            (ballLoc[0] + ballSize + gap).coerceAtMost((screenW - panelW).coerceAtLeast(0))
        }
        val y = (ballLoc[1] + ballSize + dp(4)).coerceAtMost(dm.heightPixels - dp(200))

        Log.i(TAG, "showPanel ball=(${ballLoc[0]},${ballLoc[1]}) screen=${screenW}x${dm.heightPixels} → panel=($x,$y) size=${panelW}")

        val params = buildLayoutParams(
            w = panelW, h = panelH,
            gravity = Gravity.TOP or Gravity.START,
            x = x, y = y
        )

        try {
            windowManager.addView(panel, params)
            panelView = panel
            panelParams = params
            panel.setOnTouchListener(createDragTouchListener(isBall = false))

            panel.findViewById<View>(R.id.btn_open_main).setOnClickListener {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                hidePanel()
            }
            panel.findViewById<View>(R.id.btn_new_mapping).setOnClickListener {
                val intent = Intent(context, MappingConfigActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                hidePanel()
            }
            panel.findViewById<View>(R.id.btn_close).setOnClickListener {
                hidePanel()
            }

            refreshPanel()
            startDebugLoop()
        } catch (e: Throwable) {
            Log.e(TAG, "show panel failed", e)
        }
    }

    private fun startDebugLoop() {
        debugJob?.cancel()
        debugJob = scope.launch {
            while (true) {
                val tv = panelView?.findViewById<TextView>(R.id.tv_debug) ?: break
                KeyMapperAccessibilityService.refreshForegroundPackage()
                val a11yCount = KeyMapperAccessibilityService.getA11yKeyCount()
                val touchCount = KeyMapperAccessibilityService.getTouchKeyCount()
                val engineSummary = com.keymapper.app.mapping.MappingEngine.getDebugSummary()
                val currentPkg = KeyMapperAccessibilityService.currentPackageName ?: "?"
                val currentLabel = KeyMapperAccessibilityService.currentPackageLabel
                val pkgDisplay = if (currentLabel != null) "$currentLabel($currentPkg)" else currentPkg
                val combined = buildString {
                    appendLine("📱 当前APP: $pkgDisplay")
                    appendLine("♿ A11y按键: $a11yCount  🎮触摸→按键: $touchCount")
                    append(engineSummary)
                }
                tv.text = combined
                delay(300)
            }
        }
    }

    private fun hidePanel() {
        debugJob?.cancel()
        debugJob = null
        try {
            panelView?.let { windowManager.removeViewImmediate(it) }
        } catch (_: Throwable) {}
        panelView = null
        panelParams = null
    }

    @SuppressLint("InflateParams")
    private fun createBallView(): View {
        val inflater = LayoutInflater.from(context)
        return inflater.inflate(R.layout.view_float_ball, null, false)
    }

    @SuppressLint("InflateParams")
    private fun createPanelView(): View {
        val inflater = LayoutInflater.from(context)
        return inflater.inflate(R.layout.view_float_panel, null, false)
    }

    private fun refreshPanel() {
        val panel = panelView ?: return
        val tvProfile = panel.findViewById<TextView>(R.id.tv_profile)
        val tvStatus = panel.findViewById<TextView>(R.id.tv_status)
        val listContainer = panel.findViewById<LinearLayout>(R.id.list_container)

        scope.launch(Dispatchers.Default) {
            val repo = runCatching { AppContainer.getOrCreate(context).mappingRepository }.getOrNull() ?: return@launch
            val currentPkg = KeyMapperAccessibilityService.currentPackageName
            val profile = if (currentPkg != null) repo.currentProfileFor(currentPkg) else MappingRepository.DEFAULT_PROFILE
            val mappings = repo.getActiveMappingsForApp(currentPkg)
            val enabled = mappings.count { it.enabled }
            val pkgLabel = currentPkg?.let { runCatching {
                context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(it, 0)).toString()
            }.getOrNull() } ?: "未知"
            withContext(Dispatchers.Main) {
                tvProfile?.text = "📋 $pkgLabel / $profile"
                val a11yOn = KeyMapperAccessibilityService.isRunning()
                tvStatus?.text = buildString {
                    append(if (a11yOn) "✅ 无障碍运行中" else "❌ 无障碍未开")
                    append("  |  ")
                    append(if (enabled > 0) "🟢 $enabled/${mappings.size} 启用" else "⚪ 全部停用")
                }
                renderMappingList(listContainer, mappings)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderMappingList(container: LinearLayout?, mappings: List<MappingConfig>) {
        container ?: return
        container.removeAllViews()

        if (mappings.isEmpty()) {
            container.addView(TextView(context).apply {
                text = "还没有映射，点右下角➕新建"
                textSize = 12f
                setTextColor(0xFF9E9E9E.toInt())
                setPadding(dp(8), dp(12), dp(8), dp(12))
            })
            return
        }

        val currentPkg = KeyMapperAccessibilityService.currentPackageName
        val sorted = mappings.sortedByDescending { it.enabled }
        sorted.forEach { cfg ->
            val pkg = cfg.targetPackage
            val pkgMatch = pkg.isNullOrBlank() || pkg == currentPkg
            val isActive = cfg.enabled && pkgMatch
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8), dp(10), dp(8), dp(10))
                val bgRes = when {
                    isActive -> 0xFFE8F5E9.toInt()
                    cfg.enabled -> 0xFFF1F8E9.toInt()
                    else -> 0xFFFAFAFA.toInt()
                }
                setBackgroundColor(bgRes)
            }
            val indicator = TextView(context).apply {
                text = when {
                    isActive -> "🟢"
                    cfg.enabled -> "🔵"
                    else -> "⚪"
                }
                textSize = 14f
                width = dp(28)
            }
            val pkgTag = if (pkg.isNullOrBlank()) "" else pkg.substringAfterLast('.')
            val label = TextView(context).apply {
                text = buildString {
                    append(cfg.name.ifBlank { cfg.button })
                    append("\n")
                    append(cfg.button).append(" · ").append(actionTypeCn(cfg.actionType))
                    if (pkg.isNullOrBlank()) {
                        append(" · 全局")
                    } else {
                        append(" · ").append(pkgTag)
                        if (isActive) append(" ✅") else append(" ❌")
                    }
                }
                textSize = 11f
                setTextColor(if (isActive) 0xFF1B5E20.toInt() else if (cfg.enabled) 0xFF616161.toInt() else 0xFF9E9E9E.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val btnToggle = AppCompatButton(context).apply {
                text = if (cfg.enabled) "停用" else "启用"
                textSize = 11f
                setOnClickListener {
                    scope.launch(Dispatchers.Default) {
                        runCatching {
                            val pkg = if (cfg.targetPackage.isNullOrBlank()) MappingRepository.GLOBAL_PKG else cfg.targetPackage
                            AppContainer.getOrCreate(context).mappingRepository.addMappingFor(
                                pkg!!, cfg.copy(enabled = !cfg.enabled)
                            )
                        }
                    }
                }
            }
            row.addView(indicator)
            row.addView(label)
            row.addView(btnToggle)
            container.addView(row)
        }
    }

    private fun actionTypeCn(type: ActionType): String = when (type) {
        ActionType.TAP -> "点击"
        ActionType.LONG_PRESS -> "长按"
        ActionType.SWIPE -> "滑动"
        ActionType.MOUSE_MOVE -> "鼠标模拟"
        ActionType.COMBO -> "组合"
        ActionType.DO_NOTHING -> "只屏蔽"
    }

    private fun observeMappings() {
        observeJob?.cancel()
        observeJob = scope.launch(Dispatchers.Default) {
            val repo = runCatching { AppContainer.getOrCreate(context).mappingRepository }.getOrNull() ?: return@launch
            repo.mappings.collectLatest { refreshPanel() }
        }
    }

    private fun buildLayoutParams(
        w: Int, h: Int,
        gravity: Int, x: Int, y: Int
    ): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        return WindowManager.LayoutParams(
            w, h, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = gravity
            this.x = x
            this.y = y
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createDragTouchListener(isBall: Boolean): View.OnTouchListener {
        val state = floatArrayOf(0f, 0f, 0f, 0f)
        return View.OnTouchListener { view, event ->
            val paramsRef = if (isBall) ballParams else panelParams
            val params = paramsRef ?: return@OnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    state[0] = params.x.toFloat()
                    state[1] = params.y.toFloat()
                    state[2] = event.rawX
                    state[3] = event.rawY
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - state[2]
                    val dy = event.rawY - state[3]
                    if (abs(dx) > 8f || abs(dy) > 8f) {
                        params.x = (state[0] + dx).toInt()
                        params.y = (state[1] + dy).toInt()
                        try {
                            windowManager.updateViewLayout(view, params)
                            if (isBall) ballParams = params else panelParams = params
                        } catch (_: Throwable) {}
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    state[0] = 0f; state[1] = 0f; state[2] = 0f; state[3] = 0f
                    false
                }
                else -> false
            }
        }
    }

    private fun dp(v: Int): Int = (v * context.resources.displayMetrics.density).toInt()
}
