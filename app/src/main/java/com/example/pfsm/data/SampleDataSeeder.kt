package com.example.pfsm.data


import com.example.pfsm.data.entities.BudgetEntity
import com.example.pfsm.data.entities.CategoryEntity
import com.example.pfsm.data.entities.TransactionEntity
import com.example.pfsm.data.repository.BudgetRepository
import com.example.pfsm.data.repository.CategoryRepository
import com.example.pfsm.data.repository.DailyLimitRepository
import com.example.pfsm.data.repository.TransactionRepository
import java.time.LocalDate
import kotlin.random.Random


class SampleDataSeeder(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val dailyLimitRepository: DailyLimitRepository
) {

    suspend fun seed(userId: Int) {
        if (categoryRepository.countForUser(userId) > 0) return

        val today = LocalDate.now()
        val start = today.minusMonths(5).withDayOfMonth(1)

        // ---------- Categories ----------
        val food = addCategory(userId, "Food", "food", "expense")
        val groceries = addCategory(userId, "Groceries", "groceries", "expense")
        val transport = addCategory(userId, "Transport", "transport", "expense")
        val shopping = addCategory(userId, "Shopping", "shopping", "expense")
        val entertainment = addCategory(userId, "Entertainment", "entertainment", "expense")
        val health = addCategory(userId, "Health", "health", "expense")
        val travel = addCategory(userId, "Travel", "travel", "expense")
        val utilities = addCategory(userId, "Utilities", "utilities", "expense")

        val salary = addCategory(userId, "Salary", "salary", "income")
        val freelance = addCategory(userId, "Freelance", "investment", "income")
        val bonus = addCategory(userId, "Bonus", "bonus", "income")

        // ---------- Daily limit history (limit changes over time) ----------
        dailyLimitRepository.setLimit(userId, 1000.0, start.toString())
        dailyLimitRepository.setLimit(userId, 1500.0, today.minusDays(45).toString())
        dailyLimitRepository.setLimit(userId, 1200.0, today.minusDays(10).toString())

        // ---------- Transactions ----------
        val random = Random(42) // fixed seed -> same demo pattern every time
        var counter = 0L

        suspend fun txn(date: LocalDate, cat: Int, type: String, amount: Double, desc: String) {
            counter++
            transactionRepository.add(
                TransactionEntity(
                    userId = userId,
                    categoryId = cat,
                    type = type,
                    amount = amount,
                    description = desc.take(15),
                    transactionDate = date.toString(),
                    // increasing createdAt so "recent transactions" show the newest
                    createdAt = date.toEpochDay() * 86_400_000L + counter
                )
            )
        }

        // (category, description, min, max, weight)
        data class Spend(val cat: Int, val desc: String, val min: Int, val max: Int, val weight: Int)

        val spends = listOf(
            Spend(food, "Lunch", 120, 450, 30),
            Spend(food, "Dinner out", 300, 900, 12),
            Spend(food, "Coffee", 80, 220, 15),
            Spend(groceries, "Vegetables", 150, 600, 14),
            Spend(groceries, "Supermarket", 400, 1800, 6),
            Spend(transport, "Auto / Cab", 60, 400, 18),
            Spend(transport, "Fuel", 500, 1500, 5),
            Spend(shopping, "Clothes", 600, 3500, 4),
            Spend(shopping, "Online order", 250, 2200, 5),
            Spend(entertainment, "Movie", 250, 700, 4),
            Spend(entertainment, "Subscription", 99, 649, 3),
            Spend(health, "Pharmacy", 100, 900, 3),
            Spend(travel, "Weekend trip", 1500, 5000, 1)
        )
        val totalWeight = spends.sumOf { it.weight }

        fun pickSpend(): Spend {
            var r = random.nextInt(totalWeight)
            for (s in spends) {
                r -= s.weight
                if (r < 0) return s
            }
            return spends.last()
        }

        var day = start
        while (!day.isAfter(today)) {
            if (day.dayOfMonth == 1) txn(day, salary, "credit", 45000.0, "Monthly salary")
            if (day.dayOfMonth == 5) txn(day, utilities, "debit", 1850.0 + random.nextInt(0, 400), "Electricity")
            if (day.dayOfMonth == 12) txn(day, utilities, "debit", 799.0, "Internet bill")
            if (day.dayOfMonth == 18 && random.nextInt(100) < 60) {
                txn(day, freelance, "credit", (3000 + random.nextInt(0, 9000)).toDouble(), "Freelance work")
            }
            if (day == today.minusMonths(2).withDayOfMonth(15)) {
                txn(day, bonus, "credit", 8000.0, "Festival bonus")
            }

            // 0-3 random expenses per day; always at least one today
            val count = if (day == today) random.nextInt(1, 3) else random.nextInt(0, 4)
            repeat(count) {
                val s = pickSpend()
                val amount = random.nextInt(s.min, s.max + 1).toDouble() +
                        if (random.nextBoolean()) 0.0 else 0.50
                txn(day, s.cat, "debit", amount, s.desc)
            }
            day = day.plusDays(1)
        }

        // ---------- Budgets ----------
        val y = today.year
        val m = today.monthValue
        val prev = today.minusMonths(1)

        budgetRepository.createBudget(
            BudgetEntity(
                userId = userId, name = "Monthly overall", amount = 30000.0,
                periodType = "monthly", month = m, year = y, isOverall = true
            ),
            emptyList()
        )
        budgetRepository.createBudget(
            BudgetEntity(
                userId = userId, name = "Food & Grocery", amount = 9000.0,
                periodType = "monthly", month = m, year = y, isOverall = false
            ),
            listOf(food, groceries)
        )
        budgetRepository.createBudget(
            BudgetEntity(
                userId = userId, name = "Shopping", amount = 4000.0,
                periodType = "monthly", month = m, year = y, isOverall = false
            ),
            listOf(shopping)
        )
        budgetRepository.createBudget(
            BudgetEntity(
                userId = userId, name = "Last month", amount = 28000.0,
                periodType = "monthly", month = prev.monthValue, year = prev.year, isOverall = true
            ),
            emptyList()
        )
        budgetRepository.createBudget(
            BudgetEntity(
                userId = userId, name = "Yearly overall", amount = 450000.0,
                periodType = "yearly", month = null, year = y, isOverall = true
            ),
            emptyList()
        )
    }

    private suspend fun addCategory(userId: Int, name: String, icon: String, type: String): Int =
        categoryRepository.add(
            CategoryEntity(userId = userId, name = name, icon = icon, type = type)
        ).toInt()
}