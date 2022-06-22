package com.jms.makingsubtitle.data.room

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.jms.makingsubtitle.data.model.TimeLine
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.util.*

class SubtitleFileTypeConverter {

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

    @TypeConverter
    fun fromString(contents: String): MutableList<TimeLine>? {
        val listType: Type = object : TypeToken<MutableList<TimeLine>>() {}.type
        return Gson().fromJson(contents, listType)

    }

    @TypeConverter
    fun listToString(timeLines: MutableList<TimeLine>?): String {
        return Gson().toJson(timeLines)
    }


    //타임라인의 컨버터도 추가해야할듯

}