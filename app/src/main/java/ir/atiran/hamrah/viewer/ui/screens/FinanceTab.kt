package ir.atiran.hamrah.viewer.ui.screens

/**
 * خزانه M•REPORT — مرکز مالی و بانکی
 *
 * همان‌قدر که مشتریان و کالاها مهم‌اند، حسابدار مجموعه به اسناد بانکی
 * نیاز دارد: حساب‌های بانکی، چک‌ها، دریافتی‌ها و پرداختی‌ها — همه با
 * همان زبان بصری بقیه برنامه: کارت شیشه‌ای، گوی سه‌بعدی، رنگ هماهنگ
 * با تم انتخابی کاربر و جستجوی زنده.
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import ir.atiran.hamrah.viewer.ui.components.CalmPulse
import ir.atiran.hamrah.viewer.ui.components.EmptyBox
import ir.atiran.hamrah.viewer.ui.components.GlassCard
import ir.atiran.hamrah.viewer.ui.components.MrIcons
import ir.atiran.hamrah.viewer.ui.components.NumberHero
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import ir.atiran.hamrah.viewer.utils.SoundFx

/** سند خزانه — بانک، چک، دریافتی یا پرداختی */
private data class FinDoc(
    val id: String,
    val cat: String,
    val title: String,
    val party: String,
    val amount: String,
    val date: String,
    val color: Color,
)

private val finDocs = listOf(
    FinDoc("f1", "بانک‌ها", "حساب جاری بانک ملت", "شعبه مرکزی", "۱۸۲٬۰۰۰٬۰۰۰", "همیشه فعال", Color(0xFF74C0FC)),
    FinDoc("f2", "بانک‌ها", "حساب بانک سامان", "شعبه شهرک", "۹۶٬۵۰۰٬۰۰۰", "همیشه فعال", Color(0xFF74C0FC)),
    FinDoc("f3", "بانک‌ها", "حساب بانک پاسارگاد", "شعبه مرکزی", "۱۴۳٬۲۰۰٬۰۰۰", "همیشه فعال", Color(0xFF74C0FC)),
    FinDoc("f4", "چک‌ها", "چک ۷۸۴۵۱۲ — فروشگاه زرین", "دریافتنی · سررسید فردا", "۳۵٬۰۰۰٬۰۰۰", "فردا", Color(0xFFFFA94D)),
    FinDoc("f5", "چک‌ها", "چک ۷۸۴۴۹۸ — هایپر طلایی", "وصول شد", "۲۲٬۰۰۰٬۰۰۰", "۳ روز قبل", Color(0xFF69DB7C)),
    FinDoc("f6", "چک‌ها", "چک ۷۸۴۴۷۶ — پخش نگین", "پاس شده · نیازمند پیگیری", "۱۸٬۴۰۰٬۰۰۰", "۵ روز قبل", Color(0xFFFF6B6B)),
    FinDoc("f7", "دریافتی‌ها", "دریافت ۵۰۲۱ — وصول مطالبات", "هایپر طلایی", "۴۵٬۰۰۰٬۰۰۰", "دیروز", Color(0xFF69DB7C)),
    FinDoc("f8", "دریافتی‌ها", "دریافت ۵۰۱۸ — تسویه فاکتور", "فروشگاه زرین", "۱۲٬۰۰۰٬۰۰۰", "۳ روز قبل", Color(0xFF69DB7C)),
    FinDoc("f9", "پرداختی‌ها", "پرداخت ۳۳۸۷ — خرید پسته", "باغدار یزد", "۶۴٬۰۰۰٬۰۰۰", "دیروز", Color(0xFFFF6B6B)),
    FinDoc("f10", "پرداختی‌ها", "پرداخت ۳۳۸۴ — اجاره دفتر", "مبلغ ماهانه", "۲۸٬۰۰۰٬۰۰۰", "هفته قبل", Color(0xFFFF6B6B)),
)

private val bankDonut = listOf(
    Slice3("ملت", 43f, "۱۸۲م"),
    Slice3("سامان", 23f, "۹۷م"),
    Slice3("پاسارگاد", 34f, "۱۴۳م"),
)

/** آیکون هر دسته مالی — از ست اختصاصی M•REPORT */
private fun finIcon(cat: String): ImageVector = when (cat) {
    "بانک‌ها" -> MrIcons.Bank
    "چک‌ها" -> MrIcons.Checks
    "دریافتی‌ها" -> MrIcons.Receivables
    else -> MrIcons.Wallet
}

// ============================================================ تب خزانه

@Composable
fun FinanceTab() {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var q by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("همه") }
    val cats = listOf("همه", "بانک‌ها", "چک‌ها", "دریافتی‌ها", "پرداختی‌ها")
    val list = finDocs.filter { d ->
        (filter == "همه" || d.cat == filter) &&
            (q.isBlank() || d.title.contains(q, true) || d.party.contains(q, true))
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // سربرگ: نشان سه‌بعدی خزانه
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(
                        Brush.radialGradient(listOf(scheme.primary.copy(alpha = 0.30f), Color.Transparent)),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(27.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(scheme.primary.copy(alpha = 0.32f), scheme.primary.copy(alpha = 0.10f))
                            )
                        )
                        .border(1.dp, scheme.primary.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(MrIcons.Bank, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text("خزانه", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text(
                    "بانک‌ها، چک‌ها، دریافتی‌ها و پرداختی‌ها — برای حسابدار مجموعه",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }

        // جستجوی زنده اسناد مالی — همان‌ند کالاها و مشتریان
        OutlinedTextField(
            value = q,
            onValueChange = { q = it },
            placeholder = { Text("جستجوی سند، بانک یا مشتری...") },
            singleLine = true,
            leadingIcon = { Icon(MrIcons.Search, contentDescription = null, tint = scheme.primary) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
        )

        // کارت خلاصه: موجودی کل بانک‌ها + سهم هر بانک
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "موجودی کل بانک‌ها",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    CalmPulse(extras.positive, dotSize = 7.dp)
                }
                NumberHero("۴۲۱٬۷۰۰٬۰۰۰", unit = "تومان", color = scheme.primary, autoFit = true, fixedHeight = 34.dp)
                StudioDonut(bankDonut, centerText = "۳ بانک", centerSub = "فعال")
            }
        }

        // فیلتر دسته‌های مالی
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            cats.forEach { c ->
                val sel = filter == c
                Text(
                    c,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (sel) extras.goldOn else scheme.onSurfaceVariant,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (sel) Brush.horizontalGradient(extras.goldGradient)
                            else Brush.verticalGradient(
                                listOf(scheme.surfaceVariant.copy(alpha = 0.5f), scheme.surfaceVariant.copy(alpha = 0.5f))
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = if (sel) Color.Transparent else extras.hairline,
                            shape = RoundedCornerShape(50),
                        )
                        .clickable { filter = c; SoundFx.soft() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }

        // اسناد مالی
        if (list.isEmpty()) {
            EmptyBox(
                text = "سند مالی یافت نشد",
                subtitle = "نام دیگری را جستجو کنید",
            )
        }
        list.forEach { d -> FinDocCard(d) }
    }
}

/** کارت سند مالی — گوی سه‌بعدی هماهنگ با دسته + وضعیت رنگی */
@Composable
private fun FinDocCard(d: FinDoc) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 62.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(extras.glass)
            .border(1.dp, extras.hairline, RoundedCornerShape(16.dp))
            .clickable { SoundFx.soft() }
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        // گوی سه‌بعدی دسته سند
        Box(
            Modifier
                .size(38.dp)
                .background(
                    Brush.radialGradient(listOf(d.color.copy(alpha = 0.26f), Color.Transparent)),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(d.color.copy(alpha = 0.30f), d.color.copy(alpha = 0.10f))
                        )
                    )
                    .border(1.dp, d.color.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(finIcon(d.cat), contentDescription = null, tint = d.color, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(d.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(
                d.party + " · " + d.date,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                d.amount,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = d.color,
            )
            Text(
                "تومان",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}
