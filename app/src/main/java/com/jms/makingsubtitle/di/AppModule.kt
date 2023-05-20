package com.jms.makingsubtitle.di

import android.content.ContentResolver
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.jms.makingsubtitle.data.room.SubtitleFileDatabase
import com.jms.makingsubtitle.data.room.SubtitleFileTypeConverter
import com.jms.makingsubtitle.util.Contants.DATABASE_NAME
import com.jms.makingsubtitle.util.Contants.DATASTORE_NAME
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    //Room Database
    @Singleton
    @Provides
    fun provideSubTitleDatabase(@ApplicationContext context: Context): SubtitleFileDatabase {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        return Room.databaseBuilder(
            context.applicationContext,
            SubtitleFileDatabase::class.java,
            DATABASE_NAME
        ).addTypeConverter(SubtitleFileTypeConverter(moshi)).build()
    }

    //Content Resolver
    @Singleton
    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    //DataStore
    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) }
        )
    }
}