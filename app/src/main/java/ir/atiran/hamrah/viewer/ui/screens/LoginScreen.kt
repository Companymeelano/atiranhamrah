package ir.atiran.hamrah.viewer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.data.AtiranSettings
import ir.atiran.hamrah.viewer.data.CustomerLogin
import ir.atiran.hamrah.viewer.data.Login
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(vm: AppViewModel, settings: AtiranSettings) {
    var username by remember { mutableStateOf(settings.username) }
    var password by remember { mutableStateOf(settings.password) }
    var showPassword by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("ورود به سیستم آتیران") })

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "سرور: ${settings.normalizedServer()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "CPUID: ${settings.cpuId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("نام کاربری") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
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

            ErrorBanner(error)

            if (loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        loading = true
                        error = null
                        info = null
                        scope.launch {
                            try {
                                val repo = vm.repository()
                                    ?: throw IllegalStateException("تنظیمات سرور ناقص است")
                                val role = repo.login(Login(username, password))?.Role
                                val profile = repo.getCustomerByLogin(
                                    CustomerLogin(Username = username, Password = password)
                                )
                                val customer = profile.firstOrNull()
                                val shmo = customer?.Shmo
                                info = buildString {
                                    appendLine("ورود موفق ✓")
                                    appendLine("Role: ${role ?: "—"}")
                                    appendLine("مشتری: ${customer?.Moname ?: "—"}")
                                    if (shmo != null) appendLine("ShMo: $shmo")
                                }
                                // ذخیره اطلاعات ورود و سپس رفتن به خانه (بدون race)
                                vm.saveSettings(
                                    settings.copy(
                                        username = username,
                                        password = password,
                                        shMo = shmo?.toString() ?: settings.shMo,
                                    )
                                ) { vm.goHome() }
                            } catch (e: Exception) {
                                error = "ورود ناموفق: ${e.message}"
                            } finally {
                                loading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("ورود")
                }
            }

            OutlinedButton(
                onClick = { vm.goSettings() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("تغییر تنظیمات")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                info ?: "نکته: کلید امنیتی (Token) به‌صورت خودکار از زمان دستگاه ساخته می‌شود و باید با ساعت سرور هماهنگ باشد.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
