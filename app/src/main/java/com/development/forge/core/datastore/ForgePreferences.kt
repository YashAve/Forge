package com.development.forge.core.datastore

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persistent user preferences for Forge. Currently holds only the theme flag.
 *
 * ⚠️ DEVIATION FROM THE BRIEF — read this before "fixing" it:
 * STEP 5 of the goal asked for a Jetpack `DataStore<Preferences>` with a
 * `THEME_IS_DARK` `booleanPreferencesKey`. DataStore is **not** on this module's
 * classpath (neither declared nor transitive) and the active constraints forbid
 * touching Gradle, so this is backed by [SharedPreferences] instead.
 *
 * The public surface is deliberately identical to the DataStore version the brief
 * described — [isDarkTheme] returns a cold `Flow<Boolean>` and [setDarkTheme] is a
 * `suspend` setter — so migrating to real DataStore later is a body-only change
 * with zero call-site impact. To migrate: add
 * `androidx.datastore:datastore-preferences`, swap `prefs` for a
 * `DataStore<Preferences>`, and replace the two method bodies.
 */
@Singleton
class ForgePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Cold stream of the dark-theme flag: emits the stored value immediately, then
     * again on every change. Defaults to dark ([DEFAULT_IS_DARK]) when unset.
     */
    fun isDarkTheme(): Flow<Boolean> = callbackFlow {
        trySend(prefs.getBoolean(KEY_THEME_IS_DARK, DEFAULT_IS_DARK))
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { store, key ->
            // key is null on clear(); re-read in both cases.
            if (key == KEY_THEME_IS_DARK || key == null) {
                trySend(store.getBoolean(KEY_THEME_IS_DARK, DEFAULT_IS_DARK))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.conflate().distinctUntilChanged()

    /** Persists the dark-theme flag off the main thread. */
    suspend fun setDarkTheme(isDark: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_THEME_IS_DARK, isDark).commit()
    }

    private companion object {
        const val PREFS_NAME = "forge_preferences"
        const val KEY_THEME_IS_DARK = "theme_is_dark"
        const val DEFAULT_IS_DARK = true
    }
}
