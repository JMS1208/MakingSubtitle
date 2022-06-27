package com.jms.makingsubtitle.util

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.jms.makingsubtitle.data.model.TimeLine
import com.jms.makingsubtitle.data.model.TimeLines

object Contants {

    const val DATE_FORMAT = "yyyy.MM.dd(E) hh:mm"
    const val REQUEST_VIDEO = 10
    const val REQUEST_SUBTITLE_UTF_8 = 11
    const val REQUEST_SUBTITLE_EUC_KR = 12
    const val TIME_FORMAT = "hh:mm:ss"
    const val TYPE_SRT = "srt"

    const val RECYCLERVIEW_CACHE_SIZE = 500

    const val MOVE_DIRECTION_NEXT = 1
    const val MOVE_DIRECTION_PREV = 2

    const val LAST_LINE_NUM = -1

    fun MakeToast(context: Context, text : String) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun convertLineNumToPage(lineNum: Int): Int {
        return if( lineNum < 1 ) 1
            else ( ( lineNum - 1 ) / 10 ) + 1
    }

    fun convertTimeLineListToPage(timeLineList: MutableList<TimeLine>): Int {
        return if(timeLineList.isNotEmpty()){
            convertLineNumToPage(timeLineList.last().lineNum)
        } else {
            1
        }
    }

    fun convertTimeLinesToPage(timeLines: TimeLines): Int {
        return convertTimeLineListToPage(timeLines.timeLines)
    }

    fun getLastLineNum(timeLineList: MutableList<TimeLine>): Int {
        return timeLineList.last().lineNum
    }

    fun getLastLineNum(timeLines: TimeLines): Int {
        return getLastLineNum(timeLines.timeLines)
    }

}