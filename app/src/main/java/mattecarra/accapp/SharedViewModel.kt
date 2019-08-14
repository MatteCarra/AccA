package mattecarra.accapp

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.utils.Constants
import mattecarra.accapp.utils.DataRepository
import mattecarra.accapp.utils.ProfileUtils
import kotlin.coroutines.CoroutineContext

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val mSharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    private val config: MutableLiveData<AccConfig> = MutableLiveData()

    init {
        viewModelScope.launch {
            try {
                config.value = Acc.instance.readConfig()
            } catch (ex: Exception) {
                ex.printStackTrace()
                //TODO: showConfigReadError()
                config.value = Acc.instance.defaultConfig //if mAccConfig is null I use default mAccConfig values.
            }
        }
    }

    /**
     * Sets an observer for config.
     */
    fun observeConfig(owner: LifecycleOwner, observer: Observer<AccConfig>) {
        config.observe(owner, observer)
    }

    /*
    * This method is designed to get a parameter from AccConfig or sAccConfig itself
    * Example:
    * val parameter = getAccConfigValue { it.oneParameter }
    * */
    fun <T> getAccConfigValue(callback: (AccConfig) -> T): T {
        return callback(config.value!!)
    }

    /*
    * This method is designed to set a parameter of AccConfig and write on file
    * Example:
    * updateAccConfigValue { config ->
    *   config.oneParameter = 1
    * }
    * */
    fun updateAccConfigValue(callback: (AccConfig) -> Unit) {
        val value = config.value!!

        callback(value)

        this.config.postValue(value)
        saveAccConfig(value)
    }

    /*
    * Updates the AccConfig and write on file
    * */
    fun updateAccConfig(value: AccConfig) {
        config.postValue(value)
        saveAccConfig(value)
    }

    /*
    * Saves config on file. It's run in an async thread every time config is updated.
    * TODO use some sort of mutex
    * */
    //private val saveConfigLock = Object()
    private fun saveAccConfig(value: AccConfig) = viewModelScope.launch {
        val res = Acc.instance.updateAccConfig(value)
        if(!res.isSuccessful()) {
            res.debug()
            //TODO show a toast that tells users there was an error
            /*if (!result.voltControlUpdateSuccessful) {
                Toast.makeText(this@MainActivity, R.string.wrong_volt_file, Toast.LENGTH_LONG).show()
            }*/

            val currentConfigVal = try {
                Acc.instance.readConfig()
            } catch (ex: Exception) {
                ex.printStackTrace()
                Acc.instance.defaultConfig
            }

            config.postValue(currentConfigVal)
        }
    }

    /**
     * Clears the currently selected profile ID from Shared Preferences.
     */
    fun clearCurrentSelectedProfile() {
        ProfileUtils.clearCurrentSelectedProfile(mSharedPrefs)
    }

    /**
     * Sets the profile ID to the profile key in the app's shared preferences.
     * @param profileId ID of the profile selected.
     */
    fun setCurrentSelectedProfile(profileId: Int) {
        ProfileUtils.saveCurrentProfile(profileId, mSharedPrefs)
    }
}