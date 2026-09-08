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

/** تنظیمات دستیار هوشمند «پسته» */
data class AiSettings(
    val enabled: Boolean = true,
    /** اسم کاربر — پسته برای همیشه یادش می‌سپارد */
    val userName: String = "",
    /** لحن گفتار: شوخ | رسمی | خلاصه */
    val mode: String = "شوخ",
    /** بخش‌های قابل تحلیل توسط دستیار */
    val domains: Set<String> = setOf("کالاها", "مشتریان", "مطالبات", "چک‌ها", "گزارش‌ها"),
    /** تعداد دفعات ورود — برای سلام شخصی‌سازی‌شده */
    val visits: Int = 0,
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
        val wowSweepDone = booleanPreferencesKey("wowSweepDone")
        val heroOrder = stringPreferencesKey("heroOrder")
        val aiEnabled = booleanPreferencesKey("aiEnabled")
        val aiName = stringPreferencesKey("aiName")
        val aiMode = stringPreferencesKey("aiMode")
        val aiDomains = stringPreferencesKey("aiDomains")
        val aiVisits = intPreferencesKey("aiVisits")
    }

    /** تم انتخابی کاربر (۰ تا ۳) */
    val themeId: Flow<Int> = context.dataStore.data.map { p ->
        (p[Keys.themeId] ?: 0).coerceIn(0, 4)
    }

    val experience: Flow<Experience> = context.dataStore.data.map { p ->
        Experience(
            sound = p[Keys.sound] ?: true,
            motion = p[Keys.motion] ?: true,
            ambient = p[Keys.ambient] ?: true,
        )
    }

    suspend fun setTheme(id: Int) {
        context.dataStore.edit { it[Keys.themeId] = id.coerceIn(0, 4) }
    }

    suspend fun setExperience(e: Experience) {
        context.dataStore.edit {
            it[Keys.sound] = e.sound
            it[Keys.motion] = e.motion
            it[Keys.ambient] = e.ambient
        }
    }

    /** آیا جاروی نورِ ورود اول قبلاً اجرا شده؟ (فقط یک‌بار در عمر برنامه) */
    val wowSweepDone: Flow<Boolean> = context.dataStore.data.map { p ->
        p[Keys.wowSweepDone] ?: false
    }

    suspend fun setWowSweepDone() {
        context.dataStore.edit { it[Keys.wowSweepDone] = true }
    }

    /** ترتیب کارت‌های داشبورد (شخصی‌سازی — مثل "2,0,3,1") */
    val heroOrder: Flow<String?> = context.dataStore.data.map { p -> p[Keys.heroOrder] }

    suspend fun setHeroOrder(order: String) {
        context.dataStore.edit { it[Keys.heroOrder] = order }
    }

    /** تنظیمات دستیار هوشمند پسته */
    val aiSettings: Flow<AiSettings> = context.dataStore.data.map { p ->
        AiSettings(
            enabled = p[Keys.aiEnabled] ?: true,
            userName = p[Keys.aiName] ?: "",
            mode = (p[Keys.aiMode] ?: "شوخ"),
            domains = (p[Keys.aiDomains] ?: "کالاها,مشتریان,مطالبات,چک‌ها,گزارش‌ها").split(",").filter { it.isNotBlank() }.toSet(),
            visits = p[Keys.aiVisits] ?: 0,
        )
    }

    suspend fun setAiSettings(s: AiSettings) {
        context.dataStore.edit {
            it[Keys.aiEnabled] = s.enabled
            it[Keys.aiName] = s.userName
            it[Keys.aiMode] = s.mode
            it[Keys.aiDomains] = s.domains.joinToString(",")
            it[Keys.aiVisits] = s.visits
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
