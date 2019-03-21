package mattecarra.accapp.utils

import androidx.lifecycle.LiveData
import mattecarra.accapp.database.AccaRoomDatabase
import mattecarra.accapp.models.ProfileEntity

class DataRepository constructor(private val accaRoomDatabase: AccaRoomDatabase) {

    private var sInstance: DataRepository
    private val mAccaDatabase: AccaRoomDatabase

    private val mObservableProfiles: LiveData<List<ProfileEntity>>

    fun get
}