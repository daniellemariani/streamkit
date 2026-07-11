package com.dmariani.streamkit.core.di

import com.dmariani.streamkit.core.data.repository.VideoRepositoryImpl
import com.dmariani.streamkit.core.domain.repository.VideoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binds `VideoRepository` to its default implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindVideoRepository(impl: VideoRepositoryImpl): VideoRepository
}
