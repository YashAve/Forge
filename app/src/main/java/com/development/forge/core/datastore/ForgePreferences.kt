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
 * Persistent user preferences and session data for Forge.
 *
 * Backed by Jetpack [DataStore] (`preferencesDataStore`). Readers return cold
 * `Flow`s that emit the stored value immediately and then on every change; setters
 * are `suspend` and write transactionally off the main thread (DataStore handles its
 * own IO dispatcher). Related keys are written or removed together in a single
 * `edit { }` so each grouped change is one file rewrite.
 */
@Singleton
class ForgePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.dataStore

    /**
     * Cold stream of a single preference: emits the stored value immediately, then
     * again on every change. Falls back to [default] when the key is unset.
     */
    private fun <T> preference(key: Preferences.Key<T>, default: T) =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences -> preferences[key] ?: default }
            .distinctUntilChanged()

    // ---- Readers ----

    fun isDarkTheme() = preference(KEY_THEME_IS_DARK, DEFAULT_IS_DARK)
    fun isOnboardingDone() = preference(KEY_ONBOARDING_IS_DONE, DEFAULT_IS_ONBOARDING_DONE)
    fun authToken() = preference(KEY_AUTH_TOKEN, DEFAULT_AUTH_TOKEN)
    fun userEmail() = preference(KEY_EMAIL, DEFAULT_EMAIL)

    // ---- Writers ----

    /** Persists the dark-theme flag. */
    suspend fun setDarkTheme(isDark: Boolean) =
        dataStore.edit { it[KEY_THEME_IS_DARK] = isDark }

    suspend fun setOnboardingDone(isOnboardingDone: Boolean) =
        dataStore.edit { it[KEY_ONBOARDING_IS_DONE] = isOnboardingDone }

    /** Stores the session token and user email together in one write. */
    suspend fun storeAuth(token: String, email: String) =
        dataStore.edit {
            it[KEY_AUTH_TOKEN] = token
            it[KEY_EMAIL] = email
        }

    /** Removes the session token and user email together (logout). */
    suspend fun deleteAuth() =
        dataStore.edit {
            it.remove(KEY_AUTH_TOKEN)
            it.remove(KEY_EMAIL)
        }

    private companion object {
        val KEY_THEME_IS_DARK = booleanPreferencesKey("theme_is_dark")
        val KEY_ONBOARDING_IS_DONE = booleanPreferencesKey("onboarding_is_done")
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        val KEY_EMAIL = stringPreferencesKey("user_email")

        const val DEFAULT_IS_DARK = true
        const val DEFAULT_IS_ONBOARDING_DONE = false
        const val DEFAULT_AUTH_TOKEN = ""
        const val DEFAULT_EMAIL = ""
    }
}