package org.alhaq.deenshield.guardian.di

import org.alhaq.deenshield.guardian.network.ContentFilter
import org.alhaq.deenshield.guardian.network.PacketParser
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
