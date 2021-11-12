package mattecarra.accapp.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mattecarra.accapp.database.AccaRoomDatabase
import mattecarra.accapp.database.ProfileDao
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.utils.LogExt

class ProfilesViewModel(application: Application) : AndroidViewModel(application) {

    private val mProfilesListLiveData: LiveData<List<AccaProfile>>
    private val mProfileDao: ProfileDao
    private val TAG = "ProfilesViewModelDAO"

    init
    {
        val accaDatabase = AccaRoomDatabase.getDatabase(application)
        mProfileDao = accaDatabase.profileDao()
        mProfilesListLiveData = mProfileDao.getAllProfiles()
    }

    suspend fun getProfiles(): List<AccaProfile>
    {
        mProfileDao.getProfiles().also {
            LogExt().d(TAG, "getProfiles()="+ if (it.isNullOrEmpty()) "Empty!" else it.count())
            return it
        }
    }

    suspend fun getProfileById(id: Int): AccaProfile?
    {
        mProfileDao.getProfileById(id).also {
            LogExt().d(TAG, "getProfileById($id)="+(it?.profileName ?: "null"))
            return it
        }
    }

    fun getLiveData(): LiveData<List<AccaProfile>>
    {
        return mProfilesListLiveData
    }

    fun insertProfile(profile: AccaProfile) = viewModelScope.launch {
        LogExt().d(TAG,"insertProfile(): ${profile.profileName}")
        mProfileDao.insert(profile)
    }

    fun deleteProfile(profile: AccaProfile) = viewModelScope.launch {
        LogExt().d(TAG,"deleteProfile(): ${profile.profileName}")
        mProfileDao.delete(profile)
    }

    fun updateProfile(profile: AccaProfile) = viewModelScope.launch {
        LogExt().d(TAG,"updateProfile(): ${profile.profileName}")
        mProfileDao.update(profile)
    }
}
