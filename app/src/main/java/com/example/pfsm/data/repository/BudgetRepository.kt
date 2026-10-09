package com.example.pfsm.data.repository

import com.example.pfsm.data.dao.BudgetDao
import com.example.pfsm.data.dao.BudgetWithCategories
import com.example.pfsm.data.entities.BudgetEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {

    suspend fun createBudget(budget: BudgetEntity, categoryIds: List<Int>) =
        budgetDao.createBudget(budget, categoryIds)

    suspend fun delete(budget: BudgetEntity) = budgetDao.delete(budget)

    suspend fun updateBudget(budget: BudgetEntity, categoryIds: List<Int>) =
        budgetDao.updateBudget(budget, categoryIds)

    fun getBudgetWithCategoriesById(budgetId: Int, userId: Int): Flow<BudgetWithCategories?> =
        budgetDao.getBudgetWithCategoriesById(budgetId, userId)

    fun getBudgetsWithCategories(
        userId: Int,
        year: Int,
        periodType: String,
        month: Int
    ): Flow<List<BudgetWithCategories>> =
        budgetDao.getBudgetsWithCategories(userId, year, periodType, month)
}