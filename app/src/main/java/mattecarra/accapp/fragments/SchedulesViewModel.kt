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
                                Schedule(djsSchedule.time, djsSchedule.executeOnce, djsSchedule.executeOnBoot, scheduleProfile)
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

    fun addSchedule(scheduleName: String, time: String, executeOnce: Boolean, executeOnBoot: Boolean, profile: AccConfig) = viewModelScope.launch {
        val id = insertScheduleProfile(ScheduleProfile(0, scheduleName, profile))
        Djs.instance.append(
            Schedule(time, executeOnce, executeOnBoot, ScheduleProfile(id, scheduleName, profile))
                .toDjsSchedule()
        )
        refreshSchedules()
    }

    fun editSchedule(id: Int, scheduleName: String, time: String, executeOnce: Boolean, executeOnBoot: Boolean, profile: AccConfig) = viewModelScope.launch {
        val scheduleProfile = ScheduleProfile(id, scheduleName, profile)

        updateScheduleProfile(scheduleProfile)

        Djs.instance.edit(
            Schedule(time, executeOnce, executeOnBoot, scheduleProfile)
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
