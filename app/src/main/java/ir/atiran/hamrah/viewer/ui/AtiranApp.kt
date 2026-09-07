package ir.atiran.hamrah.viewer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ir.atiran.hamrah.viewer.data.AtiranSettings
import ir.atiran.hamrah.viewer.ui.screens.HomeScreen
import ir.atiran.hamrah.viewer.ui.screens.LoginScreen
import ir.atiran.hamrah.viewer.ui.screens.SettingsScreen
import ir.atiran.hamrah.viewer.ui.screens.browse.ChecksScreen
import ir.atiran.hamrah.viewer.ui.screens.browse.CustomersScreen
import ir.atiran.hamrah.viewer.ui.screens.browse.GoodsScreen
import ir.atiran.hamrah.viewer.ui.screens.browse.InventoryScreen
import ir.atiran.hamrah.viewer.ui.screens.browse.InvoicesScreen
import ir.atiran.hamrah.viewer.ui.screens.browse.MessagesScreen
import ir.atiran.hamrah.viewer.ui.screens.browse.MiscScreen

@Composable
fun AtiranApp(vm: AppViewModel) {
    val settings by vm.settings.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        when (val s = vm.screen) {
            is Screen.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            is Screen.Settings -> {
                // اگر تنظیمات بعد از پیکربندی باز شده باشد، برگشت به صفحه قبل
                BackHandler(enabled = settings.configured) { vm.backFromSettings() }
                SettingsScreen(vm = vm, initial = settings)
            }

            is Screen.Login -> LoginScreen(vm = vm, settings = settings)

            is Screen.Home -> HomeScreen(vm = vm, settings = settings)

            is Screen.Browse -> BrowseSectionScreen(vm = vm, settings = settings, section = s.section)
        }
    }
}

/**
 * هر بخش داده در صفحه کامل خودش نمایش داده می‌شود (نه داخل اسکرول صفحه خانه) —
 * این مشکل «measured with an infinity maximum height» را رفع می‌کند.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowseSectionScreen(vm: AppViewModel, settings: AtiranSettings, section: Section) {
    BackHandler { vm.backToHome() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(section.title) },
                navigationIcon = {
                    IconButton(onClick = { vm.backToHome() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (section) {
                Section.Customers -> CustomersScreen(vm)
                Section.Goods -> GoodsScreen(vm)
                Section.Inventory -> InventoryScreen(vm)
                Section.Invoices -> InvoicesScreen(vm, settings)
                Section.Checks -> ChecksScreen(vm, settings)
                Section.Messages -> MessagesScreen(vm)
                Section.Misc -> MiscScreen(vm)
            }
        }
    }
}
