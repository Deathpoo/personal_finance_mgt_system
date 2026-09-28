package com.example.pfsm.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pfsm.data.datautil.resolveDailyLimitForDate
import com.example.pfsm.data.entities.UserEntity
import com.example.pfsm.data.repository.DailyLimitRepository
import com.example.pfsm.data.repository.SettingsRepository
import com.example.pfsm.data.repository.UserRepository
import com.example.pfsm.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ProfileUiState(
    val user: UserEntity? = null,
    val currency: String = "INR",
    val themeMode: String = "system", // "light" / "dark" / "system"
    val currentDailyLimit: Double = 0.0, // whichever limit is in effect as of today
    val isSignedOut: Boolean = false
)

private data class ProfileCombined(
    val user: UserEntity?,
    val themeMode: String,
    val todayLimit: Double
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val dailyLimitRepository: DailyLimitRepository,
    private val settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.currentUserId
                .filterNotNull()
                .collectLatest { userId ->
                    combine(
                        userRepository.getUser(userId),
                        settingsRepository.themeMode,
                        dailyLimitRepository.getHistoryForUser(userId)
                    ) { user, themeMode, limitHistory ->
                        ProfileCombined(
                            user = user,
                            themeMode = themeMode,
                            todayLimit = resolveDailyLimitForDate(limitHistory, LocalDate.now())
                        )
                    }.collectLatest { combined ->
                        _uiState.update {
                            it.copy(
                                user = combined.user,
                                themeMode = combined.themeMode,
                                currentDailyLimit = combined.todayLimit,
                                isSignedOut = false
                            )
                        }
                    }
                }
        }
    }

    fun updateName(newName: String) {
        val userId = _uiState.value.user?.id ?: return
        viewModelScope.launch { userRepository.updateName(userId, newName) }
    }

    fun updateProfilePic(uri: String) {
        val userId = _uiState.value.user?.id ?: return
        viewModelScope.launch { userRepository.updateProfilePic(userId, uri) }
    }


    fun updateDailyLimit(newLimit: Double) {
        val userId = _uiState.value.user?.id ?: return
        viewModelScope.launch {
            dailyLimitRepository.setLimit(
                userId = userId,
                amount = newLimit,
                effectiveFrom = LocalDate.now().toString()
            )
        }
    }


    fun updateThemeMode(mode: String) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun signOut() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _uiState.update { it.copy(isSignedOut = true) }
        }
    }
}