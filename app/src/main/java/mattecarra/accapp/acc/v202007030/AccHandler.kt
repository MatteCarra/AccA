package mattecarra.accapp.acc.v202007030

import androidx.annotation.WorkerThread
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mattecarra.accapp.acc.ConfigUpdateResult
import mattecarra.accapp.acc.ConfigUpdater
import mattecarra.accapp.acc._interface.AccInterfaceV2
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.BatteryInfo
import java.io.IOException
import java.util.regex.Pattern

// String resources
private const val STRING_UNKNOWN = "Unknown"
private const val STRING_NOT_CHARGING = "Not charging"
private const val STRING_DISCHARGING = "Discharging"
private const val STRING_CHARGING = "Charging"

open class AccHandler: AccInterfaceV2 {

    // RegEx Values
    // Capacity
    val SHUTDOWN_CAPACITY_REGEXP = """^\s*shutdown_capacity=(\d*)""".toRegex(RegexOption.MULTILINE)
    val COOLDOWN_CAPACITY_REGEXP = """^\s*cooldown_capacity=(\d*)""".toRegex(RegexOption.MULTILINE)
    val RESUME_CAPACITY_REGEXP = """^\s*resume_capacity=(\d*)""".toRegex(RegexOption.MULTILINE)
    val PAUSE_CAPACITY_REGEXP = """^\s*pause_capacity=(\d*)""".toRegex(RegexOption.MULTILINE)

    // Cool Down
    val COOLDOWN_TEMP_REGEXP = """^\s*cooldown_temp=(\d*)""".toRegex(RegexOption.MULTILINE)
    val MAX_TEMP_REGEXP = """^\s*max_temp=(\d*)""".toRegex(RegexOption.MULTILINE)
    val MAX_TEMP_PAUSE_REGEXP = """^\s*max_temp_pause=(\d*)""".toRegex(RegexOption.MULTILINE)

    val COOLDOWN_CHARGE_REGEXP = """^\s*cooldown_charge=(\d*)""".toRegex(RegexOption.MULTILINE)
    val COOLDOWN_PAUSE_REGEXP  = """^\s*cooldown_pause=(\d*)""".toRegex(RegexOption.MULTILINE)

    // Plugged/Pause
    val RESET_UNPLUGGED_CONFIG_REGEXP = """^\s*reset_batt_stats_on_unplug=(true|false)""".toRegex(RegexOption.MULTILINE)
    val RESET_ON_PAUSE_CONFIG_REGEXP = """^\s*reset_batt_stats_on_pause=(true|false)""".toRegex(RegexOption.MULTILINE)
    val ON_BOOT = """^\s*apply_on_boot=((?:(?!#).)*)""".toRegex(RegexOption.MULTILINE)
    val ON_PLUGGED = """^\s*apply_on_plug=((?:(?!#).)*)""".toRegex(RegexOption.MULTILINE)

    val MAX_CHARGING_VOLTAGE = """^\s*max_charging_voltage=(\d*)""".toRegex(RegexOption.MULTILINE)
    val MAX_CHARGING_CURRENT = """^\s*max_charging_current=(\d*)""".toRegex(RegexOption.MULTILINE)

    val SWITCH = """^\s*charging_switch=((?:(?!#).)*)""".toRegex(RegexOption.MULTILINE)
    val PRIORITIZE_BATTERY_IDLE = """^\s*prioritize_batt_idle_mode=(true|false)""".toRegex(RegexOption.MULTILINE)

    @WorkerThread
    override fun parseConfig(config: String): AccConfig {
        val capacityShutdown = SHUTDOWN_CAPACITY_REGEXP.find(config)!!.destructured.component1()
        val capacityCoolDown = COOLDOWN_CAPACITY_REGEXP.find(config)!!.destructured.component1()
        val capacityResume   = RESUME_CAPACITY_REGEXP.find(config)!!.destructured.component1()
        val capacityPause    = PAUSE_CAPACITY_REGEXP.find(config)!!.destructured.component1()

        val temperatureCooldown = COOLDOWN_TEMP_REGEXP.find(config)!!.destructured.component1()
        val temperatureMax      = MAX_TEMP_REGEXP.find(config)!!.destructured.component1()
        val waitSeconds         = MAX_TEMP_PAUSE_REGEXP.find(config)!!.destructured.component1()

        val coolDownChargeSeconds = COOLDOWN_CHARGE_REGEXP.find(config)?.destructured?.component1()?.toIntOrNull()
        val coolDownPauseSeconds = COOLDOWN_PAUSE_REGEXP.find(config)?.destructured?.component1()?.toIntOrNull()

        val maxChargingVoltage = MAX_CHARGING_VOLTAGE.find(config)?.destructured?.component1()
        val maxChargingCurrent = MAX_CHARGING_CURRENT.find(config)?.destructured?.component1()

        return AccConfig(
            AccConfig.ConfigCapacity(capacityShutdown.toIntOrNull() ?: 0, capacityResume.toInt(), capacityPause.toInt()),
            AccConfig.ConfigVoltage(null, maxChargingVoltage?.toIntOrNull()),
            maxChargingCurrent?.toIntOrNull(),
            AccConfig.ConfigTemperature(temperatureCooldown.toIntOrNull() ?: 90,
                temperatureMax.toIntOrNull() ?: 95,
                waitSeconds.toIntOrNull() ?: 90),
            getOnBoot(config),
            getOnPlugged(config),
            if(coolDownChargeSeconds != null && coolDownPauseSeconds != null)
                AccConfig.ConfigCoolDown(capacityCoolDown.toInt(), coolDownChargeSeconds, coolDownPauseSeconds)
            else null,
            getResetUnplugged(config),
            getResetOnPause(config),
            getCurrentChargingSwitch(config),
            isPrioritizeBatteryIdleMode(config)
        )
    }

    override suspend fun readConfig(): AccConfig = withContext(Dispatchers.IO) {
        parseConfig(readConfigToString())
    }

    override suspend fun readDefaultConfig(): AccConfig = withContext(Dispatchers.IO) {
        val defaultConfig = Shell.su("/dev/acca --set --print-default").exec().out.joinToString(separator = "\n")

        parseConfig(defaultConfig)
    }

    @Throws(IOException::class)
    @WorkerThread
    open fun readConfigToString(): String {
        return Shell.su("/dev/acca --set --print").exec().out.joinToString(separator = "\n")
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
        val res = Shell.su("/dev/acca -v :").exec()

        if(res.isSuccess)
            res.out.filter { it.isNotEmpty() }
        else
            emptyList()
    }

    override suspend fun resetBatteryStats(): Boolean = withContext(Dispatchers.IO) {
        Shell.su("/dev/acca -R").exec().isSuccess
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
    private val VOLTAGE_NOW_REGEXP = """^\s*VOLTAGE_NOW=([+-]?([0-9]*[.])?[0-9]+)""".toRegex(RegexOption.MULTILINE)
    // Regex for VOLTAGE_MAX
    private val VOLTAGE_MAX_REGEXP = """^\s*VOLTAGE_MAX=(\d+)""".toRegex(RegexOption.MULTILINE)
    // Regex for VOLTAGE_QNOVO
    private val VOLTAGE_QNOVO_REGEXP = """^\s*VOLTAGE_QNOVO=(\d+)""".toRegex(RegexOption.MULTILINE)
    private val CURRENT_NOW_REGEXP = """^\s*CURRENT_NOW=([+-]?([0-9]*[.])?[0-9]+)""".toRegex(RegexOption.MULTILINE)
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

    private val POWER_NOW_REGEXP = """^\s*POWER_NOW=([+-]?([0-9]*[.])?[0-9]+)""".toRegex(RegexOption.MULTILINE)

    override suspend fun getBatteryInfo(): BatteryInfo = withContext(Dispatchers.IO) {
        val info =  Shell.su("/dev/acca -i").exec().out.joinToString(separator = "\n")

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
            VOLTAGE_NOW_REGEXP.find(info)?.destructured?.component1()?.toFloatOrNull() ?: 0f,
            VOLTAGE_MAX_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1,
            VOLTAGE_QNOVO_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1,
            CURRENT_NOW_REGEXP.find(info)?.destructured?.component1()?.toFloatOrNull() ?: 0f,
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
            CYCLE_COUNT_REGEXP.find(info)?.destructured?.component1()?.toIntOrNull() ?: -1,
            POWER_NOW_REGEXP.find(info)?.destructured?.component1()?.toFloatOrNull() ?: 0f
        )
    }

    override suspend fun isBatteryCharging(): Boolean = withContext(Dispatchers.IO) {
        STATUS_REGEXP
            .find(
                Shell.su("/dev/acca -i").exec().out.joinToString("\n")
            )?.destructured?.component1() == STRING_CHARGING
    }

    override suspend fun isAccdRunning(): Boolean = withContext(Dispatchers.IO) {
        val code = Shell.su("/dev/acca -D").exec().code
        code == 0 || code == 8
    }

    override suspend fun abcStartDaemon(): Boolean = withContext(Dispatchers.IO) {
        Shell.su("/dev/acca -D start").exec().isSuccess
    }

    override suspend fun abcRestartDaemon(): Boolean = withContext(Dispatchers.IO) {
        Shell.su("/dev/acca -D restart").exec().isSuccess
    }

    override suspend fun abcStopDaemon(): Boolean = withContext(Dispatchers.IO) {
        Shell.su("/dev/acca -D stop").exec().isSuccess
    }

    //Charging switches
    override suspend fun listChargingSwitches(): List<String> = withContext(Dispatchers.IO) {
        val res = Shell.su("/dev/acca -s s:").exec()

        if(res.isSuccess)
            res.out.map { it.trim() }.filter { it.isNotEmpty() }
        else
            emptyList()
    }

    override suspend fun testChargingSwitch(chargingSwitch: String?): Int = withContext(Dispatchers.IO) {
        Shell.su("/dev/acca -t${chargingSwitch?.let{" $it"} ?: ""}").exec().code
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
        val res = Shell.su("/dev/acca -t --").exec()
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

    override fun getUpdateResetUnpluggedCommand(resetUnplugged: Boolean) = "/dev/acca -s reset_batt_stats_on_unplug=$resetUnplugged"

    override fun getUpdateResetOnPauseCommand(resetOnPause: Boolean) = "/dev/acca -s reset_batt_stats_on_pause=$resetOnPause"

    override fun getUpdateAccCoolDownCommand(charge: Int?, pause: Int?): String = "/dev/acca -s cooldown_charge=${charge?.toString().orEmpty()} cooldown_pause=${pause?.toString().orEmpty()}"

    override fun getUpdateAccCapacityCommand(shutdown: Int, coolDown: Int, resume: Int, pause: Int): String = "/dev/acca -s shutdown_capacity=$shutdown cooldown_capacity=$coolDown resume_capacity=$resume pause_capacity=$pause"

    override fun getUpdateAccTemperatureCommand(coolDownTemperature: Int, temperatureMax: Int, wait: Int): String = "/dev/acca -s cooldown_temp=${coolDownTemperature} max_temp=${temperatureMax} max_temp_pause=$wait"

    override fun getUpdateAccVoltControlCommand(voltFile: String?, voltMax: Int?): String = "/dev/acca --set --voltage ${voltMax?.toString().orEmpty()}"

    override fun getUpdateAccCurrentMaxCommand(currMax: Int?): String = "/dev/acca --set --current ${currMax?.toString().orEmpty()}"

    override fun getUpdateAccOnBootExitCommand(enabled: Boolean): String = "" //Not supported

    override fun getUpdateAccOnBootCommand(command: String?): String = "/dev/acca -s \"apply_on_boot=${command.orEmpty()}\""


    override fun getUpdateAccOnPluggedCommand(command: String?) : String = "/dev/acca -s \"apply_on_plug=${command.orEmpty()}\""

    override fun getUpdateAccChargingSwitchCommand(switch: String?) : String = "/dev/acca -s \"charging_switch=${switch.orEmpty()}\""

    override fun getUpgradeCommand(version: String) = "/dev/acca --upgrade $version"

    override fun getUpdatePrioritizeBatteryIdleModeCommand(enabled: Boolean): String = "/dev/acca --set prioritize_batt_idle_mode=$enabled"

    override fun getAddChargingSwitchCommand(switch: String): String = getUpdateAccChargingSwitchCommand(switch)
}