package com.example.pfsm.data.repository

import com.example.pfsm.data.dao.CategoryDao
import com.example.pfsm.data.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow



class CategoryRepository(private val categoryDao: CategoryDao) {

    fun getByType(userId: Int, type: String): Flow<List<CategoryEntity>> =
        categoryDao.getByType(userId, type)

    fun getAllForUser(userId: Int): Flow<List<CategoryEntity>> =
        categoryDao.getAllForUser(userId)

    suspend fun getById(categoryId: Int): CategoryEntity? = categoryDao.getById(categoryId)

    suspend fun add(category: CategoryEntity): Long = categoryDao.insert(category)

    suspend fun delete(category: CategoryEntity) = categoryDao.delete(category)

}