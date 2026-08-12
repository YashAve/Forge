package com.development.forge.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.development.forge.core.datastore.ForgePreferences.Companion.DEFAULT_IS_DARK
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
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

    private fun <T> preference(key: Preferences.Key<T>, default: T) =
        dataStore.data
            .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
            .map { preferences -> preferences[key] ?: default }
            .distinctUntilChanged()

    private suspend fun <T> setPreference(key: Preferences.Key<T>, value: T) =
        dataStore.edit { preferences ->
            preferences[key] = value
        }

    fun isDarkTheme() = preference(KEY_THEME_IS_DARK, DEFAULT_IS_DARK)
    fun isOnboardingDone() = preference(KEY_ONBOARDING_IS_DONE, DEFAULT_IS_ONBOARDING_DONE)
    fun isAuthTokenStored() = preference(KEY_AUTH_TOKEN, DEFAULT_AUTH_TOKEN)

    /** Persists the dark-theme flag. DataStore performs the write off the main thread. */
    suspend fun setDarkTheme(isDark: Boolean) = setPreference(KEY_THEME_IS_DARK, isDark)
    suspend fun setOnboardingDone(isOnboardingDone: Boolean) = setPreference(KEY_ONBOARDING_IS_DONE, isOnboardingDone)
    suspend fun storeAuthToken(authToken: String) = setPreference(KEY_AUTH_TOKEN, authToken)

    private companion object {
        val KEY_THEME_IS_DARK = booleanPreferencesKey("theme_is_dark")
        val KEY_ONBOARDING_IS_DONE = booleanPreferencesKey("onboarding_is_done")
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_is_stored")
        const val DEFAULT_IS_DARK = true
        const val DEFAULT_IS_ONBOARDING_DONE = false
        const val DEFAULT_AUTH_TOKEN = ""
    }
}
