package mattecarra.accapp.activities

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.utils.Constants
import mattecarra.accapp.utils.DataRepository
import kotlin.coroutines.CoroutineContext

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val mDataRepository: DataRepository
    private val mSharedPrefs: SharedPreferences


    private var mParentJob = Job()
    private val mCoroutineContext: CoroutineContext
        get() = mParentJob + Dispatchers.Main

    private val mScope = CoroutineScope(mCoroutineContext)

    init {
        mDataRepository = DataRepository(application, mScope)
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(application)
    }

    fun insertProfile(profile: AccaProfile) = mScope.launch(Dispatchers.IO) {
        mDataRepository.insertProfile(profile)
    }

    fun deleteProfile(profile: AccaProfile) = mScope.launch(Dispatchers.IO) {
        mDataRepository.deleteProfile(profile)
    }

    fun updateProfile(profile: AccaProfile) = mScope.launch(Dispatchers.IO) {
        mDataRepository.updateProfile(profile)
    }

    fun getProfileById(id: Int) : AccaProfile {
        return mDataRepository.getProfileById(id)
    }

    /**
     * Clears the currently selected profile ID from Shared Preferences.
     */
    fun clearCurrentSelectedProfile() {
        mSharedPrefs.edit().putInt(Constants.PROFILE_KEY, -1).apply()
    }

    /**
     * Sets the profile ID to the profile key in the app's shared preferences.
     * @param profileId ID of the profile selected.
     */
    fun setCurrentSelectedProfile(profileId: Int) {
        mSharedPrefs.edit().putInt(Constants.PROFILE_KEY, profileId).apply()
    }
}