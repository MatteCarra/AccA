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


@Database(entities = [AccaProfile::class], version = 2)
@TypeConverters(ConfigConverter::class)
abstract class AccaRoomDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    // abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AccaRoomDatabase? = null

        const val DATABASE_NAME = "acca_database"

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {}
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
                        .addMigrations(MIGRATION_1_2)
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