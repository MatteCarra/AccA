package mattecarra.accapp.fragments

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.utils.AccUtils
import mattecarra.accapp.utils.ConfigUtils

class AccConfigViewModel : ViewModel() {
    private val config: MutableLiveData<AccConfig> = MutableLiveData()

    init {
        try {
            this.config.value = AccUtils.readConfig()
        } catch (ex: Exception) {
            ex.printStackTrace()
            //TODO showConfigReadError()
            this.config.value = AccUtils.defaultConfig //if mAccConfig is null I use default mAccConfig values.
        }
    }

    fun observe(owner: LifecycleOwner, observer: Observer<AccConfig>) {
        config.observe(owner, observer)
    }

    fun <T> getValue(callback: (AccConfig) -> T): T {
        synchronized(config) {
            return callback(config.value!!)
        }
    }

    fun updateValue(callback: (AccConfig) -> Unit): ConfigUtils.UpdateResult {
        synchronized(config) {
            val value = config.value!!

            callback(value)

            this.config.postValue(value)
            return ConfigUtils.updateAcc(value) //TODO do async
        }
    }

    fun updateConfig(config: AccConfig): ConfigUtils.UpdateResult {
        synchronized(config) {
            this.config.postValue(config)
            return ConfigUtils.updateAcc(config) //TODO do async
        }
    }
}