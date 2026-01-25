package com.deenshield.blocker.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deenshield.blocker.auth.AuthState
import com.deenshield.blocker.auth.IdentityMode
import com.deenshield.blocker.auth.data.GuardianDatabase
import com.deenshield.blocker.auth.data.LocalAccount
import org.alhaq.deenshield.guardian.auth.local.LocalAccountManager
import org.alhaq.deenshield.guardian.auth.local.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication flows
 * 
 * Manages authentication state and coordinates between:
 * - LocalAccountManager (password hashing/validation)
 * - GuardianDatabase (persistence)
 * - TokenStorage (capability tokens)
 * 
 * **Islamic Principle: Amanah (Trust)**
 * - All operations transparent via state flow
 * - Errors exposed clearly to UI
 * - No hidden state changes
 * 
 * **Thread Safety:**
 * - All state updates via MutableStateFlow
 * - Database operations on viewModelScope
 * - UI observes via StateFlow
 * 
 * @property accountManager Local account management
 * @property tokenStorage Token encryption/storage
 * @property database Room database access
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val accountManager: LocalAccountManager,
    private val tokenStorage: TokenStorage,
    private val database: GuardianDatabase
) : ViewModel() {
    
    // Authentication state exposed to UI
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // Selected identity mode during setup
    private val _selectedIdentityMode = MutableStateFlow<IdentityMode?>(null)
    val selectedIdentityMode: StateFlow<IdentityMode?> = _selectedIdentityMode.asStateFlow()
    
    // Current logged-in account
    private val _currentAccount = MutableStateFlow<LocalAccount?>(null)
    val currentAccount: StateFlow<LocalAccount?> = _currentAccount.asStateFlow()
    
    // Error messages for UI
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Loading state for UI indicators
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        // Check if user already authenticated on startup
        checkExistingAuth()
    }
    
    /**
     * Check if user has existing authenticated session
     * 
     * Called on ViewModel initialization.
     */
    private fun checkExistingAuth() {
        viewModelScope.launch {
            try {
                // Check if account exists in database
                database.localAccountDao().getActiveAccounts()
                    .firstOrNull()
                    ?.firstOrNull()
                    ?.let { account ->
                        _currentAccount.value = account
                        _authState.value = AuthState.Authenticated(account.username)
                    }
            } catch (e: Exception) {
                // No existing auth, remain unauthenticated
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
    
    /**
     * Select identity mode during setup
     * 
     * @param mode Selected IdentityMode (EMAIL, LOCAL, DEVICE_ID, NO_IDENTITY)
     */
    fun selectIdentityMode(mode: IdentityMode) {
        _selectedIdentityMode.value = mode
        _errorMessage.value = null
    }
    
    /**
     * Create new local account
     * 
     * Validates input, creates account via LocalAccountManager,
     * stores in database, and transitions to Authenticated state.
     * 
     * @param username Display name
     * @param password Plaintext password (will be hashed)
     * @param email Optional email address (required for EMAIL mode)
     */
    fun createAccount(
        username: String,
        password: String,
        email: String? = null
    ) {
        val identityMode = _selectedIdentityMode.value
        if (identityMode == null) {
            _errorMessage.value = "Please select an identity mode first"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _authState.value = AuthState.Loading
                
                // Validate inputs
                when {
                    username.isBlank() -> {
                        _errorMessage.value = "Username cannot be empty"
                        _authState.value = AuthState.Unauthenticated
                        return@launch
                    }
                    password.length < 8 -> {
                        _errorMessage.value = "Password must be at least 8 characters"
                        _authState.value = AuthState.Unauthenticated
                        return@launch
                    }
                    identityMode == IdentityMode.EMAIL && email.isNullOrBlank() -> {
                        _errorMessage.value = "Email is required for email account mode"
                        _authState.value = AuthState.Unauthenticated
                        return@launch
                    }
                }
                
                // Check if username already exists
                val usernameExists = database.localAccountDao()
                    .usernameExists(username) > 0
                
                if (usernameExists) {
                    _errorMessage.value = "Username already exists"
                    _authState.value = AuthState.Unauthenticated
                    return@launch
                }
                
                // Create account via LocalAccountManager
                val result = accountManager.createAccount(
                    username = username,
                    password = password,
                    identityMode = identityMode,
                    email = email
                )
                
                if (result.isSuccess) {
                    val account = result.getOrNull()!!
                    
                    // Store in database
                    database.localAccountDao().insert(account)
                    
                    _currentAccount.value = account
                    _authState.value = AuthState.Authenticated(username)
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to create account"
                    _authState.value = AuthState.Unauthenticated
                }
                
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
                _authState.value = AuthState.Unauthenticated
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Login with existing account
     * 
     * Validates credentials via LocalAccountManager and transitions to Authenticated.
     * 
     * @param username Username
     * @param password Plaintext password
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _authState.value = AuthState.Loading
                
                // Validate inputs
                when {
                    username.isBlank() -> {
                        _errorMessage.value = "Username cannot be empty"
                        _authState.value = AuthState.Unauthenticated
                        return@launch
                    }
                    password.isBlank() -> {
                        _errorMessage.value = "Password cannot be empty"
                        _authState.value = AuthState.Unauthenticated
                        return@launch
                    }
                }
                
                // Get account from database
                val account = database.localAccountDao()
                    .getAccountByUsername(username)
                    .firstOrNull()
                
                if (account == null) {
                    _errorMessage.value = "Account not found"
                    _authState.value = AuthState.Unauthenticated
                    return@launch
                }
                
                // Validate password
                val isValidPassword = accountManager.validatePassword(
                    password = password,
                    account = account
                )
                
                if (isValidPassword) {
                    // Update last login timestamp
                    database.localAccountDao().updateLastLogin(
                        id = account.id,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    _currentAccount.value = account
                    _authState.value = AuthState.Authenticated(username)
                } else {
                    _errorMessage.value = "Invalid password"
                    _authState.value = AuthState.Unauthenticated
                }
                
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Login failed"
                _authState.value = AuthState.Unauthenticated
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Logout current user
     * 
     * Clears authentication state and returns to Unauthenticated.
     */
    fun logout() {
        viewModelScope.launch {
            _currentAccount.value = null
            _authState.value = AuthState.Unauthenticated
            _selectedIdentityMode.value = null
            _errorMessage.value = null
        }
    }
    
    /**
     * Clear error message
     * 
     * Called after UI displays error to user.
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Delete account (permanent)
     * 
     * **Warning:** This deletes account and all associated data.
     * 
     * @param account Account to delete
     */
    fun deleteAccount(account: LocalAccount) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Delete account from database
                database.localAccountDao().delete(account)
                
                // If deleting current account, logout
                if (_currentAccount.value?.id == account.id) {
                    logout()
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete account: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Deactivate account (soft delete)
     * 
     * Preferred over deleteAccount(). Account can be reactivated later.
     * 
     * @param account Account to deactivate
     */
    fun deactivateAccount(account: LocalAccount) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Mark account as inactive
                database.localAccountDao().setAccountActive(
                    id = account.id,
                    isActive = false
                )
                
                // If deactivating current account, logout
                if (_currentAccount.value?.id == account.id) {
                    logout()
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to deactivate account: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
