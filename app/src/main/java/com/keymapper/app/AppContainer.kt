package com.keymapper.app

import android.content.Context
import android.util.Log
import com.keymapper.app.bluetooth.BluetoothHidController
import com.keymapper.app.mapping.MappingEngine
import com.keymapper.app.mapping.MappingRepository

/**
 * DI container - NOT an Application subclass.
 * Initialized lazily from Activity in a background dispatcher.
 * Application.onCreate stays zero-code so we never ANR.
 */
class AppContainer private constructor(val context: Context) {

    val bluetoothController: BluetoothHidController
    val mappingRepository: MappingRepository
    val mappingEngine: MappingEngine

    init {
        bluetoothController = BluetoothHidController(context.applicationContext)
        mappingRepository = MappingRepository(context.applicationContext)
        mappingEngine = MappingEngine(mappingRepository)
        instance = this
        Log.i(TAG, "AppContainer initialized")
    }

    companion object {
        private const val TAG = "AppContainer"

        @Volatile
        private var instance: AppContainer? = null

        fun getOrCreate(context: Context): AppContainer {
            instance?.let { return it }
            synchronized(this) {
                instance?.let { return it }
                return AppContainer(context)
            }
        }

        fun isReady(): Boolean = instance != null

        fun require(): AppContainer = instance
            ?: error("AppContainer not initialized yet - call getOrCreate from background thread first")
    }
}
