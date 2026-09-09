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

/** حالت‌های چهره پسته — شخصیت چندلایه: شیطنت، ناز، بغض، همدردی، فکر */
enum class PistachioMood { Happy, Wink, Sulk, Cry, Think }

/**
 * شخصیت «پسته» 🥜 — دستیار زندهٔ M•REPORT
 *
 * بازسازی دقیق طرح اصلی (همان که در تصویر مرجع و آیکون لانچر است):
 * پوستهٔ بادامی با گرادیان بژ واقعی، شکاف پوسته، برگچهٔ سر، مغز سبز
 * خندان با چشمک تک‌چشمیِ امضا، لپ‌های گیج، دهان خنده با زبان و پاپیون
 * هم‌رنگ تم. زنده است: بالا-پایین می‌پرد، هر ازگاهی چشمک می‌زند،
 * به لمس واکنش نشان می‌دهد و پنج حالت چهره دارد — مثل یک آدم واقعی:
 * خوشحال، چشمک (زبان بیرون!)، بغضِ لجباز، گریهٔ همدردی و فکر کردن.
 */
@Composable
fun Pistachio(
    size: Dp = 56.dp,
    modifier: Modifier = Modifier,
    bobbing: Boolean = true,
    mood: PistachioMood = PistachioMood.Happy,
    /** رنگ پاپیون — هماهنگ با تم انتخابی کاربر */
    themeTint: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
) {
    val tr = rememberInfiniteTransition(label = "pistachio")
    val t by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "pistBob",
    )
    val blinkPhase by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing)),
        label = "pistBlink",
    )
    val blink = if (blinkPhase < 0.07f) 0.12f else 1f

    // چشمک خودبه‌خودی — پسته همیشه زنده است
    var wink by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = when {
            wink -> 1.12f
            mood == PistachioMood.Sulk -> 0.97f
            else -> 1f
        },
        animationSpec = tween(220),
        label = "pistScale",
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
    val showWink = wink || mood == PistachioMood.Wink

    androidx.compose.foundation.layout.Box(
        modifier
            .graphicsLayer {
                translationY = sin(t * 2f * PI.toFloat()) * size.toPx() * bobPx
                scaleX = scale
                scaleY = scale
                // بغض: کمی کج می‌ایستد — لجباز معروف!
                rotationZ = if (mood == PistachioMood.Sulk) -6f else 0f
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

            // ---------- پوستهٔ بادامی — گرادیان بژ واقعی (مثل تصویر مرجع)
            val shell = Path().apply {
                moveTo(x(50f), y(5f))
                cubicTo(x(79f), y(15f), x(89f), y(48f), x(74f), y(75f))
                cubicTo(x(64f), y(91f), x(36f), y(91f), x(26f), y(75f))
                cubicTo(x(11f), y(48f), x(21f), y(15f), x(50f), y(5f))
                close()
            }
            drawPath(
                shell,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFE6CB98), Color(0xFFD2AE79), Color(0xFFB08A55)),
                    startY = y(5f), endY = y(91f),
                ),
            )
            drawPath(
                shell,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF9A7A4C), Color(0xFF8A6B42)),
                    startY = y(5f), endY = y(91f),
                ),
                style = Stroke(1.7f * u),
            )

            // برق نور روی پوسته — عمق با نور
            drawOval(
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
                ),
                topLeft = Offset(x(30f), y(16f)),
                size = Size(x(22f), x(34f)),
            )

            // ---------- شکاف پوسته — خط باریک بالا
            drawPath(
                Path().apply {
                    moveTo(x(50f), y(6f))
                    quadraticBezierTo(x(46f), y(16f), x(50.5f), y(26f))
                },
                color = Color(0xFF8A6B42),
                style = Stroke(2.1f * u, cap = StrokeCap.Round),
            )

            // ---------- برگچهٔ سر پسته
            drawPath(
                Path().apply {
                    moveTo(x(50f), y(8f))
                    quadraticBezierTo(x(57f), y(0.5f), x(66f), y(4.5f))
                    quadraticBezierTo(x(58.5f), y(11.5f), x(50f), y(8f))
                    close()
                },
                color = Color(0xFF6F9A4E),
            )
            drawPath(
                Path().apply { moveTo(x(50f), y(9f)); lineTo(x(48f), y(4.5f)) },
                color = Color(0xFF5D7A40),
                style = Stroke(1.5f * u, cap = StrokeCap.Round),
            )

            // ---------- مغز سبز — صحنهٔ صورت با گرادیان شعاعی
            drawOval(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFBBD68F), Color(0xFF8FB260), Color(0xFF7A9A50)),
                    center = Offset(x(50f), y(33f)),
                    radius = x(27f),
                ),
                topLeft = Offset(x(25f), y(20f)),
                size = Size(x(50f), x(38f)),
            )

            // ---------- لپ‌های گیج
            drawCircle(Color(0xFFE2906B).copy(alpha = 0.55f), radius = x(4.2f), center = Offset(x(34.6f), y(41f)))
            drawCircle(Color(0xFFE2906B).copy(alpha = 0.55f), radius = x(4.2f), center = Offset(x(65.4f), y(41f)))

            // ---------- چشم‌ها — چپ باز، راست چشمکِ امضا
            fun eyeOpen(cx: Float) {
                drawOval(
                    color = Color(0xFFFCF8EE),
                    topLeft = Offset(x(cx - 4.4f), y(35f - 4.4f * blink)),
                    size = Size(x(8.8f), x(8.8f) * blink.coerceAtLeast(0.12f)),
                )
                if (blink > 0.5f) {
                    // مردمک — در حالت فکر به بالا-راست نگاه می‌کند
                    val lookUp = if (mood == PistachioMood.Think) 1.6f else 0f
                    val lookSide = if (mood == PistachioMood.Think) 1.3f else 0f
                    drawCircle(
                        color = Color(0xFF3A2E1B),
                        radius = x(2.2f),
                        center = Offset(x(cx + lookSide), y(35f - lookUp)),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = x(0.8f),
                        center = Offset(x(cx + 0.5f + lookSide), y(34f - lookUp)),
                    )
                }
            }
            fun eyeWinkArc(cx1: Float, cx2: Float) {
                drawPath(
                    Path().apply {
                        moveTo(x(cx1), y(35.4f))
                        quadraticBezierTo(x((cx1 + cx2) / 2f), y(32f), x(cx2), y(35.4f))
                    },
                    color = Color(0xFF3A2E1B),
                    style = Stroke(1.9f * u, cap = StrokeCap.Round),
                )
            }
            when {
                showWink -> { eyeOpen(41f); eyeWinkArc(55f, 63f) }
                mood == PistachioMood.Sulk -> { eyeWinkArc(37f, 45f); eyeWinkArc(55f, 63f) }
                else -> { eyeOpen(41f); eyeOpen(59f) }
            }
            // حالت گریه: اشک‌ها زیر چشم‌ها
            if (mood == PistachioMood.Cry) {
                listOf(38.5f to 43.5f, 61.5f to 43.5f).forEach { (tx, ty) ->
                    drawOval(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF9CD5F2).copy(alpha = 0.95f), Color(0xFF5FB0DC).copy(alpha = 0.65f)),
                        ),
                        topLeft = Offset(x(tx - 1.6f), y(ty)),
                        size = Size(x(3.2f), x(5.2f)),
                    )
                }
            }

            // ---------- ابروها — راستی بالا رفته از توّقی!
            val browLift = when {
                showWink -> 3.2f
                mood == PistachioMood.Sulk -> -1.8f
                else -> 0f
            }
            val tilt = if (mood == PistachioMood.Sulk) 1.5f else 0f
            drawPath(
                Path().apply {
                    moveTo(x(37.2f), y(28.5f - browLift + tilt))
                    quadraticBezierTo(x(41f), y(25.6f - browLift + tilt), x(44.8f), y(28.5f - browLift))
                },
                color = Color(0xFF3A2E1B),
                style = Stroke(1.7f * u, cap = StrokeCap.Round),
            )
            drawPath(
                Path().apply {
                    moveTo(x(55.2f), y(25.3f - browLift))
                    quadraticBezierTo(x(59f), y(22.4f - browLift + tilt), x(62.8f), y(25.3f - browLift + tilt))
                },
                color = Color(0xFF3A2E1B),
                style = Stroke(1.7f * u, cap = StrokeCap.Round),
            )

            // ---------- دهان — بر اساس حالت
            when {
                showWink || mood == PistachioMood.Happy -> {
                    // خندهٔ باز + زبان
                    drawPath(
                        Path().apply {
                            moveTo(x(43.5f), y(41.5f))
                            quadraticBezierTo(x(50f), y(49.5f), x(56.5f), y(41.5f))
                            close()
                        },
                        color = Color(0xFF3A2E1B),
                    )
                    drawPath(
                        Path().apply {
                            moveTo(x(47f), y(45.4f))
                            quadraticBezierTo(x(50f), y(49.6f), x(53f), y(45.4f))
                            close()
                        },
                        color = Color(0xFFE2906B),
                    )
                }
                mood == PistachioMood.Sulk -> {
                    // بغض — لب جمع‌شدهٔ کوچک
                    drawPath(
                        Path().apply {
                            moveTo(x(45f), y(45.5f))
                            quadraticBezierTo(x(50f), y(43f), x(55f), y(45.5f))
                        },
                        color = Color(0xFF3A2E1B),
                        style = Stroke(2f * u, cap = StrokeCap.Round),
                    )
                }
                mood == PistachioMood.Cry -> {
                    // دهان موجیِ غمگین
                    drawPath(
                        Path().apply {
                            moveTo(x(44f), y(45f))
                            quadraticBezierTo(x(47f), y(42.5f), x(50f), y(45f))
                            quadraticBezierTo(x(53f), y(47.5f), x(56f), y(45f))
                        },
                        color = Color(0xFF3A2E1B),
                        style = Stroke(1.9f * u, cap = StrokeCap.Round),
                    )
                }
                mood == PistachioMood.Think -> {
                    // دهان گرد کوچک «هوم...»
                    drawOval(
                        color = Color(0xFF3A2E1B),
                        topLeft = Offset(x(47f), y(42f)),
                        size = Size(x(6f), x(7f)),
                    )
                    // سه نقطهٔ فکر کنار برگچه
                    listOf(Offset(x(76f), y(7f)), Offset(x(81f), y(12f)), Offset(x(76f), y(17f))).forEachIndexed { i, c ->
                        drawCircle(
                            Color(0xFF6E4322).copy(alpha = 0.45f + 0.2f * i),
                            radius = x(1.2f),
                            center = c,
                        )
                    }
                }
            }

            // ---------- پاپیون هم‌رنگ تم — پایین پوسته
            val bow = Path().apply {
                moveTo(x(50f), y(72f))
                lineTo(x(42.5f), y(67.6f))
                quadraticBezierTo(x(40.8f), y(72f), x(42.5f), y(76.4f))
                close()
                moveTo(x(50f), y(72f))
                lineTo(x(57.5f), y(67.6f))
                quadraticBezierTo(x(59.2f), y(72f), x(57.5f), y(76.4f))
                close()
            }
            drawPath(bow, color = themeTint.copy(alpha = 0.92f))
            drawPath(bow, color = Color.Black.copy(alpha = 0.10f), style = Stroke(0.7f * u))
            drawCircle(themeTint, radius = x(1.9f), center = Offset(x(50f), y(72f)))
            drawCircle(Color.White.copy(alpha = 0.55f), radius = x(0.7f), center = Offset(x(49.2f), y(71.2f)))

            // برق کوچک روی لپ — جذابیت سه‌بعدی
            rotate(-18f, pivot = Offset(x(34.6f), y(41f))) {
                drawOval(
                    Color.White.copy(alpha = 0.32f),
                    topLeft = Offset(x(32f), y(39f)),
                    size = Size(x(5f), x(2.6f)),
                )
            }
        }
    }
}
