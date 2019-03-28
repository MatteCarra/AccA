package mattecarra.accapp.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import mattecarra.accapp.models.AccConfig

object ConfigConverter {

    @TypeConverter
    @JvmStatic
    fun fromConfigCapacity(configCapacity: AccConfig.ConfigCapacity) : String {
        return Gson().toJson(configCapacity)
    }

    @TypeConverter
    @JvmStatic
    fun toConfigCapacity(configCapacity: String) : AccConfig.ConfigCapacity {
        return Gson().fromJson(configCapacity, AccConfig.ConfigCapacity::class.java)
    }

    @TypeConverter
    @JvmStatic
    fun fromConfigVoltage(configVoltage: AccConfig.ConfigVoltage) : String {
        return Gson().toJson(configVoltage)
    }

    @TypeConverter
    @JvmStatic
    fun toConfigVoltage(configVoltage: String) : AccConfig.ConfigVoltage {
        return Gson().fromJson(configVoltage, AccConfig.ConfigVoltage::class.java)
    }

    @TypeConverter
    @JvmStatic
    fun fromConfigTemperature(configTemperature: AccConfig.ConfigTemperature) : String {
        return Gson().toJson(configTemperature)
    }

    @TypeConverter
    @JvmStatic
    fun toConfigTemperature(configTemperature: String) : AccConfig.ConfigTemperature {
        return Gson().fromJson(configTemperature, AccConfig.ConfigTemperature::class.java)
    }

    @TypeConverter
    @JvmStatic
    fun fromConfigCoolDown(configCoolDown: AccConfig.ConfigCoolDown) : String {
        return Gson().toJson(configCoolDown)
    }

    @TypeConverter
    @JvmStatic
    fun toConfigCoolDown(configCoolDown: String) : AccConfig.ConfigCoolDown {
        return Gson().fromJson(configCoolDown, AccConfig.ConfigCoolDown::class.java)
    }
}