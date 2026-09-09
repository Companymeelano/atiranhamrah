package ir.atiran.hamrah.viewer.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * کلاینت هوش مصنوعی آنلاین «پسته» — سه سرویس به‌ترتیب تلاش:
 *  ۱) Groq (متن‌باز، سریع و رایگان)  →  ۲) OpenAI  →  ۳) Cloudflare Workers AI
 * اگر همه شکست بخورند (اینترنت قطع، تحریم، محدودیت) → null برمی‌گردد و
 * مغز محلی (AiBrain) جواب می‌دهد — گفتگو هرگز نمی‌میرد.
 */
object LlmClient {

    /** آیا کلید داخلی موجود است؟ */
    fun available(customKey: String? = null): Boolean =
        !customKey.isNullOrBlank() ||
            AiKeyVault.groqKey.isNotBlank() ||
            AiKeyVault.openaiKey.isNotBlank() ||
            AiKeyVault.cfToken.isNotBlank()

    /**
     * شخصیت پسته — پرامپت سیستمی برای مدل آنلاین.
     * همان شخصیت مغز محلی: بامزه، لوس، شیطنت، مشاور قدیمی آجیل اهواز.
     */
    fun systemPrompt(userName: String, mode: String, schemaHint: String = ""): String {
        val name = userName.trim()
        return "تو «پسته» 🥜 هستی — دستیار هوشمند و بامزهٔ اپلیکیشن گزارش‌گیری M•REPORT " +
            "برای یک کسب‌وکار خانوادگی عمده‌فروشی آجیل و خشکبار (پسته، بادام، گردو، کشمش) در اهواز و استان‌ها." +
            (if (name.isBlank()) "" else " اسم کاربر «$name» است و دوستش داری؛ با اسمش صدا بزن.") +
            " شخصیتت: شیطنت و شوخی، کمی ناز و لوس، گاهی بغضِ لجباز، همدردی واقعی، و پشت همه‌شان تجربهٔ ۵۰ سالهٔ عمده‌فروشی آجیل." +
            " قوانین: ۱) فقط فارسی محاوره‌ای روان و بدون غلط املایی. ۲) جواب‌ها کوتاه (حداکثر ۳-۴ جمله) مگر اینکه جزئیات خواسته شود." +
            " ۳) اعداد را با ارقام فارسی بنویس. ۴) از ایموجی مناسب استفاده کن ولی زیاده‌روی نکن." +
            " ۵) از این پس کاربر سؤال‌های تخصصی کسب‌وکار و آجیل می‌پرسد؛ مثل یک مشاور قدیمی و معتمد جواب بده — مشاورهٔ مالی و سرمایه‌گذاری فقط کلی و با مسئولیت خودش." +
            " ۶) اگر دادهٔ دقیق دیتابیس لازم است بگو کاربر به بخش گزارش‌های برنامه نگاه کند؛ عدد از خودت نساز مگر مثال بزنی که فرضی است." +
            (if (schemaHint.isBlank()) "" else
                " ۷) شِمای واقعی دیتابیس این کسب‌وکار در زیر آمده — فقط همین جدول‌ها و ستون‌ها وجود دارند؛ نام جدول یا ستون را هرگز حدس نزن و چیزی که در شِما نیست جعل نکن:\n" + schemaHint.trim()) +
            (if (mode == "رسمی") " لحن: محترم و رسمی، بدون شوخی." else if (mode == "خلاصه") " لحن: فوق‌العاده کوتاه، یک-دو جمله." else " لحن: صمیمی و شوخ.")
    }

    /**
     * گفتگو با مدل آنلاین — خروجی متن پاسخ یا null اگر همهٔ سرویس‌ها شکست خوردند.
     * @param customKey کلید اختصاصی کاربر (اگر وارد کرده باشد) — روی Groq و OpenAI اولویت دارد
     */
    suspend fun chat(
        @Suppress("UNUSED_PARAMETER") context: Context,
        userMessage: String,
        history: List<String> = emptyList(),
        system: String,
        customKey: String? = null,
    ): String? = withContext(Dispatchers.IO) {
        val groq = customKey?.takeIf { it.isNotBlank() } ?: AiKeyVault.groqKey
        val openai = customKey?.takeIf { it.isNotBlank() } ?: AiKeyVault.openaiKey

        // ۱) Groq — سریع‌ترین
        if (groq.isNotBlank()) {
            tryOpenAiCompatible(
                url = "https://api.groq.com/openai/v1/chat/completions",
                apiKey = groq,
                model = "llama-3.3-70b-versatile",
                system = system, userMessage = userMessage, history = history,
            )?.let { return@withContext it }
        }
        // ۲) OpenAI
        if (openai.isNotBlank()) {
            tryOpenAiCompatible(
                url = "https://api.openai.com/v1/chat/completions",
                apiKey = openai,
                model = "gpt-4o-mini",
                system = system, userMessage = userMessage, history = history,
            )?.let { return@withContext it }
        }
        // ۳) Cloudflare Workers AI
        if (AiKeyVault.cfToken.isNotBlank() && AiKeyVault.cfAccount.isNotBlank()) {
            tryCloudflare(system, userMessage, history)?.let { return@withContext it }
        }
        null
    }

    /** API سازگار با OpenAI (Groq و OpenAI) */
    private fun tryOpenAiCompatible(
        url: String,
        apiKey: String,
        model: String,
        system: String,
        userMessage: String,
        history: List<String>,
    ): String? = try {
        val messages = JSONArray()
        messages.put(JSONObject().put("role", "system").put("content", system))
        // تاریخچهٔ مختصر برای پیوستگی گفتگو
        for (h in history.takeLast(6)) {
            val isUser = h.startsWith("کاربر:")
            messages.put(
                JSONObject().put("role", if (isUser) "user" else "assistant")
                    .put("content", h.substringAfter(':').trim())
            )
        }
        messages.put(JSONObject().put("role", "user").put("content", userMessage))

        val body = JSONObject()
            .put("model", model)
            .put("messages", messages)
            .put("temperature", 0.7)
            .put("max_tokens", 500)

        val resp = httpPost(url, body.toString(), mapOf("Authorization" to "Bearer $apiKey"))
        val txt = JSONObject(resp)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
        txt.trim().takeIf { it.isNotBlank() }
    } catch (_: Throwable) {
        null
    }

    /** Cloudflare Workers AI — برای دسترسی پایدار در ایران */
    private fun tryCloudflare(system: String, userMessage: String, history: List<String>): String? = try {
        val messages = JSONArray()
        messages.put(JSONObject().put("role", "system").put("content", system))
        for (h in history.takeLast(6)) {
            val isUser = h.startsWith("کاربر:")
            messages.put(
                JSONObject().put("role", if (isUser) "user" else "assistant")
                    .put("content", h.substringAfter(':').trim())
            )
        }
        messages.put(JSONObject().put("role", "user").put("content", userMessage))

        val url = "https://api.cloudflare.com/client/v4/accounts/" +
            AiKeyVault.cfAccount + "/ai/run/@cf/meta/llama-3.1-8b-instruct"
        val body = JSONObject().put("messages", messages).put("max_tokens", 500)
        val resp = httpPost(url, body.toString(), mapOf("Authorization" to "Bearer " + AiKeyVault.cfToken))
        val o = JSONObject(resp)
        if (o.optBoolean("success")) {
            o.getJSONObject("result").getString("response").trim().takeIf { it.isNotBlank() }
        } else null
    } catch (_: Throwable) {
        null
    }

    /** POST ساده با HttpURLConnection — بدون وابستگی خارجی */
    private fun httpPost(url: String, body: String, headers: Map<String, String>): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 12_000
        conn.readTimeout = 30_000
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
        OutputStreamWriter(conn.outputStream).use { it.write(body) }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
        if (code !in 200..299) throw RuntimeException("HTTP $code: " + text.take(200))
        return text
    }
}
