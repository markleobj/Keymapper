package com.keymapper.app.model

import com.google.gson.annotations.SerializedName

enum class ActionType {
    TAP, LONG_PRESS, SWIPE, MOUSE_MOVE, COMBO, DO_NOTHING;

    companion object {
        fun fromString(s: String): ActionType = entries.find { it.name == s } ?: TAP
    }
}

data class ActionStep(
    val type: ActionType,
    val targetX: Float = 0f,
    val targetY: Float = 0f,
    val duration: Long = 0L,
    val delayMs: Long = 0L,
    val comment: String = ""
)

data class MappingConfig(
    val id: String,
    val name: String = "",
    val button: String,
    val actionType: ActionType,
    val targetX: Float,
    val targetY: Float,
    val duration: Long = 0L,
    val enabled: Boolean = true,
    val deviceAddress: String? = null,
    val blocked: Boolean = true,
    val steps: List<ActionStep> = emptyList()
)

data class HidButtonEvent(
    val buttonId: String,
    val buttonName: String,
    val isPressed: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class DeviceInfo(val name: String?, val address: String)
