package ir.atiran.hamrah.viewer.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.atiran.hamrah.viewer.data.DbSettings
import ir.atiran.hamrah.viewer.data.Experience
import ir.atiran.hamrah.viewer.data.SettingsStore
import ir.atiran.hamrah.viewer.data.SqlServerDb
import ir.atiran.hamrah.viewer.data.TableInfo
import ir.atiran.hamrah.viewer.ui.components.MotionFx
import ir.atiran.hamrah.viewer.utils.SoundFx
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

    /** حالت نمایشی (دمو) — بدون نیاز به اتصال دیتابیس */
    data object Demo : Screen()
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

    /** تم فعال برنامه */
    var themeId by mutableIntStateOf(0)
        private set

    /** تنظیمات Interface Experience */
    var experience by mutableStateOf(Experience())
        private set

    /** جاروی نور ورود اول — null یعنی هنوز از DataStore نخوانده */
    val wowSweepDone = store.wowSweepDone
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        SoundFx.init(application)

        viewModelScope.launch {
            store.themeId.collect { themeId = it }
        }
        viewModelScope.launch {
            store.experience.collect {
                experience = it
                SoundFx.enabled = it.sound
                MotionFx.enabled = it.motion
            }
        }

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

    /** تغییر تم — بلافاصله اعمال و ذخیره می‌شود */
    fun setTheme(id: Int) {
        themeId = id
        viewModelScope.launch { store.setTheme(id) }
    }

    /** تغییر تنظیمات تجربه */
    fun updateExperience(e: Experience) {
        experience = e
        SoundFx.enabled = e.sound
        MotionFx.enabled = e.motion
        viewModelScope.launch { store.setExperience(e) }
    }

    /** جاروی نور اجرا شد — دیگر هرگز تکرار نشود */
    fun markWowDone() {
        viewModelScope.launch { store.setWowSweepDone() }
    }

    /**
     * ورود / تغییر اتصال — اعتبارنامه‌ها با بازکردن اتصال واقعی بررسی می‌شوند.
     */
    suspend fun login(cfg: DbSettings) {
        val candidate = SqlServerDb(cfg)
        candidate.test()
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

    fun goLogin() {
        screen = Screen.Login
    }

    fun openDemo() {
        screen = Screen.Demo
    }

    fun closeDemo() {
        screen = if (db != null) Screen.Tables else Screen.Login
    }

    fun logout() {
        viewModelScope.launch {
            store.clearPassword()
            db = null
            screen = Screen.Login
        }
    }
}
