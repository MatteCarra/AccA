package mattecarra.accapp.acc.v201903071

import com.topjohnwu.superuser.Shell

class AccHandler: mattecarra.accapp.acc.v201905111.AccHandler() {
    override fun listChargingSwitches(): List<String> {
        val res = Shell.su("acc -s s:").exec()
        return if(res.isSuccess) res.out.map { it.trim() }.filter { it.isNotEmpty() } else emptyList()
    }

    override fun getCurrentChargingSwitch(config: String): String? {
        val switch = """^\s*chargingSwitch=((?:(?!#).)*)""".toRegex(RegexOption.MULTILINE).find(readConfigToString())?.destructured?.component1()?.trim()
        return if(switch?.isNotEmpty() == true) switch else null
    }

    override fun updateAccChargingSwitch(switch: String?) : Boolean {
        if (switch.isNullOrBlank()) {
            return Shell.su("acc -s s-").exec().isSuccess
        }

        return Shell.su("acc -s s $switch").exec().isSuccess
    }
}