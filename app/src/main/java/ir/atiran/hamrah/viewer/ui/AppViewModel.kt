package ir.atiran.hamrah.viewer.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.atiran.hamrah.viewer.data.AtiranClient
import ir.atiran.hamrah.viewer.data.AtiranRepository
import ir.atiran.hamrah.viewer.data.AtiranSettings
import ir.atiran.hamrah.viewer.data.SettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** اقلام منوی اصلی (صفحات مشاهده داده) */
enum class Section(val title: String) {
    Customers("مشتریان"),
    Goods("کالاها و قیمت‌ها"),
    Inventory("موجودی انبارها"),
    Invoices("فاکتورها"),
    Checks("چک‌ها"),
    Messages("پیام‌ها"),
    Misc("گزارش‌ها و اطلاعات"),
}

sealed class Screen {
    data object Loading : Screen()
    data object Settings : Screen()
    data object Login : Screen()
    data object Home : Screen()
    data class Browse(val section: Section) : Screen()
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val store = SettingsStore(application)

    val settings: StateFlow<AtiranSettings> = store.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AtiranSettings())

    var screen by mutableStateOf<Screen>(Screen.Loading)
        private set

    var busy by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    /** صفحه‌ای که پس از بستن تنظیمات باید به آن برگردیم */
    private var settingsBackTarget: Screen? = null

    init {
        // فقط یک‌بار مقصد اولیه را تعیین می‌کنیم؛ تغییرات بعدی تنظیمات
        // نباید کاربر را از صفحه فعلی (مثلاً Home) بیرون بیندازد.
        viewModelScope.launch {
            val s = settings.first()
            screen = if (s.configured && s.serverUrl.isNotBlank() && s.cpuId.isNotBlank()) {
                Screen.Login
            } else {
                Screen.Settings
            }
        }
    }

    private var cachedRepo: AtiranRepository? = null
    private var repoCacheKey: String? = null

    /** Stable repository instance while server/CPUID unchanged. */
    fun repository(): AtiranRepository? {
        val s = settings.value
        val key = s.serverUrl.trim().trimEnd('/') + "|" + s.cpuId.trim()
        if (cachedRepo == null || repoCacheKey != key) {
            repoCacheKey = key
            cachedRepo = if (s.serverUrl.isBlank() || s.cpuId.isBlank()) {
                null
            } else {
                AtiranRepository(AtiranClient(s))
            }
        }
        return cachedRepo
    }

    fun saveSettings(s: AtiranSettings, afterSave: (() -> Unit)? = null) {
        viewModelScope.launch {
            busy = true
            error = null
            try {
                store.save(s)
                if (afterSave != null) {
                    afterSave()
                } else {
                    screen = settingsBackTarget ?: Screen.Login
                }
            } catch (e: Exception) {
                error = e.message ?: "خطا در ذخیره تنظیمات"
            } finally {
                busy = false
            }
        }
    }

    fun goSettings() {
        settingsBackTarget = when (screen) {
            is Screen.Home, is Screen.Browse -> Screen.Home
            is Screen.Login -> Screen.Login
            else -> null
        }
        screen = Screen.Settings
    }

    /** بازگشت از صفحه تنظیمات به صفحه قبلی */
    fun backFromSettings() {
        screen = settingsBackTarget ?: Screen.Login
        settingsBackTarget = null
    }

    fun goLogin() {
        screen = Screen.Login
    }

    fun goHome() {
        screen = Screen.Home
    }

    fun openSection(section: Section) {
        screen = Screen.Browse(section)
    }

    fun backToHome() {
        screen = Screen.Home
    }

    fun logout() {
        viewModelScope.launch {
            store.clear()
            error = null
            settingsBackTarget = null
            screen = Screen.Settings
        }
    }
}
