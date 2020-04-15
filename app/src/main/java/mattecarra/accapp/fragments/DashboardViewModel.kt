package mattecarra.accapp.fragments

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mattecarra.accapp.Preferences
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.models.BatteryInfo
import mattecarra.accapp.R

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _battery: MutableLiveData<BatteryInfo> = MutableLiveData()
    private val _daemon: MutableLiveData<Boolean> = MutableLiveData(false)
    private val _chargeSpeed: MutableLiveData<String> = MutableLiveData("N/A")

    val battery: LiveData<BatteryInfo> = _battery
    val daemon: LiveData<Boolean> = _daemon

    private val mPreferences: Preferences = Preferences(application)

    val chargeSpeed: LiveData<String> = _chargeSpeed

    init {
        viewModelScope.launch {
            while (true) {
                if (battery.hasActiveObservers()) {
                    val batteryInfo = Acc.instance.getBatteryInfo()
                    _battery.value = batteryInfo
                    _chargeSpeed.value =
                        application.getString(
                            if(batteryInfo.isCharging())
                                R.string.info_charging_speed_extended
                            else
                                R.string.info_discharging_speed_extended, batteryInfo.getCurrentNow(mPreferences.currentUnitOfMeasure) * (if(batteryInfo.isCharging()) 1 else -1), batteryInfo.getVoltageNow(mPreferences.voltageUnitOfMeasure)
                        )

                }

                if(daemon.hasActiveObservers()) {
                    _daemon.value = Acc.instance.isAccdRunning()
                }

                delay(1000)
            }
        }
    }
}
