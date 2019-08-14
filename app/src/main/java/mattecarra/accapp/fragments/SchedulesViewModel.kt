package mattecarra.accapp.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mattecarra.accapp.djs.Djs
import mattecarra.accapp.models.Schedule

class SchedulesViewModel : ViewModel() {
    val schedules = MutableLiveData<List<Schedule>>()

    init {
        viewModelScope.launch {
            while (true) {
                if(schedules.hasActiveObservers())
                    schedules.value = Djs.instance.list()

                delay(10000)
            }
        }
    }

    fun addSchedule(schedule: Schedule) = viewModelScope.launch {
        Djs.instance.append(schedule)
        schedules.value = Djs.instance.list()
    }

    fun removeSchedule(schedule: Schedule) = viewModelScope.launch {
        Djs.instance.delete(schedule)
        schedules.value = Djs.instance.list()
    }
}
