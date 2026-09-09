package ir.atiran.hamrah.viewer.ui.screens

/**
 * مرکز توجه M•REPORT — هشدارهای نیازمند اقدام
 *
 * از این نسخه، هشدارها از تب ناوبری به دکمه اختصاصی بالای صفحه
 * (کنار دکمه‌های تم و تجربه رابط) منتقل شده‌اند: نشان سه‌بعدی با
 * شمارنده عددی هشدارهای جدید. با باز شدن مرکز توجه، هر هشدار
 * «دیده‌شده» می‌شود و از شمارنده کم می‌شود — وضعیت ذخیره می‌ماند.
 */
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.CalmPulse
import ir.atiran.hamrah.viewer.ui.components.GlassCard
import ir.atiran.hamrah.viewer.ui.components.MrIcons
import ir.atiran.hamrah.viewer.ui.components.MrPillButton
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import ir.atiran.hamrah.viewer.utils.SoundFx
import kotlinx.coroutines.delay

/** کارت هشدار مرکز توجه — id برای ذخیره وضعیت دیده‌شدن */
data class AlertCard(
    val id: String,
    val title: String,
    val count: String,
    val desc: String,
    val color: Color,
)

/** هشدارهای نیازمند توجه — داده نمایشی هماهنگ با بقیه برنامه */
val attentionCards = listOf(
    AlertCard("chk_due", "چک سررسید", "۲ مورد", "چک‌هایی که امروز یا فردا سررسید می‌شوند", Color(0xFFFF6B6B)),
    AlertCard("recv_old", "مطالبات", "۸ مشتری", "مطالبات معوق بیش از ۶۰ روز", Color(0xFFFFA94D)),
    AlertCard("cust_follow", "پیگیری مشتری", "۵ مورد", "مشتریان نیازمند تماس پیگیری", Color(0xFFFFD43B)),
)

/** آیکون هر هشدار — از ست اختصاصی M•REPORT */
fun alertIconOf(title: String): ImageVector = when (title) {
    "چک سررسید" -> MrIcons.Checks
    "مطالبات" -> MrIcons.Receivables
    else -> MrIcons.Customers
}

/** تعداد هشدارهای دیده‌نشده — برای شمارنده نشان بالای صفحه */
fun unseenAlertCount(seen: Set<String>): Int = attentionCards.count { it.id !in seen }

// ============================================================ شیت مرکز توجه

@Composable
fun AlertsCenterSheet(
    vm: AppViewModel,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val seen by vm.alertSeen.collectAsState()

    // با باز شدن مرکز توجه، همه هشدارها دیده‌شده می‌شوند → شمارنده نشان صفر می‌شود
    LaunchedEffect(Unit) {
        delay(650)
        vm.markAlertsSeen(attentionCards.map { it.id })
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.surface.copy(alpha = 0.97f))
                .border(1.dp, extras.hairline, RoundedCornerShape(24.dp))
                .padding(18.dp)
                .heightIn(max = 560.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // سربرگ: نشان سه‌بعدی مرکز توجه با هاله نور
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFFFF6B6B).copy(alpha = 0.30f), Color.Transparent)),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFF6B6B).copy(alpha = 0.32f), Color(0xFFFF6B6B).copy(alpha = 0.10f))
                                )
                            )
                            .border(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(MrIcons.Alerts, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(15.dp))
                    }
                    if (vm.experience.motion) {
                        CalmPulse(Color(0xFFFF6B6B), dotSize = 6.dp)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("مرکز توجه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        attentionCards.size.toString() + " مورد نیازمند توجه شماست — با دیدن این بخش، شمارنده نشان تازه‌ها صفر شد",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }

            // کارت‌های هشدار
            attentionCards.forEach { a ->
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val tone = a.color
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
                                    alertIconOf(a.title),
                                    contentDescription = null,
                                    tint = tone,
                                    modifier = Modifier.size(15.dp),
                                )
                            }
                        }
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
                                color = tone,
                            )
                            Text(
                                if (a.id in seen) "دیده شد ✓" else "تازه",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (a.id in seen) scheme.onSurfaceVariant else scheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(2.dp))
            MrPillButton(
                label = "متوجه شدم",
                onClick = { onDismiss(); SoundFx.soft() },
                icon = MrIcons.Bookmark,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}
