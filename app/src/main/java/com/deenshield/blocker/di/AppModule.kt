package com.deenshield.blocker.di

import com.deenshield.blocker.network.ContentFilter
import com.deenshield.blocker.network.PacketParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideContentFilter(): ContentFilter = ContentFilter()

    @Provides
    fun providePacketParser(): PacketParser = PacketParser()
}
