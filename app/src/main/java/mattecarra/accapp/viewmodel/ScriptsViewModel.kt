package mattecarra.accapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mattecarra.accapp.database.AccaRoomDatabase
import mattecarra.accapp.database.ScriptDao
import mattecarra.accapp.models.AccaScript

class ScriptsViewModel(application: Application) : AndroidViewModel(application)
{
    private val mListLiveData: LiveData<List<AccaScript>>
    private val mScriptDao: ScriptDao

    init
    {
        val accaDatabase = AccaRoomDatabase.getDatabase(application)
        mScriptDao = accaDatabase.scriptsDao()
        mListLiveData = mScriptDao.getAllScripts()
    }

    fun deleteScript(script: AccaScript) = viewModelScope.launch {
        mScriptDao.delete(script)
    }

    fun updateScript(script: AccaScript) = viewModelScope.launch {
        mScriptDao.update(script)
    }

    fun copyScript(script: AccaScript) = viewModelScope.launch {
        script.uid = 0
        mScriptDao.insert(script)
    }

    suspend fun getScripts(): List<AccaScript>
    {
        return mScriptDao.getScripts()
    }

    suspend fun getScriptById(id: Int): AccaScript?
    {
        //return if (id>0) mScriptDao.getScriptById(id) else null
        return mScriptDao.getScriptById(id)
    }

    fun getLiveData(): LiveData<List<AccaScript>>
    {
        return mListLiveData
    }
}
