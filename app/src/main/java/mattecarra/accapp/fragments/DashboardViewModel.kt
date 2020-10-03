package mattecarra.accapp.fragments

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mattecarra.accapp.Preferences
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.models.BatteryInfo
import mattecarra.accapp.R
import mattecarra.accapp.models.DashboardValues

class DashboardViewModel : ViewModel() {

    private val dashboard: MutableLiveData<DashboardValues> = MutableLiveData()

    fun getDashboardValues(): LiveData<DashboardValues> {
        return dashboard
    }

    init {
        viewModelScope.launch() {
            while (true) {
                if (dashboard.hasActiveObservers()) {
                    dashboard.value = DashboardValues(
                        Acc.instance.getBatteryInfo(),
                        Acc.instance.isAccdRunning()
                    )
                }
                delay(1000)
            }
        }
    }
}
