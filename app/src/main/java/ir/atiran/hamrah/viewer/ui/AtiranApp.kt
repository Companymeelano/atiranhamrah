package ir.atiran.hamrah.viewer.ui
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.atiran.hamrah.viewer.R
import ir.atiran.hamrah.viewer.ui.screens.DemoScreen
import ir.atiran.hamrah.viewer.ui.screens.LoginScreen
import ir.atiran.hamrah.viewer.ui.screens.TableDataScreen
import ir.atiran.hamrah.viewer.ui.screens.TablesScreen
import ir.atiran.hamrah.viewer.ui.theme.AtiranAppTheme
import ir.atiran.hamrah.viewer.ui.theme.AppThemeId

@Composable
fun AtiranApp(vm: AppViewModel) {
    val theme = AppThemeId.entries.getOrNull(vm.themeId) ?: AppThemeId.Obsidian

    AtiranAppTheme(themeId = theme) {
        when (val s = vm.screen) {
            is Screen.Boot -> BootScreen()
            is Screen.Login -> LoginScreen(vm)
            is Screen.Tables -> TablesScreen(vm)
            is Screen.TableData -> TableDataScreen(vm = vm, schema = s.schema, table = s.table)
            is Screen.Demo -> DemoScreen(vm)
        }
    }
}

@Composable
private fun BootScreen() {
    val scheme = MaterialTheme.colorScheme
    val extras = ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras.current
    val tr = rememberInfiniteTransition(label = "boot")
    val sweep by tr.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1600),
            RepeatMode.Reverse,
        ),
        label = "sweep",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(extras.loginGradient)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // لوگوی M•
            Text(
                "M•",
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.horizontalGradient(extras.brand),
                    fontSize = 76.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                ),
            )
            Spacer(modifier = Modifier.height(18.dp))
            // خط نور که آرام حرکت می‌کند
            Box(
                modifier = Modifier
                    .width(190.dp)
                    .height(2.dp)
                    .background(extras.hairline),
            ) {
                Box(
                    modifier = Modifier
                        .absoluteOffset(x = (sweep * 65).dp)
                        .width(60.dp)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    androidx.compose.ui.graphics.Color.Transparent,
                                    extras.brand[1],
                                    extras.brand.last(),
                                    androidx.compose.ui.graphics.Color.Transparent,
                                )
                            )
                        ),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Preparing your reports",
                color = scheme.onPrimary.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 2.sp,
            )
        }
    }
}