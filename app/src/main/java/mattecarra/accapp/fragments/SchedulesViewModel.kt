package mattecarra.accapp.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.acc.ConfigUpdater
import mattecarra.accapp.database.AccaRoomDatabase
import mattecarra.accapp.database.ScheduleDao
import mattecarra.accapp.djs.Djs
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.ScheduleProfile
import mattecarra.accapp.models.Schedule

class SchedulesViewModel(application: Application) : AndroidViewModel(application) {
    val schedules = MutableLiveData<List<Schedule>>()

    private val mSchedulesDao: ScheduleDao
    init {
        val accaDatabase = AccaRoomDatabase.getDatabase(application)
        mSchedulesDao = accaDatabase.scheduleDao()

        viewModelScope.launch {
            refreshSchedules()
        }
    }

    private suspend fun refreshSchedules() {
        val newSchedules =
            Djs
                .instance
                .list()
                .map { djsSchedule ->
                    withContext(Dispatchers.IO) {
                        async {
                            val scheduleProfile =
                                getScheduleProfileById(djsSchedule.scheduleProfileId)

                            if (scheduleProfile != null)
                                Schedule(djsSchedule.time, djsSchedule.executeOnce, scheduleProfile)
                            else {
                                //TODO handle schedules created by an uninstalled version of the app
                                null
                            }
                        }
                    }
                }.mapNotNull {
                    it.await()
                }

        schedules.value = newSchedules
    }

    fun addSchedule(time: String, executeOnce: Boolean, profile: AccConfig) = viewModelScope.launch {
        val id = insertScheduleProfile(ScheduleProfile(0, profile))
        Djs.instance.append(
            Schedule(time, executeOnce, ScheduleProfile(id, profile))
                .toDjsSchedule()
        )
        refreshSchedules()
    }

    fun editSchedule(id: Int, time: String, executeOnce: Boolean, profile: AccConfig) = viewModelScope.launch {
        updateScheduleProfile(ScheduleProfile(id, profile))

        Djs.instance.deleteById(id)
        Djs.instance.append(
            Schedule(time, executeOnce, ScheduleProfile(id, profile))
                .toDjsSchedule()
        )

        refreshSchedules()
    }

    fun removeSchedule(schedule: Schedule) = viewModelScope.launch {
        deleteScheduleProfile(schedule.profile.uid)
        Djs.instance.delete(schedule.toDjsSchedule())
        refreshSchedules()
    }

    private suspend fun insertScheduleProfile(schedule: ScheduleProfile): Int {
        return mSchedulesDao.insert(schedule).toInt()
    }

    private fun deleteScheduleProfile(id: Int) = viewModelScope.launch {
        mSchedulesDao.deleteById(id)
    }

    private fun updateScheduleProfile(schedule: ScheduleProfile) = viewModelScope.launch {
        mSchedulesDao.update(schedule)
    }

    suspend fun getScheduleProfileById(id: Int): ScheduleProfile? {
        return mSchedulesDao.getScheduleById(id)
    }
}
