package com.deenshield.blocker.auth.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for LocalAccount entity
 * 
 * Provides type-safe database operations for local account management.
 * All queries return Flow for reactive updates.
 * 
 * **Thread Safety:**
 * - All suspend functions are coroutine-safe
 * - Flow emissions happen on background thread
 * - Room handles thread dispatching automatically
 * 
 * **Islamic Principle: Amanah (Trust)**
 * - Query results never expose raw passwords
 * - Account updates require explicit operations
 * - Deletion is permanent (no soft deletes by default)
 * 
 * @see LocalAccount
 */
@Dao
interface LocalAccountDao {
    
    /**
     * Insert new account into database
     * 
     * @param account Account to insert
     * @return ID of inserted account
     * @throws SQLiteConstraintException if username/email already exists
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(account: LocalAccount): Long
    
    /**
     * Update existing account
     * 
     * @param account Account with updated fields
     * @return Number of rows updated (should be 1)
     */
    @Update
    suspend fun update(account: LocalAccount): Int
    
    /**
     * Delete account from database
     * 
     * **Security Note:** This is permanent deletion. 
     * Consider deactivating instead using setAccountActive().
     * 
     * @param account Account to delete
     */
    @Delete
    suspend fun delete(account: LocalAccount)
    
    /**
     * Get account by ID
     * 
     * @param id Account ID
     * @return Flow emitting account or null if not found
     */
    @Query("SELECT * FROM local_accounts WHERE id = :id LIMIT 1")
    fun getAccountById(id: Long): Flow<LocalAccount?>
    
    /**
     * Get account by username
     * 
     * @param username Exact username match (case-sensitive)
     * @return Flow emitting account or null if not found
     */
    @Query("SELECT * FROM local_accounts WHERE username = :username LIMIT 1")
    fun getAccountByUsername(username: String): Flow<LocalAccount?>
    
    /**
     * Get account by email
     * 
     * @param email Email address (case-insensitive)
     * @return Flow emitting account or null if not found
     */
    @Query("SELECT * FROM local_accounts WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    fun getAccountByEmail(email: String): Flow<LocalAccount?>
    
    /**
     * Get account by device ID
     * 
     * @param deviceId Auto-generated device UUID
     * @return Flow emitting account or null if not found
     */
    @Query("SELECT * FROM local_accounts WHERE deviceId = :deviceId LIMIT 1")
    fun getAccountByDeviceId(deviceId: String): Flow<LocalAccount?>
    
    /**
     * Get all accounts (for testing/admin)
     * 
     * @return Flow emitting list of all accounts
     */
    @Query("SELECT * FROM local_accounts ORDER BY createdAt DESC")
    fun getAllAccounts(): Flow<List<LocalAccount>>
    
    /**
     * Get active accounts only
     * 
     * @return Flow emitting list of active accounts
     */
    @Query("SELECT * FROM local_accounts WHERE isActive = 1 ORDER BY lastLoginAt DESC")
    fun getActiveAccounts(): Flow<List<LocalAccount>>
    
    /**
     * Update last login timestamp
     * 
     * @param id Account ID
     * @param timestamp Login timestamp (milliseconds)
     */
    @Query("UPDATE local_accounts SET lastLoginAt = :timestamp WHERE id = :id")
    suspend fun updateLastLogin(id: Long, timestamp: Long)
    
    /**
     * Set account active/inactive status
     * 
     * Deactivated accounts cannot log in but are not deleted.
     * Use this instead of delete() for user-requested account removal.
     * 
     * @param id Account ID
     * @param isActive true to activate, false to deactivate
     */
    @Query("UPDATE local_accounts SET isActive = :isActive WHERE id = :id")
    suspend fun setAccountActive(id: Long, isActive: Boolean)
    
    /**
     * Check if username already exists
     * 
     * @param username Username to check
     * @return true if username exists
     */
    @Query("SELECT COUNT(*) FROM local_accounts WHERE username = :username")
    suspend fun usernameExists(username: String): Int
    
    /**
     * Check if email already exists
     * 
     * @param email Email to check (case-insensitive)
     * @return true if email exists
     */
    @Query("SELECT COUNT(*) FROM local_accounts WHERE LOWER(email) = LOWER(:email)")
    suspend fun emailExists(email: String): Int
    
    /**
     * Delete all accounts (for testing only)
     * 
     * ⚠️ WARNING: This deletes ALL account data permanently!
     */
    @Query("DELETE FROM local_accounts")
    suspend fun deleteAll()
}
