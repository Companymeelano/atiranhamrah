package ir.atiran.hamrah.viewer.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.atiran.hamrah.viewer.R
import ir.atiran.hamrah.viewer.data.DbSettings
import ir.atiran.hamrah.viewer.data.SqlServerDb
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.AmbientBackground
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import ir.atiran.hamrah.viewer.ui.components.LightLine
import ir.atiran.hamrah.viewer.ui.theme.AppThemeId
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import ir.atiran.hamrah.viewer.utils.SoundFx
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
    val scope = rememberCoroutineScope()

    AmbientBackground(enabled = vm.experience.ambient) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(44.dp))

            // ---------- پوستر M•REPORT ----------
            Text(
                "M•REPORT",
                style = TextStyle(
                    brush = Brush.horizontalGradient(extras.metallic),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                ),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Intelligent Reporting Experience",
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 4.sp,
                color = scheme.onPrimary.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(12.dp))
            LightLine(width = 210.dp)

            Spacer(Modifier.height(24.dp))

            // کارت ورود شیشه‌ای
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface.copy(alpha = 0.92f)),
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

                    // ---------- چهار شخصیت تم ----------
                    ThemePicker(vm)

                    // ---------- Interface Experience ----------
                    ExperiencePanel(vm)

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
                                } catch (e: Exception) {
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
                            Text(
                                "ورود",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                            )
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

            Spacer(Modifier.height(20.dp))
            DesignerFooter()
            Spacer(Modifier.height(28.dp))
        }
    }
}

// ------------------------------------------------------------ انتخاب تم
@Composable
private fun ThemePicker(vm: AppViewModel) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Palette, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text(
                "شخصیت بصری",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AppThemeId.entries.forEach { t ->
                val selected = vm.themeId == t.ordinal
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(themeSwatch(t)))
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected) scheme.primary else extras.hairline,
                                shape = CircleShape,
                            )
                            .clickable {
                                vm.setTheme(t.ordinal)
                                SoundFx.soft()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        t.shortName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) scheme.primary else scheme.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

private fun themeSwatch(t: AppThemeId): List<Color> = when (t) {
    AppThemeId.Obsidian -> listOf(Color(0xFFB4A0FF), Color(0xFF0C0C13))
    AppThemeId.MilanoRoyale -> listOf(Color(0xFFE3C579), Color(0xFF12110D))
    AppThemeId.Pearl -> listOf(Color(0xFF5B8DEF), Color(0xFFF4F6FA))
    AppThemeId.Ivory -> listOf(Color(0xFFA3B894), Color(0xFFF7F4EC))
}

// ------------------------------------------------------------ تنظیمات تجربه
@Composable
private fun ExperiencePanel(vm: AppViewModel) {
    val scheme = MaterialTheme.colorScheme
    val exp = vm.experience
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text(
                "Interface Experience",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(4.dp))
        ExperienceRow(
            icon = Icons.Filled.MusicNote,
            title = "Sound Effects",
            subtitle = "صداهای کوتاه فقط برای رویدادهای مهم",
            checked = exp.sound,
        ) { vm.updateExperience(exp.copy(sound = it)) }
        ExperienceRow(
            icon = Icons.Filled.GraphicEq,
            title = "Motion Effects",
            subtitle = "انیمیشن‌های نرم نمودارها و کارت‌ها",
            checked = exp.motion,
        ) { vm.updateExperience(exp.copy(motion = it)) }
        ExperienceRow(
            icon = Icons.Filled.Palette,
            title = "Ambient Effects",
            subtitle = "نور محیطی بسیار محو پس‌زمینه",
            checked = exp.ambient,
        ) { vm.updateExperience(exp.copy(ambient = it)) }
    }
}

@Composable
private fun ExperienceRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Icon(icon, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = scheme.primary),
        )
    }
}

// ------------------------------------------------------------ برندینگ
@OptIn(ExperimentalTextApi::class)
@Composable
private fun DesignerFooter() {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val shimmer = rememberInfiniteTransition(label = "footerShimmer")
    val shine by shimmer.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Reverse),
        label = "footerShine",
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .border(
                    width = 2.dp,
                    color = extras.gold.copy(alpha = 0.4f + 0.3f * shine),
                    shape = CircleShape,
                )
                .padding(3.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(extras.goldGradient)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "MY",
                color = extras.goldOn,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Milad Yaghoobi",
            style = TextStyle(
                brush = Brush.linearGradient(extras.goldGradient),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
            ),
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(42.dp)
                    .height(1.dp)
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, extras.gold.copy(alpha = 0.8f))))
            )
            Text("✦", color = extras.gold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
            Box(
                Modifier
                    .width(42.dp)
                    .height(1.dp)
                    .background(Brush.horizontalGradient(listOf(extras.gold.copy(alpha = 0.8f), Color.Transparent)))
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "M E E L A N O   S T U D I O   D E S I G N",
            style = TextStyle(
                brush = Brush.linearGradient(extras.goldGradient),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
            ),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "نسخه ${ir.atiran.hamrah.viewer.BuildConfig.VERSION_NAME} • M•REPORT",
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onPrimary.copy(alpha = 0.5f),
        )
    }
}
