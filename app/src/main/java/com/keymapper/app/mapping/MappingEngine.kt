package com.keymapper.app.mapping

import com.keymapper.app.model.ActionType
import com.keymapper.app.model.HidButtonEvent
import com.keymapper.app.model.MappingConfig
import com.keymapper.app.service.KeyMapperAccessibilityService

class MappingEngine(
    private val repository: MappingRepository
) {
    private var activeMappings: List<MappingConfig> = emptyList()
    private var enabled: Boolean = false
    private val buttonStateMap = mutableMapOf<String, Boolean>()

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    fun updateActiveMappings(list: List<MappingConfig>) {
        activeMappings = list.filter { it.enabled }
    }

    fun onButtonEvent(event: HidButtonEvent) {
        if (!enabled) return

        buttonStateMap[event.buttonId] = event.isPressed

        val mapping = activeMappings
            .firstOrNull { it.button == event.buttonId || it.button == event.buttonName }
            ?: return

        when (mapping.actionType) {
            ActionType.TAP -> {
                if (event.isPressed) dispatchTap(mapping)
            }
            ActionType.LONG_PRESS -> {
                if (event.isPressed) dispatchLongPress(mapping)
            }
            ActionType.SWIPE -> {
                if (event.isPressed) dispatchSwipe(mapping)
            }
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
            300
        )
    }

    private fun Long.ifMinus(default: Long): Long = if (this > 0) this else default

    fun getButtonPressed(): String? = buttonStateMap.entries.firstOrNull { it.value }?.key
}
