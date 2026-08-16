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
        activeMappings = list.filter { it.enabled }
        enabled = true
        Log.i(TAG, "激活映射数: ${activeMappings.size}, 引擎已自动启用")
    }

    private fun matchesDevice(event: HidButtonEvent, mapping: MappingConfig): Boolean {
        if (mapping.deviceAddress.isNullOrBlank()) return true
        val dev = event.deviceName
        if (dev.isNullOrBlank()) return true
        return dev.equals(mapping.deviceAddress, ignoreCase = true) ||
               dev.contains(mapping.deviceAddress, ignoreCase = true)
    }

    fun isEventBlocked(event: HidButtonEvent): Boolean {
        if (!enabled) return false
        val mapping = activeMappings.firstOrNull {
            (it.button == event.buttonId || it.button == event.buttonName) && matchesDevice(event, it)
        } ?: return false
        return mapping.blocked
    }

    fun onButtonEvent(event: HidButtonEvent) {
        if (!enabled) {
            Log.w(TAG, "⚠️ onButtonEvent: 引擎未启用 (enabled=false)")
            return
        }

        buttonStateMap[event.buttonId] = event.isPressed

        val mapping = activeMappings.firstOrNull {
            (it.button == event.buttonId || it.button == event.buttonName) && matchesDevice(event, it)
        } ?: run {
            if (activeMappings.isNotEmpty()) {
                Log.d(TAG, "按键 ${event.buttonId} 没有匹配的映射 (已配置: ${activeMappings.map { it.button }})")
            }
            return
        }

        Log.i(TAG, "🎯 触发: ${mapping.button} → ${mapping.actionType}")

        when (mapping.actionType) {
            ActionType.TAP -> if (event.isPressed) dispatchTap(mapping)
            ActionType.LONG_PRESS -> if (event.isPressed) dispatchLongPress(mapping)
            ActionType.SWIPE -> if (event.isPressed) dispatchSwipe(mapping)
            ActionType.MOUSE_MOVE -> if (event.isPressed) dispatchMouseMove(mapping)
            ActionType.COMBO -> if (event.isPressed) dispatchCombo(mapping)
            ActionType.DO_NOTHING -> { /* 只屏蔽 */ }
        }
    }

    private fun dispatchTap(mapping: MappingConfig) {
        KeyMapperAccessibilityService.instance?.performTap(mapping.targetX, mapping.targetY)
    }

    private fun dispatchLongPress(mapping: MappingConfig) {
        KeyMapperAccessibilityService.instance?.performLongPress(
            mapping.targetX, mapping.targetY, mapping.duration.ifMinus(500)
        )
    }

    private fun dispatchSwipe(mapping: MappingConfig) {
        KeyMapperAccessibilityService.instance?.performSwipe(
            mapping.targetX, mapping.targetY,
            mapping.targetX + 200f, mapping.targetY,
            mapping.duration.ifMinus(300)
        )
    }

    private fun dispatchMouseMove(mapping: MappingConfig) {
        // 模拟移动 + 点击：先从当前位置移到目标，再 tap
        val x = mapping.targetX
        val y = mapping.targetY
        val moveDuration = mapping.duration.ifMinus(200)
        Log.i(TAG, "🖱️ 鼠标移动 → ($x,$y) 时长${moveDuration}ms")
        KeyMapperAccessibilityService.instance?.performSwipe(
            0.5f, 0.5f, x, y, moveDuration
        )
        handler.postDelayed({
            KeyMapperAccessibilityService.instance?.performTap(x, y)
        }, moveDuration + 50)
    }

    private fun dispatchCombo(mapping: MappingConfig) {
        if (mapping.steps.isEmpty()) {
            // 没有步骤就退化成单动作
            dispatchTap(mapping)
            return
        }
        Log.i(TAG, "🎬 执行 COMBO，共 ${mapping.steps.size} 步")
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
            handler.postDelayed({
                executeStep(step, i + 1, mapping.steps.size)
            }, delay)
        }
    }

    private fun executeStep(step: ActionStep, index: Int, total: Int) {
        val svc = KeyMapperAccessibilityService.instance ?: run {
            Log.w(TAG, "⚠️ 无障碍服务未运行，跳过步骤 $index/$total")
            return
        }
        Log.i(TAG, "  ▶ 步骤 $index/$total: ${step.type} @(${step.targetX},${step.targetY}) ${step.comment}")
        when (step.type) {
            ActionType.TAP -> svc.performTap(step.targetX, step.targetY)
            ActionType.LONG_PRESS -> svc.performLongPress(step.targetX, step.targetY, step.duration.ifMinus(500))
            ActionType.SWIPE -> svc.performSwipe(
                step.targetX, step.targetY,
                step.targetX + 200f, step.targetY,
                step.duration.ifMinus(300)
            )
            ActionType.MOUSE_MOVE -> {
                svc.performSwipe(0.5f, 0.5f, step.targetX, step.targetY, step.duration.ifMinus(200))
                handler.postDelayed({ svc.performTap(step.targetX, step.targetY) }, step.duration.ifMinus(200) + 50)
            }
            ActionType.COMBO -> { /* 不支持嵌套 */ }
            ActionType.DO_NOTHING -> { /* 延时占位 */ }
        }
    }

    private fun Long.ifMinus(default: Long): Long = if (this > 0) this else default

    fun getButtonPressed(): String? = buttonStateMap.entries.firstOrNull { it.value }?.key
}
