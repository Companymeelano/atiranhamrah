package ir.atiran.hamrah.viewer.ui.components

/**
 * فیلم کوتاه ورود M•REPORT — «پسته و شیفت صبحگاهی» 🥜
 *
 * پرده ۱ — خواب عمیق: پسته خروپف می‌کند و «ز ز ز» بالای سرش شناور است.
 * پرده ۲ — آلارم!: ساعت زنگ‌دار از بالا می‌افتد، می‌پرد و «بوق! بوق!» می‌زند؛
 *          پسته با چشم‌های غول‌پیکر از خواب می‌پرد!
 * پرده ۳ — بیرون‌پریدن از پوسته: دو نیمهٔ پوسته به دو طرف پرواز می‌کنند،
 *          غبار بلند می‌شود و پسته فرود می‌آید.
 * پرده ۴ — خوش‌آمدگویی: دست‌ها را تکان می‌دهد، چشمک می‌زند و جملات
 *          خنده‌دار می‌گوید؛ کاغذرنگی پسته‌ای می‌بارد و دکمهٔ «بزن بریم!».
 * لمس صفحه در هر لحظه = رد کردن.
 */
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import ir.atiran.hamrah.viewer.utils.SoundFx
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun PistachioIntro(onDone: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    // خط زمانی کل فیلم: ۰ → ۱ در ۹ ثانیه
    val t = remember { Animatable(0f) }

    // پرده‌ها
    val sleepEnd = 0.16f
    val alarmEnd = 0.34f
    val burstEnd = 0.52f

    LaunchedEffect(Unit) {
        SoundFx.soft()
        delay(1500)
        SoundFx.alert()
        delay(1700)
        SoundFx.alert()
        delay(1900)
        SoundFx.success()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(extras.loginGradient))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onDone(); SoundFx.soft() },
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val cw = maxWidth
            val chh = maxHeight
            val tv = t.value
            val act = when {
                tv < sleepEnd -> 0
                tv < alarmEnd -> 1
                tv < burstEnd -> 2
                else -> 3
            }
            val tA = when (act) {  // پیشرفت داخل پرده (۰..۱)
                0 -> tv / sleepEnd
                1 -> (tv - sleepEnd) / (alarmEnd - sleepEnd)
                2 -> (tv - alarmEnd) / (burstEnd - alarmEnd)
                else -> (tv - burstEnd) / (1f - burstEnd)
            }

            // زمین نورانی
            val groundY = chh * 0.72f
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // نورهای محیطی هماهنگ با تم
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(extras.brand[1].copy(alpha = 0.16f), Color.Transparent)
                    ),
                    radius = w * 0.42f,
                    center = Offset(w * 0.22f, h * 0.20f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(extras.brand.last().copy(alpha = 0.12f), Color.Transparent)
                    ),
                    radius = w * 0.36f,
                    center = Offset(w * 0.85f, h * 0.75f),
                )
                // زمین
                drawOval(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.10f), Color.Transparent)
                    ),
                    topLeft = Offset(w * 0.08f, groundY.toPx()),
                    size = Size(w * 0.84f, h * 0.10f),
                )

                val px = w / 100f // واحد افقی درصدی

                // ---------- پرده ۱: «ز ز ز» شناور
                if (act == 0) {
                    for (i in 0 until 3) {
                        val phase = (tA + i * 0.33f) % 1f
                        val zzY = (groundY - chh * 0.34f - phase * chh * 0.10f).toPx()
                        drawCircle(
                            Color(0xFF6E8F2A).copy(alpha = (1f - phase) * 0.5f),
                            radius = px * (1.1f + i * 0.5f),
                            center = Offset(w * 0.58f + phase * px * 5f, zzY),
                        )
                    }
                }

                // ---------- پرده ۲: ساعت زنگ‌دار
                if (act == 1) {
                    val drop = (tA / 0.45f).coerceIn(0f, 1f)
                    val bounce = if (tA < 0.45f) 0f else sin((tA - 0.45f) * 3f * PI.toFloat()) * (1f - (tA - 0.45f) / 0.55f)
                    val clockY = (groundY - chh * 0.52f * drop - chh * 0.05f * bounce.coerceAtLeast(0f)).toPx()
                    val clockX = w * 0.62f
                    val r = px * 7.5f
                    // زنگ — خطوط تشعشعی
                    if (tA > 0.4f) {
                        val ring = ((tA * 6f) % 1f)
                        for (a in 0 until 8) {
                            val ang = a * PI.toFloat() / 4f + tA * 2f
                            val rr = r * (1.3f + ring * 0.7f)
                            drawCircle(
                                Color(0xFFFFC94D).copy(alpha = (1f - ring) * 0.6f),
                                radius = px * 0.9f,
                                center = Offset(clockX + rr * kotlin.math.cos(ang), clockY - r * 0.6f + rr * sin(ang) * 0.6f),
                            )
                        }
                    }
                    // بدنه ساعت — طلایی سه‌بعدی
                    drawCircle(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFFFE1A0), Color(0xFFF3B94F), Color(0xFFC98A2E)),
                            startY = clockY - r, endY = clockY + r,
                        ),
                        radius = r,
                        center = Offset(clockX, clockY),
                    )
                    drawCircle(Color(0xFF8A5E1E).copy(alpha = 0.8f), radius = r, center = Offset(clockX, clockY), style = Stroke(px * 0.5f))
                    // زنگ‌ها
                    drawCircle(Color(0xFFF3B94F), radius = r * 0.28f, center = Offset(clockX - r * 0.72f, clockY - r * 0.85f))
                    drawCircle(Color(0xFFF3B94F), radius = r * 0.28f, center = Offset(clockX + r * 0.72f, clockY - r * 0.85f))
                    // عقربه‌ها
                    drawLine(Color(0xFF5A3A10), Offset(clockX, clockY), Offset(clockX + r * 0.5f * kotlin.math.cos(tA * 20f), clockY - r * 0.5f), strokeWidth = px * 0.5f, cap = StrokeCap.Round)
                    drawLine(Color(0xFF5A3A10), Offset(clockX, clockY), Offset(clockX + r * 0.7f * kotlin.math.cos(tA * 6f), clockY - r * 0.7f * kotlin.math.sin(tA * 6f)), strokeWidth = px * 0.4f, cap = StrokeCap.Round)
                }

                // ---------- پرده ۳: پرواز نیمه‌های پوسته + غبار
                if (act >= 2) {
                    val fly = tA.coerceIn(0f, 1f)
                    val ease = fly * fly * (3f - 2f * fly)
                    val shellAlpha = (1f - fly * 1.3f).coerceIn(0f, 1f)
                    if (shellAlpha > 0.02f) {
                        // نیمهٔ چپ — پرواز به چپ با چرخش
                        val lx = w * 0.44f - ease * w * 0.42f
                        val ly = (groundY - chh * 0.30f - ease * chh * 0.16f).toPx()
                        rotate(-ease * 40f, pivot = Offset(lx, ly)) {
                            val sh = Path().apply {
                                moveTo(lx, ly - px * 16f)
                                cubicTo(lx - px * 10f, ly - px * 10f, lx - px * 11f, ly + px * 6f, lx - px * 3f, ly + px * 14f)
                                cubicTo(lx - px * 6f, ly + px * 2f, lx - px * 5f, ly - px * 8f, lx + px * 3f, ly - px * 13f)
                                close()
                            }
                            drawPath(sh, brush = Brush.linearGradient(listOf(Color(0xFF7B3E1E), Color(0xFF4A1F0D)).map { it.copy(alpha = shellAlpha) }), )
                            drawPath(sh, color = Color(0xFF33150A).copy(alpha = shellAlpha), style = Stroke(px * 0.4f))
                        }
                        // نیمهٔ راست — پرواز به راست
                        val rx = w * 0.56f + ease * w * 0.42f
                        val ry = (groundY - chh * 0.30f - ease * chh * 0.16f).toPx()
                        rotate(ease * 40f, pivot = Offset(rx, ry)) {
                            val sh = Path().apply {
                                moveTo(rx, ry - px * 16f)
                                cubicTo(rx + px * 10f, ry - px * 10f, rx + px * 11f, ry + px * 6f, rx + px * 3f, ry + px * 14f)
                                cubicTo(rx + px * 6f, ry + px * 2f, rx + px * 5f, ry - px * 8f, rx - px * 3f, ry - px * 13f)
                                close()
                            }
                            drawPath(sh, brush = Brush.linearGradient(listOf(Color(0xFF6B3418), Color(0xFF3F1B0C)).map { it.copy(alpha = shellAlpha) }))
                            drawPath(sh, color = Color(0xFF33150A).copy(alpha = shellAlpha), style = Stroke(px * 0.4f))
                        }
                    }
                    // غبار فرود
                    if (fly < 0.5f) {
                        val da = 1f - fly * 2f
                        for (i in 0 until 6) {
                            val ang = PI.toFloat() * (0.15f + i * 0.14f)
                            val dd = fly * 2f * px * (8f + i * 2f)
                            drawCircle(
                                Color.White.copy(alpha = da * 0.35f),
                                radius = px * (1.2f + fly * 2f),
                                center = Offset(w * 0.5f + dd * kotlin.math.cos(ang), groundY.toPx() - dd * sin(ang) * 0.5f),
                            )
                        }
                    }
                }

                // ---------- پرده ۴: بارش کاغذرنگی پسته‌ای
                if (act == 3) {
                    for (i in 0 until 12) {
                        val phase = (tA * 1.4f + i * 0.083f) % 1f
                        val fx = w * (0.08f + (i * 37 % 84) / 100f * 0.84f) + sin(phase * 3f * PI.toFloat() + i) * px * 3f
                        val fy = phase * h
                        val fr = px * 1.4f
                        drawOval(
                            if (i % 2 == 0) Color(0xFF8FB260).copy(alpha = (1f - phase) * 0.9f)
                            else Color(0xFFD2AE79).copy(alpha = (1f - phase) * 0.9f),
                            topLeft = Offset(fx - fr, fy - fr * 0.75f),
                            size = Size(fr * 2f, fr * 1.5f),
                        )
                    }
                }
            }

            // ---------- خودِ پسته — قهرمان فیلم
            val tA2 = if (act == 1) tA else 0f
            val jump = if (act == 1) sin((tA2 * 1.7f).coerceAtMost(1f) * PI.toFloat()) * chh * 0.10f else 0.dp
            val shakeDp = if (act == 2) (sin(tA * 34f) * (1f - tA) * 10f).dp else 0.dp
            val sizeP = cw * 0.46f
            Pistachio(
                size = sizeP,
                bobbing = act == 0 || act == 3,
                mood = if (act == 3 && tA > 0.55f) PistachioMood.Wink else PistachioMood.Happy,
                eyesClosed = act == 0,
                armWave = act == 3,
                themeTint = scheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = chh * 0.24f + jump - shakeDp),
            )
            // «بوق! بوق!» در پردهٔ آلارم
            if (act == 1 && tA > 0.45f) {
                Text(
                    "بوق! بوق!",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Black),
                    color = Color(0xFFFFC94D),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = chh * 0.66f)
                        .background(Color(0xFF241C10).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                )
            }
        }

        // ---------- تیتر
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 54.dp),
        ) {
            Text(
                "M•REPORT",
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.horizontalGradient(extras.brand),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                ),
            )
            Spacer(Modifier.height(4.dp))
            MrSubtitle("Intelligent Reporting Experience")
        }

        // ---------- پردهٔ ۴: جملات خنده‌دار + دکمه
        if (t.value >= burstEnd) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 118.dp, start = 24.dp, end = 24.dp),
            ) {
                val lines = listOf(
                    "سلام! من پسته‌ام 🥜",
                    "دیشب تا صبح دیتابیس رو چیدم — تو هنوز خوابیدی؟!",
                    "بزن بریم، گزارش‌ها دارن سرد می‌شن! 😂",
                )
                lines.forEachIndexed { i, line ->
                    val shown = t.value >= burstEnd + 0.10f + i * 0.10f
                    AnimatedVisibility(
                        visible = shown,
                        enter = fadeIn() + slideInVertically(tween(260)) { it / 3 },
                    ) {
                        Text(
                            line,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(scheme.surface.copy(alpha = 0.92f))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }

        // دکمهٔ «بزن بریم!» — بعد از جملهٔ آخر
        if (t.value >= 0.80f) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(extras.goldGradient))
                    .clickable { SoundFx.success(); onDone() }
                    .padding(horizontal = 26.dp, vertical = 12.dp),
            ) {
                Box(
                    Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(extras.goldOn.copy(alpha = 0.9f)),
                )
                Text(
                    "بزن بریم! 🚀",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = extras.goldOn,
                )
            }
        }

        Text(
            "لمس برای رد کردن",
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 22.dp),
        )
    }

    // پایان خودکار فیلم
    LaunchedEffect(Unit) {
        t.animateTo(1f, tween(9000, easing = LinearEasing))
        delay(400)
        onDone()
    }
}
