package com.jms.makingsubtitle.data.model

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jms.makingsubtitle.util.Contants.TYPE_SRT
import com.jms.makingsubtitle.util.SubtitleUtils
import kotlinx.parcelize.RawValue
import java.util.*

@Entity(tableName = "SubtitleFileList")
@kotlinx.parcelize.Parcelize
data class SubtitleFile(@PrimaryKey(autoGenerate = false) val uuid: UUID = UUID.randomUUID(),
                        var bornDate: Date = Date(),
                        var lastUpdate: Date = Date(),
                        var fileName: String = "",
                        var jobName: String = "",
                        var contents: @RawValue MutableList<TimeLine>? = null,
                        var thumbnail: Bitmap? = null,
                        val type: String = TYPE_SRT,

//                        var lineNum: Int = 1,
//                        var lineContent: String = "",
//                        var startTime: VideoTime = VideoTime(),
//                        var endTime: VideoTime = VideoTime()
//

) : Parcelable {


}