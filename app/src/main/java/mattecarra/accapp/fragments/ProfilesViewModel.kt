package mattecarra.accapp.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.utils.DataRepository

class ProfilesViewModel(application: Application) : AndroidViewModel(application) {

    private val mDataRepository: DataRepository

    private val mProfilesListLiveData: LiveData<List<AccaProfile>>

    private var mParentJob = Job()
    private val mCoroutineContext: CoroutineContext
        get() = mParentJob + Dispatchers.Main

    private val mScope = CoroutineScope(mCoroutineContext)

    init {

        mDataRepository = DataRepository(application, mScope)
        mProfilesListLiveData = mDataRepository.getAllProfiles()
    }

    fun getProfiles() : LiveData<List<AccaProfile>> {
        return mProfilesListLiveData
    }

    override fun onCleared() {
        super.onCleared()
        mParentJob.cancel()
    }
}
