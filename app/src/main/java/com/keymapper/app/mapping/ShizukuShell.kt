package com.keymapper.app.mapping

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.lang.reflect.Method

object ShizukuShell {
    private const val TAG = "ShizukuShell"
    private const val PERMISSION_REQUEST_CODE = 1001

    private var newProcessMethod: Method? = null

    private fun getNewProcessMethod(): Method? {
        newProcessMethod?.let { return it }
        return try {
            val m = Shizuku::class.java.getDeclaredMethod(
                "newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java
            )
            m.isAccessible = true
            newProcessMethod = m
            m
        } catch (e: Throwable) {
            Log.e(TAG, "反射获取 newProcess 方法失败", e)
            null
        }
    }

    private fun execShell(cmd: String): Process? {
        val m = getNewProcessMethod() ?: return null
        return try {
            @Suppress("UNCHECKED_CAST")
            m.invoke(null, arrayOf("/system/bin/sh", "-c", cmd), null, null) as Process
        } catch (e: Throwable) {
            Log.e(TAG, "反射调用 newProcess 失败: $cmd", e)
            null
        }
    }

    fun execProcess(cmd: String): Process? {
        if (!isPermissionGranted()) return null
        return execShell(cmd)
    }

    fun execSync(cmd: String): String {
        if (!isPermissionGranted()) return ""
        return try {
            val process = execShell(cmd) ?: return ""
            val out = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            out
        } catch (e: Throwable) {
            Log.e(TAG, "execSync failed: $cmd", e)
            ""
        }
    }

    fun isBinderAvailable(): Boolean = try {
        Shizuku.pingBinder()
    } catch (_: IllegalStateException) {
        false
    }

    fun isPermissionGranted(): Boolean = try {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (_: IllegalStateException) {
        false
    }

    fun requestPermission(context: Context, onResult: (granted: Boolean, code: Int) -> Unit) {
        Shizuku.addRequestPermissionResultListener(Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            onResult(grantResult == PackageManager.PERMISSION_GRANTED, requestCode)
        })
        Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
    }

    suspend fun tryInputTap(x: Float, y: Float): Boolean =
        exec("input tap ${x.toInt()} ${y.toInt()}")

    suspend fun tryInputSwipe(x1: Float, y1: Float, x2: Float, y2: Float, durationMs: Long): Boolean =
        exec("input swipe ${x1.toInt()} ${y1.toInt()} ${x2.toInt()} ${y2.toInt()} ${durationMs}")

    suspend fun tryInputKeyevent(keyCode: Int): Boolean =
        exec("input keyevent $keyCode")

    suspend fun tryLongPress(x: Float, y: Float, durationMs: Long): Boolean =
        exec("input swipe ${x.toInt()} ${y.toInt()} ${x.toInt()} ${y.toInt()} ${durationMs}")

    suspend fun tryGetForegroundPackage(): String? {
        val out = execForOutput("dumpsys activity top 2>/dev/null | head -5")
        return out?.lines()?.firstOrNull { it.contains("ACTIVITY") }?.let { line ->
            val m = Regex("ACTIVITY\\s+([a-zA-Z0-9_.]+)/").find(line)
            m?.groupValues?.get(1)
        }
    }

    private suspend fun exec(cmd: String): Boolean = withContext(Dispatchers.IO) {
        if (!isPermissionGranted()) {
            Log.w(TAG, "Shizuku 权限未授予")
            return@withContext false
        }
        try {
            val process = execShell(cmd) ?: return@withContext false
            val exit = process.waitFor()
            if (exit != 0) {
                val err = process.errorStream.bufferedReader().use { it.readText() }
                Log.w(TAG, "cmd='$cmd' exit=$exit err=$err")
            }
            exit == 0
        } catch (e: Throwable) {
            Log.e(TAG, "exec failed: $cmd", e)
            false
        }
    }

    private suspend fun execForOutput(cmd: String): String? = withContext(Dispatchers.IO) {
        if (!isPermissionGranted()) return@withContext null
        try {
            val process = execShell(cmd) ?: return@withContext null
            val out = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            out
        } catch (e: Throwable) {
            Log.e(TAG, "execForOutput failed: $cmd", e)
            null
        }
    }
}
