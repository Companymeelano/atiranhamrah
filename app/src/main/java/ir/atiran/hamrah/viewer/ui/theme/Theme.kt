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
/** وزیرمتن — فونت فارسی زیبا و خوانا برای کل برنامه */
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
/** چهار تم برنامه — دو تیره لاکچری + دو روشن */
enum class AppThemeId(val faName: String, val shortName: String, val isDark: Boolean) {
    GalaxyNight("شب کهکشانی", "کهکشان", true),
    RoyalEmerald("زمرد سلطنتی", "زمرد", true),
    BlossomMorning("صبح شکوفه", "شکوفه", false),
    IceSapphire("یاقوت آبی", "یاقوت", false),
}

/** رنگ‌های تکمیلی هر تم (چارت‌ها، طلایی برندینگ، ...) */
data class ThemeExtras(
    val chart: List<Color>,
    val gold: Color,
    val positive: Color,
    val goldGradient: List<Color>,
)

val LocalThemeExtras = staticCompositionLocalOf {
    ThemeExtras(
        chart = listOf(Color(0xFF9DA7FF), Color(0xFFFFD479), Color(0xFF6FE3B8), Color(0xFFFF8FA3), Color(0xFF7CD5FF)),
        gold = Color(0xFFFFD479),
        positive = Color(0xFF6FE3B8),
        goldGradient = listOf(Color(0xFFF7E7A5), Color(0xFFFFD479), Color(0xFFE8B84B)),
    )
}

// ------------------------------------------------ تم ۱: شب کهکشانی (تیره)
private val GalaxyNightScheme = darkColorScheme(
    primary = Color(0xFF9DA7FF),
    onPrimary = Color(0xFF141A66),
    primaryContainer = Color(0xFF333C8F),
    onPrimaryContainer = Color(0xFFE4E8FF),
    secondary = Color(0xFFFFD479),
    onSecondary = Color(0xFF4A3600),
    secondaryContainer = Color(0xFF5C470F),
    onSecondaryContainer = Color(0xFFFFE9B8),
    tertiary = Color(0xFFC98BFF),
    onTertiary = Color(0xFF3D1A66),
    tertiaryContainer = Color(0xFF5A3599),
    onTertiaryContainer = Color(0xFFF0DCFF),
    background = Color(0xFF0B1026),
    onBackground = Color(0xFFE3E6F5),
    surface = Color(0xFF121838),
    onSurface = Color(0xFFE3E6F5),
    surfaceVariant = Color(0xFF232B54),
    onSurfaceVariant = Color(0xFFB9C0DE),
    outline = Color(0xFF5A6390),
    outlineVariant = Color(0xFF333C6B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

// ------------------------------------------------ تم ۲: زمرد سلطنتی (تیره)
private val RoyalEmeraldScheme = darkColorScheme(
    primary = Color(0xFF47E0A9),
    onPrimary = Color(0xFF003828),
    primaryContainer = Color(0xFF0B5C43),
    onPrimaryContainer = Color(0xFFB8F5DD),
    secondary = Color(0xFFF2C14E),
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFF5B470F),
    onSecondaryContainer = Color(0xFFFFE9B0),
    tertiary = Color(0xFFFFB86B),
    onTertiary = Color(0xFF4A2800),
    tertiaryContainer = Color(0xFF6B3F0E),
    onTertiaryContainer = Color(0xFFFFDCC2),
    background = Color(0xFF06120D),
    onBackground = Color(0xFFDFF2E9),
    surface = Color(0xFF0D1F17),
    onSurface = Color(0xFFDFF2E9),
    surfaceVariant = Color(0xFF1C3328),
    onSurfaceVariant = Color(0xFFB5CCBF),
    outline = Color(0xFF5F7A6E),
    outlineVariant = Color(0xFF2E463A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

// ------------------------------------------------ تم ۳: صبح شکوفه (روشن)
private val BlossomMorningScheme = lightColorScheme(
    primary = Color(0xFFC25A2C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE4D4),
    onPrimaryContainer = Color(0xFF5B260E),
    secondary = Color(0xFF6D8B3F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6EFCB),
    onSecondaryContainer = Color(0xFF2B3A10),
    tertiary = Color(0xFFB07E1D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFEDC2),
    onTertiaryContainer = Color(0xFF5C4300),
    background = Color(0xFFFBF7EF),
    onBackground = Color(0xFF45403A),
    surface = Color(0xFFFFFCF5),
    onSurface = Color(0xFF45403A),
    surfaceVariant = Color(0xFFEDE5D5),
    onSurfaceVariant = Color(0xFF756F60),
    outline = Color(0xFF9C9482),
    outlineVariant = Color(0xFFE3DCCB),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

// ------------------------------------------------ تم ۴: یاقوت آبی (روشن)
private val IceSapphireScheme = lightColorScheme(
    primary = Color(0xFF1668C4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF0A2F66),
    secondary = Color(0xFF00897B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC9F0E6),
    onSecondaryContainer = Color(0xFF003731),
    tertiary = Color(0xFFC77E00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDDB8),
    onTertiaryContainer = Color(0xFF5C4300),
    background = Color(0xFFF3F7FC),
    onBackground = Color(0xFF1A1C1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1F),
    surfaceVariant = Color(0xFFDFE6EF),
    onSurfaceVariant = Color(0xFF42474E),
    outline = Color(0xFF72777F),
    outlineVariant = Color(0xFFC3C7CF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private fun schemeFor(id: AppThemeId) = when (id) {
    AppThemeId.GalaxyNight -> GalaxyNightScheme
    AppThemeId.RoyalEmerald -> RoyalEmeraldScheme
    AppThemeId.BlossomMorning -> BlossomMorningScheme
    AppThemeId.IceSapphire -> IceSapphireScheme
}

private fun extrasFor(id: AppThemeId) = when (id) {
    AppThemeId.GalaxyNight -> ThemeExtras(
        chart = listOf(Color(0xFF9DA7FF), Color(0xFFFFD479), Color(0xFF6FE3B8), Color(0xFFFF8FA3), Color(0xFF7CD5FF)),
        gold = Color(0xFFFFD479),
        positive = Color(0xFF6FE3B8),
        goldGradient = listOf(Color(0xFFF7E7A5), Color(0xFFFFD479), Color(0xFFE8B84B)),
    )
    AppThemeId.RoyalEmerald -> ThemeExtras(
        chart = listOf(Color(0xFF47E0A9), Color(0xFFF2C14E), Color(0xFFFFB86B), Color(0xFF7FC8FF), Color(0xFFD9A7FF)),
        gold = Color(0xFFF2C14E),
        positive = Color(0xFF47E0A9),
        goldGradient = listOf(Color(0xFFFFE9B0), Color(0xFFF2C14E), Color(0xFFD9A43A)),
    )
    AppThemeId.BlossomMorning -> ThemeExtras(
        chart = listOf(Color(0xFFC25A2C), Color(0xFF6D8B3F), Color(0xFFE0A82E), Color(0xFF4E8D7C), Color(0xFFA85C76)),
        gold = Color(0xFFB07E1D),
        positive = Color(0xFF3F8F4F),
        goldGradient = listOf(Color(0xFFD9A62E), Color(0xFFB07E1D), Color(0xFF8A5E0E)),
    )
    AppThemeId.IceSapphire -> ThemeExtras(
        chart = listOf(Color(0xFF1668C4), Color(0xFF00897B), Color(0xFFE08E00), Color(0xFF8E5BC8), Color(0xFFE05C7A)),
        gold = Color(0xFFC77E00),
        positive = Color(0xFF1E8E3E),
        goldGradient = listOf(Color(0xFFE8A93E), Color(0xFFC77E00), Color(0xFF9A5F00)),
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
