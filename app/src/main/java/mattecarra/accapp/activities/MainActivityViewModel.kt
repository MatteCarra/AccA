package mattecarra.accapp.activities

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.utils.DataRepository
import kotlin.coroutines.CoroutineContext

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val mDataRepository: DataRepository

    private var mParentJob = Job()
    private val mCoroutineContext: CoroutineContext
        get() = mParentJob + Dispatchers.Main

    private val mScope = CoroutineScope(mCoroutineContext)

    init {

        mDataRepository = DataRepository(application, mScope)
    }

    fun insertProfile(profile: AccaProfile) = mScope.launch(Dispatchers.IO) {

        mDataRepository.insertProfile(profile)
    }
}