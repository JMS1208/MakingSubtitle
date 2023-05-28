package com.jms.makingsubtitle.data.room

import android.content.Context
import androidx.room.*
import com.jms.makingsubtitle.data.model.SubtitleFile
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


@Database(
    entities = [SubtitleFile::class],
    version = 1,
    exportSchema = true,

)
@TypeConverters(SubtitleFileTypeConverter::class)
abstract class SubtitleFileDatabase : RoomDatabase() {
    abstract fun subtitleFileDao(): FileListDao



//    companion object {
//        @Volatile
//        private var INSTANCE: SubtitleFileDatabase? = null
//

//        val moshi = Moshi.Builder()
//            .addLast(KotlinJsonAdapterFactory())
//            .build()

//        private fun buildDatabase(context: Context): SubtitleFileDatabase =
//            Room.databaseBuilder(
//                context.applicationContext,
//                SubtitleFileDatabase::class.java,
//                "subtitle-fileList"
//            ).addTypeConverter(SubtitleFileTypeConverter(moshi)).build()
//
//        fun getInstance(context: Context): SubtitleFileDatabase =
//            INSTANCE ?: synchronized(SubtitleFileDatabase::class.java) {
//                INSTANCE ?: buildDatabase(context).also {
//                    INSTANCE = it
//                }
//            }
//    }
}