package ir.atiran.hamrah.viewer.ui.screens

/**
 * استودیو چارت M•REPORT — «چارت‌های من»
 *
 * بخشی از نمای کلی که کاربر خودش می‌سازد: از فهرست چارت‌های ضروری
 * مدیریت (فروش، خرید، مشتریان، چک‌ها، مطالبات، سود، خزانه) انتخاب می‌کند،
 * روی داشبورد می‌نشاند، جابه‌جا می‌کند و هر وقت بخواهد برمی‌دارد —
 * حذف فقط از «نمایش» است؛ چارت همیشه در استودیو هست و دوباره قابل افزودن.
 * همه‌چیز هماهنگ با تم انتخابی کاربر: رنگ‌ها از پالت تم خوانده می‌شود.
 */
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.GlassCard
import ir.atiran.hamrah.viewer.ui.components.MrIcons
import ir.atiran.hamrah.viewer.ui.components.MrPillButton
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import ir.atiran.hamrah.viewer.utils.SoundFx

// ============================================================ انواع داده چارت

/** برش دونات — برچسب + وزن + زیرنویس */
data class Slice3(val label: String, val value: Float, val caption: String)

/** ستون/میله — برچسب + مقدار + زیرنویس عددی */
data class Bar3(val label: String, val v: Float, val caption: String = "")

/** منحنی نرم بین نقاط — لحن یکسان با چارت‌های امضای M•REPORT */
private fun smoothPath(pts: List<Offset>): Path = Path().apply {
    if (pts.isEmpty()) return@apply
    moveTo(pts.first().x, pts.first().y)
    for (i in 1 until pts.size) {
        val mid = (pts[i - 1].x + pts[i].x) / 2f
        cubicTo(mid, pts[i - 1].y, mid, pts[i].y, pts[i].x, pts[i].y)
    }
}

// ============================================================ چارت‌های خودکفا

/** دونات شیشه‌ای هماهنگ با تم — حلقه پایه + برش‌های پالت چارت تم */
@Composable
fun StudioDonut(
    slices: List<Slice3>,
    progress: Float = 1f,
    centerText: String? = null,
    centerSub: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val total = slices.map { it.value }.sum().coerceAtLeast(0.001f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 16.dp.toPx()
                val r = (size.minDimension - stroke) / 2f
                val topLeft = Offset((size.width - r * 2f) / 2f, (size.height - r * 2f) / 2f)
                drawArc(
                    color = scheme.surfaceVariant.copy(alpha = 0.45f),
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    topLeft = topLeft, size = Size(r * 2f, r * 2f),
                    style = Stroke(stroke),
                )
                var start = -90f
                slices.forEachIndexed { i, s ->
                    val sweep = 360f * (s.value / total) * progress
                    drawArc(
                        color = extras.chart[i % extras.chart.size],
                        startAngle = start,
                        sweepAngle = (sweep - 2f).coerceAtLeast(0.5f),
                        useCenter = false,
                        topLeft = topLeft, size = Size(r * 2f, r * 2f),
                        style = Stroke(stroke, cap = StrokeCap.Butt),
                    )
                    start += 360f * (s.value / total)
                }
            }
            if (centerText != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        centerText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = scheme.primary,
                    )
                    if (centerSub != null) {
                        Text(centerSub, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            slices.forEachIndexed { i, s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(extras.chart[i % extras.chart.size]))
                    Spacer(Modifier.width(6.dp))
                    Text(s.label, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        s.caption,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = extras.chart[i % extras.chart.size],
                    )
                }
            }
        }
    }
}

/** ستون‌های سه‌بعدی متالیک — وجه کناری و بالا + گرادیان تم */
@Composable
fun StudioColumns(items: List<Bar3>, progress: Float = 1f, height: Dp = 150.dp) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            val maxV = items.maxOf { it.v } * 1.15f
            val n = items.size
            val gap = 10.dp.toPx()
            val labelSpace = 16.dp.toPx()
            val depth = 6.dp.toPx()
            val w = (size.width - gap * (n - 1)) / n
            items.forEachIndexed { i, p ->
                val hFull = (p.v / maxV) * (size.height - labelSpace - depth) * progress
                val x = i * (w + gap)
                val yTop = size.height - labelSpace - hFull
                val side = Path().apply {
                    moveTo(x + w, yTop)
                    lineTo(x + w + depth, yTop - depth)
                    lineTo(x + w + depth, size.height - labelSpace - depth)
                    lineTo(x + w, size.height - labelSpace)
                    close()
                }
                drawPath(side, color = scheme.primary.copy(alpha = 0.18f))
                val top = Path().apply {
                    moveTo(x, yTop)
                    lineTo(x + depth, yTop - depth)
                    lineTo(x + w + depth, yTop - depth)
                    lineTo(x + w, yTop)
                    close()
                }
                drawPath(top, color = scheme.primary.copy(alpha = 0.50f))
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
                    it.label.take(6),
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

/** روند تک‌خطی با هاله نور و نقطه امضای برند در انتها */
@Composable
fun StudioArea(
    values: List<Float>,
    color: Color,
    height: Dp = 126.dp,
    labels: List<String> = emptyList(),
) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            val minV = values.min()
            val maxV = values.max()
            fun norm(v: Float): Float =
                if (maxV - minV < 0.0001f) 0.5f else (v - minV) / (maxV - minV)
            val step = size.width / (values.size - 1f).coerceAtLeast(1f)
            val pts = values.mapIndexed { i, v ->
                Offset(i * step, size.height * (0.86f - 0.72f * norm(v)))
            }
            for (g in 1..2) {
                drawLine(
                    scheme.surfaceVariant.copy(alpha = 0.5f),
                    Offset(0f, size.height * g / 3f),
                    Offset(size.width, size.height * g / 3f),
                    1f,
                )
            }
            val fill = Path().apply {
                moveTo(pts.first().x, size.height)
                pts.forEach { lineTo(it.x, it.y) }
                lineTo(pts.last().x, size.height)
                close()
            }
            drawPath(fill, brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.24f), Color.Transparent)))
            drawPath(
                smoothPath(pts),
                color = color,
                style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
            )
            drawCircle(color.copy(alpha = 0.25f), radius = 4.5.dp.toPx(), center = pts.last())
            drawCircle(color, radius = 2.4.dp.toPx(), center = pts.last())
        }
        if (labels.isNotEmpty()) {
            Row(Modifier.fillMaxWidth()) {
                labels.forEach {
                    Text(
                        it.take(4),
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
}

/** دو منحنی موازی برای مقایسه (خرید/فروش) + راهنمای رنگ */
@Composable
fun StudioDualLines(
    a: List<Float>,
    b: List<Float>,
    colorA: Color,
    colorB: Color,
    labelA: String,
    labelB: String,
    height: Dp = 136.dp,
) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            LegendDot(colorA, labelA)
            LegendDot(colorB, labelB)
        }
        Canvas(Modifier.fillMaxWidth().height(height)) {
            val all = a + b
            val minV = all.min()
            val maxV = all.max()
            fun norm(v: Float): Float =
                if (maxV - minV < 0.0001f) 0.5f else (v - minV) / (maxV - minV)
            fun ptsOf(vs: List<Float>): List<Offset> {
                val step = size.width / (vs.size - 1f).coerceAtLeast(1f)
                return vs.mapIndexed { i, v -> Offset(i * step, size.height * (0.88f - 0.76f * norm(v))) }
            }
            for (g in 1..2) {
                drawLine(
                    scheme.surfaceVariant.copy(alpha = 0.5f),
                    Offset(0f, size.height * g / 3f),
                    Offset(size.width, size.height * g / 3f),
                    1f,
                )
            }
            listOf(a to colorA, b to colorB).forEach { (vs, c) ->
                val pts = ptsOf(vs)
                drawPath(
                    smoothPath(pts),
                    color = c,
                    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
                )
                drawCircle(c.copy(alpha = 0.25f), radius = 4.5.dp.toPx(), center = pts.last())
                drawCircle(c, radius = 2.4.dp.toPx(), center = pts.last())
            }
        }
    }
}

/** نقطه راهنمای رنگی */
@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** میله‌های افقی برای شکافت سن بدهی و موجودی بانک‌ها */
@Composable
fun StudioHBars(items: List<Bar3>, color: Color) {
    val scheme = MaterialTheme.colorScheme
    val maxV = items.maxOf { it.v }.coerceAtLeast(0.001f)
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        items.forEach { b ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    b.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.width(66.dp),
                    maxLines = 1,
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(11.dp)
                        .clip(RoundedCornerShape(50))
                        .background(scheme.surfaceVariant.copy(alpha = 0.35f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(b.v / maxV)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(color.copy(alpha = 0.85f), color.copy(alpha = 0.35f))
                                )
                            ),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    b.caption,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.width(52.dp),
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                )
            }
        }
    }
}

// ============================================================ فهرست چارت‌های ضروری مدیریت

/** تعریف یک چارت استودیویی — id برای ذخیره، render برای نمایش */
data class ChartDef(
    val id: String,
    val title: String,
    val desc: String,
    val cat: String,
    val icon: ImageVector,
    val render: @Composable () -> Unit,
)

private val salesWeekData = listOf(
    Bar3("شنبه", 62f, "۶۲"), Bar3("یکشنبه", 48f, "۴۸"), Bar3("دوشنبه", 75f, "۷۵"),
    Bar3("سه‌شنبه", 58f, "۵۸"), Bar3("چهارشنبه", 82f, "۸۲"), Bar3("پنجشنبه", 91f, "۹۱"),
    Bar3("جمعه", 44f, "۴۴"),
)

private val buySellBuy = listOf(120f, 135f, 110f, 150f, 140f, 165f, 155f, 180f)
private val buySellSell = listOf(140f, 128f, 158f, 145f, 172f, 160f, 188f, 205f)
private val buySellMonths = listOf("فرو", "ارد", "خرد", "تیر", "مرد", "شهر", "مهر", "آبا")

private val topCustomersData = listOf(
    Bar3("هایپر طلایی", 95f, "۴۸م"), Bar3("زرین", 78f, "۳۲م"),
    Bar3("نگین", 56f, "۱۹م"), Bar3("الماس", 42f, "۱۳م"), Bar3("بهار", 31f, "۸م"),
)

private val agingData = listOf(
    Bar3("۰ تا ۳۰ روز", 96f, "۹۶م"), Bar3("۳۰ تا ۶۰", 88f, "۸۸م"),
    Bar3("۶۰ تا ۹۰", 64f, "۶۴م"), Bar3("بیش از ۹۰", 148f, "۱۴۸م"),
)

private val bankBarsData = listOf(
    Bar3("بانک ملت", 100f, "۱۸۲م"), Bar3("بانک سامان", 53f, "۹۷م"), Bar3("پاسارگاد", 79f, "۱۴۳م"),
)

private val profitTrendData = listOf(38f, 42f, 40f, 47f, 52f, 49f, 58f, 63f, 60f, 68f, 74f, 81f)
private val profitLabels = listOf("فرو", "خرد", "تیر", "مرد", "شهر", "مهر", "آبا", "دی")

private val customerGrowth = listOf(2100f, 2110f, 2124f, 2140f, 2160f, 2185f, 2210f, 2250f, 2290f, 2340f, 2400f, 2470f)

private val marginData = listOf(14f, 15f, 14.5f, 16f, 17f, 16.5f, 18f, 18.5f, 18f, 19.5f, 20f, 21f)

/** کاتالوگ چارت‌ها — نیازهای ضروری مدیریت خرید و فروش، چک، مشتری و سود */
val chartCatalog: List<ChartDef> = listOf(
    ChartDef(
        id = "sales_week", title = "فروش هفتگی", cat = "فروش",
        desc = "مقایسه فروش ۷ روز اخیر — میلیون تومان",
        icon = MrIcons.Trend,
        render = { StudioColumns(salesWeekData) },
    ),
    ChartDef(
        id = "buy_sell", title = "خرید در برابر فروش", cat = "فروش",
        desc = "روند ماهانه خرید و فروش — ۸ ماه اخیر",
        icon = MrIcons.Reports,
        render = {
            val extras = LocalThemeExtras.current
            StudioDualLines(
                buySellBuy, buySellSell,
                colorA = extras.gold, colorB = extras.positive,
                labelA = "خرید", labelB = "فروش",
            )
        },
    ),
    ChartDef(
        id = "checks_status", title = "وضعیت چک‌ها", cat = "چک‌ها",
        desc = "در جریان، وصول‌شده و پاس‌شده — این ماه",
        icon = MrIcons.Checks,
        render = {
            StudioDonut(
                listOf(
                    Slice3("در جریان", 45f, "۴۵٪"),
                    Slice3("وصول‌شده", 35f, "۳۵٪"),
                    Slice3("پاس‌شده", 20f, "۲۰٪"),
                ),
                centerText = "۴۵٪", centerSub = "در جریان",
            )
        },
    ),
    ChartDef(
        id = "top_customers", title = "مشتریان برتر", cat = "مشتریان",
        desc = "۵ مشتری با بیشترین گردش مالی",
        icon = MrIcons.Customers,
        render = { StudioColumns(topCustomersData) },
    ),
    ChartDef(
        id = "recv_aging", title = "شکافت مطالبات", cat = "مشتریان",
        desc = "سن بدهی مشتریان — میلیون تومان",
        icon = MrIcons.Receivables,
        render = {
            val extras = LocalThemeExtras.current
            StudioHBars(agingData, extras.gold)
        },
    ),
    ChartDef(
        id = "profit_trend", title = "روند سود ماهانه", cat = "سود و زیان",
        desc = "سود خالص ۱۲ ماه اخیر — میلیون تومان",
        icon = MrIcons.Trend,
        render = {
            val extras = LocalThemeExtras.current
            StudioArea(profitTrendData, extras.positive, labels = profitLabels)
        },
    ),
    ChartDef(
        id = "cash_flow", title = "موجودی بانک‌ها", cat = "خزانه",
        desc = "گردش نقدی حساب‌های بانکی — میلیون تومان",
        icon = MrIcons.Bank,
        render = {
            val scheme = MaterialTheme.colorScheme
            StudioHBars(bankBarsData, scheme.primary)
        },
    ),
    ChartDef(
        id = "product_share", title = "سهم دسته‌های کالا", cat = "کالاها",
        desc = "پرفروش‌ترین گروه‌های کالایی از کل فروش",
        icon = MrIcons.Products,
        render = {
            StudioDonut(
                listOf(
                    Slice3("پسته", 38f, "۳۸٪"), Slice3("بادام", 24f, "۲۴٪"),
                    Slice3("گردو", 18f, "۱۸٪"), Slice3("فندق", 12f, "۱۲٪"), Slice3("کشمش", 8f, "۸٪"),
                ),
            )
        },
    ),
    ChartDef(
        id = "customer_growth", title = "رشد مشتریان", cat = "مشتریان",
        desc = "مجموع مشتریان فعال در ۱۲ ماه اخیر",
        icon = MrIcons.Customers,
        render = {
            val scheme = MaterialTheme.colorScheme
            StudioArea(customerGrowth, scheme.primary)
        },
    ),
    ChartDef(
        id = "margin", title = "حاشیه سود", cat = "سود و زیان",
        desc = "درصد حاشیه سود خالص — روند سالانه",
        icon = MrIcons.Reports,
        render = {
            val extras = LocalThemeExtras.current
            StudioArea(marginData, extras.gold)
        },
    ),
)

// ============================================================ بخش «چارت‌های من» در نمای کلی

@Composable
fun MyChartsSection(vm: AppViewModel) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val saved by vm.myCharts.collectAsState()
    val ids = remember(saved) {
        saved?.split(",")
            ?.map { it.trim() }
            ?.filter { id -> chartCatalog.any { it.id == id } }
            ?.distinct()
            ?: emptyList()
    }
    var edit by remember { mutableStateOf(false) }
    var studioOpen by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // سربرگ: نشان سه‌بعدی + عنوان + حالت ویرایش
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(scheme.primary.copy(alpha = 0.32f), scheme.primary.copy(alpha = 0.10f))
                        )
                    )
                    .border(1.dp, scheme.primary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(MrIcons.ChartPlus, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text("چارت‌های من", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (ids.isNotEmpty()) {
                Text(
                    ids.size.toString() + " چارت",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(scheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (edit) scheme.primary.copy(alpha = 0.18f) else extras.glassStrong)
                        .border(1.dp, if (edit) scheme.primary else extras.hairline, CircleShape)
                        .clickable { edit = !edit; SoundFx.soft() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        MrIcons.Settings, contentDescription = "ویرایش چارت‌ها",
                        tint = if (edit) scheme.primary else scheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp),
                    )
                }
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(scheme.primary.copy(alpha = 0.30f), scheme.primary.copy(alpha = 0.10f))
                            )
                        )
                        .border(1.dp, scheme.primary.copy(alpha = 0.5f), CircleShape)
                        .clickable { studioOpen = true; SoundFx.soft() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(MrIcons.ChartPlus, contentDescription = "افزودن چارت", tint = scheme.primary, modifier = Modifier.size(13.dp))
                }
            }
        }
        if (edit && ids.isNotEmpty()) {
            Text(
                "با فلش‌ها جابه‌جا کنید؛ با ✕ چارت را از داشبورد بردارید (از استودیو دوباره برمی‌گردد)",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.primary,
            )
        }
        if (ids.isEmpty()) {
            // دکمه خلافانه — دعوت بزرگ به ساخت داشبورد شخصی
            StudioCTA(onClick = { studioOpen = true })
        } else {
            ids.forEachIndexed { i, id ->
                val def = chartCatalog.first { it.id == id }
                MyChartCard(
                    def,
                    showHandles = edit,
                    onUp = {
                        if (i > 0) {
                            val n = ids.toMutableList()
                            n.add(i - 1, n.removeAt(i))
                            vm.setMyCharts(n)
                        }
                    },
                    onDown = {
                        if (i < ids.size - 1) {
                            val n = ids.toMutableList()
                            n.add(i + 1, n.removeAt(i))
                            vm.setMyCharts(n)
                        }
                    },
                    onRemove = {
                        vm.setMyCharts(ids - id)
                        SoundFx.soft()
                    },
                )
            }
            if (!edit) {
                // افزودن چارت دیگر — pill جمع‌وجور
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(extras.glass)
                        .border(1.dp, extras.hairline, RoundedCornerShape(14.dp))
                        .clickable { studioOpen = true; SoundFx.soft() }
                        .padding(vertical = 10.dp),
                ) {
                    Icon(MrIcons.ChartPlus, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(
                        "افزودن چارت دیگر",
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    if (studioOpen) {
        ChartStudioSheet(
            selected = ids,
            onToggle = { id ->
                val cur = ids.toMutableList()
                if (id in cur) cur.remove(id) else cur.add(id)
                vm.setMyCharts(cur)
                SoundFx.soft()
            },
            onDismiss = { studioOpen = false },
        )
    }
}

// ============================================================ دکمه «ساخت چارت» — امضای خلافانه

/** دکمه دعوت بزرگ استودیو: گرادیان تم + جاروی نور مداوم + نشان کج و جسورانه */
@Composable
private fun StudioCTA(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val tr = rememberInfiniteTransition(label = "ctaSheen")
    val sheen by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2900, easing = LinearEasing)),
        label = "ctaSheenX",
    )
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        scheme.primary.copy(alpha = 0.20f),
                        extras.accent.copy(alpha = 0.14f),
                        scheme.primary.copy(alpha = 0.20f),
                    )
                )
            )
            .border(1.dp, scheme.primary.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .drawBehind {
                // جاروی نور مورب — نفس‌کشیدن دکمه
                val w = size.width
                val x = -0.35f * w + sheen * 1.7f * w
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, scheme.primary.copy(alpha = 0.13f), Color.Transparent)
                    ),
                    topLeft = Offset(x, 0f),
                    size = Size(w * 0.34f, size.height),
                    cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                )
            }
            .clickable { onClick(); SoundFx.soft() }
            .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .size(44.dp)
                    .graphicsLayer { rotationZ = -8f }
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.verticalGradient(extras.goldGradient))
                    .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(MrIcons.ChartPlus, contentDescription = null, tint = extras.goldOn, modifier = Modifier.size(21.dp))
            }
            Column {
                Text("ساخت چارت", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text(
                    "چارت‌های مورد علاقه‌ات را انتخاب و بچین — هماهنگ با تمِ تو",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ============================================================ کارت یک چارت منتخب

@Composable
private fun MyChartCard(
    def: ChartDef,
    showHandles: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onRemove: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Box {
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(scheme.primary.copy(alpha = 0.30f), scheme.primary.copy(alpha = 0.10f))
                                )
                            )
                            .border(1.dp, scheme.primary.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(def.icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(14.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(def.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1)
                    Text(
                        def.cat,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(scheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        maxLines = 1,
                    )
                }
                def.render()
            }
        }
        if (showHandles) {
            Row(
                Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                StudioHandle(Icons.Filled.KeyboardArrowUp, onUp)
                StudioHandle(Icons.Filled.KeyboardArrowDown, onDown)
                StudioHandle(MrIcons.Close, onRemove, dangerous = true)
            }
        }
    }
}

@Composable
private fun StudioHandle(icon: ImageVector, onClick: () -> Unit, dangerous: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .size(27.dp)
            .clip(CircleShape)
            .background(scheme.surface.copy(alpha = 0.94f))
            .border(1.dp, LocalThemeExtras.current.hairline, CircleShape)
            .clickable { onClick(); SoundFx.soft() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon, contentDescription = null,
            tint = if (dangerous) scheme.error else scheme.primary,
            modifier = Modifier.size(14.dp),
        )
    }
}

// ============================================================ شیت استودیو چارت

@Composable
fun ChartStudioSheet(
    selected: List<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var filter by remember { mutableStateOf("همه") }
    val cats = listOf("همه") + chartCatalog.map { it.cat }.distinct()
    val list = chartCatalog.filter { filter == "همه" || it.cat == filter }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.surface.copy(alpha = 0.97f))
                .border(1.dp, extras.hairline, RoundedCornerShape(24.dp))
                .padding(16.dp)
                .heightIn(max = 560.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // سربرگ: نشان سه‌بعدی + تعداد چارت‌های روی داشبورد
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(scheme.primary.copy(alpha = 0.30f), scheme.primary.copy(alpha = 0.10f))
                            )
                        )
                        .border(1.dp, scheme.primary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(MrIcons.ChartPlus, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("استودیو چارت", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        selected.size.toString() + " چارت روی نمای کلی — انتخاب شما، همیشه قابل تغییر",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            // فیلتر دسته‌ها
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                cats.forEach { c ->
                    val sel = filter == c
                    Text(
                        c,
                        style = MaterialTheme.typography.labelMedium,
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
                            .clickable { filter = c; SoundFx.soft() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
            // فهرست چارت‌ها
            list.forEach { def ->
                val on = def.id in selected
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(15.dp))
                        .background(if (on) scheme.primary.copy(alpha = 0.08f) else extras.glass)
                        .border(
                            1.dp,
                            if (on) scheme.primary.copy(alpha = 0.45f) else extras.hairline,
                            RoundedCornerShape(15.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                ) {
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(scheme.primary.copy(alpha = 0.28f), scheme.primary.copy(alpha = 0.09f))
                                )
                            )
                            .border(1.dp, scheme.primary.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(def.icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(14.dp))
                    }
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text(def.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(def.desc, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, maxLines = 1)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (on) "افزوده ✓" else "افزودن",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (on) scheme.onSurface else scheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (on) Brush.verticalGradient(
                                    listOf(scheme.primary.copy(alpha = 0.30f), scheme.primary.copy(alpha = 0.14f))
                                ) else SolidColorSafe(scheme.surfaceVariant.copy(alpha = 0.4f))
                            )
                            .border(
                                1.dp,
                                if (on) scheme.primary.copy(alpha = 0.5f) else extras.hairline,
                                RoundedCornerShape(50),
                            )
                            .clickable { onToggle(def.id) }
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            MrPillButton(
                label = "تمام شد",
                onClick = onDismiss,
                icon = MrIcons.Close,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

/** پس‌زمینه ساده برای pill — جدا تا امضای گرادیانی فقط برای حالت روشن بماند */
private fun SolidColorSafe(c: Color): Brush = Brush.linearGradient(listOf(c, c))
