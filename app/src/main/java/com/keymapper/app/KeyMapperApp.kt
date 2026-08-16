package com.keymapper.app

import android.app.Application
import android.util.Log
import com.keymapper.app.bluetooth.BluetoothHidController
import com.keymapper.app.mapping.MappingEngine
import com.keymapper.app.mapping.MappingRepository

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
            mappingEngine = MappingEngine(mappingRepository)
            Log.i(TAG, "KeyMapperApp constructed OK (coroutines deferred to Activity)")
        } catch (e: Throwable) {
            Log.e(TAG, "KeyMapperApp construction FAILED", e)
        }
    }

    companion object {
        private const val TAG = "KeyMapperApp"
        lateinit var instance: KeyMapperApp
            private set
    }
}
