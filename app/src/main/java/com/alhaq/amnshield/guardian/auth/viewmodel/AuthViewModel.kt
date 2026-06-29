package com.alhaq.amnshield.guardian.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alhaq.amnshield.guardian.auth.data.GuardianDatabase
import com.alhaq.amnshield.guardian.auth.data.LocalAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.alhaq.amnshield.guardian.auth.local.LocalAccountManager
import com.alhaq.amnshield.guardian.auth.local.TokenStorage
import com.alhaq.amnshield.guardian.auth.model.AuthState
import com.alhaq.amnshield.guardian.auth.model.IdentityMode
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for authentication flows
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val accountManager: LocalAccountManager,
    private val tokenStorage: TokenStorage,
    private val database: GuardianDatabase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _selectedIdentityMode = MutableStateFlow<IdentityMode?>(null)
    val selectedIdentityMode: StateFlow<IdentityMode?> = _selectedIdentityMode.asStateFlow()

    private val _currentAccount = MutableStateFlow<LocalAccount?>(null)
    val currentAccount: StateFlow<LocalAccount?> = _currentAccount.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkExistingAuth()
    }

    private fun parseIdentityMode(value: String): IdentityMode {
        return runCatching { IdentityMode.valueOf(value) }.getOrDefault(IdentityMode.LOCAL_ACCOUNT)
    }

    private fun checkExistingAuth() {
        viewModelScope.launch {
            try {
                val accounts = database.localAccountDao().getActiveAccounts().firstOrNull()
                val account = accounts?.firstOrNull()
                if (account != null) {
                    _currentAccount.value = account
                    _authState.value = AuthState.Authenticated(
                        identityMode = parseIdentityMode(account.identityMode),
                        userId = account.email ?: account.username,
                        deviceId = account.deviceId
                    )
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (_: Exception) {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun selectIdentityMode(mode: IdentityMode) {
        _selectedIdentityMode.value = mode
        _errorMessage.value = null
    }

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
                    identityMode == IdentityMode.EMAIL_ACCOUNT && email.isNullOrBlank() -> {
                        _errorMessage.value = "Email is required for email account mode"
                        _authState.value = AuthState.Unauthenticated
                        return@launch
                    }
                }

                val usernameExists = database.localAccountDao().usernameExists(username) > 0
                if (usernameExists) {
                    _errorMessage.value = "Username already exists"
                    _authState.value = AuthState.Unauthenticated
                    return@launch
                }

                val result = accountManager.createAccount(username, password)
                if (!result.success) {
                    _errorMessage.value = result.error ?: "Failed to create account"
                    _authState.value = AuthState.Unauthenticated
                    return@launch
                }

                val hash = accountManager.getStoredPasswordHash()
                val salt = accountManager.getStoredSalt()
                if (hash.isNullOrBlank() || salt.isNullOrBlank()) {
                    _errorMessage.value = "Failed to read stored credentials"
                    _authState.value = AuthState.Unauthenticated
                    return@launch
                }

                val deviceId = if (identityMode == IdentityMode.DEVICE_ID) UUID.randomUUID().toString() else null

                val account = LocalAccount(
                    username = username,
                    email = email,
                    passwordHash = hash,
                    passwordSalt = salt,
                    identityMode = identityMode.name,
                    deviceId = deviceId,
                    lastLoginAt = System.currentTimeMillis(),
                    isActive = true
                )

                database.localAccountDao().insert(account)

                _currentAccount.value = account
                _authState.value = AuthState.Authenticated(
                    identityMode = identityMode,
                    userId = email ?: username,
                    deviceId = deviceId
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
                _authState.value = AuthState.Unauthenticated
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _authState.value = AuthState.Loading

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

                val account = database.localAccountDao().getAccountByUsername(username).firstOrNull()
                if (account == null) {
                    _errorMessage.value = "Account not found"
                    _authState.value = AuthState.Unauthenticated
                    return@launch
                }

                val isValidPassword = accountManager.validatePassword(password, account)
                if (isValidPassword) {
                    database.localAccountDao().updateLastLogin(account.id, System.currentTimeMillis())
                    _currentAccount.value = account
                    _authState.value = AuthState.Authenticated(
                        identityMode = parseIdentityMode(account.identityMode),
                        userId = account.email ?: account.username,
                        deviceId = account.deviceId
                    )
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

    fun logout() {
        viewModelScope.launch {
            _currentAccount.value = null
            _authState.value = AuthState.Unauthenticated
            _selectedIdentityMode.value = null
            _errorMessage.value = null
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun deleteAccount(account: LocalAccount) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                database.localAccountDao().delete(account)
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

    fun deactivateAccount(account: LocalAccount) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                database.localAccountDao().setAccountActive(account.id, false)
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
