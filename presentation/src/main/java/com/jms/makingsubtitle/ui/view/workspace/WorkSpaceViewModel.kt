package com.jms.makingsubtitle.ui.view.workspace

import android.app.Application
import android.app.Fragment
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jms.makingsubtitle.R
import com.jms.makingsubtitle.data.model.SubtitleFile
import com.jms.makingsubtitle.data.model.TimeLine
import com.jms.makingsubtitle.data.model.TimeLines
import com.jms.makingsubtitle.data.model.VideoTime
import com.jms.makingsubtitle.repository.MainRepository
import com.jms.makingsubtitle.util.Contants
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*
import javax.inject.Inject

@HiltViewModel
class WorkSpaceViewModel @Inject constructor(
    private val repository: MainRepository,
    private val contentResolver: ContentResolver,
    private val application: Application
): ViewModel() {

    private var _videoUri: MutableLiveData<Uri?> = MutableLiveData()
    val videoUri: LiveData<Uri?> get() = _videoUri

    fun setVideoUri(uri: Uri?) {
        _videoUri.postValue(uri)

    }

    suspend fun getLeafTimeMode() = withContext(Dispatchers.IO) {
        repository.getLeafTimeMode().first()
    }

    fun loadSubtitleFileUri(uri: Uri?, subtitleFile: SubtitleFile) {


        uri?.let {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    makeSubtitleFileByUri(uri, subtitleFile)
                } catch (E: Exception) {
                    Log.e("MainViewModel", "makeSubtitleFileByUri exception called")
                    Contants.MakeToast(
                        application.applicationContext,
                        application.resources.getString(R.string.fetchError)
                    )
                }
            }
        }
    }

    private fun makeSubtitleFileByUri(
        uri: Uri,
        subtitleFile: SubtitleFile,
        charsetName: String = "EUC-KR"
    ) = viewModelScope.launch(Dispatchers.IO) {

        val projection = arrayOf(MediaStore.Images.ImageColumns.DISPLAY_NAME)

        val cursor: Cursor? = contentResolver.query(
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


        var inputStream = contentResolver.openInputStream(uri)


        val decoder = Charset.forName("UTF-8").newDecoder()

        val newCharsetName = try {
            decoder.decode(ByteBuffer.wrap(inputStream!!.readBytes())) //이게 되면 UTF-8
            "UTF-8"
        } catch (E: Exception) { // 안 되면 EUC-KR
            "EUC-KR"
        }

        inputStream = contentResolver.openInputStream(uri)


        //오픈 소스 라이브러리 이용한 파싱


        inputStream?.use {
            val br = inputStream.bufferedReader(Charset.forName(newCharsetName))

            while (true) {


                br.readLine() ?: break

                val times = br.readLine()

                val (startTime, endTime) = if (times.contains("-->")) {
                    times.split("-->")
                } else {
                    Log.d("TAG", "times: $times")
                    throw IOException("InAppropriate Subtitle File")
                }


                val textLine = StringBuffer()
                while (true) {
                    val tmpTextLine = br.readLine()

                    if (tmpTextLine.isNotBlank()) {
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


    }

    fun getTimeLinesByUUID(uuid: UUID): LiveData<TimeLines> =
        repository.getTimeLinesByUUID(uuid)


    fun updateSubtitleFile(subtitleFile: SubtitleFile) = viewModelScope.launch(Dispatchers.IO) {
        subtitleFile.lastUpdate = Date()
        repository.updateSubtitleFile(subtitleFile)

    }

    suspend fun getVibrationOptions() = withContext(Dispatchers.IO) {
        repository.getVibrationOptions().first()
    }
}