package mattecarra.accapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mattecarra.accapp.models.*

@Database(entities = [AccaProfile::class, ScheduleProfile::class, AccaScript::class], version = 11)
@TypeConverters(ConfigConverter::class)
abstract class AccaRoomDatabase : RoomDatabase()
{
    abstract fun profileDao(): ProfileDao
    abstract fun scriptsDao(): ScriptDao
    abstract fun scheduleDao(): ScheduleDao

    companion object
    {
        @Volatile
        private var INSTANCE: AccaRoomDatabase? = null

        const val DATABASE_NAME = "acca_database"

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        }

        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE profiles_table ADD COLUMN prioritizeBatteryIdleMode INTEGER NOT NULL DEFAULT 0");
            }
        }

        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS schedules_table (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `configCapacity` TEXT NOT NULL, `configVoltage` TEXT NOT NULL, `configTemperature` TEXT NOT NULL, `configOnBoot` TEXT, `configOnPlug` TEXT, `configCoolDown` TEXT, `configResetUnplugged` INTEGER NOT NULL, `configChargeSwitch` TEXT, `prioritizeBatteryIdleMode` INTEGER NOT NULL)");
            }
        }

        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE schedules_table ADD COLUMN `scheduleName` TEXT NOT NULL DEFAULT 'Default schedule'");
            }
        }

        private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE profiles_table ADD COLUMN `configResetBsOnPause` INTEGER NOT NULL DEFAULT 0");
            }
        }

        private val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE schedules_table ADD COLUMN `configResetBsOnPause` INTEGER NOT NULL DEFAULT 0");

                database.execSQL("ALTER TABLE profiles_table ADD COLUMN `configCurrMax` INTEGER DEFAULT NULL");
                database.execSQL("ALTER TABLE schedules_table ADD COLUMN `configCurrMax` INTEGER DEFAULT NULL");
            }
        }

        private val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE profiles_table ADD COLUMN `configIsAutomaticSwitchingEnabled` INTEGER NOT NULL DEFAULT 1");
                database.execSQL("ALTER TABLE schedules_table ADD COLUMN `configIsAutomaticSwitchingEnabled` INTEGER NOT NULL DEFAULT 1");
            }
        }

        private val MIGRATION_8_9: Migration = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS scripts_table (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `scName` TEXT NOT NULL, `scDescription` TEXT NOT NULL, `scBody` TEXT NOT NULL, `scOutput` TEXT NOT NULL, `scExitCode` INTEGER NOT NULL)");
            }
        }

        private val MIGRATION_9_10: Migration = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase)
            {
                // Tested!
                database.execSQL("INSERT INTO scripts_table (scName, scDescription, scBody, scOutput, scExitCode) VALUES (\"CoolDown Temp after 40%\", \"temperature=(cooldown_temp max_temp max_temp_pause shutdown_temp)\", \"acca -s cooldown_temp=40 max_temp=45 max_temp_pause=90\", \"\", 0);");
                database.execSQL("INSERT INTO scripts_table (scName, scDescription, scBody, scOutput, scExitCode) VALUES (\"Charge to 90%\", \"capacity=(shutdown_capacity cooldown_capacity resume_capacity pause_capacity capacity_freeze2)\", \"acca -s shutdown_capacity=10 resume_capacity=85 pause_capacity=90\", \"\", 0);");
                database.execSQL("INSERT INTO scripts_table (scName, scDescription, scBody, scOutput, scExitCode) VALUES (\"Reset current Config\", \"-s|--set r|--reset Restore default config\", \"acca -s r\", \"\", 0);");
                database.execSQL("INSERT INTO scripts_table (scName, scDescription, scBody, scOutput, scExitCode) VALUES (\"Print current config\", \"-s|--set e.g., acc -s\", \"acca -s\", \"\", 0);");
                database.execSQL("INSERT INTO scripts_table (scName, scDescription, scBody, scOutput, scExitCode) VALUES (\"Test charging switches\", \"-t|--test [file] Test charging switches from a file (default: /dev/.vr25/acc/ch-switches)\", \"acca -t\", \"\", 0);");
                database.execSQL("INSERT INTO scripts_table (scName, scDescription, scBody, scOutput, scExitCode) VALUES (\"List charging switches\", \"-s|--set s:|chargingSwitch: e.g, acc -s s:\", \"acca -s s:\", \"\", 0);");
                database.execSQL("INSERT INTO scripts_table (scName, scDescription, scBody, scOutput, scExitCode) VALUES (\"Disable charging\", \"-d|--disable [#%, #s, #m or #h (optional)]\", \"acca -d\", \"\", 0);");
                database.execSQL("INSERT INTO scripts_table (scName, scDescription, scBody, scOutput, scExitCode) VALUES (\"Enable charging\", \"-e|--enable [#%, #s, #m or #h (optional)]\", \"acca -e\", \"\", 0);");
                database.execSQL("INSERT INTO scripts_table (scName, scDescription, scBody, scOutput, scExitCode) VALUES (\"Battery Info\", \"-i|--info [case insensitive egrep regex (default: .)]\", \"acca -i\", \"\", 0);");
                database.execSQL("INSERT INTO scripts_table (scName, scDescription, scBody, scOutput, scExitCode) VALUES (\"ACC Version\", \"-v|--version  Print acc version and version code\", \"acca -v\", \"\", 0);");
            }
        }

        private val MIGRATION_10_11: Migration = object : Migration(10, 11)
        {
            override fun migrate(database: SupportSQLiteDatabase)
            {
                database.execSQL("ALTER TABLE profiles_table ADD COLUMN `pEnables` TEXT NOT NULL");
                database.execSQL("ALTER TABLE profiles_table ADD COLUMN `pScripts` TEXT");
            }
        }

        fun getDatabase(context: Context): AccaRoomDatabase
        {
            val tempInstance = INSTANCE
            if (tempInstance != null) return tempInstance

            synchronized(this) {
                // Create database instance here
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AccaRoomDatabase::class.java,
                    DATABASE_NAME
                ).addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11
                    ).addCallback(object : Callback()
                    {
                        override fun onCreate(db: SupportSQLiteDatabase)
                        {
                                super.onCreate(db)
                                prepopulateDb(getDatabase(context))
                            }
                        }).build()

                return INSTANCE as AccaRoomDatabase
            }
        }

        private fun prepopulateDb(db: AccaRoomDatabase) = CoroutineScope(Dispatchers.Default).launch {

            db.profileDao().insert(
                AccaProfile(
                    0, "Default Custom",
                    AccConfig(
                        configCapacity = AccConfig.ConfigCapacity(5, 70, 80),
                        configTemperature = AccConfig.ConfigTemperature(40, 45, 90)
                    ),
                    ProfileEnables(eCapacity = true, eVoltage = true, eTemperature = true),
                )
            )

            db.profileDao().insert(
                AccaProfile(
                    0, "Charge to 90%", AccConfig(
                        configCapacity = AccConfig.ConfigCapacity(5, 85, 90),
                        configTemperature = AccConfig.ConfigTemperature(40, 45, 90)
                    ), ProfileEnables(eCapacity = true, eTemperature = true)
                )
            )

            db.profileDao().insert(
                AccaProfile(
                    0, "Cool down after 60%", AccConfig(
                        configCapacity = AccConfig.ConfigCapacity(5, 70, 80),
                        configTemperature = AccConfig.ConfigTemperature(40, 45, 90),
                        configCoolDown = AccConfig.ConfigCoolDown(60, 50, 10)
                    ), ProfileEnables(eCapacity = true, eTemperature = true, eCoolDown = true)
                )
            )

            db.scriptsDao().insert(AccaScript(0, "CoolDown Temp after 40%",
                "temperature=(cooldown_temp max_temp max_temp_pause shutdown_temp)",
                "acca -s cooldown_temp=40 max_temp=45 max_temp_pause=90 ",
                "", 0)
            )

            db.scriptsDao().insert(AccaScript(0, "Charge to 90%",
                "capacity=(shutdown_capacity cooldown_capacity resume_capacity pause_capacity capacity_freeze2)",
                "acca -s shutdown_capacity=10 resume_capacity=85 pause_capacity=90",
                "", 0)
            )

            db.scriptsDao().insert(AccaScript(0, "Reset current config",
                "-s|--set r|--reset Restore default config",
                "acca -s --reset",
                "", 0)
            )

            db.scriptsDao().insert(AccaScript(0, "Print current config",
                "-s|--set e.g., acc -s",
                "acca -s",
                "", 0)
            )

            db.scriptsDao().insert(AccaScript(0, "Test charging switches",
                "-t|--test [file] Test charging switches from a file (default: /dev/.vr25/acc/ch-switches)",
                "acca -t",
                "", 0)
            )

            db.scriptsDao().insert(AccaScript(0, "List charging switches",
                "-s|--set s:|chargingSwitch:  e.g., acc -s s:",
                "acca -s s:",
                "", 0)
            )

            db.scriptsDao().insert(AccaScript(0, "Disable charging",
                "-d|--disable [#%, #s, #m or #h (optional)]",
                "acca -d",
                "", 0)
            )

            db.scriptsDao().insert(AccaScript(0, "Enable charging",
                "-e|--enable [#%, #s, #m or #h (optional)]",
                "acca -e",
                "", 0)
            )

            db.scriptsDao().insert(AccaScript(0, "Battery Info",
                "-i|--info [case insensitive egrep regex (default: \".\")]",
                "acca -i",
                "", 0)
            )

            db.scriptsDao().insert(AccaScript(0, "ACC Version",
                "-v|--version  Print acc version and version code",
                "acca -v",
                "", 0)
            )
        }
    }
}