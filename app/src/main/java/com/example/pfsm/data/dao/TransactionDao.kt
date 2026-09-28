package com.example.pfsm.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.pfsm.data.datautil.DailyTotal
import com.example.pfsm.data.datautil.MonthTotal
import com.example.pfsm.data.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(txn: TransactionEntity): Long

    @Update
    suspend fun update(txn: TransactionEntity)

    @Delete
    suspend fun delete(txn: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Int): TransactionEntity?


    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC LIMIT 2")
    fun getRecent(userId: Int): Flow<List<TransactionEntity>>


    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY transactionDate DESC, createdAt DESC")
    fun getAllForUser(userId: Int): Flow<List<TransactionEntity>>


    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE userId = :userId AND type = :type AND transactionDate BETWEEN :start AND :end
    """)
    fun getTotalFlow(userId: Int, type: String, start: String, end: String): Flow<Double>


    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE userId = :userId AND type = 'debit' AND transactionDate = :date
    """)
    fun getDailySpendFlow(userId: Int, date: String): Flow<Double>


    @Query("""
        SELECT transactionDate, SUM(amount) as spent
        FROM transactions 
        WHERE userId = :userId AND type = 'debit' AND transactionDate BETWEEN :start AND :end
        GROUP BY transactionDate
    """)
    fun getDailyTotalsForMonthFlow(userId: Int, start: String, end: String): Flow<List<DailyTotal>>


    @Query("SELECT * FROM transactions WHERE userId = :userId AND transactionDate = :date ORDER BY createdAt DESC")
    fun getForDate(userId: Int, date: String): Flow<List<TransactionEntity>>



    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE userId = :userId AND type = 'debit' AND transactionDate = :date
    """)
    suspend fun getDailySpendOnce(userId: Int, date: String): Double
}