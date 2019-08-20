package mattecarra.accapp.models

import mattecarra.accapp.acc.Acc
import mattecarra.accapp.acc.ConfigUpdater
import mattecarra.accapp.djs.Djs
import mattecarra.accapp.djs.DjsSchedule

data class Time(val hour: Int, val minute: Int)

data class Schedule(val time: String, val executeOnce: Boolean, val profile: ScheduleProfile) {
    private val timeRegex = """([0-9]{2})([0-9]{2})""".toRegex()

    fun getCommand(): String {
        return ": accaScheduleId${profile.uid}; ${ConfigUpdater(profile.accConfig).concatenateCommands(Acc.instance)}; ${Djs.instance.getDeleteCommand(": accaScheduleId${profile.uid}")}"
    }

    fun getTime(): Time? {
        return if(isBootSchedule())
            null
        else
            timeRegex.find(time)?.destructured?.let { (hour: String, minute: String) ->
                Time(hour.toInt(), minute.toInt())
            }
    }

    fun isBootSchedule(): Boolean {
        return time == "boot"
    }

    fun toDjsSchedule(): DjsSchedule {
        return DjsSchedule(profile.uid, time, executeOnce, getCommand())
    }
}