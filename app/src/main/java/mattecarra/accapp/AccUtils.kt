package mattecarra.accapp

import android.os.Environment
import android.util.Log
import com.topjohnwu.superuser.Shell
import mattecarra.accapp.data.*
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.regex.Matcher


object AccUtils {

    const val CAPACITY_PREFIX = "capacity="
    const val TEMP_PREFIX = "temp="
    const val RESET_UNPLUGGED_PREFIX = "resetUnplugged="
    const val COOL_DOWN_PREFIX = "coolDown="

    fun updateConfig(config: AccConfig) {
        val capacity = config.capacity
        val coolDown = config.cooldown
        val temp = config.temp

        val oldConfig = readConfigToStringArray()
        val newConfig = mutableListOf<String>()

        oldConfig.forEach {
            if(it.startsWith(CAPACITY_PREFIX))
                newConfig.add("$CAPACITY_PREFIX${capacity.shutdownCapacity},${capacity.coolDownCapacity},${capacity.resumeCapacity}-${capacity.pauseCapacity} # <shutdown,coolDownTemp,resume-pause> -- ideally, <resume> shouldn't be more than 10 units below <pause>. To disable <shutdown>, and <coolDownTemp>, set these to 0 and 101, respectively (e.g., capacity=0,101,70-80). Note that the latter doesn't disable the cooling feature entirely, since it works not only based on battery capacity, but temperature as well.")
            else if(it.startsWith(TEMP_PREFIX))
                newConfig.add("$TEMP_PREFIX${temp.coolDownTemp}-${temp.pauseChargingTemp}_${temp.waitSeconds}")
            else if(it.startsWith(RESET_UNPLUGGED_PREFIX))
                newConfig.add("$RESET_UNPLUGGED_PREFIX${config.resetUnplugged}")
            else if(it.startsWith(COOL_DOWN_PREFIX) && coolDown != null)
                newConfig.add("$COOL_DOWN_PREFIX${coolDown.charge}/${coolDown.pause} # Charge/pause ratio (in seconds) -- reduces battery temperature and voltage induced stress by periodically pausing charging. This can be disabled with a null value or a preceding hashtag. If charging is too slow, turn this off or change the charge/pause ratio. Disabling this nullifies <coolDownTemp capacity> and <lower temperature> values -- leaving only a temperature limit with a cooling timeout.")
            else
                newConfig.add(it)
        }
    }

    fun readConfig(): AccConfig? {
        val config = readConfigToStringArray()
        val numberRegexp = "\\d+".toPattern()

        var capacity: Capacity? = null //this must be not null
        var coolDown: Cooldown? = null //this can be null
        var temp: Temp? = null //this must be not null
        var resetUnplugged = false //default false

        //I use this function to match the pattern and convert it to int at the same time
        val getInt: (Matcher) -> Int = {
            if(it.find())
                it.group().toInt()
            else throw IllegalStateException("Could not find an int")
        }

        config.forEach {
            try {
                if(it.startsWith(CAPACITY_PREFIX)) {
                    println(it.substring(CAPACITY_PREFIX.length))
                    val m = numberRegexp.matcher(it.substring(CAPACITY_PREFIX.length))
                    capacity = Capacity(getInt(m), getInt(m), getInt(m), getInt(m))
                } else if(it.startsWith(TEMP_PREFIX)) {
                    val m = numberRegexp.matcher(it.substring(TEMP_PREFIX.length))
                    temp = Temp(getInt(m) / 10, getInt(m) / 10, getInt(m))
                } else if(it.startsWith(RESET_UNPLUGGED_PREFIX))
                    resetUnplugged = !it.startsWith("resetUnplugged=false")
                else if(it.startsWith(COOL_DOWN_PREFIX)) {
                    val m = numberRegexp.matcher(it.substring(COOL_DOWN_PREFIX.length))
                    coolDown = Cooldown(getInt(m), getInt(m))
                }
            } catch (ex: Exception) {
                ex.printStackTrace() //Malformed config
            }
        }

        return if(capacity != null && temp != null)
            AccConfig(capacity!!, coolDown, temp!!, resetUnplugged) //success
        else
            null //failed
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

    fun resetBatteryStats(): Boolean {
        return Shell.su("acc -R").exec().isSuccess
    }

    fun isBatteryCharging(): Boolean {
        return Shell.su("acc -i").exec().out.find { it.matches(Regex("STATUS=(Charging|Discharging)")) } == "STATUS=Charging"
    }


    private val STATUS_REGEXP = "^\\s*STATUS=(Charging|Discharging)".toRegex(RegexOption.MULTILINE)
    private val HEALTH_REGEXP = "^\\s*HEALTH=([a-zA-Z]+)".toRegex(RegexOption.MULTILINE)
    private val CURRENT_NOW_REGEXP = "^\\s*CURRENT_NOW=(\\d+)".toRegex(RegexOption.MULTILINE)
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