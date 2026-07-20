package com.development.forge.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Single process-wide DataStore instance, keyed off the application Context via the
// property delegate (the delegate enforces one instance per file name per process).
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "forge_preferences")

/**
 * Persistent user preferences for Forge. Currently holds only the theme flag.
 *
 * Backed by Jetpack [DataStore] (`preferencesDataStore`). [isDarkTheme] is a cold
 * `Flow<Boolean>` that emits the stored value and every subsequent change;
 * [setDarkTheme] is a `suspend` setter that writes transactionally off the main
 * thread (DataStore handles its own IO dispatcher).
 */
@Singleton
class ForgePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.dataStore

    /**
     * Cold stream of the dark-theme flag: emits the stored value immediately, then
     * again on every change. Defaults to dark ([DEFAULT_IS_DARK]) when unset.
     */
    fun isDarkTheme(): Flow<Boolean> =
        dataStore.data
            .map { prefs -> prefs[KEY_THEME_IS_DARK] ?: DEFAULT_IS_DARK }
            .distinctUntilChanged()

    /** Persists the dark-theme flag. DataStore performs the write off the main thread. */
    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME_IS_DARK] = isDark
        }
    }

    private companion object {
        val KEY_THEME_IS_DARK = booleanPreferencesKey("theme_is_dark")
        const val DEFAULT_IS_DARK = true
    }
}
