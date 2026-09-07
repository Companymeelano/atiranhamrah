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
import ir.atiran.hamrah.viewer.data.Check
import ir.atiran.hamrah.viewer.data.AtiranSettings
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.DialogContent
import ir.atiran.hamrah.viewer.ui.components.InfoRow
import ir.atiran.hamrah.viewer.ui.components.fmt

@Composable
fun ChecksScreen(vm: AppViewModel, settings: AtiranSettings) {
    var mode by remember { mutableIntStateOf(0) } // 0 = all, 1 = customer
    var selected by remember { mutableStateOf<Check?>(null) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = { mode = 0 }, modifier = Modifier.weight(1f)) {
                Text("همه چک‌ها")
            }
            OutlinedButton(onClick = { mode = 1 }, modifier = Modifier.weight(1f)) {
                Text("چک‌های مشتری")
            }
        }

        if (mode == 0) {
            BrowseListScreen<Check>(
                title = "چک‌ها",
                vm = vm,
                load = { repo, start, fetch -> repo.checks(start, fetch) },
                modifier = Modifier.weight(1f),
                localFilter = { c, q ->
                    q.isBlank() || c.Bank.orEmpty().contains(q, true) || c.Serial.orEmpty().contains(q) || c.shmo?.toString() == q
                },
                itemKey = { it.Id },
                onItemTap = { selected = it },
            ) { c ->
                Text("چک: ${c.Serial ?: "—"}  •  بانک: ${c.Bank ?: "—"}", style = MaterialTheme.typography.titleSmall)
                Text(
                    "مبلغ: ${fmt(c.Mablagh)}  •  تاریخ: ${c.Date ?: c.SarDate ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "ShMo: ${c.shmo ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Text(
                "ShMo: ${settings.shMo.ifBlank { "— (در تنظیمات وارد کنید)" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
            )
            BrowseListScreen<Check>(
                title = "چک‌های مشتری",
                vm = vm,
                load = { repo, _, _ ->
                    val sh = vm.settings.value.shMo
                    if (sh.isBlank()) emptyList() else repo.customerChecks(sh)
                },
                modifier = Modifier.weight(1f),
                paged = false, // یک‌جا برمی‌گردد
                localFilter = { c, q ->
                    q.isBlank() || c.Bank.orEmpty().contains(q, true) || c.Serial.orEmpty().contains(q)
                },
                itemKey = { it.Id },
                onItemTap = { selected = it },
            ) { c ->
                Text("چک: ${c.Serial ?: "—"}  •  بانک: ${c.Bank ?: "—"}", style = MaterialTheme.typography.titleSmall)
                Text(
                    "مبلغ: ${fmt(c.Mablagh)}  •  تاریخ: ${c.Date ?: c.SarDate ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "وضعیت: ${c.Status ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    selected?.let { c ->
        DialogContent("چک: ${c.Serial ?: "—"}", onDismiss = { selected = null }) {
            InfoRow("شناسه (Id)", c.Id?.toString(), monospace = true)
            InfoRow("تاریخ سررسید", c.SarDate)
            InfoRow("تاریخ دریافت", c.Date)
            InfoRow("صادرکننده", c.getchkshhes)
            InfoRow("بانک", c.Bank)
            InfoRow("شعبه", c.Shobe)
            InfoRow("سریال", c.Serial)
            InfoRow("مبلغ", fmt(c.Mablagh))
            InfoRow("ShMo", c.shmo?.toString())
            InfoRow("توضیحات", c.CheckDescription)
            InfoRow("وضعیت", c.Status?.toString())
        }
    }
}
