package mattecarra.accapp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.models.ProfileEnables
import org.apache.commons.collections4.queue.CircularFifoQueue

class AccConfigEditorViewModelFactory(val application: Application, val accaProfile: AccaProfile) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AccConfigEditorViewModel(application, accaProfile) as T
    }
}

class AccConfigEditorViewModel(application: Application, private val _profile: AccaProfile): AndroidViewModel(application)
{
    var unsavedChanges = false
    private val profileHistory = CircularFifoQueue<AccaProfile>(10)
    val undoOperationAvailableLiveData = MutableLiveData(false)

    var profile: AccaProfile
        get() = AccaProfile(
            _profile.uid,
            _profile.profileName, AccConfig(
            capacity,
            voltageLimit,
            currentMaxLimit,
            temperature,
            onBoot,
            onPlug,
            coolDown,
            resetBSOnUnplug,
            resetBSOnPause,
            chargeSwitch,
            isAutomaticSwitchEanbled,
            prioritizeBatteryIdleMode),
            enables)
        set(value) {
            addToHistory(profile)
            updateProfileLiveData(value)
            unsavedChanges = true
        }

    private val enablesLiveData = MutableLiveData(_profile.pEnables)
    var enables: ProfileEnables
        get() = enablesLiveData.value!!
        set(value) {
            if (enablesLiveData.value != value) {
                addToHistory(profile)
                enablesLiveData.value = value
                unsavedChanges = true
            }
        }

    private val capacityLiveData = MutableLiveData(_profile.accConfig.configCapacity)
    var capacity: AccConfig.ConfigCapacity
        get() = capacityLiveData.value!!
        set(value) {
            if (capacityLiveData.value != value) {
                addToHistory(profile)
                capacityLiveData.value = value
                unsavedChanges = true
            }
        }

    private val voltageLiveData = MutableLiveData(_profile.accConfig.configVoltage)
    var voltageLimit: AccConfig.ConfigVoltage
        get() = voltageLiveData.value!!
        set(value) {
            if (voltageLiveData.value != value) {
                addToHistory(profile)
                voltageLiveData.value = value
                unsavedChanges = true
            }
        }

    private val currentMaxLiveData = MutableLiveData(_profile.accConfig.configCurrMax)
    var currentMaxLimit: Int?
        get() = currentMaxLiveData.value
        set(value) {
            if (currentMaxLiveData.value != value) {
                addToHistory(profile)
                currentMaxLiveData.value = value
                unsavedChanges = true
            }
        }

    private val temperatureLiveData = MutableLiveData(_profile.accConfig.configTemperature)
    var temperature: AccConfig.ConfigTemperature
        get() = temperatureLiveData.value!!
        set(value) {
            if (temperatureLiveData.value != value) {
                addToHistory(profile)
                temperatureLiveData.value = value
                unsavedChanges = true
            }
        }

    private val onBootLiveData = MutableLiveData(_profile.accConfig.configOnBoot)
    var onBoot: String?
        get() = onBootLiveData.value
        set(value) {
            if (onBootLiveData.value != value) {
                addToHistory(profile)
                onBootLiveData.value = value
                unsavedChanges = true
            }
        }

    private val onPlugLiveData = MutableLiveData(_profile.accConfig.configOnPlug)
    var onPlug: String?
        get() = onPlugLiveData.value
        set(value) {
            if (onPlugLiveData.value != value) {
                addToHistory(profile)
                onPlugLiveData.value = value
                unsavedChanges = true
            }
        }

    private val configCoolDownLiveData = MutableLiveData(_profile.accConfig.configCoolDown)
    var coolDown: AccConfig.ConfigCoolDown?
        get() = configCoolDownLiveData.value
        set(value) {
            if (configCoolDownLiveData.value != value) {
                addToHistory(profile)
                configCoolDownLiveData.value = value
                unsavedChanges = true
            }
        }

    private val configChargeSwitchLiveData = MutableLiveData(_profile.accConfig.configChargeSwitch)
    var chargeSwitch: String?
        get() = configChargeSwitchLiveData.value
        set(value)
        {
            if (configChargeSwitchLiveData.value != value) {
                addToHistory(profile)
                configChargeSwitchLiveData.value = value
                unsavedChanges = true
            }
        }

    private val configIsAutomaticSwitchEnabled = MutableLiveData(_profile.accConfig.configIsAutomaticSwitchingEnabled)
    var isAutomaticSwitchEanbled: Boolean
        get() = configIsAutomaticSwitchEnabled.value!!
        set(value) {
            if (configIsAutomaticSwitchEnabled.value != value) {
                addToHistory(profile)
                configIsAutomaticSwitchEnabled.value = value
                unsavedChanges = true
            }
        }

    private val prioritizeBatteryIdleModeLiveData = MutableLiveData(_profile.accConfig.prioritizeBatteryIdleMode)
    var prioritizeBatteryIdleMode: Boolean
        get() = prioritizeBatteryIdleModeLiveData.value!!
        set(value) {
            if (prioritizeBatteryIdleModeLiveData.value != value) {
                addToHistory(profile)
                prioritizeBatteryIdleModeLiveData.value = value
                unsavedChanges = true
            }
        }

    private val resetBSOnPauseLiveData = MutableLiveData(_profile.accConfig.configResetBsOnPause)
    var resetBSOnPause: Boolean
        get() = resetBSOnPauseLiveData.value!!
        set(value) {
            if (resetBSOnPauseLiveData.value != value) {
                addToHistory(profile)
                resetBSOnPauseLiveData.value = value
                unsavedChanges = true
            }
        }

    private val resetBSOnUnplugLiveData = MutableLiveData(_profile.accConfig.configResetUnplugged)
    var resetBSOnUnplug: Boolean
        get() = resetBSOnUnplugLiveData.value!!
        set(value) {
            if (resetBSOnUnplugLiveData.value != value) {
                addToHistory(profile)
                resetBSOnUnplugLiveData.value = value
                unsavedChanges = true
            }
        }

    private fun updateProfileLiveData(value: AccaProfile)
    {
        updateEnablesLiveData(value.pEnables)
        updateAccConfigLiveData(value.accConfig)
    }

    private fun updateEnablesLiveData(value: ProfileEnables)
    {
        enablesLiveData.value = value
    }

    private fun updateAccConfigLiveData(value: AccConfig)
    {
        capacityLiveData.value = value.configCapacity
        voltageLiveData.value = value.configVoltage
        currentMaxLiveData.value = value.configCurrMax // ??
        temperatureLiveData.value = value.configTemperature
        onBootLiveData.value = value.configOnBoot
        onPlugLiveData.value = value.configOnPlug
        configCoolDownLiveData.value = value.configCoolDown
        configChargeSwitchLiveData.value = value.configChargeSwitch
    }

    ///----------------------------------------------------------------------------

    private fun addToHistory(conf: AccaProfile)
    {
        profileHistory.add(conf)
        undoOperationAvailableLiveData.value = true
    }

    fun undoLastConfigOperation()
    {
        profileHistory.poll()?.let { updateProfileLiveData(it) }
        undoOperationAvailableLiveData.value = profileHistory.isNotEmpty()
    }

    fun clearHistory()
    {
        profileHistory.clear()
        undoOperationAvailableLiveData.value = false
        unsavedChanges = false
    }

    ///----------------------------------------------------------------------------

    fun observeEnables(owner: LifecycleOwner, observer: Observer<ProfileEnables>)
    {
        enablesLiveData.observe(owner, observer)
    }

    fun observeCapacity(owner: LifecycleOwner, observer: Observer<AccConfig.ConfigCapacity>)
    {
        capacityLiveData.observe(owner, observer)
    }

    fun observeVoltageLimit(owner: LifecycleOwner, observer: Observer<AccConfig.ConfigVoltage>)
    {
        voltageLiveData.observe(owner, observer)
    }

    fun observeCurrentMax(owner: LifecycleOwner, observer: Observer<Int?>)
    {
        currentMaxLiveData.observe(owner, observer)
    }

    fun observeTemperature(owner: LifecycleOwner, observer: Observer<AccConfig.ConfigTemperature>)
    {
        temperatureLiveData.observe(owner, observer)
    }

    fun observeOnBoot(owner: LifecycleOwner, observer: Observer<String?>)
    {
        onBootLiveData.observe(owner, observer)
    }

    fun observeOnPlug(owner: LifecycleOwner, observer: Observer<String?>)
    {
        onPlugLiveData.observe(owner, observer)
    }

    fun observeCoolDown(owner: LifecycleOwner, observer: Observer<AccConfig.ConfigCoolDown?>)
    {
        configCoolDownLiveData.observe(owner, observer)
    }

    fun observeChargeSwitch(owner: LifecycleOwner, observer: Observer<String?>)
    {
        configChargeSwitchLiveData.observe(owner, observer)
    }

    fun observeIsAutomaticSwitchEnabled(owner: LifecycleOwner, observer: Observer<Boolean>)
    {
        configIsAutomaticSwitchEnabled.observe(owner, observer)
    }

    fun observePrioritizeBatteryIdleMode(owner: LifecycleOwner, observer: Observer<Boolean>)
    {
        prioritizeBatteryIdleModeLiveData.observe(owner, observer)
    }

    fun observeResetBSOnPause(owner: LifecycleOwner, observer: Observer<Boolean>)
    {
        resetBSOnPauseLiveData.observe(owner, observer)
    }

    fun observeResetBSOnUnplug(owner: LifecycleOwner, observer: Observer<Boolean>)
    {
        resetBSOnUnplugLiveData.observe(owner, observer)
    }
}