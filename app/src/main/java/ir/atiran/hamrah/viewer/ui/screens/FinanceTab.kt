package ir.atiran.hamrah.viewer.ui.screens

/**
 * خزانه M•REPORT — مرکز مالی و بانکی حرفه‌ای
 *
 * سه بخش تخصصی برای حسابدار مجموعه:
 *  - دریافت‌ها: پوزهای کاربران فروش، نقد، چک‌های دریافتنی — با تفکیک
 *    و سرجمع هر دسته و سرجمع کل + فیلترها (تاریخ، نوع، گروه مشتری،
 *    ویزیتور، پوز/بانک، کاربر سیستم)
 *  - پرداخت‌ها: نقد پرداختی، سرفصل‌های هزینه، چک‌های پرداختی صندوق
 *  - گزارش روزانه: تصویر کامل یک روز با فیلترهای هوشمند
 * همه‌چیز هماهنگ با تم انتخابی کاربر — گوی‌های سه‌بعدی، کارت شیشه‌ای
 * و اعداد قهرمان با رنگ تم. اگر جداول سرور وصل باشند، چک‌های واقعی
 * هم در فهرست می‌آیند.
 */
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ir.atiran.hamrah.viewer.data.RealTable
import ir.atiran.hamrah.viewer.data.TableHeuristics
import ir.atiran.hamrah.viewer.data.TableRoles
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.CalmPulse
import ir.atiran.hamrah.viewer.ui.components.EmptyBox
import ir.atiran.hamrah.viewer.ui.components.GlassCard
import ir.atiran.hamrah.viewer.ui.components.MrIcons
import ir.atiran.hamrah.viewer.ui.components.NumberHero
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import ir.atiran.hamrah.viewer.utils.SoundFx

// ============================================================ داده

/** یک سند مالی — دریافت یا پرداخت */
private data class FinDoc(
    val id: String,
    val kind: String,      // نقد | پوز | چک | هزینه
    val title: String,
    val party: String,
    val amount: Long,      // تومان
    val group: String = "همه",     // گروه مشتری
    val visitor: String = "همه",   // ویزیتور
    val device: String = "همه",    // پوز / بانک / صندوق
    val user: String = "همه",      // کاربر سیستم
    val range: String = "این ماه", // امروز | این هفته | این ماه
    val header: String = "",       // سرفصل هزینه
    val isServer: Boolean = false, // از جدول واقعی سرور
)

private val posUsers = listOf("علی", "رضا", "مهدی")

private val receiptsDemo = listOf(
    // پوزهای کشیده‌شده توسط کاربران فروش
    FinDoc("r1", "پوز", "پوز فروش — هایپر طلایی", "ترمینال سامان · کارت به کارت", 8_500_000, "عمده", "علی", "پوز سامان", "علی", "امروز"),
    FinDoc("r2", "پوز", "پوز فروش — فروشگاه زرین", "ترمینال سامان · کشیده توسط رضا", 4_200_000, "نیمه‌عمده", "رضا", "پوز سامان", "رضا", "امروز"),
    FinDoc("r3", "پوز", "پوز فروش — پخش نگین", "ترمینال ملت · کاربر مهدی", 6_800_000, "عمده", "مهدی", "پوز ملت", "مهدی", "این هفته"),
    FinDoc("r4", "پوز", "پوز فروش — مارکت الماس", "ترمینال ملت", 3_100_000, "صنفی", "علی", "پوز ملت", "علی", "این هفته"),
    FinDoc("r5", "پوز", "پوز فروش — سوپرمارکت بهار", "ترمینال سامان", 1_950_000, "صنفی", "رضا", "پوز سامان", "رضا", "این ماه"),
    FinDoc("r6", "پوز", "پوز فروش — فروشگاه نگین", "ترمینال سامان", 2_400_000, "نیمه‌عمده", "مهدی", "پوز سامان", "مهدی", "این ماه"),
    // نقد گرفته‌شده
    FinDoc("r7", "نقد", "دریافت نقدی — هایپر طلایی", "وصول بخشی از مطالبات", 15_000_000, "عمده", "علی", "صندوق", "مدیر", "امروز"),
    FinDoc("r8", "نقد", "دریافت نقدی — فروشگاه زرین", "تسویه فاکتور ۱۰۳۲۵", 7_300_000, "نیمه‌عمده", "رضا", "صندوق", "مدیر", "این هفته"),
    FinDoc("r9", "نقد", "دریافت نقدی — پخش نگین", "پیش‌پرداخت سفارش جدید", 9_000_000, "عمده", "مهدی", "صندوق", "مهدی", "این هفته"),
    FinDoc("r10", "نقد", "دریافت نقدی — مارکت الماس", "سفارش اول", 4_500_000, "صنفی", "علی", "صندوق", "علی", "این ماه"),
    // چک‌های گرفته‌شده
    FinDoc("r11", "چک", "چک ۷۸۴۵۱۲ — فروشگاه زرین", "دریافتنی · سررسید فردا", 35_000_000, "نیمه‌عمده", "رضا", "بانک ملت", "مدیر", "این هفته"),
    FinDoc("r12", "چک", "چک ۷۸۴۴۹۸ — هایپر طلایی", "وصول شد", 22_000_000, "عمده", "علی", "بانک ملت", "مدیر", "این ماه"),
    FinDoc("r13", "چک", "چک ۷۸۴۴۷۶ — پخش نگین", "پاس شده · نیازمند پیگیری", 18_400_000, "عمده", "مهدی", "بانک پاسارگاد", "مدیر", "این ماه"),
    FinDoc("r14", "چک", "چک ۷۸۴۴۳۲ — مارکت الماس", "دریافتنی · سررسید هفته بعد", 12_000_000, "صنفی", "علی", "بانک سامان", "مدیر", "این ماه"),
)

private val paymentsDemo = listOf(
    // نقد پرداختی
    FinDoc("p1", "نقد", "پرداخت نقدی — خرید پسته", "باغدار یزد · تسویه محموله", 64_000_000, "همه", "همه", "صندوق", "مدیر", "این هفته", "خرید کالا"),
    FinDoc("p2", "نقد", "پرداخت نقدی — اجاره دفتر", "ماهانه شهریور", 28_000_000, "همه", "همه", "صندوق", "مدیر", "این ماه", "اجاره"),
    FinDoc("p3", "نقد", "پرداخت نقدی — کرایه سردخانه", "ماهانه", 12_500_000, "همه", "همه", "صندوق", "مدیر", "این ماه", "اجاره"),
    // سرفصل‌های هزینه ثبت‌شده
    FinDoc("p4", "هزینه", "هزینه حمل‌ونقل", "کامیون اهواز → تهران", 8_400_000, "همه", "همه", "همه", "علی", "این هفته", "حمل‌ونقل"),
    FinDoc("p5", "هزینه", "هزینه حقوق پرسنل", "۳ نفر · شهریور", 42_000_000, "همه", "همه", "همه", "مدیر", "این ماه", "حقوق"),
    FinDoc("p6", "هزینه", "هزینه تبلیغات", "بنر و پیامک منطقه‌ای", 6_200_000, "همه", "همه", "همه", "رضا", "این ماه", "تبلیغات"),
    FinDoc("p7", "هزینه", "هزینه سوخت", "ویزیت‌های هفته", 3_800_000, "همه", "همه", "همه", "علی", "این هفته", "سوخت"),
    FinDoc("p8", "هزینه", "هزینه بسته‌بندی", "کیسه و کارتن", 5_600_000, "همه", "همه", "همه", "مهدی", "این ماه", "بسته‌بندی"),
    // چک‌های پرداختی توسط صندوق
    FinDoc("p9", "چک", "چک پرداختی ۵۵۲۲۱۰ — باغدار یزد", "خرید پسته · صادر از بانک ملت", 40_000_000, "همه", "همه", "بانک ملت", "مدیر", "این هفته"),
    FinDoc("p10", "چک", "چک پرداختی ۵۵۲۱۹۸ — کارتن‌سازی", "بسته‌بندی · بانک سامان", 15_000_000, "همه", "همه", "بانک سامان", "مدیر", "این ماه"),
)

/** گزارش روزانه — به تفکیک روز */
private data class DayReport(
    val day: String,
    val invoices: Int,
    val sales: Long,
    val cashIn: Long,
    val posIn: Long,
    val checkIn: Long,
    val cashOut: Long,
    val expenseOut: Long,
    val checkOut: Long,
)

private val dailyDemo = listOf(
    DayReport("امروز", 3, 18_500_000, 15_000_000, 12_700_000, 0, 28_000_000, 6_200_000, 0),
    DayReport("دیروز", 4, 22_000_000, 9_400_000, 9_800_000, 22_000_000, 64_000_000, 8_400_000, 40_000_000),
    DayReport("۲ روز قبل", 3, 19_800_000, 7_200_000, 7_600_000, 0, 12_500_000, 3_800_000, 0),
)

/** آیکون هر نوع سند — ست اختصاصی M•REPORT */
private fun kindIcon(kind: String): ImageVector = when (kind) {
    "پوز" -> MrIcons.Bank
    "چک" -> MrIcons.Checks
    "هزینه" -> MrIcons.Excel
    else -> MrIcons.Wallet
}

/** رنگ هر نوع سند — هماهنگ با تم و خوانا */
private fun kindColor(kind: String, positive: Color): Color = when (kind) {
    "پوز" -> Color(0xFF74C0FC)
    "چک" -> Color(0xFFFFA94D)
    "هزینه" -> Color(0xFFE599F7)
    else -> positive
}

private fun faMoney(v: Long): String = TableHeuristics.faMoney(v.toDouble())

// ============================================================ فیلترها

private data class FinFilter(
    val range: String = "همه",
    val kind: String = "همه",
    val group: String = "همه",
    val visitor: String = "همه",
    val device: String = "همه",
    val user: String = "همه",
    val header: String = "همه",
) {
    val activeCount: Int
        get() = listOf(range, kind, group, visitor, device, user, header).count { it != "همه" }

    fun matches(d: FinDoc): Boolean =
        (range == "همه" || d.range == range) &&
            (kind == "همه" || d.kind == kind) &&
            (group == "همه" || d.group == group) &&
            (visitor == "همه" || d.visitor == visitor) &&
            (device == "همه" || d.device == device) &&
            (user == "همه" || d.user == user) &&
            (header == "همه" || d.header == header)
}

// ============================================================ تب خزانه

@Composable
fun FinanceTab(vm: AppViewModel, onOpenMapping: () -> Unit = {}) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val map by vm.sectionMap.collectAsState()
    val connected = vm.db != null
    var subTab by remember { mutableStateOf("دریافت‌ها") }
    var q by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(FinFilter()) }
    var filterOpen by remember { mutableStateOf(false) }

    // چک‌های واقعی سرور — اگر جدول چک‌ها وصل باشد
    var serverChecks by remember { mutableStateOf<List<FinDoc>>(emptyList()) }
    val checksRef = map.tableFor("checks")
    LaunchedEffect(checksRef) {
        serverChecks = if (checksRef != null) {
            try {
                val t: RealTable? = vm.realTableFor(checksRef, 60)
                t?.let { tbl ->
                    val r: TableRoles = TableHeuristics.detectRoles(tbl.cols)
                    tbl.rows.take(40).map { row ->
                        FinDoc(
                            id = "srv_" + (row.value(r.codeCol) ?: row.value(r.titleCol) ?: ""),
                            kind = "چک",
                            title = row.value(r.titleCol) ?: "چک از جدول سرور",
                            party = checksRef + (row.value(r.dateCol)?.let { " · " + it } ?: ""),
                            amount = TableHeuristics.parseNum(row.value(r.amountCol))?.toLong() ?: 0L,
                            isServer = true,
                        )
                    }
                } ?: emptyList()
            } catch (_: Throwable) {
                emptyList()
            }
        } else emptyList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // سربرگ: نشان سه‌بعدی خزانه
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(
                        Brush.radialGradient(listOf(scheme.primary.copy(alpha = 0.30f), Color.Transparent)),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(27.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(scheme.primary.copy(alpha = 0.32f), scheme.primary.copy(alpha = 0.10f))
                            )
                        )
                        .border(1.dp, scheme.primary.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(MrIcons.Bank, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("خزانه", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text(
                    if (connected) "گزارش‌های مالی برای حسابدار مجموعه" + (if (serverChecks.isNotEmpty()) " — چک‌های سرور متصل" else "")
                    else "بانک‌ها، چک‌ها، دریافتی‌ها و پرداختی‌ها — برای حسابدار مجموعه",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            // دکمه فیلترها با نشان تعداد فعال
            Box {
                MrOrbFilter(active = filter.activeCount) { filterOpen = true; SoundFx.soft() }
            }
        }

        // زیر‌تب‌ها: دریافت‌ها | پرداخت‌ها | روزانه
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("دریافت‌ها", "پرداخت‌ها", "روزانه").forEach { t ->
                val sel = subTab == t
                Text(
                    t,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (sel) extras.goldOn else scheme.onSurfaceVariant,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (sel) Brush.horizontalGradient(extras.goldGradient)
                            else Brush.verticalGradient(
                                listOf(scheme.surfaceVariant.copy(alpha = 0.5f), scheme.surfaceVariant.copy(alpha = 0.5f))
                            )
                        )
                        .border(1.dp, if (sel) Color.Transparent else extras.hairline, RoundedCornerShape(50))
                        .clickable { subTab = t; SoundFx.soft() }
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }

        when (subTab) {
            "دریافت‌ها" -> ReceiptsSection(q, onQ = { q = it }, filter, serverChecks, connected)
            "پرداخت‌ها" -> PaymentsSection(q, onQ = { q = it }, filter)
            else -> DailySection(filter)
        }
    }

    if (filterOpen) {
        FilterSheet(filter) { filter = it; filterOpen = false }
    }
}

// ============================================================ دریافت‌ها

@Composable
private fun ReceiptsSection(
    q: String,
    onQ: (String) -> Unit,
    filter: FinFilter,
    serverChecks: List<FinDoc>,
    connected: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current

    OutlinedTextField(
        value = q,
        onValueChange = onQ,
        placeholder = { Text("جستجوی سند، مشتری یا پوز...") },
        singleLine = true,
        leadingIcon = { Icon(MrIcons.Search, contentDescription = null, tint = scheme.primary) },
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    )

    val all = receiptsDemo + serverChecks
    val list = all.filter { filter.matches(it) && (q.isBlank() || it.title.contains(q, true) || it.party.contains(q, true)) }

    // سرجمع کل + سرجمع هر دسته
    val total = list.sumOf { it.amount }
    val posSum = list.filter { it.kind == "پوز" }.sumOf { it.amount }
    val cashSum = list.filter { it.kind == "نقد" }.sumOf { it.amount }
    val checkSum = list.filter { it.kind == "چک" }.sumOf { it.amount }

    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "سرجمع دریافتی‌ها",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                    modifier = Modifier.weight(1f),
                )
                CalmPulse(extras.positive, dotSize = 7.dp)
            }
            NumberHero(faMoney(total), unit = "تومان", color = scheme.primary, autoFit = true, fixedHeight = 36.dp)
            // تفکیک سرجمع‌ها
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                SubtotalChip("نقد", faMoney(cashSum), extras.positive, Modifier.weight(1f))
                SubtotalChip("پوز", faMoney(posSum), Color(0xFF74C0FC), Modifier.weight(1f))
                SubtotalChip("چک", faMoney(checkSum), Color(0xFFFFA94D), Modifier.weight(1f))
            }
        }
    }

    if (!connected) {
        Text(
            "★ داده‌های نمایشی — با اتصال جداول سرور، چک‌های واقعی هم اضافه می‌شوند",
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
    }

    // گروه‌بندی بر اساس نوع + سرجمع هر گروه
    listOf("پوز" to "پوزهای کشیده‌شده کاربران فروش", "نقد" to "پول‌های نقد گرفته‌شده", "چک" to "چک‌های گرفته‌شده").forEach { (kind, label) ->
        val group = list.filter { it.kind == kind }
        if (group.isNotEmpty()) {
            SectionHeader(label, group.sumOf { it.amount }, kindIcon(kind), kindColor(kind, extras.positive))
            group.forEach { d -> FinCard(d, extras) }
        }
    }

    if (list.isEmpty()) {
        EmptyBox("سند دریافتی یافت نشد", subtitle = "فیلترها را عوض کنید یا عبارت دیگری جستجو کنید")
    }
}

// ============================================================ پرداخت‌ها

@Composable
private fun PaymentsSection(q: String, onQ: (String) -> Unit, filter: FinFilter) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current

    OutlinedTextField(
        value = q,
        onValueChange = onQ,
        placeholder = { Text("جستجوی سند یا سرفصل هزینه...") },
        singleLine = true,
        leadingIcon = { Icon(MrIcons.Search, contentDescription = null, tint = scheme.primary) },
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    )

    val list = paymentsDemo.filter { filter.matches(it) && (q.isBlank() || it.title.contains(q, true) || it.header.contains(q, true)) }
    val total = list.sumOf { it.amount }
    val cash = list.filter { it.kind == "نقد" }.sumOf { it.amount }
    val expense = list.filter { it.kind == "هزینه" }.sumOf { it.amount }
    val check = list.filter { it.kind == "چک" }.sumOf { it.amount }

    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "سرجمع پرداخت‌ها",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                    modifier = Modifier.weight(1f),
                )
                CalmPulse(scheme.error, dotSize = 7.dp)
            }
            NumberHero(faMoney(total), unit = "تومان", color = scheme.primary, autoFit = true, fixedHeight = 36.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                SubtotalChip("نقد", faMoney(cash), extras.positive, Modifier.weight(1f))
                SubtotalChip("هزینه", faMoney(expense), Color(0xFFE599F7), Modifier.weight(1f))
                SubtotalChip("چک", faMoney(check), Color(0xFFFFA94D), Modifier.weight(1f))
            }
        }
    }

    listOf(
        "نقد" to "پول نقد پرداختی",
        "هزینه" to "سرفصل‌های هزینه‌های ثبت‌شده",
        "چک" to "چک‌های پرداختی توسط صندوق",
    ).forEach { (kind, label) ->
        val group = list.filter { it.kind == kind }
        if (group.isNotEmpty()) {
            SectionHeader(label, group.sumOf { it.amount }, kindIcon(kind), kindColor(kind, extras.positive))
            group.forEach { d -> FinCard(d, extras) }
        }
    }

    if (list.isEmpty()) {
        EmptyBox("سند پرداختی یافت نشد", subtitle = "فیلترها را عوض کنید یا عبارت دیگری جستجو کنید")
    }
}

// ============================================================ گزارش روزانه

@Composable
private fun DailySection(filter: FinFilter) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var day by remember { mutableStateOf("امروز") }

    // فیلترهای هوشمند روزانه
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        dailyDemo.map { it.day }.forEach { d ->
            val sel = day == d
            Text(
                d,
                style = MaterialTheme.typography.labelMedium,
                color = if (sel) extras.goldOn else scheme.onSurfaceVariant,
                fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (sel) Brush.horizontalGradient(extras.goldGradient)
                        else Brush.verticalGradient(
                            listOf(scheme.surfaceVariant.copy(alpha = 0.5f), scheme.surfaceVariant.copy(alpha = 0.5f))
                        )
                    )
                    .border(1.dp, if (sel) Color.Transparent else extras.hairline, RoundedCornerShape(50))
                    .clickable { day = d; SoundFx.soft() }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            )
        }
    }

    val r = dailyDemo.first { it.day == day }
    val cashBox = r.cashIn - r.cashOut - r.expenseOut

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // مانده صندوق — عدد قهرمان
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "مانده صندوق — $day",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                )
                NumberHero(
                    faMoney(cashBox),
                    unit = "تومان",
                    color = if (cashBox >= 0) extras.positive else scheme.error,
                    autoFit = true,
                    fixedHeight = 36.dp,
                )
                Text(
                    "دریافت نقدی منهای پرداخت نقدی و هزینه‌های همان روز",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }

        // فروش روز
        SectionHeader("فروش روز — ${r.invoices} فاکتور", r.sales, MrIcons.Trend, extras.positive)

        // دریافت‌های روز — تفکیک‌شده
        SectionHeader("دریافت‌های روز", r.cashIn + r.posIn + r.checkIn, MrIcons.Receivables, extras.positive)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
            SubtotalChip("نقد", faMoney(r.cashIn), extras.positive, Modifier.weight(1f))
            SubtotalChip("پوز", faMoney(r.posIn), Color(0xFF74C0FC), Modifier.weight(1f))
            SubtotalChip("چک", faMoney(r.checkIn), Color(0xFFFFA94D), Modifier.weight(1f))
        }

        // پرداخت‌های روز — تفکیک‌شده
        SectionHeader("پرداخت‌های روز", r.cashOut + r.expenseOut + r.checkOut, MrIcons.Wallet, Color(0xFFE599F7))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
            SubtotalChip("نقد", faMoney(r.cashOut), extras.positive, Modifier.weight(1f))
            SubtotalChip("هزینه", faMoney(r.expenseOut), Color(0xFFE599F7), Modifier.weight(1f))
            SubtotalChip("چک", faMoney(r.checkOut), Color(0xFFFFA94D), Modifier.weight(1f))
        }

        if (filter.user != "همه") {
            Text(
                "فیلتر کاربر فعال: ${filter.user} — ارقام کامل روز است",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
        }
        Text(
            "★ گزارش روزانه با فیلترهای هوشمند — برای بستن هر روز مالی",
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
    }
}

// ============================================================ اجزای مشترک

/** سربرگ گروه با سرجمع */
@Composable
private fun SectionHeader(label: String, sum: Long, icon: ImageVector, color: Color) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.30f), color.copy(alpha = 0.10f))))
                .border(1.dp, color.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        Text(
            faMoney(sum),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = color,
        )
    }
}

/** چیپ سرجمع دسته */
@Composable
private fun SubtotalChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = color,
            maxLines = 1,
        )
    }
}

/** کارت سند مالی — گوی سه‌بعدی هماهنگ با نوع + مبلغ */
@Composable
private fun FinCard(d: FinDoc, extras: ir.atiran.hamrah.viewer.ui.theme.ThemeExtras) {
    val scheme = MaterialTheme.colorScheme
    val color = kindColor(d.kind, extras.positive)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 62.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(extras.glass)
            .border(1.dp, extras.hairline, RoundedCornerShape(16.dp))
            .clickable { SoundFx.soft() }
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        // گوی سه‌بعدی نوع سند
        Box(
            Modifier
                .size(38.dp)
                .background(
                    Brush.radialGradient(listOf(color.copy(alpha = 0.26f), Color.Transparent)),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(color.copy(alpha = 0.30f), color.copy(alpha = 0.10f))
                        )
                    )
                    .border(1.dp, color.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(kindIcon(d.kind), contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(d.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(
                d.party + if (d.header.isNotBlank()) " · سرفصل: " + d.header else "",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                faMoney(d.amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = color,
            )
            Text(
                "تومان" + if (d.isServer) " · سرور" else "",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

/** دکمه گوی فیلترها با نشان تعداد فعال */
@Composable
private fun MrOrbFilter(active: Int, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Box {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(scheme.primary.copy(alpha = 0.30f), scheme.primary.copy(alpha = 0.10f))
                    )
                )
                .border(1.dp, scheme.primary.copy(alpha = 0.5f), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(MrIcons.Filter, contentDescription = "فیلترها", tint = scheme.primary, modifier = Modifier.size(15.dp))
        }
        if (active > 0) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(1.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(extras.goldGradient))
                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    TableHeuristics.faDigits(active.toString()),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = extras.goldOn,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

/** شیت فیلترهای کامل خزانه */
@Composable
private fun FilterSheet(filter: FinFilter, onApply: (FinFilter) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var f by remember { mutableStateOf(filter) }

    Dialog(onDismissRequest = { onApply(f) }) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.surface.copy(alpha = 0.97f))
                .border(1.dp, extras.hairline, RoundedCornerShape(24.dp))
                .padding(16.dp)
                .heightIn(max = 600.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(scheme.primary.copy(alpha = 0.30f), scheme.primary.copy(alpha = 0.10f))
                            )
                        )
                        .border(1.dp, scheme.primary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(MrIcons.Filter, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(9.dp))
                Text("فیلترهای خزانه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            FilterGroup("بازه تاریخ", listOf("همه", "امروز", "این هفته", "این ماه"), f.range) { f = f.copy(range = it) }
            FilterGroup("نوع سند", listOf("همه", "نقد", "پوز", "چک", "هزینه"), f.kind) { f = f.copy(kind = it) }
            FilterGroup("گروه مشتری", listOf("همه", "عمده", "نیمه‌عمده", "صنفی"), f.group) { f = f.copy(group = it) }
            FilterGroup("ویزیتور", listOf("همه") + posUsers, f.visitor) { f = f.copy(visitor = it) }
            FilterGroup("پوز / بانک", listOf("همه", "پوز سامان", "پوز ملت", "صندوق", "بانک ملت", "بانک سامان", "بانک پاسارگاد"), f.device) { f = f.copy(device = it) }
            FilterGroup("کاربر سیستم", listOf("همه", "مدیر") + posUsers, f.user) { f = f.copy(user = it) }
            FilterGroup("سرفصل هزینه", listOf("همه", "خرید کالا", "اجاره", "حمل‌ونقل", "حقوق", "تبلیغات", "سوخت", "بسته‌بندی"), f.header) { f = f.copy(header = it) }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "حذف فیلترها",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(scheme.error.copy(alpha = 0.10f))
                        .border(1.dp, scheme.error.copy(alpha = 0.4f), RoundedCornerShape(50))
                        .clickable { f = FinFilter(); SoundFx.soft() }
                        .padding(vertical = 9.dp)
                        .fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(extras.goldGradient))
                        .clickable { onApply(f); SoundFx.success() }
                        .padding(vertical = 9.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "اعمال فیلترها",
                        color = extras.goldOn,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

/** یک گروه فیلتر — برچسب + چیپ‌ها */
@Composable
private fun FilterGroup(label: String, options: List<String>, selected: String, onPick: (String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            options.forEach { o ->
                val sel = selected == o
                Text(
                    o,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (sel) scheme.onSurface else scheme.onSurfaceVariant,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (sel) scheme.primary.copy(alpha = 0.16f) else scheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                        .border(
                            1.dp,
                            if (sel) scheme.primary.copy(alpha = 0.45f) else extras.hairline,
                            RoundedCornerShape(50),
                        )
                        .clickable { onPick(o); SoundFx.soft() }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
    }
}
