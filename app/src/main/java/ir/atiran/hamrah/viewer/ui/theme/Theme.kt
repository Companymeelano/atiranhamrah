package ir.atiran.hamrah.viewer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ir.atiran.hamrah.viewer.R

// ---------------------------------------------------------------- فونت
val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.W400),
    Font(R.font.vazirmatn_medium, FontWeight.W500),
    Font(R.font.vazirmatn_semibold, FontWeight.W600),
    Font(R.font.vazirmatn_bold, FontWeight.W700),
    Font(R.font.vazirmatn_black, FontWeight.W900),
)

private val baseTypography = Typography()

val AppTypography = Typography(
    displayLarge = baseTypography.displayLarge.copy(fontFamily = Vazirmatn),
    displayMedium = baseTypography.displayMedium.copy(fontFamily = Vazirmatn),
    displaySmall = baseTypography.displaySmall.copy(fontFamily = Vazirmatn),
    headlineLarge = baseTypography.headlineLarge.copy(fontFamily = Vazirmatn),
    headlineMedium = baseTypography.headlineMedium.copy(fontFamily = Vazirmatn),
    headlineSmall = baseTypography.headlineSmall.copy(fontFamily = Vazirmatn),
    titleLarge = baseTypography.titleLarge.copy(fontFamily = Vazirmatn),
    titleMedium = baseTypography.titleMedium.copy(fontFamily = Vazirmatn),
    titleSmall = baseTypography.titleSmall.copy(fontFamily = Vazirmatn),
    bodyLarge = baseTypography.bodyLarge.copy(fontFamily = Vazirmatn),
    bodyMedium = baseTypography.bodyMedium.copy(fontFamily = Vazirmatn),
    bodySmall = baseTypography.bodySmall.copy(fontFamily = Vazirmatn),
    labelLarge = baseTypography.labelLarge.copy(fontFamily = Vazirmatn),
    labelMedium = baseTypography.labelMedium.copy(fontFamily = Vazirmatn),
    labelSmall = baseTypography.labelSmall.copy(fontFamily = Vazirmatn),
)

// ---------------------------------------------------------------- تم‌ها
enum class AppThemeId(val faName: String, val shortName: String, val isDark: Boolean) {
    OnyxGold("شب ابریسم و طلا", "شب", true),
    VelvetRuby("مخمل یاقوت", "یاقوت", true),
    PearlGold("صدف طلایی", "صدف", false),
    PlatinumPistachio("پسته پلاتینی", "پسته", false),
}

data class ThemeExtras(
    val chart: List<Color>,
    val gold: Color,
    val positive: Color,
    val goldGradient: List<Color>,
    /** گرادیان پس‌زمینه صفحه ورود/بوت */
    val loginGradient: List<Color>,
    /** رنگ‌های نمایشی انتخابگر تم */
    val swatch: List<Color>,
    /** رنگ متن روی سطوح طلایی */
    val goldOn: Color,
)

val LocalThemeExtras = staticCompositionLocalOf {
    ThemeExtras(
        chart = listOf(Color(0xFFE3C579), Color(0xFF2FD9A5), Color(0xFFFFB86B), Color(0xFF9DA7FF), Color(0xFFFF8FA3)),
        gold = Color(0xFFE3C579),
        positive = Color(0xFF2FD9A5),
        goldGradient = listOf(Color(0xFFF9EDC8), Color(0xFFE3C579), Color(0xFFB08A3A)),
        loginGradient = listOf(Color(0xFF3A2E10), Color(0xFF08080C)),
        swatch = listOf(Color(0xFFE3C579), Color(0xFF08080C)),
        goldOn = Color(0xFF221A05),
    )
}

// ---------------------------------------- تم ۱: شب ابریشم و طلا (تیره لاکچری)
private val OnyxGoldScheme = darkColorScheme(
    primary = Color(0xFFE3C579),
    onPrimary = Color(0xFF221A05),
    primaryContainer = Color(0xFF3A2E10),
    onPrimaryContainer = Color(0xFFF7EAC4),
    secondary = Color(0xFF2FD9A5),
    onSecondary = Color(0xFF00382A),
    secondaryContainer = Color(0xFF0B4A37),
    onSecondaryContainer = Color(0xFFB8F5DD),
    tertiary = Color(0xFFFFB86B),
    onTertiary = Color(0xFF4A2800),
    tertiaryContainer = Color(0xFF5C3A12),
    onTertiaryContainer = Color(0xFFFFDCC2),
    background = Color(0xFF08080C),
    onBackground = Color(0xFFEDE6D4),
    surface = Color(0xFF101017),
    onSurface = Color(0xFFEDE6D4),
    surfaceVariant = Color(0xFF1C1C26),
    onSurfaceVariant = Color(0xFFB7B1A0),
    outline = Color(0xFF6B6654),
    outlineVariant = Color(0xFF2A2A36),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

// ---------------------------------------- تم ۲: مخمل یاقوت (تیره لاکچری)
private val VelvetRubyScheme = darkColorScheme(
    primary = Color(0xFFF3C4CF),
    onPrimary = Color(0xFF3E0A1C),
    primaryContainer = Color(0xFF5D2238),
    onPrimaryContainer = Color(0xFFFFDDE6),
    secondary = Color(0xFFE3C579),
    onSecondary = Color(0xFF221A05),
    secondaryContainer = Color(0xFF3F3110),
    onSecondaryContainer = Color(0xFFF7EAC4),
    tertiary = Color(0xFFFFB86B),
    onTertiary = Color(0xFF4A2800),
    tertiaryContainer = Color(0xFF5C3A12),
    onTertiaryContainer = Color(0xFFFFDCC2),
    background = Color(0xFF140709),
    onBackground = Color(0xFFF6E9EC),
    surface = Color(0xFF1D0D12),
    onSurface = Color(0xFFF6E9EC),
    surfaceVariant = Color(0xFF2B161D),
    onSurfaceVariant = Color(0xFFD4B9C0),
    outline = Color(0xFF8A6670),
    outlineVariant = Color(0xFF3A1F28),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

// ---------------------------------------- تم ۳: صدف طلایی (روشن لاکچری)
private val PearlGoldScheme = lightColorScheme(
    primary = Color(0xFF8A6414),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF6E9C5),
    onPrimaryContainer = Color(0xFF3D2B05),
    secondary = Color(0xFF2F6B5E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFF0E1),
    onSecondaryContainer = Color(0xFF06382A),
    tertiary = Color(0xFFB4552E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC2),
    onTertiaryContainer = Color(0xFF5C2408),
    background = Color(0xFFFAF6EC),
    onBackground = Color(0xFF423A2C),
    surface = Color(0xFFFFFCF5),
    onSurface = Color(0xFF423A2C),
    surfaceVariant = Color(0xFFF0E9D8),
    onSurfaceVariant = Color(0xFF6F6653),
    outline = Color(0xFFA39980),
    outlineVariant = Color(0xFFE6DEC9),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

// ---------------------------------------- تم ۴: پسته پلاتینی (روشن لاکچری)
private val PlatinumPistachioScheme = lightColorScheme(
    primary = Color(0xFF1F7A5C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFF0E1),
    onPrimaryContainer = Color(0xFF06382A),
    secondary = Color(0xFF9C6B1F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF6E9C5),
    onSecondaryContainer = Color(0xFF3D2B05),
    tertiary = Color(0xFF3E6E9E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD6E4FF),
    onTertiaryContainer = Color(0xFF0A2F66),
    background = Color(0xFFF2F6F1),
    onBackground = Color(0xFF22302A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF22302A),
    surfaceVariant = Color(0xFFE2ECE4),
    onSurfaceVariant = Color(0xFF55655D),
    outline = Color(0xFF8C9C92),
    outlineVariant = Color(0xFFD3E0D6),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private fun schemeFor(id: AppThemeId) = when (id) {
    AppThemeId.OnyxGold -> OnyxGoldScheme
    AppThemeId.VelvetRuby -> VelvetRubyScheme
    AppThemeId.PearlGold -> PearlGoldScheme
    AppThemeId.PlatinumPistachio -> PlatinumPistachioScheme
}

private fun extrasFor(id: AppThemeId) = when (id) {
    AppThemeId.OnyxGold -> ThemeExtras(
        chart = listOf(Color(0xFFE3C579), Color(0xFF2FD9A5), Color(0xFFFFB86B), Color(0xFF9DA7FF), Color(0xFFFF8FA3)),
        gold = Color(0xFFE3C579),
        positive = Color(0xFF2FD9A5),
        goldGradient = listOf(Color(0xFFF9EDC8), Color(0xFFE3C579), Color(0xFFB08A3A)),
        loginGradient = listOf(Color(0xFF3A2E10), Color(0xFF0A2A20), Color(0xFF08080C)),
        swatch = listOf(Color(0xFFE3C579), Color(0xFF08080C)),
        goldOn = Color(0xFF221A05),
    )
    AppThemeId.VelvetRuby -> ThemeExtras(
        chart = listOf(Color(0xFFF3C4CF), Color(0xFFE3C579), Color(0xFFFFB86B), Color(0xFFC98BFF), Color(0xFF7FC8FF)),
        gold = Color(0xFFE3C579),
        positive = Color(0xFF2FD9A5),
        goldGradient = listOf(Color(0xFFF9EDC8), Color(0xFFE3C579), Color(0xFFB08A3A)),
        loginGradient = listOf(Color(0xFF5D2238), Color(0xFF2E0E18), Color(0xFF140709)),
        swatch = listOf(Color(0xFFF3C4CF), Color(0xFF140709)),
        goldOn = Color(0xFF221A05),
    )
    AppThemeId.PearlGold -> ThemeExtras(
        chart = listOf(Color(0xFF8A6414), Color(0xFF2F6B5E), Color(0xFFB4552E), Color(0xFF3E6E9E), Color(0xFF8E5BC8)),
        gold = Color(0xFF8A6414),
        positive = Color(0xFF2F6B5E),
        goldGradient = listOf(Color(0xFFD9A62E), Color(0xFF8A6414), Color(0xFF6A4C0A)),
        loginGradient = listOf(Color(0xFF8A6414), Color(0xFFB4552E), Color(0xFF6A4C0A)),
        swatch = listOf(Color(0xFF8A6414), Color(0xFFFAF6EC)),
        goldOn = Color(0xFFFFFFFF),
    )
    AppThemeId.PlatinumPistachio -> ThemeExtras(
        chart = listOf(Color(0xFF1F7A5C), Color(0xFF9C6B1F), Color(0xFF3E6E9E), Color(0xFFB4552E), Color(0xFF8E5BC8)),
        gold = Color(0xFF9C6B1F),
        positive = Color(0xFF1F7A5C),
        goldGradient = listOf(Color(0xFFD9A62E), Color(0xFF9C6B1F), Color(0xFF7A520F)),
        loginGradient = listOf(Color(0xFF1F7A5C), Color(0xFF3E6E9E), Color(0xFF123A2C)),
        swatch = listOf(Color(0xFF1F7A5C), Color(0xFFF2F6F1)),
        goldOn = Color(0xFFFFFFFF),
    )
}

@Composable
fun AtiranAppTheme(themeId: AppThemeId, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalThemeExtras provides extrasFor(themeId)) {
        MaterialTheme(
            colorScheme = schemeFor(themeId),
            typography = AppTypography,
            content = content,
        )
    }
}
