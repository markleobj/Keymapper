package com.keymapper.app.service

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import com.keymapper.app.AppContainer
import com.keymapper.app.mapping.ShizukuShell
import com.keymapper.app.model.HidButtonEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible

object InputMonitor {
    private const val TAG = "InputMonitor-K2ER"

    @Volatile var currentPackageName: String? = null
        private set
    @Volatile var currentPackageLabel: String? = null
    @Volatile var deviceCount: Int = 0
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var refreshJob: Job? = null
    private var geteventJob: Job? = null

    @Volatile private var running = false

    fun start(context: Context) {
        if (running) return
        running = true
        startPackageMonitor()
        startGeteventListener()
        Log.i(TAG, "✅ InputMonitor started (K2er mode)")
    }

    fun stop() {
        running = false
        refreshJob?.cancel()
        refreshJob = null
        geteventJob?.cancel()
        geteventJob = null
        Log.i(TAG, "InputMonitor stopped")
    }

    private fun startPackageMonitor() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            while (running) {
                refreshForegroundPackage()
                delay(500)
            }
        }
    }

    fun refreshForegroundPackage() {
        val pkg = ShizukuShell.execSync("dumpsys activity top 2>/dev/null | head -5")
            .lineSequence()
            .firstOrNull { it.contains("ACTIVITY") }
            ?.let { line ->
                val m = Regex("ACTIVITY\\s+([a-zA-Z0-9_.]+)/").find(line)
                m?.groupValues?.getOrNull(1)
            }
            ?: ShizukuShell.execSync("dumpsys window windows 2>/dev/null | grep -E 'mCurrentFocus|mFocusedApp' | head -1")
                .let { s ->
                    val m = Regex("([a-zA-Z0-9_.]+)/").find(s)
                    m?.groupValues?.getOrNull(1)
                }

        if (pkg != null && pkg != currentPackageName) {
            currentPackageName = pkg
            currentPackageLabel = getAppLabel(com.keymapper.app.AppContainer.ctx, pkg)
            runCatching {
                val engine = com.keymapper.app.AppContainer.require().mappingEngine
                engine.refreshForPackage(pkg)
            }
            Log.d(TAG, "📱 前台 APP: $pkg (${currentPackageLabel})")
        }
    }

    private fun getAppLabel(ctx: Context, pkg: String): String? {
        return try {
            ctx.packageManager.getApplicationLabel(ctx.packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (_: Throwable) { null }
    }

    private fun startGeteventListener() {
        geteventJob?.cancel()
        geteventJob = scope.launch {
            while (running) {
                if (!ShizukuShell.isPermissionGranted()) {
                    delay(2000); continue
                }
                val paths = findInputDevices()
                if (paths.isEmpty()) {
                    delay(2000); continue
                }
                Log.i(TAG, "🎮 getevent 监听: ${paths.joinToString()}")
                val ok = runGeteventLoop(paths)
                if (!ok) delay(1000)
            }
        }
    }

    private suspend fun findInputDevices(): List<String> {
        val out = ShizukuShell.execSync("dumpsys input 2>/dev/null")
        val devices = mutableSetOf<String>()
        out.lineSequence().forEach { line ->
            val hasGamepad = line.contains("Gamepad") || line.contains("Joystick") || line.contains("DPad") || line.contains("Keyboard")
            val m = Regex("(\\/dev\\/input\\/event\\d+)").find(line)
            if (hasGamepad && m != null) devices.add(m.groupValues[1])
        }
        if (devices.isEmpty()) {
            ShizukuShell.execSync("ls /dev/input/event* 2>/dev/null")
                .trim().split("\n").filter { it.isNotBlank() }.forEach { devices.add(it.trim()) }
        }
        val found = devices.toList()
        deviceCount = found.size
        return found
    }

    private suspend fun runGeteventLoop(paths: List<String>): Boolean {
        val process = ShizukuShell.execProcess("getevent -l ${paths.joinToString(" ")}") ?: return false
        try {
            val reader = process.inputStream.bufferedReader()
            var lastBtn = ""; var lastTime = 0L
            while (running) {
                val line: String? = try {
                    runInterruptible(Dispatchers.IO) { reader.readLine() }
                } catch (_: java.io.IOException) {
                    if (Thread.currentThread().isInterrupted) break
                    null
                }
                line ?: break
                val parsed = parseGeteventLine(line) ?: continue
                val now = System.currentTimeMillis()
                if (parsed.buttonId == lastBtn && now - lastTime < 40L && parsed.isDown) continue
                lastBtn = parsed.buttonId; lastTime = now

                val btnEvent = HidButtonEvent(
                    buttonId = parsed.buttonId,
                    buttonName = parsed.buttonId,
                    isPressed = parsed.isDown,
                    timestamp = now,
                    deviceName = parsed.devicePath
                )

                val engine = runCatching { com.keymapper.app.AppContainer.require().mappingEngine }.getOrNull()
                runCatching { engine?.onButtonEvent(btnEvent, currentPackageName) }
            }
            process.waitFor(); return true
        } catch (e: CancellationException) {
            Log.i(TAG, "getevent cancelled"); return false
        } catch (e: Throwable) {
            Log.e(TAG, "getevent error", e); return false
        } finally {
            runCatching { process.destroy() }
        }
    }

    private data class ParsedKey(val buttonId: String, val isDown: Boolean, val devicePath: String)

    private fun parseGeteventLine(line: String): ParsedKey? {
        if (!line.contains("EV_KEY")) return null

        val keyMatch = Regex("KEY_(\\w+)").find(line) ?: return null
        val keyName = keyMatch.groupValues[1]

        val parts = line.trim().split("\\s+".toRegex())
        val valueStr = parts.lastOrNull() ?: return null
        val value = valueStr.toIntOrNull(16) ?: valueStr.toIntOrNull() ?: return null

        val buttonId = mapKey(keyName)
        if (buttonId == "UNKNOWN") return null

        val path = parts.firstOrNull { it.startsWith("/dev/input/") } ?: "/dev/input/event0"

        return ParsedKey(buttonId, value != 0, path)
    }

    private fun mapKey(name: String): String = when (name) {
        "BTN_SOUTH", "BTN_A" -> "A"
        "BTN_EAST", "BTN_B" -> "B"
        "BTN_NORTH", "BTN_X" -> "X"
        "BTN_WEST", "BTN_Y" -> "Y"
        "BTN_TL" -> "L1"; "BTN_TR" -> "R1"
        "BTN_TL2" -> "L2"; "BTN_TR2" -> "R2"
        "BTN_SELECT" -> "SELECT"; "BTN_START" -> "START"; "BTN_MODE" -> "MODE"
        "BTN_THUMBL" -> "L3"; "BTN_THUMBR" -> "R3"
        "DPAD_UP" -> "DPAD_UP"; "DPAD_DOWN" -> "DPAD_DOWN"
        "DPAD_LEFT" -> "DPAD_LEFT"; "DPAD_RIGHT" -> "DPAD_RIGHT"
        else -> "UNKNOWN"
    }

    fun keyEventToButton(event: KeyEvent): String {
        val kc = event.keyCode
        return when (kc) {
            KeyEvent.KEYCODE_DPAD_UP -> "DPAD_UP"
            KeyEvent.KEYCODE_DPAD_DOWN -> "DPAD_DOWN"
            KeyEvent.KEYCODE_DPAD_LEFT -> "DPAD_LEFT"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "DPAD_RIGHT"
            KeyEvent.KEYCODE_BUTTON_A -> "A"
            KeyEvent.KEYCODE_BUTTON_B -> "B"
            KeyEvent.KEYCODE_BUTTON_X -> "X"
            KeyEvent.KEYCODE_BUTTON_Y -> "Y"
            KeyEvent.KEYCODE_BUTTON_L1 -> "L1"
            KeyEvent.KEYCODE_BUTTON_L2 -> "L2"
            KeyEvent.KEYCODE_BUTTON_R1 -> "R1"
            KeyEvent.KEYCODE_BUTTON_R2 -> "R2"
            KeyEvent.KEYCODE_BUTTON_START -> "START"
            KeyEvent.KEYCODE_BUTTON_SELECT -> "SELECT"
            KeyEvent.KEYCODE_BUTTON_THUMBL -> "L3"
            KeyEvent.KEYCODE_BUTTON_THUMBR -> "R3"
            KeyEvent.KEYCODE_ENTER -> "ENTER"
            KeyEvent.KEYCODE_ESCAPE -> "ESC"
            else -> "UNKNOWN"
        }
    }

    fun bringK2erTaskToFront() {
        runCatching {
            val pkg = com.keymapper.app.AppContainer.ctx.packageName
            val intent = com.keymapper.app.AppContainer.ctx.packageManager.getLaunchIntentForPackage(pkg)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            com.keymapper.app.AppContainer.ctx.startActivity(intent)
        }
    }
}
