package com.example.pfsm.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore("session")

class SessionManager(private val context: Context) {

    private val CURRENT_USER_ID = intPreferencesKey("current_user_id")

    val currentUserId: Flow<Int?> = context.sessionDataStore.data.map { it[CURRENT_USER_ID] }

    suspend fun setCurrentUser(userId: Int) {
        context.sessionDataStore.edit { it[CURRENT_USER_ID] = userId }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { it.remove(CURRENT_USER_ID) }
    }
}