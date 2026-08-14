package com.keymapper.app

import android.app.Application
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
        bluetoothController = BluetoothHidController(this)
        mappingRepository = MappingRepository(this)
        mappingEngine = MappingEngine(this, mappingRepository)

        // Wire up: button events -> mapping engine
        val scope = kotlinx.coroutines.CoroutineScope(
            kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main.immediate
        )
        scope.launch {
            bluetoothController.buttonEvents.collect { event ->
                mappingEngine.onButtonEvent(event)
            }
        }
    }

    companion object {
        lateinit var instance: KeyMapperApp
            private set
    }
}
