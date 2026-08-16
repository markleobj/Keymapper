package com.keymapper.app.mapping

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.keymapper.app.model.ActionStep
import com.keymapper.app.model.ActionType
import com.keymapper.app.model.HidButtonEvent
import com.keymapper.app.model.MappingConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MappingEngine(
    private val repository: MappingRepository
) {
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var activeMappings: List<MappingConfig> = emptyList()
    private var enabled: Boolean = false
    private val buttonStateMap = mutableMapOf<String, Boolean>()

    companion object {
        private const val TAG = "Shizuku-K2ER"

        @Volatile var debugLastKey: String = "[等待按键...]"
        @Volatile var debugEngineMsg: String = ""
        @Volatile var debugExecMsg: String = ""

        fun getDebugSummary(): String = buildString {
            appendLine("🔑 按键: $debugLastKey")
            appendLine("⚙️ 引擎: $debugEngineMsg")
            appendLine("🎯 执行: $debugExecMsg")
        }
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        Log.i(TAG, "引擎 ${if (enabled) "已启用" else "已禁用"}")
    }

    fun updateActiveMappings(list: List<MappingConfig>) {
        activeMappings = list.filter { it.enabled }
        enabled = true
        debugEngineMsg = "激活 ${activeMappings.size}/${list.size} 条 | Shizuku=${if (ShizukuShell.isPermissionGranted()) "✅" else "❌"}"
        val sb2 = StringBuilder()
        activeMappings.forEach { sb2.append("${it.button}→${it.actionType.name}(${it.targetX},${it.targetY}), ") }
        Log.i(TAG, "🎯 激活 ${activeMappings.size}/${list.size}: [$sb2]")
    }

    private fun matchesDevice(event: HidButtonEvent, mapping: MappingConfig): Boolean {
        if (mapping.deviceAddress.isNullOrBlank()) return true
        val dev = event.deviceName
        if (dev.isNullOrBlank()) return true
        return dev.equals(mapping.deviceAddress, ignoreCase = true) ||
               dev.contains(mapping.deviceAddress, ignoreCase = true)
    }

    private fun matchesPackage(mapping: MappingConfig, currentPkg: String?): Boolean {
        val target = mapping.targetPackage
        if (target.isNullOrBlank()) return true
        return currentPkg != null && currentPkg == target
    }

    private fun findMappingFor(event: HidButtonEvent, currentPkg: String?): MappingConfig? {
        return activeMappings.firstOrNull {
            (it.button == event.buttonId || it.button == event.buttonName)
                && matchesDevice(event, it)
                && matchesPackage(it, currentPkg)
        }
    }

    fun isEventBlocked(event: HidButtonEvent, currentPkg: String?): Boolean {
        if (!enabled) return false
        val mapping = findMappingFor(event, currentPkg) ?: return false
        return mapping.blocked
    }

    fun onButtonEvent(event: HidButtonEvent, currentPkg: String?) {
        debugLastKey = "${if (event.isPressed) "↓" else "↑"} ${event.buttonId}(${event.buttonName})"
        if (!enabled) {
            debugExecMsg = "❌ 引擎未启用"
            return
        }

        buttonStateMap[event.buttonId] = event.isPressed

        val mapping = findMappingFor(event, currentPkg) ?: run {
            val pkgMismatch = activeMappings.filter {
                (it.button == event.buttonId || it.button == event.buttonName) && matchesDevice(event, it)
            }.any { !matchesPackage(it, currentPkg) }
            debugExecMsg = if (pkgMismatch) {
                "📱 当前APP不匹配 (前台: $currentPkg)"
            } else {
                "⚠️ 无匹配映射"
            }
            return
        }

        if (event.isPressed) {
            debugExecMsg = "✅ ${mapping.button} → ${mapping.actionType.name} [${mapping.targetPackage ?: "全局"}]"
            Log.i(TAG, "🎯 ${mapping.button} → ${mapping.actionType} @(${mapping.targetX},${mapping.targetY})")
            executeAction(mapping)
        }
    }

    private fun executeAction(mapping: MappingConfig) {
        when (mapping.actionType) {
            ActionType.TAP -> dispatchTap(mapping.targetX, mapping.targetY)
            ActionType.LONG_PRESS -> dispatchLongPress(
                mapping.targetX, mapping.targetY, mapping.duration.ifMinus(500)
            )
            ActionType.SWIPE -> dispatchSwipe(
                mapping.targetX, mapping.targetY,
                mapping.targetX + 200f, mapping.targetY,
                mapping.duration.ifMinus(300)
            )
            ActionType.MOUSE_MOVE -> {
                val dur = mapping.duration.ifMinus(200)
                dispatchSwipe(0.5f, 0.5f, mapping.targetX, mapping.targetY, dur)
                handler.postDelayed({ dispatchTap(mapping.targetX, mapping.targetY) }, dur + 50)
            }
            ActionType.COMBO -> executeCombo(mapping)
            ActionType.DO_NOTHING -> { }
        }
    }

    private fun dispatchTap(x: Float, y: Float) {
        scope.launch {
            val ok = ShizukuShell.tryInputTap(x, y)
            if (!ok) Log.w(TAG, "⚠️ tap 失败: ($x, $y)")
        }
    }

    private fun dispatchSwipe(x1: Float, y1: Float, x2: Float, y2: Float, durationMs: Long) {
        scope.launch {
            ShizukuShell.tryInputSwipe(x1, y1, x2, y2, durationMs)
        }
    }

    private fun dispatchLongPress(x: Float, y: Float, durationMs: Long) {
        scope.launch {
            ShizukuShell.tryLongPress(x, y, durationMs)
        }
    }

    private fun dispatchKeyevent(keyCode: Int) {
        scope.launch {
            ShizukuShell.tryInputKeyevent(keyCode)
        }
    }

    private fun executeCombo(mapping: MappingConfig) {
        if (mapping.steps.isEmpty()) { dispatchTap(mapping.targetX, mapping.targetY); return }
        Log.i(TAG, "🎬 COMBO ${mapping.steps.size}步")
        var scheduledAt = 0L
        mapping.steps.forEachIndexed { i, step ->
            scheduledAt += step.delayMs
            val delay = scheduledAt
            scheduledAt += when (step.type) {
                ActionType.TAP -> 80L
                ActionType.LONG_PRESS -> step.duration.ifMinus(500) + 50L
                ActionType.SWIPE -> step.duration.ifMinus(300) + 50L
                ActionType.MOUSE_MOVE -> step.duration.ifMinus(200) + 100L
                else -> 50L
            }
            handler.postDelayed({ executeStep(step, i + 1, mapping.steps.size) }, delay)
        }
    }

    private fun executeStep(step: ActionStep, index: Int, total: Int) {
        Log.i(TAG, "  ▶ 步骤 $index/$total: ${step.type} @(${step.targetX},${step.targetY})")
        when (step.type) {
            ActionType.TAP -> dispatchTap(step.targetX, step.targetY)
            ActionType.LONG_PRESS -> dispatchLongPress(step.targetX, step.targetY, step.duration.ifMinus(500))
            ActionType.SWIPE -> dispatchSwipe(
                step.targetX, step.targetY,
                step.targetX + 200f, step.targetY,
                step.duration.ifMinus(300)
            )
            ActionType.MOUSE_MOVE -> {
                val dur = step.duration.ifMinus(200)
                dispatchSwipe(0.5f, 0.5f, step.targetX, step.targetY, dur)
                handler.postDelayed({ dispatchTap(step.targetX, step.targetY) }, dur + 50)
            }
            ActionType.COMBO, ActionType.DO_NOTHING -> { }
        }
    }

    private fun Long.ifMinus(default: Long): Long = if (this > 0) this else default

    fun getButtonPressed(): String? = buttonStateMap.entries.firstOrNull { it.value }?.key
}
