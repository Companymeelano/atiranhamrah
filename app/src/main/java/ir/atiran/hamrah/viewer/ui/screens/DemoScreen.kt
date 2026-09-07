package ir.atiran.hamrah.viewer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.LuxSection
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import kotlinx.coroutines.delay

// ============================================================ داده نمونه
private val tabs = listOf("فروش و درآمد", "مشتریان", "محصولات", "انبار و مالی", "عملکرد تیم")

private val weekDays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
private val weekSales = listOf(42f, 58f, 51f, 73f, 66f, 88f, 95f)
private val months = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور")
private val monthlySales = listOf(180f, 210f, 195f, 260f, 240f, 310f)
private val monthlyTarget = listOf(200f, 200f, 220f, 240f, 260f, 300f)

private data class Slice(val label: String, val value: Float)
private val productShare = listOf(
    Slice("پسته", 38f), Slice("بادام", 24f), Slice("گردو", 18f),
    Slice("فندق", 12f), Slice("کشمش و خرما", 8f),
)
private val customerSegments = listOf(
    Slice("پلاتینی", 12f), Slice("طلایی", 22f), Slice("نقره‌ای", 31f), Slice("برنزی", 35f),
)

private val radarAxes = listOf("کیفیت", "سرعت", "قیمت", "تنوع", "بسته‌بندی", "پشتیبانی")
private val radarValues = listOf(0.92f, 0.78f, 0.85f, 0.70f, 0.88f, 0.81f)

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

private data class FunnelStep(val label: String, val count: Int, val fraction: Float)
private val funnel = listOf(
    FunnelStep("بازدید و تماس", 4820, 1f),
    FunnelStep("استعلام قیمت", 2340, 0.62f),
    FunnelStep("ثبت سفارش", 1120, 0.42f),
    FunnelStep("پرداخت موفق", 940, 0.35f),
)

private data class Bar(val label: String, val value: String, val fraction: Float, val icon: ImageVector? = null)
private val cities = listOf(
    Bar("تهران", "۸۶۴ م", 1f, Icons.Filled.LocationOn),
    Bar("اصفهان", "۴۳۲ م", 0.5f, Icons.Filled.LocationOn),
    Bar("مشهد", "۳۱۸ م", 0.37f, Icons.Filled.LocationOn),
    Bar("شیراز", "۲۶۴ م", 0.31f, Icons.Filled.LocationOn),
    Bar("تبریز", "۱۹۲ م", 0.22f, Icons.Filled.LocationOn),
)
private val categoryRevenue = listOf(
    Bar("پسته", "۵۴۲ م", 1f), Bar("بادام", "۳۴۱ م", 0.63f),
    Bar("گردو", "۲۵۶ م", 0.47f), Bar("فندق", "۱۷۰ م", 0.31f), Bar("خشکبار پلویی", "۱۱۶ م", 0.21f),
)
private val aging = listOf(
    Bar("جاری (۰ تا ۳۰ روز)", "۴۲۰ م", 1f), Bar("۳۱ تا ۶۰ روز", "۲۶۵ م", 0.63f),
    Bar("۶۱ تا ۹۰ روز", "۱۳۸ م", 0.33f), Bar("بیش از ۹۰ روز", "۶۲ م", 0.15f),
)
private val warehouses = listOf(
    Bar("انبار مرکزی", "۲٫۸ میلیارد", 1f, Icons.Filled.Inventory2),
    Bar("انبار شهرک صنعتی", "۱٫۴ میلیارد", 0.5f, Icons.Filled.Inventory2),
    Bar("شعبه بازار", "۶۸۰ م", 0.24f, Icons.Filled.Inventory2),
)
private val reps = listOf(
    Bar("علی رضایی", "۱۱۲٪", 1f, Icons.Filled.Person),
    Bar("مریم احمدی", "۹۸٪", 0.87f, Icons.Filled.Person),
    Bar("رضا کریمی", "۸۷٪", 0.78f, Icons.Filled.Person),
    Bar("سارا موسوی", "۷۶٪", 0.68f, Icons.Filled.Person),
)

private data class Bullet(val label: String, val percent: Int)
private val loyalty = listOf(
    Bullet("رضایت مشتریان", 94), Bullet("تکرار خرید", 78),
    Bullet("شاخص NPS", 68), Bullet("نرخ نگهداشت مشتری", 85),
)
private val operations = listOf(
    Bullet("تحویل به‌موقع", 92), Bullet("پاسخگویی پشتیبانی", 96),
    Bullet("دقت موجودی", 89), Bullet("کیفیت بسته‌بندی", 94),
)

private data class Kpi(
    val title: String, val target: Float, val delta: String,
    val icon: ImageVector, val up: Boolean, val spark: List<Int>,
)
private val kpis = listOf(
    Kpi("فروش امروز (میلیون)", 124.5f, "+۱۲٪", Icons.Filled.Paid, true, listOf(40, 55, 48, 70, 62, 85, 95)),
    Kpi("سفارش‌های فعال", 348f, "+۸٪", Icons.Filled.ShoppingBag, true, listOf(50, 60, 55, 75, 70, 80, 92)),
    Kpi("مشتریان فعال", 2120f, "+۵٪", Icons.Filled.Group, true, listOf(45, 50, 65, 60, 75, 85, 90)),
    Kpi("میانگین سبد (هزار تومان)", 845f, "+۹٪", Icons.Filled.TrendingUp, true, listOf(35, 48, 52, 66, 74, 82, 95)),
)

private data class MiniStat(val title: String, val value: String, val icon: ImageVector)
private val customerStats = listOf(
    MiniStat("مشتری فعال", "۲٬۱۲۰", Icons.Filled.Group),
    MiniStat("جدید این ماه", "۱۸۶", Icons.Filled.Person),
    MiniStat("بازگشتی", "۷۴۲", Icons.Filled.Verified),
    MiniStat("مشتریان VIP", "۱۱۸", Icons.Filled.WorkspacePremium),
    MiniStat("میانگین سفارش", "۳٫۲ در ماه", Icons.Filled.ShoppingBag),
    MiniStat("نرخ ریزش", "۴٪-", Icons.Filled.Star),
)
private val financeStats = listOf(
    MiniStat("ارزش موجودی", "۴٫۹ میلیارد", Icons.Filled.Inventory2),
    MiniStat("مطالبات", "۸۸۵ م", Icons.Filled.Paid),
    MiniStat("چک در جریان", "۳۲۴ م", Icons.Filled.CheckCircle),
    MiniStat("خالص نقدینگی", "۱٫۲ میلیارد", Icons.Filled.TrendingUp),
    MiniStat("حاشیه سود", "۲۳٪", Icons.Filled.Insights),
    MiniStat("بدهی تامین‌کنندگان", "۵۴۰ م", Icons.Filled.Warning),
)

private val cashIn = listOf(210f, 260f, 240f, 310f, 290f, 350f)
private val cashOut = listOf(150f, 190f, 175f, 220f, 210f, 245f)
private val newCust = listOf(120f, 145f, 132f, 168f, 158f, 186f)
private val returning = listOf(420f, 460f, 445f, 520f, 495f, 560f)

private data class Cust(val name: String, val amount: Float, val share: Float)
private val topCustomers = listOf(
    Cust("هایپر طلایی", 312f, 1f), Cust("فروشگاه زرین", 268f, 0.86f),
    Cust("پخش نگین", 204f, 0.65f), Cust("مارکت الماس", 176f, 0.56f),
    Cust("سوپرمارکت بهار", 122f, 0.39f),
)

private data class TopProduct(val name: String, val amount: Float, val share: Float)
private val topProducts = listOf(
    TopProduct("پسته اکبری درجه‌یک", 482f, 1f),
    TopProduct("بادام ممتاز شور", 316f, 0.66f),
    TopProduct("گردو تازه کاغذی", 245f, 0.51f),
    TopProduct("مغز پسته خام", 198f, 0.41f),
    TopProduct("کشمش تیزابی ممتاز", 134f, 0.28f),
)

private data class StockAlert(val name: String, val remaining: Int, val fraction: Float, val critical: Boolean)
private val stockAlerts = listOf(
    StockAlert("پسته فندقی ۵۰۰ گرمی", 8, 0.08f, true),
    StockAlert("بادام زمینی شور", 14, 0.14f, true),
    StockAlert("مغز گردو درجه ۲", 34, 0.34f, false),
    StockAlert("میکس لوکس ۷۵۰ گرمی", 52, 0.52f, false),
)

private data class Order(val no: String, val customer: String, val amount: String, val done: Boolean)
private val orders = listOf(
    Order("۱۴۲۵۸", "هایپر طلایی", "۴۸٬۲۰۰٬۰۰۰", true),
    Order("۱۴۲۵۷", "فروشگاه زرین", "۳۱٬۷۰۰٬۰۰۰", true),
    Order("۱۴۲۵۶", "پخش نگین", "۱۹٬۴۰۰٬۰۰۰", false),
    Order("۱۴۲۵۵", "مارکت الماس", "۱۲٬۹۰۰٬۰۰۰", true),
    Order("۱۴۲۵۴", "سوپرمارکت بهار", "۸٬۳۰۰٬۰۰۰", false),
)

// ============================================================ صفحه اصلی
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScreen(vm: AppViewModel) {
    BackHandler { vm.closeDemo() }
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var tab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("نمایش دمو — داشبورد مدیریتی") },
            navigationIcon = {
                IconButton(onClick = { vm.closeDemo() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                }
            },
        )

        // تب‌های دسته‌بندی
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tabs.forEachIndexed { i, t ->
                DemoTab(label = t, selected = tab == i) { tab = i }
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
                0 -> SalesTab()
                1 -> CustomersTab()
                2 -> ProductsTab()
                3 -> FinanceTab()
                else -> PerformanceTab()
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

// ============================================================ تب‌ها
@Composable
private fun DemoTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) Brush.horizontalGradient(extras.goldGradient)
                else scheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else scheme.outlineVariant.copy(alpha = 0.7f),
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

// ============================================================ تب ۱: فروش
@Composable
private fun SalesTab() {
    val g1 = stagger(0)
    val g2 = stagger(150)
    val g3 = stagger(300)
    val g4 = stagger(450)
    val g5 = stagger(600)
    val g6 = stagger(750)

    HeroBanner(g1)
    LuxSection("تحقق هدف فروش سالانه") {
        Gauge(percent = 0.87f, progress = g2, centerTop = "۸۷٪", centerBottom = "۳٫۱ از ۳٫۶ میلیارد تومان")
    }
    LuxSection("شاخص‌های کلیدی امروز") {
        KpiGrid(startDelay = 250)
    }
    LuxSection("فروش هفتگی (میلیون تومان)") {
        BarChart(weekSales, weekDays, progress = g3)
    }
    LuxSection("روند فروش و هدف ماهانه") {
        LineChart(monthlySales, months, progress = g4, values2 = monthlyTarget, color2 = null)
    }
    LuxSection("هدف و عملکرد فصلی (میلیون)") {
        QuarterChart(quarters, progress = g5)
    }
    LuxSection("قیف تبدیل فروش") {
        FunnelChart(funnel, progress = g6)
    }
}

// ============================================================ تب ۲: مشتریان
@Composable
private fun CustomersTab() {
    val g1 = stagger(0)
    val g2 = stagger(150)
    val g3 = stagger(300)
    val g4 = stagger(450)

    LuxSection("نمای کلی مشتریان") {
        MiniStatGrid(customerStats)
    }
    LuxSection("سطح‌بندی مشتریان") {
        DonutChart(customerSegments, progress = g1)
    }
    LuxSection("مشتریان برتر ماه") {
        TopCustomers(topCustomers)
    }
    LuxSection("پراکندگی جغرافیایی فروش") {
        HBarList(cities, progress = g2)
    }
    LuxSection("شاخص‌های وفاداری") {
        BulletList(loyalty, progress = g3)
    }
    LuxSection("مشتریان جدید در برابر بازگشتی") {
        LineChart(newCust, months, progress = g4, values2 = returning, color2 = null)
    }
}

// ============================================================ تب ۳: محصولات
@Composable
private fun ProductsTab() {
    val g1 = stagger(0)
    val g2 = stagger(150)
    val g3 = stagger(300)
    val g4 = stagger(450)

    LuxSection("سهم فروش دسته‌ها") {
        DonutChart(productShare, progress = g1)
    }
    LuxSection("پرفروش‌ترین محصولات ماه") {
        TopProducts(topProducts)
    }
    LuxSection("درآمد هر دسته (شش‌ماهه)") {
        HBarList(categoryRevenue, progress = g2)
    }
    LuxSection("تحلیل شش‌بعدی برند") {
        RadarChart(radarAxes, radarValues, progress = g3)
    }
    LuxSection("هشدار موجودی محصولات") {
        StockAlerts(stockAlerts)
    }
}

// ============================================================ تب ۴: انبار و مالی
@Composable
private fun FinanceTab() {
    val g1 = stagger(0)
    val g2 = stagger(150)
    val g3 = stagger(300)

    LuxSection("نمای کلی مالی") {
        MiniStatGrid(financeStats)
    }
    LuxSection("جریان نقدی شش‌ماهه (میلیون)") {
        LineChart(cashIn, months, progress = g1, values2 = cashOut, color2 = null)
    }
    LuxSection("سنی مطالبات (میلیون)") {
        HBarList(aging, progress = g2)
    }
    LuxSection("ارزش موجودی انبارها") {
        HBarList(warehouses, progress = g3)
    }
}

// ============================================================ تب ۵: عملکرد
@Composable
private fun PerformanceTab() {
    val g1 = stagger(0)
    val g2 = stagger(150)
    val g3 = stagger(300)

    LuxSection("نقشه حرارتی فعالیت تیم فروش (۱۲ هفته)") {
        Heatmap(heat, progress = g1)
    }
    LuxSection("تحقق هدف نمایندگان فروش") {
        HBarList(reps, progress = g2)
    }
    LuxSection("شاخص‌های عملیاتی") {
        BulletList(operations, progress = g3)
    }
    LuxSection("آخرین سفارش‌ها") {
        OrdersList(orders)
    }
}

// ============================================================ اجزای مشترک
/** انیمیشن پلکانی */
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

// ============================================================ بنر قهرمان
@Composable
private fun HeroBanner(progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val pulse = rememberInfiniteTransition(label = "pulse")
    val dot by pulse.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "dot",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, extras.gold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        scheme.primary.copy(alpha = 0.35f),
                        scheme.surface,
                        scheme.tertiary.copy(alpha = 0.30f),
                    )
                )
            )
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(extras.positive.copy(alpha = dot))
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    "درآمد امروز — به‌روزرسانی زنده",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "+۱۲٪ نسبت به دیروز",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = extras.positive,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(extras.positive.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            Text(
                formatInt((1245000000L * progress)) + " ریال",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = scheme.primary,
            )
            Text(
                "بهترین روز فروش در ۳۰ روز گذشته",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            Sparkline(listOf(40, 52, 47, 68, 60, 82, 95).map { it.toFloat() }, extras.gold, 1f)
        }
    }
}

// ============================================================ گیج
@Composable
private fun Gauge(percent: Float, progress: Float, centerTop: String, centerBottom: String) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(190.dp)) {
            val stroke = 22.dp.toPx()
            val r = (size.minDimension - stroke) / 2
            val topLeft = Offset((size.width - r * 2) / 2, (size.height - r * 2) / 2)
            val arcSize = Size(r * 2, r * 2)
            drawArc(
                color = scheme.surfaceVariant,
                startAngle = 135f, sweepAngle = 270f, useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
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
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(extras.gold, extras.chart[1], extras.gold, extras.gold),
                ),
                startAngle = 135f,
                sweepAngle = 270f * percent * progress,
                useCenter = false,
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

// ============================================================ KPI
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

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
            .background(scheme.surface)
            .padding(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background((if (k.up) extras.positive else scheme.error).copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
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
        drawPath(path, color = color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(color, radius = 3.dp.toPx(), center = pts.last())
    }
}

// ============================================================ مینی‌آمار
@Composable
private fun MiniStatGrid(stats: List<MiniStat>) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stats.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEachIndexed { i, s ->
                    val accent = extras.chart[(stats.indexOf(s) + i) % extras.chart.size]
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .background(scheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(10.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(s.icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                            Text(s.value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                            Text(
                                s.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================ ستونی
@Composable
private fun BarChart(values: List<Float>, labels: List<String>, progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(Modifier.fillMaxWidth().height(160.dp)) {
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
        Box(Modifier.size(140.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 24.dp.toPx()
                val r = (size.minDimension - stroke) / 2
                val topLeft = Offset((size.width - r * 2) / 2, (size.height - r * 2) / 2)
                var start = -90f
                slices.forEachIndexed { i, s ->
                    val sweep = 360f * (s.value / total) * progress
                    drawArc(
                        color = extras.chart[i % extras.chart.size],
                        startAngle = start,
                        sweepAngle = (sweep - 2.5f).coerceAtLeast(0.5f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(r * 2, r * 2),
                        style = Stroke(stroke, cap = StrokeCap.Butt),
                    )
                    start += 360f * (s.value / total)
                }
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
        Canvas(Modifier.size(220.dp)) {
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
            for (ring in 1..4) {
                val frac = ring / 4f
                val grid = Path().apply {
                    for (i in 0 until n) {
                        val p = pt(i, frac)
                        if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                    }
                    close()
                }
                drawPath(grid, color = scheme.outlineVariant.copy(alpha = 0.4f), style = Stroke(1.5f))
            }
            for (i in 0 until n) {
                drawLine(scheme.outlineVariant.copy(alpha = 0.4f), c, pt(i, 1f), 1.5f)
            }
            val poly = Path().apply {
                for (i in 0 until n) {
                    val p = pt(i, values[i] * progress)
                    if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                }
                close()
            }
            drawPath(poly, color = extras.gold.copy(alpha = 0.25f))
            drawPath(poly, color = extras.gold, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
            for (i in 0 until n) {
                drawCircle(extras.gold, radius = 4.dp.toPx(), center = pt(i, values[i] * progress))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            axes.forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    }
}

// ============================================================ خطی (دو سری)
@Composable
private fun LineChart(
    values: List<Float>,
    labels: List<String>,
    progress: Float,
    values2: List<Float>? = null,
    color2: Color? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val secondColor = color2 ?: scheme.tertiary
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (values2 != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Legend(extras.gold, "مقدار واقعی")
                Legend(secondColor, "مقایسه")
            }
        }
        Canvas(Modifier.fillMaxWidth().height(160.dp)) {
            val all = values + (values2 ?: emptyList())
            val maxV = all.max() * 1.2f
            val labelSpace = 22.dp.toPx()
            val w = size.width
            val h = size.height - labelSpace
            val stepX = w / (values.size - 1)
            for (g in 1..3) {
                val y = h * g / 4f
                drawLine(scheme.outlineVariant.copy(alpha = 0.3f), Offset(0f, y), Offset(w, y), 1.dp.toPx())
            }
            fun drawSeries(series: List<Float>, color: Color, fill: Boolean) {
                val pts = series.mapIndexed { i, v -> Offset(i * stepX, h - (v / maxV) * h * progress) }
                if (fill) {
                    val fillPath = Path().apply {
                        moveTo(pts.first().x, h)
                        pts.forEach { lineTo(it.x, it.y) }
                        lineTo(pts.last().x, h)
                        close()
                    }
                    drawPath(fillPath, brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.02f))))
                }
                val line = Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    for (i in 1 until pts.size) {
                        val mid = (pts[i - 1].x + pts[i].x) / 2
                        cubicTo(mid, pts[i - 1].y, mid, pts[i].y, pts[i].x, pts[i].y)
                    }
                }
                drawPath(line, color = color, style = Stroke(if (fill) 3.dp.toPx() else 2.dp.toPx(), cap = StrokeCap.Round))
                if (fill) {
                    pts.forEach {
                        drawCircle(color.copy(alpha = 0.3f), radius = 9.dp.toPx(), center = it)
                        drawCircle(scheme.surface, radius = 5.dp.toPx(), center = it)
                        drawCircle(color, radius = 3.5.dp.toPx(), center = it)
                    }
                }
            }
            values2?.let { drawSeries(it, secondColor, fill = false) }
            drawSeries(values, extras.gold, fill = true)
        }
        Row(Modifier.fillMaxWidth()) {
            labels.forEach {
                Text(it.take(3), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant,
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

// ============================================================ نقشه حرارتی
@Composable
private fun Heatmap(data: List<List<Float>>, progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(Modifier.fillMaxWidth().height(130.dp)) {
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
            Legend(extras.gold, "عملکرد واقعی")
            Legend(scheme.primary, "هدف")
        }
        Canvas(Modifier.fillMaxWidth().height(150.dp)) {
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
                    color = scheme.primary.copy(alpha = 0.35f),
                    topLeft = Offset(x, size.height - labelSpace - hT),
                    size = Size(bw, hT),
                    cornerRadius = CornerRadius(6f, 6f),
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(extras.gold, extras.gold.copy(alpha = 0.6f))),
                    topLeft = Offset(x + bw + 5.dp.toPx(), size.height - labelSpace - hA),
                    size = Size(bw, hA),
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

// ============================================================ قیف فروش
@Composable
private fun FunnelChart(steps: List<FunnelStep>, progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        steps.forEachIndexed { i, s ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(extras.goldGradient)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${i + 1}", color = extras.goldOn, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(s.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text("${s.count}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
                    if (i > 0) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${(steps[i - 1].count.takeIf { it > 0 }?.let { s.count * 100 / it } ?: 0)}٪",
                            style = MaterialTheme.typography.labelSmall,
                            color = extras.positive,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(extras.positive.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(scheme.surfaceVariant.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(s.fraction * progress)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Brush.horizontalGradient(extras.goldGradient))
                    )
                }
            }
        }
    }
}

// ============================================================ نوار افقی
@Composable
private fun HBarList(items: List<Bar>, progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEachIndexed { i, b ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (b.icon != null) {
                    Icon(b.icon!!, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(b.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(b.value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(scheme.surfaceVariant.copy(alpha = 0.6f)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(b.fraction * progress)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(scheme.primary, extras.gold)
                            )
                        ),
                )
            }
        }
    }
}

// ============================================================ فهرست گلوله‌ای
@Composable
private fun BulletList(items: List<Bullet>, progress: Float) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { b ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(b.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text("${b.percent}٪", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(scheme.surfaceVariant.copy(alpha = 0.6f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(b.percent / 100f * progress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Brush.horizontalGradient(extras.goldGradient)),
                    )
                }
            }
        }
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
                    if (i == 0) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                    } else {
                        Text("${i + 1}", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
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
                                .background(
                                    if (i == 0) Brush.horizontalGradient(extras.goldGradient)
                                    else Brush.horizontalGradient(listOf(scheme.primary, scheme.tertiary))
                                )
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text("${c.amount} م", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
            }
        }
    }
}

// ============================================================ پرفروش‌ها
@Composable
private fun TopProducts(products: List<TopProduct>) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        products.forEachIndexed { i, p ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(extras.chart[i % extras.chart.size], extras.chart[i % extras.chart.size].copy(alpha = 0.5f)))),
                    contentAlignment = Alignment.Center,
                ) {
                    if (i == 0) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("${i + 1}", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(p.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(scheme.surfaceVariant)) {
                        Box(
                            Modifier
                                .fillMaxWidth(p.share)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Brush.horizontalGradient(listOf(extras.chart[i % extras.chart.size], scheme.primary)))
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text("${p.amount} م", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
            }
        }
    }
}

// ============================================================ هشدار موجودی
@Composable
private fun StockAlerts(alerts: List<StockAlert>) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        alerts.forEach { a ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        1.dp,
                        (if (a.critical) scheme.error else scheme.outlineVariant).copy(alpha = if (a.critical) 0.6f else 0.65f),
                        RoundedCornerShape(14.dp),
                    )
                    .background((if (a.critical) scheme.error else scheme.surfaceVariant).copy(alpha = if (a.critical) 0.08f else 0.35f))
                    .padding(horizontal = 10.dp, vertical = 9.dp),
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (a.critical) scheme.error else scheme.tertiary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(a.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(scheme.surfaceVariant)) {
                        Box(
                            Modifier
                                .fillMaxWidth(a.fraction)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (a.critical) scheme.error else extras.gold)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text("${a.remaining}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        if (a.critical) "بحرانی" else "کم",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (a.critical) scheme.error else scheme.tertiary,
                    )
                }
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
                    .border(1.dp, scheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .background(scheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 10.dp, vertical = 9.dp),
            ) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background((if (o.done) extras.positive else scheme.tertiary).copy(alpha = 0.18f)),
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
