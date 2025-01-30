package com.halead.catalog.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Single data store reference
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class Settings @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val PERSPECTIVE_SWITCH_KEY = booleanPreferencesKey("example_counter")
    val perspectiveSwitchValue: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            // No type safety.
            preferences[PERSPECTIVE_SWITCH_KEY] ?: true
        }

    suspend fun changePerspectiveSwitch(value: Boolean) {
        context.dataStore.edit { settings ->
            settings[PERSPECTIVE_SWITCH_KEY] = value
        }
    }
}
