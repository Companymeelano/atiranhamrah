package ir.atiran.hamrah.viewer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import kotlinx.coroutines.delay

// ============================================================ داده نمونه
private val weekDays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
private val weekSales = listOf(42f, 58f, 51f, 73f, 66f, 88f, 95f)

private data class Slice(val label: String, val value: Float)
private val productShare = listOf(
    Slice("پسته", 38f), Slice("بادام", 24f), Slice("گردو", 18f),
    Slice("فندق", 12f), Slice("کشمش و خرما", 8f),
)

private val months = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور")
private val monthlySales = listOf(180f, 210f, 195f, 260f, 240f, 310f)

private val radarAxes = listOf("کیفیت", "سرعت", "قیمت", "تنوع", "بسته‌بندی", "پشتیبانی")
private val radarValues = listOf(0.92f, 0.78f, 0.85f, 0.70f, 0.88f, 0.81f)

/** نقشه حرارتی: ۱۲ هفته × ۷ روز (۰ تا ۱) */
private val heat = listOf(
    listOf(.2f, .4f, .3f, .6f, .5f, .8f, .9f), listOf(.3f, .5f, .4f, .7f, .6f, .9f, 1f),
    listOf(.1f, .3f, .5f, .4f, .7f, .6f, .8f), listOf(.4f, .6f, .7f, .5f, .8f, 1f, .9f),
    listOf(.3f, .4f, .6f, .8f, .7f, .9f, 1f), listOf(.5f, .7f, .6f, .9f, .8f, 1f, .9f),
    listOf(.2f, .5f, .4f, .6f, .9f, .7f, 1f), listOf(.6f, .8f, .7f, 1f, .9f, .8f, 1f),
    listOf(.4f, .6f, .8f, .7f, 1f, .9f, 1f), listOf(.7f, .9f, .8f, 1f, .8f, 1f, .9f),
    listOf(.5f, .8f, .9f, .7f, 1f, .9f, 1f), listOf(.8f, 1f, .9f, 1f, .9f, 1f, 1f),
)

private data class Quarter(val name: String, val target: Float, val actual: Float)
private val quarters = listOf(
    Quarter("بهار", 600f, 585f), Quarter("تابستان", 650f, 620f),
    Quarter("پاییز", 700f, 735f), Quarter("زمستان", 800f, 780f),
)

private data class Cust(val name: String, val amount: Float, val share: Float)
private val topCustomers = listOf(
    Cust("هایپر طلایی", 312f, 1f), Cust("فروشگاه زرین", 268f, 0.86f),
    Cust("پخش نگین", 204f, 0.65f), Cust("مارکت الماس", 176f, 0.56f),
    Cust("سوپرمارکت بهار", 122f, 0.39f),
)

private data class Order(val no: String, val customer: String, val amount: String, val done: Boolean)
private val orders = listOf(
    Order("۱۴۲۵۸", "هایپر طلایی", "۴۸٬۲۰۰٬۰۰۰", true),
    Order("۱۴۲۵۷", "فروشگاه زرین", "۳۱٬۷۰۰٬۰۰۰", true),
    Order("۱۴۲۵۶", "پخش نگین", "۱۹٬۴۰۰٬۰۰۰", false),
    Order("۱۴۲۵۵", "مارکت الماس", "۱۲٬۹۰۰٬۰۰۰", true),
    Order("۱۴۲۵۴", "سوپرمارکت بهار", "۸٬۳۰۰٬۰۰۰", false),
)

private data class Kpi(
    val title: String, val target: Float, val suffix: String, val delta: String,
    val icon: ImageVector, val up: Boolean, val spark: List<Int>,
)

private val kpis = listOf(
    Kpi("فروش امروز (میلیون)", 124.5f, "", "+۱۲٪", Icons.Filled.Paid, true, listOf(40, 55, 48, 70, 62, 85, 95)),
    Kpi("سفارش‌های فعال", 348f, "", "+۸٪", Icons.Filled.ShoppingBag, true, listOf(50, 60, 55, 75, 70, 80, 92)),
    Kpi("مشتریان فعال", 2120f, "", "+۵٪", Icons.Filled.Group, true, listOf(45, 50, 65, 60, 75, 85, 90)),
    Kpi("میانگین سبد (هزار تومان)", 845f, "", "+۹٪", Icons.Filled.TrendingUp, true, listOf(35, 48, 52, 66, 74, 82, 95)),
)

// ============================================================ صفحه
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScreen(vm: AppViewModel) {
    BackHandler { vm.closeDemo() }
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("نمایش دمو — داشبورد مدیریتی") },
            navigationIcon = {
                IconButton(onClick = { vm.closeDemo() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ------------------------------------------------ ۱) بنر
            LuxuryCard(gold = true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Insights, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "داشبورد هوشمند مدیریتی آجیل و خشکبار — نمایی زنده و لوکس از گزارش‌های حرفه‌ای",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onPrimary,
                    )
                }
            }

            // ------------------------------------------------ ۲) گیج هدف
            val g1 = stagger(0)
            SectionCard("تحقق هدف فروش سالانه") {
                Gauge(percent = 0.87f, progress = g1, centerTop = "۸۷٪", centerBottom = "۳٫۱ از ۳٫۶ میلیارد")
            }

            // ------------------------------------------------ ۳) KPI
            SectionCard("شاخص‌های کلیدی امروز") {
                KpiGrid(startDelay = 100)
            }

            // ------------------------------------------------ ۴) فروش هفتگی
            val g2 = stagger(300)
            SectionCard("فروش هفتگی (میلیون تومان)") {
                BarChart(weekSales, weekDays, progress = g2)
            }

            // ------------------------------------------------ ۵) دونات
            val g3 = stagger(500)
            SectionCard("سهم فروش دسته‌های محصولات") {
                DonutChart(productShare, progress = g3)
            }

            // ------------------------------------------------ ۶) رادار
            val g4 = stagger(700)
            SectionCard("تحلیل ۶‌بعدی عملکرد برند") {
                RadarChart(radarAxes, radarValues, progress = g4)
            }

            // ------------------------------------------------ ۷) روند
            val g5 = stagger(900)
            SectionCard("روند فروش شش‌ماهه (میلیون تومان)") {
                LineChart(monthlySales, months, progress = g5)
            }

            // ------------------------------------------------ ۸) نقشه حرارتی
            val g6 = stagger(1100)
            SectionCard("نقشه حرارتی فعالیت فروش (۱۲ هفته اخیر)") {
                Heatmap(heat, progress = g6)
            }

            // ------------------------------------------------ ۹) هدف/واقعی
            val g7 = stagger(1300)
            SectionCard("مقایسه هدف و عملکرد فصلی (میلیون)") {
                QuarterChart(quarters, progress = g7)
            }

            // ------------------------------------------------ ۱۰) مشتریان
            SectionCard("مشتریان برتر ماه") {
                TopCustomers(topCustomers)
            }

            // ------------------------------------------------ ۱۱) سفارش‌ها
            SectionCard("آخرین سفارش‌ها") {
                OrdersList(orders)
            }

            Text(
                "★ داده‌های این بخش صرفاً برای نمایش قابلیت‌های برنامه است",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }
}

// ============================================================ اجزای عمومی
/** انیمیشن پلکانی: هر بخش با تأخیر خودش شروع می‌شود */
@Composable
private fun stagger(delayMs: Int): Float {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        started = true
    }
    return animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "stagger$delayMs",
    ).value
}

@Composable
private fun LuxuryCard(gold: Boolean = false, content: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (gold) Brush.horizontalGradient(extras.goldGradient)
                    else Brush.verticalGradient(listOf(scheme.surface, scheme.surface))
                )
        ) {
            Box(Modifier.padding(14.dp)) { content() }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(extras.goldGradient))
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

// ============================================================ گیج دایره‌ای
@Composable
private fun Gauge(percent: Float, progress: Float, centerTop: String, centerBottom: String) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val shine by shimmer.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "shine",
    )
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(190.dp)) {
            val stroke = 22.dp.toPx()
            val r = (size.minDimension - stroke) / 2
            val topLeft = Offset((size.width - r * 2) / 2, (size.height - r * 2) / 2)
            val arcSize = Size(r * 2, r * 2)
            // ریل
            drawArc(
                color = scheme.surfaceVariant,
                startAngle = 135f, sweepAngle = 270f, useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            // درجه‌بندی ظریف
            for (i in 0..27) {
                val a = Math.toRadians((135.0 + 270.0 * i / 27) - 90.0)
                val r1 = r + stroke / 2 + 3.dp.toPx()
                val r2 = r1 + 4.dp.toPx()
                drawLine(
                    color = scheme.outlineVariant.copy(alpha = 0.6f),
                    start = Offset(center.x + (r1 * kotlin.math.cos(a)).toFloat(), center.y + (r1 * kotlin.math.sin(a)).toFloat()),
                    end = Offset(center.x + (r2 * kotlin.math.cos(a)).toFloat(), center.y + (r2 * kotlin.math.sin(a)).toFloat()),
                    strokeWidth = 2f,
                )
            }
            // مقدار با گرادیان طلایی و درخشش
            val sweep = 270f * percent * progress
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(extras.gold, extras.chart[1], extras.gold.copy(alpha = 0.6f + 0.4f * shine), extras.gold),
                ),
                startAngle = 135f, sweepAngle = sweep, useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerTop, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = scheme.primary)
            Text(centerBottom, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
        }
    }
}

// ============================================================ KPI + اسپارک‌لاین
@Composable
private fun KpiGrid(startDelay: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        kpis.chunked(2).forEachIndexed { ri, rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEachIndexed { ci, k ->
                    KpiCard(k, ri * 2 + ci, startDelay, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun KpiCard(k: Kpi, index: Int, startDelay: Int, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay((startDelay + index * 90).toLong()); started = true }
    val p by animateFloatAsState(if (started) 1f else 0f, tween(800), label = "kpi$index")
    val accent = extras.chart[index % extras.chart.size]

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier,
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.55f)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(k.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    k.delta,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (k.up) extras.positive else scheme.error,
                )
            }
            Text(
                if (k.target % 1f == 0f) formatInt((k.target * p).toLong()) else String.format("%.1f", k.target * p),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
            )
            Text(k.title, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            Sparkline(k.spark.map { it.toFloat() }, accent, p)
        }
    }
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
private fun Sparkline(values: List<Float>, color: Color, progress: Float) {
    Canvas(Modifier.fillMaxWidth().height(28.dp)) {
        val maxV = values.max()
        val step = size.width / (values.size - 1)
        val pts = values.mapIndexed { i, v ->
            Offset(i * step, size.height - (v / maxV) * size.height * 0.9f * progress)
        }
        val path = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            for (i in 1 until pts.size) {
                val mid = (pts[i - 1].x + pts[i].x) / 2
                cubicTo(mid, pts[i - 1].y, mid, pts[i].y, pts[i].x, pts[i].y)
            }
        }
        drawPath(path, color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(color, 3.dp.toPx(), pts.last())
    }
}

// ============================================================ ستونی
@Composable
private fun BarChart(values: List<Float>, labels: List<String>, progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(Modifier.fillMaxWidth().height(170.dp)) {
            val maxV = values.max() * 1.15f
            val n = values.size
            val gap = 10.dp.toPx()
            val labelSpace = 20.dp.toPx()
            val w = (size.width - gap * (n - 1)) / n
            values.forEachIndexed { i, v ->
                val h = (v / maxV) * (size.height - labelSpace) * progress
                val x = i * (w + gap)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(extras.gold, scheme.primary.copy(alpha = 0.45f)),
                        startY = size.height - labelSpace - h, endY = size.height - labelSpace,
                    ),
                    topLeft = Offset(x, size.height - labelSpace - h),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(w / 3f, w / 3f),
                )
            }
        }
        Row(Modifier.fillMaxWidth()) {
            labels.forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
    }
}

// ============================================================ دونات
@Composable
private fun DonutChart(slices: List<Slice>, progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 26.dp.toPx()
                val r = (size.minDimension - stroke) / 2
                val topLeft = Offset((size.width - r * 2) / 2, (size.height - r * 2) / 2)
                var start = -90f
                slices.forEachIndexed { i, s ->
                    val sweep = 360f * (s.value / total) * progress
                    drawArc(
                        extras.chart[i % extras.chart.size],
                        start, (sweep - 2.5f).coerceAtLeast(0.5f), false,
                        topLeft, Size(r * 2, r * 2),
                        Stroke(stroke, cap = StrokeCap.Butt),
                    )
                    start += 360f * (s.value / total)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("مجموع", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                Text("۱٬۴۲۵ م", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slices.forEachIndexed { i, s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(11.dp).clip(CircleShape).background(extras.chart[i % extras.chart.size]))
                    Spacer(Modifier.width(8.dp))
                    Text(s.label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text("${(s.value / total * 100).toInt()}٪", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold, color = extras.chart[i % extras.chart.size])
                }
            }
        }
    }
}

// ============================================================ رادار
@Composable
private fun RadarChart(axes: List<String>, values: List<Float>, progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Canvas(Modifier.size(230.dp)) {
            val c = center
            val r = size.minDimension / 2 - 26.dp.toPx()
            val n = axes.size
            fun pt(idx: Int, frac: Float): Offset {
                val a = Math.toRadians((360.0 * idx / n) - 90.0)
                return Offset(
                    c.x + (r * frac * kotlin.math.cos(a)).toFloat(),
                    c.y + (r * frac * kotlin.math.sin(a)).toFloat(),
                )
            }
            // شبکه
            for (ring in 1..4) {
                val frac = ring / 4f
                val grid = Path().apply {
                    for (i in 0 until n) {
                        val p = pt(i, frac)
                        if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                    }
                    close()
                }
                drawPath(grid, scheme.outlineVariant.copy(alpha = 0.4f), Stroke(1.5f))
            }
            for (i in 0 until n) drawLine(scheme.outlineVariant.copy(alpha = 0.4f), c, pt(i, 1f), 1.5f)
            // مقدار
            val poly = Path().apply {
                for (i in 0 until n) {
                    val p = pt(i, values[i] * progress)
                    if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                }
                close()
            }
            drawPath(poly, extras.gold.copy(alpha = 0.25f))
            drawPath(poly, extras.gold, Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
            for (i in 0 until n) drawCircle(extras.gold, 4.dp.toPx(), pt(i, values[i] * progress))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            axes.forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    }
}

// ============================================================ خطی
@Composable
private fun LineChart(values: List<Float>, labels: List<String>, progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(Modifier.fillMaxWidth().height(170.dp)) {
            val maxV = values.max() * 1.2f
            val labelSpace = 22.dp.toPx()
            val w = size.width
            val h = size.height - labelSpace
            val stepX = w / (values.size - 1)
            for (g in 1..3) {
                val y = h * g / 4f
                drawLine(scheme.outlineVariant.copy(alpha = 0.3f), Offset(0f, y), Offset(w, y), 1.dp.toPx())
            }
            val pts = values.mapIndexed { i, v -> Offset(i * stepX, h - (v / maxV) * h * progress) }
            val fill = Path().apply {
                moveTo(pts.first().x, h)
                pts.forEach { lineTo(it.x, it.y) }
                lineTo(pts.last().x, h); close()
            }
            drawPath(fill, Brush.verticalGradient(listOf(extras.gold.copy(alpha = 0.4f), extras.gold.copy(alpha = 0.02f))))
            val line = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                for (i in 1 until pts.size) {
                    val mid = (pts[i - 1].x + pts[i].x) / 2
                    cubicTo(mid, pts[i - 1].y, mid, pts[i].y, pts[i].x, pts[i].y)
                }
            }
            drawPath(line, extras.gold, Stroke(3.dp.toPx(), cap = StrokeCap.Round))
            pts.forEach {
                drawCircle(extras.gold.copy(alpha = 0.3f), 9.dp.toPx(), it)
                drawCircle(scheme.surface, 5.dp.toPx(), it)
                drawCircle(extras.gold, 3.5.dp.toPx(), it)
            }
        }
        Row(Modifier.fillMaxWidth()) {
            labels.forEach {
                Text(it.take(3), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
    }
}

// ============================================================ نقشه حرارتی
@Composable
private fun Heatmap(data: List<List<Float>>, progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(Modifier.fillMaxWidth().height(140.dp)) {
            val cols = data.size
            val rows = 7
            val gap = 3.dp.toPx()
            val cw = (size.width - gap * (cols - 1)) / cols
            val ch = (size.height - gap * (rows - 1)) / rows
            data.forEachIndexed { x, week ->
                week.forEachIndexed { y, v ->
                    val a = (v * progress).coerceIn(0f, 1f)
                    drawRoundRect(
                        color = if (a < 0.03f) scheme.surfaceVariant
                        else extras.gold.copy(alpha = 0.15f + 0.85f * a),
                        topLeft = Offset(x * (cw + gap), y * (ch + gap)),
                        size = Size(cw, ch),
                        cornerRadius = CornerRadius(4f, 4f),
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("کم", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                listOf(0.15f, 0.35f, 0.55f, 0.75f, 1f).forEach { a ->
                    Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(extras.gold.copy(alpha = a)))
                }
            }
            Text("زیاد", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        }
    }
}

// ============================================================ هدف/واقعی
@Composable
private fun QuarterChart(quarters: List<Quarter>, progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Legend(extras.gold, "هدف")
            Legend(scheme.primary, "عملکرد واقعی")
        }
        Canvas(Modifier.fillMaxWidth().height(160.dp)) {
            val maxV = quarters.maxOf { maxOf(it.target, it.actual) } * 1.15f
            val groupGap = 18.dp.toPx()
            val labelSpace = 20.dp.toPx()
            val gw = (size.width - groupGap * (quarters.size - 1)) / quarters.size
            quarters.forEachIndexed { i, q ->
                val x = i * (gw + groupGap)
                val bw = (gw - 5.dp.toPx()) / 2
                val hT = (q.target / maxV) * (size.height - labelSpace) * progress
                val hA = (q.actual / maxV) * (size.height - labelSpace) * progress
                drawRoundRect(
                    scheme.primary.copy(alpha = 0.35f),
                    topLeft = Offset(x, size.height - labelSpace - hT), size = Size(bw, hT),
                    cornerRadius = CornerRadius(6f, 6f),
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(extras.gold, extras.gold.copy(alpha = 0.6f))),
                    topLeft = Offset(x + bw + 5.dp.toPx(), size.height - labelSpace - hA), size = Size(bw, hA),
                    cornerRadius = CornerRadius(6f, 6f),
                )
            }
        }
        Row(Modifier.fillMaxWidth()) {
            quarters.forEach {
                Text(it.name, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

// ============================================================ مشتریان برتر
@Composable
private fun TopCustomers(customers: List<Cust>) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        customers.forEachIndexed { i, c ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == 0) Brush.linearGradient(extras.goldGradient)
                            else Brush.linearGradient(listOf(scheme.primary.copy(alpha = 0.8f), scheme.primary.copy(alpha = 0.4f)))
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (i == 0) Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                    else Text("${i + 1}", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(c.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        if (i == 0) {
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Filled.Verified, contentDescription = null, tint = extras.gold, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(scheme.surfaceVariant)) {
                        Box(
                            Modifier
                                .fillMaxWidth(c.share)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (i == 0) Brush.horizontalGradient(extras.goldGradient) else Brush.horizontalGradient(listOf(scheme.primary, scheme.tertiary)))
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text("${c.amount} م", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
            }
        }
    }
}

// ============================================================ سفارش‌ها
@Composable
private fun OrdersList(orders: List<Order>) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        orders.forEach { o ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(scheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 9.dp),
            ) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (o.done) extras.positive.copy(alpha = 0.2f) else scheme.tertiary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (o.done) Icons.Filled.CheckCircle else Icons.Filled.HourglassTop,
                        contentDescription = null,
                        tint = if (o.done) extras.positive else scheme.tertiary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(o.customer, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text("سفارش ${o.no}", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(o.amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = scheme.primary)
                    Text(
                        if (o.done) "پرداخت شده" else "در انتظار",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (o.done) extras.positive else scheme.tertiary,
                    )
                }
            }
        }
    }
}
