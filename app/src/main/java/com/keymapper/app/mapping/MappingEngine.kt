package com.keymapper.app.mapping

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.keymapper.app.model.ActionStep
import com.keymapper.app.model.ActionType
import com.keymapper.app.model.HidButtonEvent
import com.keymapper.app.model.Mapping
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MappingEngine(
    private val repository: MappingRepository
) {
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile var currentMappings: List<Mapping> = emptyList()
        private set
    @Volatile var enabled: Boolean = false

    companion object {
        private const val TAG = "K2ER-Engine"

        @Volatile var debugLastKey: String = "[等待按键...]"
        @Volatile var debugLastAction: String = ""
        @Volatile var debugExecMsg: String = ""

        fun getDebugSummary(): String = buildString {
            appendLine("🔑 $debugLastKey")
            appendLine("🎯 $debugLastAction")
            appendLine("⚡ Shizuku=${if (ShizukuShell.isPermissionGranted()) "✅" else "❌"}")
            append("📋 ${debugExecMsg}")
        }
    }


    fun refreshForPackage(packageName: String?) {
        currentMappings = repository.getActiveMappingsForApp(packageName)
        debugExecMsg = "激活 ${currentMappings.size} 条（pkg=$packageName）"
        Log.i(TAG, "🎯 刷新映射: ${currentMappings.size} 条 @ $packageName")
    }

    fun onButtonEvent(event: HidButtonEvent, currentPkg: String?) {
        debugLastKey = "${if (event.isPressed) "↓" else "↑"} ${event.buttonId}"
        if (!enabled) { debugExecMsg = "❌ 引擎未启用"; return }
        if (currentMappings.isEmpty()) refreshForPackage(currentPkg)

        val mapping = currentMappings.firstOrNull { it.button == event.buttonId }
        if (mapping == null) {
            debugExecMsg = "⚠️ $currentPkg 无匹配映射"; return
        }
        if (!mapping.enabled) return

        if (event.isPressed) {
            debugLastAction = "${mapping.button} → ${mapping.actionType.zh} @(${mapping.targetX},${mapping.targetY})"
            Log.i(TAG, "🎮 ${mapping.button} → ${mapping.actionType}")
            execute(mapping)
        }
    }

    private fun execute(m: Mapping) {
        when (m.actionType) {
            ActionType.TAP -> dispatchTap(m.targetX, m.targetY)
            ActionType.LONG_PRESS -> dispatchLongPress(m.targetX, m.targetY, m.durationMs.ifMinus(500))
            ActionType.SWIPE -> dispatchSwipe(m.targetX, m.targetY, m.targetX + 200f, m.targetY, m.durationMs.ifMinus(300))
            ActionType.COMBO -> executeCombo(m.steps)
            ActionType.DO_NOTHING -> { }
        }
    }

    private fun executeCombo(steps: List<ActionStep>) {
        if (steps.isEmpty()) return
        var at = 0L
        steps.forEachIndexed { i, step ->
            at += step.delayMs
            val dur = step.durationMs.ifMinus(300)
            handler.postDelayed({
                when (step.type) {
                    ActionType.TAP -> dispatchTap(step.targetX, step.targetY)
                    ActionType.LONG_PRESS -> dispatchLongPress(step.targetX, step.targetY, dur)
                    ActionType.SWIPE -> dispatchSwipe(step.targetX, step.targetY, step.targetX + 200f, step.targetY, dur)
                    else -> { }
                }
            }, at)
        }
    }

    private fun dispatchTap(x: Float, y: Float) {
        scope.launch { ShizukuShell.execSync("input tap ${x.toInt()} ${y.toInt()}") }
    }

    private fun dispatchSwipe(x1: Float, y1: Float, x2: Float, y2: Float, durationMs: Long) {
        scope.launch { ShizukuShell.execSync("input swipe ${x1.toInt()} ${y1.toInt()} ${x2.toInt()} ${y2.toInt()} $durationMs") }
    }

    private fun dispatchLongPress(x: Float, y: Float, durationMs: Long) {
        scope.launch { ShizukuShell.execSync("input swipe ${x.toInt()} ${y.toInt()} ${x.toInt()} ${y.toInt()} $durationMs") }
    }

    private fun Long.ifMinus(default: Long): Long = if (this > 0) this else default

    fun onJoystickMove(axisX: Float, axisY: Float, currentPkg: String?) {}
}
