package mattecarra.accapp.activities

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import mattecarra.accapp.database.AccaRoomDatabase
import mattecarra.accapp.database.ProfileDao
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.models.ProfileExportItem

class ImportExportActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val mClipboardProfiles: MutableLiveData<List<AccaProfile>> = MutableLiveData()
    private val mProfileExportItems: MutableLiveData<ArrayList<ProfileExportItem>> = MutableLiveData()
    private val mProfilesListLiveData: LiveData<List<AccaProfile>>
    private val mProfileDao: ProfileDao

    init {
        val accaDatabase = AccaRoomDatabase.getDatabase(application)
        mProfileDao = accaDatabase.profileDao()
        mProfilesListLiveData = mProfileDao.getAllProfiles()
    }

    /**
     * Create the new ProfileExportItem for use in the recycler
     */
    fun createProfileExportItems(profiles: List<AccaProfile>) {
        val exportProfiles = ArrayList<ProfileExportItem>()

        for (profile in profiles) {
            var exPro = ProfileExportItem(profile, profile.profileName)
            exportProfiles.add(exPro)
        }

        mProfileExportItems.value = exportProfiles
    }

    fun getProfileExportItems() : MutableLiveData<ArrayList<ProfileExportItem>> {
        return mProfileExportItems
    }

    fun getProfiles() : LiveData<List<AccaProfile>> {
        return mProfilesListLiveData
    }

    fun setClipboardProfiles(profiles: List<AccaProfile>) {
        mClipboardProfiles.value = profiles
    }

    fun getClipboardProfiles() : LiveData<List<AccaProfile>> {
        return mClipboardProfiles
    }
}