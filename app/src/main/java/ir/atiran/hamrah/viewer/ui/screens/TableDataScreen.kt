package ir.atiran.hamrah.viewer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.data.PageData
import ir.atiran.hamrah.viewer.data.SqlServerDb
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.DialogContent
import ir.atiran.hamrah.viewer.ui.components.EmptyBox
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import ir.atiran.hamrah.viewer.ui.components.InfoRow
import ir.atiran.hamrah.viewer.ui.components.LoadingBox
import ir.atiran.hamrah.viewer.ui.components.SearchField
import ir.atiran.hamrah.viewer.ui.components.fmt

private const val PAGE_SIZE = 50

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableDataScreen(vm: AppViewModel, schema: String, table: String) {
    BackHandler { vm.backToTables() }

    var pageIdx by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var activeQuery by remember { mutableStateOf("") }
    var data by remember { mutableStateOf<PageData?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedRow by remember { mutableStateOf<List<String?>?>(null) }
    var reloadTick by remember { mutableIntStateOf(0) }

    // جستجو با تاخیر کوتاه (debounce) — صفحه به ابتدا برمی‌گردد
    LaunchedEffect(query) {
        kotlinx.coroutines.delay(400)
        if (query != activeQuery) {
            activeQuery = query
            pageIdx = 0
        }
    }

    LaunchedEffect(activeQuery, pageIdx, reloadTick) {
        loading = true
        error = null
        try {
            val db = vm.db ?: throw IllegalStateException("اتصال برقرار نیست — دوباره وارد شوید")
            data = db.page(
                schema = schema,
                table = table,
                offset = (pageIdx * PAGE_SIZE).toLong(),
                limit = PAGE_SIZE.toLong(),
                searchQuery = activeQuery.trim(),
            )
        } catch (e: Exception) {
            error = SqlServerDb.friendly(e)
        } finally {
            loading = false
        }
    }

    val total = data?.total ?: 0L
    val totalPages = ((total + PAGE_SIZE - 1) / PAGE_SIZE).coerceAtLeast(1)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(table, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
                IconButton(onClick = { vm.backToTables() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                }
            },
        )

        Text(
            "اسکیما: $schema  •  رکوردها: ${fmt(total)}" +
                (data?.columns?.size?.let { "  •  ستون‌ها: $it" } ?: ""),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        )

        SearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = "جستجو در ستون‌های متنی این جدول...",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )

        ErrorBanner(error)

        when {
            loading && data == null -> LoadingBox()
            data == null -> {}
            else -> {
                if (error != null) {
                    Button(
                        onClick = { reloadTick += 1 },
                        modifier = Modifier.padding(bottom = 4.dp),
                    ) { Text("تلاش دوباره") }
                }
                val d = data!!
                if (d.rows.isEmpty()) {
                    EmptyBox(if (activeQuery.isBlank()) "این جدول خالی است" else "نتیجه‌ای برای جستجو یافت نشد")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    ) {
                        itemsIndexed(d.rows) { _, row ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { selectedRow = row },
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    val preview = d.columns.take(4)
                                    preview.forEachIndexed { i, col ->
                                        Text(
                                            "$col: ${row[i] ?: "—"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = if (i == 0) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (i == 0) FontWeight.Medium else FontWeight.Normal,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // نوار صفحه‌بندی
        if (data != null && total > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Arrangement.Center),
            ) {
                OutlinedButton(
                    onClick = { if (pageIdx > 0) pageIdx -= 1 },
                    enabled = pageIdx > 0 && !loading,
                ) { Text("قبلی") }
                Text(
                    "صفحه ${pageIdx + 1} از $totalPages",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp),
                )
                OutlinedButton(
                    onClick = { if (pageIdx < totalPages - 1) pageIdx += 1 },
                    enabled = pageIdx < totalPages - 1 && !loading,
                ) { Text("بعدی") }
            }
        }
    }

    // جزئیات رکورد
    val d = data
    val row = selectedRow
    if (d != null && row != null) {
        DialogContent(
            title = "جزئیات رکورد — $table",
            onDismiss = { selectedRow = null },
        ) {
            d.columns.forEachIndexed { i, col ->
                InfoRow(col, row.getOrNull(i)?.take(2000))
            }
        }
    }
}
