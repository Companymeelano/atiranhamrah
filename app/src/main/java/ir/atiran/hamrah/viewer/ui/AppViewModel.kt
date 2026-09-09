package ir.atiran.hamrah.viewer.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.atiran.hamrah.viewer.data.AiSettings
import ir.atiran.hamrah.viewer.data.DbSettings
import ir.atiran.hamrah.viewer.data.Experience
import ir.atiran.hamrah.viewer.data.Overview
import ir.atiran.hamrah.viewer.data.RealRow
import ir.atiran.hamrah.viewer.data.RealTable
import ir.atiran.hamrah.viewer.data.SectionMap
import ir.atiran.hamrah.viewer.data.SettingsStore
import ir.atiran.hamrah.viewer.data.SqlServerDb
import ir.atiran.hamrah.viewer.data.TableInfo
import ir.atiran.hamrah.viewer.data.parseSectionMap
import ir.atiran.hamrah.viewer.data.realLatest
import ir.atiran.hamrah.viewer.data.realTable
import ir.atiran.hamrah.viewer.data.realTop
import ir.atiran.hamrah.viewer.data.serializeSectionMap
import ir.atiran.hamrah.viewer.data.splitRef
import ir.atiran.hamrah.viewer.ui.components.MotionFx
import ir.atiran.hamrah.viewer.utils.SoundFx
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/** یادآور شخصی کاربر — هشدار بر پایه تقویم شمسی با آلارم قابل تنظیم */
data class Reminder(
    val id: String,
    val cat: String,
    val text: String,
    val jy: Int,
    val jm: Int,
    val jd: Int,
    /** ساعت آلارم */
    val hh: Int = 9,
    val mm: Int = 0,
    /** چند وقت قبل از موعد، آلارم به صدا دربیاید */
    val offsetValue: Int = 0,
    val offsetUnit: String = "دقیقه",
    /** نوع صدای آلارم: ملایم | معمولی | فوری */
    val sound: String = "معمولی",
    /** یادداشت انجام (وقتی کاربر تکمیلش کند) */
    val note: String? = null,
)

/** سریال‌سازی یادآورها به JSON — امن برای هر متنی */
private fun serializeReminders(list: List<Reminder>): String {
    val arr = JSONArray()
    list.forEach { r ->
        arr.put(
            JSONObject().apply {
                put("id", r.id)
                put("cat", r.cat)
                put("text", r.text)
                put("y", r.jy)
                put("m", r.jm)
                put("d", r.jd)
                put("hh", r.hh)
                put("mm", r.mm)
                put("ov", r.offsetValue)
                put("ou", r.offsetUnit)
                put("snd", r.sound)
                if (r.note != null) put("note", r.note)
            }
        )
    }
    return arr.toString()
}

private fun parseReminders(raw: String): List<Reminder> = try {
    val arr = JSONArray(raw)
    (0 until arr.length()).map { i ->
        val o = arr.getJSONObject(i)
        Reminder(
            id = o.optString("id", ""),
            cat = o.optString("cat", "سایر"),
            text = o.optString("text", ""),
            jy = o.optInt("y", 1404),
            jm = o.optInt("m", 1).coerceIn(1, 12),
            jd = o.optInt("d", 1).coerceIn(1, 31),
            hh = o.optInt("hh", 9).coerceIn(0, 23),
            mm = o.optInt("mm", 0).coerceIn(0, 59),
            offsetValue = o.optInt("ov", 0).coerceAtLeast(0),
            offsetUnit = o.optString("ou", "دقیقه"),
            sound = o.optString("snd", "معمولی"),
            note = if (o.has("note")) o.optString("note") else null,
        )
    }.filter { it.id.isNotBlank() && it.text.isNotBlank() }
} catch (_: Throwable) {
    emptyList()
}

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

    /** ترتیب کارت‌های داشبورد — شخصی‌سازی کاربر */
    val heroOrder = store.heroOrder
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** فهرست جداول سرور (بعد از ورود) — برای نگاشت بخش‌ها */
    var dbOverview by mutableStateOf<Overview?>(null)
        private set

    /** نگاشت بخش‌های M•REPORT به جداول واقعی سرور */
    val sectionMap = store.sectionMap
        .map { parseSectionMap(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SectionMap())

    fun updateSection(section: String, ref: String?) {
        viewModelScope.launch {
            store.setSectionMap(serializeSectionMap(sectionMap.value.withSection(section, ref)))
        }
    }

    fun setSectionMap(m: SectionMap) {
        viewModelScope.launch { store.setSectionMap(serializeSectionMap(m)) }
    }

    fun loadOverview() {
        val db = db ?: return
        viewModelScope.launch {
            dbOverview = try {
                db.overview()
            } catch (_: Throwable) {
                null
            }
            // اتصال خودکار بخش‌های وصل‌نشده — با نام‌های واقعی تست‌شدهٔ Atiran
            // (CUSTOMERS / inventory / sailfact / چک‌ها) از نسخهٔ ویندوز M•R
            val ov = dbOverview ?: return@launch
            val current = sectionMap.value
            val detected = ir.atiran.hamrah.viewer.data.TableHeuristics.detectMap(ov.tables)
            val merged = current.copy(
                customers = current.customers ?: detected.customers,
                products = current.products ?: detected.products,
                invoices = current.invoices ?: detected.invoices,
                checks = current.checks ?: detected.checks,
                banks = current.banks ?: detected.banks,
            )
            if (merged != current) setSectionMap(merged)
        }
    }

    // -------- خواندن داده واقعی برای بخش‌های M•REPORT --------

    suspend fun realTableFor(ref: String, sample: Int = 200): RealTable? = db?.realTable(ref, sample)

    suspend fun realCountFor(ref: String): Long? = try {
        val (s, t) = splitRef(ref)
        db?.count(s, t)
    } catch (_: Throwable) {
        null
    }

    suspend fun realSumFor(ref: String, col: String?): Double? {
        if (col == null) return null
        return try {
            val (s, t) = splitRef(ref)
            db?.sumOf(s, t, col)
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun realTopFor(ref: String, col: String?, limit: Int = 5): List<RealRow> {
        if (col == null) return emptyList()
        return try {
            db?.realTop(ref, col, limit) ?: emptyList()
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun realLatestFor(ref: String, col: String?, limit: Int = 8): List<RealRow> {
        if (col == null) return emptyList()
        return try {
            db?.realLatest(ref, col, limit) ?: emptyList()
        } catch (_: Throwable) {
            emptyList()
        }
    }

    /** تنظیمات دستیار هوشمند «پسته» */
    var ai by mutableStateOf(AiSettings())
        private set

    /** زمان آخرین دریافت اطلاعات (Auto Refresh) */
    var lastSyncMs by mutableLongStateOf(System.currentTimeMillis())
        private set

    fun touchSync() {
        lastSyncMs = System.currentTimeMillis()
    }

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
        viewModelScope.launch {
            store.aiSettings.collect { ai = it }
        }

        // اتصال خودکار اگر رمز ذخیره‌شده داریم؛ در غیر این صورت صفحه ورود
        viewModelScope.launch {
            val s = settings.first()
            if (s.password.isNotBlank() && s.host.isNotBlank()) {
                val candidate = SqlServerDb(s)
                try {
                    candidate.test()
                    db = candidate
                    screen = Screen.Demo
                    loadOverview()
                } catch (_: Throwable) {
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

    /** ذخیره چیدمان شخصی داشبورد */
    fun setHeroOrder(order: List<Int>) {
        viewModelScope.launch { store.setHeroOrder(order.joinToString(",")) }
    }

    /** چارت‌های منتخب کاربر در نمای کلی — استودیو چارت */
    val myCharts = store.myCharts
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setMyCharts(ids: List<String>) {
        viewModelScope.launch { store.setMyCharts(ids.joinToString(",")) }
    }

    /** هشدارهای دیده‌شده — شمارنده نشان مرکز توجه */
    val alertSeen = store.alertSeen
        .map { raw -> raw.split(",").filter { it.isNotBlank() }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    fun markAlertsSeen(ids: Collection<String>) {
        val next = alertSeen.value + ids
        viewModelScope.launch { store.setAlertSeen(next.joinToString(",")) }
    }

    /** اعلان‌های انجام‌شده (خط‌خورده و منتقل به پایین) */
    val notifDone = store.notifDone
        .map { raw -> raw.split(",").filter { it.isNotBlank() }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    fun toggleNotifDone(id: String) {
        val cur = notifDone.value
        val next = if (id in cur) cur - id else cur + id
        viewModelScope.launch { store.setNotifDone(next.joinToString(",")) }
    }

    /** یادآورهای شخصی کاربر — بر پایه تقویم شمسی */
    val reminders = store.reminders
        .map { raw -> parseReminders(raw) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addReminder(
        cat: String, text: String, jy: Int, jm: Int, jd: Int,
        hh: Int, mm: Int, offsetValue: Int, offsetUnit: String, sound: String,
    ) {
        val r = Reminder(
            id = "r" + System.currentTimeMillis(), cat = cat, text = text.trim(),
            jy = jy, jm = jm, jd = jd, hh = hh, mm = mm,
            offsetValue = offsetValue, offsetUnit = offsetUnit, sound = sound,
        )
        val next = reminders.value + r
        viewModelScope.launch { store.setReminders(serializeReminders(next)) }
        scheduleReminderAlarm(r)
    }

    /** تکمیل یادآور با یادداشت اختیاری — خط قرمز + انتقال به پایین */
    fun completeReminder(id: String, note: String?) {
        val next = reminders.value.map {
            if (it.id == id) it.copy(note = note?.trim()?.takeIf { n -> n.isNotBlank() }) else it
        }
        viewModelScope.launch { store.setReminders(serializeReminders(next)) }
        if (id !in notifDone.value) toggleNotifDone(id)
    }

    /** زمان‌بندی آلارم واقعی یادآور (AlarmManager + اعلان + صدا) */
    private fun scheduleReminderAlarm(r: Reminder) {
        try {
            val ctx = getApplication<android.app.Application>()
            val (gy, gm, gd) = ir.atiran.hamrah.viewer.utils.Jalali.toGregorian(r.jy, r.jm, r.jd)
            val cal = java.util.Calendar.getInstance().apply {
                set(gy, gm - 1, gd, r.hh, r.mm, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            var trigger = cal.timeInMillis - when (r.offsetUnit) {
                "دقیقه" -> r.offsetValue * 60_000L
                "ساعت" -> r.offsetValue * 3_600_000L
                "روز" -> r.offsetValue * 86_400_000L
                else -> 0L
            }
            if (trigger <= System.currentTimeMillis()) {
                trigger = System.currentTimeMillis() + 60_000L
            }
            val am = ctx.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = android.content.Intent(ctx, ir.atiran.hamrah.viewer.utils.ReminderReceiver::class.java)
                .putExtra("text", r.text)
                .putExtra("cat", r.cat)
                .putExtra("sound", r.sound)
                .putExtra("rid", r.id.hashCode())
            val pi = android.app.PendingIntent.getBroadcast(
                ctx, r.id.hashCode(), intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
            )
            if (android.os.Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                am.setWindow(android.app.AlarmManager.RTC_WAKEUP, trigger, 10 * 60_000L, pi)
            } else {
                am.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, trigger, pi)
            }
        } catch (_: Throwable) {
            // زمان‌بندی نشد ولی یادآور خودش در فهرست هست
        }
    }

    /**
     * ورود / تغییر اتصال — اعتبارنامه‌ها با بازکردن اتصال واقعی بررسی می‌شوند.
     */
    suspend fun login(cfg: DbSettings) {
        val candidate = SqlServerDb(cfg)
        candidate.test()
        store.save(cfg.copy(password = if (cfg.remember) cfg.password else ""))
        db = candidate
        screen = Screen.Demo
        loadOverview()
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
        // پسته ورود کاربر را می‌شمارد تا دفعه بعد شخصی سلام کند
        updateAi { it.copy(visits = it.visits + 1) }
    }

    /** تغییر تنظیمات دستیار هوشمند */
    fun updateAi(transform: (AiSettings) -> AiSettings) {
        val next = transform(ai)
        ai = next
        viewModelScope.launch { store.setAiSettings(next) }
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
