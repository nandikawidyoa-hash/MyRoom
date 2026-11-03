package com.ourspace.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ourspace.app.ui.theme.AppThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "our_space_preferences")

data class AppUserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val biometricLockEnabled: Boolean = false,
    val privatePinHash: String? = null,
    val autoLockTimeoutMinutes: Int = 5,
    val notifySharedNotes: Boolean = true,
    val notifyLoveNotes: Boolean = true,
    val notifyNewMemories: Boolean = true,
    val notifyAnniversaryReminders: Boolean = true,
    val notifyCalendarEvents: Boolean = true,
    val notifySurprises: Boolean = true,
    val cachedCoupleId: String? = null,
    val cachedPartnerNickname: String = "My Love"
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock_enabled")
        val PRIVATE_PIN_HASH = stringPreferencesKey("private_pin_hash")
        val AUTO_LOCK_TIMEOUT = intPreferencesKey("auto_lock_timeout_minutes")
        val NOTIFY_SHARED_NOTES = booleanPreferencesKey("notify_shared_notes")
        val NOTIFY_LOVE_NOTES = booleanPreferencesKey("notify_love_notes")
        val NOTIFY_NEW_MEMORIES = booleanPreferencesKey("notify_new_memories")
        val NOTIFY_ANNIVERSARY = booleanPreferencesKey("notify_anniversary")
        val NOTIFY_CALENDAR = booleanPreferencesKey("notify_calendar")
        val NOTIFY_SURPRISES = booleanPreferencesKey("notify_surprises")
        val CACHED_COUPLE_ID = stringPreferencesKey("cached_couple_id")
        val CACHED_PARTNER_NICKNAME = stringPreferencesKey("cached_partner_nickname")
    }

    val preferencesFlow: Flow<AppUserPreferences> = context.dataStore.data.map { prefs ->
        val modeName = prefs[Keys.THEME_MODE] ?: AppThemeMode.LIGHT.name
        val themeMode = runCatching { AppThemeMode.valueOf(modeName) }.getOrDefault(AppThemeMode.LIGHT)

        AppUserPreferences(
            themeMode = themeMode,
            biometricLockEnabled = prefs[Keys.BIOMETRIC_LOCK] ?: false,
            privatePinHash = prefs[Keys.PRIVATE_PIN_HASH],
            autoLockTimeoutMinutes = prefs[Keys.AUTO_LOCK_TIMEOUT] ?: 5,
            notifySharedNotes = prefs[Keys.NOTIFY_SHARED_NOTES] ?: true,
            notifyLoveNotes = prefs[Keys.NOTIFY_LOVE_NOTES] ?: true,
            notifyNewMemories = prefs[Keys.NOTIFY_NEW_MEMORIES] ?: true,
            notifyAnniversaryReminders = prefs[Keys.NOTIFY_ANNIVERSARY] ?: true,
            notifyCalendarEvents = prefs[Keys.NOTIFY_CALENDAR] ?: true,
            notifySurprises = prefs[Keys.NOTIFY_SURPRISES] ?: true,
            cachedCoupleId = prefs[Keys.CACHED_COUPLE_ID],
            cachedPartnerNickname = prefs[Keys.CACHED_PARTNER_NICKNAME] ?: "My Love"
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setPrivateSecurity(biometricEnabled: Boolean, pinHash: String?, timeoutMinutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BIOMETRIC_LOCK] = biometricEnabled
            if (pinHash != null) prefs[Keys.PRIVATE_PIN_HASH] = pinHash else prefs.remove(Keys.PRIVATE_PIN_HASH)
            prefs[Keys.AUTO_LOCK_TIMEOUT] = timeoutMinutes
        }
    }

    suspend fun updateCachedCoupleSession(coupleId: String?, partnerNickname: String?) {
        context.dataStore.edit { prefs ->
            if (coupleId != null) prefs[Keys.CACHED_COUPLE_ID] = coupleId else prefs.remove(Keys.CACHED_COUPLE_ID)
            if (partnerNickname != null) prefs[Keys.CACHED_PARTNER_NICKNAME] = partnerNickname
        }
    }
}
