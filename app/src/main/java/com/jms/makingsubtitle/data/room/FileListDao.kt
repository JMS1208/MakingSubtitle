package com.jms.makingsubtitle.data.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.jms.makingsubtitle.data.model.SubtitleFile
import java.util.*

@Dao
interface FileListDao {

    @Query("SELECT * FROM SubtitleFileList")
    fun getSubtitleFileList(): LiveData<List<SubtitleFile>>

    @Query("SELECT * FROM SubtitleFileList WHERE uuid = :uuid")
    fun getSubtitleByUUID(uuid: UUID) : LiveData<SubtitleFile>

    @Query("SELECT * FROM SubtitleFileList WHERE fileName = :fileName")
    fun getSubtitleByFileName(fileName: String) : LiveData<List<SubtitleFile>>

    @Query("DELETE FROM SubtitleFileList")
    suspend fun deleteAllSubtitleFiles()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtitleFile(subtitleFile: SubtitleFile)

    @Delete
    suspend fun deleteSubtitleFile(subtitleFile: SubtitleFile)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSubtitleFile(subtitleFile: SubtitleFile)


}