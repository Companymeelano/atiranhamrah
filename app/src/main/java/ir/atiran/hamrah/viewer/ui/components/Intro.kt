package ir.atiran.hamrah.viewer.ui.components

/**
 * انیمیشن ورود M•REPORT — «فندق و گونی آجیل»
 *
 * صحنه زنده: فندق گونیِ پر از آجیل را می‌کشد و حواسش نیست که آجیل‌ها
 * سه‌بعدی از گونی بیرون می‌ریزند؛ وسط راه می‌ایستد، سرش را می‌خاراند،
 * به کاربر نگاه می‌کند، چشمک جذابی می‌زند و وارد برنامه می‌شود.
 * لمس صفحه = رد کردن انیمیشن.
 */
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import ir.atiran.hamrah.viewer.utils.SoundFx
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun FandoghIntro(onDone: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    // خط زمانی کل صحنه: ۰ → ۱ در ۶.۸ ثانیه
    val t = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        SoundFx.soft()
        t.animateTo(1f, tween(6800, easing = LinearEasing))
        delay(350)
        onDone()
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
        // نورهای محیطی هماهنگ با تم — عمق با نور
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
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

            val tv = t.value
            val groundY = h * 0.74f

            // زمین نورانی
            drawOval(
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.10f), Color.Transparent)
                ),
                topLeft = Offset(w * 0.08f, groundY),
                size = Size(w * 0.84f, h * 0.10f),
            )

            // ---------- جای‌گذاری فندق و گونی
            // فندق از راست به چپ گونی را می‌کشد (فاز ۰)، بعد می‌ایستد
            val pullT = (tv / 0.55f).coerceIn(0f, 1f)
            val ease = pullT * pullT * (3f - 2f * pullT) // smoothstep
            val bodyX = w * (0.68f - 0.40f * ease)
            val bodyY = groundY - h * 0.115f

            // گونی پشت فندق (سمت راست) — با طناب به فندق
            val sackX = bodyX + w * 0.155f
            val sackY = groundY - h * 0.085f
            val sackW = w * 0.21f
            val sackH = h * 0.155f
            val sackTilt = -10f + sin(tv * 2f * PI.toFloat()) * 2.5f

            rotate(sackTilt, pivot = Offset(sackX, sackY)) {
                // بدنه گونی — پارچهٔ کنفی با گرادیان
                val sack = Path().apply {
                    moveTo(sackX - sackW * 0.42f, sackY - sackH * 0.42f)
                    cubicTo(
                        sackX - sackW * 0.62f, sackY - sackH * 0.1f,
                        sackX - sackW * 0.55f, sackY + sackH * 0.45f,
                        sackX - sackW * 0.18f, sackY + sackH * 0.5f,
                    )
                    cubicTo(
                        sackX + sackW * 0.25f, sackY + sackH * 0.55f,
                        sackX + sackW * 0.5f, sackY + sackH * 0.2f,
                        sackX + sackW * 0.42f, sackY - sackH * 0.2f,
                    )
                    cubicTo(
                        sackX + sackW * 0.3f, sackY - sackH * 0.5f,
                        sackX - sackW * 0.1f, sackY - sackH * 0.55f,
                        sackX - sackW * 0.42f, sackY - sackH * 0.42f,
                    )
                    close()
                }
                drawPath(
                    sack,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFC9A86B), Color(0xFFA8863F), Color(0xFF8A6B33)),
                        startY = sackY - sackH * 0.5f, endY = sackY + sackH * 0.5f,
                    ),
                )
                drawPath(sack, color = Color(0xFF6E5327), style = Stroke(1.6f))
                // گرهٔ گونی
                drawCircle(Color(0xFF8A6B33), radius = sackW * 0.09f, center = Offset(sackX - sackW * 0.30f, sackY - sackH * 0.42f))
                // بوی آجیل؟ نه — چند آجیل توی گونی از دهانه پیداست
                drawCircle(Color(0xFFB98A5A), radius = sackW * 0.07f, center = Offset(sackX - sackW * 0.05f, sackY - sackH * 0.30f))
                drawCircle(Color(0xFF96633A), radius = sackW * 0.06f, center = Offset(sackX + sackW * 0.12f, sackY - sackH * 0.26f))
            }

            // طناب — از دست فندق به گونی
            drawPath(
                Path().apply {
                    moveTo(bodyX + w * 0.028f, bodyY + h * 0.055f)
                    quadraticBezierTo(
                        bodyX + w * 0.09f, bodyY + h * 0.075f,
                        sackX - sackW * 0.38f, sackY - sackH * 0.1f,
                    )
                },
                color = Color(0xFF7A5C2E),
                style = Stroke(2.2f, cap = StrokeCap.Round),
            )

            // ---------- آجیل‌های سه‌بعدی که از گونی می‌ریزند (حواس فندق نیست!)
            if (tv < 0.62f) {
                val nutColors = listOf(
                    Color(0xFFC79263), Color(0xFF96633A), Color(0xFFB98A5A),
                    Color(0xFF8C5830), Color(0xFFAD7B4B), Color(0xFFC79263),
                )
                for (i in 0 until 6) {
                    val seed = tv * 1.6f + i * 0.36f
                    val np = seed % 1f
                    val originX = sackX - sackW * 0.34f
                    val originY = sackY - sackH * 0.20f
                    // پرتاب سهموی به چپ-پایین
                    val nx = originX - w * 0.10f * np - (i % 3) * w * 0.018f
                    val ny = originY + (h * 0.16f) * np * np + h * 0.02f * np
                    val alpha = (1f - np * 0.7f).coerceIn(0.25f, 1f)
                    val r = w * 0.014f
                    rotate(np * 320f + i * 40f, pivot = Offset(nx, ny)) {
                        drawOval(
                            brush = Brush.radialGradient(
                                listOf(nutColors[i].copy(alpha = alpha), nutColors[(i + 2) % 6].copy(alpha = alpha * 0.85f)),
                                center = Offset(nx - r * 0.3f, ny - r * 0.3f),
                                radius = r * 1.4f,
                            ),
                            topLeft = Offset(nx - r, ny - r * 0.82f),
                            size = Size(r * 2f, r * 1.64f),
                        )
                        // هایلایت آجیل — سه‌بعدی
                        drawCircle(
                            Color.White.copy(alpha = 0.35f * alpha),
                            radius = r * 0.22f,
                            center = Offset(nx - r * 0.32f, ny - r * 0.38f),
                        )
                    }
                }
                // آجیل‌های ریخته روی زمین
                listOf(
                    Offset(sackX - sackW * 0.62f, groundY - 4f),
                    Offset(sackX - sackW * 0.85f, groundY - 2f),
                    Offset(sackX - sackW * 1.05f, groundY - 5f),
                ).forEachIndexed { i, c ->
                    drawOval(
                        brush = Brush.radialGradient(
                            listOf(nutColors[i], nutColors[(i + 3) % 6]),
                            center = Offset(c.x - 3f, c.y - 3f),
                            radius = w * 0.016f,
                        ),
                        topLeft = Offset(c.x - w * 0.013f, c.y - w * 0.011f),
                        size = Size(w * 0.026f, w * 0.022f),
                    )
                }
            }

            // ---------- علامت سؤال در فاز خاراندن سر
            if (tv >= 0.55f && tv < 0.78f) {
                val qAlpha = if (tv < 0.72f) 0.85f else (1f - (tv - 0.72f) / 0.06f).coerceIn(0f, 1f)
                val bob = sin(tv * 9f) * 5f
                val qx = bodyX - w * 0.035f
                val qy = bodyY - h * 0.20f + bob
                drawArc(
                    color = Color(0xFF6E4322).copy(alpha = qAlpha),
                    startAngle = 200f, sweepAngle = 260f, useCenter = false,
                    topLeft = Offset(qx - w * 0.016f, qy - w * 0.020f),
                    size = Size(w * 0.032f, w * 0.032f),
                    style = Stroke(2.4f, cap = StrokeCap.Round),
                )
                drawCircle(
                    Color(0xFF6E4322).copy(alpha = qAlpha),
                    radius = 2.4f,
                    center = Offset(qx, qy + w * 0.026f),
                )
            }
        }

        // ---------- خود فندق — روی Canvas، با موقعیت و حالت فاز
        val tv = t.value
        val mood = when {
            tv < 0.55f -> FandoghMood.Happy       // در حال کشیدن
            tv < 0.78f -> FandoghMood.Think       // سرش را می‌خاراند
            else -> FandoghMood.Wink              // چشمک جذاب
        }
        val wFraction = 0.68f - 0.40f * (tv / 0.55f).coerceIn(0f, 1f).let { p -> p * p * (3f - 2f * p) }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val bodyX = maxWidth * wFraction
            val bodyY = maxHeight * 0.74f - maxHeight * 0.115f
            val step = if (tv < 0.55f) sin(tv * 16f * PI.toFloat()) * 3f else 0f
            val scratchTilt = if (tv >= 0.55f && tv < 0.78f) sin(tv * 10f * PI.toFloat()) * 7f else 0f
            Box(
                Modifier
                    .absoluteOffset(
                        x = bodyX - maxWidth * 0.055f,
                        y = bodyY - maxHeight * 0.055f + step.dp,
                    )
                    .graphicsLayerTilt(scratchTilt)
            ) {
                Fandogh(
                    size = maxWidth * 0.24f,
                    bobbing = tv < 0.55f,
                    mood = mood,
                    themeTint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                )
            }
        }

        // ---------- تیتر و متن پایانی
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp),
        ) {
            Text(
                "M•REPORT",
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.horizontalGradient(extras.brand),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                ),
            )
            Spacer(Modifier.height(4.dp))
            MrSubtitle("Intelligent Reporting Experience")
        }

        // پیام فندق در فاز چشمک
        AnimatedVisibility(
            visible = tv >= 0.78f,
            enter = fadeIn(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(scheme.surface.copy(alpha = 0.92f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(scheme.primary),
                )
                Text(
                    "فندق رسید! بزن بریم 🌰",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
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
}

/** کمکی: چرخش ملایم فندق هنگام خاراندن سر */
private fun Modifier.graphicsLayerTilt(deg: Float): Modifier =
    this.graphicsLayer { rotationZ = deg }
