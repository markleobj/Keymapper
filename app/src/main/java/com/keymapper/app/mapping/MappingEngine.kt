package com.keymapper.app.mapping

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.keymapper.app.model.ActionStep
import com.keymapper.app.model.ActionType
import com.keymapper.app.model.HidButtonEvent
import com.keymapper.app.model.MappingConfig
import com.keymapper.app.service.KeyMapperAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MappingEngine(
    private val repository: MappingRepository
) {
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeMappings: List<MappingConfig> = emptyList()
    private var enabled: Boolean = false
    private var requiredDeviceName: String? = null
    private val buttonStateMap = mutableMapOf<String, Boolean>()

    companion object {
        private const val TAG = "K2ER-Engine"

        @Volatile var debugLastKey: String = "[等待按键...]"
        @Volatile var debugEngineMsg: String = ""
        @Volatile var debugExecMsg: String = ""
        @Volatile var debugInjectorMsg: String = ""

        fun getDebugSummary(): String = buildString {
            appendLine("🔑 按键: $debugLastKey")
            appendLine("⚙️ 引擎: $debugEngineMsg")
            appendLine("💉 注入: $debugInjectorMsg")
            appendLine("🎯 执行: $debugExecMsg")
        }
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        Log.i(TAG, "引擎 ${if (enabled) "已启用" else "已禁用"}")
    }

    fun setRequiredDevice(name: String?) {
        requiredDeviceName = name
        Log.i(TAG, "🎯 引擎设备过滤: ${name ?: "不限制（所有手柄都触发）"}")
    }

    fun updateActiveMappings(list: List<MappingConfig>) {
        val sb = StringBuilder()
        list.forEach { sb.append("${it.button}(${if (it.enabled) "✅" else "❌"}), ") }
        Log.i(TAG, "📋 收到 ${list.size} 条: [$sb]")
        activeMappings = list.filter { it.enabled }
        enabled = true
        debugEngineMsg = "激活 ${activeMappings.size}/${list.size} 条"
        val sb2 = StringBuilder()
        activeMappings.forEach { sb2.append("${it.button}→${it.actionType.name}(${it.targetX},${it.targetY}), ") }
        Log.i(TAG, "🎯 激活 ${activeMappings.size}/${list.size}: [$sb2]")
        updateInjectorStatus()
    }

    private fun updateInjectorStatus() {
        val hasShell = ShellExecutor.hasSecureSettingsPermission()
        val hasA11y = KeyMapperAccessibilityService.isRunning()
        debugInjectorMsg = when {
            hasShell -> "✅ Shell (input tap)"
            hasA11y -> "♿ 无障碍手势 (fallback)"
            else -> "❌ 无注入器！需 ADB 授权 或 开无障碍"
        }
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
            if (pkgMismatch) {
                debugExecMsg = "📱 当前APP不匹配 (前台: ${KeyMapperAccessibilityService.currentPackageName ?: "?"})"
            } else {
                debugExecMsg = "⚠️ 无匹配映射"
            }
            return
        }

        debugExecMsg = "✅ ${mapping.button} → ${mapping.actionType.name} [${mapping.targetPackage ?: "全局"}]"
        Log.i(TAG, "🎯 ${mapping.button} → ${mapping.actionType}")

        if (!hasAnyInjector()) {
            debugExecMsg = "❌ 无注入器！请授权 WRITE_SECURE_SETTINGS 或开启无障碍"
            Log.w(TAG, "⚠️ 无注入器，跳过执行")
            return
        }

        when (mapping.actionType) {
            ActionType.TAP -> if (event.isPressed) dispatchTap(mapping.targetX, mapping.targetY)
            ActionType.LONG_PRESS -> if (event.isPressed) dispatchLongPress(mapping.targetX, mapping.targetY, mapping.duration.ifMinus(500))
            ActionType.SWIPE -> if (event.isPressed) dispatchSwipe(
                mapping.targetX, mapping.targetY,
                mapping.targetX + 200f, mapping.targetY,
                mapping.duration.ifMinus(300)
            )
            ActionType.MOUSE_MOVE -> if (event.isPressed) dispatchMouseMove(mapping)
            ActionType.COMBO -> if (event.isPressed) dispatchCombo(mapping)
            ActionType.DO_NOTHING -> { }
        }
    }

    private fun hasAnyInjector(): Boolean {
        return ShellExecutor.hasSecureSettingsPermission() || KeyMapperAccessibilityService.isRunning()
    }

    private fun dispatchTap(x: Float, y: Float) {
        scope.launch {
            val ok = ShellExecutor.tryTap(x, y)
            if (!ok) fallbackA11yTap(x, y)
        }
    }

    private fun dispatchLongPress(x: Float, y: Float, durationMs: Long) {
        scope.launch {
            val ok = ShellExecutor.tryLongPress(x, y, durationMs)
            if (!ok) fallbackA11yLongPress(x, y, durationMs)
        }
    }

    private fun dispatchSwipe(x1: Float, y1: Float, x2: Float, y2: Float, durationMs: Long) {
        scope.launch {
            val ok = ShellExecutor.trySwipe(x1, y1, x2, y2, durationMs)
            if (!ok) fallbackA11ySwipe(x1, y1, x2, y2, durationMs)
        }
    }

    private fun dispatchMouseMove(mapping: MappingConfig) {
        val x = mapping.targetX; val y = mapping.targetY
        val dur = mapping.duration.ifMinus(200)
        dispatchSwipe(0.5f, 0.5f, x, y, dur)
        handler.postDelayed({ dispatchTap(x, y) }, dur + 50)
    }

    private fun dispatchCombo(mapping: MappingConfig) {
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

    private fun fallbackA11yTap(x: Float, y: Float) {
        KeyMapperAccessibilityService.instance?.performTap(x, y)
            ?: Log.w(TAG, "⚠️ Shell 和 A11y 都不可用，无法 tap")
    }

    private fun fallbackA11yLongPress(x: Float, y: Float, dur: Long) {
        KeyMapperAccessibilityService.instance?.performLongPress(x, y, dur)
            ?: Log.w(TAG, "⚠️ Shell 和 A11y 都不可用，无法 longPress")
    }

    private fun fallbackA11ySwipe(x1: Float, y1: Float, x2: Float, y2: Float, dur: Long) {
        KeyMapperAccessibilityService.instance?.performSwipe(x1, y1, x2, y2, dur)
            ?: Log.w(TAG, "⚠️ Shell 和 A11y 都不可用，无法 swipe")
    }

    private fun Long.ifMinus(default: Long): Long = if (this > 0) this else default

    fun getButtonPressed(): String? = buttonStateMap.entries.firstOrNull { it.value }?.key

    fun getDebugStatus(): String = buildString {
        append("enabled=").append(enabled)
        append("  active=").append(activeMappings.size)
    }
}
