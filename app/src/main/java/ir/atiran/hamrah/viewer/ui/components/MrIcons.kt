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
}
