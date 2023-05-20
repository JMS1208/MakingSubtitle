package com.jms.makingsubtitle.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimeLines(
    var timeLines: MutableList<TimeLine>
): Parcelable {
}