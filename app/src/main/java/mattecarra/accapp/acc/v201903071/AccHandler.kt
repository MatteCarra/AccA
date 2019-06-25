package mattecarra.accapp.acc.v201903071

import com.topjohnwu.superuser.Shell

class AccHandler: mattecarra.accapp.acc.v201905111.AccHandler() {
    override fun listChargingSwitches(): List<String> {
        val res = Shell.su("acc -s s:").exec()
        return if(res.isSuccess) res.out.map { it.trim() }.filter { it.isNotEmpty() } else emptyList()
    }
}