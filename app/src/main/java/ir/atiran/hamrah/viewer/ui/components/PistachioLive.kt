package ir.atiran.hamrah.viewer.ui.components

/**
 * پستهٔ زندهٔ فتورئال 🥜 — همان کاراکتر رندر سه‌بعدی واقعی، اما زنده:
 * نفس می‌کشد، تکان می‌خورد، بغض می‌کند، گریه می‌کند (اشک واقعی روی تصویر)،
 * فکر می‌کند (نقطه‌های فکر) و چشمک می‌زند (برق ستاره‌ای).
 * جایگزین نسخهٔ Canvas در گفتگوی هوش مصنوعی.
 */
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.R
import kotlin.math.PI
import kotlin.math.sin

/** حالت‌های چهره پسته */
enum class PistachioMood { Happy, Wink, Sulk, Cry, Think }

@Composable
fun PistachioLive(
    size: Dp = 56.dp,
    modifier: Modifier = Modifier,
    bobbing: Boolean = true,
    mood: PistachioMood = PistachioMood.Happy,
    thinking: Boolean = false,
) {
    val tr = rememberInfiniteTransition(label = "pistLive")
    val t by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "pistLiveT",
    )
    val wig by tr.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1700, easing = LinearEasing)),
        label = "pistLiveWig",
    )

    val bob = if (bobbing) sin(t * 2f * PI.toFloat()) * 0.030f else 0f
    val tilt = when (mood) {
        PistachioMood.Sulk -> -7f
        PistachioMood.Wink -> sin(t * 2f * PI.toFloat()) * 3.5f
        else -> sin(t * PI.toFloat()) * 1.6f
    }
    val breathe = 1f + sin(t * 2f * PI.toFloat()) * 0.012f

    Box(
        modifier
            .size(size)
            .graphicsLayer {
                translationY = bob * this.size.height
                rotationZ = tilt
                scaleX = breathe
                scaleY = breathe
            }
    ) {
        Image(
            painter = painterResource(R.drawable.mascot_pistachio),
            contentDescription = "پسته",
            modifier = Modifier.fillMaxSize(),
        )
        // اورلی‌های زنده روی چهرهٔ واقعی
        Canvas(Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            when {
                mood == PistachioMood.Cry -> {
                    // اشک‌های واقعی — دو قطرهٔ سرازیر شونده از چشم‌ها
                    listOf(0.415f to 0.470f, 0.615f to 0.475f).forEach { (ex, ey) ->
                        val fall = (t * 1.6f) % 1f
                        val ty = ey + fall * 0.11f
                        drawOval(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFFBFE6FA).copy(alpha = (1f - fall * 0.6f)), Color(0xFF5FB0DC).copy(alpha = (1f - fall) * 0.8f)),
                            ),
                            topLeft = Offset(w * (ex - 0.016f), h * ty),
                            size = Size(w * 0.032f, h * 0.050f),
                        )
                    }
                }
                mood == PistachioMood.Think || thinking -> {
                    // نقطه‌های فکر — بالای سر، پلک‌انبازی
                    listOf(Offset(0.80f, 0.235f), Offset(0.865f, 0.185f), Offset(0.92f, 0.125f)).forEachIndexed { i, c ->
                        val ph = (t * 1.5f + i * 0.33f) % 1f
                        drawCircle(
                            Color(0xFFE9C777).copy(alpha = 0.25f + 0.65f * ph),
                            radius = w * (0.011f + i * 0.003f),
                            center = Offset(w * c.x + sin(ph * PI.toFloat()) * w * 0.008f, h * (c.y - ph * 0.02f)),
                        )
                    }
                }
                mood == PistachioMood.Wink -> {
                    // برق ستاره‌ای کنار چشم
                    val sp = (t * 1.2f) % 1f
                    val scx = w * 0.72f
                    val scy = h * 0.34f - sp * h * 0.03f
                    val sr = w * 0.028f * (1f - sp * 0.5f)
                    rotate(45f, pivot = Offset(scx, scy)) {
                        drawLine(Color.White.copy(alpha = 0.9f * (1f - sp)), Offset(scx - sr, scy), Offset(scx + sr, scy), strokeWidth = w * 0.006f)
                        drawLine(Color.White.copy(alpha = 0.9f * (1f - sp)), Offset(scx, scy - sr), Offset(scx, scy + sr), strokeWidth = w * 0.006f)
                    }
                }
                mood == PistachioMood.Happy -> {
                    // قلب کوچک بالای سر — عشق به کاربر
                    if (sin(wig * PI / 2) > 0.86f) {
                        val hy = h * (0.16f - ((t * 1.2f) % 1f) * 0.04f)
                        drawCircle(Color(0xFFE57373).copy(alpha = 0.85f), radius = w * 0.016f, center = Offset(w * 0.63f, hy))
                        drawCircle(Color(0xFFE57373).copy(alpha = 0.85f), radius = w * 0.016f, center = Offset(w * 0.66f, hy))
                        val tri = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.614f, hy + w * 0.008f)
                            lineTo(w * 0.676f, hy + w * 0.008f)
                            lineTo(w * 0.645f, hy + w * 0.036f)
                            close()
                        }
                        drawPath(tri, Color(0xFFE57373).copy(alpha = 0.85f))
                    }
                }
                else -> {}
            }
        }
    }
}
