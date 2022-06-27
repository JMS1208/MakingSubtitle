package com.jms.makingsubtitle.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.jms.makingsubtitle.data.model.SubtitleFile
import com.jms.makingsubtitle.data.model.TimeLine
import com.jms.makingsubtitle.data.model.TimeLines
import com.jms.makingsubtitle.data.room.FileListDao
import com.jms.makingsubtitle.data.room.SubtitleFileDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.*

class MainRepository(private val db: SubtitleFileDatabase) {

    private val dao: FileListDao get() = db.subtitleFileDao()

    //    fun getSubtitleFileList(): LiveData<List<SubtitleFile>> {
//        return dao.getSubtitleFileList()
//    }




    fun getSubtitleFileList(): Flow<List<SubtitleFile>> {
        return dao.getSubtitleFileList()
    }

    fun getSubtitleFileByUUID(uuid: UUID): SubtitleFile {
        return dao.getSubtitleFileByUUID(uuid)
    }


    fun getSubtitleByUUID(uuid: UUID): LiveData<SubtitleFile> {
        return dao.getSubtitleByUUID(uuid)
    }


    fun getSubtitleByFileName(fileName: String): LiveData<List<SubtitleFile>> {
        return dao.getSubtitleByFileName(fileName)
    }


    suspend fun deleteAllSubtitleFiles() {
        dao.deleteAllSubtitleFiles()
    }

    suspend fun insertSubtitleFile(subtitleFile: SubtitleFile) {
        dao.insertSubtitleFile(subtitleFile)
    }

    suspend fun deleteSubtitleFile(subtitleFile: SubtitleFile) {
        dao.deleteSubtitleFile(subtitleFile)
    }

    suspend fun updateSubtitleFile(subtitleFile: SubtitleFile) {
        dao.updateSubtitleFile(subtitleFile)
    }



}