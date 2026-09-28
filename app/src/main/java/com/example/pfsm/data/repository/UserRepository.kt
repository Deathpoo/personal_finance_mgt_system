package com.example.pfsm.data.repository

import com.example.pfsm.data.dao.UserDao
import com.example.pfsm.data.entities.UserEntity
import kotlinx.coroutines.flow.Flow


class UserRepository(private val userDao: UserDao) {

    fun getUser(userId: Int): Flow<UserEntity?> = userDao.getUser(userId)

    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)

    suspend fun register(user: UserEntity): Long = userDao.insert(user)

    suspend fun updateProfile(user: UserEntity) = userDao.update(user)

    suspend fun updateProfilePic(userId: Int, uri: String) =
        userDao.updateProfilePic(userId, uri)

    suspend fun updateName(userId: Int, name: String) =
        userDao.updateName(userId, name)

    suspend fun updatePassword(userId: Int, newHash: String) =
        userDao.updatePasswordHash(userId, newHash)
}