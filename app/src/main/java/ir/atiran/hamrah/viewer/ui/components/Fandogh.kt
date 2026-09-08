package ir.atiran.hamrah.viewer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.utils.SoundFx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/** حالت‌های چهره فندق — شخصیت چندلایه: شیطنت، ناز، بغض، همدردی، فکر */
enum class FandoghMood { Happy, Wink, Sulk, Cry, Think }

/**
 * شخصیت «فندق» 🌰 — دستیار جدید M•REPORT
 *
 * فندق بامزه‌تر از پسته است: بدن گرد قهوه‌ای با کلاهک نمدی روشن،
 * لپ‌های خیلی بزرگ، ابروهای بانمک و پاپیون هم‌رنگ تم. حالت‌های چهره:
 * خوشحال، چشمک (زبان بیرون!)، بغضِ لجباز، گریهٔ همدردی و فکر کردن.
 * زنده است: بالا-پایین می‌پرد، هر ازگاهی چشمک می‌زند و به لمس واکنش نشان می‌دهد.
 */
@Composable
fun Fandogh(
    size: Dp = 56.dp,
    modifier: Modifier = Modifier,
    bobbing: Boolean = true,
    mood: FandoghMood = FandoghMood.Happy,
    /** رنگ اکسسوری — هماهنگ با تم انتخابی کاربر */
    themeTint: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
) {
    val tr = rememberInfiniteTransition(label = "fandogh")
    val t by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "fandBob",
    )
    val blinkPhase by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing)),
        label = "fandBlink",
    )
    val blink = if (blinkPhase < 0.07f) 0.12f else 1f

    // چشمک خودبه‌خودی — فندق همیشه زنده است
    var wink by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = when {
            wink -> 1.12f
            mood == FandoghMood.Sulk -> 0.97f
            else -> 1f
        },
        animationSpec = tween(220),
        label = "fandScale",
    )
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        while (true) {
            delay(4600L)
            wink = true
            delay(430L)
            wink = false
        }
    }

    val bobPx = if (bobbing) 0.045f else 0f
    val showWink = wink || mood == FandoghMood.Wink

    androidx.compose.foundation.layout.Box(
        modifier
            .graphicsLayer {
                translationY = sin(t * 2f * PI.toFloat()) * size.toPx() * bobPx
                scaleX = scale
                scaleY = scale
                // بغض: کمی کج می‌ایستد — لجباز معروف!
                rotationZ = if (mood == FandoghMood.Sulk) -6f else 0f
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    SoundFx.soft()
                    scope.launch {
                        wink = true
                        delay(500L)
                        wink = false
                    }
                }
            },
    ) {
        Canvas(Modifier.size(size)) {
            val u = this.size.minDimension / 100f
            fun x(v: Float) = v * u
            fun y(v: Float) = v * u

            // ---------- بدن گرد فندق (قهوه‌ای گرم با گرادیان)
            val body = Path().apply {
                moveTo(x(50f), y(10f))
                cubicTo(x(78f), y(12f), x(90f), y(36f), x(88f), y(60f))
                cubicTo(x(86f), y(84f), x(70f), y(94f), x(50f), y(94f))
                cubicTo(x(30f), y(94f), x(14f), y(84f), x(12f), y(60f))
                cubicTo(x(10f), y(36f), x(22f), y(12f), x(50f), y(10f))
                close()
            }
            drawPath(
                body,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFC79263), Color(0xFFA9713F), Color(0xFF8C5830)),
                    startY = y(10f), endY = y(94f),
                ),
            )
            drawPath(body, color = Color(0xFF6E4322), style = Stroke(1.7f * u))

            // برق نور روی بدن — عمق با نور
            drawOval(
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                ),
                topLeft = Offset(x(26f), y(16f)),
                size = Size(x(30f), x(26f)),
            )

            // ---------- کلاهک نمدی روشن با لبهٔ موج‌دار + دمگل
            val cap = Path().apply {
                moveTo(x(30f), y(26f))
                cubicTo(x(32f), y(8f), x(46f), y(2f), x(52f), y(2f))
                cubicTo(x(64f), y(3f), x(72f), y(14f), x(71f), y(26f))
                // لبه موج‌دار کلاهک
                quadraticBezierTo(x(65f), y(21f), x(59f), y(26f))
                quadraticBezierTo(x(53f), y(21f), x(47f), y(26f))
                quadraticBezierTo(x(41f), y(21f), x(35f), y(26f))
                close()
            }
            drawPath(
                cap,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFEBD3A8), Color(0xFFD3B381)),
                    startY = y(2f), endY = y(26f),
                ),
            )
            drawPath(cap, color = Color(0xFFB08D55), style = Stroke(1.2f * u))
            // دمگل کلاهک
            drawPath(
                Path().apply {
                    moveTo(x(52f), y(3f))
                    quadraticBezierTo(x(56f), y(-1.5f), x(60f), y(1.5f))
                },
                color = Color(0xFF8F6D3E),
                style = Stroke(1.6f * u, cap = StrokeCap.Round),
            )

            // ---------- لپ‌های خیلی بزرگ (امضای فندق)
            drawCircle(Color(0xFFE58A63).copy(alpha = 0.5f), radius = x(7.2f), center = Offset(x(31f), y(60f)))
            drawCircle(Color(0xFFE58A63).copy(alpha = 0.5f), radius = x(7.2f), center = Offset(x(69f), y(60f)))

            // ---------- چشم‌ها
            fun eye(cx: Float, winkThis: Boolean) {
                if (winkThis) {
                    drawArc(
                        color = Color(0xFF352412),
                        startAngle = 20f, sweepAngle = 140f, useCenter = false,
                        topLeft = Offset(x(cx - 4.8f), y(45.2f)),
                        size = Size(x(9.6f), x(7.4f)),
                        style = Stroke(1.9f * u, cap = StrokeCap.Round),
                    )
                } else {
                    drawOval(
                        color = Color(0xFFFDF6E9),
                        topLeft = Offset(x(cx - 4.6f), y(48.2f - 4.6f * blink)),
                        size = Size(x(9.2f), x(9.2f) * blink.coerceAtLeast(0.12f)),
                    )
                    if (blink > 0.5f) {
                        // مردمک — در حالت فکر به بالا-راست نگاه می‌کند
                        val lookUp = if (mood == FandoghMood.Think) 1.4f else 0f
                        val lookSide = if (mood == FandoghMood.Think) 1.2f else 0f
                        drawCircle(
                            color = Color(0xFF352412),
                            radius = x(2.4f),
                            center = Offset(x(cx + lookSide), y(49f - lookUp)),
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.9f),
                            radius = x(0.9f),
                            center = Offset(x(cx - 0.9f + lookSide), y(48f - lookUp)),
                        )
                    }
                }
            }
            // حالت گریه: چشم‌ها براق و اشک زیرشان
            eye(39f, showWink)
            eye(61f, false)
            if (mood == FandoghMood.Cry) {
                // اشک‌ها — دو قطره زیر چشم‌ها
                listOf(36.5f to 58.5f, 63.5f to 58.5f).forEach { (tx, ty) ->
                    drawOval(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF9CD5F2).copy(alpha = 0.95f), Color(0xFF5FB0DC).copy(alpha = 0.65f)),
                        ),
                        topLeft = Offset(x(tx - 1.6f), y(ty)),
                        size = Size(x(3.2f), x(5.4f)),
                    )
                }
            }

            // ---------- ابروها — بانمک و حالت‌دار
            val browLift = when {
                showWink -> 3.4f
                mood == FandoghMood.Sulk -> -1.8f
                else -> 0f
            }
            // بغض: ابروها از وسط پایین (اخم بانمک)
            val tilt = if (mood == FandoghMood.Sulk) 1.6f else 0f
            drawPath(
                Path().apply {
                    moveTo(x(34.4f), y(43.5f - browLift + tilt))
                    quadraticBezierTo(x(38.6f), y(40.6f - browLift + tilt), x(43f), y(43.5f - browLift))
                },
                color = Color(0xFF352412),
                style = Stroke(1.7f * u, cap = StrokeCap.Round),
            )
            drawPath(
                Path().apply {
                    moveTo(x(57f), y(43.5f - browLift))
                    quadraticBezierTo(x(61.4f), y(40.6f - browLift + tilt), x(65.6f), y(43.5f - browLift + tilt))
                },
                color = Color(0xFF352412),
                style = Stroke(1.7f * u, cap = StrokeCap.Round),
            )

            // ---------- دهان — بر اساس حالت
            when {
                showWink -> {
                    // خندهٔ باز + زبان
                    drawPath(
                        Path().apply {
                            moveTo(x(41.5f), y(56.5f))
                            quadraticBezierTo(x(50f), y(66f), x(58.5f), y(56.5f))
                            close()
                        },
                        color = Color(0xFF352412),
                    )
                    drawPath(
                        Path().apply {
                            moveTo(x(46.4f), y(61f))
                            quadraticBezierTo(x(50f), y(66f), x(53.6f), y(61f))
                            close()
                        },
                        color = Color(0xFFE58A63),
                    )
                }
                mood == FandoghMood.Sulk -> {
                    // بغض — لب جمع‌شده کوچک
                    drawPath(
                        Path().apply {
                            moveTo(x(45f), y(61.5f))
                            quadraticBezierTo(x(50f), y(58.8f), x(55f), y(61.5f))
                        },
                        color = Color(0xFF352412),
                        style = Stroke(2f * u, cap = StrokeCap.Round),
                    )
                }
                mood == FandoghMood.Cry -> {
                    // دهان موجیِ غمگین
                    drawPath(
                        Path().apply {
                            moveTo(x(44f), y(61f))
                            quadraticBezierTo(x(47f), y(58.5f), x(50f), y(61f))
                            quadraticBezierTo(x(53f), y(63.5f), x(56f), y(61f))
                        },
                        color = Color(0xFF352412),
                        style = Stroke(1.9f * u, cap = StrokeCap.Round),
                    )
                }
                mood == FandoghMood.Think -> {
                    // دهان گرد کوچک «هوم...»
                    drawOval(
                        color = Color(0xFF352412),
                        topLeft = Offset(x(47f), y(58f)),
                        size = Size(x(6f), x(7f)),
                    )
                    // سه نقطه فکر کنار کلاهک
                    listOf(Offset(x(75f), y(7f)), Offset(x(80f), y(12f)), Offset(x(75f), y(17f))).forEachIndexed { i, c ->
                        drawCircle(
                            Color(0xFF6E4322).copy(alpha = 0.45f + 0.2f * i),
                            radius = x(1.2f),
                            center = c,
                        )
                    }
                }
                else -> {
                    // لبخند همیشگی
                    drawArc(
                        color = Color(0xFF352412),
                        startAngle = 15f, sweepAngle = 150f, useCenter = false,
                        topLeft = Offset(x(43f), y(53.5f)),
                        size = Size(x(14f), x(12f)),
                        style = Stroke(2f * u, cap = StrokeCap.Round),
                    )
                }
            }

            // ---------- پاپیون هم‌رنگ تم — پایین بدن
            val bow = Path().apply {
                moveTo(x(50f), y(84f))
                lineTo(x(41.5f), y(79.2f))
                quadraticBezierTo(x(39.5f), y(84f), x(41.5f), y(88.8f))
                close()
                moveTo(x(50f), y(84f))
                lineTo(x(58.5f), y(79.2f))
                quadraticBezierTo(x(60.5f), y(84f), x(58.5f), y(88.8f))
                close()
            }
            drawPath(bow, color = themeTint.copy(alpha = 0.92f))
            drawPath(bow, color = Color.Black.copy(alpha = 0.10f), style = Stroke(0.7f * u))
            drawCircle(themeTint, radius = x(2f), center = Offset(x(50f), y(84f)))
            drawCircle(Color.White.copy(alpha = 0.55f), radius = x(0.75f), center = Offset(x(49.2f), y(83f)))

            // برق کوچک روی لپ — جذابیت سه‌بعدی
            rotate(-18f, pivot = Offset(x(31f), y(60f))) {
                drawOval(
                    Color.White.copy(alpha = 0.35f),
                    topLeft = Offset(x(28f), y(57.6f)),
                    size = Size(x(6f), x(3f)),
                )
            }
        }
    }
}
