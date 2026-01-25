package com.deenshield.blocker.auth.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deenshield.blocker.auth.IdentityMode
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for LocalAccountManager
 * 
 * Tests password hashing, account creation, and validation.
 * 
 * **Coverage Areas:**
 * - PBKDF2 password hashing
 * - Password validation
 * - Account creation for all identity modes
 * - Security (timing-safe comparisons, unique salts)
 */
@RunWith(AndroidJUnit4::class)
class LocalAccountManagerTest {
    
    private lateinit var context: Context
    private lateinit var accountManager: LocalAccountManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        accountManager = LocalAccountManager.getInstance(context)
    }
    
    @After
    fun tearDown() {
        // Clear any test data
        accountManager.clearAllAccounts()
    }
    
    @Test
    fun testPasswordHashing_GeneratesUniqueHashes() {
        val password = "testPassword123"
        val salt1 = accountManager.generateSalt()
        val salt2 = accountManager.generateSalt()
        
        val hash1 = accountManager.hashPassword(password, salt1)
        val hash2 = accountManager.hashPassword(password, salt2)
        
        // Same password with different salts should produce different hashes
        assertNotEquals(hash1, hash2)
    }
    
    @Test
    fun testPasswordHashing_ConsistentWithSameSalt() {
        val password = "testPassword123"
        val salt = accountManager.generateSalt()
        
        val hash1 = accountManager.hashPassword(password, salt)
        val hash2 = accountManager.hashPassword(password, salt)
        
        // Same password and salt should produce same hash
        assertEquals(hash1, hash2)
    }
    
    @Test
    fun testPasswordValidation_CorrectPassword() {
        val password = "testPassword123"
        val account = accountManager.createAccount(
            username = "testuser",
            password = password,
            identityMode = IdentityMode.LOCAL
        ).getOrNull()!!
        
        val isValid = accountManager.validatePassword(password, account)
        
        assertTrue(isValid)
    }
    
    @Test
    fun testPasswordValidation_IncorrectPassword() {
        val correctPassword = "testPassword123"
        val wrongPassword = "wrongPassword456"
        
        val account = accountManager.createAccount(
            username = "testuser",
            password = correctPassword,
            identityMode = IdentityMode.LOCAL
        ).getOrNull()!!
        
        val isValid = accountManager.validatePassword(wrongPassword, account)
        
        assertFalse(isValid)
    }
    
    @Test
    fun testAccountCreation_LocalMode() {
        val result = accountManager.createAccount(
            username = "testuser",
            password = "password123",
            identityMode = IdentityMode.LOCAL
        )
        
        assertTrue(result.isSuccess)
        val account = result.getOrNull()!!
        assertEquals("testuser", account.username)
        assertEquals("LOCAL", account.identityMode)
        assertNull(account.email)
        assertNull(account.deviceId)
    }
    
    @Test
    fun testAccountCreation_EmailMode() {
        val result = accountManager.createAccount(
            username = "testuser",
            password = "password123",
            identityMode = IdentityMode.EMAIL,
            email = "test@example.com"
        )
        
        assertTrue(result.isSuccess)
        val account = result.getOrNull()!!
        assertEquals("testuser", account.username)
        assertEquals("EMAIL", account.identityMode)
        assertEquals("test@example.com", account.email)
    }
    
    @Test
    fun testAccountCreation_DeviceIdMode() {
        val result = accountManager.createAccount(
            username = "testuser",
            password = "password123",
            identityMode = IdentityMode.DEVICE_ID
        )
        
        assertTrue(result.isSuccess)
        val account = result.getOrNull()!!
        assertEquals("testuser", account.username)
        assertEquals("DEVICE_ID", account.identityMode)
        assertNotNull(account.deviceId)
        assertTrue(account.deviceId!!.isNotBlank())
    }
    
    @Test
    fun testAccountCreation_EmailModeWithoutEmail_Fails() {
        val result = accountManager.createAccount(
            username = "testuser",
            password = "password123",
            identityMode = IdentityMode.EMAIL,
            email = null
        )
        
        assertTrue(result.isFailure)
    }
    
    @Test
    fun testPasswordSalt_IsUnique() {
        val salt1 = accountManager.generateSalt()
        val salt2 = accountManager.generateSalt()
        val salt3 = accountManager.generateSalt()
        
        // All salts should be unique
        assertNotEquals(salt1, salt2)
        assertNotEquals(salt2, salt3)
        assertNotEquals(salt1, salt3)
    }
    
    @Test
    fun testPasswordHashing_PBKDF2Iterations() {
        val password = "testPassword123"
        val salt = accountManager.generateSalt()
        
        val startTime = System.currentTimeMillis()
        accountManager.hashPassword(password, salt)
        val endTime = System.currentTimeMillis()
        
        // 100,000 iterations should take measurable time (at least 50ms on most devices)
        val duration = endTime - startTime
        assertTrue("PBKDF2 hashing too fast (possible low iteration count)", duration >= 50)
    }
    
    @Test
    fun testAccountCreation_WeakPassword_Allowed() {
        // Manager doesn't enforce password strength (that's ViewModel's job)
        val result = accountManager.createAccount(
            username = "testuser",
            password = "123", // Very weak password
            identityMode = IdentityMode.LOCAL
        )
        
        // Manager still creates account (strength validation happens in ViewModel)
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun testAccountCreation_EmptyUsername_Allowed() {
        // Manager doesn't enforce validation (that's ViewModel's job)
        val result = accountManager.createAccount(
            username = "", // Empty username
            password = "password123",
            identityMode = IdentityMode.LOCAL
        )
        
        // Manager still creates account (validation happens in ViewModel)
        assertTrue(result.isSuccess)
    }
}
