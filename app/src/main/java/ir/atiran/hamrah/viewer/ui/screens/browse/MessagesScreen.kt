package ir.atiran.hamrah.viewer.ui.screens.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.data.Message
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.DialogContent
import ir.atiran.hamrah.viewer.ui.components.InfoRow

@Composable
fun MessagesScreen(vm: AppViewModel) {
    var mode by remember { mutableIntStateOf(0) } // 0 = visitor messages, 1 = unread
    var selected by remember { mutableStateOf<Message?>(null) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = { mode = 0 }, modifier = Modifier.weight(1f)) {
                Text("پیام‌های ویزیتور")
            }
            OutlinedButton(onClick = { mode = 1 }, modifier = Modifier.weight(1f)) {
                Text("پیام‌های خوانده‌نشده")
            }
        }

        if (mode == 0) {
            BrowseListScreen<Message>(
                title = "پیام‌ها",
                vm = vm,
                load = { repo, start, fetch -> repo.visitorMessages(start, fetch) },
                modifier = Modifier.weight(1f),
                localFilter = { m, q ->
                    q.isBlank() || m.Title.orEmpty().contains(q, true) || m.Description.orEmpty().contains(q, true)
                },
                itemKey = { it.Id },
                onItemTap = { selected = it },
            ) { m ->
                Text(
                    (if (m.isImportant == 1 || m.isImportant == 2) "⭐ " else "") + (m.Title ?: "—"),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (m.isImportant == 1 || m.isImportant == 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "تاریخ: ${m.CreateDate ?: m.CreateTimeDate ?: "—"}  •  زمان: ${m.CreateTime ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    (m.Description ?: "").take(80) + if ((m.Description?.length ?: 0) > 80) "…" else "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                )
            }
        } else {
            Text(
                "VisitorID: ${vm.settings.value.visitorId.ifBlank { "— (در تنظیمات وارد کنید)" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
            )
            BrowseListScreen<Message>(
                title = "پیام‌های خوانده‌نشده",
                vm = vm,
                load = { repo, _, _ ->
                    val vid = vm.settings.value.visitorId
                    if (vid.isBlank()) emptyList() else repo.unreadMessages(vid)
                },
                modifier = Modifier.weight(1f),
                paged = false, // یک‌جا برمی‌گردد
                localFilter = { m, q -> q.isBlank() || m.Title.orEmpty().contains(q, true) },
                itemKey = { it.Id },
                onItemTap = { selected = it },
            ) { m ->
                Text(m.Title ?: "—", style = MaterialTheme.typography.titleSmall)
                Text(
                    "تاریخ: ${m.CreateDate ?: m.CreateTimeDate ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    selected?.let { m ->
        DialogContent(m.Title ?: "پیام", onDismiss = { selected = null }) {
            InfoRow("شناسه (Id)", m.Id?.toString(), monospace = true)
            InfoRow("تاریخ ایجاد", m.CreateDate ?: m.CreateTimeDate)
            InfoRow("زمان ایجاد", m.CreateTime)
            InfoRow("تاریخ خواندن", m.ReadDate ?: m.ReadTimeDate)
            InfoRow("زمان خواندن", m.ReadTime)
            InfoRow("مهم", m.isImportant?.let { if (it == 1) "بله" else "خیر" })
            InfoRow("ویزیتور (VisitorId)", m.VisitorId?.toString())
            InfoRow("وضعیت", m.Status?.toString())
            Text(
                m.Description ?: "—",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
