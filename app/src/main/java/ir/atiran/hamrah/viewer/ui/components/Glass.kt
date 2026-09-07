package ir.atiran.hamrah.viewer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import kotlin.math.PI
import kotlin.math.sin

// ============================================================ پس‌زمینه محیطی
/**
 * نور محیطی بسیار محو که با چرخه ~۴۵ ثانیه آرام جابه‌جا می‌شود.
 * کاربر تقریباً متوجه حرکت نمی‌شود، اما صفحه را زنده حس می‌کند.
 */
@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val transition = rememberInfiniteTransition(label = "ambient")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(45000, easing = LinearEasing)),
        label = "ambientT",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(scheme.background, scheme.surface, scheme.background)
                )
            ),
    ) {
        if (enabled) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val r = w * 0.75f
                fun blob(cx: Float, cy: Float, color: Color, alpha: Float) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(color.copy(alpha = alpha), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = r,
                        ),
                        radius = r,
                        center = Offset(cx, cy),
                    )
                }
                val a = t * 2f * PI.toFloat()
                blob(
                    w * (0.22f + 0.16f * sin(a)),
                    h * (0.28f + 0.10f * sin(a + 1.3f)),
                    extras.ambient[0], 0.10f,
                )
                blob(
                    w * (0.80f + 0.14f * sin(a + 2.1f)),
                    h * (0.20f + 0.12f * sin(a + 0.6f)),
                    extras.ambient[1], 0.08f,
                )
                blob(
                    w * (0.55f + 0.18f * sin(a + 4.2f)),
                    h * (0.85f + 0.08f * sin(a + 2.8f)),
                    extras.ambient[2], 0.07f,
                )
            }
        }
        content()
    }
}

// ============================================================ کارت شیشه‌ای
/** پنل شیشه‌ای نیمه‌شفاف با خط مویی — بلوک ساختمانی M•REPORT */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 20.dp,
    fill: Color = LocalThemeExtras.current.glass,
    content: @Composable () -> Unit,
) {
    val hairline = LocalThemeExtras.current.hairline
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(fill)
            .border(1.dp, hairline, RoundedCornerShape(corner)),
    ) {
        Box(Modifier.padding(14.dp)) { content() }
    }
}

// ============================================================ نبض آرام
/** چراغ با محو‌شدن بسیار آرام — نه چشمک تند */
@Composable
fun CalmPulse(color: Color, dotSize: Dp = 10.dp, enabled: Boolean = true) {
    if (!enabled) {
        Box(
            Modifier
                .size(dotSize)
                .clip(CircleShape)
                .background(color)
        )
        return
    }
    val tr = rememberInfiniteTransition(label = "calmPulse")
    val core by tr.animateFloat(
        initialValue = 1f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "core",
    )
    val halo by tr.animateFloat(
        initialValue = 0.55f, targetValue = 1.7f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "halo",
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(dotSize * halo)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.14f))
        )
        Box(
            Modifier
                .size(dotSize)
                .clip(CircleShape)
                .background(color.copy(alpha = core))
        )
    }
}

// ============================================================ دکمه-آیکون شیشه‌ای
/** Action به‌شکل Icon Object — آیکون داخل دایره شیشه‌ای با هاله رنگی */
@Composable
fun GlassAction(
    icon: ImageVector,
    label: String,
    glow: Color,
    onClick: () -> Unit,
) {
    val extras = LocalThemeExtras.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(extras.glassStrong)
                .border(1.dp, extras.hairline, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            // هاله بسیار کم پشت آیکون
            Box(
                Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(glow.copy(alpha = 0.16f))
            )
            Icon(icon, contentDescription = label, tint = glow, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.size(5.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ============================================================ خط نور زیر لوگو
/** خط بسیار ظریف که مثل نور از زیر عنوان عبور می‌کند */
@Composable
fun LightLine(width: Dp = 190.dp, alpha: Float = 0.9f) {
    val extras = LocalThemeExtras.current
    val c = extras.brand
    Box(
        Modifier
            .width(width)
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        c.first().copy(alpha = alpha * 0.5f),
                        c.last().copy(alpha = alpha),
                        c.first().copy(alpha = alpha * 0.5f),
                        Color.Transparent,
                    )
                )
            )
    )
}
