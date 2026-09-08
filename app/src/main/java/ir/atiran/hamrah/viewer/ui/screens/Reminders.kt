package ir.atiran.hamrah.viewer.ui.screens

/**
 * یادآور جدید M•REPORT — هشدار شخصی بر پایه تقویم هجری شمسی
 *
 * کاربر برای خودش یادآور می‌سازد: موضوع (مشتری، کالا، چک، بانک، سایر)،
 * متن آزاد و تاریخ شمسی با انتخابگر سال/ماه/روز. یادآور ذخیره می‌شود،
 * در صندوق اعلان‌ها بالا می‌آید و مثل بقیه اعلان‌ها قابل «انجام‌شدن» است.
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.MrIcons
import ir.atiran.hamrah.viewer.ui.components.MrPillButton
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import ir.atiran.hamrah.viewer.utils.Jalali
import ir.atiran.hamrah.viewer.utils.SoundFx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddReminderSheet(vm: AppViewModel, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var text by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("مشتری") }
    val cats = listOf("مشتری", "کالا", "چک", "بانک", "سایر")
    val today = remember { Jalali.today() }
    var jy by remember { mutableStateOf(today.first) }
    var jm by remember { mutableStateOf(today.second) }
    var jd by remember { mutableStateOf(today.third) }
    val scope = rememberCoroutineScope()
    var saved by remember { mutableStateOf(false) }
    val maxD = Jalali.monthLen(jy, jm)
    val day = jd.coerceIn(1, maxD)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.surface.copy(alpha = 0.97f))
                .border(1.dp, extras.hairline, RoundedCornerShape(24.dp))
                .padding(16.dp)
                .heightIn(max = 580.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // سربرگ: نشان سه‌بعدی یادآور
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
                    Icon(MrIcons.Reminder, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("یادآور جدید", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "هشدار شخصی بر پایه تقویم شمسی — همیشه در صندوق اعلان‌ها",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }

            // متن یادآور
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("متن یادآور... مثلاً: تماس با هایپر طلایی برای تسویه") },
                singleLine = true,
                leadingIcon = { Icon(MrIcons.Message, contentDescription = null, tint = scheme.primary) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            // موضوع یادآور
            Text("موضوع", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = scheme.primary)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                cats.forEach { c ->
                    val sel = cat == c
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
                            .clickable { cat = c; SoundFx.soft() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            // تاریخ — تقویم شمسی
            Text(
                "تاریخ یادآور — " + Jalali.format(jy, jm, day),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
            )
            // سال
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(extras.glass)
                        .border(1.dp, extras.hairline, CircleShape)
                        .clickable { if (jy > today.first - 2) { jy -= 1; SoundFx.soft() } },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("−", style = MaterialTheme.typography.titleSmall, color = scheme.primary, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    Jalali.fa(jy.toString()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = scheme.onSurface,
                )
                Spacer(Modifier.width(14.dp))
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(extras.glass)
                        .border(1.dp, extras.hairline, CircleShape)
                        .clickable { if (jy < today.first + 5) { jy += 1; SoundFx.soft() } },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", style = MaterialTheme.typography.titleSmall, color = scheme.primary, fontWeight = FontWeight.Black)
                }
            }
            // ماه
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Jalali.monthNames.forEachIndexed { i, name ->
                    val sel = jm == i + 1
                    Text(
                        name,
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
                            .clickable { jm = i + 1; SoundFx.soft() }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }
            // روز — شبکه ۷ ستونه
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                (1..maxD).chunked(7).forEach { week ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        week.forEach { d ->
                            val sel = day == d
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (sel) Brush.verticalGradient(
                                            listOf(scheme.primary.copy(alpha = 0.32f), scheme.primary.copy(alpha = 0.14f))
                                        ) else Brush.verticalGradient(
                                            listOf(scheme.surfaceVariant.copy(alpha = 0.30f), scheme.surfaceVariant.copy(alpha = 0.30f))
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        if (sel) scheme.primary.copy(alpha = 0.55f) else extras.hairline,
                                        CircleShape,
                                    )
                                    .clickable { jd = d; SoundFx.soft() },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    Jalali.fa(d.toString()),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (sel) scheme.primary else scheme.onSurfaceVariant,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                )
                            }
                        }
                        repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            Spacer(Modifier.height(2.dp))
            if (saved) {
                Text(
                    "یادآور ذخیره شد ✓",
                    style = MaterialTheme.typography.labelMedium,
                    color = extras.positive,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            MrPillButton(
                label = if (saved) "ذخیره شد ✓" else "ذخیره یادآور",
                onClick = {
                    if (!saved && text.isNotBlank()) {
                        saved = true
                        SoundFx.success()
                        vm.addReminder(cat, text, jy, jm, day)
                        scope.launch {
                            delay(1300)
                            onDismiss()
                        }
                    }
                },
                icon = MrIcons.Bookmark,
                tint = scheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}
