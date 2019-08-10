package mattecarra.accapp.activities

import android.app.Application
import androidx.lifecycle.*
import mattecarra.accapp.models.AccConfig
import org.apache.commons.collections4.queue.CircularFifoQueue

class AccConfigEditorViewModelFactory(val application: Application, val accConfig: AccConfig): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AccConfigEditorViewModel(application, accConfig) as T
    }
}

class AccConfigEditorViewModel(application: Application, private val mAccConfig: AccConfig): AndroidViewModel(application) {
    var unsavedChanges = false
        private set

    val configHistory = CircularFifoQueue<AccConfig>(10)

    val undoOperationAvailableLiveData = MutableLiveData(false)

    private val capacityLiveData = MutableLiveData(mAccConfig.configCapacity)
    var capacity: AccConfig.ConfigCapacity
        get() = capacityLiveData.value!!
        set(value) {
            addConfigToHistory(accConfig)
            capacityLiveData.value = value
            unsavedChanges = true
        }

    private val voltageLiveData = MutableLiveData(mAccConfig.configVoltage)
    var voltageLimit: AccConfig.ConfigVoltage
        get() = voltageLiveData.value!!
        set(value) {
            addConfigToHistory(accConfig)
            voltageLiveData.value = value
            unsavedChanges = true
        }

    private val temperatureLiveData = MutableLiveData(mAccConfig.configTemperature)
    var temperature: AccConfig.ConfigTemperature
        get() = temperatureLiveData.value!!
        set(value) {
            addConfigToHistory(accConfig)
            temperatureLiveData.value = value
            unsavedChanges = true
        }

    private val onBootLiveData = MutableLiveData(mAccConfig.configOnBoot)
    var onBoot: String?
        get() = onBootLiveData.value
        set(value) {
            addConfigToHistory(accConfig)
            onBootLiveData.value = value
            unsavedChanges = true
        }

    private val onPlugLiveData = MutableLiveData(mAccConfig.configOnPlug)
    var onPlug: String?
        get() = onPlugLiveData.value
        set(value) {
            addConfigToHistory(accConfig)
            onPlugLiveData.value = value
            unsavedChanges = true
        }

    private val configCoolDownLiveData = MutableLiveData(mAccConfig.configCoolDown)
    var coolDown: AccConfig.ConfigCoolDown?
        get() = configCoolDownLiveData.value
        set(value) {
            addConfigToHistory(accConfig)
            configCoolDownLiveData.value = value
            unsavedChanges = true
        }

/*    private val configResetUnpluggedLiveData = MutableLiveData(mAccConfig.configResetUnplugged)
    var resetUnplugged: Boolean
        get() = configResetUnpluggedLiveData.value!!
        set(value) {
            configResetUnpluggedLiveData.value = value
            addConfigToHistory(mAccConfig)
            unsavedChanges = true
        }*/

    private val configChargeSwitchLiveData = MutableLiveData(mAccConfig.configChargeSwitch)
    var chargeSwitch: String?
        get() = configChargeSwitchLiveData.value
        set(value) {
            addConfigToHistory(accConfig)
            configChargeSwitchLiveData.value = value
            unsavedChanges = true
        }

    var accConfig: AccConfig
        get() = AccConfig(capacity, voltageLimit, temperature, onBoot, onPlug, coolDown, mAccConfig.configResetUnplugged /* resetUnplugged */, chargeSwitch)
        set(value) {
            addConfigToHistory(value)
            updateAccConfigLiveData(value)
            unsavedChanges = true
        }


    private fun updateAccConfigLiveData(value: AccConfig) {
        capacityLiveData.value = value.configCapacity
        voltageLiveData.value = value.configVoltage
        temperatureLiveData.value = value.configTemperature
        onBootLiveData.value = value.configOnBoot
        onPlugLiveData.value = value.configOnPlug
        configCoolDownLiveData.value = value.configCoolDown
        //configResetUnpluggedLiveData.value = value.configResetUnplugged
        configChargeSwitchLiveData.value = value.configChargeSwitch
    }

    private fun addConfigToHistory(conf: AccConfig) {
        configHistory.add(conf)
        undoOperationAvailableLiveData.value = true
    }

    fun undoLastConfigOperation() {
        configHistory.poll()?.let {
            updateAccConfigLiveData(it)
        }
        undoOperationAvailableLiveData.value = configHistory.isNotEmpty()
    }

    fun observeCapacity(owner: LifecycleOwner, observer: Observer<AccConfig.ConfigCapacity>) {
        capacityLiveData.observe(owner, observer)
    }

    fun observeVoltageLimit(owner: LifecycleOwner, observer: Observer<AccConfig.ConfigVoltage>) {
        voltageLiveData.observe(owner, observer)
    }

    fun observeTemperature(owner: LifecycleOwner, observer: Observer<AccConfig.ConfigTemperature>) {
        temperatureLiveData.observe(owner, observer)
    }

    fun observeOnBoot(owner: LifecycleOwner, observer: Observer<String?>) {
        onBootLiveData.observe(owner, observer)
    }

    fun observeOnPlug(owner: LifecycleOwner, observer: Observer<String?>) {
        onPlugLiveData.observe(owner, observer)
    }

    fun observeCoolDown(owner: LifecycleOwner, observer: Observer<AccConfig.ConfigCoolDown?>) {
        configCoolDownLiveData.observe(owner, observer)
    }

/*    fun observeResetUnplugged(owner: LifecycleOwner, observer: Observer<Boolean>) {
        configResetUnpluggedLiveData.observe(owner, observer)
    }*/

    fun observeChargeSwitch(owner: LifecycleOwner, observer: Observer<String?>) {
        configChargeSwitchLiveData.observe(owner, observer)
    }
}