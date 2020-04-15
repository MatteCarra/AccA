package mattecarra.accapp.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import mattecarra.accapp.database.AccaRoomDatabase
import mattecarra.accapp.database.ProfileDao


import mattecarra.accapp.models.AccaProfile

class ProfilesViewModel(application: Application) : AndroidViewModel(application) {

    private val mProfilesListLiveData: LiveData<List<AccaProfile>>
    private val mProfileDao: ProfileDao

    init {
        val accaDatabase = AccaRoomDatabase.getDatabase(application)
        mProfileDao = accaDatabase.profileDao()
        mProfilesListLiveData = mProfileDao.getAllProfiles()
    }

    suspend fun getProfile(id: Int): AccaProfile? {
        return mProfileDao.getProfileById(id)
    }

    fun getProfiles() : LiveData<List<AccaProfile>> {
        return mProfilesListLiveData
    }
}
