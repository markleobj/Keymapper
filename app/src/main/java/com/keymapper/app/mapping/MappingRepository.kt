package com.keymapper.app.mapping

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.keymapper.app.model.AppConfig
import com.keymapper.app.model.Mapping
import com.keymapper.app.model.Scene
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class MappingRepository(private val context: Context) {

    companion object {
        private const val TAG = "MappingRepo-K2ER"
        private const val FILE_NAME = "k2er_profiles.json"

        @Volatile
        private var instance: MappingRepository? = null

        fun getInstance(context: Context): MappingRepository {
            return instance ?: synchronized(this) {
                instance ?: MappingRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val dataFile: File by lazy { File(context.filesDir, FILE_NAME) }

    private val _apps = MutableStateFlow<List<AppConfig>>(emptyList())
    val apps: StateFlow<List<AppConfig>> = _apps.asStateFlow()

    init {
        load()
    }

    private fun load() {
        try {
            if (!dataFile.exists()) {
                _apps.value = emptyList()
                return
            }
            val json = dataFile.readText()
            val type = object : TypeToken<List<AppConfig>>() {}.type
            _apps.value = gson.fromJson(json, type) ?: emptyList()
            Log.i(TAG, "✅ 加载 ${_apps.value.size} 个 APP 配置")
        } catch (e: Throwable) {
            Log.e(TAG, "加载配置失败", e)
            _apps.value = emptyList()
        }
    }

    private fun save() {
        try {
            val json = gson.toJson(_apps.value)
            dataFile.writeText(json)
            Log.i(TAG, "💾 保存 ${_apps.value.size} 个 APP 配置 → ${dataFile.absolutePath}")
        } catch (e: Throwable) {
            Log.e(TAG, "保存配置失败", e)
        }
    }

    fun upsertApp(app: AppConfig) {
        val list = _apps.value.toMutableList()
        val idx = list.indexOfFirst { it.packageName == app.packageName }
        if (idx >= 0) list[idx] = app else list.add(app)
        _apps.value = list
        save()
    }

    fun getApp(packageName: String): AppConfig? {
        return _apps.value.firstOrNull { it.packageName == packageName }
    }

    fun getOrCreateApp(packageName: String, appName: String = ""): AppConfig {
        return getApp(packageName) ?: run {
            val newApp = AppConfig(
                packageName = packageName,
                appName = appName.ifBlank { packageName.substringAfterLast('.') },
                scenes = listOf(Scene(name = "全局")),
                activeSceneId = null,
                enabled = true
            )
            upsertApp(newApp)
            newApp
        }
    }

    fun deleteApp(packageName: String) {
        _apps.value = _apps.value.filter { it.packageName != packageName }
        save()
    }

    fun toggleAppEnabled(packageName: String) {
        val app = getApp(packageName) ?: return
        upsertApp(app.copy(enabled = !app.enabled))
    }

    fun toggleSceneEnabled(packageName: String, sceneId: String) {
        val app = getApp(packageName) ?: return
        val scenes = app.scenes.map { s ->
            if (s.id == sceneId) s.copy(enabled = !s.enabled) else s
        }
        upsertApp(app.copy(scenes = scenes))
    }

    fun addScene(packageName: String, sceneName: String): Scene? {
        val app = getApp(packageName) ?: return null
        val scene = Scene(name = sceneName)
        upsertApp(app.copy(scenes = app.scenes + scene))
        return scene
    }

    fun deleteScene(packageName: String, sceneId: String) {
        val app = getApp(packageName) ?: return
        if (app.scenes.size <= 1) return
        upsertApp(app.copy(scenes = app.scenes.filter { it.id != sceneId }))
    }

    fun setActiveScene(packageName: String, sceneId: String?) {
        val app = getApp(packageName) ?: return
        upsertApp(app.copy(activeSceneId = sceneId))
    }

    fun addMapping(packageName: String, sceneId: String, mapping: Mapping) {
        val app = getApp(packageName) ?: return
        val scenes = app.scenes.map { s ->
            if (s.id == sceneId) s.copy(mappings = s.mappings + mapping) else s
        }
        upsertApp(app.copy(scenes = scenes))
    }

    fun updateMapping(packageName: String, sceneId: String, mapping: Mapping) {
        val app = getApp(packageName) ?: return
        val scenes = app.scenes.map { s ->
            if (s.id == sceneId) {
                s.copy(mappings = s.mappings.map { if (it.id == mapping.id) mapping else it })
            } else s
        }
        upsertApp(app.copy(scenes = scenes))
    }

    fun deleteMapping(packageName: String, sceneId: String, mappingId: String) {
        val app = getApp(packageName) ?: return
        val scenes = app.scenes.map { s ->
            if (s.id == sceneId) {
                s.copy(mappings = s.mappings.filter { it.id != mappingId })
            } else s
        }
        upsertApp(app.copy(scenes = scenes))
    }

    fun toggleMappingEnabled(packageName: String, sceneId: String, mappingId: String) {
        val app = getApp(packageName) ?: return
        val scenes = app.scenes.map { s ->
            if (s.id == sceneId) {
                s.copy(mappings = s.mappings.map { m ->
                    if (m.id == mappingId) m.copy(enabled = !m.enabled) else m
                })
            } else s
        }
        upsertApp(app.copy(scenes = scenes))
    }

    fun getActiveMappingsForApp(packageName: String?): List<Mapping> {
        val pkg = packageName ?: return emptyList()
        val app = getApp(pkg) ?: return emptyList()
        if (!app.enabled) return emptyList()

        val targetScene = app.activeSceneId?.let { sid -> app.scenes.firstOrNull { it.id == sid && it.enabled } }
            ?: app.scenes.firstOrNull { it.enabled }
            ?: app.scenes.firstOrNull()

        return targetScene?.mappings?.filter { it.enabled } ?: emptyList()
    }

    fun exportJson(): String = gson.toJson(_apps.value)
    fun importJson(json: String): Boolean {
        return try {
            val type = object : TypeToken<List<AppConfig>>() {}.type
            val list = gson.fromJson<List<AppConfig>>(json, type)
            _apps.value = list
            save()
            true
        } catch (e: Throwable) {
            Log.e(TAG, "import 失败", e)
            false
        }
    }
}
