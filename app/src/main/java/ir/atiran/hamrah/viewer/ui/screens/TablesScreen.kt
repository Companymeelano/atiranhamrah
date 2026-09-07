package ir.atiran.hamrah.viewer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.data.Overview
import ir.atiran.hamrah.viewer.data.SqlServerDb
import ir.atiran.hamrah.viewer.data.TableInfo
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.EmptyBox
import ir.atiran.hamrah.viewer.ui.components.LuxPanel
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import ir.atiran.hamrah.viewer.ui.components.LoadingBox
import ir.atiran.hamrah.viewer.ui.components.SearchField
import ir.atiran.hamrah.viewer.ui.components.fmt
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesScreen(vm: AppViewModel) {
    val settings by vm.settings.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var overview by remember { mutableStateOf<Overview?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var reloadTick by remember { mutableStateOf(0) }

    LaunchedEffect(reloadTick) {
        loading = true
        error = null
        try {
            val db = vm.db ?: throw IllegalStateException("اتصال برقرار نیست — دوباره وارد شوید")
            overview = db.overview()
        } catch (e: Exception) {
            error = SqlServerDb.friendly(e)
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("آتیران همراه — مدیریت دیتابیس") },
            actions = {
                IconButton(onClick = { vm.openDemo() }) {
                    Icon(Icons.Filled.Insights, contentDescription = "نمایش دمو")
                }
                IconButton(onClick = { reloadTick += 1 }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "بازخوانی")
                }
                IconButton(onClick = { vm.goLogin() }) {
                    Icon(Icons.Filled.Settings, contentDescription = "تغییر اتصال")
                }
                IconButton(onClick = { vm.logout() }) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "خروج")
                }
            },
        )

        // هدر وضعیت اتصال — پنل یکدست هم‌رنگ تم
        overview?.let { o ->
            LuxPanel(container = scheme.primaryContainer) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(extras.positive, RoundedCornerShape(50))
                    )
                    Box(Modifier.size(8.dp))
                    Text(
                        "متصل به ${settings.host} • ${settings.database}",
                        color = scheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    "کاربر: ${settings.user}" +
                        (o.version.takeIf { it.isNotBlank() }?.let { "  •  $it" } ?: ""),
                    color = scheme.onPrimaryContainer.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    StatBlock("جداول", fmt(o.tables.size.toLong()), scheme.onPrimaryContainer)
                    StatBlock("مجموع رکوردها", fmt(o.totalRows), scheme.onPrimaryContainer)
                    StatBlock(
                        "حجم دیتابیس",
                        o.sizeMb?.let { String.format("%.1f MB", it) } ?: "—",
                        scheme.onPrimaryContainer,
                    )
                }
            }
        }

        SearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = "جستجوی نام جدول...",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )

        ErrorBanner(error)
        if (error != null && overview == null) {
            Button(
                onClick = { reloadTick += 1 },
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .align(Alignment.CenterHorizontally),
            ) { Text("تلاش دوباره") }
        }

        when {
            loading && overview == null -> LoadingBox()
            overview == null -> EmptyBox("اطلاعاتی دریافت نشد")
            else -> {
                val tables = overview!!.tables
                    .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
                if (tables.isEmpty()) {
                    EmptyBox("جدولی با این نام یافت نشد")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(tables, key = { it.schema + "." + it.name }) { t ->
                            TableRowItem(t) { vm.openTable(t) }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String, valueColor: Color) {
    Column {
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label, color = valueColor.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TableRowItem(t: TableInfo, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(t.name, fontWeight = FontWeight.Medium) },
        supportingContent = {
            Text(
                (if (t.schema != "dbo") "اسکیما: ${t.schema}  •  " else "") + "تعداد رکورد: ${fmt(t.rows)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = {
            Icon(Icons.Filled.TableChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}
