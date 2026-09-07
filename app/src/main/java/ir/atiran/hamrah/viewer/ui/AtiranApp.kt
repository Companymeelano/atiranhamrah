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
import ir.atiran.hamrah.viewer.R
import ir.atiran.hamrah.viewer.ui.screens.LoginScreen
import ir.atiran.hamrah.viewer.ui.screens.TableDataScreen
import ir.atiran.hamrah.viewer.ui.screens.TablesScreen

val BrandGradient = Brush.verticalGradient(
    listOf(Color(0xFF00382A), Color(0xFF00674B), Color(0xFF0E8A64))
)

@Composable
fun AtiranApp(vm: AppViewModel) {
    when (val s = vm.screen) {
        is Screen.Boot -> BootScreen()
        is Screen.Login -> LoginScreen(vm)
        is Screen.Tables -> TablesScreen(vm)
        is Screen.TableData -> TableDataScreen(vm = vm, schema = s.schema, table = s.table)
    }
}

@Composable
private fun BootScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(BrandGradient),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(96.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("در حال اتصال به سرور...", color = Color.White)
        }
    }
}
