package com.example.pfsm.data.repository

import com.example.pfsm.data.datautil.DailyTotal
import com.example.pfsm.data.datautil.MonthTotal
import com.example.pfsm.data.dao.TransactionDao
import com.example.pfsm.data.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow



class TransactionRepository(private val transactionDao: TransactionDao) {

    suspend fun add(txn: TransactionEntity): Long = transactionDao.insert(txn)

    suspend fun update(txn: TransactionEntity) = transactionDao.update(txn)

    suspend fun delete(txn: TransactionEntity) = transactionDao.delete(txn)

    suspend fun getById(id: Int, userId: Int): TransactionEntity? = transactionDao.getById(id, userId)
    fun getRecent(userId: Int): Flow<List<TransactionEntity>> =
        transactionDao.getRecent(userId)

    fun getAllForUser(userId: Int): Flow<List<TransactionEntity>> =
        transactionDao.getAllForUser(userId)



    fun getForDate(userId: Int, date: String): Flow<List<TransactionEntity>> =
        transactionDao.getForDate(userId, date)

    fun getTotal(userId: Int, type: String, start: String, end: String): Flow<Double> =
        transactionDao.getTotalFlow(userId, type, start, end)

    fun getDailySpend(userId: Int, date: String): Flow<Double> =
        transactionDao.getDailySpendFlow(userId, date)

    fun getDailyTotalsForMonth(userId: Int, start: String, end: String): Flow<List<DailyTotal>> =
        transactionDao.getDailyTotalsForMonthFlow(userId, start, end)



    suspend fun getDailySpendOnce(userId: Int, date: String): Double =
        transactionDao.getDailySpendOnce(userId, date)
}
