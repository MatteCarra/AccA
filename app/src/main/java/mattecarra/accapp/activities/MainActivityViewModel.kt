package mattecarra.accapp.activities

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import mattecarra.accapp.R
import mattecarra.accapp.database.AccaRoomDatabase
import mattecarra.accapp.database.ProfileDao
import mattecarra.accapp.database.ScheduleDao
import mattecarra.accapp.models.AccaProfile

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    var selectedNavBarItem = R.id.botNav_home

    val profiles: LiveData<List<AccaProfile>>

    private val mProfileDao: ProfileDao
    init {
        val accaDatabase = AccaRoomDatabase.getDatabase(application)
        mProfileDao = accaDatabase.profileDao()

        profiles = mProfileDao.getAllProfiles()
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

    suspend fun getProfiles(): List<AccaProfile> {
        return mProfileDao.getProfiles()
    }

    suspend fun getProfileById(id: Int): AccaProfile? {
        return mProfileDao.getProfileById(id)
    }
}