package ir.atiran.hamrah.viewer.ui.screens.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.data.CustGroup
import ir.atiran.hamrah.viewer.data.DTOCompany
import ir.atiran.hamrah.viewer.data.KaGroup
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.DialogContent
import ir.atiran.hamrah.viewer.ui.components.EmptyBox
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import ir.atiran.hamrah.viewer.ui.components.InfoRow
import ir.atiran.hamrah.viewer.ui.components.LoadingBox
import kotlinx.coroutines.launch

@Composable
fun MiscScreen(vm: AppViewModel) {
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var company by remember { mutableStateOf<DTOCompany?>(null) }
    var custGroups by remember { mutableStateOf<List<CustGroup>?>(null) }
    var kaGroups by remember { mutableStateOf<List<KaGroup>?>(null) }
    var counts by remember { mutableStateOf<String?>(null) }
    var periods by remember { mutableStateOf<List<String>?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("گزارش‌ها و اطلاعات", style = MaterialTheme.typography.titleMedium)
        ErrorBanner(error)
        if (busy) LoadingBox()

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("اطلاعات شرکت", style = MaterialTheme.typography.titleSmall)
                Button(
                    onClick = {
                        busy = true; error = null
                        scope.launch {
                            try { company = vm.repository()?.companyInfo() } catch (e: Exception) { error = e.message }
                            finally { busy = false }
                        }
                    },
                    enabled = !busy,
                ) { Text("دریافت اطلاعات شرکت") }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("شمارنده‌ها", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            busy = true; error = null; counts = null
                            scope.launch {
                                try {
                                    val r = vm.repository()
                                    counts = buildString {
                                        appendLine("تعداد کالا (CountKa): ${r?.countKa() ?: "—"}")
                                        appendLine("تعداد مشتری (CountMo): ${r?.countMo() ?: "—"}")
                                        appendLine("آخرین ShMo (MaxShMo): ${r?.maxShMo() ?: "—"}")
                                    }
                                } catch (e: Exception) { error = e.message }
                                finally { busy = false }
                            }
                        },
                        enabled = !busy,
                        modifier = Modifier.weight(1f),
                    ) { Text("دریافت شمارنده‌ها") }
                    OutlinedButton(
                        onClick = {
                            busy = true; error = null; periods = null
                            scope.launch {
                                try { periods = vm.repository()?.periods() } catch (e: Exception) { error = e.message }
                                finally { busy = false }
                            }
                        },
                        enabled = !busy,
                        modifier = Modifier.weight(1f),
                    ) { Text("دوره‌ها") }
                }
                counts?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                periods?.let {
                    Text("دوره‌ها: " + it.joinToString("، "), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("گروه‌ها", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            busy = true; error = null; custGroups = null
                            scope.launch {
                                try { custGroups = vm.repository()?.custGroups() } catch (e: Exception) { error = e.message }
                                finally { busy = false }
                            }
                        },
                        enabled = !busy,
                        modifier = Modifier.weight(1f),
                    ) { Text("گروه‌های مشتری") }
                    OutlinedButton(
                        onClick = {
                            busy = true; error = null; kaGroups = null
                            scope.launch {
                                try { kaGroups = vm.repository()?.kaGroups() } catch (e: Exception) { error = e.message }
                                finally { busy = false }
                            }
                        },
                        enabled = !busy,
                        modifier = Modifier.weight(1f),
                    ) { Text("گروه‌های کالا") }
                }
            }
        }
    }

    company?.let { c ->
        DialogContent("اطلاعات شرکت", onDismiss = { company = null }) {
            InfoRow("نام", c.Name)
            InfoRow("آدرس", c.Address)
            InfoRow("تلفن ۱", c.Tel1)
            InfoRow("تلفن ۲", c.Tel2)
            InfoRow("فکس", c.Fax)
            InfoRow("ثبت", c.CEgh)
            InfoRow("کد ملی", c.CMeli)
            InfoRow("کد پستی", c.CPos)
            InfoRow("نام ویزیتور", c.VisName)
            InfoRow("موبایل ویزیتور", c.VisCell)
            InfoRow("خطوط", c.Lines?.joinToString("، ") { "${it.LineID ?: ""}: ${it.Name ?: ""}" })
            InfoRow("انبارها", c.Anbars?.joinToString("، ") { it.Name ?: "" })
            InfoRow("بانک‌ها", c.Banks?.joinToString("، ") { it.Name ?: "" })
        }
    }

    custGroups?.let { groups ->
        DialogContent("گروه‌های مشتری (${groups.size})", onDismiss = { custGroups = null }) {
            if (groups.isEmpty()) { EmptyBox() } else {
                groups.forEach { g ->
                    InfoRow("${g.GroupRdf ?: "—"}", g.GroupName, monospace = false)
                }
            }
        }
    }

    kaGroups?.let { groups ->
        DialogContent("گروه‌های کالا (${groups.size})", onDismiss = { kaGroups = null }) {
            if (groups.isEmpty()) { EmptyBox() } else {
                groups.forEach { g ->
                    InfoRow("${g.GroupRdf ?: "—"} (والد: ${g.ParentGroupRdf ?: "—"})", g.GroupName)
                }
            }
        }
    }
}
