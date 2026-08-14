package com.keymapper.app.mapping

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.keymapper.app.model.MappingConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.mappingDataStore by preferencesDataStore(name = "keymapper_mappings")

class MappingRepository(private val context: Context) {

    private val gson = Gson()
    private val MAPPINGS_KEY = stringPreferencesKey("mappings_json")

    val mappings: Flow<List<MappingConfig>> = context.mappingDataStore.data.map { prefs ->
        val json = prefs[MAPPINGS_KEY] ?: return@map emptyList()
        try {
            val type = object : TypeToken<List<MappingConfig>>() {}.type
            gson.fromJson<List<MappingConfig>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveAll(list: List<MappingConfig>) {
        context.mappingDataStore.edit { prefs ->
            prefs[MAPPINGS_KEY] = gson.toJson(list)
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

    private suspend fun getCurrent(): List<MappingConfig> {
        val json = context.mappingDataStore.data.map { it[MAPPINGS_KEY] ?: "" }
            .let { flow ->
                var result = ""
                flow.collect { result = it }
                result
            }
        if (json.isBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<MappingConfig>>() {}.type
            gson.fromJson<List<MappingConfig>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
