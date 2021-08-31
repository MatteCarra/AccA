package mattecarra.accapp.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mattecarra.accapp.database.AccaRoomDatabase
import mattecarra.accapp.database.ProfileDao
import mattecarra.accapp.models.AccaProfile

class ProfilesViewModel(application: Application) : AndroidViewModel(application) {

    private val mProfilesListLiveData: LiveData<List<AccaProfile>>
    private val mProfileDao: ProfileDao

    init
    {
        val accaDatabase = AccaRoomDatabase.getDatabase(application)
        mProfileDao = accaDatabase.profileDao()
        mProfilesListLiveData = mProfileDao.getAllProfiles()
    }

    suspend fun getProfiles(): List<AccaProfile> {
        return mProfileDao.getProfiles()
    }

    suspend fun getProfileById(id: Int): AccaProfile? {
        return mProfileDao.getProfileById(id)
    }

    fun getLiveData(): LiveData<List<AccaProfile>> {
        return mProfilesListLiveData
    }

    fun insertProfile(profile: AccaProfile) = viewModelScope.launch {
        mProfileDao.insert(profile)
    }

    fun deleteProfile(profile: AccaProfile) = viewModelScope.launch {
        mProfileDao.delete(profile)
    }

    fun updateProfile(profile: AccaProfile) = viewModelScope.launch {
        mProfileDao.update(profile)
    }
}
