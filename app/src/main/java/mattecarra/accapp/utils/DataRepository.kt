package mattecarra.accapp.utils

import android.app.Application
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mattecarra.accapp.database.AccaRoomDatabase
import mattecarra.accapp.database.ProfileDao
import mattecarra.accapp.models.AccaProfile

class DataRepository(application: Application, private val scope: CoroutineScope) {

    private val mProfileDao: ProfileDao

    // Live Data
    private val mProfileListLiveData: LiveData<List<AccaProfile>>

    init {
        val accaDatabase = AccaRoomDatabase.getDatabase(application)
        mProfileDao = accaDatabase.profileDao()

        mProfileListLiveData = mProfileDao.getAllProfiles()
    }

    fun getAllProfiles(): LiveData<List<AccaProfile>> {
        return mProfileListLiveData
    }

    fun insertProfile(profile: AccaProfile) = scope.launch {
        mProfileDao.insert(profile)
    }

    fun deleteProfile(profile: AccaProfile) = scope.launch {
        mProfileDao.delete(profile)
    }

    fun updateProfile(profile: AccaProfile) = scope.launch {
        mProfileDao.update(profile)
    }

    suspend fun getProfileById(id: Int): AccaProfile {
        return mProfileDao.getProfileById(id)
    }
}