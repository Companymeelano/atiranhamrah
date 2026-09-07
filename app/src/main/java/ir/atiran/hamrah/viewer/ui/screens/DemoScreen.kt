package ir.atiran.hamrah.viewer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras

// ------------------------------------------------------------ داده نمونه
private val weekDays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
private val weekSales = listOf(42f, 58f, 51f, 73f, 66f, 88f, 95f) // میلیون تومان

private data class DonutSlice(val label: String, val value: Float)
private val productShare = listOf(
    DonutSlice("پسته", 38f),
    DonutSlice("بادام", 24f),
    DonutSlice("گردو", 18f),
    DonutSlice("فندق", 12f),
    DonutSlice("کشمش و خرما", 8f),
)

private val months = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور")
private val monthlySales = listOf(180f, 210f, 195f, 260f, 240f, 310f)

private data class TopProduct(val name: String, val amount: Float, val share: Float)
private val topProducts = listOf(
    TopProduct("پسته اکبری درجه‌یک", 482f, 1f),
    TopProduct("بادام ممتاز شور", 316f, 0.66f),
    TopProduct("گردو تازه کاغذی", 245f, 0.51f),
    TopProduct("مغز پسته خام", 198f, 0.41f),
    TopProduct("کشمش تیزابی ممتاز", 134f, 0.28f),
)

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
            Spacer(Modifier.height(2.dp))

            // بنر معرفی
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.primaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.Insights,
                        contentDescription = null,
                        tint = scheme.onPrimaryContainer,
                        modifier = Modifier.size(30.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "نمایی از توانمندی برنامه در ارائه گزارش‌ها و تحلیل‌های مدیریتی هوشمند — همه‌چیز زنده، رنگارنگ و هماهنگ با تم شما",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onPrimaryContainer,
                    )
                }
            }

            // کارت‌های شاخص
            KpiGrid(extras.chart)

            // فروش هفتگی
            SectionCard(title = "فروش هفتگی (میلیون تومان)") {
                BarChart(
                    values = weekSales,
                    labels = weekDays,
                    barColor = scheme.primary,
                    barColor2 = scheme.tertiary,
                )
            }

            // سهم دسته‌ها + چارت دونات
            SectionCard(title = "سهم فروش دسته‌های محصولات") {
                DonutChart(slices = productShare, palette = extras.chart)
            }

            // روند شش‌ماهه
            SectionCard(title = "روند فروش شش‌ماهه (میلیون تومان)") {
                LineChart(
                    values = monthlySales,
                    labels = months,
                    lineColor = scheme.primary,
                    fillColor = scheme.primary,
                )
            }

            // گزارش مدیریتی
            SectionCard(title = "خلاصه گزارش مدیریتی") {
                SummaryReport(extras.positive)
            }

            // برترین محصولات
            SectionCard(title = "برترین محصولات ماه") {
                TopProductsList(topProducts, extras.chart)
            }

            // یادداشت
            Text(
                "★ داده‌های این بخش صرفاً برای نمایش قابلیت‌های برنامه است",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp),
            )
        }
    }
}

// ------------------------------------------------------------ اجزا
@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

@Composable
private fun KpiGrid(palette: List<Color>) {
    val scheme = MaterialTheme.colorScheme
    data class Kpi(
        val title: String,
        val value: String,
        val delta: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val up: Boolean,
    )
    val kpis = listOf(
        Kpi("فروش امروز", "۱۲۴٫۵ م", "+۱۲٪", Icons.Filled.Paid, true),
        Kpi("سفارش‌های فعال", "۳۴۸", "+۸٪", Icons.Filled.ShoppingBag, true),
        Kpi("مشتریان فعال", "۲٬۱۲۰", "+۵٪", Icons.Filled.Group, true),
        Kpi("سود خالص ماه", "۳۸٫۲ م", "-۲٪", Icons.Filled.TrendingDown, false),
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        kpis.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEachIndexed { i, k ->
                    val accent = palette[(kpis.indexOf(k)) % palette.size]
                    KpiCard(k.title, k.value, k.delta, k.icon, k.up, accent, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    delta: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    up: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.55f)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
            Text(
                delta,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (up) LocalThemeExtras.current.positive else scheme.error,
            )
        }
    }
}

// ------------------------------------------------------------ چارت ستونی
@Composable
private fun BarChart(values: List<Float>, labels: List<String>, barColor: Color, barColor2: Color) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "bars",
    )
    val scheme = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxWidth().height(170.dp),
        ) {
            val maxV = (values.maxOrNull() ?: 1f) * 1.15f
            val n = values.size
            val gap = 10.dp.toPx()
            val labelSpace = 22.dp.toPx()
            val w = (size.width - gap * (n - 1)) / n
            values.forEachIndexed { i, v ->
                val h = (v / maxV) * (size.height - labelSpace) * progress
                val x = i * (w + gap)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(barColor, barColor2.copy(alpha = 0.6f)),
                        startY = size.height - labelSpace - h,
                        endY = size.height - labelSpace,
                    ),
                    topLeft = Offset(x, size.height - labelSpace - h),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(w / 3.2f, w / 3.2f),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { l ->
                Text(
                    l,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

// ------------------------------------------------------------ چارت دونات
@Composable
private fun DonutChart(slices: List<DonutSlice>, palette: List<Color>) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "donut",
    )
    val scheme = MaterialTheme.colorScheme
    val total = slices.sumOf { it.value.toDouble() }.toFloat()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 26.dp.toPx()
                val radius = (minOf(size.width, size.height) - stroke) / 2
                val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
                var startAngle = -90f
                slices.forEachIndexed { i, s ->
                    val sweep = 360f * (s.value / total) * progress
                    drawArc(
                        color = palette[i % palette.size],
                        startAngle = startAngle,
                        sweepAngle = (sweep - 2.5f).coerceAtLeast(0.5f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = stroke, cap = StrokeCap.Butt),
                    )
                    startAngle += 360f * (s.value / total)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("مجموع", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                Text(
                    "۱٬۴۲۵ م",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slices.forEachIndexed { i, s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(palette[i % palette.size])
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        s.label,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${(s.value / total * 100).toInt()}٪",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette[i % palette.size],
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------ چارت خطی
@Composable
private fun LineChart(values: List<Float>, labels: List<String>, lineColor: Color, fillColor: Color) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 1100),
        label = "line",
    )
    val scheme = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(170.dp)) {
            val maxV = (values.maxOrNull() ?: 1f) * 1.2f
            val minV = 0f
            val labelSpace = 24.dp.toPx()
            val w = size.width
            val h = size.height - labelSpace
            val stepX = w / (values.size - 1)

            // خطوط راهنما
            for (g in 1..3) {
                val y = h * g / 4f
                drawLine(
                    color = scheme.outlineVariant.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val points = values.mapIndexed { i, v ->
                val x = i * stepX
                val y = h - ((v - minV) / (maxV - minV)) * h * progress
                Offset(x, y)
            }

            // ناحیه زیر منحنی
            val fillPath = Path().apply {
                moveTo(points.first().x, h)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, h)
                close()
            }
            drawPath(
                fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(fillColor.copy(alpha = 0.35f), fillColor.copy(alpha = 0.02f)),
                ),
            )

            // منحنی نرم
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val cur = points[i]
                    val midX = (prev.x + cur.x) / 2
                    cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
                }
            }
            drawPath(
                linePath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )

            // نقاط
            points.forEach { p ->
                drawCircle(color = lineColor, radius = 5.dp.toPx(), center = p)
                drawCircle(color = fillColor.copy(alpha = 0.3f), radius = 9.dp.toPx(), center = p)
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            labels.forEach { l ->
                Text(
                    l.take(3),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

// ------------------------------------------------------------ گزارش مدیریتی
@Composable
private fun SummaryReport(positive: Color) {
    val scheme = MaterialTheme.colorScheme
    data class Row_(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String, val value: String, val accent: Color)
    val rows = listOf(
        Row_(Icons.Filled.TrendingUp, "رشد فروش نسبت به ماه قبل", "۱۲٪+", positive),
        Row_(Icons.Filled.ThumbUp, "رضایت مشتریان", "۹۴٪", scheme.primary),
        Row_(Icons.Filled.TrendingDown, "نرخ مرجوعی کالا", "۲٪-", scheme.tertiary),
        Row_(Icons.Filled.Flag, "تحقق هدف فروش ماه", "۸۷٪", scheme.secondary),
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { r ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(r.accent, r.accent.copy(alpha = 0.5f)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(r.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(r.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(
                    r.value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = r.accent,
                )
            }
        }
    }
}

// ------------------------------------------------------------ برترین محصولات
@Composable
private fun TopProductsList(products: List<TopProduct>, palette: List<Color>) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        products.forEachIndexed { i, p ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(palette[i % palette.size], palette[i % palette.size].copy(alpha = 0.5f)))),
                    contentAlignment = Alignment.Center,
                ) {
                    if (i == 0) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            "${i + 1}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(p.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(scheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(p.share)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Brush.horizontalGradient(listOf(palette[i % palette.size], scheme.primary)))
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "${p.amount} م",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                )
            }
        }
    }
}
