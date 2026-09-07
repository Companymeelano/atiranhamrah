package ir.atiran.hamrah.viewer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.data.AtiranSettings
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.Section

private val Section.icon: ImageVector
    get() = when (this) {
        Section.Customers -> Icons.Filled.Group
        Section.Goods -> Icons.Filled.Category
        Section.Inventory -> Icons.Filled.Inventory2
        Section.Invoices -> Icons.Filled.Receipt
        Section.Checks -> Icons.Filled.Payments
        Section.Messages -> Icons.Filled.Email
        Section.Misc -> Icons.Filled.Description
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: AppViewModel, settings: AtiranSettings) {
    val current by vm.settings.collectAsState()
    val sections = Section.entries

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("آتیران همراه — مشاهده داده‌ها") },
            actions = {
                IconButton(onClick = { vm.goSettings() }) {
                    Icon(Icons.Filled.Settings, contentDescription = "تنظیمات")
                }
                IconButton(onClick = { vm.logout() }) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "خروج")
                }
            },
        )

        Text(
            "سرور: ${current.normalizedServer()}  •  CPUID: ${current.cpuId}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        if (current.username.isNotBlank()) {
            Text(
                "کاربر: ${current.username}" +
                    (current.shMo.takeIf { it.isNotBlank() }?.let { "  •  ShMo: $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))

        // هر بخش در صفحه کامل خودش باز می‌شود؛ این لیست تخت است و
        // LazyColumn تو در تو داخل اسکرول بی‌نهایت ندارد (رفع کرش).
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(sections) { section ->
                ListItem(
                    headlineContent = { Text(section.title) },
                    leadingContent = {
                        Icon(section.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { vm.openSection(section) },
                )
                HorizontalDivider()
            }
        }
    }
}
