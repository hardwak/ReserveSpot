package com.pwr_zpi.reservespotapp.data

import android.content.Context
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
        private val CURRENT_CITY = stringPreferencesKey("city")
    }

    suspend fun saveBackendToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[BACKEND_TOKEN_KEY] = token
        }
    }

    suspend fun saveCity(city: String?) {
        context.dataStore.edit { prefs ->
            if (city != null) {
                prefs[CURRENT_CITY] = city
            }
            else {
                prefs.remove(CURRENT_CITY)
            }
        }
    }

    val backendToken: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[BACKEND_TOKEN_KEY] }

    val city: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[CURRENT_CITY] }


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

    suspend fun getCity(): String? {
        return context.dataStore.data.map { prefs -> prefs[CURRENT_CITY] }.first()
    }


}
