package com.jms.makingsubtitle.data.model

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.parcelize.RawValue
import java.util.*

@kotlinx.parcelize.Parcelize
data class TimeLine(

    var lineNum: Int = 1,
    var lineContent: String = "",
    var startTime: @RawValue VideoTime = VideoTime(),
    var endTime: @RawValue VideoTime = VideoTime()
) : Parcelable {


}