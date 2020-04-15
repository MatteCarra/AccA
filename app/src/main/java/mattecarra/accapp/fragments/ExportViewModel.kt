package mattecarra.accapp.fragments

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import mattecarra.accapp.database.AccaRoomDatabase
import mattecarra.accapp.database.ProfileDao
import mattecarra.accapp.models.AccaProfile

class ExportViewModel(application: Application) : AndroidViewModel(application) {

    private val mProfilesListLiveData: LiveData<List<AccaProfile>>
    private val mProfileDao: ProfileDao
    private val mExportProfileList: ArrayList<AccaProfile>

    init {
        val accaDatabase = AccaRoomDatabase.getDatabase(application)
        mProfileDao = accaDatabase.profileDao()
        mProfilesListLiveData = mProfileDao.getAllProfiles()
        mExportProfileList = ArrayList()
    }

    suspend fun getProfile(id: Int): AccaProfile? {
        return mProfileDao.getProfileById(id)
    }

    fun getProfiles() : LiveData<List<AccaProfile>> {
        return mProfilesListLiveData
    }

    fun addProfileToExport(profile: AccaProfile) {
        mExportProfileList.add(profile)
    }

    fun removeProfileToExport(profile: AccaProfile) {
        mExportProfileList.remove(profile)
    }

    fun exportProfiles() {
        // TODO: Export to a file in the internal storage, or let the user select whether they want to share it, or save to file

        var string: StringBuilder = StringBuilder()
        for (p in mExportProfileList) {
            string.append("\n" + p.profileName)
        }
        Toast.makeText(getApplication(), "Profiles: $string", Toast.LENGTH_SHORT).show()
    }
}