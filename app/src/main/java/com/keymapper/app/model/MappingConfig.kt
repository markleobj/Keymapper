package com.keymapper.app.model

data class MappingConfig(
    val id: String,
    val button: String,
    val actionType: ActionType,
    val targetX: Float,
    val targetY: Float,
    val duration: Long = 0L,
    val enabled: Boolean = true,
    val deviceAddress: String? = null
)

enum class ActionType {
    TAP,
    LONG_PRESS,
    SWIPE;

    companion object {
        fun fromString(s: String): ActionType = entries.find { it.name == s } ?: TAP
    }
}

data class DeviceInfo(val name: String?, val address: String)

data class HidButtonEvent(
    val buttonId: String,
    val buttonName: String,
    val isPressed: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class JoystickEvent(
    val axis: String,
    val x: Float,
    val y: Float,
    val timestamp: Long = System.currentTimeMillis()
)
