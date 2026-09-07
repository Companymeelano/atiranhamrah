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
import ir.atiran.hamrah.viewer.data.SailFactor
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.DialogContent
import ir.atiran.hamrah.viewer.ui.components.InfoRow
import ir.atiran.hamrah.viewer.ui.components.fmt
import ir.atiran.hamrah.viewer.data.AtiranSettings

@Composable
fun InvoicesScreen(vm: AppViewModel, settings: AtiranSettings) {
    var mode by remember { mutableIntStateOf(0) } // 0 = factors, 1 = not-paid
    var selected by remember { mutableStateOf<SailFactor?>(null) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = { mode = 0 }, modifier = Modifier.weight(1f)) {
                Text("فاکتورهای مشتری")
            }
            OutlinedButton(onClick = { mode = 1 }, modifier = Modifier.weight(1f)) {
                Text("فاکتورهای پرداخت‌نشده")
            }
        }

        Text(
            "ShMo مورد استفاده: ${settings.shMo.ifBlank { "— (در تنظیمات وارد کنید)" }}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        )

        if (mode == 0) {
            BrowseListScreen<SailFactor>(
                title = "فاکتورها",
                vm = vm,
                load = { repo, _, _ ->
                    val sh = vm.settings.value.shMo
                    if (sh.isBlank()) emptyList() else repo.customerFactors(sh)
                },
                modifier = Modifier.weight(1f),
                paged = false, // این سرویس همه فاکتورها را یک‌جا برمی‌گرداند
                localFilter = { f, q ->
                    q.isBlank() || f.ShFact?.toString() == q || f.Date.orEmpty().contains(q)
                },
                itemKey = { it.Id?.toLong() },
                onItemTap = { selected = it },
            ) { f ->
                Text("فاکتور: ${f.ShFact ?: "—"}", style = MaterialTheme.typography.titleSmall)
                Text(
                    "تاریخ: ${f.Date ?: "—"}  •  ShMo: ${f.ShMo ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "مبلغ کل: ${fmt(f.AllFel)}  •  دریافتی: ${fmt(f.MabDaryaftFactor)}  •  تخفیف: ${fmt(f.Tdf)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            BrowseListScreen<SailFactor>(
                title = "فاکتورهای پرداخت‌نشده",
                vm = vm,
                load = { repo, _, _ ->
                    val sh = vm.settings.value.shMo
                    if (sh.isBlank()) emptyList() else repo.notPaidFactors(sh)
                },
                modifier = Modifier.weight(1f),
                paged = false,
                localFilter = { f, q ->
                    q.isBlank() || f.ShFact?.toString() == q || f.TDate.orEmpty().contains(q)
                },
                itemKey = { it.Id?.toLong() },
                onItemTap = { selected = it },
            ) { f ->
                Text("فاکتور: ${f.ShFact ?: "—"}", style = MaterialTheme.typography.titleSmall)
                Text(
                    "تاریخ: ${f.TDate ?: f.Date ?: "—"}  •  ShMo: ${f.ShMo ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "مبلغ: ${fmt(f.AllFel)}  •  دریافتی: ${fmt(f.MabDaryaftFactor)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    selected?.let { f ->
        DialogContent("فاکتور: ${f.ShFact ?: "—"}", onDismiss = { selected = null }) {
            InfoRow("شماره فاکتور", f.ShFact?.toString(), monospace = true)
            InfoRow("شناسه (Id)", f.Id?.toString(), monospace = true)
            InfoRow("تاریخ", f.Date)
            InfoRow("تاریخ پرداخت (TDate)", f.TDate)
            InfoRow("ShMo", f.ShMo?.toString())
            InfoRow("مبلغ کل", fmt(f.AllFel))
            InfoRow("مبلغ دریافتی", fmt(f.MabDaryaftFactor))
            InfoRow("تخفیف", fmt(f.Tdf))
        }
    }
}
