package ir.atiran.hamrah.viewer.ui.screens

/**
 * یادآور جدید M•REPORT — هشدار شخصی بر پایه تقویم هجری شمسی
 *
 * متن آزاد + موضوع + انتخابگر خلاقانه تاریخ (سال/ماه/روز) و ساعت
 * (گویهای عددی) + تنظیم اطلاع‌رسانی: نوع صدای آلارم (ملایم/معمولی/فوری)
 * و زمان یادآوری قبل از موعد (دقیقه/ساعت/روز) — آلارم واقعی با
 * AlarmManager به صدا درمی‌آید و اعلان اندروید می‌فرستد.
 */
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("مشتری") }
    val cats = listOf("مشتری", "کالا", "چک", "بانک", "سایر")
    val today = remember { Jalali.today() }
    var jy by remember { mutableStateOf(today.first) }
    var jm by remember { mutableStateOf(today.second) }
    var jd by remember { mutableStateOf(today.third) }
    // ساعت آلارم
    var hh by remember { mutableStateOf(9) }
    var mm by remember { mutableStateOf(0) }
    // اطلاع‌رسانی
    var alarmOn by remember { mutableStateOf(true) }
    var sound by remember { mutableStateOf("معمولی") }
    var offsetValue by remember { mutableStateOf(30) }
    var offsetUnit by remember { mutableStateOf("دقیقه") }
    val scope = rememberCoroutineScope()
    var saved by remember { mutableStateOf(false) }
    val maxD = Jalali.monthLen(jy, jm)
    val day = jd.coerceIn(1, maxD)

    // مجوز اعلان (اندروید ۱۳+) — یک‌بار هنگام باز شدن شیت
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.surface.copy(alpha = 0.97f))
                .border(1.dp, extras.hairline, RoundedCornerShape(24.dp))
                .padding(16.dp)
                .heightIn(max = 620.dp)
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
                        "هشدار شخصی با آلارم — بر پایه تقویم شمسی",
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

            // ---------- تاریخ — تقویم شمسی
            Text(
                "تاریخ — " + Jalali.format(jy, jm, day),
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
                OrbStepButton("−") { if (jy > today.first - 2) { jy -= 1; SoundFx.soft() } }
                Spacer(Modifier.width(14.dp))
                Text(
                    Jalali.fa(jy.toString()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = scheme.onSurface,
                )
                Spacer(Modifier.width(14.dp))
                OrbStepButton("+") { if (jy < today.first + 5) { jy += 1; SoundFx.soft() } }
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

            // ---------- ساعت آلارم — گوی‌های عددی
            Text(
                "ساعت آلارم — " + Jalali.fa(
                    (hh.toString()).padStart(2, '0') + ":" + (mm.toString()).padStart(2, '0')
                ),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
            )
            // ساعت ۰ تا ۲۳
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                (0..23).forEach { h ->
                    val sel = hh == h
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (sel) Brush.verticalGradient(extras.goldGradient)
                                else Brush.verticalGradient(
                                    listOf(scheme.surfaceVariant.copy(alpha = 0.32f), scheme.surfaceVariant.copy(alpha = 0.32f))
                                )
                            )
                            .border(
                                1.dp,
                                if (sel) androidx.compose.ui.graphics.Color.Transparent else extras.hairline,
                                CircleShape,
                            )
                            .clickable { hh = h; SoundFx.soft() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            Jalali.fa(h.toString()),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (sel) extras.goldOn else scheme.onSurfaceVariant,
                            fontWeight = if (sel) FontWeight.Black else FontWeight.Medium,
                        )
                    }
                }
            }
            // دقیقه — پنج‌دقیه‌ای
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                (0..55 step 5).forEach { m ->
                    val sel = mm == m
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (sel) Brush.verticalGradient(
                                    listOf(scheme.primary.copy(alpha = 0.32f), scheme.primary.copy(alpha = 0.14f))
                                ) else Brush.verticalGradient(
                                    listOf(scheme.surfaceVariant.copy(alpha = 0.32f), scheme.surfaceVariant.copy(alpha = 0.32f))
                                )
                            )
                            .border(
                                1.dp,
                                if (sel) scheme.primary.copy(alpha = 0.55f) else extras.hairline,
                                CircleShape,
                            )
                            .clickable { mm = m; SoundFx.soft() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            Jalali.fa(m.toString()),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (sel) scheme.primary else scheme.onSurfaceVariant,
                            fontWeight = if (sel) FontWeight.Black else FontWeight.Medium,
                        )
                    }
                }
            }

            // ---------- اطلاع‌رسانی: آلارم + نوع صدا + زمان قبل از موعد
            Text(
                "اطلاع‌رسانی",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
            )
            // آلارم روشن/خاموش
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(extras.glass)
                    .border(1.dp, extras.hairline, RoundedCornerShape(14.dp))
                    .clickable { alarmOn = !alarmOn; SoundFx.soft() }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
            ) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            if (alarmOn) Brush.verticalGradient(
                                listOf(extras.positive.copy(alpha = 0.30f), extras.positive.copy(alpha = 0.10f))
                            ) else Brush.verticalGradient(
                                listOf(scheme.surfaceVariant.copy(alpha = 0.30f), scheme.surfaceVariant.copy(alpha = 0.30f))
                            )
                        )
                        .border(
                            1.dp,
                            if (alarmOn) extras.positive.copy(alpha = 0.5f) else extras.hairline,
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        MrIcons.Notifications,
                        contentDescription = null,
                        tint = if (alarmOn) extras.positive else scheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Spacer(Modifier.width(9.dp))
                Column {
                    Text("آلارم و اعلان", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (alarmOn) "در زمان مقرر صدا و ویبره به صدا درمی‌آید" else "فقط در فهرست اعلان‌ها نمایش داده می‌شود",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            if (alarmOn) {
                // نوع صدای آلارم
                Text("نوع صدای آلارم", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("ملایم" to "یک بوق آرام", "معمولی" to "سه بوق", "فوری" to "آژیر پنج‌باره").forEach { (s, d) ->
                        val sel = sound == s
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (sel) scheme.primary.copy(alpha = 0.14f) else extras.glass
                                )
                                .border(
                                    1.dp,
                                    if (sel) scheme.primary.copy(alpha = 0.45f) else extras.hairline,
                                    RoundedCornerShape(12.dp),
                                )
                                .clickable { sound = s; SoundFx.alert() }
                                .padding(vertical = 7.dp),
                        ) {
                            Text(
                                s,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (sel) scheme.primary else scheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                d,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = scheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }
                // یادآوری قبل از موعد
                Text("یادآوری قبل از موعد", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf(
                        "همان زمان" to (0 to "دقیقه"),
                        "۵ دقیقه قبل" to (5 to "دقیقه"),
                        "۱۵ دقیقه قبل" to (15 to "دقیقه"),
                        "۳۰ دقیقه قبل" to (30 to "دقیقه"),
                        "۱ ساعت قبل" to (1 to "ساعت"),
                        "۲ ساعت قبل" to (2 to "ساعت"),
                        "۱ روز قبل" to (1 to "روز"),
                    ).forEach { (label, ov) ->
                        val sel = offsetValue == ov.first && offsetUnit == ov.second
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (sel) scheme.onSurface else scheme.onSurfaceVariant,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (sel) Brush.verticalGradient(
                                        listOf(scheme.primary.copy(alpha = 0.30f), scheme.primary.copy(alpha = 0.12f))
                                    ) else Brush.verticalGradient(
                                        listOf(scheme.surfaceVariant.copy(alpha = 0.30f), scheme.surfaceVariant.copy(alpha = 0.30f))
                                    )
                                )
                                .border(
                                    1.dp,
                                    if (sel) scheme.primary.copy(alpha = 0.55f) else extras.hairline,
                                    RoundedCornerShape(50),
                                )
                                .clickable { offsetValue = ov.first; offsetUnit = ov.second; SoundFx.soft() }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(2.dp))
            if (saved) {
                Text(
                    "یادآور ذخیره شد ✓" + if (alarmOn) " — آلارم تنظیم شد" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = extras.positive,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                )
            }
            MrPillButton(
                label = if (saved) "ذخیره شد ✓" else "ذخیره یادآور",
                onClick = {
                    if (!saved && text.isNotBlank()) {
                        saved = true
                        SoundFx.success()
                        if (alarmOn) {
                            vm.addReminder(cat, text, jy, jm, day, hh, mm, offsetValue, offsetUnit, sound)
                        } else {
                            vm.addReminder(cat, text, jy, jm, day, hh, mm, 0, "دقیقه", "خاموش")
                        }
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

/** گوی کوچک +/− برای سال */
@Composable
private fun OrbStepButton(sign: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Box(
        Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(extras.glass)
            .border(1.dp, extras.hairline, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            sign,
            style = MaterialTheme.typography.titleSmall,
            color = scheme.primary,
            fontWeight = FontWeight.Black,
        )
    }
}
