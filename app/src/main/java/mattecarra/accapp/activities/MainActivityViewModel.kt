package mattecarra.accapp.activities

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.utils.Constants
import mattecarra.accapp.utils.DataRepository
import kotlin.coroutines.CoroutineContext

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    var selectedNavBarItem: Int = R.id.botNav_home

    private val mDataRepository: DataRepository
    private var mParentJob = Job()
    private val mCoroutineContext: CoroutineContext
        get() = mParentJob + Dispatchers.IO

    private val mScope = CoroutineScope(mCoroutineContext)

    init {
        mDataRepository = DataRepository(application, mScope)
    }

    fun insertProfile(profile: AccaProfile) = mScope.launch(Dispatchers.IO) {
        mDataRepository.insertProfile(profile)
    }

    fun deleteProfile(profile: AccaProfile) = mScope.launch(Dispatchers.IO) {
        mDataRepository.deleteProfile(profile)
    }

    fun updateProfile(profile: AccaProfile) = mScope.launch(Dispatchers.IO) {
        mDataRepository.updateProfile(profile)
    }

    fun getProfileById(id: Int) : AccaProfile {
        return mDataRepository.getProfileById(id)
    }
}