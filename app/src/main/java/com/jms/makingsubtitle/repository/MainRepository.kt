package com.jms.makingsubtitle.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.LiveData
import com.jms.makingsubtitle.data.model.SubtitleFile
import com.jms.makingsubtitle.data.model.TimeLines
import com.jms.makingsubtitle.data.room.FileListDao
import com.jms.makingsubtitle.data.room.SubtitleFileDatabase
import com.jms.makingsubtitle.repository.MainRepository.PreferencesKeys.LEAF_TIME_MODE
import com.jms.makingsubtitle.repository.MainRepository.PreferencesKeys.THEME_MODE
import com.jms.makingsubtitle.data.datastore.LeafTimeMode
import com.jms.makingsubtitle.data.datastore.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.*

class MainRepository(
    private val db: SubtitleFileDatabase,
    private val dataStore: DataStore<Preferences>
) {

    private val dao: FileListDao get() = db.subtitleFileDao()


    fun getAllSubtitleFileList(): LiveData<List<SubtitleFile>> {
        return dao.getAllSubtitleFileList()
    }

    fun getSubtitleFileByUUID(uuid: UUID): SubtitleFile {
        return dao.getSubtitleFileByUUID(uuid)
    }

    fun getTimeLinesByUUID(uuid: UUID): LiveData<TimeLines> {
        return dao.getTimeLinesByUUID(uuid)
    }


    suspend fun deleteSubtitleFileByUUID(uuid: UUID) {
        dao.deleteSubtitleFileByUUID(uuid)
    }


    suspend fun deleteAllSubtitleFiles() {
        dao.deleteAllSubtitleFiles()
    }

    suspend fun insertSubtitleFile(subtitleFile: SubtitleFile) {
        dao.insertSubtitleFile(subtitleFile)
    }

    suspend fun updateSubtitleFile(subtitleFile: SubtitleFile) {
        dao.updateSubtitleFile(subtitleFile)
    }




    //DataStore
    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LEAF_TIME_MODE = stringPreferencesKey("leaf_time_mode")
    }
    suspend fun saveLeafTimeMode(mode: String) {
        dataStore.edit { prefs->
            prefs[LEAF_TIME_MODE] = mode

        }
    }

    suspend fun getLeafTimeMode(): Flow<String> {
        return dataStore.data
            .catch { exception ->
                if(exception is IOException) {
                    exception.printStackTrace()
                    emit(emptyPreferences())
                } else {
                    throw exception
                }

            }.map { prefs->
                prefs[LEAF_TIME_MODE] ?: LeafTimeMode.FIVE_SECOND.value
            }
    }

    suspend fun saveThemeMode(mode: String) {
        dataStore.edit { prefs->
            prefs[THEME_MODE] = mode

        }
    }

    suspend fun getThemeMode(): Flow<String> {
        return dataStore.data
            .catch{ exception ->
                if(exception is IOException) {
                    exception.printStackTrace()
                    emit(emptyPreferences())
                } else {
                    throw exception
                }

            }.map { prefs->
                prefs[THEME_MODE] ?: ThemeMode.DEFAULT.value
            }
    }
}