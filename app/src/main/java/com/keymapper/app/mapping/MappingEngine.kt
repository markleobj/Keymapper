package com.keymapper.app.mapping

import android.content.Context
import com.keymapper.app.model.ActionType
import com.keymapper.app.model.HidButtonEvent
import com.keymapper.app.model.MappingConfig
import com.keymapper.app.service.KeyMapperAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MappingEngine(
    context: Context,
    private val repository: MappingRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _activeMappings = MutableStateFlow<List<MappingConfig>>(emptyList())
    val activeMappings: StateFlow<List<MappingConfig>> = _activeMappings.asStateFlow()

    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val buttonStateMap = mutableMapOf<String, Boolean>()

    init {
        scope.launch {
            repository.mappings.collect { list ->
                _activeMappings.value = list.filter { it.enabled }
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        _enabled.value = enabled
    }

    fun onButtonEvent(event: HidButtonEvent) {
        if (!_enabled.value) return

        buttonStateMap[event.buttonId] = event.isPressed

        val mapping = _activeMappings.value
            .firstOrNull { it.button == event.buttonId || it.button == event.buttonName }
            ?: return

        // Edge-triggered: only fire on press-down for tap, on release for long-press confirmation
        when (mapping.actionType) {
            ActionType.TAP -> {
                if (event.isPressed) {
                    dispatchTap(mapping)
                }
            }
            ActionType.LONG_PRESS -> {
                if (event.isPressed) {
                    dispatchLongPress(mapping)
                }
            }
            ActionType.SWIPE -> {
                if (event.isPressed) {
                    dispatchSwipe(mapping)
                }
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
        // Swipe from (x,y) to (x+offset, y) - placeholder
        KeyMapperAccessibilityService.instance?.performSwipe(
            mapping.targetX, mapping.targetY,
            mapping.targetX + 200f, mapping.targetY,
            300
        )
    }

    private fun Long.ifMinus(default: Long): Long = if (this > 0) this else default

    fun getButtonPressed(): String? = buttonStateMap.entries.firstOrNull { it.value }?.key
}
