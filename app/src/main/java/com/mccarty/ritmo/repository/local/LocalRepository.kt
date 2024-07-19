package com.mccarty.ritmo.repository.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mccarty.ritmo.domain.model.RecentlyPlayedItem
import com.mccarty.ritmo.repository.Constants.INSERTION_TIME_PREFERENCES_KEY
import com.mccarty.ritmo.repository.Constants.INTERVAL_PREFERENCES_KEY
import com.mccarty.ritmo.repository.Constants.USER_PREFERENCES_NAME
import com.mccarty.ritmo.repository.db.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalRepository @Inject constructor (
    private val context: Context,
    private val db: AppDatabase,
    ) {
    data class UserPreferences(val spotifyRetryInterval: Int)

    // TODO: seconds Long?
    suspend fun saveRetryIntervalSeconds(seconds: Int, insertionTime: Long) {
        context.dataStore.edit {
            it[PreferencesKeys.RETRY_INTERVAL] = seconds
        }
        context.dataStore.edit {
            //it[PreferencesKeys.INSERTION_TIME] = insertionTime
        }
    }

    suspend fun saveRetryInsertionTime(insertionTime: Long) {
        context.dataStore.edit {
            it[PreferencesKeys.INSERTION_TIME] = insertionTime
        }
    }

    fun getRetryIntervalSeconds(): Flow<Int> {
        return context.dataStore.data.map {
            it[PreferencesKeys.RETRY_INTERVAL] ?: 0
        }
    }

    fun getInsertionTimeSeconds(): Flow<Long> {
        return context.dataStore.data.map {
            it[PreferencesKeys.INSERTION_TIME] ?: 0
        }
    }

    companion object {
        val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)
    }

    private object PreferencesKeys {
        val RETRY_INTERVAL = intPreferencesKey(INTERVAL_PREFERENCES_KEY)
        val INSERTION_TIME = longPreferencesKey(INSERTION_TIME_PREFERENCES_KEY)
    }

    suspend fun insertRecentlyPlayedList(list: List<RecentlyPlayedItem>) = db.musicDao().insertRecentlyPlayedItems(list)

    fun getRecentlyPlayed() = db.musicDao().getRecentlyPlayed()
}