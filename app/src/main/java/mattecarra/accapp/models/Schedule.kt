package mattecarra.accapp.models

import mattecarra.accapp.acc.Acc
import mattecarra.accapp.acc.ConfigUpdater
import mattecarra.accapp.djs.DjsSchedule

data class Schedule(val hour: Int, val minute: Int, val profile: ScheduleProfile) {
    fun getCommand(): String {
        return "accaScheduleId=${profile.uid}; ${ConfigUpdater(profile.accConfig).concatenateCommands(Acc.instance)}"
    }

    fun toDjsSchedule(): DjsSchedule {
        return DjsSchedule(profile.uid, hour, minute, getCommand())
    }
}