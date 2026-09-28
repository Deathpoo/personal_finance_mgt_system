package com.example.pfsm.viewModels

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pfsm.PFSMApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {

        initializer {
            HomeViewModel(
                userRepository = pfsmApplication().container.userRepository,
                dailyLimitRepository = pfsmApplication().container.dailyLimitRepository,
                transactionRepository = pfsmApplication().container.transactionRepository,
                sessionManager = pfsmApplication().container.sessionManager
            )
        }

        initializer {
            ProfileViewModel(
                userRepository = pfsmApplication().container.userRepository,
                dailyLimitRepository = pfsmApplication().container.dailyLimitRepository,
                settingsRepository = pfsmApplication().container.settingsRepository,
                sessionManager = pfsmApplication().container.sessionManager
            )
        }

        initializer {
            CategoryViewModel(
                categoryRepository = pfsmApplication().container.categoryRepository,
                sessionManager = pfsmApplication().container.sessionManager
            )
        }

        initializer {
            AddTransactionViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                transactionRepository = pfsmApplication().container.transactionRepository,
                categoryRepository = pfsmApplication().container.categoryRepository,
                dailyLimitRepository = pfsmApplication().container.dailyLimitRepository,
                sessionManager = pfsmApplication().container.sessionManager
            )
        }


        initializer {
            CalendarViewModel(
                transactionRepository = pfsmApplication().container.transactionRepository,
                dailyLimitRepository = pfsmApplication().container.dailyLimitRepository,
                sessionManager = pfsmApplication().container.sessionManager
            )
        }


        initializer {
            TransactionViewModel(
                transactionRepository = pfsmApplication().container.transactionRepository,
                categoryRepository = pfsmApplication().container.categoryRepository,
                sessionManager = pfsmApplication().container.sessionManager
            )
        }

        initializer {
            DashboardViewModel(
                transactionRepository = pfsmApplication().container.transactionRepository,
                categoryRepository = pfsmApplication().container.categoryRepository,
                dailyLimitRepository = pfsmApplication().container.dailyLimitRepository,
                sessionManager = pfsmApplication().container.sessionManager
            )
        }


        initializer {
            BudgetViewModel(
                budgetRepository = pfsmApplication().container.budgetRepository,
                transactionRepository = pfsmApplication().container.transactionRepository,
                sessionManager = pfsmApplication().container.sessionManager
            )
        }


        initializer {
            AddBudgetViewModel(
                budgetRepository = pfsmApplication().container.budgetRepository,
                categoryRepository = pfsmApplication().container.categoryRepository,
                sessionManager = pfsmApplication().container.sessionManager
            )
        }

        initializer {
            BudgetDetailViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                budgetRepository = pfsmApplication().container.budgetRepository,
                transactionRepository = pfsmApplication().container.transactionRepository,
                categoryRepository = pfsmApplication().container.categoryRepository,
                sessionManager = pfsmApplication().container.sessionManager
            )
        }

        initializer {
            SignInViewModel(
                userRepository = pfsmApplication().container.userRepository,
                sessionManager = pfsmApplication().container.sessionManager
            )
        }

        initializer {
            SignUpViewModel(
                userRepository = pfsmApplication().container.userRepository,
                sessionManager = pfsmApplication().container.sessionManager
            )
        }
    }
}

fun CreationExtras.pfsmApplication(): PFSMApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as PFSMApplication)