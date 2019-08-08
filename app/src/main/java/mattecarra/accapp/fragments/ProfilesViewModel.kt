package mattecarra.accapp.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.utils.DataRepository

class ProfilesViewModel(application: Application) : AndroidViewModel(application) {

    private val mDataRepository: DataRepository = DataRepository(application, viewModelScope)

    private val mProfilesListLiveData: LiveData<List<AccaProfile>> = mDataRepository.getAllProfiles()

    fun getProfiles() : LiveData<List<AccaProfile>> {
        return mProfilesListLiveData
    }
}
