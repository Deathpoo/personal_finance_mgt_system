package com.example.pfsm.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pfsm.data.repository.UserRepository
import com.example.pfsm.data.session.SessionManager
import com.example.pfsm.ui.theme.util.hashPassword
import com.example.pfsm.ui.theme.util.verifyPassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)

class SignInViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun submit() {
        val state = _uiState.value
        var hasError = false

        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Enter your email") }
            hasError = true
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Enter your password") }
            hasError = true
        }
        if (hasError) return

        _uiState.update { it.copy(isLoading = true, generalError = null) }

        viewModelScope.launch {
            val user = userRepository.getUserByEmail(state.email.trim().lowercase())
            if (user == null) {
                _uiState.update { it.copy(isLoading = false, generalError = "No account found with this email") }
                return@launch
            }
            if (!verifyPassword(state.password, user.passwordHash)) {
                _uiState.update { it.copy(isLoading = false, generalError = "Incorrect password") }
                return@launch
            }
            sessionManager.setCurrentUser(user.id)
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}