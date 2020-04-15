package mattecarra.accapp.djs

data class DjsSchedule(val scheduleProfileId: Int, val isEnabled: Boolean, val time: String, val executeOnce: Boolean, val executeOnBoot: Boolean, val command: String)