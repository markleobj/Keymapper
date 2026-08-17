package com.keymapper.app

import android.content.Context
import android.util.Log
import com.keymapper.app.mapping.MappingEngine
import com.keymapper.app.mapping.MappingRepository

class AppContainer private constructor(val context: Context) {

    val mappingRepository: MappingRepository
    val mappingEngine: MappingEngine

    init {
        mappingRepository = MappingRepository.getInstance(context)
        mappingEngine = MappingEngine(mappingRepository)
        mappingEngine.enabled = true
        instance = this
        Log.i(TAG, "✅ AppContainer initialized")
    }

    companion object {
        private const val TAG = "AppContainer"

        @Volatile
        private var instance: AppContainer? = null

        fun getOrCreate(context: Context): AppContainer {
            instance?.let { return it }
            synchronized(this) {
                instance?.let { return it }
                return AppContainer(context.applicationContext)
            }
        }

        fun require(): AppContainer = instance
            ?: error("AppContainer not ready")

        val ctx: Context get() = require().context
    }
}
