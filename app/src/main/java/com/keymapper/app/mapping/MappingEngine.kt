package com.keymapper.app.mapping

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.keymapper.app.model.ActionStep
import com.keymapper.app.model.ActionType
import com.keymapper.app.model.HidButtonEvent
import com.keymapper.app.model.MappingConfig
import com.keymapper.app.service.KeyMapperAccessibilityService

class MappingEngine(
    private val repository: MappingRepository
) {
    private val handler = Handler(Looper.getMainLooper())
    private var activeMappings: List<MappingConfig> = emptyList()
    private var enabled: Boolean = false
    private var requiredDeviceName: String? = null
    private val buttonStateMap = mutableMapOf<String, Boolean>()

    companion object {
        private const val TAG = "K2ER-Engine"

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

    fun setRequiredDevice(name: String?) {
        requiredDeviceName = name
        Log.i(TAG, "🎯 引擎设备过滤: ${name ?: "不限制"}")
    }

    fun updateActiveMappings(list: List<MappingConfig>) {
        activeMappings = list.filter { it.enabled }
        enabled = true
        debugEngineMsg = "激活 ${activeMappings.size}/${list.size} 条"
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

    private fun matchesPackage(mapping: MappingConfig): Boolean {
        val target = mapping.targetPackage
        if (target.isNullOrBlank()) return true
        val current = KeyMapperAccessibilityService.currentPackageName ?: return false
        return current == target
    }

    private fun findMappingFor(event: HidButtonEvent): MappingConfig? {
        return activeMappings.firstOrNull {
            (it.button == event.buttonId || it.button == event.buttonName)
                && matchesDevice(event, it)
                && matchesPackage(it)
        }
    }

    fun isEventBlocked(event: HidButtonEvent): Boolean {
        if (!enabled) return false
        val mapping = findMappingFor(event) ?: return false
        return mapping.blocked
    }

    fun onButtonEvent(event: HidButtonEvent) {
        debugLastKey = "${if (event.isPressed) "↓" else "↑"} ${event.buttonId}(${event.buttonName})"
        if (!enabled) {
            debugExecMsg = "❌ 引擎未启用"
            return
        }

        buttonStateMap[event.buttonId] = event.isPressed

        val mapping = findMappingFor(event) ?: run {
            val pkgMismatch = activeMappings.filter {
                (it.button == event.buttonId || it.button == event.buttonName) && matchesDevice(event, it)
            }.any { !matchesPackage(it) }
            debugExecMsg = if (pkgMismatch) {
                "📱 当前APP不匹配 (前台: ${KeyMapperAccessibilityService.currentPackageName ?: "?"})"
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

    private fun ensureService(): KeyMapperAccessibilityService? {
        val svc = KeyMapperAccessibilityService.instance
        if (svc == null) {
            debugExecMsg = "❌ 无障碍服务未运行！请开启无障碍"
            Log.w(TAG, "⚠️ AccessibilityService 未运行，跳过执行")
        }
        return svc
    }

    private fun executeAction(mapping: MappingConfig) {
        val svc = ensureService() ?: return
        when (mapping.actionType) {
            ActionType.TAP -> svc.performTap(mapping.targetX, mapping.targetY)
            ActionType.LONG_PRESS -> svc.performLongPress(
                mapping.targetX, mapping.targetY, mapping.duration.ifMinus(500)
            )
            ActionType.SWIPE -> svc.performSwipe(
                mapping.targetX, mapping.targetY,
                mapping.targetX + 200f, mapping.targetY,
                mapping.duration.ifMinus(300)
            )
            ActionType.MOUSE_MOVE -> {
                val dur = mapping.duration.ifMinus(200)
                svc.performSwipe(0.5f, 0.5f, mapping.targetX, mapping.targetY, dur)
                handler.postDelayed({ svc.performTap(mapping.targetX, mapping.targetY) }, dur + 50)
            }
            ActionType.COMBO -> executeCombo(mapping, svc)
            ActionType.DO_NOTHING -> { }
        }
    }

    private fun executeCombo(mapping: MappingConfig, svc: KeyMapperAccessibilityService) {
        if (mapping.steps.isEmpty()) { svc.performTap(mapping.targetX, mapping.targetY); return }
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
            handler.postDelayed({ executeStep(step, i + 1, mapping.steps.size, svc) }, delay)
        }
    }

    private fun executeStep(step: ActionStep, index: Int, total: Int, svc: KeyMapperAccessibilityService) {
        Log.i(TAG, "  ▶ 步骤 $index/$total: ${step.type} @(${step.targetX},${step.targetY})")
        when (step.type) {
            ActionType.TAP -> svc.performTap(step.targetX, step.targetY)
            ActionType.LONG_PRESS -> svc.performLongPress(step.targetX, step.targetY, step.duration.ifMinus(500))
            ActionType.SWIPE -> svc.performSwipe(
                step.targetX, step.targetY,
                step.targetX + 200f, step.targetY,
                step.duration.ifMinus(300)
            )
            ActionType.MOUSE_MOVE -> {
                val dur = step.duration.ifMinus(200)
                svc.performSwipe(0.5f, 0.5f, step.targetX, step.targetY, dur)
                handler.postDelayed({ svc.performTap(step.targetX, step.targetY) }, dur + 50)
            }
            ActionType.COMBO, ActionType.DO_NOTHING -> { }
        }
    }

    private fun Long.ifMinus(default: Long): Long = if (this > 0) this else default

    fun getButtonPressed(): String? = buttonStateMap.entries.firstOrNull { it.value }?.key
}
