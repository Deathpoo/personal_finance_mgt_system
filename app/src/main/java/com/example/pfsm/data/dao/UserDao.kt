package com.example.pfsm.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.pfsm.data.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUser(userId: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("UPDATE users SET profilePicUri = :uri WHERE id = :userId")
    suspend fun updateProfilePic(userId: Int, uri: String)

    @Query("UPDATE users SET username = :name WHERE id = :userId")
    suspend fun updateName(userId: Int, name: String)
}