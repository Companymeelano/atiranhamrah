package ir.atiran.hamrah.viewer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.data.AtiranClient
import ir.atiran.hamrah.viewer.data.AtiranRepository
import ir.atiran.hamrah.viewer.data.AtiranSettings
import ir.atiran.hamrah.viewer.data.CustomerLogin
import ir.atiran.hamrah.viewer.data.Login
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import ir.atiran.hamrah.viewer.ui.components.LoadingBox
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, initial: AtiranSettings) {
    var s by remember { mutableStateOf(initial) }
    var testing by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var testError by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("تنظیمات اتصال به سرور آتیران") },
            navigationIcon = {
                if (initial.configured) {
                    IconButton(onClick = { vm.backFromSettings() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            },
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "برای استفاده از این برنامه، ابتدا تبلت/دستگاه شما باید در نسخه ویندوزی «آتیران همراه» برای همین CPUID فعال شده باشد.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = s.serverUrl,
                onValueChange = { s = s.copy(serverUrl = it) },
                label = { Text("آدرس سرویس (LocalServices.svc)") },
                placeholder = { Text("http://192.168.1.10/LocalServices.svc") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = s.cpuId,
                onValueChange = { s = s.copy(cpuId = it) },
                label = { Text("CPUID (شناسه دستگاه فعال‌شده)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("اطلاعات حساب (برای ورود و مشاهده داده‌ها)", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = s.username,
                onValueChange = { s = s.copy(username = it) },
                label = { Text("نام کاربری") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = s.password,
                onValueChange = { s = s.copy(password = it) },
                label = { Text("رمز عبور") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPassword) "پنهان کردن رمز" else "نمایش رمز",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Text("شماره مشتری / ویزیتور (اختیاری)", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = s.shMo,
                onValueChange = { s = s.copy(shMo = it) },
                label = { Text("ShMo مشتری (برای فاکتورها و چک‌ها)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = s.visitorId,
                onValueChange = { s = s.copy(visitorId = it) },
                label = { Text("شناسه ویزیتور (VisitorID، اختیاری)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("تنظیمات SetInfo (برای جستجوی فیلترشده — اختیاری)", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = s.ownerNum,
                    onValueChange = { s = s.copy(ownerNum = it) },
                    label = { Text("OwnerNum") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = s.inventoryNum,
                    onValueChange = { s = s.copy(inventoryNum = it) },
                    label = { Text("InventoryNum") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = s.atiranNum,
                    onValueChange = { s = s.copy(atiranNum = it) },
                    label = { Text("AtiranNum") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = s.carrierNum,
                    onValueChange = { s = s.copy(carrierNum = it) },
                    label = { Text("CarrierNum") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
            OutlinedTextField(
                value = s.activeLine,
                onValueChange = { s = s.copy(activeLine = it) },
                label = { Text("ActiveLine") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            ErrorBanner(testError)
            if (testing) LoadingBox()

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { vm.saveSettings(s) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("ذخیره و ادامه")
                }
                OutlinedButton(
                    onClick = {
                        testing = true
                        testError = null
                        testResult = null
                        scope.launch {
                            try {
                                val repo = AtiranRepository(AtiranClient(s))
                                val company = repo.companyInfo()
                                val role = repo.login(Login(s.username, s.password))?.Role
                                val profile = repo.getCustomerByLogin(
                                    CustomerLogin(Username = s.username, Password = s.password)
                                )
                                val who = profile.firstOrNull()?.Moname ?: "مشتری ای یافت نشد"
                                testResult = buildString {
                                    appendLine("اتصال موفق ✓")
                                    appendLine("شرکت: ${company?.Name ?: "—"}")
                                    appendLine("نقش (Role): ${role ?: "—"}")
                                    appendLine("مشتری: $who")
                                    val shmo = profile.firstOrNull()?.Shmo
                                    if (shmo != null) appendLine("ShMo: $shmo")
                                }
                            } catch (e: Exception) {
                                testError = "تست اتصال ناموفق: ${e.message}"
                            } finally {
                                testing = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !testing,
                ) {
                    Text("تست اتصال")
                }
            }
        }
    }

    if (testResult != null) {
        AlertDialog(
            onDismissRequest = { testResult = null },
            title = { Text("نتیجه تست اتصال") },
            text = { Text(testResult ?: "") },
            confirmButton = {
                TextButton(onClick = { testResult = null }) { Text("بستن") }
            },
        )
    }
}
