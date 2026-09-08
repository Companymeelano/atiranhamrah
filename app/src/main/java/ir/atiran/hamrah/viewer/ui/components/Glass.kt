package ir.atiran.hamrah.viewer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawBehind
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
/**
 * پنل شیشه‌ای نیمه‌شفاف با خط مویی — بلوک ساختمانی M•REPORT.
 *
 * سه لایه نور (فلسفه «نور منجمد»):
 * ۱. هاله رنگی پشت کارت با ته‌رنگ تم — سایه‌ها هرگز مشکی نیستند
 * ۲. سایه رنگی بسیار کم (elevation ۲dp)
 * ۳. نور زیر نقطه لمس — کاربر نور را لمس می‌کند، نه دکمه را
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 24.dp,
    fill: Color = LocalThemeExtras.current.glass,
    glow: Boolean = true,
    touchLight: Boolean = true,
    content: @Composable () -> Unit,
) {
    val extras = LocalThemeExtras.current
    val shape = RoundedCornerShape(corner)

    // ---- نور زیر انگشت (draw-only، بدون recomposition) ----
    val lightPos = remember { mutableStateOf<Offset?>(null) }
    val lastPos = remember { mutableStateOf(Offset.Zero) }
    val lightAlpha = remember { Animatable(0f) }
    val hasLight by remember { derivedStateOf { lightPos.value != null } }
    LaunchedEffect(hasLight) {
        lightAlpha.animateTo(
            targetValue = if (hasLight) 1f else 0f,
            animationSpec = tween(if (hasLight) 160 else 420),
        )
    }

    Box(modifier) {
        // ۱) هاله رنگی پشت کارت — نور از بالا، با ته‌رنگ تم
        if (glow) {
            Box(
                Modifier
                    .matchParentSize()
                    .drawBehind {
                        val pad = 16.dp.toPx()
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    extras.glow.copy(alpha = 0.10f),
                                    Color.Transparent,
                                ),
                                center = Offset(size.width * 0.5f, -size.height * 0.3f),
                                radius = size.width.coerceAtLeast(1f) * 1.15f,
                            ),
                            topLeft = Offset(-pad, -pad),
                            size = Size(size.width + pad * 2f, size.height + pad * 2f),
                            cornerRadius = CornerRadius(corner.toPx() + pad),
                        )
                    }
            )
        }
        // ۲+۳) بدنه شیشه‌ای + سایه رنگی + نور لمس
        Box(
            Modifier
                .shadow(
                    elevation = 2.dp,
                    shape = shape,
                    ambientColor = extras.glow.copy(alpha = 0.30f),
                    spotColor = extras.glow.copy(alpha = 0.45f),
                )
                .clip(shape)
                .background(fill)
                .drawWithContent {
                    drawContent()
                    if (touchLight && MotionFx.enabled && lightAlpha.value > 0.01f) {
                        val p = lightPos.value ?: lastPos.value
                        val r = 170.dp.toPx()
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    extras.glow.copy(alpha = 0.20f * lightAlpha.value),
                                    Color.Transparent,
                                ),
                                center = p,
                                radius = r,
                            ),
                            radius = r,
                            center = p,
                        )
                    }
                }
                .border(1.dp, extras.hairline, shape)
                .then(
                    if (touchLight) {
                        Modifier.pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                lightPos.value = down.position
                                lastPos.value = down.position
                                try {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val pressed = event.changes.lastOrNull { it.pressed }
                                        if (pressed == null) break
                                        lightPos.value = pressed.position
                                        lastPos.value = pressed.position
                                    }
                                } finally {
                                    lightPos.value = null
                                }
                            }
                        }
                    } else Modifier
                ),
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            ) {
                Box(Modifier.padding(16.dp)) { content() }
            }
        }
    }
}

/** سوئیچ سراسری افکت‌های حرکتی — از تنظیمات Interface Experience سینک می‌شود */
object MotionFx {
    @Volatile
    var enabled = true
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
    // فیزیک نور: با فشردن، دکمه به داخل فرو می‌رود و نورش کم می‌شود — نه بیرون‌زدن
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = tween(120),
        label = "pressScale",
    )
    val haloAlpha by animateFloatAsState(
        targetValue = if (pressed) 0.05f else 0.16f,
        animationSpec = tween(120),
        label = "pressHalo",
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(
                    elevation = 3.dp,
                    shape = CircleShape,
                    ambientColor = glow.copy(alpha = 0.35f),
                    spotColor = glow.copy(alpha = 0.55f),
                )
                .size(50.dp)
                .clip(CircleShape)
                .background(extras.glassStrong)
                .border(1.dp, extras.hairline, CircleShape)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // هاله بسیار کم پشت آیکون — با فشردن محو می‌شود
            Box(
                Modifier
                    .size(27.dp)
                    .clip(CircleShape)
                    .background(glow.copy(alpha = haloAlpha))
            )
            Icon(icon, contentDescription = label, tint = glow, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.size(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

// ============================================================ عدد قهرمان
/**
 * تایپوگرافی اعداد M•REPORT — عدد مثل لوگو تایپ می‌شود:
 * واحد کوچک و کم‌رنگ در بالای عدد (سبک مجلات ممتاز)،
 * عدد درشت با وزن Black و ارقام جدولی (tnum) تا ستون‌ها نلرزند.
 */
@Composable
fun NumberHero(
    value: String,
    unit: String? = null,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    fontSize: TextUnit = 33.sp,
) {
    Column(modifier) {
        if (unit != null) {
            Text(
                unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
            )
            Spacer(Modifier.height(2.dp))
        }
        Text(
            value,
            color = color,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = "tnum"),
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
