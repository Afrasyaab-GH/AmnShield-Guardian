package com.alhaq.amnshield.guardian.di

import android.content.Context
import com.alhaq.amnshield.guardian.auth.data.GuardianDatabase
import com.alhaq.amnshield.guardian.auth.local.LocalAccountManager
import com.alhaq.amnshield.guardian.auth.local.TokenStorage
import com.alhaq.amnshield.guardian.network.ContentFilter
import com.alhaq.amnshield.guardian.network.PacketParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Dependency Injection Module for Guardian App
 * 
 * Phase 1: Authentication Module
 * Provides singleton instances for:
 * - Authentication (LocalAccountManager, TokenStorage)
 * - Database (GuardianDatabase)
 * - Network/blocking providers (ContentFilter, PacketParser)
 * 
 * All provided instances are application-scoped singletons.
 * 
 * **Islamic Principle: Amanah (Trust)**
 * - Single instances prevent state duplication
 * - Centralized dependency management
 * - Clear lifecycle management
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    /**
     * Provide GuardianDatabase singleton
     * 
     * Room database for authentication and capabilities.
     * Manages local accounts, capability grants, and tokens.
     */
    @Provides
    @Singleton
    fun provideGuardianDatabase(@ApplicationContext context: Context): GuardianDatabase {
        return GuardianDatabase.getInstance(context)
    }
    
    /**
     * Provide LocalAccountManager singleton
     * 
     * Manages local account creation, password hashing, and validation.
     * No network calls - all operations are local.
     */
    @Provides
    @Singleton
    fun provideLocalAccountManager(@ApplicationContext context: Context): LocalAccountManager {
        return LocalAccountManager.getInstance(context)
    }
    
    /**
     * Provide TokenStorage singleton
     * 
     * Manages encrypted token storage, validation, and expiration.
     * Uses Android Keystore for AES-256 encryption.
     */
    @Provides
    @Singleton
    fun provideTokenStorage(@ApplicationContext context: Context): TokenStorage {
        return TokenStorage.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideContentFilter(): ContentFilter = ContentFilter()

    @Provides
    @Singleton
    fun providePacketParser(): PacketParser = PacketParser()
}

