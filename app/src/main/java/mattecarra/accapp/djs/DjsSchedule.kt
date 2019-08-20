package mattecarra.accapp.djs

data class DjsSchedule(val scheduleProfileId: Int, val time: String, val executeOnce: Boolean, val command: String)