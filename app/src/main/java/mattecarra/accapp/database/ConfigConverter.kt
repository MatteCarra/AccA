package mattecarra.accapp.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.ProfileEnables

object ConfigConverter
{

    @TypeConverter
    @JvmStatic
    fun fromEnables(enables: ProfileEnables): String
    {
        return Gson().toJson(enables)
    }

    @TypeConverter
    @JvmStatic
    fun toEnables(enables: String): ProfileEnables
    {
        return Gson().fromJson(enables, ProfileEnables::class.java)
    }

    @TypeConverter
    @JvmStatic
    fun fromScripts(scripts: List<Int>?): String?
    {
        return Gson().toJson(scripts)
    }

    @TypeConverter
    @JvmStatic
    fun toScripts(scripts: String?): List<Int>?
    {
        return Gson().fromJson(scripts, object : TypeToken<List<Int>>()
        {}.type)
    }

    @TypeConverter
    @JvmStatic
    fun fromConfigCapacity(configCapacity: AccConfig.ConfigCapacity): String
    {
        return Gson().toJson(configCapacity)
    }

    @TypeConverter
    @JvmStatic
    fun toConfigCapacity(configCapacity: String): AccConfig.ConfigCapacity
    {
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
    fun fromConfigCoolDown(configCoolDown: AccConfig.ConfigCoolDown?) : String {
        return Gson().toJson(configCoolDown)
    }

    @TypeConverter
    @JvmStatic
    fun toConfigCoolDown(configCoolDown: String) : AccConfig.ConfigCoolDown? {
        return Gson().fromJson(configCoolDown, AccConfig.ConfigCoolDown::class.java)
    }
}