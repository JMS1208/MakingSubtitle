package com.jms.makingsubtitle.ui.viewmodel

import android.app.Application
import android.database.Cursor
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
import com.jms.makingsubtitle.util.Contants.LAST_LINE_NUM
import com.jms.makingsubtitle.util.Contants.MOVE_DIRECTION_NEXT
import com.jms.makingsubtitle.util.Contants.MOVE_DIRECTION_PREV
import com.jms.makingsubtitle.util.Contants.convertLineNumToPage
import com.jms.makingsubtitle.util.Contants.getLastLineNum
import com.jms.makingsubtitle.util.SingleLiveEvent
import fr.noop.subtitle.base.BaseSubtitleCue
import fr.noop.subtitle.model.SubtitleCue
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

    val subtitleFileList: StateFlow<List<SubtitleFile>> =
        mainRepository.getSubtitleFileList().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000),
            listOf()
        )







    fun insertSubtitleFile(subtitleFile: SubtitleFile) = viewModelScope.launch(Dispatchers.IO) {
        mainRepository.insertSubtitleFile(subtitleFile)
    }


    fun deleteAllSubtitleFiles() = viewModelScope.launch(Dispatchers.IO) {
        mainRepository.deleteAllSubtitleFiles()
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

    private var _stopPosition: MutableLiveData<Long> = MutableLiveData(0)
    val stopPosition: LiveData<Long> get() = _stopPosition

    fun setStopPosition(stopPosition: Long) {
        _stopPosition.postValue(stopPosition)
    }









    fun getSubtitleFileByUUID(uuid: UUID): SubtitleFile {
        return mainRepository.getSubtitleFileByUUID(uuid)
    }


    //여기에 옵저버 달아서 씀
    private var _loadedSubtitleFile: MutableLiveData<SubtitleFile> = MutableLiveData()
    val loadedSubtitleFile: MutableLiveData<SubtitleFile> get() = _loadedSubtitleFile



    val currentList: MutableLiveData<MutableList<TimeLine>> = MutableLiveData()







    fun setupCurrentList(page: Int) {
         val subtitleFile: SubtitleFile? = loadedSubtitleFile.value
        subtitleFile?.let {
            val contents = subtitleFile.contents
            contents.let {
                val timeLines = contents.timeLines
                currentList.postValue(timeLines.filter{ convertLineNumToPage(it.lineNum) == page  }.toMutableList())

            }

        }
    }

    fun setupSubtitleFile(subtitleFile: SubtitleFile) {
        _loadedSubtitleFile.postValue(subtitleFile)
    }

    fun deleteTimeLine(lineNum: Int) = viewModelScope.launch(Dispatchers.Default) {
        val subtitleFile = loadedSubtitleFile.value
        subtitleFile?.let {
            val lastLineNum: Int = getLastLineNum(subtitleFile.contents)
            Log.d("TAG","0: $lineNum")
            Log.d("TAG","1: $lastLineNum")
            val page = if(lastLineNum == lineNum) { //마지막애 지우는 경우
                convertLineNumToPage(lineNum-1)
            } else {
                convertLineNumToPage(lineNum)
            }
            Log.d("TAG","2: $page")

            subtitleFile.contents.let { contents->
                contents.timeLines.asSequence().filter{ it.lineNum >= lineNum }.map{ it.lineNum-- }.toMutableList()
                contents.timeLines.removeAt(lineNum - 1 )

            }

            withContext(Dispatchers.IO) {
                updateSubtitleFile(subtitleFile)
                //setupSubtitleFile(subtitleFile)
                setupCurrentList(page)
            }


        }


    }

    fun addTimeLine(timeLine: TimeLine) = viewModelScope.launch(Dispatchers.Default) {
        val lineNum = timeLine.lineNum
        val subtitleFile = loadedSubtitleFile.value
        subtitleFile?.let {
            subtitleFile.contents.let { contents->
                if(lineNum == LAST_LINE_NUM) {
                    contents.timeLines.add(TimeLine(lineNum = contents.timeLines.lastIndex + 2))
                } else {
                    if( lineNum > contents.timeLines.lastIndex + 1) {
                        // 훨씬 크면
                        contents.timeLines.add(TimeLine(lineNum = contents.timeLines.lastIndex + 2))
                    } else {
                        contents.timeLines.asSequence().filter{ it.lineNum >= lineNum }.map{ it.lineNum++ }.toMutableList()
                        contents.timeLines.add(lineNum, timeLine.apply{ this.lineNum++ })

                    }
                    //if(마지막보다 크거나 작은 경우)
                }

                withContext(Dispatchers.IO) {
                    updateSubtitleFile(subtitleFile)
                    //setupSubtitleFile(subtitleFile)
                }

                val tmpLineNum: Int = if(lineNum == LAST_LINE_NUM) subtitleFile.contents.timeLines.last().lineNum else lineNum
                val page: Int = convertLineNumToPage(tmpLineNum)
                setupCurrentList(page)


            }
        }
    }

    fun addTimeLine(lineNum: Int) = viewModelScope.launch(Dispatchers.Default) {
        val subtitleFile = loadedSubtitleFile.value
        subtitleFile?.let {
            subtitleFile.contents.let { contents->
                if(lineNum == LAST_LINE_NUM) {
                    contents.timeLines.add(TimeLine(lineNum = contents.timeLines.lastIndex + 2))
                } else {
                    if( lineNum > contents.timeLines.lastIndex + 1) {
                        // 훨씬 크면
                        contents.timeLines.add(TimeLine(lineNum = contents.timeLines.lastIndex + 2))
                    } else {
                        contents.timeLines.asSequence().filter{ it.lineNum >= lineNum }.map{ it.lineNum++ }.toMutableList()
                        contents.timeLines.add(lineNum-1, TimeLine(lineNum = lineNum))

                    }
                    //if(마지막보다 크거나 작은 경우)
                }

                withContext(Dispatchers.IO) {
                    updateSubtitleFile(subtitleFile)
                    //setupSubtitleFile(subtitleFile)
                }

                val tmpLineNum: Int = if(lineNum == LAST_LINE_NUM) subtitleFile.contents.timeLines.last().lineNum else lineNum
                val page: Int = convertLineNumToPage(tmpLineNum)
                setupCurrentList(page)


            }
        }
    }


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
                val index =
                //cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                    //cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE_SUBTITLE)
                    it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                fileName = cursor.getString(index).removeSuffix(".srt")
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
                val timeLine = TimeLine(_cue.id.toInt(), _cue.text, startTime, endTime)
                timeLines.add(timeLine)
            }

        }


        subtitleFile.apply {
            if (this.fileName.isEmpty()) {
                this.fileName = fileName
            }
            contents = TimeLines(timeLines)

            setupSubtitleFile(this)
            setupCurrentList(1)

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







}