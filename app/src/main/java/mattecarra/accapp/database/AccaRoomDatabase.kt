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
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.models.ScheduleProfile


@Database(entities = [AccaProfile::class, ScheduleProfile::class], version = 6)
@TypeConverters(ConfigConverter::class)
abstract class AccaRoomDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
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

        fun getDatabase(context: Context): AccaRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                // Create database instance here
                INSTANCE =
                    Room.databaseBuilder(context.applicationContext, AccaRoomDatabase::class.java, DATABASE_NAME)
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                        .addCallback(object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                prepopulateDb(getDatabase(context))
                            }
                        }).build()

                return INSTANCE as AccaRoomDatabase
            }
        }

        private fun prepopulateDb(db: AccaRoomDatabase) = CoroutineScope(Dispatchers.Default).launch {
            db.profileDao().insert(
                AccaProfile(0, "Default",
                    AccConfig(
                        AccConfig.ConfigCapacity(5, 70, 80),
                        AccConfig.ConfigVoltage(null, null),
                        AccConfig.ConfigTemperature(40, 45, 90),
                        null,
                        null,
                        null,
                        false,
                        false,
                        null,
                        false)
                )
            )
            db.profileDao().insert(
                AccaProfile(0, "Charge to 90%",
                    AccConfig(
                        AccConfig.ConfigCapacity(5, 85, 90),
                        AccConfig.ConfigVoltage(null, null),
                        AccConfig.ConfigTemperature(40, 45, 90),
                        null,
                        null,
                        null,
                        false,
                        false,
                        null,
                        false)
                )
            )
            db.profileDao().insert(
                AccaProfile(0, "Cool down after 60%",
                    AccConfig(
                        AccConfig.ConfigCapacity(5, 70, 80),
                        AccConfig.ConfigVoltage(null, null),
                        AccConfig.ConfigTemperature(40, 45, 90),
                        null,
                        null,
                        AccConfig.ConfigCoolDown(60, 50, 10),
                        false,
                        false,
                        null,
                        false)
                )
            )
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}