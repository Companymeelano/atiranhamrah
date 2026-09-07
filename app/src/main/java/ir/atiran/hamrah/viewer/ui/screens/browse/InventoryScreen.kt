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
import ir.atiran.hamrah.viewer.data.KalaAnb
import ir.atiran.hamrah.viewer.data.VWInventoryAnbars
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.DialogContent
import ir.atiran.hamrah.viewer.ui.components.InfoRow
import ir.atiran.hamrah.viewer.ui.components.fmt

@Composable
fun InventoryScreen(vm: AppViewModel) {
    var mode by remember { mutableIntStateOf(0) } // 0 = kala-anbar, 1 = inventory-anbars
    var selected by remember { mutableStateOf<KalaAnb?>(null) }
    var selectedInv by remember { mutableStateOf<VWInventoryAnbars?>(null) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = { mode = 0 }, modifier = Modifier.weight(1f)) {
                Text("کالا-انبار")
            }
            OutlinedButton(onClick = { mode = 1 }, modifier = Modifier.weight(1f)) {
                Text("موجودی انبارها")
            }
        }

        if (mode == 0) {
            BrowseListScreen<KalaAnb>(
                title = "کالا-انبار",
                vm = vm,
                load = { repo, start, fetch -> repo.kalaAnbs(start, fetch) },
                modifier = Modifier.weight(1f),
                localFilter = { k, q -> q.isBlank() || k.ShKa?.toString() == q || k.AnbName.orEmpty().contains(q, true) },
                itemKey = { it.ShKa?.let { s -> s * 1000 + (it.RdfAnb ?: 0) } },
                onItemTap = { selected = it },
            ) { k ->
                Text(
                    "ShKa: ${k.ShKa ?: "—"}  •  انبار: ${k.AnbName ?: "—"}",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    "موجودی: ${fmt(k.Moj)}  •  واحد: ${k.MohVah ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            BrowseListScreen<VWInventoryAnbars>(
                title = "موجودی انبارها",
                vm = vm,
                load = { repo, start, fetch -> repo.inventoryAnbars(start, fetch) },
                modifier = Modifier.weight(1f),
                localFilter = { k, q ->
                    q.isBlank() || k.shka?.toString() == q || k.AnbName.orEmpty().contains(q, true) || k.name.orEmpty().contains(q, true)
                },
                itemKey = { it.shka?.let { s -> s * 1000 + (it.rdf_anbars ?: 0) } },
                onItemTap = { selectedInv = it },
            ) { k ->
                Text(
                    "ShKa: ${k.shka ?: "—"}  •  انبار: ${k.AnbName ?: k.name ?: "—"}",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    "موجودی کل: ${fmt(k.mojkavah)}  •  واحد: ${k.tedbastebandi ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "موجودی جزء: ${fmt(k.mojkajoz)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    selected?.let { k ->
        DialogContent("کالا-انبار — ShKa: ${k.ShKa ?: "—"}", onDismiss = { selected = null }) {
            InfoRow("شناسه کالا (ShKa)", k.ShKa?.toString(), monospace = true)
            InfoRow("انبار (RdfAnb)", k.RdfAnb?.toString())
            InfoRow("نام انبار", k.AnbName)
            InfoRow("موجودی (Moj)", fmt(k.Moj))
            InfoRow("واحد (MohVah)", k.MohVah?.toString())
        }
    }

    selectedInv?.let { k ->
        DialogContent("موجودی انبار — ShKa: ${k.shka ?: "—"}", onDismiss = { selectedInv = null }) {
            InfoRow("شناسه کالا (shka)", k.shka?.toString(), monospace = true)
            InfoRow("انبار (rdf_anbars)", k.rdf_anbars?.toString())
            InfoRow("نام انبار", k.AnbName ?: k.name)
            InfoRow("تعداد بسته‌بندی", k.tedbastebandi?.toString())
            InfoRow("موجودی کالای واحد", fmt(k.mojkavah))
            InfoRow("موجودی کالای جزء", fmt(k.mojkajoz))
            InfoRow("موجودی پیش واحد", fmt(k.MojodiPish_vah))
            InfoRow("موجودی پیش جزء", fmt(k.MojodiPish_joz))
        }
    }
}
