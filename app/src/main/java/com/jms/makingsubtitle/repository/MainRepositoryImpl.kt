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
import com.jms.makingsubtitle.repository.MainRepositoryImpl.PreferencesKeys.LEAF_TIME_MODE
import com.jms.makingsubtitle.repository.MainRepositoryImpl.PreferencesKeys.THEME_MODE
import com.jms.makingsubtitle.data.datastore.LeafTimeMode
import com.jms.makingsubtitle.data.datastore.ShowOptions
import com.jms.makingsubtitle.data.datastore.ThemeMode
import com.jms.makingsubtitle.data.datastore.VibrationOptions
import com.jms.makingsubtitle.repository.MainRepositoryImpl.PreferencesKeys.SHOW_OPTION
import com.jms.makingsubtitle.repository.MainRepositoryImpl.PreferencesKeys.VIBRATION_OPTION
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepositoryImpl @Inject constructor(
    private val db: SubtitleFileDatabase,
    private val dataStore: DataStore<Preferences>
): MainRepository {

    private val dao: FileListDao get() = db.subtitleFileDao()


    override fun getAllSubtitleFileList(): LiveData<List<SubtitleFile>> {
        return dao.getAllSubtitleFileList()
    }

    override fun getSubtitleFileByUUID(uuid: UUID): SubtitleFile {
        return dao.getSubtitleFileByUUID(uuid)
    }

    override fun getTimeLinesByUUID(uuid: UUID): LiveData<TimeLines> {
        return dao.getTimeLinesByUUID(uuid)
    }


    override suspend fun deleteSubtitleFileByUUID(uuid: UUID) {
        dao.deleteSubtitleFileByUUID(uuid)
    }


    override suspend fun deleteAllSubtitleFiles() {
        dao.deleteAllSubtitleFiles()
    }

    override suspend fun insertSubtitleFile(subtitleFile: SubtitleFile) {
        dao.insertSubtitleFile(subtitleFile)
    }

    override suspend fun updateSubtitleFile(subtitleFile: SubtitleFile) {
        dao.updateSubtitleFile(subtitleFile)
    }




    //DataStore
    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LEAF_TIME_MODE = stringPreferencesKey("leaf_time_mode")
        val SHOW_OPTION = stringPreferencesKey("show_options")
        val VIBRATION_OPTION = stringPreferencesKey("vibration_options")
    }

    override suspend fun saveVibrationOptions(options: String) {
        dataStore.edit { prefs->
            prefs[VIBRATION_OPTION] = options
        }
    }

    override suspend fun getVibrationOptions(): Flow<String>{
        return dataStore.data
            .catch { exception ->
                if(exception is IOException) {
                    exception.printStackTrace()
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { prefs->
                prefs[VIBRATION_OPTION] ?: VibrationOptions.ACTIVATE.value
            }
    }

    override suspend fun saveShowOptions(options: String) {
        dataStore.edit { prefs->
            prefs[SHOW_OPTION] = options
        }
    }

    override suspend fun getShowOptions(): Flow<String> {
        return dataStore.data
            .catch { exception ->
                if(exception is IOException) {
                    exception.printStackTrace()
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { prefs->
                prefs[SHOW_OPTION] ?: ShowOptions.SHOW_AGAIN.value
            }
    }

    override suspend fun saveLeafTimeMode(mode: String) {
        dataStore.edit { prefs->
            prefs[LEAF_TIME_MODE] = mode

        }
    }

    override suspend fun getLeafTimeMode(): Flow<String> {
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

    override suspend fun saveThemeMode(mode: String) {
        dataStore.edit { prefs->
            prefs[THEME_MODE] = mode

        }
    }

    override suspend fun getThemeMode(): Flow<String> {
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