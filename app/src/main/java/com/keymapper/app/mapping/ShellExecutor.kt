package com.keymapper.app.mapping

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ShellExecutor {
    private const val TAG = "ShellExec"

    @Volatile
    private var hasSecurePermission: Boolean? = null

    fun hasSecureSettingsPermission(): Boolean {
        hasSecurePermission?.let { return it }
        return try {
            val proc = Runtime.getRuntime().exec(arrayOf("settings", "get", "secure", "enabled_accessibility_services"))
            proc.waitFor()
            true
        } catch (e: Throwable) {
            false
        }.also { hasSecurePermission = it }
    }

    suspend fun tryTap(x: Float, y: Float): Boolean {
        return exec(arrayOf("input", "tap", x.toInt().toString(), y.toInt().toString()))
    }

    suspend fun trySwipe(x1: Float, y1: Float, x2: Float, y2: Float, durationMs: Long): Boolean {
        return exec(arrayOf(
            "input", "swipe",
            x1.toInt().toString(), y1.toInt().toString(),
            x2.toInt().toString(), y2.toInt().toString(),
            durationMs.toString()
        ))
    }

    suspend fun tryLongPress(x: Float, y: Float, durationMs: Long): Boolean {
        return trySwipe(x, y, x, y, durationMs)
    }

    suspend fun tryKeyevent(keyCode: Int): Boolean {
        return exec(arrayOf("input", "keyevent", keyCode.toString()))
    }

    private suspend fun exec(cmd: Array<String>): Boolean = withContext(Dispatchers.IO) {
        try {
            val proc = Runtime.getRuntime().exec(cmd)
            val exit = proc.waitFor()
            if (exit != 0) {
                val err = proc.errorStream.bufferedReader().use { it.readText() }
                Log.w(TAG, "cmd=${cmd.joinToString(" ")} exit=$exit err=$err")
                false
            } else {
                true
            }
        } catch (e: Throwable) {
            Log.e(TAG, "exec failed cmd=${cmd.joinToString(" ")}", e)
            false
        }
    }

    fun invalidatePermissionCache() {
        hasSecurePermission = null
    }
}
