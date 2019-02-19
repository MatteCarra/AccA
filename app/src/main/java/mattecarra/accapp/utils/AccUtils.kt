package mattecarra.accapp.utils

import android.os.Environment
import com.topjohnwu.superuser.Shell
import mattecarra.accapp.adapters.Schedule
import mattecarra.accapp.data.*
import java.io.File
import java.io.IOException


object AccUtils {
    val CAPACITY_CONFIG_REGEXP = """^\s*capacity=(\d*),(\d*),(\d+)-(\d+)""".toRegex(RegexOption.MULTILINE)
    val COOLDOWN_CONFIG_REGEXP = """^\s*coolDown=(\d*)/(\d*)""".toRegex(RegexOption.MULTILINE)
    val TEMP_CONFIG_REGEXP = """^\s*temp=(\d*)-(\d*)_(\d*)""".toRegex(RegexOption.MULTILINE)
    val RESET_UNPLUGGED_CONFIG_REGEXP = """^\s*resetUnplugged=(true|false)""".toRegex(RegexOption.MULTILINE)
    val ON_BOOT_EXIT = """^\s*onBootExit=(true|false)""".toRegex(RegexOption.MULTILINE)
    val ON_BOOT = """^\s*onBoot=([^#]+)""".toRegex(RegexOption.MULTILINE)
    val VOLT_FILE = """^\s*voltFile=([^#]+)""".toRegex(RegexOption.MULTILINE)

    val defaultConfig: AccConfig = AccConfig(
        Capacity(5, 60, 70, 80),
        Cooldown(50, 10),
        Temp(40, 45, 90),
        VoltControl(null, null),
        false,
        false,
        null
    )

    private fun getVoltageMax(): Int? {
        return Shell.su("acc -v -").exec().out.joinToString(separator = "\n").trim().toIntOrNull()
    }

    fun readConfig(): AccConfig {
        val config = readConfigToStringArray().joinToString(separator = "\n")

        val (shutdown, coolDown, resume, pause) = CAPACITY_CONFIG_REGEXP.find(config)!!.destructured
        val capacity = Capacity(shutdown.toIntOrNull() ?: 0, coolDown.toInt() ?: 101, resume.toInt(), pause.toInt())

        val coolDownMatchResult = COOLDOWN_CONFIG_REGEXP.find(config)
        val cooldown: Cooldown? =
            coolDownMatchResult?.let {
                val (coolDownChargeSeconds, coolDownPauseSeconds) = coolDownMatchResult.destructured
                coolDownChargeSeconds.toIntOrNull()?.let { chargeInt ->
                    coolDownPauseSeconds.toIntOrNull()?.let { Cooldown(chargeInt, it) }
                }
            }

        val (coolDownTemp, pauseChargingTemp, waitSeconds) = TEMP_CONFIG_REGEXP.find(config)!!.destructured
        val temp = Temp(
            coolDownTemp.toIntOrNull()?.let { it/10 } ?: 90,
            pauseChargingTemp.toIntOrNull()?.let { it/10 } ?: 95,
            waitSeconds.toIntOrNull() ?: 90
        )

        val voltFile = VOLT_FILE.find(config)?.destructured?.component1()
        val voltControl = VoltControl(voltFile, getVoltageMax())

        return AccConfig(
            capacity,
            cooldown,
            temp,
            voltControl,
            RESET_UNPLUGGED_CONFIG_REGEXP.find(config)?.destructured?.component1() == "true",
            ON_BOOT_EXIT.find(config)?.destructured?.component1() == "true",
            ON_BOOT.find(config)?.destructured?.component1())
    }

    @Throws(IOException::class)
    fun readConfigToStringArray(): List<String> {
        val config = File(Environment.getExternalStorageDirectory(), "acc/config.txt")
        return if (config.exists())
            config.readText(charset = Charsets.UTF_8).split("\n")
        else
            emptyList()
    }

    @Throws(IOException::class)
    fun writeConfigFromStringArray(text: List<String>): Boolean {
        val config = File(Environment.getExternalStorageDirectory(), "acc/config.txt")
        if (config.exists()) {
            config.writeText(text.joinToString(separator = "\n"))
            return true
        } else {
            return false
        }
    }

    //update temp command
    fun updateTempCommand(cooldDownTemp: Int, pauseChargingTemp: Int, waitSeconds: Int) = "acc -s temp ${cooldDownTemp*10}-${pauseChargingTemp*10}_$waitSeconds"

    fun updateTemp(cooldDownTemp: Int, pauseChargingTemp: Int, waitSeconds: Int): Boolean {
        return Shell.su(updateTempCommand(cooldDownTemp, pauseChargingTemp, waitSeconds)).exec().isSuccess
    }

    //Update cool down command
    fun updateCoolDownCommand(charge: Int, pause: Int) = "acc -s coolDown $charge/$pause"

    fun updateCoolDown(charge: Int, pause: Int): Boolean {
        return Shell.su(updateCoolDownCommand(charge, pause)).exec().isSuccess
    }

    //Update capacity command
    fun updateCapacityCommand(shutdown: Int, coolDown: Int, resume: Int, pause: Int): String = "acc -s capacity $shutdown,$coolDown,$resume-$pause"

    fun updateCapacity(shutdown: Int, coolDown: Int, resume: Int, pause: Int): Boolean {
        return Shell.su(updateCapacityCommand(shutdown, coolDown, resume, pause)).exec().isSuccess
    }

    //reset unplugged command
    fun updateResetUnpluggedCommand(resetUnplugged: Boolean): String = "acc -s resetUnplugged $resetUnplugged"

    fun updateResetUnplugged(resetUnplugged: Boolean): Boolean {
        return Shell.su(updateResetUnpluggedCommand(resetUnplugged)).exec().isSuccess
    }

    //update on boot exit
    fun updateOnBootExitCommand(value: Boolean): String = "acc -s onBootExit $value"

    fun updateOnBootExit(value: Boolean): Boolean {
        return Shell.su(updateOnBootExitCommand(value)).exec().isSuccess
    }

    //Update on boot
    fun updateOnBootCommand(value: String?): String = "acc -s onBoot${ value?.let{ " $it" } ?: ""}"

    fun updateOnBoot(value: String?): Boolean {
        return Shell.su(updateOnBootCommand(value)).exec().isSuccess
    }

    //Update volt file
    fun updateVoltageCommand(voltControl: String?, voltMax: Int?): String {
        return if(voltControl != null && voltMax != null)
            "acc -v $voltControl:$voltMax"
        else if(voltMax != null)
            "acc -v $voltMax"
        else if(voltControl != null)
            "acc -v $voltControl:"
        else
            "acc -v "
    }

    fun updateVoltage(voltControl: String?, voltMax: Int?): Boolean {
        return Shell.su(updateVoltageCommand(voltControl, voltMax)).exec().isSuccess
    }

    fun resetBatteryStats(): Boolean {
        return Shell.su("acc -R").exec().isSuccess
    }

    private val STATUS_REGEXP = "^\\s*STATUS=(Charging|Discharging)".toRegex(RegexOption.MULTILINE)
    private val HEALTH_REGEXP = "^\\s*HEALTH=([a-zA-Z]+)".toRegex(RegexOption.MULTILINE)
    private val CURRENT_NOW_REGEXP = "^\\s*CURRENT_NOW=(-?\\d+)".toRegex(RegexOption.MULTILINE)
    private val VOLTAGE_NOW_REGEXP = "^\\s*VOLTAGE_NOW=(\\d+)".toRegex(RegexOption.MULTILINE)
    private val TEMP_REGEXP = "^\\s*TEMP=(\\d+)".toRegex(RegexOption.MULTILINE)
    //private val VOLTAGE_REGEXP = "^\\s*VOLTAGE_MAX=(\\d+)".toRegex(RegexOption.MULTILINE)

    fun getBatteryInfo(): BatteryInfo {
        val info =  Shell.su("acc -i").exec().out.joinToString(separator = "\n")

        return BatteryInfo(
            STATUS_REGEXP.find(info)?.destructured?.component1() ?: "Discharging",
            HEALTH_REGEXP.find(info)?.destructured?.component1() ?: "Unknown",
            CURRENT_NOW_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1,
            VOLTAGE_NOW_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1,
            TEMP_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull()?.let { it/10 } ?: -1
        )
    }

    fun isBatteryCharging(): Boolean {
        return Shell.su("acc -i").exec().out.find { it.matches(STATUS_REGEXP) } == "STATUS=Charging"
    }

    fun isAccdRunning(): Boolean {
        return Shell.su("acc -D").exec().out.find { it.contains("accd is running") } != null
    }

    fun accStartDeamon(): Boolean {
        return Shell.su("acc -D start").exec().isSuccess
    }

    fun accRestartDeamon(): Boolean {
        return Shell.su("acc -D restart").exec().isSuccess
    }

    fun accStopDeamon(): Boolean {
        return Shell.su("acc -D stop").exec().isSuccess
    }

    fun deleteSchedule(once: Boolean, name: String): Boolean {
        return Shell.su("djs cancel ${if(once) "once" else "daily" } $name").exec().isSuccess
    }

    fun schedule(once: Boolean, hour: Int, minute: Int, commands: List<String>): Boolean {
        return AccUtils.schedule(once, hour, minute, commands.joinToString(separator = "; "))
    }

    fun schedule(once: Boolean, hour: Int, minute: Int, commands: String): Boolean {
        return Shell.su("djs ${if(once) 'o' else 'd' } ${String.format("%02d", hour)} ${String.format("%02d", minute)} \"${commands}\"").exec().isSuccess
    }

    private val SCHEDULE_REGEXP = """^\s*([0-9]{2})([0-9]{2}): (.*)$""".toRegex()

    fun listSchedules(once: Boolean): List<Schedule> {
        return Shell.su("djs i ${if(once) 'o' else 'd'}").exec().out.filter { it.matches(SCHEDULE_REGEXP) }.map {
            val (hour, minute, command) = SCHEDULE_REGEXP.find(it)!!.destructured
            Schedule("$hour$minute", once, hour.toInt(), minute.toInt(), command)
        }
    }

    fun listAllSchedules(): List<Schedule> {
        val res = ArrayList<Schedule>(listSchedules(true))
        res.addAll(listSchedules(false))
        return res
    }
}
