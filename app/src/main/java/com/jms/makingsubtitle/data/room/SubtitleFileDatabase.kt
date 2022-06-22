package com.jms.makingsubtitle.data.room

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jms.makingsubtitle.data.model.SubtitleFile


@Database(
    entities = [SubtitleFile::class],
    version = 1,
    exportSchema = true,

)
@TypeConverters(SubtitleFileTypeConverter::class)
abstract class SubtitleFileDatabase : RoomDatabase() {
    abstract fun subtitleFileDao(): FileListDao



    companion object {
        @Volatile
        private var INSTANCE: SubtitleFileDatabase? = null


        private fun buildDatabase(context: Context): SubtitleFileDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                SubtitleFileDatabase::class.java,
                "subtitle-fileList"
            ).build()

        fun getInstance(context: Context): SubtitleFileDatabase =
            INSTANCE ?: synchronized(SubtitleFileDatabase::class.java) {
                INSTANCE ?: buildDatabase(context).also {
                    INSTANCE = it
                }
            }
    }
}