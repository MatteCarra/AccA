package mattecarra.accapp.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

    @Entity(tableName = "profiles_table")
    data class AccaProfile(

        @PrimaryKey(autoGenerate = true) val uid: Int,
        var profileName: String,
        @Embedded var accConfig: AccConfig,
        var pEnables: ProfileEnables,
        var pScripts: List<Int>? = null, // contain uid from script_table
    ) : Serializable

    //----------------------------------------------------------------------
    // Enable\disable options in profile for greater flexibility !)

    data class ProfileEnables(
        var eCapacity: Boolean = true,
        var eVoltage: Boolean = false,
        var eCurrMax: Boolean = false,
        var eTemperature: Boolean = true,
        var eCoolDown: Boolean = false,
        var eScripts: Boolean = false,
        var eRunOnBoot: Boolean = false,
        var eRunOnPlug: Boolean = false,
        var eChargingSwitch: Boolean = true, // temporary always ON
    ) : Serializable

