package mattecarra.accapp.fragments

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.models.BatteryInfo
import mattecarra.accapp.acc.v201905111.AccHandler
import org.jetbrains.anko.doAsync

class DashboardViewModel : ViewModel() {
    private val batteryInfo: MutableLiveData<BatteryInfo> = MutableLiveData()
    private val isDaemonRunning: MutableLiveData<Boolean> = MutableLiveData()

    //Used to update battery info every second
    private val handler = Handler()
    private val updateBatteryInfoRunnable = object : Runnable {
        override fun run() {
            val r = this //need this to make it recursive
            doAsync {
                batteryInfo.postValue(Acc.instance.getBatteryInfo())
                isDaemonRunning.postValue(Acc.instance.isAccdRunning())

                handler.postDelayed(r, 1000)// Repeat the same runnable code block again after 1 seconds
            }
        }
    }

    fun getBatteryInfo(): LiveData<BatteryInfo> {
        return batteryInfo
    }

    fun getIsDaemonRunning(): LiveData<Boolean> {
        return isDaemonRunning
    }

    init {
        handler.post(updateBatteryInfoRunnable)
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(updateBatteryInfoRunnable)
    }
}
