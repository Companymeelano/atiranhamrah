package ir.atiran.hamrah.viewer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ir.atiran.hamrah.viewer.ui.AppViewModel
import ir.atiran.hamrah.viewer.ui.theme.LocalThemeExtras
import ir.atiran.hamrah.viewer.utils.AiBrain
import ir.atiran.hamrah.viewer.utils.LlmClient
import ir.atiran.hamrah.viewer.utils.SoundFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

private data class AiMsg(val me: Boolean, val text: String)

// ============================================================ گفتگو با پسته
/**
 * دستیار هوشمند «پسته» — گفتگوی فارسی با تحلیل داده‌ها، حافظه‌ی اسم
 * کاربر و لحن قابل‌تنظیم (شوخ/رسمی/خلاصه).
 */
@Composable
fun AiChatSheet(vm: AppViewModel, onDismiss: () -> Unit, onOpenSettings: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    val appCtx = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var awaitingName by remember { mutableStateOf(vm.ai.userName.isBlank()) }
    val listState = rememberLazyListState()
    var msgs by remember {
        mutableStateOf(
            listOf(
                AiMsg(
                    false,
                    AiBrain.greeting(AiBrain.Ctx(vm.ai.userName, vm.ai.mode, vm.ai.domains, vm.ai.visits)),
                )
            )
        )
    }
    LaunchedEffect(msgs.size) {
        if (msgs.isNotEmpty()) listState.animateScrollToItem(msgs.lastIndex)
    }

    var thinking by remember { mutableStateOf(false) }

    fun send(raw: String) {
        val text = raw.trim()
        if (text.isEmpty()) return
        SoundFx.soft()
        val brainCtx = AiBrain.Ctx(vm.ai.userName, vm.ai.mode, vm.ai.domains, vm.ai.visits)
        msgs = msgs + AiMsg(true, text)
        if (awaitingName && AiBrain.looksLikeName(text)) {
            vm.updateAi { it.copy(userName = text) }
            awaitingName = false
            msgs = msgs + AiMsg(false, AiBrain.nameThanks(text))
            return
        }
        // اول هوش مصنوعی آنلاین (اگر فعال و در دسترس)؛ اگر نشد مغز محلی پسته
        thinking = true
        msgs = msgs + AiMsg(false, "🥜 ...")
        scope.launch {
            val online = vm.ai.online && LlmClient.available(vm.ai.apiKey.takeIf { it.isNotBlank() })
            val history = msgs.dropLast(2).takeLast(6).map {
                (if (it.me) "کاربر:" else "پسته:") + " " + it.text
            }
            val onlineReply = if (online) {
                try {
                    LlmClient.chat(
                        context = appCtx,
                        userMessage = text,
                        history = history,
                        system = LlmClient.systemPrompt(vm.ai.userName, vm.ai.mode),
                        customKey = vm.ai.apiKey.takeIf { it.isNotBlank() },
                    )
                } catch (_: Throwable) {
                    null
                }
            } else null
            val finalAnswer = onlineReply ?: AiBrain.reply(text, brainCtx)
            if (vm.ai.userName.isBlank() && AiBrain.askingName(finalAnswer)) awaitingName = true
            thinking = false
            msgs = msgs.dropLast(1) + AiMsg(false, finalAnswer)
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .fillMaxSize()
                .background(scheme.background),
        ) {
            // ---------- سربرگ
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                PistachioLive(size = 38.dp, bobbing = false, thinking = thinking)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("پسته", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text(
                        "دستیار هوشمند · متصل به داده‌ها",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
                MrOrbButton(MrIcons.Settings, "تنظیمات پسته", size = 32.dp) { onOpenSettings(); SoundFx.soft() }
                Spacer(Modifier.width(6.dp))
                MrOrbButton(MrIcons.Close, "بستن", size = 32.dp, tint = scheme.onSurfaceVariant) { onDismiss() }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(extras.hairline))

            // ---------- پیام‌ها
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(msgs.size) { i ->
                    val m = msgs[i]
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = if (m.me) Arrangement.Start else Arrangement.End,
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            if (!m.me) {
                                PistachioLive(size = 26.dp, bobbing = false)
                                Spacer(Modifier.width(6.dp))
                            }
                            Box(
                                Modifier
                                    .widthIn(max = 300.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (m.me) scheme.primary.copy(alpha = 0.13f)
                                        else extras.glass
                                    )
                                    .border(
                                        1.dp,
                                        if (m.me) scheme.primary.copy(alpha = 0.35f) else extras.hairline,
                                        RoundedCornerShape(16.dp),
                                    )
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                            ) {
                                Text(
                                    m.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurface,
                                )
                            }
                            if (m.me) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(scheme.primary.copy(alpha = 0.16f))
                                        .border(1.dp, scheme.primary.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("من", style = MaterialTheme.typography.labelSmall, color = scheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // ---------- پیشنهادهای سریع (متناسب با دامنه‌های فعال)
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                listOf(
                    "فروش امروز؟" to "گزارش‌ها",
                    "مقایسه با دیروز" to "گزارش‌ها",
                    "بهترین مشتری کیه؟" to "مشتریان",
                    "مشتریان جدید" to "مشتریان",
                    "بهترین کالا" to "کالاها",
                    "موجودی پسته" to "کالاها",
                    "وضعیت مطالبات" to "مطالبات",
                    "بدهکارها کیان؟" to "مطالبات",
                    "چک‌های نزدیک" to "چک‌ها",
                    "سررسید فردا" to "چک‌ها",
                    "پیشنهاد بده" to "",
                ).forEach { (q, domain) ->
                    if (domain.isBlank() || domain in vm.ai.domains) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(extras.glass)
                                .border(1.dp, extras.hairline, RoundedCornerShape(50))
                                .clickable { send(q) }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                        ) {
                            Text(
                                q,
                                style = MaterialTheme.typography.labelMedium,
                                color = scheme.primary,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            // ---------- ورودی
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("از پسته بپرس...") },
                    maxLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                )
                MrOrbButton(MrIcons.Send, "ارسال", size = 40.dp) { send(input); input = "" }
            }
        }
    }
}

// ============================================================ تنظیمات پسته
@Composable
fun AiSettingsSheet(vm: AppViewModel, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    var name by remember(vm.ai.userName) { mutableStateOf(vm.ai.userName) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.surface.copy(alpha = 0.97f))
                .border(1.dp, extras.hairline, RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // سربرگ
            Row(verticalAlignment = Alignment.CenterVertically) {
                PistachioLive(size = 34.dp, bobbing = false)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("دستیار هوشمند «پسته»", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text("تنظیمات، حافظه و لحن گفتار", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                }
            }

            // فعال/غیرفعال
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("دستیار هوشمند", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text("خوشامدگویی و گفتگو با پسته", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                }
                Switch(
                    checked = vm.ai.enabled,
                    onCheckedChange = { vm.updateAi { a -> a.copy(enabled = it) }; SoundFx.soft() },
                    colors = SwitchDefaults.colors(checkedTrackColor = scheme.primary),
                )
            }

            // هوش مصنوعی آنلاین — پاسخ‌های واقعی مدل زبانی
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("پاسخ‌های آنلاین 🧠", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(
                        "گفتگوی واقعی با هوش مصنوعی — بدون اینترنت، مغز محلی پسته جواب می‌دهد",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = vm.ai.online,
                    onCheckedChange = { vm.updateAi { a -> a.copy(online = it) }; SoundFx.soft() },
                    colors = SwitchDefaults.colors(checkedTrackColor = scheme.primary),
                )
            }

            // کلید API اختصاصی (اختیاری) — بر کلیدهای داخلی اولویت دارد
            var apiKeyInput by remember(vm.ai.apiKey) { mutableStateOf(vm.ai.apiKey) }
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                placeholder = { Text("کلید API اختصاصی (اختیاری — Groq یا OpenAI)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "اگر کلید خودتان را وارد کنید، پسته با آن جواب می‌دهد؛ خالی بگذارید تا کلید داخلی برنامه استفاده شود.",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )

            // اسم کاربر — حافظه پسته (ذخیرهٔ فوری)
            Text("اسم من چی باشه؟", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = scheme.primary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("مثلاً: مهندس میلاد") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                )
                if (name.trim() != vm.ai.userName && name.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        vm.updateAi { it.copy(userName = name.trim()) }
                        SoundFx.success()
                    }) { Text("ذخیره") }
                }
            }

            // لحن گفتار
            Text("لحن گفتار", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = scheme.primary)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("شوخ" to "شوخ و خودمونی", "رسمی" to "جدی و کاری", "خلاصه" to "فقط نتیجه").forEach { (m, d) ->
                    val sel = vm.ai.mode == m
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (sel) Brush.verticalGradient(
                                    listOf(scheme.primary.copy(alpha = 0.22f), scheme.primary.copy(alpha = 0.06f))
                                ) else androidx.compose.ui.graphics.SolidColor(extras.glass)
                            )
                            .border(
                                1.dp,
                                if (sel) scheme.primary.copy(alpha = 0.5f) else extras.hairline,
                                RoundedCornerShape(12.dp),
                            )
                            .clickable { vm.updateAi { it.copy(mode = m) }; SoundFx.soft() }
                            .padding(vertical = 8.dp),
                    ) {
                        Text(
                            m,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (sel) scheme.primary else scheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            d,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }

            // دامنه‌های تحلیل
            Text("بخش‌های قابل تحلیل", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = scheme.primary)
            AiBrain.allDomains.chunked(3).forEach { rowDomains ->
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    rowDomains.forEach { d ->
                        val sel = d in vm.ai.domains
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (sel) scheme.primary.copy(alpha = 0.12f) else extras.glass
                                )
                                .border(
                                    1.dp,
                                    if (sel) scheme.primary.copy(alpha = 0.45f) else extras.hairline,
                                    RoundedCornerShape(12.dp),
                                )
                                .clickable {
                                    vm.updateAi {
                                        it.copy(
                                            domains = if (sel) it.domains - d else it.domains + d
                                        )
                                    }
                                    SoundFx.soft()
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                        ) {
                            Box(
                                Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (sel) scheme.primary else scheme.onSurfaceVariant.copy(alpha = 0.4f))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                d,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (sel) scheme.primary else scheme.onSurfaceVariant,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                            )
                        }
                    }
                    if (rowDomains.size < 3) repeat(3 - rowDomains.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            Text(
                "تنظیمات فقط روی همین دستگاه ذخیره می‌شود و به هیچ سروری ارسال نمی‌گردد.",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
            // دکمه ذخیره + اطلاع‌رسانی
            var saved by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (saved) {
                    Text(
                        "تنظیمات ذخیره شد ✓",
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                MrPillButton(
                    label = if (saved) "ذخیره شد ✓" else "ذخیره تنظیمات",
                    icon = MrIcons.Bookmark,
                    onClick = {
                    if (!saved) {
                        if (name.trim() != vm.ai.userName && name.isNotBlank()) {
                            vm.updateAi { it.copy(userName = name.trim()) }
                        }
                        if (apiKeyInput.trim() != vm.ai.apiKey) {
                            vm.updateAi { it.copy(apiKey = apiKeyInput.trim()) }
                        }
                        saved = true
                        SoundFx.success()
                        scope.launch {
                            delay(1300)
                            onDismiss()
                        }
                    }
                },
                )
            }
        }
    }
}

// ============================================================ خوشامدگویی پسته
/** حباب سلام پسته بعد از ورود — لمس پسته یا دکمه، گفتگو را باز می‌کند */
@Composable
fun AiGreetingOverlay(
    text: String,
    onChat: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val extras = LocalThemeExtras.current
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PistachioLive(size = 56.dp)
            Column(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(scheme.surface.copy(alpha = 0.96f))
                    .border(1.dp, extras.hairline, RoundedCornerShape(18.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurface,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(scheme.primary.copy(alpha = 0.14f))
                            .border(1.dp, scheme.primary.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .clickable { onChat(); SoundFx.soft() }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(
                            "صحبت کنیم",
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(extras.glass)
                            .border(1.dp, extras.hairline, RoundedCornerShape(50))
                            .clickable { onClose() }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(
                            "بعداً",
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
