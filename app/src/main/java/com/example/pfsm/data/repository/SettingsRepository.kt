package com.example.pfsm.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore("settings")

class SettingsRepository(private val context: Context) {

    private val THEME_MODE = stringPreferencesKey("theme_mode") // "light" / "dark" / "system"



    val themeMode: Flow<String> =
        context.settingsDataStore.data.map { it[THEME_MODE] ?: "system" }



    suspend fun setThemeMode(value: String) {
        context.settingsDataStore.edit { it[THEME_MODE] = value }
    }
}