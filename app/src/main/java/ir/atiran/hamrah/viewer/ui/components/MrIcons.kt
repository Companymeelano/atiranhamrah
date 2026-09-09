package ir.atiran.hamrah.viewer.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * ست آیکون اختصاصی M•REPORT — نه آیکون آماده.
 *
 * زبان یکسان در همه سیستم:
 *  - خط ۱.۵ (نه ۲ پررنگ آماده متریال)
 *  - گوشه‌های گرد (StrokeCap/Join = Round)
 *  - هندسه مینیمال
 *  - امضای برند: نقطه پر (•) تکرارشونده در آیکون‌ها — همان نقطه M•REPORT
 */
private fun mrIcon(
    name: String,
    strokes: List<String> = emptyList(),
    fills: List<String> = emptyList(),
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    strokes.forEach { p ->
        addPath(
            pathData = addPathNodes(p),
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
    fills.forEach { p ->
        addPath(pathData = addPathNodes(p), fill = SolidColor(Color.Black))
    }
}.build()

object MrIcons {

    /** نمای کلی — گرید چهارخانه با گوشه‌های گرد */
    val Overview: ImageVector = mrIcon(
        name = "MROverview",
        strokes = listOf(
            // چهار کاشی ۷.۵×۷.۵ با شعاع ۲
            "M5,3 h3.5 a2,2 0 0 1 2,2 v3.5 a2,2 0 0 1 -2,2 h-3.5 a2,2 0 0 1 -2,-2 v-3.5 a2,2 0 0 1 2,-2 z" +
                "M15.5,3 h3.5 a2,2 0 0 1 2,2 v3.5 a2,2 0 0 1 -2,2 h-3.5 a2,2 0 0 1 -2,-2 v-3.5 a2,2 0 0 1 2,-2 z" +
                "M5,13.5 h3.5 a2,2 0 0 1 2,2 v3.5 a2,2 0 0 1 -2,2 h-3.5 a2,2 0 0 1 -2,-2 v-3.5 a2,2 0 0 1 2,-2 z" +
                "M15.5,13.5 h3.5 a2,2 0 0 1 2,2 v3.5 a2,2 0 0 1 -2,2 h-3.5 a2,2 0 0 1 -2,-2 v-3.5 a2,2 0 0 1 2,-2 z",
        ),
    )

    /** مشتریان — یک شخص + نقطه برند به‌عنوان «مشتری دیگر» */
    val Customers: ImageVector = mrIcon(
        name = "MRCustomers",
        strokes = listOf(
            "M9.5,4.75 a3.75,3.75 0 1,0 0,7.5 a3.75,3.75 0 1,0 0,-7.5",
            "M3.25,20 a6.25,5.25 0 0 1 12.5,0",
        ),
        fills = listOf(
            "M17.75,6.75 a1.75,1.75 0 1,0 0,3.5 a1.75,1.75 0 1,0 0,-3.5",
        ),
    )

    /** کالاها — مکعب سه‌بعدی (هم‌خانواده ستون‌های سه‌بعدی چارت) */
    val Products: ImageVector = mrIcon(
        name = "MRProducts",
        strokes = listOf(
            "M12,2.9 L20.2,7.45 v9.1 L12,21.1 L3.8,16.55 v-9.1 z",
            "M3.8,7.45 L12,12 L20.2,7.45 M12,12 v9.1",
        ),
    )

    /** گزارش‌ها — سند با دو خط + نقطه برند به‌جای خط سوم */
    val Reports: ImageVector = mrIcon(
        name = "MRReports",
        strokes = listOf(
            "M7,2.75 h10 a2,2 0 0 1 2,2 v14.5 a2,2 0 0 1 -2,2 h-10 a2,2 0 0 1 -2,-2 v-14.5 a2,2 0 0 1 2,-2 z",
            "M8.5,8 h7 M8.5,11.75 h7",
        ),
        fills = listOf(
            "M12,14.75 a1.25,1.25 0 1,0 0,2.5 a1.25,1.25 0 1,0 0,-2.5",
        ),
    )

    /** هشدارها — مثلث آرام با نقطه (نه علامت تعجبران تیز) */
    val Alerts: ImageVector = mrIcon(
        name = "MRAlerts",
        strokes = listOf(
            "M12,4 L20.75,19.75 h-17.5 z",
        ),
        fills = listOf(
            "M12,14 a1.5,1.5 0 1,0 0,3 a1.5,1.5 0 1,0 0,-3",
        ),
    )

    /** اعلان‌ها — زنگ آرام با گوی‌های گرد */
    val Notifications: ImageVector = mrIcon(
        name = "MRNotifications",
        strokes = listOf(
            "M6,16.75 h12 M12,4 a5.5,5.5 0 0 1 5.5,5.5 c0,3 1.25,4.75 2.5,7.25 h-16 c1.25,-2.5 2.5,-4.25 2.5,-7.25 a5.5,5.5 0 0 1 5.5,-5.5 z",
        ),
        fills = listOf(
            "M12,18.25 a1.5,1.5 0 1,0 0,3 a1.5,1.5 0 1,0 0,-3",
        ),
    )

    /** جستجو — ذره‌بین مینیمال */
    val Search: ImageVector = mrIcon(
        name = "MRSearch",
        strokes = listOf(
            "M10.25,4 a6.25,6.25 0 1,0 0,12.5 a6.25,6.25 0 1,0 0,-12.5",
            "M15,15 L20.5,20.5",
        ),
    )

    /** تنظیمات — سه خط با گوی (tune) */
    val Settings: ImageVector = mrIcon(
        name = "MRSettings",
        strokes = listOf(
            "M4,7.25 h16 M4,12 h16 M4,16.75 h16",
        ),
        fills = listOf(
            "M8.75,5.25 a2,2 0 1,0 0,4 a2,2 0 1,0 0,-4",
            "M15.25,10 a2,2 0 1,0 0,4 a2,2 0 1,0 0,-4",
            "M6.75,14.75 a2,2 0 1,0 0,4 a2,2 0 1,0 0,-4",
        ),
    )

    /** همگام‌سازی — دو قوس چرخه با نقطه‌های برند در انتها */
    val Sync: ImageVector = mrIcon(
        name = "MRSync",
        strokes = listOf(
            "M12,5.25 A6.75,6.75 0 0 1 18.75,12",
            "M12,18.75 A6.75,6.75 0 0 1 5.25,12",
        ),
        fills = listOf(
            "M18.75,10.35 a1.65,1.65 0 1 0 0,3.3 a1.65,1.65 0 1 0 0,-3.3",
            "M5.25,10.35 a1.65,1.65 0 1 0 0,3.3 a1.65,1.65 0 1 0 0,-3.3",
        ),
    )

    /** PDF — سند با گوشه تاخورده و نقطه برند */
    val Pdf: ImageVector = mrIcon(
        name = "MRPdf",
        strokes = listOf(
            "M7,3.75 h7 l4,4 v11.75 a1.5,1.5 0 0 1 -1.5,1.5 h-9.5 a1.5,1.5 0 0 1 -1.5,-1.5 v-14.25 a1.5,1.5 0 0 1 1.5,-1.5 z",
            "M14,3.75 v4 h4",
        ),
        fills = listOf(
            "M12,13.4 a1.3,1.3 0 1,0 0,2.6 a1.3,1.3 0 1,0 0,-2.6",
        ),
    )

    /** Excel — جدول شبکه‌ای */
    val Excel: ImageVector = mrIcon(
        name = "MRExcel",
        strokes = listOf(
            "M5.5,5 h13 a1.5,1.5 0 0 1 1.5,1.5 v11 a1.5,1.5 0 0 1 -1.5,1.5 h-13 a1.5,1.5 0 0 1 -1.5,-1.5 v-11 a1.5,1.5 0 0 1 1.5,-1.5 z",
            "M4,9.75 h16 M9.75,5 v14",
        ),
    )

    /** Word — سند با خطوط متن */
    val Word: ImageVector = mrIcon(
        name = "MRWord",
        strokes = listOf(
            "M7,3.75 h10 a1.5,1.5 0 0 1 1.5,1.5 v13.5 a1.5,1.5 0 0 1 -1.5,1.5 h-10 a1.5,1.5 0 0 1 -1.5,-1.5 v-13.5 a1.5,1.5 0 0 1 1.5,-1.5 z",
            "M9,9 h6 M9,12.5 h6 M9,16 h3.5",
        ),
    )

    /** چاپ — پرینتر با کاغذ */
    val Print: ImageVector = mrIcon(
        name = "MRPrint",
        strokes = listOf(
            "M7.5,8 V4.5 h9 V8",
            "M6,8 h12 a1.75,1.75 0 0 1 1.75,1.75 v4.75 h-15.5 v-4.75 a1.75,1.75 0 0 1 1.75,-1.75 z",
            "M8.25,14.5 h7.5 v5 h-7.5 z",
        ),
    )

    /** اشتراک — سه نقطه برند به هم متصل (طبق طرح اصلی) */
    val Share: ImageVector = mrIcon(
        name = "MRShare",
        strokes = listOf(
            "M10.9,8.6 L7.6,14.4 M13.1,8.6 L16.4,14.4",
        ),
        fills = listOf(
            "M12,4.9 a1.9,1.9 0 1,0 0,3.8 a1.9,1.9 0 1,0 0,-3.8",
            "M6.2,14.7 a1.9,1.9 0 1,0 0,3.8 a1.9,1.9 0 1,0 0,-3.8",
            "M17.8,14.7 a1.9,1.9 0 1,0 0,3.8 a1.9,1.9 0 1,0 0,-3.8",
        ),
    )

    /** یادآور — ساعت با عقربه و نقطه مرکز */
    val Reminder: ImageVector = mrIcon(
        name = "MRReminder",
        strokes = listOf(
            "M12,4.75 a7.25,7.25 0 1,0 0,14.5 a7.25,7.25 0 1,0 0,-14.5",
            "M12,9 v3.2 l2.6,1.7",
        ),
        fills = listOf(
            "M12,11.15 a0.85,0.85 0 1,0 0,1.7 a0.85,0.85 0 1,0 0,-1.7",
        ),
    )

    /** پیام — پاکت با نقطه برند */
    val Message: ImageVector = mrIcon(
        name = "MRMessage",
        strokes = listOf(
            "M4.75,6.5 h14.5 a1.5,1.5 0 0 1 1.5,1.5 v8 a1.5,1.5 0 0 1 -1.5,1.5 h-14.5 a1.5,1.5 0 0 1 -1.5,-1.5 v-8 a1.5,1.5 0 0 1 1.5,-1.5 z",
            "M4.6,7.6 l7.4,5.4 7.4,-5.4",
        ),
        fills = listOf(
            "M12,15.4 a1.15,1.15 0 1,0 0,2.3 a1.15,1.15 0 1,0 0,-2.3",
        ),
    )

    /** تماس — گوشی تلفن با خطوط یکنواخت برند */
    val Call: ImageVector = mrIcon(
        name = "MRCall",
        strokes = listOf(
            "M8.4,4.2 c-1.3,-0.5 -2.7,0.3 -2.9,1.7 c-0.7,5.9 3.6,12.3 11.2,13.5 c1.4,0.2 2.6,-0.9 2.6,-2.3 v-1.7 c0,-1 -0.7,-1.9 -1.7,-2.1 l-2.3,-0.5 c-0.8,-0.2 -1.6,0.1 -2.1,0.7 l-0.7,0.9 c-1.9,-1.1 -3.5,-2.7 -4.6,-4.6 l0.9,-0.7 c0.6,-0.5 0.9,-1.3 0.7,-2.1 l-0.5,-2.3 c-0.2,-1 -1.1,-1.7 -2.1,-1.7 z",
        ),
    )

    /** نشان/الگو — روبان نشان‌ها */
    val Bookmark: ImageVector = mrIcon(
        name = "MRBookmark",
        strokes = listOf(
            "M7.5,4.25 h9 a1.25,1.25 0 0 1 1.25,1.25 v14 l-5.75,-3.5 -5.75,3.5 v-14 a1.25,1.25 0 0 1 1.25,-1.25 z",
        ),
        fills = listOf(
            "M12,9.5 a1.2,1.2 0 1,0 0,2.4 a1.2,1.2 0 1,0 0,-2.4",
        ),
    )

    /** فیلتر — قیف با نقطه برند */
    val Filter: ImageVector = mrIcon(
        name = "MRFilter",
        strokes = listOf(
            "M5,6 h14 M7.5,6 l3.2,4.6 v5.6 l2.6,-1.6 v-4 l3.2,-4.6",
        ),
        fills = listOf(
            "M12,14.7 a1.2,1.2 0 1,0 0,2.4 a1.2,1.2 0 1,0 0,-2.4",
        ),
    )

    /** هوش مصنوعی — جرقه چهارپر با نقطه برند */
    val Spark: ImageVector = mrIcon(
        name = "MRSpark",
        strokes = listOf(
            "M12,3.4 L13.85,10.15 L20.6,12 L13.85,13.85 L12,20.6 L10.15,13.85 L3.4,12 L10.15,10.15 Z",
        ),
        fills = listOf(
            "M17.8,4.4 a1.25,1.25 0 1,0 0,2.5 a1.25,1.25 0 1,0 0,-2.5",
        ),
    )

    /** ارسال — کاغذ پرنده */
    val Send: ImageVector = mrIcon(
        name = "MRSend",
        strokes = listOf(
            "M20.2,4.3 L4.2,10.8 L10.6,13.1 L12.9,19.6 Z M10.6,13.1 L20.2,4.3",
        ),
    )

    /** بستن — ضربدر گرد */
    val Close: ImageVector = mrIcon(
        name = "MRClose",
        strokes = listOf(
            "M7.2,7.2 L16.8,16.8 M16.8,7.2 L7.2,16.8",
        ),
    )

    /** شخصیت بصری — جواهر/منشور با وجه‌های بریده */
    val Theme: ImageVector = mrIcon(
        name = "MRTheme",
        strokes = listOf(
            "M12,3.6 L18.8,9 L12,20.4 L5.2,9 z M5.2,9 h13.6 M12,3.6 L9.2,9 l2.8,11.4 M12,3.6 L14.8,9 l-2.8,11.4",
        ),
    )

    /** تجربه رابط — اکولایزر با میله‌های گرد */
    val Waves: ImageVector = mrIcon(
        name = "MRWaves",
        strokes = listOf(
            "M5.5,9.75 v4.5 M9.75,7.5 v9 M14,8.5 v7 M18.25,10.25 v3.5",
        ),
    )

    /** مطالبات — سکه با علامت وصول */
    val Receivables: ImageVector = mrIcon(
        name = "MRReceivables",
        strokes = listOf(
            "M12,4 a8,8 0 1,0 0,16 a8,8 0 1,0 0,-16",
            "M8.75,12.25 l2.25,2.25 l4.25,-4.75",
        ),
    )

    /** چک — سند با خط امضا و نقطه برند */
    val Checks: ImageVector = mrIcon(
        name = "MRChecks",
        strokes = listOf(
            "M6.75,6.75 h10.5 a1.6,1.6 0 0 1 1.6,1.6 v7.3 a1.6,1.6 0 0 1 -1.6,1.6 h-10.5 a1.6,1.6 0 0 1 -1.6,-1.6 v-7.3 a1.6,1.6 0 0 1 1.6,-1.6 z",
            "M8.5,14.75 h4.5",
        ),
        fills = listOf(
            "M15.6,13.9 a1.1,1.1 0 1,0 0,2.2 a1.1,1.1 0 1,0 0,-2.2",
        ),
    )

    /** روند — خط صعودی با نوک گرد + نقطه برند در مبدأ */
    val Trend: ImageVector = mrIcon(
        name = "MRTrend",
        strokes = listOf(
            "M3.5,17.5 L9.5,11.25 L13.25,15 L20.5,7.75",
            "M15.5,7.75 h5 v5",
        ),
        fills = listOf(
            "M3.5,16 a1.5,1.5 0 1,0 0,3 a1.5,1.5 0 1,0 0,-3",
        ),
    )

    /** خزانه — بانک با سقف مثلثی، ستون‌ها و نقطه برند روی گنبد */
    val Bank: ImageVector = mrIcon(
        name = "MRBank",
        strokes = listOf(
            "M4.75,10.75 L12,5.5 L19.25,10.75",
            "M6.5,10.75 v6 M12,10.75 v6 M17.5,10.75 v6",
            "M4.75,19.25 h14.5",
        ),
        fills = listOf(
            "M12,2.9 a1.1,1.1 0 1,0 0,2.2 a1.1,1.1 0 1,0 0,-2.2",
        ),
    )

    /** پرداختی — کیف پول با کارت بیرون‌زده و قفلک برند */
    val Wallet: ImageVector = mrIcon(
        name = "MRWallet",
        strokes = listOf(
            "M4.9,7.4 h11.4 a1.8,1.8 0 0 1 1.8,1.8 v5.6 a1.8,1.8 0 0 1 -1.8,1.8 h-11.4 a1.8,1.8 0 0 1 -1.8,-1.8 v-5.6 a1.8,1.8 0 0 1 1.8,-1.8 z",
            "M3.1,10.6 h10.4",
        ),
        fills = listOf(
            "M15.9,11.9 a1.2,1.2 0 1,0 0,2.4 a1.2,1.2 0 1,0 0,-2.4",
        ),
    )

    /** چارت‌سازی — ستون‌های نمودار با علامت افزودن و نقطه برند */
    val ChartPlus: ImageVector = mrIcon(
        name = "MRChartPlus",
        strokes = listOf(
            "M4,17.75 h12.75",
            "M6,14.25 v3.5 M10.25,10.25 v7.5 M14.5,12.75 v5",
            "M17.25,6.25 h4.5 M19.5,4 v4.5",
        ),
        fills = listOf(
            "M9.4,9.1 a0.85,0.85 0 1,0 1.7,0 a0.85,0.85 0 1,0 -1.7,0",
        ),
    )
}
