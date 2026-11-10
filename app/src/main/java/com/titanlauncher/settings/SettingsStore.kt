package com.titanlauncher.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    /** Personagem ligado ou desligado. Padrão: ligado. */
    val titanEnabled: Flow<Boolean> = store.data.map { it[TITAN_ENABLED] ?: true }

    suspend fun setTitanEnabled(value: Boolean) {
        store.edit { it[TITAN_ENABLED] = value }
    }

    private companion object {
        val TITAN_ENABLED = booleanPreferencesKey("titan_enabled")
    }
}
