package com.dmariani.streamkit.core.di

import android.content.Context
import androidx.room.Room
import com.dmariani.streamkit.core.data.local.StreamKitDatabase
import com.dmariani.streamkit.core.data.local.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the singleton Room database and its DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "streamkit.db"

    @Provides
    @Singleton
    fun provideStreamKitDatabase(@ApplicationContext context: Context): StreamKitDatabase =
        Room.databaseBuilder(context, StreamKitDatabase::class.java, DATABASE_NAME).build()

    @Provides
    @Singleton
    fun provideVideoDao(database: StreamKitDatabase): VideoDao = database.videoDao()
}
