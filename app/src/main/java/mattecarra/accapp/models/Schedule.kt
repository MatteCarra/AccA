package mattecarra.accapp.models

import mattecarra.accapp.acc.Acc
import mattecarra.accapp.acc.ConfigUpdater
import mattecarra.accapp.djs.Djs
import mattecarra.accapp.djs.DjsSchedule
import java.lang.StringBuilder

data class Time(val hour: Int, val minute: Int)

data class Schedule(val isEnabled: Boolean, val time: String, val executeOnce: Boolean, val executeOnBoot: Boolean, val profile: ScheduleProfile) {
    private val timeRegex = """([0-9]{2})([0-9]{2})""".toRegex()

    fun getCommand(): String {
        val string = StringBuilder(": accaScheduleId${profile.uid}; ${ConfigUpdater(profile.accConfig).concatenateCommands(Acc.instance)}")

        if(executeOnce)
            string.append("; : --delete")

        if(executeOnBoot)
            string.append("; : --boot")

        return string.toString()
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
        return DjsSchedule(profile.uid, isEnabled, time, executeOnce, executeOnBoot, getCommand())
    }
}