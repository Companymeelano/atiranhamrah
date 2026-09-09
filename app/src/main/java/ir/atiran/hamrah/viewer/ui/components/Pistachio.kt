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
 * شخصیت «پسته» 🥜 — دستیار زندهٔ M•REPORT (نسخهٔ ۲ — بازسازی از تصویر مرجع)
 *
 * مثل یک آدم واقعی زنده است: پلک می‌زند، نفس می‌کشد، دست تکان می‌دهد،
 * می‌پرد، به لمس واکنش نشان می‌دهد و پنج حالت چهره دارد — خوشحال،
 * چشمکِ شیطنت‌آمیز، بغضِ لجباز، گریهٔ همدردی و فکر کردن.
 *
 * ساختار (مطابق تصویر مرجع): نیمه‌های تیرهٔ پوستهٔ پسته که باز شده‌اند،
 * بدن/مغز کرمی گرد، برگچهٔ سبز روی سر، دو چشم غول‌پیکر براق مشکی،
 * لبخند پهن با زبان، دو دست سبز تکان‌دهنده و دو پای سبز کوچک.
 */
@Composable
fun Pistachio(
    size: Dp = 56.dp,
    modifier: Modifier = Modifier,
    bobbing: Boolean = true,
    mood: PistachioMood = PistachioMood.Happy,
    /** رنگ دست‌ها — هماهنگ با تم انتخابی کاربر */
    themeTint: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
    /** چشم‌های بسته — برای صحنهٔ خواب در انیمیشن ورود */
    eyesClosed: Boolean = false,
    /** تکان‌دادن دست‌ها — سلام و احوال‌پرسی */
    armWave: Boolean = true,
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
    val blink = if (blinkPhase < 0.07f) 0.10f else 1f

    // چشمک خودبه‌خودی — پسته همیشه زنده است
    var wink by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = when {
            wink -> 1.10f
            mood == PistachioMood.Sulk -> 0.97f
            else -> 1f
        },
        animationSpec = tween(220),
        label = "pistScale",
    )
    val scope = rememberCoroutineScope()
    if (!eyesClosed) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(4600L)
                wink = true
                delay(430L)
                wink = false
            }
        }
    }

    val bobPx = if (bobbing) 0.042f else 0f
    val showWink = (wink || mood == PistachioMood.Wink) && !eyesClosed
    // موج دست‌ها — تکان‌دادن شیطنت‌آمیز
    val waveA = if (armWave) sin(t * 2f * PI.toFloat()) * 16f else 6f

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

            // ---------- نیمه‌های پوستهٔ تیره — باز شده به دو طرف
            val shellL = Path().apply {
                moveTo(x(50f), y(12f))
                cubicTo(x(36f), y(14f), x(24f), y(24f), x(20f), y(40f))
                cubicTo(x(17f), y(52f), x(22f), y(66f), x(34f), y(74f))
                cubicTo(x(26f), y(60f), x(27f), y(44f), x(36f), y(32f))
                cubicTo(x(42f), y(24f), x(48f), y(18f), x(56f), y(16f))
                close()
            }
            drawPath(
                shellL,
                brush = Brush.linearGradient(
                    listOf(Color(0xFF7B3E1E), Color(0xFF4A1F0D)),
                    start = Offset(x(18f), y(12f)),
                    end = Offset(x(52f), y(74f)),
                ),
            )
            drawPath(shellL, color = Color(0xFF33150A), style = Stroke(1.0f * u))

            val shellR = Path().apply {
                moveTo(x(50f), y(12f))
                cubicTo(x(64f), y(14f), x(76f), y(24f), x(80f), y(40f))
                cubicTo(x(83f), y(52f), x(78f), y(66f), x(66f), y(74f))
                cubicTo(x(74f), y(60f), x(73f), y(44f), x(64f), y(32f))
                cubicTo(x(58f), y(24f), x(52f), y(18f), x(44f), y(16f))
                close()
            }
            drawPath(
                shellR,
                brush = Brush.linearGradient(
                    listOf(Color(0xFF6B3418), Color(0xFF3F1B0C)),
                    start = Offset(x(82f), y(12f)),
                    end = Offset(x(48f), y(74f)),
                ),
            )
            drawPath(shellR, color = Color(0xFF33150A), style = Stroke(1.0f * u))

            // ---------- برگچهٔ سبز روی سر
            val leaf = Path().apply {
                moveTo(x(52f), y(32f))
                cubicTo(x(48f), y(26f), x(47f), y(18f), x(51f), y(10f))
                cubicTo(x(55f), y(4f), x(61f), y(3f), x(64f), y(6f))
                cubicTo(x(60f), y(8f), x(57f), y(13f), x(57f), y(20f))
                cubicTo(x(57f), y(25f), x(56f), y(29f), x(54f), y(33f))
                close()
            }
            drawPath(
                leaf,
                brush = Brush.linearGradient(
                    listOf(Color(0xFFA9C43C), Color(0xFF6E8F2A)),
                    start = Offset(x(58f), y(8f)),
                    end = Offset(x(50f), y(34f)),
                ),
            )
            drawPath(leaf, color = Color(0xFF546E1E), style = Stroke(0.8f * u))
            drawPath(
                Path().apply {
                    moveTo(x(53f), y(31f))
                    cubicTo(x(53f), y(24f), x(54f), y(17f), x(57f), y(11f))
                },
                color = Color(0xFF546E1E),
                style = Stroke(0.8f * u, cap = StrokeCap.Round),
            )

            // ---------- پاهای سبز کوچک
            drawLine(Color(0xFF5C7820), Offset(x(43f), y(91f)), Offset(x(43f), y(96f)), strokeWidth = 7f * u, cap = StrokeCap.Round)
            drawLine(Color(0xFF5C7820), Offset(x(57f), y(91f)), Offset(x(57f), y(96f)), strokeWidth = 7f * u, cap = StrokeCap.Round)

            // ---------- دست‌های سبز — تکان‌دهنده!
            val armColor = listOf(Color(0xFF8FA834), Color(0xFF66831F))
            rotate(waveA, pivot = Offset(x(27f), y(64f))) {
                drawLine(
                    brush = Brush.linearGradient(armColor, start = Offset(x(27f), y(64f)), end = Offset(x(13f), y(45f))),
                    start = Offset(x(27f), y(64f)),
                    end = Offset(x(13f), y(45f)),
                    strokeWidth = 7f * u,
                    cap = StrokeCap.Round,
                )
            }
            rotate(-waveA, pivot = Offset(x(73f), y(64f))) {
                drawLine(
                    brush = Brush.linearGradient(armColor, start = Offset(x(73f), y(64f)), end = Offset(x(87f), y(45f))),
                    start = Offset(x(73f), y(64f)),
                    end = Offset(x(87f), y(45f)),
                    strokeWidth = 7f * u,
                    cap = StrokeCap.Round,
                )
            }

            // ---------- بدن کرمی — مغز پسته
            drawOval(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFBF2DA), Color(0xFFEFDCB6), Color(0xFFD9BE93)),
                    startY = y(27f), endY = y(93f),
                ),
                topLeft = Offset(x(19f), y(27f)),
                size = Size(x(62f), x(66f)),
            )
            drawOval(
                color = Color(0xFFC9AC80),
                topLeft = Offset(x(19f), y(27f)),
                size = Size(x(62f), x(66f)),
                style = Stroke(1.2f * u),
            )
            // نور نقطه‌ای روی بدن — عمق با نور
            drawOval(
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.50f), Color.Transparent),
                ),
                topLeft = Offset(x(26f), y(27f)),
                size = Size(x(24f), x(18f)),
            )

            // ---------- لپ‌های نرم
            drawOval(Color(0xFFF0C9A4).copy(alpha = 0.55f), topLeft = Offset(x(22.4f), y(63.2f)), size = Size(x(9.2f), x(5.6f)))
            drawOval(Color(0xFFF0C9A4).copy(alpha = 0.55f), topLeft = Offset(x(68.4f), y(63.2f)), size = Size(x(9.2f), x(5.6f)))

            // ---------- چشم‌ها — غول‌پیکر و براق
            fun eyeOpen(cx: Float, cy: Float, rx: Float, ry: Float) {
                drawOval(
                    color = Color(0xFF241C10),
                    topLeft = Offset(x(cx - rx), y(cy - ry * blink)),
                    size = Size(x(rx * 2f), x(ry * 2f * blink.coerceAtLeast(0.10f))),
                )
                if (blink > 0.5f) {
                    // مردمک‌ها در حالت فکر به بالا-راست نگاه می‌کنند
                    val lookUp = if (mood == PistachioMood.Think) 1.8f else 0f
                    val lookSide = if (mood == PistachioMood.Think) 1.6f else 0f
                    drawCircle(
                        Color.White.copy(alpha = 0.95f),
                        radius = x(rx * 0.34f),
                        center = Offset(x(cx - rx * 0.30f + lookSide), y(cy - ry * 0.32f - lookUp)),
                    )
                    drawCircle(
                        Color.White.copy(alpha = 0.65f),
                        radius = x(rx * 0.13f),
                        center = Offset(x(cx + rx * 0.26f + lookSide), y(cy + ry * 0.24f - lookUp)),
                    )
                }
            }
            fun eyeClosedArc(cx1: Float, cx2: Float, cy: Float, flip: Boolean = false) {
                drawPath(
                    Path().apply {
                        moveTo(x(cx1), y(cy))
                        quadraticBezierTo(
                            x((cx1 + cx2) / 2f), y(if (flip) cy - 3.6f else cy + 3.6f),
                            x(cx2), y(cy),
                        )
                    },
                    color = Color(0xFF241C10),
                    style = Stroke(2.0f * u, cap = StrokeCap.Round),
                )
            }
            when {
                eyesClosed -> { eyeClosedArc(30f, 44f, 54f); eyeClosedArc(56f, 70f, 55f) }
                showWink -> { eyeOpen(37f, 54f, 8.8f, 10f); eyeClosedArc(56f, 70f, 55f, flip = true) }
                mood == PistachioMood.Sulk -> { eyeClosedArc(30f, 44f, 54f, flip = true); eyeClosedArc(56f, 70f, 55f, flip = true) }
                else -> { eyeOpen(37f, 54f, 8.8f, 10f); eyeOpen(63f, 55f, 9.8f, 11f) }
            }
            // گریه: قطره‌های اشک زیر چشم‌ها
            if (mood == PistachioMood.Cry && !eyesClosed) {
                val tearY = 64f + (t * 8f) % 1f * 6f
                listOf(37f to tearY, 63f to tearY).forEach { (tx, ty) ->
                    drawOval(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF9CD5F2).copy(alpha = 0.95f), Color(0xFF5FB0DC).copy(alpha = 0.60f)),
                        ),
                        topLeft = Offset(x(tx - 1.7f), y(ty)),
                        size = Size(x(3.4f), x(5.4f)),
                    )
                }
            }

            // ---------- ابروها — فقط در بغض و فکر
            if (mood == PistachioMood.Sulk || mood == PistachioMood.Think) {
                val lift = if (mood == PistachioMood.Think) -1.5f else 0f
                drawPath(
                    Path().apply {
                        moveTo(x(31f), y(42f + lift))
                        quadraticBezierTo(x(37f), y(40f + lift), x(43f), y(42.5f + lift))
                    },
                    color = Color(0xFF241C10),
                    style = Stroke(1.6f * u, cap = StrokeCap.Round),
                )
                drawPath(
                    Path().apply {
                        moveTo(x(57f), y(42.5f + lift))
                        quadraticBezierTo(x(63f), y(40f + lift), x(69f), y(42f + lift))
                    },
                    color = Color(0xFF241C10),
                    style = Stroke(1.6f * u, cap = StrokeCap.Round),
                )
            }

            // ---------- دهان — بر اساس حالت
            when {
                eyesClosed -> {
                    // دهان کوچک خواب — نفس می‌کشد
                    drawOval(
                        color = Color(0xFF241C10).copy(alpha = 0.85f),
                        topLeft = Offset(x(47.5f), y(70f)),
                        size = Size(x(5f), x(6.4f * (0.6f + 0.4f * sin(t * 2f * PI.toFloat()).coerceAtLeast(0f)))),
                    )
                }
                showWink || mood == PistachioMood.Happy -> {
                    // لبخند پهن + زبان
                    drawPath(
                        Path().apply {
                            moveTo(x(39f), y(69f))
                            quadraticBezierTo(x(50f), y(79f), x(61f), y(69f))
                            quadraticBezierTo(x(50f), y(74f), x(39f), y(69f))
                            close()
                        },
                        color = Color(0xFF241C10),
                    )
                    drawPath(
                        Path().apply {
                            moveTo(x(45f), y(73.6f))
                            quadraticBezierTo(x(50f), y(78f), x(55f), y(73.6f))
                            quadraticBezierTo(x(50f), y(76.4f), x(45f), y(73.6f))
                            close()
                        },
                        color = Color(0xFFD97C64),
                    )
                }
                mood == PistachioMood.Sulk -> {
                    drawPath(
                        Path().apply {
                            moveTo(x(45f), y(72f))
                            quadraticBezierTo(x(50f), y(69.6f), x(55f), y(72f))
                        },
                        color = Color(0xFF241C10),
                        style = Stroke(2f * u, cap = StrokeCap.Round),
                    )
                }
                mood == PistachioMood.Cry -> {
                    drawPath(
                        Path().apply {
                            moveTo(x(44f), y(72f))
                            quadraticBezierTo(x(47f), y(69.5f), x(50f), y(72f))
                            quadraticBezierTo(x(53f), y(74.5f), x(56f), y(72f))
                        },
                        color = Color(0xFF241C10),
                        style = Stroke(1.9f * u, cap = StrokeCap.Round),
                    )
                }
                mood == PistachioMood.Think -> {
                    drawOval(
                        color = Color(0xFF241C10),
                        topLeft = Offset(x(47f), y(69f)),
                        size = Size(x(6f), x(7f)),
                    )
                    // سه نقطهٔ فکر کنار برگچه
                    listOf(Offset(x(76f), y(9f)), Offset(x(81f), y(14f)), Offset(x(76f), y(19f))).forEachIndexed { i, c ->
                        drawCircle(
                            Color(0xFF6E4322).copy(alpha = 0.45f + 0.2f * i),
                            radius = x(1.2f),
                            center = c,
                        )
                    }
                }
            }

            // برق کوچک روی لپ — جذابیت سه‌بعدی
            rotate(-18f, pivot = Offset(x(27f), y(66f))) {
                drawOval(
                    Color.White.copy(alpha = 0.30f),
                    topLeft = Offset(x(24.6f), y(64.8f)),
                    size = Size(x(5f), x(2.4f)),
                )
            }
        }
    }
}
