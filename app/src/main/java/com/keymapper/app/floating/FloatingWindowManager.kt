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
import com.keymapper.app.mapping.ShizukuShell
import com.keymapper.app.model.AppConfig
import com.keymapper.app.service.InputMonitor
import com.keymapper.app.ui.MainActivity
import com.keymapper.app.ui.MappingConfigActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class FloatingWindowManager(private val context: Context) {

    companion object {
        private const val TAG = "FloatMgr-K2ER"

        @Volatile private var instance: FloatingWindowManager? = null
        fun getInstance(c: Context): FloatingWindowManager {
            return instance ?: synchronized(this) {
                instance ?: FloatingWindowManager(c.applicationContext).also { instance = it }
            }
        }
        fun canDrawOverlay(c: Context): Boolean =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(c) else true
    }

    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val selfPkg = context.packageName

    private var ballView: View? = null
    private var panelView: View? = null
    private var ballParams: WindowManager.LayoutParams? = null
    private var panelParams: WindowManager.LayoutParams? = null
    private var running = false
    private var ballShown = false
    private var observeJob: Job? = null
    private var refreshJob: Job? = null

    private fun lp(w: Int, h: Int, x: Int, y: Int): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        return WindowManager.LayoutParams(
            w, h, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; this.x = x; this.y = y }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun show() {
        if (running) return
        if (!canDrawOverlay(context)) { Log.e(TAG, "no overlay permission"); return }
        running = true
        startObserve()
        startRefresh()
        Log.i(TAG, "✅ FloatingWindowManager started (auto-hide self)")
    }

    fun hide() {
        refreshJob?.cancel(); observeJob?.cancel()
        hidePanel(); hideBallInternal()
        ballView = null; panelView = null; ballParams = null; panelParams = null
        running = false
        Log.i(TAG, "FloatingWindowManager stopped")
    }

    private fun showBallInternal() {
        if (ballShown || ballView == null) return
        val ball = ballView ?: return
        val params = ballParams ?: return
        try {
            wm.addView(ball, params); ballShown = true; updateBall()
            Log.d(TAG, "🎾 ball shown")
        } catch (e: Throwable) { Log.e(TAG, "show ball failed", e) }
    }

    private fun hideBallInternal() {
        if (!ballShown) return
        val ball = ballView ?: return
        try { wm.removeViewImmediate(ball); ballShown = false; hidePanel() } catch (_: Throwable) {}
        Log.d(TAG, "🎾 ball hidden")
    }

    private fun ensureBallCreated(): Boolean {
        if (ballView != null) return true
        if (!canDrawOverlay(context)) return false
        val dm = context.resources.displayMetrics
        val ball = LayoutInflater.from(context).inflate(R.layout.view_float_ball, null, false)
        val params = lp(dp(48), dp(48), dp(8), dm.heightPixels / 3)
        try {
            ballView = ball; ballParams = params
            ball.setOnTouchListener(dragBall)
            ball.setOnClickListener { togglePanel() }
            return true
        } catch (e: Throwable) {
            ballView = null; ballParams = null
            Log.e(TAG, "create ball failed", e); return false
        }
    }

    private fun togglePanel() {
        if (panelView != null) hidePanel() else showPanel()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showPanel() {
        val ball = ballView ?: return
        val panel = LayoutInflater.from(context).inflate(R.layout.view_float_panel, null, false)
        val dm = context.resources.displayMetrics
        val loc = IntArray(2).also { ball.getLocationOnScreen(it) }
        val ballSize = dp(48); val panelW = dp(280); val gap = dp(8)
        val x = (if (loc[0] + ballSize / 2 > dm.widthPixels / 2)
            (loc[0] - panelW - gap).coerceAtLeast(0)
        else
            (loc[0] + ballSize + gap).coerceAtMost((dm.widthPixels - panelW).coerceAtLeast(0)))
        val y = (loc[1] + ballSize + dp(4)).coerceAtMost(dm.heightPixels - dp(200))
        try {
            wm.addView(panel, lp(panelW, WindowManager.LayoutParams.WRAP_CONTENT, x, y))
            panelView = panel; panelParams = panel.layoutParams as? WindowManager.LayoutParams
            panel.setOnTouchListener(dragPanel)
            panel.findViewById<View>(R.id.btn_open_main).setOnClickListener {
                context.startActivity(Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                hidePanel()
            }
            panel.findViewById<View>(R.id.btn_new_mapping).setOnClickListener {
                context.startActivity(Intent(context, MappingConfigActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                hidePanel()
            }
            panel.findViewById<View>(R.id.btn_close).setOnClickListener { hidePanel() }
            scope.launch { renderPanel() }
        } catch (e: Throwable) { Log.e(TAG, "show panel failed", e) }
    }

    private fun hidePanel() {
        runCatching { panelView?.let { wm.removeViewImmediate(it) } }
        panelView = null; panelParams = null
    }

    private fun updateBall() {
        val ball = ballView ?: return
        val ind = ball.findViewById<TextView>(R.id.ball_indicator) ?: return
        val shizukuOk = ShizukuShell.isPermissionGranted()
        val active = AppContainer.getOrCreate(context).mappingRepository
            .getActiveMappingsForApp(InputMonitor.currentPackageName).size
        val (t, c) = when {
            !shizukuOk -> "!" to 0xFFE53935.toInt()
            active > 0 -> "●" to 0xFF43A047.toInt()
            else -> "○" to 0xFF9E9E9E.toInt()
        }
        ind.text = t; ind.setTextColor(c)
    }

    private fun startRefresh() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            while (running) {
                val pkg = InputMonitor.currentPackageName
                val selfForeground = pkg != null && pkg == selfPkg
                if (!selfForeground) {
                    if (ensureBallCreated()) showBallInternal()
                    updateBall(); renderPanel()
                } else {
                    hideBallInternal()
                }
                delay(500)
            }
        }
    }

    private fun startObserve() {
        observeJob?.cancel()
        observeJob = scope.launch(Dispatchers.Default) {
            AppContainer.getOrCreate(context).mappingRepository.apps.collectLatest {
                scope.launch { updateBall(); renderPanel() }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun renderPanel() {
        val panel = panelView ?: return
        withContext(Dispatchers.Main) {
            val title = panel.findViewById<TextView>(R.id.tv_profile)
            val status = panel.findViewById<TextView>(R.id.tv_status)
            val list = panel.findViewById<LinearLayout>(R.id.list_container)
            val debug = panel.findViewById<TextView>(R.id.tv_debug)

            val repo = AppContainer.getOrCreate(context).mappingRepository
            val pkg = InputMonitor.currentPackageName
            val pkgLabel = InputMonitor.currentPackageLabel ?: pkg?.substringAfterLast('.') ?: "未知"
            val app = pkg?.let { repo.getApp(it) }
            val shizukuOk = ShizukuShell.isPermissionGranted()
            val active = repo.getActiveMappingsForApp(pkg).size

            title.text = "📱 $pkgLabel"
            status.text = "${if (shizukuOk) "✅" else "❌"} Shizuku | 🟢 $active 激活"
            debug.text = com.keymapper.app.mapping.MappingEngine.getDebugSummary()

            list.removeAllViews()
            if (app == null) {
                list.addView(TextView(context).apply {
                    text = "📭 该 APP 暂无配置"
                    textSize = 12f; setTextColor(0xFF9E9E9E.toInt())
                    setPadding(dp(8), dp(12), dp(8), dp(12))
                })
            } else {
                renderMappings(list, app, repo)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderMappings(container: LinearLayout, app: AppConfig, repo: com.keymapper.app.mapping.MappingRepository) {
        val scene = app.activeSceneId?.let { app.scenes.firstOrNull { s -> s.id == it } }
            ?: app.scenes.firstOrNull() ?: return
        container.addView(TextView(context).apply {
            text = "🎬 场景: ${scene.name} (${scene.mappings.size})"
            textSize = 11f; setTextColor(0xFF616161.toInt())
            setPadding(dp(8), dp(6), dp(8), dp(2))
        })
        scene.mappings.forEach { m ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8), dp(8), dp(8), dp(8))
                setBackgroundColor(if (m.enabled) 0xFFF5F5F5.toInt() else 0xFFFAFAFA.toInt())
            }
            val ind = TextView(context).apply { text = if (m.enabled) "🟢" else "⚪"; textSize = 14f; width = dp(28) }
            val label = TextView(context).apply {
                text = "${m.button} → ${m.actionType.zh}${if (m.name.isNotBlank()) " (${m.name})" else ""}"
                textSize = 11f; setTextColor(if (m.enabled) 0xFF212121.toInt() else 0xFF9E9E9E.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val btn = AppCompatButton(context).apply {
                text = if (m.enabled) "停" else "启"; textSize = 10f
                setOnClickListener {
                    scope.launch(Dispatchers.Default) {
                        runCatching {
                            val app2 = repo.getApp(app.packageName) ?: return@runCatching
                            val scene2 = app2.scenes.firstOrNull { it.id == scene.id } ?: return@runCatching
                            val mapping2 = scene2.mappings.firstOrNull { it.id == m.id } ?: return@runCatching
                            repo.updateMapping(app.packageName, scene.id, mapping2.copy(enabled = !m.enabled))
                        }
                    }
                }
            }
            row.addView(ind); row.addView(label); row.addView(btn)
            container.addView(row)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun drag(ball: Boolean): View.OnTouchListener {
        val st = floatArrayOf(0f, 0f, 0f, 0f); var moved = false
        return View.OnTouchListener { v, e ->
            val ref = if (ball) ballParams else panelParams
            val p = ref ?: return@OnTouchListener false
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { st[0] = p.x.toFloat(); st[1] = p.y.toFloat(); st[2] = e.rawX; st[3] = e.rawY; moved = false }
                MotionEvent.ACTION_MOVE -> {
                    val dx = e.rawX - st[2]; val dy = e.rawY - st[3]
                    if (abs(dx) > 8f || abs(dy) > 8f) {
                        moved = true; p.x = (st[0] + dx).toInt(); p.y = (st[1] + dy).toInt()
                        runCatching { wm.updateViewLayout(v, p) }
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (!moved && ball) v.performClick()
                }
            }
            true
        }
    }

    private val dragBall = drag(true)
    private val dragPanel = drag(false)
    private fun dp(v: Int): Int = (v * context.resources.displayMetrics.density).toInt()
}
