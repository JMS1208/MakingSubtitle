package com.jms.makingsubtitle.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.google.android.exoplayer2.util.Util
import com.jms.makingsubtitle.data.model.TimeLine
import com.jms.makingsubtitle.data.model.TimeLines

object Contants {

    const val DATE_FORMAT = "yyyy.MM.dd(E) hh:mm"
    const val REQUEST_VIDEO = 10
    const val REQUEST_SUBTITLE = 11
    //const val REQUEST_SUBTITLE_EUC_KR = 12
    const val REQUEST_EXPORT_FILE = 13

    const val TYPE_SRT = "srt"

    const val LAST_LINE_NUM = -1
    const val DATASTORE_NAME = "preferences_datastore"

    const val YOUTUBE_API_KEY = "AIzaSyC70SyviJ29bDSciWvz8A5AwDITAxEAtys"
    const val YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v="
    const val YOUTUBE_BASE_URL_MOBILE  = "https://youtu.be/"

    const val DOUBLE_CLICK_DELAY: Long = 200

    fun MakeToast(context: Context, text : String) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }






}