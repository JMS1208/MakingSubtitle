package com.jms.makingsubtitle.data.room

import android.net.Uri
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.jms.makingsubtitle.data.model.TimeLine
import com.jms.makingsubtitle.data.model.TimeLines
import com.jms.makingsubtitle.data.model.VideoTime
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.util.*

@ProvidedTypeConverter
class SubtitleFileTypeConverter(
    private val moshi: Moshi
) {

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(date: Long?): Date? {
        return date?.let{
            Date(it)
        }
    }

    @TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @TypeConverter
    fun toUUID(uuid: String): UUID {
        return UUID.fromString(uuid)
    }


    @TypeConverter
    fun toTimeLines(value: String): TimeLines? {
        val adapter: JsonAdapter<TimeLines> = moshi.adapter(TimeLines::class.java)
        return adapter.fromJson(value)
    }
    @TypeConverter
    fun fromTimeLines(type: TimeLines?): String {
        val adapter: JsonAdapter<TimeLines?> = moshi.adapter(TimeLines::class.java)
        return adapter.toJson(type)
    }
    @TypeConverter
    fun toTimeLine(value: String): TimeLine? {
        val adapter: JsonAdapter<TimeLine> = moshi.adapter(TimeLine::class.java)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun fromTimeLine(type: TimeLine): String {
        val adapter: JsonAdapter<TimeLine> = moshi.adapter(TimeLine::class.java)
        return adapter.toJson(type)
    }

    @TypeConverter
    fun toVideoTime(value: String): VideoTime? {
        val adapter: JsonAdapter<VideoTime> = moshi.adapter(VideoTime::class.java)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun fromVideoTime(type: VideoTime): String {
        val adapter: JsonAdapter<VideoTime> = moshi.adapter(VideoTime::class.java)
        return adapter.toJson(type)
    }

    @TypeConverter
    fun toUri(value: String?): Uri? {
        return if (value == null) null else Uri.parse(value)
    }

    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }



}