package com.temizlepro.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val includeDuplicatesKey = booleanPreferencesKey("include_duplicates")
    private val largeThresholdMbKey = intPreferencesKey("large_threshold_mb")

    val includeDuplicates: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[includeDuplicatesKey] ?: true
    }

    val largeThresholdMb: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[largeThresholdMbKey] ?: 200
    }

    suspend fun setIncludeDuplicates(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[includeDuplicatesKey] = value
        }
    }

    suspend fun setLargeThresholdMb(value: Int) {
        context.dataStore.edit { prefs ->
            prefs[largeThresholdMbKey] = value
        }
    }
}
