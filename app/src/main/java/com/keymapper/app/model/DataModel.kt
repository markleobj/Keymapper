package com.keymapper.app.model

enum class ActionType(val zh: String) {
    TAP("点击"),
    LONG_PRESS("长按"),
    SWIPE("滑动"),
    COMBO("组合"),
    DO_NOTHING("屏蔽");

    companion object {
        fun fromString(s: String): ActionType = entries.find { it.name == s } ?: TAP
    }
}

data class ActionStep(
    val type: ActionType,
    val targetX: Float = 0f,
    val targetY: Float = 0f,
    val durationMs: Long = 300L,
    val delayMs: Long = 0L
)

data class Mapping(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val button: String,
    val actionType: ActionType = ActionType.TAP,
    val targetX: Float = 0f,
    val targetY: Float = 0f,
    val durationMs: Long = 300L,
    val enabled: Boolean = true,
    val blocked: Boolean = true,
    val steps: List<ActionStep> = emptyList()
)

data class Scene(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val enabled: Boolean = true,
    val mappings: List<Mapping> = emptyList()
)

data class AppConfig(
    val packageName: String,
    val appName: String = "",
    val iconBase64: String? = null,
    val scenes: List<Scene> = listOf(Scene(name = "全局")),
    val activeSceneId: String? = null,
    val enabled: Boolean = true
)

data class HidButtonEvent(
    val buttonId: String,
    val buttonName: String,
    val isPressed: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceName: String? = null
)
