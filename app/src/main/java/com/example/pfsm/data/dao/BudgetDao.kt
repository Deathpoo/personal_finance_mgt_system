package com.example.pfsm.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.example.pfsm.data.entities.BudgetCategoryCrossRef
import com.example.pfsm.data.entities.BudgetEntity
import com.example.pfsm.data.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

data class BudgetWithCategories(
    @Embedded val budget: BudgetEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BudgetCategoryCrossRef::class,
            parentColumn = "budgetId",
            entityColumn = "categoryId"
        )
    )
    val categories: List<CategoryEntity>
)

@Dao
interface BudgetDao {

    @Insert
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Insert
    suspend fun insertCrossRefs(refs: List<BudgetCategoryCrossRef>)

    @Transaction
    suspend fun createBudget(budget: BudgetEntity, categoryIds: List<Int>) {
        val id = insertBudget(budget).toInt()
        if (categoryIds.isNotEmpty()) {
            insertCrossRefs(categoryIds.map { BudgetCategoryCrossRef(id, it) })
        }
    }

    @Transaction
    @Query("SELECT * FROM budgets WHERE id = :budgetId AND userId = :userId")
    fun getBudgetWithCategoriesById(budgetId: Int, userId: Int): Flow<BudgetWithCategories?>


    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Update
    suspend fun update(budget: BudgetEntity)

    @Query("DELETE FROM budget_categories WHERE budgetId = :budgetId")
    suspend fun deleteCrossRefsForBudget(budgetId: Int)


    @Transaction
    suspend fun updateBudget(budget: BudgetEntity, categoryIds: List<Int>) {
        update(budget)
        deleteCrossRefsForBudget(budget.id)
        if (categoryIds.isNotEmpty()) {
            insertCrossRefs(categoryIds.map { BudgetCategoryCrossRef(budget.id, it) })
        }
    }

    @Transaction
    @Query("""
        SELECT * FROM budgets 
        WHERE userId = :userId AND year = :year AND periodType = :periodType
        AND (periodType != 'monthly' OR month = :month)
    """)
    fun getBudgetsWithCategories(
        userId: Int,
        year: Int,
        periodType: String,
        month: Int
    ): Flow<List<BudgetWithCategories>>
}