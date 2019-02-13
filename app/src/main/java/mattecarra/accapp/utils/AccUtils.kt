package mattecarra.accapp.utils

import android.os.Environment
import com.topjohnwu.superuser.Shell
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

    val defaultConfig: AccConfig = AccConfig(
        Capacity(5, 60, 70, 80),
        Cooldown(50, 10),
        Temp(40, 45, 90),
        false,
        false,
        null
    )

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

        return AccConfig(
            capacity,
            cooldown,
            temp,
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

    //not used. I'm using "acc -s key value" commands instead
    fun updateConfig(config: AccConfig) {
        val capacity = config.capacity
        val coolDown = config.cooldown
        val temp = config.temp

        val oldConfig = readConfigToStringArray()
        val newConfig = mutableListOf<String>()

        oldConfig.forEach {
            if(it.startsWith("capacity="))
                newConfig.add("capacity=${capacity.shutdownCapacity},${capacity.coolDownCapacity},${capacity.resumeCapacity}-${capacity.pauseCapacity} # <shutdown,coolDownTemp,resume-pause> -- ideally, <resume> shouldn't be more than 10 units below <pause>. To disable <shutdown>, and <coolDownTemp>, set these to 0 and 101, respectively (e.g., capacity=0,101,70-80). Note that the latter doesn't disable the cooling feature entirely, since it works not only based on battery capacity, but temperature as well.")
            else if(it.startsWith("temp="))
                newConfig.add("temp=${temp.coolDownTemp}-${temp.pauseChargingTemp}_${temp.waitSeconds}")
            else if(it.startsWith("resetUnplugged="))
                newConfig.add("resetUnplugged=${config.resetUnplugged}")
            else if(it.startsWith("coolDown=") && coolDown != null)
                newConfig.add("coolDown=${coolDown.charge}/${coolDown.pause} # Charge/pause ratio (in seconds) -- reduces battery temperature and voltage induced stress by periodically pausing charging. This can be disabled with a null value or a preceding hashtag. If charging is too slow, turn this off or change the charge/pause ratio. Disabling this nullifies <coolDownTemp capacity> and <lower temperature> values -- leaving only a temperature limit with a cooling timeout.")
            else
                newConfig.add(it)
        }

        writeConfigFromStringArray(newConfig)
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

    fun updateTemp(cooldDownTemp: Int, pauseChargingTemp: Int, waitSeconds: Int): Boolean {
        return Shell.su("acc -s temp ${cooldDownTemp*10}-${pauseChargingTemp*10}_${waitSeconds}").exec().isSuccess
    }

    fun updateCoolDown(charge: Int, pause: Int): Boolean {
        return Shell.su("acc -s coolDown ${charge}/${pause}").exec().isSuccess
    }

    fun updateCapacity(shutdown: Int, coolDown: Int, resume: Int, pause: Int): Boolean {
        return Shell.su("acc -s capacity ${shutdown},${coolDown},${resume}-${pause}").exec().isSuccess
    }

    fun updateResetUnplugged(resetUnplugged: Boolean): Boolean {
        return Shell.su("acc -s resetUnplugged $resetUnplugged").exec().isSuccess
    }

    fun updateOnBootExit(value: Boolean): Boolean {
        return Shell.su("acc -s onBootExit $value").exec().isSuccess
    }

    fun updateOnBoot(value: String?): Boolean {
        return Shell.su("acc -s onBoot ${value ?: ""}").exec().isSuccess
    }

    fun resetBatteryStats(): Boolean {
        return Shell.su("acc -R").exec().isSuccess
    }

    private val STATUS_REGEXP = "^\\s*STATUS=(Charging|Discharging)".toRegex(RegexOption.MULTILINE)
    private val HEALTH_REGEXP = "^\\s*HEALTH=([a-zA-Z]+)".toRegex(RegexOption.MULTILINE)
    private val CURRENT_NOW_REGEXP = "^\\s*CURRENT_NOW=(-?\\d+)".toRegex(RegexOption.MULTILINE)
    private val VOLTAGE_NOW_REGEXP = "^\\s*VOLTAGE_NOW=(\\d+)".toRegex(RegexOption.MULTILINE)
    private val TEMP_REGEXP = "^\\s*TEMP=(\\d+)".toRegex(RegexOption.MULTILINE)

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
        return Shell.su("accd").exec().isSuccess
    }

    fun accStopDeamon(): Boolean {
        return Shell.su("acc -D stop").exec().isSuccess
    }
}