package ir.atiran.hamrah.viewer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
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
    val theme = AppThemeId.entries.getOrNull(vm.themeId) ?: AppThemeId.OnyxGold

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
    val scheme = androidx.compose.material3.MaterialTheme.colorScheme
    val extras = ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(extras.loginGradient)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = null,
                modifier = Modifier.size(88.dp),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                "M•REPORT",
                color = scheme.onPrimary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                letterSpacing = 3.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Intelligent Reporting Experience",
                color = scheme.onPrimary.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 3.sp,
            )
            Spacer(modifier = Modifier.height(18.dp))
            CircularProgressIndicator(color = scheme.onPrimary, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("در حال اتصال به سرور...", color = scheme.onPrimary.copy(alpha = 0.8f))
        }
    }
}
