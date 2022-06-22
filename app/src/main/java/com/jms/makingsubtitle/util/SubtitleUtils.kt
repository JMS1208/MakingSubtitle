package com.jms.makingsubtitle.util

import com.jms.makingsubtitle.data.model.TimeLine

interface SubtitleUtils {
    fun addLine(timeLineList: MutableList<TimeLine>, timeLine: TimeLine)
    fun getTimeLineList(): MutableList<TimeLine>
}