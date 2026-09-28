package com.example.pfsm.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.pfsm.data.entities.DailyLimitEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface DailyLimitDao {
    @Insert
    suspend fun insert(entity: DailyLimitEntity): Long

    @Query("SELECT * FROM daily_limits WHERE userId = :userId ORDER BY effectiveFrom ASC")
    fun getAllForUser(userId: Int): Flow<List<DailyLimitEntity>>
}