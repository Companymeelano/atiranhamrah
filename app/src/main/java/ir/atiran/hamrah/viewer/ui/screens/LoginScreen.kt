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
import androidx.compose.material.icons.filled.Lock
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
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import ir.atiran.hamrah.viewer.ui.theme.AppThemeId
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(extras.loginGradient))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))

            // لوگوی سه‌بعدی
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(3.dp, scheme.primary.copy(alpha = 0.55f), CircleShape),
            ) {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "آتیران همراه",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = scheme.onPrimary,
            )
            Text(
                "مدیریت هوشمند آجیل و خشکبار",
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onPrimary.copy(alpha = 0.85f),
            )

            Spacer(Modifier.height(22.dp))

            // کارت ورود — هم‌سبک با پنل‌های برنامه (حاشیه مویی به‌جای سایه جداشده)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.75f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "ورود به سیستم",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                    )

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
                                    contentDescription = if (showPass) "پنهان‌کردن رمز" else "نمایش رمز",
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

                    // انتخاب تم
                    ThemePicker(vm)

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
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                    ) {
                        Icon(Icons.Filled.PlayCircle, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("مشاهده دموی برنامه")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // برندینگ طراح
            DesignerFooter()

            Spacer(Modifier.height(28.dp))
        }
    }
}

// ------------------------------------------------------------ انتخاب تم
@Composable
private fun ThemePicker(vm: AppViewModel) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Palette,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.size(6.dp))
            Text(
                "ظاهر برنامه",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AppThemeId.entries.forEach { t ->
                val selected = vm.themeId == t.ordinal
                val swatch = themeSwatch(t)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(swatch))
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected) scheme.primary else scheme.outlineVariant,
                                shape = CircleShape,
                            )
                            .clickable { vm.setTheme(t.ordinal) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
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

/** رنگ نمایشی هر تم برای دایره انتخاب */
private fun themeSwatch(t: AppThemeId): List<Color> = when (t) {
    AppThemeId.OnyxGold -> listOf(Color(0xFFE3C579), Color(0xFF08080C))
    AppThemeId.VelvetRuby -> listOf(Color(0xFFF3C4CF), Color(0xFF140709))
    AppThemeId.PearlGold -> listOf(Color(0xFF8A6414), Color(0xFFFAF6EC))
    AppThemeId.PlatinumPistachio -> listOf(Color(0xFF1F7A5C), Color(0xFFF2F6F1))
}

// ------------------------------------------------------------ برندینگ
/** پلاک لوکس طراح — کارت با حاشیه طلایی، مونوگرام، نام گرادیانی و نشان استودیو */
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

    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, extras.gold.copy(alpha = 0.55f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                scheme.surface.copy(alpha = 0.0f),
                                scheme.surface.copy(alpha = 0.85f),
                                scheme.surface.copy(alpha = 0.0f),
                            )
                        )
                    )
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // مونوگرام
                    Box(
                        modifier = Modifier
                            .size(56.dp)
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
                        "طراحی و توسعه اپلیکیشن",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onPrimary.copy(alpha = 0.65f),
                    )
                    Spacer(Modifier.height(3.dp))
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
                    // جداکننده تزئینی
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .width(42.dp)
                                .height(1.dp)
                                .background(Brush.horizontalGradient(listOf(Color.Transparent, extras.gold.copy(alpha = 0.8f))))
                        )
                        Text(
                            "✦",
                            color = extras.gold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
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
                        "نسخه ${ir.atiran.hamrah.viewer.BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onPrimary.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}
