package com.example.pfsm.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pfsm.data.entities.UserEntity
import com.example.pfsm.data.repository.UserRepository
import com.example.pfsm.data.session.SessionManager
import com.example.pfsm.ui.theme.util.hashPassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignUpUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)

class SignUpViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(username = value, usernameError = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    }

    fun submit() {
        val state = _uiState.value
        var hasError = false

        if (state.username.isBlank()) {
            _uiState.update { it.copy(usernameError = "Enter your name") }
            hasError = true
        }
        if (state.email.isBlank() || !state.email.contains("@")) {
            _uiState.update { it.copy(emailError = "Enter a valid email") }
            hasError = true
        }
        val hasLetter = state.password.any { it.isLetter() }
        val hasDigit = state.password.any { it.isDigit() }
        if (state.password.length < 6 || !hasLetter || !hasDigit) {
            _uiState.update {
                it.copy(passwordError = "At least 6 characters, with both letters and numbers")
            }
            hasError = true
        }
        if (state.confirmPassword != state.password) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords don't match") }
            hasError = true
        }
        if (hasError) return

        _uiState.update { it.copy(isLoading = true, generalError = null) }

        viewModelScope.launch {
            val existing = userRepository.getUserByEmail(state.email.trim())
            if (existing != null) {
                _uiState.update { it.copy(isLoading = false, generalError = "An account with this email already exists") }
                return@launch
            }

            val newUser = UserEntity(
                username = state.username.trim(),
                email = state.email.trim(),
                passwordHash = hashPassword(state.password),
                createdAt = System.currentTimeMillis().toString()
            )
            val newUserId = userRepository.register(newUser).toInt()
            sessionManager.setCurrentUser(newUserId)
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}