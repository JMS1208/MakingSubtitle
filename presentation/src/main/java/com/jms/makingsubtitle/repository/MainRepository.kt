package com.jms.makingsubtitle.repository

import androidx.lifecycle.LiveData
import com.jms.makingsubtitle.data.model.SubtitleFile
import com.jms.makingsubtitle.data.model.TimeLines
import kotlinx.coroutines.flow.Flow
import java.util.*

interface MainRepository {

    fun getAllSubtitleFileList(): LiveData<List<SubtitleFile>>

    fun getSubtitleFileByUUID(uuid: UUID): SubtitleFile
    fun getTimeLinesByUUID(uuid: UUID): LiveData<TimeLines>

    suspend fun deleteSubtitleFileByUUID(uuid: UUID)
    suspend fun deleteAllSubtitleFiles()

    suspend fun insertSubtitleFile(subtitleFile: SubtitleFile)
    suspend fun updateSubtitleFile(subtitleFile: SubtitleFile)



    suspend fun saveVibrationOptions(options: String)

    suspend fun getVibrationOptions(): Flow<String>
    suspend fun saveShowOptions(options: String)

    suspend fun getShowOptions(): Flow<String>

    suspend fun saveLeafTimeMode(mode: String)

    suspend fun getLeafTimeMode(): Flow<String>

    suspend fun saveThemeMode(mode: String)

    suspend fun getThemeMode(): Flow<String>
}