package mattecarra.accapp.acc.v201910132

import androidx.annotation.WorkerThread
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.acc.AccInterface
import mattecarra.accapp.acc.ConfigUpdateResult
import mattecarra.accapp.acc.ConfigUpdater
import mattecarra.accapp.models.*
import java.io.IOException
import java.util.regex.Pattern


open class AccHandler: AccInterface {
    // String resources
    private val STRING_UNKNOWN = "Unknown"
    private val STRING_NOT_CHARGING = "Not charging"
    private val STRING_DISCHARGING = "Discharging"
    private val STRING_CHARGING = "Charging"

    // ACC Config Regex
    val CAPACITY_CONFIG_REGEXP = """^\s*capacity=(\d*),(\d*),(\d+)-(\d+)""".toRegex(RegexOption.MULTILINE)
    val COOLDOWN_CONFIG_REGEXP = """^\s*coolDownRatio=(\d*)/(\d*)""".toRegex(RegexOption.MULTILINE)
    val TEMP_CONFIG_REGEXP = """^\s*temperature=(\d*)-(\d*)_(\d*)""".toRegex(RegexOption.MULTILINE)
    val RESET_UNPLUGGED_CONFIG_REGEXP = """^\s*resetBsOnUnplug=(true|false)""".toRegex(RegexOption.MULTILINE)
    val RESET_ON_PAUSE_CONFIG_REGEXP = """^\s*resetBsOnPause=(true|false)""".toRegex(RegexOption.MULTILINE)
    val ON_BOOT = """^\s*applyOnBoot=((?:(?!#).)*)""".toRegex(RegexOption.MULTILINE)
    val ON_PLUGGED = """^\s*applyOnPlug=((?:(?!#).)*)""".toRegex(RegexOption.MULTILINE)
    val VOLT_FILE = """^\s*maxChargingVoltage=((?:(?!:).)*):(\d+)""".toRegex(RegexOption.MULTILINE)
    val SWITCH = """^\s*chargingSwitch=((?:(?!#).)*)""".toRegex(RegexOption.MULTILINE)
    val PRIORITIZE_BATTERY_IDLE = """^\s*prioritizeBattIdleMode=(true|false)""".toRegex(RegexOption.MULTILINE)

    override val defaultConfig: AccConfig
        get() =
            AccConfig(
                AccConfig.ConfigCapacity(5, 70, 80),
                AccConfig.ConfigVoltage(null, null),
                null,
                AccConfig.ConfigTemperature(40, 45, 90),
                null,
                null,
                null,
                false,
                false,
                null,
                false
            )

    override suspend fun readConfig(): AccConfig = withContext(Dispatchers.IO) {
        val config = readConfigToString()

        val (capacityShutdown, capacityCoolDown, capacityResume, capacityPause) = CAPACITY_CONFIG_REGEXP.find(config)!!.destructured
        val (temperatureCooldown, temperatureMax, waitSeconds) = TEMP_CONFIG_REGEXP.find(config)!!.destructured

        val coolDownMatchResult = COOLDOWN_CONFIG_REGEXP.find(config)?.destructured

        val cVolt = VOLT_FILE.find(config)?.destructured

        AccConfig(
            AccConfig.ConfigCapacity(capacityShutdown.toIntOrNull() ?: 0, capacityResume.toInt(), capacityPause.toInt()),
            AccConfig.ConfigVoltage(cVolt?.component1()?.ifBlank { null }, cVolt?.component2()?.toIntOrNull()),
            null,
            AccConfig.ConfigTemperature(temperatureCooldown.toIntOrNull() ?: 90,
                temperatureMax.toIntOrNull() ?: 95,
                waitSeconds.toIntOrNull() ?: 90),
            getOnBoot(config),
            getOnPlugged(config),
            coolDownMatchResult?.let { (coolDownChargeSeconds, coolDownPauseSeconds) ->
                AccConfig.ConfigCoolDown(capacityCoolDown.toInt(), coolDownChargeSeconds.toInt(), coolDownPauseSeconds.toInt())
            },
            getResetUnplugged(config),
            getResetOnPause(config),
            getCurrentChargingSwitch(config),
            isPrioritizeBatteryIdleMode(config)
        )
    }

    @Throws(IOException::class)
    @WorkerThread
    fun readConfigToString(): String {
        return Shell.su("acc-en --config cat").exec().out.joinToString(separator = "\n")
    }

    // Returns OnBoot value
    private fun getOnBoot(config: CharSequence) : String? {
        return ON_BOOT.find(config)?.destructured?.component1()?.trim()?.ifBlank { null }
    }

    // Returns OnPlugged value
    private fun getOnPlugged(config: CharSequence) : String? {
        return ON_PLUGGED.find(config)?.destructured?.component1()?.trim()?.ifBlank { null }
    }

    // Returns ResetUnplugged value
    private fun getResetUnplugged(config: CharSequence) : Boolean {
        return RESET_UNPLUGGED_CONFIG_REGEXP.find(config)?.destructured?.component1() == "true"
    }

    // Returns ResetOnPause value
    private fun getResetOnPause(config: CharSequence) : Boolean {
        return RESET_ON_PAUSE_CONFIG_REGEXP.find(config)?.destructured?.component1() == "true"
    }

    override suspend fun listVoltageSupportedControlFiles(): List<String> = withContext(Dispatchers.IO) {
        val res = Shell.su("acc-en -v :").exec()

        if(res.isSuccess)
            res.out.filter { it.isNotEmpty() }
        else
            emptyList()
    }

    override suspend fun resetBatteryStats(): Boolean = withContext(Dispatchers.IO) {
        Shell.su("acc-en -R").exec().isSuccess
    }

    /**
     * Regex for acc -i (info)
     */
    // Regex for determining NAME of BATTERY
    private val NAME_REGEXP = """^\s*NAME=([a-zA-Z0-9]+)""".toRegex(RegexOption.MULTILINE)
    // Regex for INPUT_SUSPEND
    private val INPUT_SUSPEND_REGEXP = """^\s*INPUT_SUSPEND=([01])""".toRegex(RegexOption.MULTILINE)
    private val STATUS_REGEXP = """^\s*STATUS=(${STRING_CHARGING}|${STRING_DISCHARGING}|${STRING_NOT_CHARGING})""".toRegex(RegexOption.MULTILINE)
    private val HEALTH_REGEXP = """^\s*HEALTH=([a-zA-Z]+)""".toRegex(RegexOption.MULTILINE)
    // Regex for PRESENT value
    private val PRESENT_REGEXP = """^\s*PRESENT=(\d+)""".toRegex(RegexOption.MULTILINE)
    // Regex for determining CHARGE_TYPE
    private val CHARGE_TYPE_REGEXP = """^\s*CHARGE_TYPE=(N/A|[a-zA-Z]+)""".toRegex(RegexOption.MULTILINE)
    // Regex for battery CAPACITY
    private val CAPACTIY_REGEXP = """^\s*CAPACITY=(\d+)""".toRegex(RegexOption.MULTILINE)
    // Regex for CHARGER_TEMP
    private val CHARGER_TEMP_REGEXP = """^\s*CHARGER_TEMP=(\d+)""".toRegex(RegexOption.MULTILINE)
    // Regex for CHARGER_TEMP_MAX
    private val CHARGER_TEMP_MAX_REGEXP = """^\s*CHARGER_TEMP_MAX=(\d+)""".toRegex(RegexOption.MULTILINE)
    // Regex for INPUT_CURRENT_LIMITED, 0 = false, 1 = true
    private val INPUT_CURRENT_LIMITED_REGEXP = """^\s*INPUT_CURRENT_LIMITED=([01])""".toRegex(RegexOption.MULTILINE)
    private val VOLTAGE_NOW_REGEXP = """^\s*VOLTAGE_NOW=(\d+)""".toRegex(RegexOption.MULTILINE)
    // Regex for VOLTAGE_MAX
    private val VOLTAGE_MAX_REGEXP = """^\s*VOLTAGE_MAX=(\d+)""".toRegex(RegexOption.MULTILINE)
    // Regex for VOLTAGE_QNOVO
    private val VOLTAGE_QNOVO_REGEXP = """^\s*VOLTAGE_QNOVO=(\d+)""".toRegex(RegexOption.MULTILINE)
    private val CURRENT_NOW_REGEXP = """^\s*CURRENT_NOW=(-?\d+)""".toRegex(RegexOption.MULTILINE)
    // Regex for CURRENT_QNOVO
    private val CURRENT_QNOVO_REGEXP = """^\s*CURRENT_NOW=(-?\d+)""".toRegex(RegexOption.MULTILINE)
    // Regex for CONSTANT_CHARGE_CURRENT_MAX
    private val CONSTANT_CHARGE_CURRENT_MAX_REGEXP = """^\s*CONSTANT_CHARGE_CURRENT_MAX=(\d+)""".toRegex(RegexOption.MULTILINE)
    private val TEMP_REGEXP = """^\s*TEMP=(\d+)""".toRegex(RegexOption.MULTILINE)
    // Regex for remaining 'acc -i' values
    private val TECHNOLOGY_REGEXP = """^\s*TECHNOLOGY=([a-zA-Z\-]+)""".toRegex(RegexOption.MULTILINE)
    private val STEP_CHARGING_ENABLED_REGEXP = """^\s*STEP_CHARGING_ENABLED=([01])""".toRegex(RegexOption.MULTILINE)
    private val SW_JEITA_ENABLED_REGEXP = """^\s*SW_JEITA_ENABLED=([01])""".toRegex(RegexOption.MULTILINE)
    private val TAPER_CONTROL_ENABLED_REGEXP = """^\s*TAPER_CONTROL_ENABLED=([01])""".toRegex(RegexOption.MULTILINE)
    // CHARGE_DISABLE is true when ACC disables charging due to conditions
    private val CHARGE_DISABLE_REGEXP = """^\s*TAPER_CONTROL_ENABLED=([01])""".toRegex(RegexOption.MULTILINE)
    // CHARGE_DONE is true when the battery is done charging.
    private val CHARGE_DONE_REGEXP = """^\s*CHARGE_DONE=([01])""".toRegex(RegexOption.MULTILINE)
    private val PARALLEL_DISABLE_REGEXP = """^\s*PARALLEL_DISABLE=([01])""".toRegex(RegexOption.MULTILINE)
    private val SET_SHIP_MODE_REGEXP = """^\s*SET_SHIP_MODE=([01])""".toRegex(RegexOption.MULTILINE)
    private val DIE_HEALTH_REGEXP = """^\s*DIE_HEALTH=([a-zA-Z]+)""".toRegex(RegexOption.MULTILINE)
    private val RERUN_AICL_REGEXP = """^\s*RERUN_AICL=([01])""".toRegex(RegexOption.MULTILINE)
    private val DP_DM_REGEXP = """^\s*DP_DM=(\d+)""".toRegex(RegexOption.MULTILINE)
    private val CHARGE_CONTROL_LIMIT_MAX_REGEXP = """^\s*CHARGE_CONTROL_LIMIT_MAX=(\d+)""".toRegex(RegexOption.MULTILINE)
    private val CHARGE_CONTROL_LIMIT_REGEXP = """^\s*CHARGE_CONTROL_LIMIT=(\d+)""".toRegex(RegexOption.MULTILINE)
    private val CHARGE_COUNTER_REGEXP = """^\s*CHARGE_COUNTER=(\d+)""".toRegex(RegexOption.MULTILINE)
    private val INPUT_CURRENT_MAX_REGEXP = """^\s*INPUT_CURRENT_MAX=(\d+)""".toRegex(RegexOption.MULTILINE)
    private val CYCLE_COUNT_REGEXP = """^\s*CYCLE_COUNT=(\d+)""".toRegex(RegexOption.MULTILINE)

    override suspend fun getBatteryInfo(): BatteryInfo = withContext(Dispatchers.IO) {
        val info =  Shell.su("acc-en -i").exec().out.joinToString(separator = "\n")

        BatteryInfo(
            NAME_REGEXP.find(info)?.destructured?.component1() ?: STRING_UNKNOWN,
            INPUT_SUSPEND_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull().let { // If r == true (input is suspended)
                it == 0
            },
            STATUS_REGEXP.find(info)?.destructured?.component1() ?: STRING_DISCHARGING,
            HEALTH_REGEXP.find(info)?.destructured?.component1() ?: STRING_UNKNOWN,
            PRESENT_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1,
            CHARGE_TYPE_REGEXP.find(info)?.destructured?.component1() ?: STRING_UNKNOWN,
            CAPACTIY_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1,
            CHARGER_TEMP_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull()?.let { it/10 } ?: -1,
            CHARGER_TEMP_MAX_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull()?.let { it/10 } ?: -1,
            INPUT_CURRENT_LIMITED_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull().let {
                it == 0
            },
            (VOLTAGE_NOW_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1).toFloat(),
            VOLTAGE_MAX_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1,
            VOLTAGE_QNOVO_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1,
            (CURRENT_NOW_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1).toFloat(),
            CURRENT_QNOVO_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1,
            CONSTANT_CHARGE_CURRENT_MAX_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1,
            TEMP_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull()?.let { it/10 } ?: -1,
            TECHNOLOGY_REGEXP.find(info)?.destructured?.component1() ?: STRING_UNKNOWN,
            STEP_CHARGING_ENABLED_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull().let {
                it == 0
            },
            SW_JEITA_ENABLED_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull().let {
                it == 0
            },
            TAPER_CONTROL_ENABLED_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull().let {
                it == 0
            },
            CHARGE_DISABLE_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull().let {
                it == 0
            },
            CHARGE_DONE_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull().let {
                it == 0
            },
            PARALLEL_DISABLE_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull().let {
                it == 0
            },
            SET_SHIP_MODE_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull().let {
                it == 0
            },
            DIE_HEALTH_REGEXP.find(info)?.destructured?.component1() ?: STRING_UNKNOWN,
            RERUN_AICL_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull().let {
                it == 0
            },
            DP_DM_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull().let {
                it == 0
            },
            CHARGE_CONTROL_LIMIT_MAX_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1,
            CHARGE_CONTROL_LIMIT_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1,
            INPUT_CURRENT_MAX_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1,
            CYCLE_COUNT_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1
        )
    }

    override suspend fun isBatteryCharging(): Boolean = withContext(Dispatchers.IO) {
        STATUS_REGEXP
            .find(
                Shell.su("acc-en -i").exec().out.joinToString("\n")
            )?.destructured?.component1() == STRING_CHARGING
    }

    override suspend fun isAccdRunning(): Boolean = withContext(Dispatchers.IO) {
        Shell.su("acc-en -D").exec().out.find { it.contains("accd is running") } != null
    }

    override suspend fun abcStartDaemon(): Boolean = withContext(Dispatchers.IO) {
        Shell.su("acc-en -D start").exec().isSuccess
    }

    override suspend fun abcRestartDaemon(): Boolean = withContext(Dispatchers.IO) {
        Shell.su("acc-en -D restart").exec().isSuccess
    }

    override suspend fun abcStopDaemon(): Boolean = withContext(Dispatchers.IO) {
        Shell.su("acc-en -D stop").exec().isSuccess
    }

    //Charging switches
    override suspend fun listChargingSwitches(): List<String> = withContext(Dispatchers.IO) {
        val res = Shell.su("acc-en -s s:").exec()

        if(res.isSuccess)
            res.out.map { it.trim() }.filter { it.isNotEmpty() }
        else
            emptyList()
    }

    override suspend fun testChargingSwitch(chargingSwitch: String?): Int = withContext(Dispatchers.IO) {
        Shell.su("acc-en -t${chargingSwitch?.let{" $it"} ?: ""}").exec().code
    }

    override fun getCurrentChargingSwitch(config: String): String? {
        return SWITCH.find(readConfigToString())?.destructured?.component1()?.trim()?.ifBlank { null }
    }

    override fun isPrioritizeBatteryIdleMode(config: String): Boolean {
        return PRIORITIZE_BATTERY_IDLE.find(config)?.destructured?.component1()?.toBoolean() ?: false
    }

    override suspend fun setChargingLimitForOneCharge(limit: Int): Boolean = withContext(Dispatchers.IO) {
        Shell.su("(acc -f $limit &) &").exec().isSuccess
    }

    val BATTERY_IDLE_SUPPORTED = """^\s*-\s*battIdleMode=true""".toPattern(Pattern.MULTILINE)
    override suspend fun isBatteryIdleSupported(): Pair<Int, Boolean> = withContext(Dispatchers.IO) {
        val res = Shell.su("acc-en -t --").exec()
        Pair(
            res.code,
            BATTERY_IDLE_SUPPORTED.matcher(res.out.joinToString("\n")).find()
        )
    }

    //Update config part:

    /**
     * Function takes in AccConfig file and will apply it.
     * @param accConfig Configuration file to apply.
     * @return ConfigUpdateResult data class.
     */
    override suspend fun updateAccConfig(accConfig: AccConfig): ConfigUpdateResult {
        return ConfigUpdater(accConfig)
            .execute(this)
    }

    override fun getUpdateResetUnpluggedCommand(resetUnplugged: Boolean) = "acc-en -s resetBsOnUnplug $resetUnplugged"

    override fun getUpdateResetOnPauseCommand(resetOnPause: Boolean) = "acc-en -s resetBsOnPause $resetOnPause"

    override fun getUpdateAccCoolDownCommand(charge: Int?, pause: Int?): String {
        return if(charge != null && pause != null)
           "acc-en -s coolDownRatio $charge/$pause"
        else
           "acc-en -s coolDownRatio"
    }

    override fun getUpdateAccCapacityCommand(shutdown: Int, coolDown: Int, resume: Int, pause: Int): String = "acc-en -s capacity $shutdown,$coolDown,$resume-$pause"

    override fun getUpdateAccTemperatureCommand(coolDownTemperature: Int, temperatureMax: Int, wait: Int): String = "acc-en -s temperature ${coolDownTemperature}-${temperatureMax}_$wait"

    override fun getUpdateAccVoltControlCommand(voltFile: String?, voltMax: Int?): String =
        if(voltFile != null && voltMax != null)
            "acc-en --set maxChargingVoltage $voltFile:$voltMax"
        else if(voltMax != null)
            "acc-en --set maxChargingVoltage $voltMax"
        else
            "acc-en --set maxChargingVoltage"

    override fun getUpdateAccCurrentMaxCommand(currMax: Int?): String = "" //NOT SUPPORTED

    override fun getUpdateAccOnBootExitCommand(enabled: Boolean): String = "acc-en -s onBootExit $enabled"

    override fun getUpdateAccOnBootCommand(command: String?): String = "acc-en -s applyOnBoot${command?.let{ " $it" } ?: ""}"


    override fun getUpdateAccOnPluggedCommand(command: String?) : String = "acc-en -s applyOnPlug${command?.let{ " $it" } ?: ""}"

    override fun getUpdateAccChargingSwitchCommand(switch: String?) : String =
        if (switch.isNullOrBlank())
            "acc-en -s s-"
        else
            "acc-en -s s $switch"

    override fun getUpgradeCommand(version: String) = "acc-en --upgrade $version"

    override fun getUpdatePrioritizeBatteryIdleModeCommand(enabled: Boolean): String = "acc-en --set prioritizeBattIdleMode $enabled"

    override fun getAddChargingSwitchCommand(switch: String): String = "acc --set chargingSwitch $switch"
}