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
/** چهار شخصیت واقعی — نه فقط تعویض رنگ پس‌زمینه */
enum class AppThemeId(val faName: String, val shortName: String, val isDark: Boolean) {
    Obsidian("اُبسیدین — سایبر لاکچری", "اُبسیدین", true),
    MilanoRoyale("میلانو رویال — فایننس لوکس", "میلانو", true),
    Pearl("پرل — سوئیس فایننس", "پرل", false),
    Ivory("آیووری — ادیتوریال پرمیوم", "آیووری", false),
}

/**
 * توکن‌های زبان بصری «Luxury Data Glass / Milano Future»:
 * شیشه نیمه‌شفاف، خطوط مویی، متالیک کنترل‌شده، نورهای محیطی محو.
 */
data class ThemeExtras(
    /** پالت ۵رنگه چارت‌ها */
    val chart: List<Color>,
    /** رنگ متالیک اصلی */
    val gold: Color,
    val positive: Color,
    /** گرادیان متالیک (جاروی کنترل‌شده) */
    val goldGradient: List<Color>,
    /** گرادیان پس‌زمینه ورود */
    val loginGradient: List<Color>,
    /** نمایش انتخابگر تم */
    val swatch: List<Color>,
    /** متن روی سطوح متالیک */
    val goldOn: Color,
    // ---------- توکن‌های شیشه ----------
    /** گرادیان امضایی متالیک (۲-۳ توقف، بسیار کنترل‌شده) */
    val metallic: List<Color>,
    /** رنگ اکسنت بسیار محدود */
    val accent: Color,
    /** پرکردن شیشه‌ای نیمه‌شفاف */
    val glass: Color,
    /** شیشه پررنگ‌تر برای overlay ها */
    val glassStrong: Color,
    /** خط مویی */
    val hairline: Color,
    /** رنگ‌های نور محیطی (آلفا در کامپوننت اعمال می‌شود) */
    val ambient: List<Color>,
    /** گرادیان برند M•REPORT — در تم‌های روشن تیره‌تر برای کنتراست */
    val brand: List<Color>,
    /** ته‌رنگ نور و سایه‌ها — سایه‌ها هرگز مشکی نیستند */
    val glow: Color,
)

val LocalThemeExtras = staticCompositionLocalOf {
    ThemeExtras(
        chart = listOf(Color(0xFFB4A0FF), Color(0xFF9BD4FF), Color(0xFFE8CFA0), Color(0xFF7CE8C3), Color(0xFFFF9EB8)),
        gold = Color(0xFFB4A0FF),
        positive = Color(0xFF7CE8C3),
        goldGradient = listOf(Color(0xFFD2C6FF), Color(0xFFB4A0FF), Color(0xFF8A76E8)),
        loginGradient = listOf(Color(0xFF15102E), Color(0xFF07070B)),
        swatch = listOf(Color(0xFFB4A0FF), Color(0xFF0C0C13)),
        goldOn = Color(0xFF231A4D),
        metallic = listOf(Color(0xFFB4A0FF), Color(0xFF8FA8FF), Color(0xFF9BD4FF)),
        accent = Color(0xFF9BD4FF),
        glass = Color(0x0FFFFFFF),
        glassStrong = Color(0x17FFFFFF),
        hairline = Color(0x1AFFFFFF),
        ambient = listOf(Color(0xFF6E4EFF), Color(0xFF3E7BD9), Color(0xFF4A2A8A)),
        brand = listOf(Color(0xFFB4A0FF), Color(0xFF8FA8FF), Color(0xFF9BD4FF)),
        glow = Color(0xFF6E4EFF),
    )
}

// ---------------------------------------- تم ۱: اُبسیدین (مشکی + بنفش + یخ)
private val ObsidianScheme = darkColorScheme(
    primary = Color(0xFFB4A0FF),
    onPrimary = Color(0xFF231A4D),
    primaryContainer = Color(0xFF2E2657),
    onPrimaryContainer = Color(0xFFE7E0FF),
    secondary = Color(0xFF9BD4FF),
    onSecondary = Color(0xFF00344F),
    secondaryContainer = Color(0xFF1C3A52),
    onSecondaryContainer = Color(0xFFD4EAFF),
    tertiary = Color(0xFFE8CFA0),
    onTertiary = Color(0xFF402D0E),
    tertiaryContainer = Color(0xFF544224),
    onTertiaryContainer = Color(0xFFFFDDAF),
    background = Color(0xFF07070B),
    onBackground = Color(0xFFE4E1EC),
    surface = Color(0xFF0C0C13),
    surfaceDim = Color(0xFF09090F),
    surfaceBright = Color(0xFF3A3A4A),
    surfaceContainer = Color(0xFF15151F),
    surfaceContainerHigh = Color(0xFF1B1B27),
    surfaceContainerHighest = Color(0xFF232331),
    onSurface = Color(0xFFE4E1EC),
    surfaceVariant = Color(0xFF17171F),
    onSurfaceVariant = Color(0xFFC4C0D6),
    outline = Color(0xFF56536B),
    outlineVariant = Color(0xFF282734),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val ObsidianExtras = ThemeExtras(
    chart = listOf(Color(0xFFB4A0FF), Color(0xFF9BD4FF), Color(0xFFE8CFA0), Color(0xFF7CE8C3), Color(0xFFFF9EB8)),
    gold = Color(0xFFB4A0FF),
    positive = Color(0xFF7CE8C3),
    goldGradient = listOf(Color(0xFFD2C6FF), Color(0xFFB4A0FF), Color(0xFF8A76E8)),
    loginGradient = listOf(Color(0xFF15102E), Color(0xFF0A0A14), Color(0xFF07070B)),
    swatch = listOf(Color(0xFFB4A0FF), Color(0xFF0C0C13)),
    goldOn = Color(0xFF231A4D),
    metallic = listOf(Color(0xFFB4A0FF), Color(0xFF8FA8FF), Color(0xFF9BD4FF)),
    accent = Color(0xFF9BD4FF),
    glass = Color(0x0BFFFFFF),
    glassStrong = Color(0x17FFFFFF),
    hairline = Color(0x1AFFFFFF),
    ambient = listOf(Color(0xFF6E4EFF), Color(0xFF3E7BD9), Color(0xFF4A2A8A)),
    brand = listOf(Color(0xFFB4A0FF), Color(0xFF8FA8FF), Color(0xFF9BD4FF)),
    glow = Color(0xFF6E4EFF),
)

// ---------------------------------------- تم ۲: میلانو رویال (گرافیتی + طلای شامپاینی)
private val MilanoRoyaleScheme = darkColorScheme(
    primary = Color(0xFFE3C579),
    onPrimary = Color(0xFF241B04),
    primaryContainer = Color(0xFF3D2F10),
    onPrimaryContainer = Color(0xFFF8ECC8),
    secondary = Color(0xFFC89B5A),
    onSecondary = Color(0xFF2E2005),
    secondaryContainer = Color(0xFF4A3818),
    onSecondaryContainer = Color(0xFFF5E3BF),
    tertiary = Color(0xFFA9C6D9),
    onTertiary = Color(0xFF0E2A3A),
    tertiaryContainer = Color(0xFF2A4152),
    onTertiaryContainer = Color(0xFFD3E7F5),
    background = Color(0xFF0B0A08),
    onBackground = Color(0xFFE9E3D6),
    surface = Color(0xFF12110D),
    surfaceDim = Color(0xFF0E0D0A),
    surfaceBright = Color(0xFF3E3A2E),
    surfaceContainer = Color(0xFF1A1812),
    surfaceContainerHigh = Color(0xFF201E15),
    surfaceContainerHighest = Color(0xFF292619),
    onSurface = Color(0xFFE9E3D6),
    surfaceVariant = Color(0xFF1D1B15),
    onSurfaceVariant = Color(0xFFCFC7B0),
    outline = Color(0xFF6A644F),
    outlineVariant = Color(0xFF2E2B20),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val MilanoExtras = ThemeExtras(
    chart = listOf(Color(0xFFE3C579), Color(0xFFC89B5A), Color(0xFFA9C6D9), Color(0xFFE8B4A0), Color(0xFF9CC9A8)),
    gold = Color(0xFFE3C579),
    positive = Color(0xFF9CC9A8),
    goldGradient = listOf(Color(0xFFF6EBCC), Color(0xFFE3C579), Color(0xFFBE9450)),
    loginGradient = listOf(Color(0xFF2A2110), Color(0xFF141108), Color(0xFF0B0A08)),
    swatch = listOf(Color(0xFFE3C579), Color(0xFF12110D)),
    goldOn = Color(0xFF241B04),
    metallic = listOf(Color(0xFFF2E0AC), Color(0xFFE3C579), Color(0xFFBE9450)),
    accent = Color(0xFFE8CFA0),
    glass = Color(0x0DFFFFFF),
    glassStrong = Color(0x1AFFFFFF),
    hairline = Color(0x1CFFFFFF),
    ambient = listOf(Color(0xFFB98F44), Color(0xFF6A4E26), Color(0xFF43506A)),
    brand = listOf(Color(0xFFF2E0AC), Color(0xFFE3C579), Color(0xFFBE9450)),
    glow = Color(0xFFC9A45C),
)

// ---------------------------------------- تم ۳: پرل (سفید + نقره + آبی آسمانی)
private val PearlScheme = lightColorScheme(
    primary = Color(0xFF2E6BE6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE7FB),
    onPrimaryContainer = Color(0xFF0A2A6B),
    secondary = Color(0xFF55677E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9E1F0),
    onSecondaryContainer = Color(0xFF152234),
    tertiary = Color(0xFFC9A45C),
    onTertiary = Color(0xFF3B2D0B),
    tertiaryContainer = Color(0xFFF6E8C8),
    onTertiaryContainer = Color(0xFF56430F),
    background = Color(0xFFF4F6FA),
    onBackground = Color(0xFF171A20),
    surface = Color(0xFFFCFDFF),
    surfaceDim = Color(0xFFDDDFE8),
    surfaceBright = Color(0xFFFAF9FE),
    surfaceContainer = Color(0xFFEDF0F7),
    surfaceContainerHigh = Color(0xFFE8ECF4),
    surfaceContainerHighest = Color(0xFFE2E7F0),
    onSurface = Color(0xFF171A20),
    surfaceVariant = Color(0xFFE5E9F2),
    onSurfaceVariant = Color(0xFF5A6270),
    outline = Color(0xFF8A92A1),
    outlineVariant = Color(0xFFC9CFDA),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val PearlExtras = ThemeExtras(
    chart = listOf(Color(0xFF2E6BE6), Color(0xFF7C9BE8), Color(0xFFC9A45C), Color(0xFF4FB8A8), Color(0xFFE08AA8)),
    gold = Color(0xFF2E6BE6),
    positive = Color(0xFF2E9E6B),
    goldGradient = listOf(Color(0xFF8FB0F2), Color(0xFF5B8DEF), Color(0xFF2E6BE6)),
    loginGradient = listOf(Color(0xFF5B8DEF), Color(0xFF2E5CB8), Color(0xFF1E3A6E)),
    swatch = listOf(Color(0xFF5B8DEF), Color(0xFFF4F6FA)),
    goldOn = Color(0xFFFFFFFF),
    metallic = listOf(Color(0xFF5B8DEF), Color(0xFF8FB0F2), Color(0xFFAFC3E8)),
    accent = Color(0xFF2E6BE6),
    glass = Color(0x09000000),
    glassStrong = Color(0x12000000),
    hairline = Color(0x1A000000),
    ambient = listOf(Color(0xFF7FA7E8), Color(0xFFB9C6D9), Color(0xFF9DB8E8)),
    brand = listOf(Color(0xFF5B8DEF), Color(0xFF2E6BE6), Color(0xFF1E4FB8)),
    glow = Color(0xFF6FA3F2),
)

// ---------------------------------------- تم ۴: آیووری (عاج + مریم‌گلی + شامپاینی)
private val IvoryScheme = lightColorScheme(
    primary = Color(0xFF6E7F60),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE4EADC),
    onPrimaryContainer = Color(0xFF222E1B),
    secondary = Color(0xFFB08D4F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF5E9D2),
    onSecondaryContainer = Color(0xFF3B2E12),
    tertiary = Color(0xFFA85C38),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC7),
    onTertiaryContainer = Color(0xFF4E2410),
    background = Color(0xFFF7F4EC),
    onBackground = Color(0xFF1E1D16),
    surface = Color(0xFFFDFBF6),
    surfaceDim = Color(0xFFE2DFD4),
    surfaceBright = Color(0xFFFDFBF4),
    surfaceContainer = Color(0xFFF0EDE2),
    surfaceContainerHigh = Color(0xFFEAE6D9),
    surfaceContainerHighest = Color(0xFFE3DECE),
    onSurface = Color(0xFF1E1D16),
    surfaceVariant = Color(0xFFEAE6D9),
    onSurfaceVariant = Color(0xFF5C5A4C),
    outline = Color(0xFF8D8A78),
    outlineVariant = Color(0xFFD8D3C2),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val IvoryExtras = ThemeExtras(
    chart = listOf(Color(0xFF7A8B6F), Color(0xFFB08D4F), Color(0xFFA85C38), Color(0xFF5F7A8A), Color(0xFFC98A9A)),
    gold = Color(0xFFB08D4F),
    positive = Color(0xFF5E8A50),
    goldGradient = listOf(Color(0xFFC9AE72), Color(0xFFB08D4F), Color(0xFF8A6C32)),
    loginGradient = listOf(Color(0xFF7A8B6F), Color(0xFF55624A), Color(0xFF39442F)),
    swatch = listOf(Color(0xFFA3B894), Color(0xFFF7F4EC)),
    goldOn = Color(0xFFFFFFFF),
    metallic = listOf(Color(0xFFA3B894), Color(0xFFD6BE8A), Color(0xFFC9AE72)),
    accent = Color(0xFFB08D4F),
    glass = Color(0x09000000),
    glassStrong = Color(0x12000000),
    hairline = Color(0x1A000000),
    ambient = listOf(Color(0xFFA3B894), Color(0xFFD6BE8A), Color(0xFFC9AE72)),
    brand = listOf(Color(0xFF8FA36F), Color(0xFF6E7F60), Color(0xFF55624A)),
    glow = Color(0xFF9BB08A),
)

private fun schemeFor(id: AppThemeId) = when (id) {
    AppThemeId.Obsidian -> ObsidianScheme
    AppThemeId.MilanoRoyale -> MilanoRoyaleScheme
    AppThemeId.Pearl -> PearlScheme
    AppThemeId.Ivory -> IvoryScheme
}

private fun extrasFor(id: AppThemeId) = when (id) {
    AppThemeId.Obsidian -> ObsidianExtras
    AppThemeId.MilanoRoyale -> MilanoExtras
    AppThemeId.Pearl -> PearlExtras
    AppThemeId.Ivory -> IvoryExtras
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
