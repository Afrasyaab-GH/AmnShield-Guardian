package com.alhaq.amnshield.guardian.auth.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room Database for Guardian authentication and capabilities
 * 
 * Provides centralized database access for:
 * - Local accounts (LocalAccount)
 * - Guardian capabilities (GuardianCapabilityEntity)
 * - Capability tokens (CapabilityTokenEntity)
 * 
 * **Islamic Principle: Amanah (Trust)**
 * - All sensitive data encrypted via EncryptedSharedPreferences
 * - Database file stored in app-private storage
 * - No external access to database
 * 
 * **Thread Safety:**
 * - Singleton pattern ensures single instance
 * - Room handles thread dispatching automatically
 * - All DAO methods are coroutine-safe
 * 
 * @property localAccountDao Access to local accounts
 * @property capabilityDao Access to capabilities
 * @property tokenDao Access to tokens
 */
@Database(
    entities = [
        LocalAccount::class,
        GuardianCapabilityEntity::class,
        CapabilityTokenEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GuardianDatabase : RoomDatabase() {
    
    abstract fun localAccountDao(): LocalAccountDao
    abstract fun capabilityDao(): GuardianCapabilityDao
    abstract fun tokenDao(): CapabilityTokenDao
    
    companion object {
        private const val DATABASE_NAME = "guardian_database"
        
        @Volatile
        private var INSTANCE: GuardianDatabase? = null
        
        /**
         * Get database instance (singleton)
         * 
         * Thread-safe lazy initialization with double-checked locking.
         * 
         * @param context Application context
         * @return GuardianDatabase instance
         */
        fun getInstance(context: Context): GuardianDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        /**
         * Build database with migrations
         * 
         * @param context Application context
         * @return Configured GuardianDatabase
         */
        private fun buildDatabase(context: Context): GuardianDatabase {
            val builder = if (System.getProperty("java.vendor")?.contains("Android") != true) {
                Room.inMemoryDatabaseBuilder(
                    context.applicationContext,
                    GuardianDatabase::class.java
                ).allowMainThreadQueries()
            } else {
                Room.databaseBuilder(
                    context.applicationContext,
                    GuardianDatabase::class.java,
                    DATABASE_NAME
                )
            }
            return builder
                .addMigrations(/* Add migrations here when schema changes */)
                .fallbackToDestructiveMigration() // For development only
                .build()
        }
        
        /**
         * Example migration (version 1 â†’ 2)
         * 
         * Add migrations when database schema changes to preserve user data.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: Add new column to local_accounts
                // database.execSQL("ALTER TABLE local_accounts ADD COLUMN newColumn TEXT")
            }
        }
        
        /**
         * Close database and clear instance (for testing)
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}

