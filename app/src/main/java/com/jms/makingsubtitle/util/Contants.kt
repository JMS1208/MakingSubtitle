package com.jms.makingsubtitle.util

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast

object Contants {

    const val DATE_FORMAT = "yyyy.MM.dd(E) hh:mm"
    const val REQUEST_VIDEO = 10
    const val REQUEST_SUBTITLE = 11
    const val TIME_FORMAT = "hh:mm:ss"
    const val TYPE_SRT = "srt"


    fun MakeToast(context: Context, text : String) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }



}