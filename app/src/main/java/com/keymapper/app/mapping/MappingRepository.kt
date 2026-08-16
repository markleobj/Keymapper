package com.keymapper.app.mapping

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.keymapper.app.model.MappingConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.mappingDataStore by preferencesDataStore(name = "keymapper_mappings")
private val Context.profileDataStore by preferencesDataStore(name = "keymapper_profiles")

class MappingRepository(private val context: Context) {

    private val gson = Gson()

    companion object {
        const val GLOBAL_PKG = "__GLOBAL__"
        const val DEFAULT_PROFILE = "默认"
    }

    private fun allProfilesKey(pkg: String) = stringPreferencesKey("all_profiles_of_$pkg")
    private fun currentProfileKey(pkg: String) = stringPreferencesKey("current_profile_of_$pkg")
    private fun profileMappingsKey(pkg: String, profile: String) =
        stringPreferencesKey("profile_${pkg}_$profile")

    private suspend fun readJson(key: androidx.datastore.preferences.core.Preferences.Key<String>): String =
        context.profileDataStore.data.map { it[key] ?: "" }.first()

    private suspend fun writeJson(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: String) {
        context.profileDataStore.edit { it[key] = value }
    }

    // ---- 方案列表 ----
    suspend fun listProfilesFor(pkg: String): List<String> {
        val json = readJson(allProfilesKey(pkg))
        if (json.isBlank()) return listOf(DEFAULT_PROFILE)
        return runCatching {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, type) ?: listOf(DEFAULT_PROFILE)
        }.getOrDefault(listOf(DEFAULT_PROFILE))
    }

    suspend fun currentProfileFor(pkg: String): String {
        val saved = readJson(currentProfileKey(pkg))
        if (saved.isNotBlank()) return saved
        val profiles = listProfilesFor(pkg)
        return profiles.firstOrNull() ?: DEFAULT_PROFILE
    }

    suspend fun switchProfileFor(pkg: String, profile: String) {
        writeJson(currentProfileKey(pkg), profile)
    }

    suspend fun addProfileFor(pkg: String, name: String) {
        val list = listProfilesFor(pkg).toMutableList()
        if (name !in list) {
            list.add(name)
            writeJson(allProfilesKey(pkg), gson.toJson(list))
        }
        switchProfileFor(pkg, name)
    }

    suspend fun deleteProfileFor(pkg: String, name: String) {
        val list = listProfilesFor(pkg).filter { it != name }.toMutableList()
        if (list.isEmpty()) list.add(DEFAULT_PROFILE)
        writeJson(allProfilesKey(pkg), gson.toJson(list))
        context.profileDataStore.edit { it.remove(profileMappingsKey(pkg, name)) }
        if (currentProfileFor(pkg) == name) {
            switchProfileFor(pkg, list.first())
        }
    }

    suspend fun renameProfileFor(pkg: String, old: String, new: String) {
        val list = listProfilesFor(pkg).map { if (it == old) new else it }
        writeJson(allProfilesKey(pkg), gson.toJson(list))
        val oldVal = readJson(profileMappingsKey(pkg, old))
        if (oldVal.isNotBlank()) {
            writeJson(profileMappingsKey(pkg, new), oldVal)
            context.profileDataStore.edit { it.remove(profileMappingsKey(pkg, old)) }
        }
        if (currentProfileFor(pkg) == old) switchProfileFor(pkg, new)
    }

    // ---- 映射读写 ----
    private fun parseMappings(json: String): List<MappingConfig> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<MappingConfig>>() {}.type
            gson.fromJson<List<MappingConfig>>(json, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    suspend fun getMappingsFor(pkg: String, profile: String): List<MappingConfig> {
        val json = readJson(profileMappingsKey(pkg, profile))
        return parseMappings(json)
    }

    suspend fun getCurrentMappingsFor(pkg: String): List<MappingConfig> {
        val profile = currentProfileFor(pkg)
        return getMappingsFor(pkg, profile)
    }

    suspend fun saveMappingsFor(pkg: String, profile: String, list: List<MappingConfig>) {
        writeJson(profileMappingsKey(pkg, profile), gson.toJson(list))
    }

    suspend fun saveCurrentMappingsFor(pkg: String, list: List<MappingConfig>) {
        val profile = currentProfileFor(pkg)
        saveMappingsFor(pkg, profile, list)
    }

    suspend fun addMappingFor(pkg: String, cfg: MappingConfig) {
        val list = getCurrentMappingsFor(pkg).filter { it.id != cfg.id } + cfg
        saveCurrentMappingsFor(pkg, list)
    }

    suspend fun removeMappingFor(pkg: String, id: String) {
        val list = getCurrentMappingsFor(pkg).filter { it.id != id }
        saveCurrentMappingsFor(pkg, list)
    }

    suspend fun updateMappingFor(pkg: String, cfg: MappingConfig) {
        addMappingFor(pkg, cfg)
    }

    // ---- 汇总：某 APP 当前激活方案的所有映射 + 全局映射 ----
    suspend fun getActiveMappingsForApp(currentPkg: String?): List<MappingConfig> {
        val result = mutableListOf<MappingConfig>()
        val pkg = currentPkg
        if (!pkg.isNullOrBlank()) {
            result.addAll(getCurrentMappingsFor(pkg))
        }
        result.addAll(getCurrentMappingsFor(GLOBAL_PKG))
        return result
    }

    // ---- 扫描所有已配置的 APP ----
    suspend fun listConfiguredApps(): List<String> {
        // 通过枚举 DataStore 所有 key 来提取
        val all = context.profileDataStore.data.first().asMap().keys
        val pkgSet = mutableSetOf<String>()
        val prefix = "all_profiles_of_"
        for (k in all) {
            val name = k.name
            if (name.startsWith(prefix)) {
                val pkg = name.removePrefix(prefix)
                if (pkg.isNotBlank()) pkgSet.add(pkg)
            }
            if (name.startsWith("profile_")) {
                val rest = name.removePrefix("profile_")
                val parts = rest.split("_", limit = 2)
                if (parts.size == 2) pkgSet.add(parts[0])
            }
        }
        return pkgSet.toList()
    }

    // ---- 兼容旧全局 Profile（迁移用） ----
    suspend fun migrateIfNeeded(): Boolean {
        val legacyCurrent = readJson(stringPreferencesKey("current_profile"))
        if (legacyCurrent.isBlank()) return false
        val legacyListJson = readJson(stringPreferencesKey("all_profiles"))
        if (legacyListJson.isBlank()) return false
        val legacyList: List<String> = runCatching {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(legacyListJson, type) ?: emptyList()
        }.getOrDefault(emptyList())
        if (legacyList.isEmpty()) return false

        // 把旧的全局 "默认" profile 的映射迁到 GLOBAL_PKG
        val legacyDefaultJson = readJson(stringPreferencesKey("profile_${legacyList.first()}"))
        if (legacyDefaultJson.isNotBlank()) {
            writeJson(profileMappingsKey(GLOBAL_PKG, DEFAULT_PROFILE), legacyDefaultJson)
            writeJson(allProfilesKey(GLOBAL_PKG), gson.toJson(listOf(DEFAULT_PROFILE)))
            writeJson(currentProfileKey(GLOBAL_PKG), DEFAULT_PROFILE)
        }
        // 清空旧 key
        context.profileDataStore.edit { prefs ->
            legacyList.forEach { prefs.remove(stringPreferencesKey("profile_$it")) }
            prefs.remove(stringPreferencesKey("all_profiles"))
            prefs.remove(stringPreferencesKey("current_profile"))
        }
        return true
    }

    // ---- Flow：某 APP 当前方案映射 ----
    fun currentMappingsFlowFor(pkg: String): Flow<List<MappingConfig>> {
        val profileKey = currentProfileKey(pkg)
        val mappingsKey = androidx.datastore.preferences.core.stringPreferencesKey("PLACEHOLDER")
        return context.profileDataStore.data.map { prefs ->
            val profile = prefs[profileKey] ?: DEFAULT_PROFILE
            val json = prefs[profileMappingsKey(pkg, profile)] ?: ""
            parseMappings(json)
        }
    }

    fun currentMappingsFlowForGlobal(): Flow<List<MappingConfig>> =
        currentMappingsFlowFor(GLOBAL_PKG)

    // ---- 兼容旧 API（MainActivity 等还在用） ----
    private val allAppsWithProfiles = mutableSetOf<String>()

    val mappings: Flow<List<MappingConfig>> = context.profileDataStore.data.map { prefs ->
        val result = mutableListOf<MappingConfig>()
        // 收集所有 GLOBAL 映射 + 已配置 APP 当前方案的映射
        val pkgSet = mutableSetOf<String>()
        prefs.asMap().keys.forEach { k ->
            if (k.name.startsWith("current_profile_of_")) {
                val pkg = k.name.removePrefix("current_profile_of_")
                if (pkg.isNotBlank()) pkgSet.add(pkg)
            }
        }
        for (pkg in pkgSet) {
            val profile = prefs[currentProfileKey(pkg)] ?: DEFAULT_PROFILE
            val json = prefs[profileMappingsKey(pkg, profile)] ?: continue
            result.addAll(parseMappings(json))
        }
        result
    }

    suspend fun getCurrent(): List<MappingConfig> {
        val all = context.profileDataStore.data.first().asMap().keys
        val pkgSet = mutableSetOf<String>()
        for (k in all) {
            if (k.name.startsWith("current_profile_of_")) {
                val pkg = k.name.removePrefix("current_profile_of_")
                if (pkg.isNotBlank()) pkgSet.add(pkg)
            }
        }
        val result = mutableListOf<MappingConfig>()
        for (pkg in pkgSet) {
            val profile = currentProfileFor(pkg)
            result.addAll(getMappingsFor(pkg, profile))
        }
        return result
    }
}
