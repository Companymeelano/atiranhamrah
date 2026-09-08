package ir.atiran.hamrah.viewer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ir.atiran.hamrah.viewer.data.DbSettings
import ir.atiran.hamrah.viewer.data.SqlServerDb
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.AmbientBackground
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import ir.atiran.hamrah.viewer.ui.components.LightLine
import ir.atiran.hamrah.viewer.ui.components.MrIcons
import ir.atiran.hamrah.viewer.ui.components.AiSettingsSheet
import ir.atiran.hamrah.viewer.ui.components.MrOrbButton
import ir.atiran.hamrah.viewer.ui.components.MrPillButton
import ir.atiran.hamrah.viewer.ui.components.MrSubtitle
import ir.atiran.hamrah.viewer.ui.theme.AppThemeId
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import ir.atiran.hamrah.viewer.utils.SoundFx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(vm: AppViewModel) {
    val saved by vm.settings.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current

    var host by remember { mutableStateOf(saved.host) }
    var port by remember { mutableStateOf(saved.port) }
    var database by remember { mutableStateOf(saved.database) }
    var user by remember { mutableStateOf(saved.user) }
    var password by remember { mutableStateOf(saved.password) }
    var showPass by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(saved.remember) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var themeSheet by remember { mutableStateOf(false) }
    var soundSheet by remember { mutableStateOf(false) }
    var aiSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AmbientBackground(enabled = vm.experience.ambient) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(18.dp))

            // ---------- نوار بالا: دو آیکون شیشه‌ای ظریف (تم + صدا) ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MrOrbButton(
                    MrIcons.Theme, "شخصیت بصری",
                    tint = extras.gold,
                    onClick = { themeSheet = true; SoundFx.soft() },
                )
                Spacer(Modifier.width(8.dp))
                MrOrbButton(
                    MrIcons.Waves, "تجربه رابط",
                    tint = extras.accent,
                    onClick = { soundSheet = true; SoundFx.soft() },
                )
                Spacer(Modifier.width(8.dp))
                MrOrbButton(
                    MrIcons.Spark, "دستیار هوشمند پسته",
                    tint = Color(0xFF8FB260),
                    onClick = { aiSheet = true; SoundFx.soft() },
                )
            }

            Spacer(Modifier.height(26.dp))

            // ---------- پوستر M•REPORT — با جاروی نور ورودی ----------
            val entry = remember { androidx.compose.animation.core.Animatable(0f) }
            LaunchedEffect(Unit) {
                entry.animateTo(1f, tween(1200, easing = androidx.compose.animation.core.LinearEasing))
            }
            val et = entry.value
            val titleBrush = if (et > 0f && et < 1f) {
                val band = 0.3f
                val c = (et * (1f + band) - band / 2f)
                val hi = androidx.compose.ui.graphics.lerp(extras.brand[1], Color.White, 0.85f)
                Brush.horizontalGradient(
                    0f to extras.brand.first(),
                    (c - band / 2f).coerceIn(0f, 1f) to extras.brand[1],
                    c.coerceIn(0f, 1f) to hi,
                    (c + band / 2f).coerceIn(0f, 1f) to extras.brand[1],
                    1f to extras.brand.last(),
                )
            } else {
                Brush.horizontalGradient(extras.brand)
            }
            Text(
                "M•REPORT",
                style = TextStyle(
                    brush = titleBrush,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                ),
            )
            Spacer(Modifier.height(6.dp))
            MrSubtitle("Intelligent Reporting Experience")
            Spacer(Modifier.height(14.dp))
            LightLine(width = 210.dp)

            Spacer(Modifier.height(28.dp))

            // ---------- کارت ورود (فقط ضروری‌ها) ----------
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface.copy(alpha = 0.94f)),
                border = BorderStroke(1.dp, extras.hairline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("سرور (IP)") },
                        placeholder = { Text("37.143.147.19") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Dns, contentDescription = null) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it },
                            label = { Text("پورت") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = database,
                            onValueChange = { database = it },
                            label = { Text("نام دیتابیس") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Storage, contentDescription = null) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(2f),
                        )
                    }

                    OutlinedTextField(
                        value = user,
                        onValueChange = { user = it },
                        label = { Text("نام کاربری") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("رمز عبور") },
                        singleLine = true,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null,
                                )
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { rememberMe = !rememberMe }
                            .padding(vertical = 2.dp),
                    ) {
                        Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                        Text("اطلاعات ورود را به خاطر بسپار", style = MaterialTheme.typography.bodyMedium)
                    }

                    ErrorBanner(error)

                    Button(
                        onClick = {
                            busy = true
                            error = null
                            scope.launch {
                                try {
                                    val cfg = DbSettings(
                                        host = host.trim(),
                                        port = port.trim().ifBlank { "1433" },
                                        database = database.trim(),
                                        user = user.trim(),
                                        password = password,
                                        remember = rememberMe,
                                    )
                                    vm.login(cfg)
                                    SoundFx.success()
                                } catch (e: Throwable) {
                                    error = SqlServerDb.friendly(e)
                                } finally {
                                    busy = false
                                }
                            }
                        },
                        enabled = !busy && host.isNotBlank() && database.isNotBlank() &&
                            user.isNotBlank() && password.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = scheme.primary,
                            contentColor = scheme.onPrimary,
                        ),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        if (busy) {
                            CircularProgressIndicator(
                                color = scheme.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(Modifier.size(10.dp))
                            Text("در حال اتصال...")
                        } else {
                            Text("ورود", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    OutlinedButton(
                        onClick = { vm.openDemo() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.primary),
                        border = BorderStroke(1.dp, extras.hairline),
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                    ) {
                        Icon(Icons.Filled.PlayCircle, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("مشاهده تجربه M•REPORT")
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ---------- امضای فشرده طراح ----------
            DesignerStrip()

            Spacer(Modifier.height(26.dp))
        }
    }

    if (busy) {
        MrLoadingOverlay()
    }
    if (themeSheet) {
        ThemeSheet(vm) { themeSheet = false }
    }
    if (soundSheet) {
        ExperienceSheet(vm) { soundSheet = false }
    }
    if (aiSheet) {
        AiSettingsSheet(vm) { aiSheet = false }
    }
}

// ------------------------------------------------------------ لودینگ اختصاصی M•REPORT
/**
 * تجربه ورود: M• گرادیانی + خط نور متحرک + توضیح کوتاه + صدای نرم —
 * هماهنگ با تم انتخابی، سپس ورود به برنامه.
 */
@Composable
private fun MrLoadingOverlay() {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    LaunchedEffect(Unit) { SoundFx.soft() }
    val tr = rememberInfiniteTransition(label = "mrLoading")
    val sweep = tr.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "sweep",
    ).value
    Dialog(onDismissRequest = {}, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(extras.loginGradient)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "M•",
                    style = TextStyle(
                        brush = Brush.horizontalGradient(extras.brand),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                    ),
                )
                Spacer(Modifier.height(16.dp))
                // خط نور که آرام رفت‌وآمد می‌کند
                Box(
                    Modifier
                        .width(170.dp)
                        .height(2.dp)
                        .background(extras.hairline),
                ) {
                    Box(
                        modifier = Modifier
                            .absoluteOffset(x = (sweep * 55).dp)
                            .width(60.dp)
                            .height(2.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        extras.brand[1],
                                        extras.brand.last(),
                                        Color.Transparent,
                                    )
                                )
                            ),
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "در حال اتصال به سرویس آتیران...",
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 1.sp,
                    color = scheme.onBackground.copy(alpha = 0.75f),
                )
            }
        }
    }
}

// ------------------------------------------------------------ شیت انتخاب تم
@Composable
fun ThemeSheet(vm: AppViewModel, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.surface.copy(alpha = 0.97f))
                .border(1.dp, extras.hairline, RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Palette, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("شخصیت بصری", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            AppThemeId.entries.forEach { t ->
                val selected = vm.themeId == t.ordinal
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) scheme.primaryContainer.copy(alpha = 0.5f) else extras.glass)
                        .border(
                            1.dp,
                            if (selected) scheme.primary else extras.hairline,
                            RoundedCornerShape(14.dp),
                        )
                        .clickable { vm.setTheme(t.ordinal); SoundFx.soft(); onDismiss() }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(themeSwatch(t)))
                            .border(1.dp, extras.hairline, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(t.shortName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(t.faName, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    }
                }
            }
            // ذخیره + اطلاع‌رسانی
            var savedTheme by remember { mutableStateOf(false) }
            val themeScope = rememberCoroutineScope()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (savedTheme) {
                    Text(
                        "تنظیمات ذخیره شد ✓",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                MrPillButton(
                    label = if (savedTheme) "ذخیره شد ✓" else "ذخیره",
                    icon = MrIcons.Bookmark,
                    onClick = {
                        if (!savedTheme) {
                            savedTheme = true
                            SoundFx.success()
                            themeScope.launch { delay(1300); onDismiss() }
                        }
                    },
                )
            }
        }
    }
}

private fun themeSwatch(t: AppThemeId): List<Color> = when (t) {
    AppThemeId.Obsidian -> listOf(Color(0xFFB4A0FF), Color(0xFF0C0C13))
    AppThemeId.MilanoRoyale -> listOf(Color(0xFFE3C579), Color(0xFF12110D))
    AppThemeId.Pearl -> listOf(Color(0xFF5B8DEF), Color(0xFFF4F6FA))
    AppThemeId.Ivory -> listOf(Color(0xFFA3B894), Color(0xFFF7F4EC))
    AppThemeId.Emerald -> listOf(Color(0xFF34D399), Color(0xFF0F172A))
}

// ------------------------------------------------------------ شیت تجربه رابط
@Composable
fun ExperienceSheet(vm: AppViewModel, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val exp = vm.experience
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.surface.copy(alpha = 0.97f))
                .border(1.dp, LocalThemeExtras.current.hairline, RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Interface Experience", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            ExpRow("Sound Effects", "صداهای کوتاه فقط برای رویدادهای مهم", exp.sound) {
                vm.updateExperience(exp.copy(sound = it))
            }
            ExpRow("Motion Effects", "انیمیشن‌های نرم نمودارها و کارت‌ها", exp.motion) {
                vm.updateExperience(exp.copy(motion = it))
            }
            ExpRow("Ambient Effects", "نور محیطی بسیار محو پس‌زمینه", exp.ambient) {
                vm.updateExperience(exp.copy(ambient = it))
            }
            // ذخیره + اطلاع‌رسانی
            var savedExp by remember { mutableStateOf(false) }
            val expScope = rememberCoroutineScope()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (savedExp) {
                    Text(
                        "تنظیمات ذخیره شد ✓",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                MrPillButton(
                    label = if (savedExp) "ذخیره شد ✓" else "ذخیره",
                    icon = MrIcons.Bookmark,
                    onClick = {
                        if (!savedExp) {
                            savedExp = true
                            SoundFx.success()
                            expScope.launch { delay(1300); onDismiss() }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ExpRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
        )
    }
}

// ------------------------------------------------------------ امضای فشرده طراح
/** یک خط ظریف — مونوگرام کوچک + نام · استودیو؛ بدون اشغال فضا */
@OptIn(ExperimentalTextApi::class)
@Composable
private fun DesignerStrip() {
    val extras = LocalThemeExtras.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(extras.goldGradient))
                .border(0.5.dp, Color.White.copy(alpha = 0.22f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("MY", color = extras.goldOn, fontSize = 7.5.sp, fontWeight = FontWeight.Black)
        }
        Text(
            "Milad Yaghoobi",
            style = TextStyle(
                brush = Brush.horizontalGradient(extras.goldGradient),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp,
            ),
        )
        Text("·", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f), fontSize = 11.sp)
        Text(
            "Meelano Studio Design",
            style = TextStyle(
                brush = Brush.horizontalGradient(extras.goldGradient),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
            ),
        )
    }
}
