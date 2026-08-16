package com.keymapper.app

import android.app.Application
import android.util.Log
import com.keymapper.app.bluetooth.BluetoothHidController
import com.keymapper.app.mapping.MappingEngine
import com.keymapper.app.mapping.MappingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class KeyMapperApp : Application() {

    lateinit var bluetoothController: BluetoothHidController
        private set
    lateinit var mappingRepository: MappingRepository
        private set
    lateinit var mappingEngine: MappingEngine
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
        }

        try {
            bluetoothController = BluetoothHidController(this)
            mappingRepository = MappingRepository(this)
            mappingEngine = MappingEngine(this, mappingRepository)

            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            scope.launch {
                bluetoothController.buttonEvents.collect { event ->
                    try {
                        mappingEngine.onButtonEvent(event)
                    } catch (e: Exception) {
                        Log.e(TAG, "button event dispatch failed", e)
                    }
                }
            }
            Log.i(TAG, "KeyMapperApp initialized OK")
        } catch (e: Throwable) {
            Log.e(TAG, "KeyMapperApp init FAILED", e)
        }
    }

    companion object {
        private const val TAG = "KeyMapperApp"
        lateinit var instance: KeyMapperApp
            private set
    }
}
