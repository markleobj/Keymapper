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

    suspend fun listProfiles(): List<String> {
        val json = context.profileDataStore.data.map { it[ALL_PROFILES_KEY] ?: "" }.first()
        if (json.isBlank()) return listOf("默认")
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, type) ?: listOf("默认")
        } catch (_: Exception) {
            listOf("默认")
        }
    }

    suspend fun addProfile(name: String) {
        val list = listProfiles().toMutableList()
        if (name !in list) {
            list.add(name)
            context.profileDataStore.edit { it[ALL_PROFILES_KEY] = gson.toJson(list) }
        }
    }

    suspend fun deleteProfile(name: String) {
        if (name == currentProfile()) return
        val list = listProfiles().filter { it != name }.toMutableList()
        if (list.isEmpty()) list.add("默认")
        context.profileDataStore.edit { it[ALL_PROFILES_KEY] = gson.toJson(list) }
        context.profileDataStore.edit { it.remove(profileKey(name)) }
    }

    suspend fun renameProfile(old: String, new: String) {
        val list = listProfiles().map { if (it == old) new else it }
        context.profileDataStore.edit { it[ALL_PROFILES_KEY] = gson.toJson(list) }
        val oldKey = profileKey(old)
        val newKey = profileKey(new)
        val oldVal = context.profileDataStore.data.map { it[oldKey] ?: "" }.first()
        context.profileDataStore.edit {
            if (oldVal.isNotBlank()) {
                it[newKey] = oldVal
                it.remove(oldKey)
            }
        }
        if (currentProfile() == old) switchProfile(new)
    }

    suspend fun currentProfile(): String {
        return context.profileDataStore.data.map { it[CURRENT_PROFILE_KEY] ?: "默认" }.first()
    }

    suspend fun switchProfile(name: String) {
        context.profileDataStore.edit { it[CURRENT_PROFILE_KEY] = name }
    }

    private fun profileKey(name: String) = stringPreferencesKey("profile_$name")

    private val ALL_PROFILES_KEY = stringPreferencesKey("all_profiles")
    private val CURRENT_PROFILE_KEY = stringPreferencesKey("current_profile")

    val profilesFlow: Flow<List<String>> = context.profileDataStore.data.map { prefs ->
        val json = prefs[ALL_PROFILES_KEY] ?: ""
        if (json.isBlank()) return@map listOf("默认")
        try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, type) ?: listOf("默认")
        } catch (_: Exception) {
            listOf("默认")
        }
    }

    val currentProfileFlow: Flow<String> = context.profileDataStore.data.map { prefs ->
        prefs[CURRENT_PROFILE_KEY] ?: "默认"
    }

    fun listProfilesFlow() = profilesFlow

    val mappings: Flow<List<MappingConfig>> = context.profileDataStore.data.map { prefs ->
        val current = prefs[CURRENT_PROFILE_KEY] ?: "默认"
        val json = prefs[profileKey(current)] ?: return@map emptyList()
        parseMappings(json)
    }

    private fun parseMappings(json: String): List<MappingConfig> {
        if (json.isBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<MappingConfig>>() {}.type
            gson.fromJson<List<MappingConfig>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveAll(list: List<MappingConfig>) {
        val current = currentProfile()
        context.profileDataStore.edit { prefs ->
            prefs[profileKey(current)] = gson.toJson(list)
        }
    }

    suspend fun add(config: MappingConfig) {
        val current = getCurrent()
        saveAll(current.filter { it.id != config.id } + config)
    }

    suspend fun remove(id: String) {
        val current = getCurrent()
        saveAll(current.filter { it.id != id })
    }

    suspend fun update(config: MappingConfig) {
        add(config)
    }

    suspend fun getCurrent(): List<MappingConfig> {
        val current = currentProfile()
        val json = context.profileDataStore.data.map { it[profileKey(current)] ?: "" }.first()
        return parseMappings(json)
    }
}
