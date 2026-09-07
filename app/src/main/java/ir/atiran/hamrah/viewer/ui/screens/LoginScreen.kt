package ir.atiran.hamrah.viewer.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ir.atiran.hamrah.viewer.R
import ir.atiran.hamrah.viewer.data.DbSettings
import ir.atiran.hamrah.viewer.data.SqlServerDb
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.BrandGradient
import ir.atiran.hamrah.viewer.ui.components.ErrorBanner
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(vm: AppViewModel) {
    val saved by vm.settings.collectAsState()

    var host by remember { mutableStateOf(saved.host) }
    var port by remember { mutableStateOf(saved.port) }
    var database by remember { mutableStateOf(saved.database) }
    var user by remember { mutableStateOf(saved.user) }
    var password by remember { mutableStateOf(saved.password) }
    var showPass by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(saved.remember) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(BrandGradient)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // لوگو
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(84.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "آتیران همراه",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                "نمایشگر مدیریت پایگاه داده",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xCCFFFFFF),
            )

            Spacer(modifier = Modifier.height(28.dp))

            // کارت ورود
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "ورود به سیستم",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00513F),
                    )

                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("سرور (IP)") },
                        placeholder = { Text("37.143.147.19") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Dns, contentDescription = null) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it },
                            label = { Text("پورت") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = database,
                            onValueChange = { database = it },
                            label = { Text("نام دیتابیس") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Storage, contentDescription = null) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(2f),
                        )
                    }

                    OutlinedTextField(
                        value = user,
                        onValueChange = { user = it },
                        label = { Text("نام کاربری") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("رمز عبور") },
                        singleLine = true,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            androidx.compose.material3.IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPass) "پنهان‌کردن رمز" else "نمایش رمز",
                                )
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { rememberMe = !rememberMe }
                            .padding(vertical = 2.dp),
                    ) {
                        Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                        Text("اطلاعات ورود را به خاطر بسپار", style = MaterialTheme.typography.bodyMedium)
                    }

                    ErrorBanner(error)

                    Button(
                        onClick = {
                            busy = true
                            error = null
                            scope.launch {
                                try {
                                    val cfg = DbSettings(
                                        host = host.trim(),
                                        port = port.trim().ifBlank { "1433" },
                                        database = database.trim(),
                                        user = user.trim(),
                                        password = password,
                                        remember = rememberMe,
                                    )
                                    vm.login(cfg)
                                } catch (e: Exception) {
                                    error = SqlServerDb.friendly(e)
                                } finally {
                                    busy = false
                                }
                            }
                        },
                        enabled = !busy && host.isNotBlank() && database.isNotBlank() &&
                            user.isNotBlank() && password.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00674B)),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        if (busy) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(modifier = Modifier.size(10.dp))
                            Text("در حال اتصال...")
                        } else {
                            Text("ورود", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                "اتصال امن (TLS) مستقیم به Microsoft SQL Server",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0x99FFFFFF),
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
