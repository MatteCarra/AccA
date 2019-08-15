package mattecarra.accapp.activities

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.*
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
    var selectedNavBarItem = R.id.botNav_home

    private val mDataRepository: DataRepository = DataRepository(application, viewModelScope)

    val profiles = mDataRepository.getAllProfiles()

    fun insertProfile(profile: AccaProfile) = mDataRepository.insertProfile(profile)

    fun deleteProfile(profile: AccaProfile) = mDataRepository.deleteProfile(profile)

    fun updateProfile(profile: AccaProfile) = mDataRepository.updateProfile(profile)

    suspend fun getProfileById(id: Int) : AccaProfile = mDataRepository.getProfileById(id)
}