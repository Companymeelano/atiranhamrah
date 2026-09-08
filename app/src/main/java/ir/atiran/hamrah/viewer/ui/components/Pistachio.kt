package ir.atiran.hamrah.viewer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.utils.SoundFx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * شخصیت «پسته» 🌰 — دستیار هوشمند M•REPORT
 *
 * طرح کاملاً زنده: پوسته بادامی واقعی (بژ/کرم) با شکاف باز، مغز سبزِ
 * خندان داخلش، چشمک‌زدن دوره‌ای، بالاپایین‌پریدن آرام و واکنش به لمس
 * (چشمکِ تک‌چشمی + پرش + صدا).
 */
@Composable
fun Pistachio(
    size: Dp = 56.dp,
    modifier: Modifier = Modifier,
    bobbing: Boolean = true,
    /** رنگ اکسسوری — هماهنگ با تم انتخابی کاربر */
    themeTint: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
) {
    val tr = rememberInfiniteTransition(label = "pistachio")
    val t by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "pistBob",
    )
    val blinkPhase by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing)),
        label = "pistBlink",
    )
    val blink = if (blinkPhase < 0.07f) 0.12f else 1f

    var wink by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (wink) 1.10f else 1f,
        animationSpec = tween(200),
        label = "pistScale",
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // هر ازگاهی خودش چشمک تک‌چشمی می‌زند — زنده است!
        while (true) {
            delay(5200L)
            wink = true
            delay(420L)
            wink = false
        }
    }

    val bobPx = if (bobbing) 0.035f else 0f

    androidx.compose.foundation.layout.Box(
        modifier
            .graphicsLayer {
                translationY = sin(t * 2f * PI.toFloat()) * size.toPx() * bobPx
                scaleX = scale
                scaleY = scale
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

            // ---------- پوسته بادامی (بژ واقعی با گرادیان)
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
                    startY = y(5f), endY = y(92f),
                ),
            )
            drawPath(shell, color = Color(0xFF8A6B42), style = Stroke(1.7f * u))

            // شکاف پوسته — خط باریک بالا
            drawPath(
                Path().apply {
                    moveTo(x(50f), y(6f))
                    quadraticBezierTo(x(46f), y(16f), x(50.5f), y(26f))
                },
                color = Color(0xFF8A6B42),
                style = Stroke(2.1f * u, cap = StrokeCap.Round),
            )

            // ---------- مغز سبز (صحنه صورت)
            drawOval(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFBBD68F), Color(0xFF8FB260), Color(0xFF7A9A50)),
                    center = Offset(x(50f), y(38f)),
                    radius = x(25f),
                ),
                topLeft = Offset(x(25f), y(20f)),
                size = Size(x(50f), x(38f)),
            )

            // ---------- چشم‌ها (چشمک‌زن)
            fun eye(cx: Float, winkThis: Boolean) {
                if (winkThis) {
                    // چشم بسته خندان — کمان
                    drawArc(
                        color = Color(0xFF3A2E1B),
                        startAngle = 20f, sweepAngle = 140f, useCenter = false,
                        topLeft = Offset(x(cx - 4.6f), y(31.2f)),
                        size = Size(x(9.2f), x(7f)),
                        style = Stroke(1.9f * u, cap = StrokeCap.Round),
                    )
                } else {
                    // سفیدی چشم (با چشمک: له‌شده)
                    drawOval(
                        color = Color(0xFFFCF8EE),
                        topLeft = Offset(x(cx - 4.4f), y(34.4f - 4.4f * blink)),
                        size = Size(x(8.8f), x(8.8f) * blink.coerceAtLeast(0.12f)),
                    )
                    if (blink > 0.5f) {
                        drawCircle(
                            color = Color(0xFF3A2E1B),
                            radius = x(2.2f),
                            center = Offset(x(cx), y(35f)),
                        )
                        // برق چشم
                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = x(0.8f),
                            center = Offset(x(cx - 0.9f), y(34f)),
                        )
                    }
                }
            }
            eye(41f, wink)
            eye(59f, false)

            // ---------- ابروهای بانمک (هنگام چشمک بالا می‌روند!)
            val browLift = if (wink) 3.2f else 0f
            drawPath(
                Path().apply {
                    moveTo(x(37.2f), y(28.5f - browLift))
                    quadraticBezierTo(x(41f), y(25.6f - browLift), x(44.8f), y(28.5f - browLift))
                },
                color = Color(0xFF3A2E1B),
                style = Stroke(1.7f * u, cap = StrokeCap.Round),
            )
            drawPath(
                Path().apply {
                    moveTo(x(55.2f), y(28.5f - browLift))
                    quadraticBezierTo(x(59f), y(25.6f - browLift), x(62.8f), y(28.5f - browLift))
                },
                color = Color(0xFF3A2E1B),
                style = Stroke(1.7f * u, cap = StrokeCap.Round),
            )

            // ---------- لپ‌های گیج (بزرگ‌تر و بامزه‌تر)
            drawCircle(Color(0xFFE2906B).copy(alpha = 0.38f), radius = x(4.2f), center = Offset(x(34.6f), y(41f)))
            drawCircle(Color(0xFFE2906B).copy(alpha = 0.38f), radius = x(4.2f), center = Offset(x(65.4f), y(41f)))

            // ---------- لبخند (هنگام چشمک: دهان باز خنده‌دار + زبان!)
            if (wink) {
                // دهان خنده باز
                drawPath(
                    Path().apply {
                        moveTo(x(43.5f), y(41.5f))
                        quadraticBezierTo(x(50f), y(49.5f), x(56.5f), y(41.5f))
                        close()
                    },
                    color = Color(0xFF3A2E1B),
                )
                // زبان
                drawPath(
                    Path().apply {
                        moveTo(x(47f), y(45.4f))
                        quadraticBezierTo(x(50f), y(49.6f), x(53f), y(45.4f))
                        close()
                    },
                    color = Color(0xFFE2906B),
                )
            } else {
                drawArc(
                    color = Color(0xFF3A2E1B),
                    startAngle = 15f, sweepAngle = 150f, useCenter = false,
                    topLeft = Offset(x(43.5f), y(38.5f)),
                    size = Size(x(13f), x(11f)),
                    style = Stroke(1.9f * u, cap = StrokeCap.Round),
                )
            }

            // ---------- پاپیون هم‌رنگ تم — هماهنگی پسته با قالب
            val bow = Path().apply {
                moveTo(x(50f), y(62f))
                lineTo(x(42.5f), y(57.6f))
                quadraticBezierTo(x(40.8f), y(62f), x(42.5f), y(66.4f))
                close()
                moveTo(x(50f), y(62f))
                lineTo(x(57.5f), y(57.6f))
                quadraticBezierTo(x(59.2f), y(62f), x(57.5f), y(66.4f))
                close()
            }
            drawPath(bow, color = themeTint.copy(alpha = 0.92f))
            drawPath(bow, color = Color.Black.copy(alpha = 0.10f), style = Stroke(0.7f * u))
            drawCircle(themeTint, radius = x(1.9f), center = Offset(x(50f), y(62f)))
            drawCircle(Color.White.copy(alpha = 0.5f), radius = x(0.7f), center = Offset(x(49.3f), y(61.2f)))

            // ---------- برگچه سرش
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
                Path().apply {
                    moveTo(x(50f), y(9f))
                    lineTo(x(48f), y(4.5f))
                },
                color = Color(0xFF5D7A40),
                style = Stroke(1.5f * u, cap = StrokeCap.Round),
            )
        }
    }
}
