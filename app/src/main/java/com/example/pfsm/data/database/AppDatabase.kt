package com.example.pfsm.data.database



import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pfsm.data.entities.UserEntity
import com.example.pfsm.data.entities.DailyLimitEntity
import com.example.pfsm.data.entities.TransactionEntity
import com.example.pfsm.data.entities.CategoryEntity
import com.example.pfsm.data.entities.BudgetEntity
import com.example.pfsm.data.entities.BudgetCategoryCrossRef
import com.example.pfsm.data.dao.BudgetDao
import com.example.pfsm.data.dao.CategoryDao
import com.example.pfsm.data.dao.DailyLimitDao
import com.example.pfsm.data.dao.TransactionDao
import com.example.pfsm.data.dao.UserDao


@Database(
    entities = [
        UserEntity::class,
        DailyLimitEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        BudgetCategoryCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun dailyLimitDao(): DailyLimitDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_app.db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}