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

        val accaDatabase = AccaRoomDatabase.getDatabase(application, scope)
        mProfileDao = accaDatabase.profileDao()
        mProfileListLiveData = mProfileDao.getAllProfiles()
    }

    fun getAllProfiles(): LiveData<List<AccaProfile>> {
        return mProfileListLiveData
    }

    @WorkerThread
    suspend fun insertProfile(profile: AccaProfile) {
        mProfileDao.insert(profile)
    }



}