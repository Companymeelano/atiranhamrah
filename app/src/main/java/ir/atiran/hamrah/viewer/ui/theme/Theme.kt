package ir.atiran.hamrah.viewer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF00674B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8EF8CD),
    onPrimaryContainer = Color(0xFF002116),
    secondary = Color(0xFF4C6358),
    secondaryContainer = Color(0xFFCFE9DA),
    background = Color(0xFFF7FBF7),
    surface = Color(0xFFF7FBF7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF72DBAF),
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF00513C),
    onPrimaryContainer = Color(0xFF8EF8CD),
    secondary = Color(0xFFB3CCBE),
    secondaryContainer = Color(0xFF344B40),
)

@Composable
fun AtiranTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
