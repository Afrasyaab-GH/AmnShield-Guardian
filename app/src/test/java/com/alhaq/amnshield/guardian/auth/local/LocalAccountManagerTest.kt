package com.alhaq.amnshield.guardian.auth.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for LocalAccountManager
 * 
 * Tests account creation, authentication, and validation against EncryptedSharedPreferences.
 */
@RunWith(AndroidJUnit4::class)
class LocalAccountManagerTest {
    
    private lateinit var context: Context
    private lateinit var accountManager: LocalAccountManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        accountManager = LocalAccountManager.getInstance(context)
        
        // Clear any existing accounts by deleting shared preferences file
        context.getSharedPreferences("deenshield_local_accounts", Context.MODE_PRIVATE).edit().clear().commit()
    }
    
    @After
    fun tearDown() {
        context.getSharedPreferences("deenshield_local_accounts", Context.MODE_PRIVATE).edit().clear().commit()
    }
    
    @Test
    fun testAccountCreation_Success() {
        val prefs = context.getSharedPreferences("deenshield_local_accounts", Context.MODE_PRIVATE)
        println("TEST DEBUG: all keys in prefs: ${prefs.all.keys}")
        println("TEST DEBUG: contains username: ${prefs.contains("account_username")}")
        val result = accountManager.createAccount("testuser", "password123")
        assertTrue("Account creation failed: ${result.error}", result.success)
        assertTrue(accountManager.accountExists())
        assertEquals("testuser", accountManager.getUsername())
    }
    
    @Test
    fun testAccountCreation_Duplicate_Fails() {
        val firstResult = accountManager.createAccount("testuser", "password123")
        assertTrue(firstResult.success)
        
        val secondResult = accountManager.createAccount("anotheruser", "password456")
        assertFalse(secondResult.success)
        assertEquals("Account already exists. Delete existing account first.", secondResult.error)
    }
    
    @Test
    fun testAuthentication_Success() {
        accountManager.createAccount("testuser", "password123")
        
        val authResult = accountManager.authenticateAccount("testuser", "password123")
        assertTrue(authResult.success)
    }
    
    @Test
    fun testAuthentication_WrongPassword_Fails() {
        accountManager.createAccount("testuser", "password123")
        
        val authResult = accountManager.authenticateAccount("testuser", "wrongpassword")
        assertFalse(authResult.success)
        assertEquals("Invalid username or password", authResult.error)
    }
    
    @Test
    fun testAuthentication_WrongUsername_Fails() {
        accountManager.createAccount("testuser", "password123")
        
        val authResult = accountManager.authenticateAccount("wronguser", "password123")
        assertFalse(authResult.success)
        assertEquals("Invalid username or password", authResult.error)
    }
    
    @Test
    fun testAccountCreation_InvalidUsername_Fails() {
        // Username too short
        val shortUsernameResult = accountManager.createAccount("ab", "password123")
        assertFalse(shortUsernameResult.success)
        
        // Username contains invalid characters
        val invalidCharResult = accountManager.createAccount("test user", "password123")
        assertFalse(invalidCharResult.success)
    }
    
    @Test
    fun testAccountCreation_InvalidPassword_Fails() {
        // Password too short
        val shortPasswordResult = accountManager.createAccount("testuser", "1234567")
        assertFalse(shortPasswordResult.success)
    }
}
