package com.example.twosize.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "twosize_prefs")

class UserPrefsRepository(private val context: Context) {
    private val keyUserId = stringPreferencesKey("user_id")
    private val keyRoomCode = stringPreferencesKey("room_code")

    val userIdFlow: Flow<String?> = context.dataStore.data.map { it[keyUserId] }
    val roomCodeFlow: Flow<String?> = context.dataStore.data.map { it[keyRoomCode] }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { prefs: MutablePreferences ->
            prefs[keyUserId] = userId
        }
    }

    suspend fun saveRoomCode(roomCode: String) {
        context.dataStore.edit { prefs: MutablePreferences ->
            prefs[keyRoomCode] = roomCode
        }
    }
}
