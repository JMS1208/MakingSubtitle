package com.jms.makingsubtitle.data.room

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.jms.makingsubtitle.data.model.TimeLine
import com.jms.makingsubtitle.data.model.TimeLines
import com.jms.makingsubtitle.data.model.VideoTime
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
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
    fun toByteArray(bitmap: Bitmap?): ByteArray? {
        bitmap?.let {
            val os = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            return os.toByteArray()
        } ?: return null

    }

    @TypeConverter
    fun toBitmap(bytes: ByteArray?): Bitmap? {
        bytes?.let {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } ?: return null

    }

//    @TypeConverter
//    fun fromString(contents: String): MutableList<TimeLine>? {
////        val listType: Type = object : TypeToken<MutableList<TimeLine>>() {}.type
////        return Gson().fromJson(contents, listType)
//        val listType = Types.newParameterizedType(MutableList::class.java,TimeLine::class.java)
//        val adapter : JsonAdapter<MutableList<TimeLine>> = moshi.adapter(listType)
//        return adapter.fromJson(contents)
//    }
//
//    @TypeConverter
//    fun listToString(timeLines: MutableList<TimeLine>?): String {
//        //return Gson().toJson(timeLines)
//        val listType = Types.newParameterizedType(MutableList::class.java, TimeLine::class.java)
//        val adapter: JsonAdapter<MutableList<TimeLine>> = moshi.adapter(listType)
//        return adapter.toJson(timeLines)
//    }

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





    //타임라인의 컨버터도 추가해야할듯

}