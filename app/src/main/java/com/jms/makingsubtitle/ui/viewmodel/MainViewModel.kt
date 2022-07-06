package com.jms.makingsubtitle.ui.viewmodel

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.*
import com.jms.makingsubtitle.data.model.SubtitleFile
import com.jms.makingsubtitle.data.model.TimeLine
import com.jms.makingsubtitle.data.model.TimeLines
import com.jms.makingsubtitle.data.model.VideoTime
import com.jms.makingsubtitle.repository.MainRepository

import fr.noop.subtitle.srt.SrtObject
import fr.noop.subtitle.srt.SrtParser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*


class MainViewModel(
    private val mainRepository: MainRepository,
    application: Application
) : AndroidViewModel(application) {


    // Room


    val allSubtitleList: LiveData<List<SubtitleFile>> = mainRepository.getAllSubtitleFileList()

    fun insertSubtitleFile(subtitleFile: SubtitleFile) = viewModelScope.launch(Dispatchers.IO) {
        mainRepository.insertSubtitleFile(subtitleFile)
    }


    fun deleteAllSubtitleFiles() = viewModelScope.launch(Dispatchers.IO) {
        mainRepository.deleteAllSubtitleFiles()
    }

    fun deleteSubtitleFileByUUID(uuid: UUID) = viewModelScope.launch(Dispatchers.IO) {
        mainRepository.deleteSubtitleFileByUUID(uuid)
    }

    fun updateSubtitleFile(subtitleFile: SubtitleFile) = viewModelScope.launch(Dispatchers.IO) {
        subtitleFile.lastUpdate = Date()
        mainRepository.updateSubtitleFile(subtitleFile)

    }


    //WorkSpace

    private var _videoUri: MutableLiveData<Uri?> = MutableLiveData()
    val videoUri: LiveData<Uri?> get() = _videoUri

    fun setVideoUri(uri: Uri?) {
        _videoUri.postValue(uri)

    }





    fun getTimeLinesByUUID(uuid: UUID): LiveData<TimeLines>
        = mainRepository.getTimeLinesByUUID(uuid)



    private fun makeSubtitleFileByUri(
        uri: Uri,
        subtitleFile: SubtitleFile,
        charsetName: String = "EUC-KR"
    ) = viewModelScope.launch(Dispatchers.IO) {

        val projection = arrayOf(MediaStore.Images.ImageColumns.DISPLAY_NAME)

        val cursor: Cursor? = getApplication<Application>().contentResolver.query(
            uri,
            projection,
            null,
            null,
            null,
            null
        )

        var fileName = "파일명_미지정"

        cursor?.use {
            if (cursor.moveToFirst()) {
                val index_fileName =
                //cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                    //cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE_SUBTITLE)
                    it.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                fileName = cursor.getString(index_fileName).removeSuffix(".srt")


            }
        }

        val timeLines: MutableList<TimeLine> = mutableListOf()


        val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)

        // TODO charsetName 내부에서 알아낼 수 있으면 추가하기


        //오픈 소스 라이브러리 이용한 파싱

        val parser = SrtParser(charsetName)
        val subtitle: SrtObject = parser.parse(inputStream)



        withContext(Dispatchers.Default) {
            for (i in subtitle.cues.indices) {
                val _cue = subtitle.cues[i]
                val startTime = VideoTime(
                    _cue.startTime.hour,
                    _cue.startTime.minute,
                    _cue.startTime.second,
                    _cue.startTime.millisecond
                )
                val endTime = VideoTime(
                    _cue.endTime.hour,
                    _cue.endTime.minute,
                    _cue.endTime.second,
                    _cue.endTime.millisecond
                )
                val timeLine = TimeLine(_cue.text, startTime, endTime)
                timeLines.add(timeLine)
            }

        }


        subtitleFile.apply {
            if (this.fileName.isEmpty()) {
                this.fileName = fileName
            }
            contents = TimeLines(timeLines)

        }


        updateSubtitleFile(subtitleFile)


        //만들 때 디비 저장도 같이
    }


    fun loadSubtitleFileUri(uri: Uri?, subtitleFile: SubtitleFile, charsetName: String) {


        uri?.let {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    makeSubtitleFileByUri(uri, subtitleFile, charsetName)
                } catch (E: Exception) {
                    Log.e("MainViewModel", "makeSubtitleFileByUri exception called")
                }
            }
        }
    }


    //DataStore
    fun saveThemeMode(value: String) = viewModelScope.launch(Dispatchers.IO) {
        mainRepository.saveThemeMode(value)
    }

    suspend fun getThemeMode() = withContext(Dispatchers.IO) {
        mainRepository.getThemeMode().first()
    }

    fun saveLeafTimeMode(value: String) = viewModelScope.launch(Dispatchers.IO) {
        mainRepository.saveLeafTimeMode(value)
    }

    suspend fun getLeafTimeMode() = withContext(Dispatchers.IO) {
        mainRepository.getLeafTimeMode().first()
    }

}