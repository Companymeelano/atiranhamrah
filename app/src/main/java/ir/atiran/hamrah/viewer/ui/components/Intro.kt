package ir.atiran.hamrah.viewer.ui.components

/**
 * سکانس سینمایی ورود M•REPORT — «میز کار آجیل» 🥜
 *
 * یک ویدئوی کوتاه کاملاً واقعی و لوکس:
 *  نور گرم مطالعه روی میز چوبی، ذرات غبار معلق در نور، مشتی پستهٔ واقعی
 *  روی میز، نمودار ستونی شیشه‌ای و حلقهٔ طلایی فلزی که با هم بالا می‌آیند،
 *  شمارنده‌های زندهٔ فروش، و در انتها قهرمانِ فتورئال (پسته) با چشم‌های
 *  براق فرود می‌آید و به شما چشمک می‌زند.
 *
 *  پرده‌ها (خط زمانی ۰→۱ در ۹.۲ ثانیه):
 *   ۰.۰۸–۰.۴۲ نمودارها بالا می‌آیند · ۰.۱۴–۰.۴۴ حلقهٔ طلایی می‌چرخد داخل صحنه
 *   ۰.۳۰–۰.۵۴ پستهٔ قهرمان با جهش فرود می‌آید · ۰.۵۲+ لوگوی طلایی و شمارنده‌ها
 *   ۰.۸۰+ دکمهٔ «ورود به گزارش‌ها»
 *  لمس صفحه = رد کردن.
 */
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.atiran.hamrah.viewer.R
import ir.atiran.hamrah.viewer.data.TableHeuristics
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import ir.atiran.hamrah.viewer.utils.SoundFx
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

/** پیشرفت نرم */
private fun easeOutCubic(p: Float): Float = 1f - (1f - p).let { it * it * it }

/** فرود با جهش — overshoot ملایم */
private fun landBounce(p: Float): Float {
    val q = p.coerceIn(0f, 1f)
    return if (q < 0.7f) {
        val k = q / 0.7f
        k * k * (3f - 2f * k) * 1.04f
    } else {
        1.04f - (q - 0.7f) / 0.3f * 0.04f + sin((q - 0.7f) * PI.toFloat() * 2f) * 0.012f * (1f - q)
    }
}

@Composable
fun PistachioIntro(onDone: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    // خط زمانی کل سکانس: ۰ → ۱ در ۹.۲ ثانیه
    val t = remember { Animatable(0f) }
    val tv = t.value
    // تنفس بی‌پایان پس از فرود
    val tr = rememberInfiniteTransition(label = "sceneBreath")
    val breath by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Reverse),
        label = "breath",
    )

    LaunchedEffect(Unit) {
        SoundFx.soft()
        delay(2900)
        SoundFx.success()
        // فیلم تمام می‌شود اما ورود فقط با واکنش کاربر — پسته منتظر می‌ماند
        t.animateTo(1f, tween(9200, easing = LinearEasing))
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
        // ================== صحنهٔ واقعی — میز چوبی و نور ==================
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val fadeIn = easeOutCubic((tv / 0.08f).coerceIn(0f, 1f))
            val deskY = h * 0.64f

            // نور مطالعهٔ گرم — بالا-چپ
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFFE3B0).copy(alpha = 0.26f * fadeIn), Color.Transparent),
                    center = Offset(w * 0.16f, h * 0.10f),
                    radius = w * 0.55f,
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.16f, h * 0.10f),
            )
            // نور محیطی تم — راست
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(extras.brand.last().copy(alpha = 0.12f * fadeIn), Color.Transparent),
                ),
                radius = w * 0.4f,
                center = Offset(w * 0.88f, h * 0.30f),
            )

            // ---------- میز چوبی
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF7A5533), Color(0xFF5C3D22), Color(0xFF3E2915)),
                    startY = deskY, endY = h,
                ),
                topLeft = Offset(0f, deskY),
                size = Size(w, h - deskY),
            )
            // خط افق روشن
            drawLine(
                Color(0xFFC9A876).copy(alpha = 0.35f * fadeIn),
                Offset(0f, deskY), Offset(w, deskY),
                strokeWidth = 1.2f,
            )
            // بافت چوب — رگه‌های موجی
            for (i in 0 until 13) {
                val gy = deskY + (h - deskY) * (i + 1) / 14f
                val amp = 3f + (i % 3) * 2f
                val phase = i * 1.7f
                val grain = Path().apply {
                    moveTo(0f, gy)
                    var x = 0f
                    while (x < w) {
                        lineTo(x + 26f, gy + sin(x / w * 9f * PI.toFloat() + phase) * amp)
                        x += 26f
                    }
                }
                drawPath(
                    grain,
                    color = if (i % 2 == 0) Color(0x14302010) else Color(0x14FFDCA8),
                    style = Stroke(1.4f),
                )
            }
            // بازتاب نور روی میز
            drawOval(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFFE3B0).copy(alpha = 0.14f * fadeIn), Color.Transparent),
                ),
                topLeft = Offset(w * 0.05f, deskY + (h - deskY) * 0.10f),
                size = Size(w * 0.9f, (h - deskY) * 0.8f),
            )

            // ---------- پرتوهای نور مطالعه — از بالای چپ
            for (i in 0 until 2) {
                rotate(if (i == 0) 24f else 38f, pivot = Offset(w * 0.10f, -h * 0.05f)) {
                    val rw = w * (if (i == 0) 0.10f else 0.06f)
                    val rh = h * 0.95f
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFFFE3B0).copy(alpha = (0.10f - i * 0.03f) * fadeIn), Color.Transparent),
                        ),
                        topLeft = Offset(w * 0.10f + i * w * 0.11f, -h * 0.05f),
                        size = Size(rw, rh),
                    )
                }
            }

            // ---------- پسته‌های شناور واقعی در عمق
            for (i in 0 until 3) {
                val depth = 0.45f + i * 0.28f
                val drift = sin(tv * (0.8f + i * 0.3f) * 2f * PI.toFloat() + i * 2f)
                val fx = w * (0.06f + i * 0.41f) + drift * w * 0.022f
                val fy = h * (0.16f + i * 0.13f) + sin(tv * (0.6f + i * 0.2f) * 2f * PI.toFloat() + i) * h * 0.014f
                rotate(14f + i * 21f, pivot = Offset(fx, fy)) {
                    drawMiniPistachio(fx, fy, w * 0.028f * depth, angle = 0f, alpha = (0.30f + i * 0.10f) * fadeIn)
                }
            }

            // ---------- ذرات غبار معلق در نور — واقعی
            for (i in 0 until 11) {
                val ph = (tv * 0.22f + i * 0.093f) % 1f
                val dx = w * (0.06f + (i * 43 % 88) / 100f * 0.88f) + sin(ph * 4f * PI.toFloat() + i) * w * 0.012f
                val dy = h * 0.88f - ph * h * 0.72f
                val depth = 0.35f + (i % 3) * 0.22f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFFFE9C4).copy(alpha = 0.30f * fadeIn * (1f - ph * 0.4f)), Color.Transparent),
                    ),
                    radius = w * 0.006f * (1f + depth),
                    center = Offset(dx, dy),
                )
            }

            // ---------- مشتی پستهٔ واقعی روی میز — چپ
            val pile = listOf(
                Triple(w * 0.20f, deskY + (h - deskY) * 0.42f, w * 0.046f),
                Triple(w * 0.30f, deskY + (h - deskY) * 0.58f, w * 0.038f),
                Triple(w * 0.13f, deskY + (h - deskY) * 0.66f, w * 0.032f),
            )
            val pile2 = listOf(
                Triple(w * 0.245f, deskY + (h - deskY) * 0.50f, w * 0.040f),
                Triple(w * 0.35f, deskY + (h - deskY) * 0.36f, w * 0.030f),
                Triple(w * 0.09f, deskY + (h - deskY) * 0.34f, w * 0.028f),
            )
            (pile + pile2).forEachIndexed { i, (px_, py_, pr_) ->
                drawMiniPistachio(
                    px_, py_ - pr_ * 0.55f, pr_,
                    angle = -18f + i * 13f,
                    alpha = fadeIn * 0.95f,
                )
            }

            // ---------- نمودار ستونی شیشه‌ای — راست
            val chartA = easeOutCubic(((tv - 0.10f) / 0.32f).coerceIn(0f, 1f))
            if (chartA > 0.01f) {
                val cx = w * 0.775f
                val cy = h * 0.335f
                val pw = w * 0.30f
                val phh = h * 0.27f
                rotate(-3.5f, pivot = Offset(cx, cy + phh / 2f)) {
                    // پنل شیشه‌ای
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.085f), Color.White.copy(alpha = 0.03f)),
                            startY = cy - phh / 2f, endY = cy + phh / 2f,
                        ),
                        topLeft = Offset(cx - pw / 2f, cy - phh / 2f),
                        size = Size(pw, phh),
                        cornerRadius = CornerRadius(w * 0.018f),
                    )
                    drawRoundRect(
                        color = Color(0xFFE9C777).copy(alpha = 0.35f * chartA),
                        topLeft = Offset(cx - pw / 2f, cy - phh / 2f),
                        size = Size(pw, phh),
                        cornerRadius = CornerRadius(w * 0.018f),
                        style = Stroke(1.2f),
                    )
                    // سه ستون شیشه‌ای
                    val base = cy + phh / 2f - pw * 0.09f
                    val heights = listOf(0.44f, 0.68f, 0.95f)
                    heights.forEachIndexed { i, hf ->
                        val bp = easeOutCubic(((chartA - i * 0.16f) / (1f - i * 0.16f)).coerceIn(0f, 1f))
                        if (bp > 0.01f) {
                            val bw = pw * 0.16f
                            val bx = cx - pw / 2f + pw * (0.20f + i * 0.28f)
                            val bh = (phh * 0.72f) * hf * bp
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.42f * chartA),
                                        Color(0xFFE9C777).copy(alpha = 0.34f * chartA),
                                        Color(0xFFC9963C).copy(alpha = 0.16f * chartA),
                                    ),
                                    startY = base - bh, endY = base,
                                ),
                                topLeft = Offset(bx - bw / 2f, base - bh),
                                size = Size(bw, bh),
                                cornerRadius = CornerRadius(bw / 2f),
                            )
                            // درپوش روشن ستون
                            drawOval(
                                color = Color.White.copy(alpha = 0.55f * chartA * bp),
                                topLeft = Offset(bx - bw / 2f + bw * 0.12f, base - bh - bw * 0.055f),
                                size = Size(bw * 0.76f, bw * 0.11f),
                            )
                        }
                    }
                    // خط پایه
                    drawLine(
                        Color(0xFFE9C777).copy(alpha = 0.5f * chartA),
                        Offset(cx - pw * 0.42f, base), Offset(cx + pw * 0.42f, base),
                        strokeWidth = 1.4f,
                    )
                }
            }

            // ---------- حلقهٔ طلایی فلزی — چپ
            val ringA = easeOutCubic(((tv - 0.14f) / 0.30f).coerceIn(0f, 1f))
            if (ringA > 0.01f) {
                val rcx = w * 0.185f
                val rcy = h * 0.335f
                val rr = w * 0.105f
                // مسیر کم‌رنگ
                drawCircle(
                    color = Color(0xFFE9C777).copy(alpha = 0.14f * ringA),
                    radius = rr,
                    center = Offset(rcx, rcy),
                    style = Stroke(w * 0.018f),
                )
                // حلقهٔ فلزی با گرادیان چرخشی
                rotate(120f * (1f - ringA) + tv * 40f, pivot = Offset(rcx, rcy)) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFF8A5E1E), Color(0xFFE9C777), Color(0xFFFFF3D0),
                                Color(0xFFD9A85C), Color(0xFF8A5E1E),
                            ),
                            center = Offset(rcx, rcy),
                        ),
                        startAngle = -60f,
                        sweepAngle = 300f * ringA,
                        useCenter = false,
                        topLeft = Offset(rcx - rr, rcy - rr),
                        size = Size(rr * 2f, rr * 2f),
                        style = Stroke(w * 0.018f, cap = StrokeCap.Round),
                    )
                }
                // برق روی حلقه
                drawArc(
                    color = Color.White.copy(alpha = 0.5f * ringA),
                    startAngle = -20f + tv * 240f,
                    sweepAngle = 26f,
                    useCenter = false,
                    topLeft = Offset(rcx - rr, rcy - rr),
                    size = Size(rr * 2f, rr * 2f),
                    style = Stroke(w * 0.006f, cap = StrokeCap.Round),
                )
            }

            // ---------- سایهٔ قهرمان — با فرودش بزرگ می‌شود
            val heroP = ((tv - 0.30f) / 0.24f).coerceIn(0f, 1f)
            if (heroP > 0.01f) {
                val shScale = landBounce(heroP)
                drawOval(
                    brush = Brush.radialGradient(
                        listOf(Color(0x40000000).copy(alpha = 0.30f * heroP), Color.Transparent),
                    ),
                    topLeft = Offset(w * 0.5f - w * 0.16f * shScale, deskY + (h - deskY) * 0.30f),
                    size = Size(w * 0.32f * shScale, w * 0.07f * shScale),
                )
            }
        }

        // ================== قهرمان — پستهٔ فتورئال ==================
        val heroP = ((tv - 0.30f) / 0.24f).coerceIn(0f, 1f)
        if (heroP > 0.01f) {
            val landScale = landBounce(heroP)
            val breathe = if (heroP >= 1f) 1f + breath * 0.010f else 1f
            Image(
                painter = painterResource(R.drawable.mascot_pistachio),
                contentDescription = "پسته",
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        // اسکواش-استرچ فرود — مثل جسم واقعی نرم
                        val squash = if (heroP < 1f) sin(heroP * PI.toFloat()) * 0.07f else 0f
                        scaleX = landScale * breathe * (1f + squash)
                        scaleY = landScale * breathe * (1f - squash)
                        alpha = heroP
                        translationY = (1f - heroP) * 90f
                    }
                    .fillMaxWidth(0.74f)
                    .aspectRatio(1f),
            )
        }

        // ================== لوگو و شعار ==================
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp),
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = tv >= 0.52f,
                enter = fadeIn(tween(500)),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "M•REPORT",
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF8A5E1E), Color(0xFFE9C777), Color(0xFFFFF3D0), Color(0xFFD9A85C))
                            ),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                        ),
                    )
                    Spacer(Modifier.height(4.dp))
                    MrSubtitle("Intelligent Reporting Experience")
                }
            }
        }

        // ================== شمارنده‌های زنده ==================
        androidx.compose.animation.AnimatedVisibility(
            visible = tv >= 0.58f,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 200.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val cp = easeOutCubic(((tv - 0.58f) / 0.26f).coerceIn(0f, 1f))
                StatChip("فروش امروز", TableHeuristics.faMoney(18_500_000.0 * cp) + " تومان")
                StatChip("مشتریان فعال", TableHeuristics.faMoney(2_120.0 * cp))
                StatChip("گردش ماه", TableHeuristics.faMoney(864_000_000.0 * cp))
            }
        }

        // ================== دکمهٔ ورود ==================
        if (tv >= 0.80f) {
            val pulse by rememberInfiniteTransition(label = "btnPulse").animateFloat(
                initialValue = 1f,
                targetValue = 1.055f,
                animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                label = "btnPulseF",
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 92.dp)
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    }
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF8A5E1E), Color(0xFFE9C777), Color(0xFFC9963C))
                        )
                    )
                    .clickable { SoundFx.success(); onDone() }
                    .padding(horizontal = 28.dp, vertical = 13.dp),
            ) {
                Box(
                    Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF3D0)),
                )
                Text(
                    "ورود به گزارش‌ها",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFF3D0),
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

/** چیپ آمار شیشه‌ای */
@Composable
private fun StatChip(label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color(0xFFE9C777).copy(alpha = 0.30f), RoundedCornerShape(13.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFFE9C777),
            maxLines = 1,
        )
    }
}

/** پستهٔ کوچک واقعی — برای مشتی پسته روی میز */
private fun DrawScope.drawMiniPistachio(cx: Float, cy: Float, r: Float, angle: Float, alpha: Float) {
    // سایهٔ نرم
    drawOval(
        brush = Brush.radialGradient(
            listOf(Color(0x26000000).copy(alpha = 0.5f * alpha), Color.Transparent),
        ),
        topLeft = Offset(cx - r * 1.15f, cy + r * 0.62f),
        size = Size(r * 2.3f, r * 0.55f),
    )
    rotate(angle, pivot = Offset(cx, cy)) {
        // بدنهٔ بادامی — گرادیان واقعی
        val body = Path().apply {
            moveTo(cx, cy - r)
            cubicTo(cx + r * 0.85f, cy - r * 0.65f, cx + r * 0.95f, cy + r * 0.45f, cx + r * 0.15f, cy + r)
            cubicTo(cx - r * 0.15f, cy + r, cx - r * 0.95f, cy + r * 0.45f, cx - r * 0.85f, cy - r * 0.65f)
            close()
        }
        drawPath(
            body,
            brush = Brush.linearGradient(
                listOf(Color(0xFFF0DBB2).copy(alpha = alpha), Color(0xFFCBA873).copy(alpha = alpha), Color(0xFF9A7847).copy(alpha = alpha)),
                start = Offset(cx - r, cy - r),
                end = Offset(cx + r * 0.6f, cy + r),
            ),
        )
        drawPath(body, color = Color(0xFF7A5C33).copy(alpha = 0.8f * alpha), style = Stroke(r * 0.09f))
        // شکاف
        drawLine(
            Color(0xFF7A5C33).copy(alpha = 0.75f * alpha),
            Offset(cx, cy - r * 0.85f), Offset(cx + r * 0.08f, cy + r * 0.75f),
            strokeWidth = r * 0.11f,
            cap = StrokeCap.Round,
        )
        // مغز سبز کوچک
        drawOval(
            brush = Brush.radialGradient(
                listOf(Color(0xFFA9C452).copy(alpha = alpha), Color(0xFF6E8F2A).copy(alpha = 0.9f * alpha)),
            ),
            topLeft = Offset(cx + r * 0.14f, cy - r * 0.34f),
            size = Size(r * 0.34f, r * 0.5f),
        )
        // برق واقعی
        drawOval(
            brush = Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.5f * alpha), Color.Transparent),
            ),
            topLeft = Offset(cx - r * 0.52f, cy - r * 0.55f),
            size = Size(r * 0.55f, r * 0.38f),
        )
    }
}
