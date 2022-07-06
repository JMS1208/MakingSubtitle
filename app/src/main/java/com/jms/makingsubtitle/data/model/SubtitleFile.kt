package com.jms.makingsubtitle.data.model

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jms.makingsubtitle.util.Contants.TYPE_SRT
import java.util.*

@Entity(tableName = "SubtitleFileList")
@kotlinx.parcelize.Parcelize
data class SubtitleFile(@PrimaryKey(autoGenerate = false) val uuid: UUID = UUID.randomUUID(),
                        var bornDate: Date = Date(),
                        var lastUpdate: Date = Date(),
                        var fileName: String = "파일명_미지정",
                        var jobName: String = "작업명_미지정",
                        var contents: TimeLines = TimeLines(mutableListOf()),
                        var thumbnailUri: Uri? = null,
                        val type: String = TYPE_SRT,
) : Parcelable {


}