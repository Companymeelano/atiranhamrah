package ir.atiran.hamrah.viewer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.AiChatSheet
import ir.atiran.hamrah.viewer.ui.components.AiGreetingOverlay
import ir.atiran.hamrah.viewer.ui.components.AiSettingsSheet
import ir.atiran.hamrah.viewer.ui.components.AmbientBackground
import ir.atiran.hamrah.viewer.ui.components.CalmPulse
import ir.atiran.hamrah.viewer.ui.components.EmptyBox
import ir.atiran.hamrah.viewer.ui.components.GlassAction
import ir.atiran.hamrah.viewer.ui.components.GlassCard
import ir.atiran.hamrah.viewer.ui.components.LightLine
import ir.atiran.hamrah.viewer.ui.components.MrPillButton
import ir.atiran.hamrah.viewer.ui.components.MrIcons
import ir.atiran.hamrah.viewer.ui.components.MrOrbButton
import ir.atiran.hamrah.viewer.ui.components.MrSubtitle
import ir.atiran.hamrah.viewer.ui.components.NumberHero
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import ir.atiran.hamrah.viewer.utils.AiBrain
import ir.atiran.hamrah.viewer.utils.SoundFx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ============================================================ داده نمونه
private val tabs = listOf("نمای کلی", "مشتریان", "کالاها", "گزارش‌ها", "هشدارها", "اعلان‌ها")

private val flowDates = listOf(
    "۱ شهریور", "۳ شهریور", "۵ شهریور", "۷ شهریور", "۹ شهریور", "۱۱ شهریور",
    "۱۳ شهریور", "۱۵ شهریور", "۱۷ شهریور", "۱۹ شهریور", "۲۱ شهریور", "۲۳ شهریور",
)
private val flowValues = listOf(120f, 95f, 160f, 140f, 185f, 130f, 175f, 210f, 165f, 195f, 230f, 185f)

private data class Slice(val label: String, val value: Float, val amount: String)
private val receivables = listOf(
    Slice("وصول‌شده", 68f, "۶۸٪"),
    Slice("باز", 22f, "۲۲٪"),
    Slice("سررسیدشده", 10f, "۱۰٪"),
)
private val productShare = listOf(
    Slice("پسته", 38f, ""), Slice("بادام", 24f, ""), Slice("گردو", 18f, ""),
    Slice("فندق", 12f, ""), Slice("کشمش", 8f, ""),
)

private data class Hero(
    val title: String, val value: String, val unit: String, val desc: String,
    val icon: ImageVector, val spark: List<Int>,
)
private val heroes = listOf(
    Hero("مشتریان", "۲٬۱۲۰", "نفر", "مشتری فعال در این دوره", MrIcons.Customers, listOf(40, 48, 52, 58, 55, 64, 72)),
    Hero("کالاها", "۳۴۸", "قلم", "قلم کالای فعال", MrIcons.Products, listOf(60, 58, 62, 65, 63, 68, 70)),
    Hero("گردش مالی", "۸۶۴م", "تومان", "گردش حساب این ماه", MrIcons.Trend, listOf(35, 45, 42, 58, 64, 72, 88)),
    Hero("مطالبات معوق", "۷", "مشتری", "بدهی بیش از ۳۰ روز — نیازمند پیگیری", MrIcons.Receivables, listOf(26, 29, 27, 31, 30, 34, 38)),
)

private data class P3(val name: String, val v: Float)
private val topProducts3D = listOf(
    P3("پسته اکبری", 95f), P3("بادام ممتاز", 72f), P3("گردو کاغذی", 58f),
    P3("مغز پسته", 46f), P3("کشمش تیزابی", 34f), P3("فندق", 26f),
)

private data class CustomerD(
    val name: String, val balance: String, val tag: String, val share: Float,
)
private val customersDemo = listOf(
    CustomerD("هایپر طلایی", "+۴۸٬۲۰۰٬۰۰۰", "پلاتینی", 1f),
    CustomerD("فروشگاه زرین", "+۳۱٬۷۰۰٬۰۰۰", "طلایی", 0.86f),
    CustomerD("پخش نگین", "+۱۹٬۴۰۰٬۰۰۰", "طلایی", 0.65f),
    CustomerD("مارکت الماس", "−۱۲٬۹۰۰٬۰۰۰", "نقره‌ای", 0.56f),
    CustomerD("سوپرمارکت بهار", "+۸٬۳۰۰٬۰۰۰", "برنزی", 0.39f),
)

private data class TimelineEntry(val when_: String, val title: String, val amount: String, val positive: Boolean)

/** گردش مشتری در سه بازه زمانی — فیلتر واقعی */
private val activityByRange = mapOf(
    "هفته" to listOf(
        TimelineEntry("امروز", "فاکتور 10325", "+ ۱۸٬۵۰۰٬۰۰۰", true),
        TimelineEntry("۲ روز قبل", "دریافت", "− ۱۰٬۰۰۰٬۰۰۰", false),
        TimelineEntry("۵ روز قبل", "فاکتور 10302", "+ ۷٬۸۰۰٬۰۰۰", true),
    ),
    "ماه" to listOf(
        TimelineEntry("امروز", "فاکتور 10325", "+ ۱۸٬۵۰۰٬۰۰۰", true),
        TimelineEntry("۲ روز قبل", "دریافت", "− ۱۰٬۰۰۰٬۰۰۰", false),
        TimelineEntry("۵ روز قبل", "فاکتور 10302", "+ ۷٬۸۰۰٬۰۰۰", true),
        TimelineEntry("هفته قبل", "چک وصول شد", "− ۲۲٬۰۰۰٬۰۰۰", false),
        TimelineEntry("۲ هفته قبل", "فاکتور 10288", "+ ۹٬۶۰۰٬۰۰۰", true),
        TimelineEntry("۳ هفته قبل", "برگشتی کالا", "− ۳٬۱۰۰٬۰۰۰", false),
    ),
    "سال" to listOf(
        TimelineEntry("امروز", "فاکتور 10325", "+ ۱۸٬۵۰۰٬۰۰۰", true),
        TimelineEntry("۵ روز قبل", "فاکتور 10302", "+ ۷٬۸۰۰٬۰۰۰", true),
        TimelineEntry("۲ هفته قبل", "فاکتور 10288", "+ ۹٬۶۰۰٬۰۰۰", true),
        TimelineEntry("مرداد", "دریافت", "− ۴۵٬۰۰۰٬۰۰۰", false),
        TimelineEntry("تیر", "فاکتور 10241", "+ ۳۲٬۰۰۰٬۰۰۰", true),
        TimelineEntry("خرداد", "چک وصول شد", "− ۲۸٬۰۰۰٬۰۰۰", false),
        TimelineEntry("اردیبهشت", "فاکتور 10210", "+ ۲۱٬۵۰۰٬۰۰۰", true),
        TimelineEntry("فروردین", "دریافت", "− ۱۵٬۰۰۰٬۰۰۰", false),
    ),
)

private data class AlertCard(val title: String, val count: String, val desc: String, val color: Color)
private val attentionCards = listOf(
    AlertCard("چک سررسید", "۲ مورد", "چک‌هایی که امروز یا فردا سررسید می‌شوند", Color(0xFFFF6B6B)),
    AlertCard("مطالبات", "۸ مشتری", "مطالبات معوق بیش از ۶۰ روز", Color(0xFFFFA94D)),
    AlertCard("پیگیری مشتری", "۵ مورد", "مشتریان نیازمند تماس پیگیری", Color(0xFFFFD43B)),
)

private data class Notif(val cat: String, val title: String, val sub: String, val time: String, val color: Color)
private val notifs = listOf(
    Notif("چک", "چک مشتری «فروشگاه زرین»", "سررسید امروز — ۱۲:۴۵", "۱۲:۴۵", Color(0xFFFF6B6B)),
    Notif("مالی", "وصول ۴۵٬۰۰۰٬۰۰۰ تومان", "از حساب هایپر طلایی", "۱۱:۲۰", Color(0xFF69DB7C)),
    Notif("مهم", "گزارش ماهانه آماده شد", "گزارش گردش حساب شهریور", "۱۰:۰۵", Color(0xFFFFD43B)),
    Notif("مشتری", "مشتری جدید ثبت شد", "سوپرمارکت بهار — توسط علی", "۰۹:۳۰", Color(0xFF74C0FC)),
    Notif("چک", "چک مشتری «پخش نگین»", "سررسید فردا", "دیروز", Color(0xFFFF6B6B)),
)

private val reportTypes = listOf("مشتریان", "کالاها", "موجودی", "چک", "گردش حساب", "مطالبات")
private val reportFilters = listOf("تاریخ", "گروه", "مشتری", "کالا", "مبلغ", "وضعیت")

// ============================================================ صفحه
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScreen(vm: AppViewModel) {
    BackHandler { vm.closeDemo() }
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var tab by remember { mutableIntStateOf(0) }
    var paletteOpen by remember { mutableStateOf(false) }
    var customerOpen by remember { mutableStateOf<CustomerD?>(null) }
    var productOpen by remember { mutableStateOf<P3?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val refresh: (Boolean) -> Unit = { silent ->
        if (!refreshing) {
            refreshing = true
            scope.launch {
                delay(900)
                vm.touchSync()
                refreshing = false
                if (!silent) SoundFx.soft()
            }
        }
    }
    BackHandler(enabled = customerOpen != null) { customerOpen = null }
    var syncSheetOpen by remember { mutableStateOf(false) }
    var themeSheetOpen by remember { mutableStateOf(false) }
    var expSheetOpen by remember { mutableStateOf(false) }
    var aiChatOpen by remember { mutableStateOf(false) }
    var aiSettingsOpen by remember { mutableStateOf(false) }
    var greetVisible by remember { mutableStateOf(false) }
    // پسته بعد از ورود سلام می‌کند (فقط اگر فعال باشد)
    LaunchedEffect(vm.ai.enabled) {
        if (vm.ai.enabled) {
            delay(1100)
            greetVisible = true
            delay(7500)
            greetVisible = false
        }
    }
    var intervalSec by remember { mutableStateOf(0) }
    // به‌روزرسانی خودکار — بدون تغییر هیچ داده‌ای در آتیران
    LaunchedEffect(intervalSec) {
        if (intervalSec > 0) {
            while (true) {
                delay(intervalSec.toLong() * 1000L)
                refresh(true)
            }
        }
    }

    AmbientBackground(enabled = vm.experience.ambient) {
        Box(Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
                title = {
                    Text(
                        "M•REPORT",
                        style = TextStyle(
                            brush = Brush.horizontalGradient(extras.brand),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { vm.closeDemo() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    // نوار کنترل: جستجو + همگام‌سازی آتیران + شخصیت بصری + تجربه رابط
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(end = 4.dp),
                    ) {
                        MrOrbButton(MrIcons.Spark, "دستیار هوشمند پسته", size = 33.dp, tint = Color(0xFF8FB260)) {
                            if (vm.ai.enabled) aiChatOpen = true else aiSettingsOpen = true
                            SoundFx.soft()
                        }
                        MrOrbButton(MrIcons.Search, "جستجوی هوشمند", size = 33.dp) { paletteOpen = true; SoundFx.soft() }
                        MrOrbButton(
                            MrIcons.Sync, "اتصال و همگام‌سازی آتیران",
                            size = 33.dp,
                            tint = if (refreshing) Color(0xFFFFA94D) else Color(0xFF69DB7C),
                            active = refreshing,
                        ) { syncSheetOpen = true; SoundFx.soft() }
                        MrOrbButton(MrIcons.Theme, "شخصیت بصری", size = 33.dp, tint = extras.gold) { themeSheetOpen = true; SoundFx.soft() }
                        MrOrbButton(MrIcons.Waves, "تجربه رابط", size = 33.dp, tint = extras.accent) { expSheetOpen = true; SoundFx.soft() }
                    }
                },
            )

            // نوار ناوبری — همه بخش‌ها همیشه در یک نگاه
            MrTabBar(
                selected = tab,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            ) { i ->
                tab = i
                SoundFx.soft()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(Modifier.height(2.dp))
                when (tab) {
                    0 -> OverviewTab(vm, refreshing)
                    1 -> CustomersTab { customerOpen = it }
                    2 -> ProductsTab { productOpen = it }
                    3 -> ReportsTab()
                    4 -> AlertsTab(vm)
                    else -> NotificationsTab(vm) { tab = 4 }
                }
                Text(
                    "★ داده‌های این بخش صرفاً برای نمایش قابلیت‌های M•REPORT است",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
            }
        }
        // خوشامدگویی پسته — دستیار هوشمند
        if (greetVisible) {
            AiGreetingOverlay(
                text = AiBrain.greeting(
                    AiBrain.Ctx(vm.ai.userName, vm.ai.mode, vm.ai.domains, vm.ai.visits)
                ),
                onChat = { greetVisible = false; aiChatOpen = true },
                onClose = { greetVisible = false },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 14.dp, vertical = 84.dp),
            )
        }
        }
    }

    if (paletteOpen) {
        CommandPalette(
            onDismiss = { paletteOpen = false },
            onOpenCustomer = { paletteOpen = false; customerOpen = it },
            onOpenProduct = { paletteOpen = false; tab = 2; productOpen = it },
            onGoTab = { paletteOpen = false; tab = it },
        )
    }

    customerOpen?.let { c ->
        Customer360(c, onDismiss = { customerOpen = null })
    }

    productOpen?.let { p ->
        Product360(p, onDismiss = { productOpen = null })
    }

    if (syncSheetOpen) {
        SyncSheet(
            vm = vm,
            refreshing = refreshing,
            intervalSec = intervalSec,
            onInterval = { intervalSec = it },
            onRefresh = { refresh(false) },
            onDismiss = { syncSheetOpen = false },
        )
    }
    if (themeSheetOpen) {
        ThemeSheet(vm) { themeSheetOpen = false }
    }
    if (expSheetOpen) {
        ExperienceSheet(vm) { expSheetOpen = false }
    }
    if (aiChatOpen) {
        AiChatSheet(
            vm = vm,
            onDismiss = { aiChatOpen = false },
            onOpenSettings = { aiChatOpen = false; aiSettingsOpen = true },
        )
    }
    if (aiSettingsOpen) {
        AiSettingsSheet(vm) { aiSettingsOpen = false }
    }
}

// ============================================================ نوار ناوبری اصلی
/**
 * شش بخش M•REPORT همیشه در یک صفحه — بدون اسکرول:
 * آیکون اختصاصی + برچسب، حالت انتخاب با شیشه‌ی رنگی و نقطه امضای برند.
 */
@Composable
private fun MrTabBar(
    selected: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val icons = listOf(
        MrIcons.Overview, MrIcons.Customers, MrIcons.Products,
        MrIcons.Reports, MrIcons.Alerts, MrIcons.Notifications,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(scheme.surfaceVariant.copy(alpha = 0.22f))
            .border(1.dp, extras.hairline, RoundedCornerShape(18.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        tabs.forEachIndexed { i, label ->
            val sel = selected == i
            val c = scheme.primary
            // سلول با ارتفاع ثابت — فشردن دکمه هرگز چیدمان را تکان نمی‌دهد
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (sel) Brush.verticalGradient(listOf(c.copy(alpha = 0.15f), c.copy(alpha = 0.04f)))
                        else SolidColor(Color.Transparent)
                    )
                    .border(
                        1.dp,
                        if (sel) c.copy(alpha = 0.42f) else Color.Transparent,
                        RoundedCornerShape(14.dp),
                    )
                    .clickable { onSelect(i) },
            ) {
                Spacer(Modifier.height(7.dp))
                // گوی شیشه‌ای سه‌بعدی — ثابت‌اندازه، فقط جلوه‌اش عوض می‌شود
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (sel) Brush.verticalGradient(listOf(c.copy(alpha = 0.34f), c.copy(alpha = 0.10f)))
                            else SolidColor(scheme.surfaceVariant.copy(alpha = 0.30f))
                        )
                        .border(1.dp, if (sel) c.copy(alpha = 0.55f) else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icons[i],
                        contentDescription = label,
                        tint = if (sel) scheme.primary else scheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp),
                    )
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                    color = if (sel) scheme.onSurface else scheme.onSurfaceVariant,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                )
                // جای نقطه همیشه رزرو شده — هیچ جهشی در ارتفاع رخ نمی‌دهد
                Box(
                    Modifier.height(6.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(3.5.dp)
                            .clip(CircleShape)
                            .background(if (sel) c else Color.Transparent)
                    )
                }
            }
        }
    }
}

// ============================================================ تب‌ها
@Composable
private fun MTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) Brush.horizontalGradient(extras.goldGradient)
                else SolidColor(scheme.surfaceVariant.copy(alpha = 0.5f))
            )
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else extras.hairline,
                shape = RoundedCornerShape(50),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    ) {
        Text(
            label,
            color = if (selected) extras.goldOn else scheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

// ============================================================ تب نمای کلی
@Composable
private fun OverviewTab(vm: AppViewModel, refreshing: Boolean) {
    val motion = vm.experience.motion
    val extras = LocalThemeExtras.current
    val scheme = MaterialTheme.colorScheme
    val savedOrder by vm.heroOrder.collectAsState()
    val order = savedOrder?.split(",")
        ?.mapNotNull { it.trim().toIntOrNull() }
        ?.filter { it in heroes.indices }
        ?.takeIf { it.size == heroes.size }
        ?: heroes.indices.toList()
    var editLayout by remember { mutableStateOf(false) }
    val g1 = stagger(0, motion)
    val g2 = stagger(200, motion)
    val g3 = stagger(400, motion)
    val g4 = stagger(600, motion)

    // ---------- پوستر ----------
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
    ) {
        MReportTitle(vm)
    }

    // ---------- M•R Pulse: تابلوی وضعیت کسب‌وکار — امضای بصری ----------
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "نبض کسب‌وکار",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                    modifier = Modifier.weight(1f),
                )
                CalmPulse(
                    if (refreshing) Color(0xFFFFA94D) else Color(0xFF69DB7C),
                    dotSize = 8.dp,
                    enabled = refreshing,
                )
            }
            PulseRow(MrIcons.Customers, "مشتریان", Color(0xFF69DB7C), "پایدار", 0.95f)
            PulseRow(MrIcons.Receivables, "مطالبات", Color(0xFFFFA94D), "نیازمند توجه", 0.62f)
            PulseRow(MrIcons.Checks, "چک‌ها", Color(0xFFFF6B6B), "بحرانی", 0.35f)
            PulseRow(MrIcons.Products, "موجودی", Color(0xFF69DB7C), "پایدار", 0.88f)
            PulseRow(MrIcons.Settings, "سیستم", extras.accent, "پایدار", 1f)
            PulseRow(
                MrIcons.Sync, "اتصال آتیران",
                if (refreshing) Color(0xFFFFA94D) else Color(0xFF69DB7C),
                if (refreshing) "در حال دریافت" else "متصل",
                if (refreshing) 0.55f else 1f,
            )
        }
    }

    // ---------- M•R Intelligence: لایه هوشمند روی داده‌ها ----------
    Section("M•R Intelligence") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InsightRow(
                MrIcons.Alerts, Color(0xFFFFA94D),
                "۷ مشتری بیش از ۳۰ روز بدهی دارند",
                "مجموع مطالبات این گروه: ۲۴۸٬۰۰۰٬۰۰۰ تومان",
                chart = { MiniAgingBars() },
            )
            InsightRow(
                MrIcons.Customers, scheme.error,
                "بیشترین بدهکار: هایپر طلایی",
                "۴۸٬۲۰۰٬۰۰۰ تومان — نزدیک‌ترین سررسید: ۱۸ شهریور",
                chart = { MiniShareDonut() },
            )
            InsightRow(
                MrIcons.Trend, extras.positive,
                "روند مطالبات نسبت به دوره قبل",
                "کاهش ۱۲٪ — بهبود وضعیت وصول مطالبات",
                chart = { MiniTrendDown(extras.positive) },
            )
            Text(
                "تحلیل خودکار روی داده‌های موجود — بدون تغییر هیچ اطلاعاتی",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }

    // ---------- چهار کارت اصلی — قابل شخصی‌سازی ----------
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (editLayout) {
            Text("چیدمان دلخواه را با فلش‌ها مرتب کنید", style = MaterialTheme.typography.labelSmall, color = scheme.primary)
            Spacer(Modifier.width(8.dp))
        }
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (editLayout) scheme.primary.copy(alpha = 0.18f) else extras.glassStrong)
                .border(1.dp, if (editLayout) scheme.primary else extras.hairline, CircleShape)
                .clickable { editLayout = !editLayout; SoundFx.soft() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(MrIcons.Settings, contentDescription = "شخصی‌سازی داشبورد", tint = if (editLayout) scheme.primary else scheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
    }
    val ordered = order.map { heroes[it] }
    ordered.chunked(2).forEachIndexed { r, row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEachIndexed { ci, h ->
                val pos = r * 2 + ci
                HeroCard(
                    h, motion, Modifier.weight(1f),
                    showHandles = editLayout,
                    onUp = {
                        if (pos > 0) {
                            val n = order.toMutableList(); n.add(pos - 1, n.removeAt(pos)); vm.setHeroOrder(n)
                        }
                    },
                    onDown = {
                        if (pos < order.size - 1) {
                            val n = order.toMutableList(); n.add(pos + 1, n.removeAt(pos)); vm.setHeroOrder(n)
                        }
                    },
                )
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }

    // ---------- چارت اصلی: روند مطالبات و وصول طلب ----------
    Section("روند مطالبات و وصول طلب — ۸ ماه اخیر") {
        ReceivablesTrendChart(progress = g2)
    }

    // ---------- دونات مطالبات ----------
    Section("وضعیت مطالبات") {
        ReceivablesDonut(progress = g3)
    }

    // ---------- ستون‌های سه‌بعدی ----------
    Section("Top Products — امضای بصری M•REPORT") {
        Columns3D(topProducts3D, progress = g4)
    }
}

// ============================================================ تیتر پوستر
@OptIn(ExperimentalTextApi::class)
@Composable
private fun MReportTitle(vm: AppViewModel) {
    val extras = LocalThemeExtras.current
    val wowDone by vm.wowSweepDone.collectAsState()

    // ---- جاروی نور: فقط یک‌بار در اولین ورود (۱۰٪ Wow — بعد بازنشسته می‌شود) ----
    val sweep = remember { Animatable(0f) }
    var played by remember { mutableStateOf(false) }
    LaunchedEffect(wowDone) {
        if (wowDone == false && !played) {
            played = true
            delay(700)
            sweep.animateTo(1f, tween(1500, easing = LinearEasing))
            vm.markWowDone()
        }
    }

    // نوار روشن که یک‌بار از چپ به راست از روی تیتر عبور می‌کند
    val t = sweep.value
    val titleBrush = if (t > 0f && t < 1f) {
        val band = 0.26f
        val c = t * (1f + band) - band / 2f
        val s0 = (c - band / 2f).coerceIn(0f, 1f)
        val s1 = c.coerceIn(0f, 1f)
        val s2 = (c + band / 2f).coerceIn(0f, 1f)
        val hi = lerp(extras.brand[1], Color.White, 0.85f)
        Brush.horizontalGradient(
            0f to extras.brand.first(),
            s0 to extras.brand[1],
            s1 to hi,
            s2 to extras.brand[1],
            1f to extras.brand.last(),
        )
    } else {
        Brush.horizontalGradient(extras.brand)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "M•REPORT",
            style = TextStyle(
                brush = titleBrush,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
            ),
        )
        Spacer(Modifier.height(4.dp))
        MrSubtitle("Intelligent Reporting Experience")
        Spacer(Modifier.height(10.dp))
        LightLine(width = 200.dp)
    }
}

// ============================================================ ردیف تابلوی نبض
/** رنگ وضعیت هماهنگ با تم — در تم‌های روشن تیره/اشباع می‌شود تا خوانا بماند */
@Composable
private fun statusTone(base: Color): Color {
    val light = MaterialTheme.colorScheme.background.luminance() > 0.5f
    return if (light) lerp(base, Color.Black, 0.34f) else base
}

/** نشان درخشان + برچسب تک‌خطی + نوار سیگنال گرادیانی + واژه وضعیت — هم‌زبان با تم */
@Composable
private fun PulseRow(icon: ImageVector, label: String, base: Color, word: String, frac: Float) {
    val scheme = MaterialTheme.colorScheme
    val color = statusTone(base)
    Row(verticalAlignment = Alignment.CenterVertically) {
        // نشان درخشان: هاله نور پشت گوی شیشه‌ای
        Box(
            Modifier
                .size(32.dp)
                .background(
                    Brush.radialGradient(listOf(color.copy(alpha = 0.30f), Color.Transparent)),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(color.copy(alpha = 0.30f), color.copy(alpha = 0.12f))
                        )
                    )
                    .border(1.dp, color.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = scheme.onSurface,
            maxLines = 1,
            modifier = Modifier.width(68.dp),
        )
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(50))
                .background(scheme.surfaceVariant.copy(alpha = 0.45f)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(frac)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.45f), color))),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            word,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = color,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.width(80.dp),
        )
    }
}

// ============================================================ ردیف هوشمندی
@Composable
private fun InsightRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    sub: String,
    chart: (@Composable () -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .border(1.dp, LocalThemeExtras.current.hairline, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (chart != null) {
            Spacer(Modifier.width(10.dp))
            chart()
        }
    }
}

// ============================================================ میکروچارت‌های هوشمندی
/** سن بدهی: سه ستون ۳۰-۶۰ / ۶۰-۹۰ / +۹۰ روز — رنگ گرم وضعیت */
@Composable
private fun MiniAgingBars() {
    val extras = LocalThemeExtras.current
    Canvas(Modifier.size(62.dp, 38.dp)) {
        val heights = listOf(0.42f, 0.68f, 0.95f)
        val cols = listOf(Color(0xFFFFA94D), Color(0xFFFF8A5C), Color(0xFFFF6B6B))
        val w = size.width / 3f
        // خط پایه مویی
        drawLine(extras.hairline, Offset(0f, size.height), Offset(size.width, size.height), 1f)
        heights.forEachIndexed { i, f ->
            val h = size.height * f * 0.90f
            val left = i * w + w * 0.26f
            // هاله نور پشت ستون
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(cols[i].copy(alpha = 0.16f), Color.Transparent)),
                topLeft = Offset(left, size.height - h - 3.dp.toPx()),
                size = Size(w * 0.48f, h + 3.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx()),
            )
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(cols[i].copy(alpha = 0.45f), cols[i])),
                topLeft = Offset(left, size.height - h),
                size = Size(w * 0.48f, h),
                cornerRadius = CornerRadius(2.5.dp.toPx()),
            )
        }
    }
}

/** سهم بزرگ‌ترین بدهکار از کل مطالبات — حلقه با متالیک تم */
@Composable
private fun MiniShareDonut() {
    val extras = LocalThemeExtras.current
    Canvas(Modifier.size(38.dp)) {
        val stroke = 5.dp.toPx()
        val r = (size.minDimension - stroke) / 2f
        val tl = Offset((size.width - r * 2) / 2f, (size.height - r * 2) / 2f)
        drawArc(
            color = extras.hairline,
            startAngle = 0f, sweepAngle = 360f, useCenter = false,
            topLeft = tl, size = Size(r * 2, r * 2),
            style = Stroke(stroke),
        )
        // هاله نور دور قوس
        drawArc(
            color = extras.gold.copy(alpha = 0.18f),
            startAngle = -90f, sweepAngle = 252f, useCenter = false,
            topLeft = tl, size = Size(r * 2, r * 2),
            style = Stroke(stroke + 3.dp.toPx()),
        )
        drawArc(
            color = extras.gold,
            startAngle = -90f, sweepAngle = 252f, useCenter = false,
            topLeft = tl, size = Size(r * 2, r * 2),
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
        drawCircle(
            extras.gold.copy(alpha = 0.5f),
            radius = 2.dp.toPx(),
            center = Offset(size.width / 2f, size.height / 2f),
        )
    }
}

/** روند نزولی مطالبات — بهبود وصول، هم‌رنگ positive تم */
@Composable
private fun MiniTrendDown(color: Color) {
    Canvas(Modifier.size(62.dp, 38.dp)) {
        val top = 5.dp.toPx()
        val pts = listOf(
            Offset(0f, top + size.height * 0.10f),
            Offset(size.width * 0.3f, top + size.height * 0.34f),
            Offset(size.width * 0.55f, top + size.height * 0.30f),
            Offset(size.width * 0.8f, top + size.height * 0.58f),
            Offset(size.width, top + size.height * 0.72f),
        )
        val path = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            for (i in 1 until pts.size) {
                val mid = (pts[i - 1].x + pts[i].x) / 2f
                cubicTo(mid, pts[i - 1].y, mid, pts[i].y, pts[i].x, pts[i].y)
            }
        }
        // ناحیه محو زیر منحنی
        val fill = Path().apply {
            moveTo(pts.first().x, size.height)
            pts.forEach { lineTo(it.x, it.y) }
            lineTo(pts.last().x, size.height)
            close()
        }
        drawPath(fill, Brush.verticalGradient(listOf(color.copy(alpha = 0.14f), Color.Transparent)))
        drawPath(path, color = color.copy(alpha = 0.15f), style = Stroke(4.5.dp.toPx(), cap = StrokeCap.Round))
        drawPath(path, color = color, style = Stroke(1.6.dp.toPx(), cap = StrokeCap.Round))
        // نقطه پایان با هاله
        drawCircle(color.copy(alpha = 0.25f), radius = 5.dp.toPx(), center = pts.last())
        drawCircle(color, radius = 2.4.dp.toPx(), center = pts.last())
    }
}

// ============================================================ شیت اتصال و همگام‌سازی آتیران
@Composable
private fun SyncSheet(
    vm: AppViewModel,
    refreshing: Boolean,
    intervalSec: Int,
    onInterval: (Int) -> Unit,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val statusColor = if (refreshing) Color(0xFFFFA94D) else Color(0xFF69DB7C)
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.surface.copy(alpha = 0.97f))
                .border(1.dp, extras.hairline, RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // سربرگ: نشان سه‌بعدی وضعیت
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(statusColor.copy(alpha = 0.30f), statusColor.copy(alpha = 0.10f))))
                        .border(1.dp, statusColor.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(MrIcons.Sync, contentDescription = null, tint = statusColor, modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("اتصال به سرویس آتیران", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (refreshing) "در حال دریافت اطلاعات..." else "متصل — آخرین دریافت: " + fmtTime(vm.lastSyncMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            // زمان پاسخ سرویس
            Text("زمان پاسخ سرویس — ۲۳ میلی‌ثانیه (پایدار)", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            MicroArea(listOf(22f, 18f, 26f, 21f, 19f, 24f, 20f, 23f), scheme.primary)
            // بازه به‌روزرسانی خودکار
            Text("به‌روزرسانی خودکار", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = scheme.primary)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("دستی" to 0, "۳۰ ثانیه" to 30, "۱ دقیقه" to 60, "۵ دقیقه" to 300).forEach { (label, sec) ->
                    val sel = intervalSec == sec
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (sel) scheme.primary.copy(alpha = 0.14f) else extras.glass)
                            .border(1.dp, if (sel) scheme.primary.copy(alpha = 0.45f) else extras.hairline, RoundedCornerShape(12.dp))
                            .clickable { onInterval(sec); SoundFx.soft() }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (sel) scheme.primary else scheme.onSurfaceVariant,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
            }
            // دکمه دریافت مجدد — متالیک تم
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(extras.goldGradient))
                    .clickable { onRefresh(); onDismiss() }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(MrIcons.Sync, contentDescription = null, tint = extras.goldOn, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(8.dp))
                Text("دریافت مجدد اطلاعات", color = extras.goldOn, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

/** قالب زمان «آخرین دریافت اطلاعات» */
private fun fmtTime(ms: Long): String =
    java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date(ms))

// ============================================================ کارت اصلی
@Composable
private fun HeroCard(
    h: Hero, motion: Boolean, modifier: Modifier = Modifier,
    showHandles: Boolean = false, onUp: () -> Unit = {}, onDown: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Box(modifier) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                // سربرگ: نشان سه‌بعدی متالیک + عنوان
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.verticalGradient(extras.goldGradient))
                            .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(h.icon, contentDescription = null, tint = extras.goldOn, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(h.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                // عدد قهرمان — جاگذاری هوشمند، همیشه داخل کارت
                NumberHero(h.value, h.unit, color = scheme.primary, autoFit = true, fixedHeight = 36.dp)
                Text(h.desc, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, maxLines = 1)
                // پنجره داده: چارت در چاهِ نورانی — هم‌عرض و هم‌مرکز کارت
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(scheme.surfaceVariant.copy(alpha = 0.28f))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                ) {
                    MicroArea(h.spark.map { it.toFloat() }, scheme.primary)
                }
            }
        }
        if (showHandles) {
            Row(
                Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                HandleButton(Icons.Filled.KeyboardArrowUp, onUp)
                HandleButton(Icons.Filled.KeyboardArrowDown, onDown)
            }
        }
    }
}

@Composable
private fun HandleButton(icon: ImageVector, onClick: () -> Unit) {
    Box(
        Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .border(1.dp, LocalThemeExtras.current.hairline, CircleShape)
            .clickable { onClick(); SoundFx.soft() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun MicroArea(values: List<Float>, color: Color) {
    Canvas(Modifier.fillMaxWidth().height(32.dp)) {
        // نرمال‌سازی min→max: هر چارت در نوار میانی متقارن ۱۸٪..۸۲٪ — قرینه و مرکز کارت
        val minV = values.min()
        val maxV = values.max()
        fun norm(v: Float): Float =
            if (maxV - minV < 0.0001f) 0.5f else (v - minV) / (maxV - minV)
        val step = size.width / (values.size - 1)
        val pts = values.mapIndexed { i, v ->
            Offset(i * step, size.height * (0.82f - 0.64f * norm(v)))
        }
        val fill = Path().apply {
            moveTo(pts.first().x, size.height)
            pts.forEach { lineTo(it.x, it.y) }
            lineTo(pts.last().x, size.height)
            close()
        }
        drawPath(fill, brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.25f), Color.Transparent)))
        val line = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            for (i in 1 until pts.size) {
                val mid = (pts[i - 1].x + pts[i].x) / 2
                cubicTo(mid, pts[i - 1].y, mid, pts[i].y, pts[i].x, pts[i].y)
            }
        }
        drawPath(line, color = color, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round))
        // نقطه امضا در انتهای روند
        drawCircle(color.copy(alpha = 0.25f), radius = 4.dp.toPx(), center = pts.last())
        drawCircle(color, radius = 2.2.dp.toPx(), center = pts.last())
    }
}

// ============================================================ روند مطالبات و وصول طلب
private data class TrendPoint(val month: String, val receivable: Float, val collected: Float)
private val receivablesTrend = listOf(
    TrendPoint("فروردین", 2.1f, 1.6f),
    TrendPoint("اردیبهشت", 2.4f, 1.9f),
    TrendPoint("خرداد", 2.3f, 2.0f),
    TrendPoint("تیر", 2.6f, 2.2f),
    TrendPoint("مرداد", 2.5f, 2.3f),
    TrendPoint("شهریور", 2.7f, 2.5f),
    TrendPoint("مهر", 2.9f, 2.6f),
    TrendPoint("آبان", 3.1f, 2.9f),
)

/** تبدیل ارقام لاتین به فارسی */
private fun faNum(v: String): String =
    v.map { if (it in '0'..'9') ('۰' + (it - '0')) else it }.joinToString("")

/**
 * چارت دوقلوی مطالبات و وصول — حیاتی‌ترین نمودار مدیریت:
 * منحنی مطالبات (متالیک تم) و وصول طلب (سبز تم) با هاله نور،
 * نقاط امضای برند و Tooltip شیشه‌ای دوقلو.
 */
@Composable
private fun ReceivablesTrendChart(progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var selected by remember { mutableStateOf<Int?>(null) }

    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendChip(extras.gold, "مطالبات")
            LegendChip(extras.positive, "وصول طلب")
        }
        Spacer(Modifier.height(8.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { pos ->
                            val step = size.width / (receivablesTrend.size - 1f)
                            selected = (pos.x / step).roundToInt().coerceIn(0, receivablesTrend.size - 1)
                            SoundFx.soft()
                        }
                    },
            ) {
                val maxV = 3.4f
                val labelSpace = 18.dp.toPx()
                val h = size.height - labelSpace
                val stepX = size.width / (receivablesTrend.size - 1f)
                for (g in 1..3) {
                    drawLine(extras.hairline, Offset(0f, h * g / 4f), Offset(size.width, h * g / 4f), 1f)
                }
                fun ptsOf(pick: (TrendPoint) -> Float): List<Offset> =
                    receivablesTrend.mapIndexed { i, t -> Offset(i * stepX, h - (pick(t) / maxV) * h * progress) }
                val rp = ptsOf { it.receivable }
                val cp = ptsOf { it.collected }
                fun smooth(ps: List<Offset>): Path = Path().apply {
                    moveTo(ps.first().x, ps.first().y)
                    for (i in 1 until ps.size) {
                        val mid = (ps[i - 1].x + ps[i].x) / 2f
                        cubicTo(mid, ps[i - 1].y, mid, ps[i].y, ps[i].x, ps[i].y)
                    }
                }
                fun area(ps: List<Offset>): Path = Path().apply {
                    moveTo(ps.first().x, h)
                    ps.forEach { lineTo(it.x, it.y) }
                    lineTo(ps.last().x, h)
                    close()
                }
                // ناحیه‌های محو
                drawPath(area(rp), Brush.verticalGradient(listOf(extras.gold.copy(alpha = 0.15f), Color.Transparent)))
                drawPath(area(cp), Brush.verticalGradient(listOf(extras.positive.copy(alpha = 0.13f), Color.Transparent)))
                // هاله + خط هر دو منحنی
                drawPath(smooth(cp), color = extras.positive.copy(alpha = 0.12f), style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
                drawPath(smooth(cp), color = extras.positive, style = Stroke(1.6.dp.toPx(), cap = StrokeCap.Round))
                drawPath(smooth(rp), color = extras.gold.copy(alpha = 0.12f), style = Stroke(7.dp.toPx(), cap = StrokeCap.Round))
                drawPath(smooth(rp), color = extras.gold, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round))
                // نقاط امضای برند روی منحنی مطالبات
                rp.forEachIndexed { i, pt ->
                    if (i != selected) {
                        drawCircle(extras.gold.copy(alpha = 0.18f), radius = 4.5.dp.toPx(), center = pt)
                        drawCircle(extras.gold, radius = 2.2.dp.toPx(), center = pt)
                    }
                }
                selected?.let { i ->
                    val pr = rp[i]
                    val pc = cp[i]
                    drawLine(extras.hairline, Offset(pr.x, 0f), Offset(pr.x, h), 1f)
                    drawCircle(extras.positive.copy(alpha = 0.25f), radius = 9.dp.toPx(), center = pc)
                    drawCircle(extras.positive, radius = 3.dp.toPx(), center = pc)
                    drawCircle(extras.gold.copy(alpha = 0.25f), radius = 11.dp.toPx(), center = pr)
                    drawCircle(scheme.surface, radius = 5.5.dp.toPx(), center = pr)
                    drawCircle(extras.gold, radius = 3.dp.toPx(), center = pr)
                }
            }
            // Tooltip شیشه‌ای دوقلو
            selected?.let { i ->
                val t = receivablesTrend[i]
                val stepDp = maxWidth / (receivablesTrend.size - 1)
                val x = (stepDp * i - 74.dp).coerceIn(0.dp, maxWidth - 148.dp)
                Column(
                    modifier = Modifier
                        .absoluteOffset(x = x, y = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(extras.glassStrong)
                        .border(1.dp, extras.hairline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(t.month, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(extras.gold))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "مطالبات " + faNum(t.receivable.toString()).replace('.', '٫'),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(extras.positive))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "وصول " + faNum(t.collected.toString()).replace('.', '٫'),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = extras.positive,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendChip(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ============================================================ چارت گردش حساب با Tooltip
@Composable
private fun GlowAreaChart(
    values: List<Float>,
    dates: List<String>,
    progress: Float,
    motion: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var selected by remember { mutableStateOf<Int?>(null) }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val chartH = 200.dp
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(chartH)
                .pointerInput(values.size) {
                    detectTapGestures { pos ->
                        val step = size.width / (values.size - 1f)
                        selected = (pos.x / step).roundToInt().coerceIn(0, values.size - 1)
                        SoundFx.soft()
                    }
                },
        ) {
            val maxV = values.max() * 1.2f
            val labelSpace = 18.dp.toPx()
            val h = size.height - labelSpace
            val stepX = size.width / (values.size - 1f)
            // خطوط راهنمای مویی
            for (g in 1..3) {
                val y = h * g / 4f
                drawLine(
                    color = extras.hairline,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                )
            }
            val pts = values.mapIndexed { i, v ->
                Offset(i * stepX, h - (v / maxV) * h * progress)
            }
            // ناحیه محو
            val fill = Path().apply {
                moveTo(pts.first().x, h)
                pts.forEach { lineTo(it.x, it.y) }
                lineTo(pts.last().x, h)
                close()
            }
            drawPath(
                fill,
                brush = Brush.verticalGradient(
                    listOf(extras.gold.copy(alpha = 0.20f), Color.Transparent)
                ),
            )
            // منحنی نرم + Glow بسیار کم
            val line = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                for (i in 1 until pts.size) {
                    val mid = (pts[i - 1].x + pts[i].x) / 2
                    cubicTo(mid, pts[i - 1].y, mid, pts[i].y, pts[i].x, pts[i].y)
                }
            }
            drawPath(line, color = extras.gold.copy(alpha = 0.10f), style = Stroke(7.dp.toPx(), cap = StrokeCap.Round))
            drawPath(line, color = extras.gold.copy(alpha = 0.18f), style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
            drawPath(line, color = extras.gold, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round))
            // امضای برند: نقطه‌های پر ظریف روی همه نقاط منحنی
            pts.forEachIndexed { i, pt ->
                if (i != selected) {
                    drawCircle(extras.gold.copy(alpha = 0.18f), radius = 4.5.dp.toPx(), center = pt)
                    drawCircle(extras.gold, radius = 2.2.dp.toPx(), center = pt)
                }
            }
            // نقطه انتخاب‌شده
            selected?.let { i ->
                val p = pts[i]
                drawLine(
                    color = extras.hairline,
                    start = Offset(p.x, 0f),
                    end = Offset(p.x, h),
                    strokeWidth = 1f,
                )
                drawCircle(extras.gold.copy(alpha = 0.25f), radius = 11.dp.toPx(), center = p)
                drawCircle(scheme.surface, radius = 5.5.dp.toPx(), center = p)
                drawCircle(extras.gold, radius = 3.dp.toPx(), center = p)
            }
        }
        // Tooltip شیشه‌ای
        selected?.let { i ->
            val stepDp = maxWidth / (values.size - 1)
            val x = (stepDp * i - 70.dp).coerceIn(0.dp, maxWidth - 140.dp)
            Column(
                modifier = Modifier
                    .absoluteOffset(x = x, y = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(extras.glassStrong)
                    .border(1.dp, extras.hairline, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(dates[i], style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                Text(
                    "${formatInt((values[i] * 1_000_000).toLong())} ریال",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                )
            }
        }
    }
}

// ============================================================ دونات مطالبات
@Composable
private fun ReceivablesDonut(progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val total = receivables.sumOf { it.value.toDouble() }.toFloat()
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 20.dp.toPx()
                    val r = (size.minDimension - stroke) / 2
                    val topLeft = Offset((size.width - r * 2) / 2, (size.height - r * 2) / 2)
                    drawArc(
                        color = scheme.surfaceVariant,
                        startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        topLeft = topLeft, size = Size(r * 2, r * 2),
                        style = Stroke(stroke),
                    )
                    var start = -90f
                    receivables.forEachIndexed { i, s ->
                        val sweep = 360f * (s.value / total) * progress
                        drawArc(
                            color = extras.chart[i],
                            startAngle = start,
                            sweepAngle = (sweep - 2f).coerceAtLeast(0.5f),
                            useCenter = false,
                            topLeft = topLeft, size = Size(r * 2, r * 2),
                            style = Stroke(stroke, cap = StrokeCap.Butt),
                        )
                        start += 360f * (s.value / total)
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "۶۸٪",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = scheme.primary,
                    )
                    Text("وصول‌شده", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                receivables.forEachIndexed { i, s ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(extras.chart[i]))
                        Spacer(Modifier.width(7.dp))
                        Text(s.label, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${s.value.toInt()}٪",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = extras.chart[i],
                        )
                    }
                }
            }
        }
    }
}

// ============================================================ ستون‌های سه‌بعدی
@Composable
private fun Columns3D(items: List<P3>, progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(Modifier.fillMaxWidth().height(180.dp)) {
            val maxV = items.maxOf { it.v } * 1.15f
            val n = items.size
            val gap = 12.dp.toPx()
            val labelSpace = 18.dp.toPx()
            val depth = 7.dp.toPx()
            val w = (size.width - gap * (n - 1)) / n
            items.forEachIndexed { i, p ->
                val hFull = (p.v / maxV) * (size.height - labelSpace - depth) * progress
                val x = i * (w + gap)
                val yTop = size.height - labelSpace - hFull
                // وجه کناری (تیره‌تر)
                val side = Path().apply {
                    moveTo(x + w, yTop)
                    lineTo(x + w + depth, yTop - depth)
                    lineTo(x + w + depth, size.height - labelSpace - depth)
                    lineTo(x + w, size.height - labelSpace)
                    close()
                }
                drawPath(side, color = extras.gold.copy(alpha = 0.20f))
                // وجه بالا (روشن‌تر)
                val top = Path().apply {
                    moveTo(x, yTop)
                    lineTo(x + depth, yTop - depth)
                    lineTo(x + w + depth, yTop - depth)
                    lineTo(x + w, yTop)
                    close()
                }
                drawPath(top, color = extras.gold.copy(alpha = 0.55f))
                // وجه جلو با گرادیان متالیک
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = extras.metallic,
                        startY = yTop,
                        endY = size.height - labelSpace,
                    ),
                    topLeft = Offset(x, yTop),
                    size = Size(w, hFull),
                    cornerRadius = CornerRadius(w / 5f, w / 5f),
                )
            }
        }
        Row(Modifier.fillMaxWidth()) {
            items.forEach {
                Text(
                    it.name.take(6),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

// ============================================================ تب مشتریان
@Composable
private fun CustomersTab(onOpen: (CustomerD) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var q by remember { mutableStateOf("") }
    val list = customersDemo.filter {
        q.isBlank() || it.name.contains(q, true) || it.tag.contains(q, true)
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("پرونده مشتریان", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        // جستجوی زنده — هم‌زبان با تم
        OutlinedTextField(
            value = q,
            onValueChange = { q = it },
            placeholder = { Text("جستجوی مشتری...") },
            singleLine = true,
            leadingIcon = { Icon(MrIcons.Search, contentDescription = null, tint = scheme.primary) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Section("برای مشاهده پرونده ۳۶۰ درجه لمس کنید") {
            if (list.isEmpty()) {
                EmptyBox("مشتری‌ای یافت نشد", subtitle = "نام یا سطح دیگری را جستجو کنید")
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                list.forEach { c -> CustomerCard(c, onOpen) }
            }
        }
    }
}

/** کارت یکدست مشتری — گوی سه‌بعدی هم‌رنگ تم + نام تک‌خطی + سهم + مانده رنگ‌بندی‌شده */
@Composable
private fun CustomerCard(c: CustomerD, onOpen: (CustomerD) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val positive = c.balance.startsWith("+")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(extras.glass)
            .border(1.dp, extras.hairline, RoundedCornerShape(16.dp))
            .clickable { onOpen(c); SoundFx.soft() }
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        // گوی سه‌بعدی مشتری — نور از بالا با رنگ تم
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(scheme.primary.copy(alpha = 0.30f), scheme.primary.copy(alpha = 0.08f))
                    )
                )
                .border(1.dp, scheme.primary.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                c.name.take(1),
                color = scheme.primary,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    c.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    c.tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(scheme.primary.copy(alpha = 0.12f))
                        .border(1.dp, scheme.primary.copy(alpha = 0.30f), RoundedCornerShape(50))
                        .padding(horizontal = 7.dp, vertical = 1.dp),
                )
            }
            Spacer(Modifier.height(5.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(scheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(c.share)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(extras.goldGradient))
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                c.balance,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = if (positive) extras.positive else scheme.error,
            )
            Text("مانده", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProductsTab(onOpen: (P3) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var q by remember { mutableStateOf("") }
    val list = topProducts3D.filter { q.isBlank() || it.name.contains(q, true) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("کالاها و موجودی", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        // جستجوی زنده کالا
        OutlinedTextField(
            value = q,
            onValueChange = { q = it },
            placeholder = { Text("جستجوی کالا...") },
            singleLine = true,
            leadingIcon = { Icon(MrIcons.Search, contentDescription = null, tint = scheme.primary) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Section("کارت کالا — برای پرونده و کاردکس لمس کنید") {
            if (list.isEmpty()) {
                EmptyBox("کالایی یافت نشد", subtitle = "نام دیگری را جستجو کنید")
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                list.forEach { p -> ProductCard(p, onOpen) }
            }
        }
        Section("سهم فروش دسته‌ها") {
            DonutSimple(productShare, stagger(0, true))
        }
        Section("پرفروش‌های هفته") {
            Columns3D(topProducts3D, stagger(200, true))
        }
    }
}

/** کارت یکدست کالا — گوی سه‌بعدی + مشخصات + سهم فروش */
@Composable
private fun ProductCard(p: P3, onOpen: (P3) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(extras.glass)
            .border(1.dp, extras.hairline, RoundedCornerShape(16.dp))
            .clickable { onOpen(p); SoundFx.soft() }
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        // گوی سه‌بعدی کالا
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(scheme.primary.copy(alpha = 0.30f), scheme.primary.copy(alpha = 0.08f))
                    )
                )
                .border(1.dp, scheme.primary.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(MrIcons.Products, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(p.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(
                "گروه: خشکبار · سهم فروش " + p.v.toInt() + "٪",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = scheme.primary,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** ردیف کاردکس — نشان رنگی هماهنگ با نوع تراکنش */
@Composable
private fun CardexRow(icon: ImageVector, date: String, title: String, qty: String, color: Color) {
    val c = statusTone(color)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
            .border(1.dp, LocalThemeExtras.current.hairline, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(listOf(c.copy(alpha = 0.28f), c.copy(alpha = 0.10f)))
                )
                .border(1.dp, c.copy(alpha = 0.50f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = c, modifier = Modifier.size(13.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            qty,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = c,
        )
    }
}

@Composable
private fun DonutSimple(slices: List<Slice>, progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 20.dp.toPx()
                val r = (size.minDimension - stroke) / 2
                val topLeft = Offset((size.width - r * 2) / 2, (size.height - r * 2) / 2)
                var start = -90f
                slices.forEachIndexed { i, s ->
                    val sweep = 360f * (s.value / total) * progress
                    drawArc(
                        color = extras.chart[i % extras.chart.size],
                        startAngle = start,
                        sweepAngle = (sweep - 2f).coerceAtLeast(0.5f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(r * 2, r * 2),
                        style = Stroke(stroke, cap = StrokeCap.Butt),
                    )
                    start += 360f * (s.value / total)
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            slices.forEachIndexed { i, s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(extras.chart[i % extras.chart.size]))
                    Spacer(Modifier.width(7.dp))
                    Text(s.label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(
                        "${s.value.toInt()}٪",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = extras.chart[i % extras.chart.size],
                    )
                }
            }
        }
    }
}

// ============================================================ تب گزارش‌ها (Report Studio)
@Composable
private fun ReportsTab() {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var type by remember { mutableStateOf(0) }

    Section("Report Studio") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("نوع گزارش", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                reportTypes.forEachIndexed { i, t ->
                    ReportChip(t, type == i) { type = i; SoundFx.soft() }
                }
            }
            Text("فیلترها", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                reportFilters.forEach { f ->
                    ReportChip(f, false) { SoundFx.soft() }
                }
            }
            // پیش‌نمایش — هویت گزارش Enterprise
            GlassCard(corner = 16.dp) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "M•R",
                            style = TextStyle(brush = Brush.horizontalGradient(extras.brand), fontSize = 18.sp, fontWeight = FontWeight.Black),
                        )
                        Text(
                            "MEELANO REPORTS",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 2.sp,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                    LightLine(width = 260.dp)
                    Text("گزارش وضعیت مطالبات مشتریان", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("دوره: ۱ تا ۳۱ مرداد ۱۴۰۴", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Executive Summary", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = scheme.primary, modifier = Modifier.align(Alignment.Start))
                        SummaryRow("تعداد مشتریان", "۱٬۲۴۸")
                        SummaryRow("مطالبات کل", "۲۴۸٬۰۰۰٬۰۰۰ تومان")
                        SummaryRow("مشتریان پرریسک", "۷")
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(extras.hairline))
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row {
                            Text("مشتری", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, modifier = Modifier.weight(2f))
                            Text("مانده", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }
                        repeat(4) { i ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(customersDemo[i].name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2f))
                                Text(
                                    customersDemo[i].balance,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (customersDemo[i].balance.startsWith("+")) extras.positive else scheme.error,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End,
                                )
                            }
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(extras.hairline))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("M•R — Meelano Reports", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("Meelano Studio Design · Milad Yaghoobi", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    }
                }
            }
            // خروجی‌ها
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                GlassAction(MrIcons.Pdf, "PDF", Color(0xFFFF6B6B)) { SoundFx.success() }
                GlassAction(MrIcons.Excel, "Excel", Color(0xFF69DB7C)) { SoundFx.success() }
                GlassAction(MrIcons.Word, "Word", Color(0xFF74C0FC)) { SoundFx.success() }
                GlassAction(MrIcons.Print, "Print", scheme.primary) { SoundFx.soft() }
                GlassAction(MrIcons.Share, "Share", extras.accent) { SoundFx.soft() }
            }
            Row(
                Modifier.fillMaxWidth().clickable { SoundFx.soft() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(MrIcons.Bookmark, contentDescription = null, tint = extras.gold, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "ذخیره به‌عنوان الگو — «گزارش ماهانه مشتریان»",
                    style = MaterialTheme.typography.labelMedium,
                    color = extras.gold,
                )
            }
        }
    }
}

/** چیپ گزارش — گوی کوچک سه‌بعدی هم‌زبان با تم (جایگزین دکمه بیضی ساده) */
@Composable
private fun ReportChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val c = scheme.primary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) Brush.verticalGradient(listOf(c.copy(alpha = 0.24f), c.copy(alpha = 0.08f)))
                else SolidColor(extras.glass)
            )
            .border(1.dp, if (selected) c.copy(alpha = 0.5f) else extras.hairline, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (selected) c else scheme.onSurfaceVariant.copy(alpha = 0.5f))
        )
        Spacer(Modifier.width(7.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) c else scheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

/** ردیف «برچسب …… مقدار» در Executive Summary */
@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

// ============================================================ تب هشدارها (مرکز توجه)
@Composable
private fun AlertsTab(vm: AppViewModel) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // نشان سه‌بعدی مرکز توجه — با هاله نور
            Box(
                Modifier
                    .size(36.dp)
                    .background(
                        Brush.radialGradient(listOf(Color(0xFFFF6B6B).copy(alpha = 0.30f), Color.Transparent)),
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
                                listOf(Color(0xFFFF6B6B).copy(alpha = 0.32f), Color(0xFFFF6B6B).copy(alpha = 0.10f))
                            )
                        )
                        .border(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(MrIcons.Alerts, contentDescription = null, tint = statusTone(Color(0xFFFF6B6B)), modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "مرکز توجه",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            )
        }
        Text(
            "۳ مورد نیازمند توجه شماست",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
        attentionCards.forEach { a ->
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // نشان سه‌بعدی هماهنگ با نوع هشدار + نبض آرام
                    val tone = statusTone(a.color)
                    Box(
                        Modifier
                            .size(42.dp)
                            .background(
                                Brush.radialGradient(listOf(a.color.copy(alpha = 0.28f), Color.Transparent)),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            Modifier
                                .size(31.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(a.color.copy(alpha = 0.30f), a.color.copy(alpha = 0.10f))
                                    )
                                )
                                .border(1.dp, a.color.copy(alpha = 0.55f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                when (a.title) {
                                    "چک سررسید" -> MrIcons.Checks
                                    "مطالبات" -> MrIcons.Receivables
                                    else -> MrIcons.Customers
                                },
                                contentDescription = null,
                                tint = tone,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                        if (vm.experience.motion) {
                            CalmPulse(a.color, dotSize = 6.dp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(a.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(a.desc, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    }
                    Text(
                        a.count,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = tone,
                    )
                }
            }
        }
    }
}

// ============================================================ تب اعلان‌ها (Inbox)
@Composable
private fun NotificationsTab(vm: AppViewModel, onOpenAlerts: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var filter by remember { mutableStateOf("همه") }
    val cats = listOf("همه", "مهم", "مالی", "چک", "مشتری")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // نشان سه‌بعدی صندوق اعلان
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
                    Icon(MrIcons.Notifications, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "اعلان‌ها",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            )
        }
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            cats.forEach { c ->
                MTab(c, filter == c) { filter = c; SoundFx.soft() }
            }
        }
        val list = notifs.filter { filter == "همه" || it.cat == filter }
        if (list.isEmpty()) {
            ir.atiran.hamrah.viewer.ui.components.EmptyBox(
                text = "همه‌چیز مرتب است",
                subtitle = "در این دسته اعلان جدیدی وجود ندارد",
            )
        }
        list.forEach { n ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(extras.glass)
                    .border(1.dp, extras.hairline, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 11.dp),
            ) {
                // نشان سه‌بعدی هماهنگ با دسته اعلان
                val tone = statusTone(n.color)
                Box(
                    Modifier
                        .size(34.dp)
                        .background(
                            Brush.radialGradient(listOf(n.color.copy(alpha = 0.26f), Color.Transparent)),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(25.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(n.color.copy(alpha = 0.28f), n.color.copy(alpha = 0.10f))
                                )
                            )
                            .border(1.dp, n.color.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            when (n.cat) {
                                "چک" -> MrIcons.Checks
                                "مالی" -> MrIcons.Receivables
                                "مهم" -> MrIcons.Alerts
                                else -> MrIcons.Customers
                            },
                            contentDescription = null,
                            tint = tone,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(n.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(n.sub, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(n.time, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "مشاهده",
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(scheme.primary.copy(alpha = 0.10f))
                            .clickable { SoundFx.soft(); onOpenAlerts() }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

// ============================================================ پالت فرمان (Universal Search)
private data class SearchItem(
    val title: String, val hint: String, val group: String,
    val icon: ImageVector, val action: () -> Unit,
)

/**
 * Command Center — جستجوی همزمان در صفحه‌ها، مشتریان، کالاها،
 * هشدارها و گزارش‌ها؛ انتخاب مستقیم به مقصد می‌رود.
 */
@Composable
private fun CommandPalette(
    onDismiss: () -> Unit,
    onOpenCustomer: (CustomerD) -> Unit,
    onOpenProduct: (P3) -> Unit,
    onGoTab: (Int) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var q by remember { mutableStateOf("") }

    val items = listOf(
        // صفحه‌ها
        SearchItem("نمای کلی", "داشبورد اصلی", "صفحه‌ها", MrIcons.Overview) { onGoTab(0) },
        SearchItem("مشتریان", "فهرست و پرونده دیجیتال", "صفحه‌ها", MrIcons.Customers) { onGoTab(1) },
        SearchItem("کالاها", "موجودی و کارت کالا", "صفحه‌ها", MrIcons.Products) { onGoTab(2) },
        SearchItem("گزارش‌ها", "Report Studio", "صفحه‌ها", MrIcons.Reports) { onGoTab(3) },
        SearchItem("هشدارها", "مرکز توجه", "صفحه‌ها", MrIcons.Alerts) { onGoTab(4) },
        SearchItem("اعلان‌ها", "صندوق ورودی", "صفحه‌ها", MrIcons.Notifications) { onGoTab(5) },
        // مشتریان
        *customersDemo.map {
            SearchItem(it.name, "پرونده ۳۶۰ مشتری — مانده " + it.balance, "مشتریان", MrIcons.Customers) { onOpenCustomer(it) }
        }.toTypedArray(),
        // کالاها
        *topProducts3D.map {
            SearchItem(it.name, "کارت کالا — سهم فروش " + it.v.toInt() + "٪", "کالاها", MrIcons.Products) { onOpenProduct(it) }
        }.toTypedArray(),
        // هشدارها و گزارش‌ها
        SearchItem("چک‌های سررسید شده", "مرکز توجه — چک‌های سررسیدشده", "هشدارها و گزارش‌ها", MrIcons.Alerts) { onGoTab(4) },
        SearchItem("مطالبات مشتریان", "گزارش وضعیت مطالبات", "هشدارها و گزارش‌ها", MrIcons.Reports) { onGoTab(3) },
        SearchItem("گزارش موجودی انبار", "Report Studio", "هشدارها و گزارش‌ها", MrIcons.Reports) { onGoTab(3) },
        SearchItem("گردش حساب", "گزارش گردش مالی", "هشدارها و گزارش‌ها", MrIcons.Trend) { onGoTab(3) },
    )
    val filtered = items.filter {
        q.isBlank() || it.title.contains(q, true) || it.hint.contains(q, true) || it.group.contains(q, true)
    }
    val groups = listOf("صفحه‌ها", "مشتریان", "کالاها", "هشدارها و گزارش‌ها")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 90.dp, start = 20.dp, end = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(scheme.surface.copy(alpha = 0.97f))
                    .border(1.dp, extras.hairline, RoundedCornerShape(24.dp))
                    .clickable(enabled = false) {},
            ) {
                OutlinedTextField(
                    value = q,
                    onValueChange = { q = it },
                    placeholder = { Text("چه کاری می‌خواهید انجام دهید؟ (نام مشتری، کالا، چک‌های سررسید...)") },
                    singleLine = true,
                    leadingIcon = { Icon(MrIcons.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                )
                Column(
                    Modifier
                        .padding(bottom = 10.dp)
                        .verticalScroll(rememberScrollState())
                        .heightIn(max = 340.dp),
                ) {
                    groups.forEach { g ->
                        val gItems = filtered.filter { it.group == g }
                        if (gItems.isNotEmpty()) {
                            Text(
                                g,
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                            gItems.forEach { c ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { c.action(); SoundFx.soft() }
                                        .padding(horizontal = 16.dp, vertical = 9.dp),
                                ) {
                                    Icon(
                                        c.icon,
                                        contentDescription = null,
                                        tint = scheme.primary,
                                        modifier = Modifier.size(17.dp),
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(c.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        Text(c.hint, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                    if (filtered.isEmpty()) {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("موردی یافت نشد", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("عبارت دیگری را جستجو کنید", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================ پرونده کالا ۳۶۰
@Composable
private fun Product360(p: P3, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val idx = topProducts3D.indexOf(p).coerceAtLeast(0)
    val price: Long = 850_000L + idx * 120_000L
    val totalStock = (p.v * 12).toInt()
    var cardexOpen by remember { mutableStateOf(false) }
    var range by remember { mutableStateOf("ماه") }
    val cardexData = mapOf(
        "هفته" to listOf(
            CardexEntry("۰۷ شهریور", "گردش به انبار شعبه", "۶۰ کیلوگرم", "گردش", extras.accent),
            CardexEntry("۰۵ شهریور", "فروش — پخش نگین", "−۱۲۰ کیلوگرم", "فروش", scheme.error),
            CardexEntry("۰۳ شهریور", "فروش — هایپر طلایی", "−۸۵ کیلوگرم", "فروش", scheme.error),
            CardexEntry("۰۱ شهریور", "ورود به انبار مرکزی", "+۲۴۰ کیلوگرم", "ورود", extras.positive),
        ),
        "ماه" to listOf(
            CardexEntry("۰۷ شهریور", "گردش به انبار شعبه", "۶۰ کیلوگرم", "گردش", extras.accent),
            CardexEntry("۰۵ شهریور", "فروش — پخش نگین", "−۱۲۰ کیلوگرم", "فروش", scheme.error),
            CardexEntry("۰۳ شهریور", "فروش — هایپر طلایی", "−۸۵ کیلوگرم", "فروش", scheme.error),
            CardexEntry("۰۱ شهریور", "ورود به انبار مرکزی", "+۲۴۰ کیلوگرم", "ورود", extras.positive),
            CardexEntry("۲۸ مرداد", "فروش — مارکت الماس", "−۹۵ کیلوگرم", "فروش", scheme.error),
            CardexEntry("۲۴ مرداد", "خرید از باغداران", "+۳۱۰ کیلوگرم", "ورود", extras.positive),
            CardexEntry("۲۰ مرداد", "فروش — هایپر طلایی", "−۱۴۰ کیلوگرم", "فروش", scheme.error),
            CardexEntry("۱۵ مرداد", "مرجوعی کیفیت", "−۲۵ کیلوگرم", "مرجوعی", scheme.error),
        ),
        "سال" to listOf(
            CardexEntry("۰۷ شهریور", "گردش به انبار شعبه", "۶۰ کیلوگرم", "گردش", extras.accent),
            CardexEntry("۰۱ شهریور", "ورود به انبار مرکزی", "+۲۴۰ کیلوگرم", "ورود", extras.positive),
            CardexEntry("۲۴ مرداد", "خرید از باغداران", "+۳۱۰ کیلوگرم", "ورود", extras.positive),
            CardexEntry("۱۰ مرداد", "فروش عمده — پخش نگین", "−۴۸۰ کیلوگرم", "فروش", scheme.error),
            CardexEntry("۰۲ مرداد", "ورود فصل برداشت", "+۸۵۰ کیلوگرم", "ورود", extras.positive),
            CardexEntry("۱۸ تیر", "فروش — هایپر طلایی", "−۲۶۰ کیلوگرم", "فروش", scheme.error),
            CardexEntry("۰۵ تیر", "فروش — مارکت الماس", "−۱۹۰ کیلوگرم", "فروش", scheme.error),
            CardexEntry("۲۲ خرداد", "گردش به فروشگاه", "۴۵ کیلوگرم", "گردش", extras.accent),
            CardexEntry("۱۲ خرداد", "خرید از باغداران", "+۴۲۰ کیلوگرم", "ورود", extras.positive),
            CardexEntry("۳۰ اردیبهشت", "فروش عمده — هایپر طلایی", "−۵۳۰ کیلوگرم", "فروش", scheme.error),
            CardexEntry("۱۵ اردیبهشت", "فروش — پخش نگین", "−۱۷۵ کیلوگرم", "فروش", scheme.error),
            CardexEntry("۰۲ فروردین", "موجودی اول دوره", "۵۲۰ کیلوگرم", "افتتاح", extras.accent),
        ),
    )
    val entries = cardexData[range] ?: emptyList()
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.surface.copy(alpha = 0.97f))
                .border(1.dp, extras.hairline, RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            // سربرگ + دکمه بستن
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(p.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text("کد کالا: ۱۰۲۳" + (idx + 4) + " — گروه: خشکبار", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                }
                MrPillButton(label = "بستن", onClick = { onDismiss() }, icon = MrIcons.Close)
            }
            NumberHero(formatInt(totalStock.toLong()), unit = "موجودی کل (کیلوگرم)", color = scheme.primary, fontSize = 30.sp)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("موجودی هر انبار", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = scheme.primary)
                listOf(
                    "انبار مرکزی" to (totalStock * 0.5f).toInt(),
                    "انبار شعبه" to (totalStock * 0.3f).toInt(),
                    "فروشگاه حضوری" to (totalStock * 0.2f).toInt(),
                ).forEach { (name, qty) ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(formatInt(qty.toLong()), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("اطلاعات مرتبط", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = scheme.primary)
                listOf(
                    "قیمت فروش" to (formatInt(price) + " تومان"),
                    "آخرین فروش" to "۲ شهریور ۱۴۰۴",
                    "میانگین فروش ماهانه" to (formatInt((totalStock / 3).toLong()) + " کیلوگرم"),
                ).forEach { (k, v) ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(k, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text(v, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
            // دکمه کاردکس — کاربر خودش تصمیم می‌گیرد
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (cardexOpen) Brush.verticalGradient(
                            listOf(scheme.primary.copy(alpha = 0.20f), scheme.primary.copy(alpha = 0.06f))
                        ) else SolidColor(extras.glass)
                    )
                    .border(
                        1.dp,
                        if (cardexOpen) scheme.primary.copy(alpha = 0.5f) else extras.hairline,
                        RoundedCornerShape(14.dp),
                    )
                    .clickable { cardexOpen = !cardexOpen; SoundFx.soft() }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(scheme.primary.copy(alpha = 0.30f), scheme.primary.copy(alpha = 0.08f))
                            )
                        )
                        .border(1.dp, scheme.primary.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(MrIcons.Excel, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(15.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        "کاردکس و گردش کالا",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (cardexOpen) scheme.primary else scheme.onSurface,
                    )
                    Text(
                        if (cardexOpen) "برای بستن دوباره لمس کنید" else "گردش ورود، فروش و انبار — لمس کنید",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
                Icon(
                    if (cardexOpen) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            // کاردکس تاشو — فیلتر زمانی + اسکرول
            if (cardexOpen) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("هفته", "ماه", "سال").forEach { r ->
                            val sel = range == r
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (sel) Brush.verticalGradient(
                                            listOf(scheme.primary.copy(alpha = 0.24f), scheme.primary.copy(alpha = 0.08f))
                                        ) else SolidColor(extras.glass)
                                    )
                                    .border(
                                        1.dp,
                                        if (sel) scheme.primary.copy(alpha = 0.5f) else extras.hairline,
                                        RoundedCornerShape(12.dp),
                                    )
                                    .clickable { range = r; SoundFx.soft() }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(MrIcons.Filter, contentDescription = null, tint = if (sel) scheme.primary else scheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        r,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (sel) scheme.primary else scheme.onSurfaceVariant,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                    Column(
                        Modifier
                            .heightIn(max = 260.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        entries.forEach { e ->
                            CardexRow(e.icon(), e.date, e.title, e.qty, e.color)
                        }
                        Text(
                            "روند گردش " + (if (range == "سال") "سالانه" else if (range == "ماه") "ماهانه" else "هفتگی"),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                        )
                        MicroArea(listOf(120f, 95f, 140f, 110f, 160f, 130f, 175f, 150f), scheme.primary)
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                GlassAction(MrIcons.Pdf, "PDF", Color(0xFFFF6B6B)) { SoundFx.success() }
                GlassAction(MrIcons.Excel, "Excel", Color(0xFF69DB7C)) { SoundFx.success() }
                GlassAction(MrIcons.Share, "Share", extras.accent) { SoundFx.soft() }
            }
        }
    }
}

/** رکورد کاردکس با آیکون و رنگ مرتبط */
private data class CardexEntry(
    val date: String, val title: String, val qty: String,
    val type: String, val color: Color,
)

private fun CardexEntry.icon(): ImageVector = when (type) {
    "ورود" -> MrIcons.Sync
    "فروش" -> MrIcons.Excel
    "مرجوعی" -> MrIcons.Alerts
    else -> MrIcons.Trend
}

// ============================================================ پرونده مشتری ۳۶۰
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Customer360(c: CustomerD, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    // Master → Detail Drawer: پنل کناری با انیمیشن ۲۸۰ms — نه پنجره تمام‌صفحه
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    Box(Modifier.fillMaxSize()) {
        // پرده تیره پشت پنل
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(onClick = onDismiss),
        )
        AnimatedVisibility(
            visible = shown,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(280)),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Column(
                Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight()
                    .background(scheme.background),
            ) {
                TopAppBar(
                    title = { Text("پرونده دیجیتال مشتری", fontWeight = FontWeight.Bold) },
                    actions = {
                        MrPillButton(label = "بستن", onClick = { onDismiss() }, icon = MrIcons.Close)
                        Spacer(Modifier.width(10.dp))
                    },
                )
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Spacer(Modifier.height(4.dp))
                    // سربرگ پرونده
                    GlassCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(scheme.primary.copy(alpha = 0.30f), scheme.primary.copy(alpha = 0.08f))
                                            )
                                        )
                                        .border(1.dp, scheme.primary.copy(alpha = 0.45f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        c.name.take(1),
                                        color = scheme.primary,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(c.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                        Spacer(Modifier.width(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(extras.positive.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp),
                                        ) {
                                            Box(Modifier.size(7.dp).clip(CircleShape).background(extras.positive))
                                            Spacer(Modifier.width(5.dp))
                                            Text("فعال", style = MaterialTheme.typography.labelSmall, color = extras.positive)
                                        }
                                    }
                                    Text(
                                        "سطح ${c.tag} • مشتری از ۱۴۰۱",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = scheme.onSurfaceVariant,
                                    )
                                }
                            }
                            // باکس مانده حساب — هم‌شکل با کارت‌های لیست مشتریان
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(extras.glass)
                                    .border(1.dp, extras.hairline, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                NumberHero(
                                    c.balance,
                                    unit = "مانده حساب (تومان)",
                                    color = if (c.balance.startsWith("+")) extras.positive else scheme.error,
                                    fontSize = 28.sp,
                                )
                            }
                        }
                    }
                    // فعالیت حساب — با فیلتر بازه زمانی
                    var range by remember { mutableStateOf("ماه") }
                    val entries = activityByRange[range] ?: emptyList()
                    Section("گردش حساب مشتری") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // دکمه فیلتر سه‌بعدی — هم‌زبان با تم
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                listOf("هفته", "ماه", "سال").forEach { r ->
                                    val sel = range == r
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (sel) Brush.verticalGradient(
                                                    listOf(scheme.primary.copy(alpha = 0.24f), scheme.primary.copy(alpha = 0.08f))
                                                ) else SolidColor(extras.glass)
                                            )
                                            .border(
                                                1.dp,
                                                if (sel) scheme.primary.copy(alpha = 0.5f) else extras.hairline,
                                                RoundedCornerShape(12.dp),
                                            )
                                            .clickable { range = r; SoundFx.soft() }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                MrIcons.Filter,
                                                contentDescription = null,
                                                tint = if (sel) scheme.primary else scheme.onSurfaceVariant,
                                                modifier = Modifier.size(13.dp),
                                            )
                                            Spacer(Modifier.width(5.dp))
                                            Text(
                                                r,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (sel) scheme.primary else scheme.onSurfaceVariant,
                                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            entries.forEachIndexed { i, e ->
                                Row(Modifier.height(IntrinsicSize.Min)) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(26.dp),
                                    ) {
                                        Box(
                                            Modifier
                                                .size(11.dp)
                                                .clip(CircleShape)
                                                .background(if (e.positive) extras.positive else scheme.error)
                                        )
                                        if (i < entries.size - 1) {
                                            Box(
                                                Modifier
                                                    .width(1.dp)
                                                    .weight(1f)
                                                    .background(extras.hairline)
                                            )
                                        }
                                    }
                                    Column(
                                        Modifier
                                            .weight(1f)
                                            .padding(start = 10.dp, bottom = 18.dp),
                                    ) {
                                        Text(
                                            e.when_,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = scheme.onSurfaceVariant,
                                        )
                                        Row {
                                            Column(Modifier.weight(1f)) {
                                                Text(e.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                            }
                                            Text(
                                                "${e.amount} تومان",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (e.positive) extras.positive else scheme.error,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // اکشن‌ها
                    Section("اقدامات") {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // ردیف اول — ارتباط
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                GlassAction(MrIcons.Call, "تماس", scheme.primary) { SoundFx.soft() }
                                GlassAction(MrIcons.Message, "پیام", extras.accent) { SoundFx.soft() }
                                GlassAction(MrIcons.Reminder, "یادآور", Color(0xFFFFA94D)) { SoundFx.soft() }
                                GlassAction(MrIcons.Share, "اشتراک", extras.gold) { SoundFx.soft() }
                            }
                            // ردیف دوم — خروجی‌ها
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                GlassAction(MrIcons.Pdf, "PDF", Color(0xFFFF6B6B)) { SoundFx.success() }
                                GlassAction(MrIcons.Excel, "Excel", Color(0xFF69DB7C)) { SoundFx.success() }
                                GlassAction(MrIcons.Print, "چاپ", scheme.primary) { SoundFx.soft() }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

// ============================================================ اجزای مشترک
@Composable
private fun stagger(delayMs: Int, motion: Boolean): Float {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!motion) {
            started = true
        } else {
            delay(delayMs.toLong())
            started = true
        }
    }
    return animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = if (motion) 900 else 0),
        label = "stagger$delayMs",
    ).value
}

private fun formatInt(v: Long): String {
    val sb = StringBuilder()
    val s = v.toString()
    var cnt = 0
    for (i in s.length - 1 downTo 0) {
        sb.append(s[i]); cnt++
        if (cnt % 3 == 0 && i > 0) sb.append(',')
    }
    return sb.reverse().toString()
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // نقطه امضای برند قبل از عنوان بخش
                Box(
                    Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(scheme.primary)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                )
            }
            content()
        }
    }
}
