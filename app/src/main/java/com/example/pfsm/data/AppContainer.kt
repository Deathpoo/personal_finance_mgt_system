package com.example.pfsm.data



import android.content.Context

import com.example.pfsm.data.database.AppDatabase
import com.example.pfsm.data.repository.BudgetRepository
import com.example.pfsm.data.repository.CategoryRepository
import com.example.pfsm.data.repository.DailyLimitRepository
import com.example.pfsm.data.repository.SettingsRepository
import com.example.pfsm.data.repository.TransactionRepository
import com.example.pfsm.data.repository.UserRepository
import com.example.pfsm.data.session.SessionManager

interface AppContainer {
    val userRepository: UserRepository
    val dailyLimitRepository: DailyLimitRepository
    val categoryRepository: CategoryRepository
    val transactionRepository: TransactionRepository
    val budgetRepository: BudgetRepository
    val settingsRepository: SettingsRepository
    val sessionManager: SessionManager
}

class AppDataContainer(private val context: Context) : AppContainer {

    private val database by lazy { AppDatabase.getInstance(context) }

    override val sessionManager: SessionManager by lazy { SessionManager(context) }
    override val settingsRepository: SettingsRepository by lazy { SettingsRepository(context) }

    override val userRepository: UserRepository by lazy {
        UserRepository(database.userDao())
    }
    override val dailyLimitRepository: DailyLimitRepository by lazy {
        DailyLimitRepository(database.dailyLimitDao())
    }
    override val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(database.categoryDao())
    }
    override val transactionRepository: TransactionRepository by lazy {
        TransactionRepository(database.transactionDao())
    }
    override val budgetRepository: BudgetRepository by lazy {
        BudgetRepository(database.budgetDao())
    }
}