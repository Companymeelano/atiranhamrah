package ir.atiran.hamrah.viewer.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "atiran_db_settings")

/** تنظیمات «Interface Experience» — قابل خاموش‌کردن توسط کاربر */
data class Experience(
    val sound: Boolean = true,
    val motion: Boolean = true,
    val ambient: Boolean = true,
)

/** ذخیره‌سازی تنظیمات اتصال و تجربه (DataStore) */
class SettingsStore(private val context: Context) {

    private object Keys {
        val host = stringPreferencesKey("host")
        val port = stringPreferencesKey("port")
        val database = stringPreferencesKey("database")
        val user = stringPreferencesKey("user")
        val password = stringPreferencesKey("password")
        val remember = booleanPreferencesKey("remember")
        val themeId = intPreferencesKey("themeId")
        val sound = booleanPreferencesKey("sound")
        val motion = booleanPreferencesKey("motion")
        val ambient = booleanPreferencesKey("ambient")
    }

    /** تم انتخابی کاربر (۰ تا ۳) */
    val themeId: Flow<Int> = context.dataStore.data.map { p ->
        (p[Keys.themeId] ?: 0).coerceIn(0, 3)
    }

    val experience: Flow<Experience> = context.dataStore.data.map { p ->
        Experience(
            sound = p[Keys.sound] ?: true,
            motion = p[Keys.motion] ?: true,
            ambient = p[Keys.ambient] ?: true,
        )
    }

    suspend fun setTheme(id: Int) {
        context.dataStore.edit { it[Keys.themeId] = id.coerceIn(0, 3) }
    }

    suspend fun setExperience(e: Experience) {
        context.dataStore.edit {
            it[Keys.sound] = e.sound
            it[Keys.motion] = e.motion
            it[Keys.ambient] = e.ambient
        }
    }

    val settings: Flow<DbSettings> = context.dataStore.data.map { p ->
        DbSettings(
            host = p[Keys.host] ?: DbSettings.DEFAULT_HOST,
            port = p[Keys.port] ?: "1433",
            database = p[Keys.database] ?: DbSettings.DEFAULT_DB,
            user = p[Keys.user] ?: DbSettings.DEFAULT_USER,
            password = p[Keys.password] ?: "",
            remember = p[Keys.remember] ?: true,
        )
    }

    suspend fun save(s: DbSettings) {
        context.dataStore.edit { p ->
            p[Keys.host] = s.host.trim()
            p[Keys.port] = s.port.trim().ifBlank { "1433" }
            p[Keys.database] = s.database.trim()
            p[Keys.user] = s.user.trim()
            p[Keys.password] = s.password
            p[Keys.remember] = s.remember
        }
    }

    /** فقط رمز را پاک می‌کند (خروج)؛ بقیه تنظیمات برای ورود بعدی می‌ماند */
    suspend fun clearPassword() {
        context.dataStore.edit { it.remove(Keys.password) }
    }
}
