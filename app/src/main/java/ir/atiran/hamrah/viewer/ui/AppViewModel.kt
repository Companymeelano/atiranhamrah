package ir.atiran.hamrah.viewer.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.atiran.hamrah.viewer.data.DbSettings
import ir.atiran.hamrah.viewer.data.SettingsStore
import ir.atiran.hamrah.viewer.data.SqlServerDb
import ir.atiran.hamrah.viewer.data.TableInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    /** در حال اتصال خودکار اولیه */
    data object Boot : Screen()
    data object Login : Screen()
    data object Tables : Screen()
    data class TableData(val schema: String, val table: String) : Screen()
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val store = SettingsStore(application)

    val settings = store.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, DbSettings())

    var screen by mutableStateOf<Screen>(Screen.Boot)
        private set

    /** اتصال فعال (بعد از ورود موفق) */
    var db by mutableStateOf<SqlServerDb?>(null)
        private set

    init {
        // اتصال خودکار اگر رمز ذخیره‌شده داریم؛ در غیر این صورت صفحه ورود
        viewModelScope.launch {
            val s = settings.first()
            if (s.password.isNotBlank() && s.host.isNotBlank()) {
                val candidate = SqlServerDb(s)
                try {
                    candidate.test()
                    db = candidate
                    screen = Screen.Tables
                } catch (_: Exception) {
                    screen = Screen.Login
                }
            } else {
                screen = Screen.Login
            }
        }
    }

    /**
     * ورود / تغییر اتصال — اعتبارنامه‌ها با بازکردن اتصال واقعی بررسی می‌شوند.
     * رمز فقط در صورت فعال‌بودن «مرا به خاطر بسپار» ذخیره می‌شود.
     */
    suspend fun login(cfg: DbSettings) {
        val candidate = SqlServerDb(cfg)
        candidate.test() // در صورت خطا exception پرتاب می‌شود
        store.save(cfg.copy(password = if (cfg.remember) cfg.password else ""))
        db = candidate
        screen = Screen.Tables
    }

    fun openTable(t: TableInfo) {
        screen = Screen.TableData(t.schema, t.name)
    }

    fun backToTables() {
        screen = Screen.Tables
    }

    /** رفتن به صفحه ورود برای تغییر تنظیمات اتصال */
    fun goLogin() {
        screen = Screen.Login
    }

    /** خروج — رمز ذخیره‌شده پاک می‌شود */
    fun logout() {
        viewModelScope.launch {
            store.clearPassword()
            db = null
            screen = Screen.Login
        }
    }
}
