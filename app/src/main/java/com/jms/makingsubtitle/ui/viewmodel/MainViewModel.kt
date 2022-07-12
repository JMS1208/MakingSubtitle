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
import com.jms.makingsubtitle.util.Contants.MakeToast

import fr.noop.subtitle.srt.SrtObject
import fr.noop.subtitle.srt.SrtParser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.net.URLEncoder
import java.nio.ByteBuffer
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

        var fileName = "file_name"

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


        var inputStream = getApplication<Application>().contentResolver.openInputStream(uri)


        val decoder = Charset.forName("UTF-8").newDecoder()

        val newCharsetName = try {
            decoder.decode(ByteBuffer.wrap(inputStream!!.readBytes())) //이게 되면 UTF-8
            "UTF-8"
        } catch(E: Exception) { // 안 되면 EUC-KR
            "EUC-KR"
        }

        inputStream = getApplication<Application>().contentResolver.openInputStream(uri)


        //오픈 소스 라이브러리 이용한 파싱


        inputStream?.use {
            val br = inputStream.bufferedReader(Charset.forName(newCharsetName))

            while(true) {


                br.readLine() ?: break

                val times = br.readLine()

                val (startTime, endTime) = if(times.contains("-->")) {
                    times.split("-->")
                }else {
                    Log.d("TAG", "times: $times")
                    throw IOException("InAppropriate Subtitle File")
                }


                val textLine = StringBuffer()
                while(true) {
                    val tmpTextLine = br.readLine()

                    if(tmpTextLine.isNotBlank()) {
                        textLine.append(tmpTextLine)
                        textLine.append("\n")
                    } else {
                        break
                    }

                }


                val timeLine = TimeLine().apply {
                    this.startTime = VideoTime(startTime)
                    this.endTime = VideoTime(endTime)
                    this.lineContent = textLine.trim().toString()
                }

                timeLines.add(timeLine)


            }

        }

//
//        val parser = if(charsetName != "UTF-8" && charsetName != "EUC-KR") {
//            SrtParser(charsetName)
//        } else {
//            SrtParser(newCharsetName)
//        }
//
//        val subtitle: SrtObject = parser.parse(inputStream)
//
//
//
//        withContext(Dispatchers.Default) {
//            for (i in subtitle.cues.indices) {
//                val _cue = subtitle.cues[i]
//                val startTime = VideoTime(
//                    _cue.startTime.hour,
//                    _cue.startTime.minute,
//                    _cue.startTime.second,
//                    _cue.startTime.millisecond
//                )
//                val endTime = VideoTime(
//                    _cue.endTime.hour,
//                    _cue.endTime.minute,
//                    _cue.endTime.second,
//                    _cue.endTime.millisecond
//                )
//                val timeLine = TimeLine(_cue.text, startTime, endTime)
//                timeLines.add(timeLine)
//            }
//
//        }


        subtitleFile.apply {
            if (this.fileName.isEmpty()) {
                this.fileName = fileName
            }
            contents = TimeLines(timeLines)

        }


        updateSubtitleFile(subtitleFile)


        //만들 때 디비 저장도 같이
    }


    fun loadSubtitleFileUri(uri: Uri?, subtitleFile: SubtitleFile) {


        uri?.let {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    makeSubtitleFileByUri(uri, subtitleFile)
                } catch (E: Exception) {
                    Log.e("MainViewModel", "makeSubtitleFileByUri exception called")
                    MakeToast(getApplication<Application>().applicationContext,"자막 파일을 읽을 수 없습니다")
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

    fun saveShowOptions(value: String) = viewModelScope.launch(Dispatchers.IO) {
        mainRepository.saveShowOptions(value)
    }

    suspend fun getShowOptions() = withContext(Dispatchers.IO) {
        mainRepository.getShowOptions().first()
    }

    fun saveVibrationOptions(value: String) = viewModelScope.launch(Dispatchers.IO) {
        mainRepository.saveVibrationOptions(value)
    }

    suspend fun getVibrationOptions() = withContext(Dispatchers.IO) {
        mainRepository.getVibrationOptions().first()
    }

}