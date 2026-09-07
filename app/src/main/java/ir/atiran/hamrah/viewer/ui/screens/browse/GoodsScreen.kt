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
import ir.atiran.hamrah.viewer.data.ForoshPrice
import ir.atiran.hamrah.viewer.data.Kala
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.DialogContent
import ir.atiran.hamrah.viewer.ui.components.InfoRow
import ir.atiran.hamrah.viewer.ui.components.fmt

@Composable
fun GoodsScreen(vm: AppViewModel) {
    var mode by remember { mutableIntStateOf(0) } // 0 = kalas, 1 = prices
    var selectedKala by remember { mutableStateOf<Kala?>(null) }
    var selectedPrice by remember { mutableStateOf<ForoshPrice?>(null) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = { mode = 0 }, modifier = Modifier.weight(1f)) {
                Text("کالاها")
            }
            OutlinedButton(onClick = { mode = 1 }, modifier = Modifier.weight(1f)) {
                Text("قیمت‌های فروش")
            }
        }

        if (mode == 0) {
            BrowseListScreen<Kala>(
                title = "کالاها",
                vm = vm,
                load = { repo, start, fetch -> repo.kalas(start, fetch) },
                modifier = Modifier.weight(1f),
                localFilter = { k, q ->
                    q.isBlank() ||
                        k.NaKa.orEmpty().contains(q, true) ||
                        k.Coka.orEmpty().contains(q, true) ||
                        k.Shka?.toString() == q
                },
                itemKey = { it.Shka },
                onItemTap = { selectedKala = it },
            ) { k ->
                Text(k.NaKa ?: "—", style = MaterialTheme.typography.titleSmall)
                Text(
                    "ShKa: ${k.Shka ?: "—"}  •  واحد: ${k.VahSanj ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (k.FinalSalePrice != null) {
                    Text(
                        "قیمت فروش: ${fmt(k.FinalSalePrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        } else {
            BrowseListScreen<ForoshPrice>(
                title = "قیمت‌های فروش",
                vm = vm,
                load = { repo, start, fetch -> repo.foroshPrices(start, fetch) },
                modifier = Modifier.weight(1f),
                localFilter = { p, q -> q.isBlank() || p.ShKa?.toString() == q },
                itemKey = { it.ShKa },
                onItemTap = { selectedPrice = it },
            ) { p ->
                Text("ShKa: ${p.ShKa ?: "—"}", style = MaterialTheme.typography.titleSmall)
                Text(
                    "قیمت ۱: ${fmt(p.FP1)}  •  قیمت ۲: ${fmt(p.FP2)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "قیمت ۳: ${fmt(p.FP3)}  •  قیمت ۴: ${fmt(p.FP4)}  •  قیمت ۵: ${fmt(p.FP5)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    selectedKala?.let { k ->
        DialogContent("کالا: ${k.NaKa ?: "—"}", onDismiss = { selectedKala = null }) {
            InfoRow("شناسه (ShKa)", k.Shka?.toString(), monospace = true)
            InfoRow("نام کالا", k.NaKa)
            InfoRow("کد کالا (Coka)", k.Coka)
            InfoRow("واحد سنجش", k.VahSanj)
            InfoRow("موجودی واحد", k.MohVah?.toString())
            InfoRow("گروه", k.GroupRdf?.toString())
            InfoRow("قیمت فروش نهایی", fmt(k.FinalSalePrice))
            InfoRow("حداکثر تخفیف", k.MaxTaf?.toString())
            InfoRow("مالیات", k.PTax?.toString())
            InfoRow("عوارض", k.PAvarez?.toString())
            InfoRow("بسته‌بندی", k.Bastebandi)
            InfoRow("تاریخ انقضا", k.ExpirationDate)
            InfoRow("متن تبلیغاتی", k.PromotionText)
            InfoRow("وضعیت", when (k.Active) { true -> "فعال"; false -> "غیرفعال"; null -> "—" })
            InfoRow("مناطق دسترسی", k.AccessLine?.joinToString { it.toString() })
        }
    }

    selectedPrice?.let { p ->
        DialogContent("قیمت فروش — ShKa: ${p.ShKa ?: "—"}", onDismiss = { selectedPrice = null }) {
            InfoRow("قیمت ۱", "${fmt(p.FP1)} (حداقل ${p.MP1 ?: "—"} / واحد ${p.VP1 ?: "—"})")
            InfoRow("قیمت ۲", "${fmt(p.FP2)} (حداقل ${p.MP2 ?: "—"} / واحد ${p.VP2 ?: "—"})")
            InfoRow("قیمت ۳", "${fmt(p.FP3)} (حداقل ${p.MP3 ?: "—"} / واحد ${p.VP3 ?: "—"})")
            InfoRow("قیمت ۴", "${fmt(p.FP4)} (حداقل ${p.MP4 ?: "—"} / واحد ${p.VP4 ?: "—"})")
            InfoRow("قیمت ۵", "${fmt(p.FP5)} (حداقل ${p.MP5 ?: "—"} / واحد ${p.VP5 ?: "—"})")
        }
    }
}
