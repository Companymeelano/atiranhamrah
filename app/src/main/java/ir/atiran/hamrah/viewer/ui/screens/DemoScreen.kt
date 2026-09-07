package ir.atiran.hamrah.viewer.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
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
import ir.atiran.hamrah.viewer.ui.components.AmbientBackground
import ir.atiran.hamrah.viewer.ui.components.CalmPulse
import ir.atiran.hamrah.viewer.ui.components.GlassAction
import ir.atiran.hamrah.viewer.ui.components.GlassCard
import ir.atiran.hamrah.viewer.ui.components.LightLine
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import ir.atiran.hamrah.viewer.utils.SoundFx
import kotlinx.coroutines.delay
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
    val title: String, val value: String, val desc: String,
    val icon: ImageVector, val spark: List<Int>,
)
private val heroes = listOf(
    Hero("مشتریان", "۲٬۱۲۰", "مشتری فعال در این دوره", Icons.Filled.Group, listOf(40, 48, 52, 58, 55, 64, 72)),
    Hero("کالاها", "۳۴۸", "قلم کالای فعال", Icons.Filled.Inventory2, listOf(60, 58, 62, 65, 63, 68, 70)),
    Hero("گردش مالی", "۸۶۴م", "گردش حساب این ماه (تومان)", Icons.Filled.Paid, listOf(35, 45, 42, 58, 64, 72, 88)),
    Hero("هشدارها", "۱۵", "مورد نیازمند توجه", Icons.Filled.Warning, listOf(20, 35, 28, 42, 38, 30, 25)),
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
private val activity = listOf(
    TimelineEntry("امروز", "فاکتور 10325", "+ 18,500,000", true),
    TimelineEntry("۲ روز قبل", "دریافت", "− 10,000,000", false),
    TimelineEntry("۵ روز قبل", "فاکتور 10302", "+ 7,800,000", true),
    TimelineEntry("هفته قبل", "چک وصول شد", "− 22,000,000", false),
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

    AmbientBackground(enabled = vm.experience.ambient) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        "M•REPORT",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { vm.closeDemo() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = { paletteOpen = true; SoundFx.soft() }) {
                        Icon(Icons.Filled.Search, contentDescription = "جستجوی هوشمند")
                    }
                },
            )

            // تب‌های خلوت
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tabs.forEachIndexed { i, t ->
                    MTab(t, tab == i) {
                        tab = i
                        SoundFx.soft()
                    }
                }
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
                    0 -> OverviewTab(vm)
                    1 -> CustomersTab { customerOpen = it }
                    2 -> ProductsTab()
                    3 -> ReportsTab()
                    4 -> AlertsTab(vm)
                    else -> NotificationsTab(vm)
                }
                Text(
                    "★ داده‌های این بخش صرفاً برای نمایش قابلیت‌های M•REPORT است",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
            }
        }
    }

    if (paletteOpen) {
        CommandPalette(
            onDismiss = { paletteOpen = false },
            onSelect = { cmd ->
                paletteOpen = false
                when (cmd.action) {
                    "customer" -> customerOpen = customersDemo.firstOrNull { it.name == cmd.target }
                    "alerts" -> tab = 4
                    "reports" -> tab = 3
                    "overview" -> tab = 0
                }
            },
        )
    }

    customerOpen?.let { c ->
        Customer360(c, onDismiss = { customerOpen = null })
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
private fun OverviewTab(vm: AppViewModel) {
    val motion = vm.experience.motion
    val extras = LocalThemeExtras.current
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

    // ---------- M•R Pulse: امضای بصری ----------
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Business Pulse",
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                PulseItem("مشتریان", Color(0xFF69DB7C))
                PulseItem("مطالبات", Color(0xFFFFA94D))
                PulseItem("چک‌ها", Color(0xFFFF6B6B))
                PulseItem("موجودی", Color(0xFF69DB7C))
                PulseItem("سیستم", extras.accent)
            }
        }
    }

    // ---------- چهار کارت اصلی ----------
    heroes.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            row.forEach { h ->
                HeroCard(h, motion, Modifier.weight(1f))
            }
        }
    }

    // ---------- چارت اصلی: گردش حساب ----------
    Section("گردش حساب — ۱۲ نقطه اخیر") {
        GlowAreaChart(
            values = flowValues,
            dates = flowDates,
            progress = g2,
            motion = motion,
        )
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
        Text(
            "Intelligent Reporting Experience",
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 3.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        LightLine(width = 200.dp)
    }
}

// ============================================================ آیتم نبض
@Composable
private fun PulseItem(label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CalmPulse(color, dotSize = 8.dp, enabled = false)
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ============================================================ کارت اصلی
@Composable
private fun HeroCard(h: Hero, motion: Boolean, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    GlassCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(extras.goldGradient)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(h.icon, contentDescription = null, tint = extras.goldOn, modifier = Modifier.size(17.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(h.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Text(
                h.value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = scheme.primary,
            )
            Text(h.desc, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            MicroArea(h.spark.map { it.toFloat() }, scheme.primary)
        }
    }
}

@Composable
private fun MicroArea(values: List<Float>, color: Color) {
    Canvas(Modifier.fillMaxWidth().height(30.dp)) {
        val maxV = values.max()
        val step = size.width / (values.size - 1)
        val pts = values.mapIndexed { i, v ->
            Offset(i * step, size.height - (v / maxV) * size.height * 0.85f)
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
    Section("پرونده مشتریان — برای مشاهده ۳۶۰ درجه لمس کنید") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            customersDemo.forEach { c ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(extras.glass)
                        .border(1.dp, extras.hairline, RoundedCornerShape(14.dp))
                        .clickable { onOpen(c); SoundFx.soft() }
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                ) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(extras.goldGradient)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            c.name.take(1),
                            color = extras.goldOn,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(c.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                c.tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = extras.accent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(extras.accent.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                        Spacer(Modifier.height(5.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(scheme.surfaceVariant)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(c.share)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Brush.horizontalGradient(extras.goldGradient))
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            c.balance,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (c.balance.startsWith("+")) extras.positive else scheme.error,
                        )
                        Text("مانده حساب", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ============================================================ تب کالاها
@Composable
private fun ProductsTab() {
    val g1 = stagger(0, true)
    Section("سهم فروش دسته‌ها") {
        DonutSimple(productShare, g1)
    }
    Section("پرفروش‌های ماه") {
        Columns3D(topProducts3D, stagger(200, true))
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                reportTypes.forEachIndexed { i, t ->
                    MTab(t, type == i) { type = i; SoundFx.soft() }
                }
            }
            Text("فیلترها", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                reportFilters.forEach { f ->
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(extras.glass)
                            .border(1.dp, extras.hairline, RoundedCornerShape(50))
                            .clickable { SoundFx.soft() }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                    ) {
                        Text(f, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                    }
                }
            }
            // پیش‌نمایش
            GlassCard(corner = 14.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
            }
            // خروجی‌ها
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                GlassAction(Icons.Filled.PictureAsPdf, "PDF", Color(0xFFFF6B6B)) { SoundFx.success() }
                GlassAction(Icons.Filled.TableChart, "Excel", Color(0xFF69DB7C)) { SoundFx.success() }
                GlassAction(Icons.Filled.InsertDriveFile, "Word", Color(0xFF74C0FC)) { SoundFx.success() }
                GlassAction(Icons.Filled.Print, "Print", scheme.primary) { SoundFx.soft() }
                GlassAction(Icons.Filled.Share, "Share", extras.accent) { SoundFx.soft() }
            }
            Row(
                Modifier.fillMaxWidth().clickable { SoundFx.soft() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = extras.gold, modifier = Modifier.size(15.dp))
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

// ============================================================ تب هشدارها (Attention Center)
@Composable
private fun AlertsTab(vm: AppViewModel) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CalmPulse(Color(0xFFFF6B6B), enabled = vm.experience.motion)
            Spacer(Modifier.width(8.dp))
            Text(
                "Attention Center",
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
                    CalmPulse(a.color, enabled = vm.experience.motion)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(a.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(a.desc, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            a.count,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = a.color,
                        )
                    }
                }
            }
        }
    }
}

// ============================================================ تب اعلان‌ها (Inbox)
@Composable
private fun NotificationsTab(vm: AppViewModel) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var filter by remember { mutableStateOf("همه") }
    val cats = listOf("همه", "مهم", "مالی", "چک", "مشتری")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Notifications",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
        )
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(extras.glass)
                    .border(1.dp, extras.hairline, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 11.dp),
            ) {
                CalmPulse(n.color, dotSize = 8.dp, enabled = false)
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
                            .clickable { SoundFx.soft() }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

// ============================================================ پالت فرمان
private data class Command(val title: String, val hint: String, val action: String, val target: String = "")

@Composable
private fun CommandPalette(onDismiss: () -> Unit, onSelect: (Command) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var q by remember { mutableStateOf("") }
    val commands = remember {
        listOf(
            Command("هایپر طلایی", "Customer — ورود به پرونده", "customer", "هایپر طلایی"),
            Command("فروشگاه زرین", "Customer — ورود به پرونده", "customer", "فروشگاه زرین"),
            Command("چک‌های سررسید", "گزارش — مرکز توجه", "alerts"),
            Command("گزارش ماهانه مشتریان", "Report Studio", "reports"),
            Command("نمای کلی", "Overview — داشبورد اصلی", "overview"),
        )
    }
    val filtered = commands.filter {
        q.isBlank() || it.title.contains(q, true) || it.hint.contains(q, true)
    }
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
                    .clip(RoundedCornerShape(20.dp))
                    .background(scheme.surface.copy(alpha = 0.97f))
                    .border(1.dp, extras.hairline, RoundedCornerShape(20.dp))
                    .clickable(enabled = false) {},
            ) {
                OutlinedTextField(
                    value = q,
                    onValueChange = { q = it },
                    placeholder = { Text("جستجو کنید... (نام مشتری، چک‌های سررسید، گزارش)") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                )
                Column(Modifier.padding(bottom = 10.dp)) {
                    filtered.forEach { c ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(c); SoundFx.soft() }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(c.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(c.hint, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                            }
                            Icon(
                                Icons.Filled.TrendingUp,
                                contentDescription = null,
                                tint = extras.accent,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }
                    if (filtered.isEmpty()) {
                        Text(
                            "نتیجه‌ای یافت نشد",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }
}

// ============================================================ پرونده مشتری ۳۶۰
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Customer360(c: CustomerD, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        AmbientBackground(enabled = false) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(scheme.background.copy(alpha = 0.97f)),
            ) {
                TopAppBar(
                    title = { Text("پرونده دیجیتال مشتری", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
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
                                        .background(Brush.linearGradient(extras.goldGradient)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        c.name.take(1),
                                        color = extras.goldOn,
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
                            Column {
                                Text("مانده حساب", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                                Text(
                                    c.balance + " تومان",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (c.balance.startsWith("+")) extras.positive else scheme.error,
                                )
                            }
                        }
                    }
                    // فعالیت حساب
                    Section("Account Activity") {
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            activity.forEachIndexed { i, e ->
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
                                        if (i < activity.size - 1) {
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
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            GlassAction(Icons.Filled.Call, "تماس", scheme.primary) { SoundFx.soft() }
                            GlassAction(Icons.Filled.Email, "پیام", extras.accent) { SoundFx.soft() }
                            GlassAction(Icons.Filled.Print, "چاپ", scheme.onSurfaceVariant) { SoundFx.soft() }
                            GlassAction(Icons.Filled.PictureAsPdf, "PDF", Color(0xFFFF6B6B)) { SoundFx.success() }
                            GlassAction(Icons.Filled.TableChart, "Excel", Color(0xFF69DB7C)) { SoundFx.success() }
                            GlassAction(Icons.Filled.Share, "اشتراک", extras.gold) { SoundFx.soft() }
                            GlassAction(Icons.Filled.Alarm, "یادآور", Color(0xFFFFA94D)) { SoundFx.soft() }
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
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
            )
            content()
        }
    }
}
