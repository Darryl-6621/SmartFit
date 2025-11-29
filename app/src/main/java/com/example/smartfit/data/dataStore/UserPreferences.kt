package com.example.smartfit.data.dataStore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class UserPreferences(private val context: Context) {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val EMAIL_KEY = stringPreferencesKey("user_email")
        private val PROFILE_IMAGE_URI_KEY = stringPreferencesKey("profile_image_uri")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE_KEY] ?: false }
    val email: Flow<String?> = context.dataStore.data.map { it[EMAIL_KEY] ?: "User" }
    val profileImageUri: Flow<String?> = context.dataStore.data.map { it[PROFILE_IMAGE_URI_KEY] }

    suspend fun saveThemeMode(isDarkMode: Boolean) {
        context.dataStore.edit { it[DARK_MODE_KEY] = isDarkMode }
    }

    suspend fun saveEmail(email: String) {
        context.dataStore.edit { it[EMAIL_KEY] = email }
    }

    suspend fun saveProfileImageUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri == null) preferences.remove(PROFILE_IMAGE_URI_KEY)
            else preferences[PROFILE_IMAGE_URI_KEY] = uri
        }
    }

    fun isLoggedInBlocking(): Boolean = runBlocking {
        context.dataStore.data.map { it[EMAIL_KEY] != null }.first()
    }

    suspend fun logout() {
        context.dataStore.edit { it.clear() }
    }
}