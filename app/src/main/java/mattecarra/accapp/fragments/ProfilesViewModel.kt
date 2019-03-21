package mattecarra.accapp.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


import mattecarra.accapp.models.ProfileEntity
import mattecarra.accapp.utils.DataRepository

class ProfilesViewModel(application: Application) : AndroidViewModel(application) {

    private val mDataRepository: DataRepository

    private val mProfilesListLiveData: LiveData<List<ProfileEntity>>

    private var mParentJob = Job()
    private val mCoroutineContext: CoroutineContext
        get() = mParentJob + Dispatchers.Main

    private val mScope = CoroutineScope(mCoroutineContext)

    init {

        mDataRepository = DataRepository(application)
        mProfilesListLiveData = mDataRepository.getAllProfiles()
    }

    fun insertProfile(profile: ProfileEntity) = mScope.launch(Dispatchers.IO) {

        mDataRepository.insertProfile(profile)
    }

    override fun onCleared() {
        super.onCleared()
        mParentJob.cancel()
    }
}
