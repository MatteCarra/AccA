package mattecarra.accapp.utils

import android.app.Application
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import mattecarra.accapp.database.AccaRoomDatabase
import mattecarra.accapp.database.ProfileDao
import mattecarra.accapp.models.AccaProfile

class DataRepository(application: Application, scope: CoroutineScope) {

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

    @WorkerThread
    fun insertProfile(profile: AccaProfile) {
        mProfileDao.insert(profile)
    }

    @WorkerThread
    fun deleteProfile(profile: AccaProfile) {
        mProfileDao.delete(profile)
    }

    @WorkerThread
    fun updateProfile(profile: AccaProfile) {
        mProfileDao.update(profile)
    }

    @WorkerThread
    fun getProfileById(id: Int): AccaProfile {
        return mProfileDao.getProfileById(id)
    }
}