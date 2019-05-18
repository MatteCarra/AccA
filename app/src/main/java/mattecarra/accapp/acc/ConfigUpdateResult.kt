package mattecarra.accapp.acc

/**
 * Data class for returning and interpreting update results when applying new values.
 */
data class ConfigUpdateResult(
    val capacityUpdateSuccessful: Boolean,
    val voltControlUpdateSuccessful: Boolean,
    val tempUpdateSuccessful: Boolean,
    val coolDownUpdateSuccessful: Boolean,
    val resetUnpluggedUpdateSuccessful: Boolean,
    val onBootUpdateSuccessful: Boolean,
    val onPluggedUpdateSuccessful: Boolean,
    val chargingSwitchUpdateSuccessful: Boolean
) {
    fun debug() {
        if(!capacityUpdateSuccessful) println("Update capacity update failed")

        if(!voltControlUpdateSuccessful) println("Volt control update failed")

        if(!tempUpdateSuccessful) println("Temp update update failed")

        if(!coolDownUpdateSuccessful) println("Cooldown update update failed")

        if(!resetUnpluggedUpdateSuccessful) println("Reset unplugged update failed")

        if(!onBootUpdateSuccessful) println("onBoot update failed")

        if(!onPluggedUpdateSuccessful) println("onPlugged update failed")

        if(!chargingSwitchUpdateSuccessful) println("Charging switch update failed")
    }

    fun isSuccessful(): Boolean {
        return capacityUpdateSuccessful && voltControlUpdateSuccessful && tempUpdateSuccessful && coolDownUpdateSuccessful && resetUnpluggedUpdateSuccessful && onBootUpdateSuccessful && onPluggedUpdateSuccessful && chargingSwitchUpdateSuccessful
    }
}
