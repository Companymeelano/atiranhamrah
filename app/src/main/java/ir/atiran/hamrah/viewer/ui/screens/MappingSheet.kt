package ir.atiran.hamrah.viewer.ui.screens

/**
 * شیت «اتصال جداول سرور» — نگاشت تک‌به‌تک بخش‌های M•REPORT به جداول
 * واقعی دیتابیس: مشتریان، کالاها، فروش، چک‌ها و بانک‌ها؛ با تشخیص
 * خودکار، جستجوی زنده جدول و پیش‌نمایش تعداد رکورد — هماهنگ با تم.
 */
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ir.atiran.hamrah.viewer.data.TableHeuristics
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.MrIcons
import ir.atiran.hamrah.viewer.ui.components.MrPillButton
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import ir.atiran.hamrah.viewer.utils.SoundFx

/** بخش‌های قابل اتصال — id، برچسب فارسی، آیکون اختصاصی */
private val mapSections: List<Triple<String, String, ImageVector>> = listOf(
    Triple("customers", "مشتریان", MrIcons.Customers),
    Triple("products", "کالاها", MrIcons.Products),
    Triple("invoices", "فروش و فاکتورها", MrIcons.Trend),
    Triple("checks", "چک‌ها", MrIcons.Checks),
    Triple("banks", "بانک‌ها و حساب‌ها", MrIcons.Bank),
)

private fun sectionLabel(id: String): String = mapSections.firstOrNull { it.first == id }?.second ?: id

@Composable
fun MappingSheet(vm: AppViewModel, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val map by vm.sectionMap.collectAsState()
    val overview = vm.dbOverview
    var pickerFor by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.surface.copy(alpha = 0.97f))
                .border(1.dp, extras.hairline, RoundedCornerShape(24.dp))
                .padding(16.dp)
                .heightIn(max = 600.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // سربرگ: نشان سه‌بعدی اتصال
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
                    Icon(MrIcons.Bank, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("اتصال جداول سرور", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "هر بخش M•REPORT به کدام جدول سرور وصل شود؟",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }

            // تشخیص خودکار از روی نام جداول سرور
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(extras.goldGradient))
                    .clickable {
                        if (overview != null) {
                            vm.setSectionMap(TableHeuristics.detectMap(overview.tables))
                            SoundFx.success()
                        }
                    }
                    .padding(vertical = 11.dp),
            ) {
                Icon(MrIcons.Spark, contentDescription = null, tint = extras.goldOn, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (overview != null) "تشخیص خودکار جداول" else "در حال خواندن جداول سرور...",
                    color = extras.goldOn,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                )
            }

            // بخش‌ها — انتخاب جدول برای هرکدام
            mapSections.forEach { (id, label, icon) ->
                val ref = map.tableFor(id)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(15.dp))
                        .background(if (ref != null) scheme.primary.copy(alpha = 0.08f) else extras.glass)
                        .border(
                            1.dp,
                            if (ref != null) scheme.primary.copy(alpha = 0.45f) else extras.hairline,
                            RoundedCornerShape(15.dp),
                        )
                        .clickable { pickerFor = id; SoundFx.soft() }
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
                        Icon(icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(14.dp))
                    }
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(
                            ref ?: "لمس برای انتخاب جدول...",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (ref != null) scheme.primary else scheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    if (ref != null) {
                        Box(
                            Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(scheme.error.copy(alpha = 0.12f))
                                .border(1.dp, scheme.error.copy(alpha = 0.4f), CircleShape)
                                .clickable { vm.updateSection(id, null); SoundFx.soft() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(MrIcons.Close, contentDescription = "قطع اتصال", tint = scheme.error, modifier = Modifier.size(12.dp))
                        }
                    } else {
                        Text(
                            "انتخاب",
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Text(
                "اتصال فقط-خواندنی است — M•REPORT هیچ داده‌ای را در سرور تغییر نمی‌دهد. نقش ستون‌ها (نام، مبلغ، تاریخ، کد) به‌صورت هوشمند تشخیص داده می‌شود.",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(2.dp))
            MrPillButton(
                label = "تمام شد",
                onClick = onDismiss,
                icon = MrIcons.Close,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }

    pickerFor?.let { id ->
        TablePickerDialog(vm, id, onDismiss = { pickerFor = null })
    }
}

/** انتخابگر جدول — جستجوی زنده در جداول واقعی سرور + تعداد رکورد */
@Composable
private fun TablePickerDialog(vm: AppViewModel, section: String, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val tables = vm.dbOverview?.tables ?: emptyList()
    var q by remember { mutableStateOf("") }
    val filtered = tables.filter { q.isBlank() || (it.schema + "." + it.name).contains(q, true) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.surface.copy(alpha = 0.97f))
                .border(1.dp, extras.hairline, RoundedCornerShape(24.dp))
                .padding(14.dp)
                .heightIn(max = 620.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "انتخاب جدول برای «" + sectionLabel(section) + "»",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            OutlinedTextField(
                value = q,
                onValueChange = { q = it },
                placeholder = { Text("جستجوی جدول...") },
                singleLine = true,
                leadingIcon = { Icon(MrIcons.Search, contentDescription = null, tint = scheme.primary) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            LazyColumn(
                Modifier.heightIn(max = 430.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(filtered) { t ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(13.dp))
                            .background(extras.glass)
                            .border(1.dp, extras.hairline, RoundedCornerShape(13.dp))
                            .clickable {
                                vm.updateSection(section, t.schema + "." + t.name)
                                SoundFx.soft()
                                onDismiss()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        Box(
                            Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(scheme.primary.copy(alpha = 0.26f), scheme.primary.copy(alpha = 0.08f))
                                    )
                                )
                                .border(1.dp, scheme.primary.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(MrIcons.Overview, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(12.dp))
                        }
                        Spacer(Modifier.width(9.dp))
                        Text(
                            t.schema + "." + t.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                        )
                        Text(
                            TableHeuristics.faDigits(t.rows.toString()) + " رکورد",
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (filtered.isEmpty()) {
                Text(
                    if (tables.isEmpty()) "هنوز فهرست جداول دریافت نشده — دوباره باز کنید" else "جدولی با این نام یافت نشد",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }
        }
    }
}
