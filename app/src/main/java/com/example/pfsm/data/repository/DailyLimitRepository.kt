package com.example.pfsm.data.repository

import com.example.pfsm.data.dao.DailyLimitDao
import com.example.pfsm.data.entities.DailyLimitEntity
import kotlinx.coroutines.flow.Flow


class DailyLimitRepository(private val dailyLimitDao: DailyLimitDao) {

    fun getHistoryForUser(userId: Int): Flow<List<DailyLimitEntity>> =
        dailyLimitDao.getAllForUser(userId)

    suspend fun setLimit(userId: Int, amount: Double, effectiveFrom: String) {
        dailyLimitDao.insert(DailyLimitEntity(userId = userId, amount = amount, effectiveFrom = effectiveFrom))
    }
}