package com.pwr_zpi.reservespotapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        private val BACKEND_TOKEN_KEY = stringPreferencesKey("backend_token")
        private val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
    }

    suspend fun saveBackendToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[BACKEND_TOKEN_KEY] = token
        }
    }

    suspend fun saveRememberMe(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[REMEMBER_ME_KEY] = enabled
        }
    }

    val backendToken: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[BACKEND_TOKEN_KEY] }

    val rememberMe: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[REMEMBER_ME_KEY] ?: false }


    // Suspend function to read token once
    suspend fun getBackendToken(): String? {
        return context.dataStore.data.map { prefs -> prefs[BACKEND_TOKEN_KEY] }.first()
    }

    // Clear token
    suspend fun clearBackendToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(BACKEND_TOKEN_KEY)
        }
    }


}
