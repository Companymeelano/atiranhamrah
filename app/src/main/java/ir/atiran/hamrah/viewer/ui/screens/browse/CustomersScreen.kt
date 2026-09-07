package ir.atiran.hamrah.viewer.ui.screens.browse

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ir.atiran.hamrah.viewer.data.Customer
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.DialogContent
import ir.atiran.hamrah.viewer.ui.components.InfoRow
import ir.atiran.hamrah.viewer.ui.components.fmt

@Composable
fun CustomersScreen(vm: AppViewModel) {
    var selected by remember { mutableStateOf<Customer?>(null) }

    BrowseListScreen<Customer>(
        title = "مشتریان",
        vm = vm,
        load = { repo, start, fetch -> repo.customers(start, fetch) },
        localFilter = { c, q ->
            q.isBlank() ||
                c.Moname.orEmpty().contains(q, true) ||
                c.Tel1.orEmpty().contains(q) ||
                c.Cell.orEmpty().contains(q) ||
                c.Shmo.toString() == q
        },
        itemKey = { it.Shmo?.toLong() },
        onItemTap = { selected = it },
    ) { c ->
        Text(c.Moname ?: "—", style = MaterialTheme.typography.titleSmall)
        Text(
            "ShMo: ${c.Shmo ?: "—"}  •  تلفن: ${c.Tel1 ?: c.Cell ?: "—"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (c.Credit != null || c.Man != null) {
            Text(
                "اعتبار: ${fmt(c.Credit)}  •  بدهی: ${fmt(c.Man)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    selected?.let { c ->
        DialogContent("مشتری: ${c.Moname ?: "—"}", onDismiss = { selected = null }) {
            InfoRow("شماره (ShMo)", c.Shmo?.toString(), monospace = true)
            InfoRow("نام مشتری", c.Moname)
            InfoRow("شماره شناسنامه", c.Code)
            InfoRow("تلفن ۱", c.Tel1)
            InfoRow("تلفن ۲", c.Tel2)
            InfoRow("موبایل", c.Cell)
            InfoRow("ایمیل", c.Email)
            InfoRow("آدرس", c.Address)
            InfoRow("شغل", c.Job)
            InfoRow("توضیحات", c.Sharh)
            InfoRow("اعتبار", fmt(c.Credit))
            InfoRow("مانده / بدهی", fmt(c.Man))
            InfoRow("ویزیتور (VisRdf)", c.VisRdf?.toString())
            InfoRow("مسیر (Masir)", c.Masir?.toString())
            InfoRow("گروه مشتری", c.GroupRdf?.toString())
            InfoRow("وضعیت", when (c.Active) { true -> "فعال"; false -> "غیرفعال"; null -> "—" })
            InfoRow("لیست سیاه", when (c.BlackList) { true -> "بله"; false -> "خیر"; null -> "—" })
            InfoRow("ویژه", when (c.Special) { true -> "بله"; false -> "خیر"; null -> "—" })
            InfoRow("طول جغرافیایی", c.Longitude?.toString())
            InfoRow("عرض جغرافیایی", c.Latitude?.toString())
            InfoRow("مناطق دسترسی", c.AccessLine?.joinToString { it.toString() })
        }
    }
}
