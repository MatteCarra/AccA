package mattecarra.accapp.fragments

import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.models.BatteryInfo
import mattecarra.accapp.acc.v201905111.AccHandler
import org.jetbrains.anko.doAsync

class DashboardViewModel : ViewModel() {
    private val batteryInfo: MutableLiveData<BatteryInfo> = MutableLiveData()
    private val isDaemonRunning: MutableLiveData<Boolean> = MutableLiveData()

    @Volatile private var run = false

    init {
        viewModelScope.launch {
            while (true) {
                if (batteryInfo.hasActiveObservers()) {
                    batteryInfo.value = withContext(Dispatchers.IO) {
                        Acc.instance.getBatteryInfo()
                    }
                }

                if(isDaemonRunning.hasActiveObservers()) {
                    isDaemonRunning.value = withContext(Dispatchers.IO) {
                        Acc.instance.isAccdRunning()
                    }
                }

                delay(1000)
            }
        }
    }

    fun getBatteryInfo(): LiveData<BatteryInfo> {
        return batteryInfo
    }

    fun getIsDaemonRunning(): LiveData<Boolean> {
        return isDaemonRunning
    }
}
