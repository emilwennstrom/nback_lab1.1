package mobappdev.example.nback_cimpl.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import mobappdev.example.nback_cimpl.ui.viewmodels.GameSettings
import java.io.IOException

/**
 * This repository provides a way to interact with the DataStore api,
 * with this API you can save key:value pairs
 *
 * Currently this file contains only one thing: getting the highscore as a flow
 * and writing to the highscore preference.
 * (a flow is like a waterpipe; if you put something different in the start,
 * the end automatically updates as long as the pipe is open)
 *
 * Date: 25-08-2023
 * Version: Skeleton code version 1.0
 * Author: Yeetivity
 *
 */

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val HIGHSCORE = intPreferencesKey("highscore")
        val EVENTS = intPreferencesKey("events")
        val NBACK = intPreferencesKey("nback")
        val GRID_SIZE = intPreferencesKey("gridSize")
        val AUDIO_COMBINATIONS = intPreferencesKey("audioCombinations")
        val EVENT_INTERVAL = longPreferencesKey("eventInterval")
        const val TAG = "UserPreferencesRepo"
    }

    val highscore: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[HIGHSCORE] ?: 0
        }

    val events: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[EVENTS] ?: 10
        }

    val nback: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[NBACK] ?: 1
        }


    val gridSize: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[GRID_SIZE] ?: 3
        }


    val audioCombinations: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[AUDIO_COMBINATIONS] ?: 3
        }

    val eventInterval: Flow<Long> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[EVENT_INTERVAL] ?: 2000
        }


    suspend fun saveHighScore(score: Int) {
        dataStore.edit { preferences ->
            preferences[HIGHSCORE] = score
        }
    }

    suspend fun saveSettings(settings: GameSettings) {
        dataStore.edit { preferences ->
            preferences[GRID_SIZE] = settings.gridSize
        }
        dataStore.edit { preferences ->
            preferences[EVENTS] = settings.events
        }
        dataStore.edit { preferences ->
            preferences[AUDIO_COMBINATIONS] = settings.audioCombinations
        }
        dataStore.edit { preferences ->
            preferences[NBACK] = settings.nBack
        }
        dataStore.edit { preferences ->
            preferences[EVENT_INTERVAL] = settings.eventInterval
        }

    }

}