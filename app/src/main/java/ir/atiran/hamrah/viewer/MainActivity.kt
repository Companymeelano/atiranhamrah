package ir.atiran.hamrah.viewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.AtiranApp
import ir.atiran.hamrah.viewer.ui.theme.AtiranTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AtiranTheme {
                Root()
            }
        }
    }
}

@Composable
private fun Root(vm: AppViewModel = viewModel()) {
    AtiranApp(vm)
}
