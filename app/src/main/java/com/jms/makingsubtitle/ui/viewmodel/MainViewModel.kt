package com.jms.makingsubtitle.ui.viewmodel

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.*
import com.jms.makingsubtitle.data.model.SubtitleFile
import com.jms.makingsubtitle.data.model.TimeLine
import com.jms.makingsubtitle.data.model.VideoTime
import com.jms.makingsubtitle.repository.MainRepository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.nio.charset.Charset
import java.util.*

class MainViewModel(
    private val mainRepository: MainRepository,
    application: Application
) : AndroidViewModel(application) {


    // Room
    val subtitleFileListLiveData: LiveData<List<SubtitleFile>>
        get() = mainRepository.getSubtitleFileList()


    private var _subtitleFileByUUID : LiveData<SubtitleFile> = MutableLiveData()
    val subtitleFileByUUID: LiveData<SubtitleFile> get() = _subtitleFileByUUID

    fun loadSubtitleByUUID(uuid: UUID) {
        mainRepository.getSubtitleByUUID(uuid).let {
            _subtitleFileByUUID = it
        }

    }

    private var _subtitleFileListByFileName : LiveData<List<SubtitleFile>> = MutableLiveData()
    val subtitleFileListByFileName: LiveData<List<SubtitleFile>> get() = _subtitleFileListByFileName

    fun loadSubtitleByFileName(fileName: String) {
        mainRepository.getSubtitleByFileName(fileName).let {
            _subtitleFileListByFileName = it
        }
    }


    fun insertSubtitleFile(subtitleFile: SubtitleFile) = viewModelScope.launch(Dispatchers.IO){
        mainRepository.insertSubtitleFile(subtitleFile)
    }

    fun deleteSubtitleFile(subtitleFile: SubtitleFile) = viewModelScope.launch(Dispatchers.IO) {
        mainRepository.deleteSubtitleFile(subtitleFile)
    }

    fun deleteAllSubtitleFiles() = viewModelScope.launch(Dispatchers.IO) {
        mainRepository.deleteAllSubtitleFiles()
    }

    fun updateSubtitleFile(subtitleFile: SubtitleFile) = viewModelScope.launch(Dispatchers.IO){
        subtitleFile.lastUpdate = Date()
        mainRepository.updateSubtitleFile(subtitleFile)
    }



    //WorkSpace

    private var _videoUri: MutableLiveData<Uri?> = MutableLiveData()
    val videoUri: LiveData<Uri?> get() = _videoUri

    fun setVideoUri(uri: Uri?) {
        _videoUri.postValue(uri)
    }

    private var _stopPosition: MutableLiveData<Long> = MutableLiveData(0)
    val stopPosition: LiveData<Long> get() = _stopPosition

    fun setStopPosition(stopPosition: Long) {
        _stopPosition.postValue(stopPosition)
    }





    private var _loadedSubtitleFile: MutableLiveData<SubtitleFile> = MutableLiveData()
    val loadedSubtitleFile: LiveData<SubtitleFile> get() = _loadedSubtitleFile



    private fun makeSubtitleFileByUri(uri: Uri, subtitleFile: SubtitleFile) {

        val projection = arrayOf(MediaStore.Images.ImageColumns.DISPLAY_NAME)

        val cursor: Cursor? = getApplication<Application>().contentResolver.query(uri, projection, null, null, null,null)

        var fileName = "파일명_미지정"

        cursor?.use {
            if(cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                fileName = cursor.getString(index).removeSuffix(".srt")
            }
        }

        val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)

        val scanner = Scanner(inputStream, "EUC-KR")

        val timeLines: MutableList<TimeLine> = mutableListOf()

        //TODO{Scanner보다 BufferedReader가 더 빠르니까 교체 고려}
        scanner.use {
            while(scanner.hasNextLine()) {
                val index = scanner.nextLine().toInt()

                val (startTime, endTime) =  scanner.nextLine().split(" --> ")


                var tmpLine = scanner.nextLine()
                val line = StringBuffer("")

                while(!tmpLine.equals("")) {
                    line.append(tmpLine)
                    line.append("\n")
                    tmpLine = scanner.nextLine()
                }

                val timeLine = TimeLine(index,line.toString(), VideoTime(startTime), VideoTime(endTime))

                timeLines.add(timeLine)
            }


        }

        subtitleFile.apply {
            if(this.fileName.isEmpty()) {
                this.fileName = fileName
            }
            contents = timeLines
            _loadedSubtitleFile.postValue(this)
        }


        updateSubtitleFile(subtitleFile)




        //만들 때 디비 저장도 같이
    }



    fun loadSubtitleFileUri(uri: Uri?, subtitleFile: SubtitleFile) {


        uri?.let {
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    makeSubtitleFileByUri(uri, subtitleFile)
                } catch(E: Exception) {
                    Log.e("MainViewModel","makeSubtitleFileByUri exception called")
                }


            }
        }
    }



}