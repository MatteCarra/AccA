package mattecarra.accapp.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import mattecarra.accapp.database.AccaRoomDatabase


import mattecarra.accapp.models.AccaProfile

class ProfilesViewModel(application: Application) : AndroidViewModel(application) {

    private val mProfilesListLiveData: LiveData<List<AccaProfile>>

    init {
        val accaDatabase = AccaRoomDatabase.getDatabase(application)

        mProfilesListLiveData = accaDatabase.profileDao().getAllProfiles()
    }

    fun getProfiles() : LiveData<List<AccaProfile>> {
        return mProfilesListLiveData
    }
}
