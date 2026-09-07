package ir.atiran.hamrah.viewer.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class AtiranException(message: String, val statusCode: Int = 0) : Exception(message)

/**
 * Thin HTTP client for the Atiran WCF (webHttpBinding / JSON) service.
 *
 * Wire format reverse-engineered from AtiranLocalServices.dll:
 *   * every operation is POST
 *   * UriTemplates are relative to ".../LocalServices.svc"
 *   * BodyStyle 0/1 (None/Bare) -> body = single DTO JSON ("bare")
 *   * BodyStyle 2 (Wrapped)     -> body = {"paramName": {...}, ...}
 *   * response = AtiranResult {"Status","Type","Result"}
 *     Result is a JSON *string* produced by JavaScriptSerializer.
 */
class AtiranClient(settings: AtiranSettings) {

    private val base = settings.normalizedServer()
    private val cpuId = settings.cpuId.trim()

    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        coerceInputValues = true
        isLenient = true
    }

    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ------------------------------------------------------------ token
    /**
     * SecurityToken.Key algorithm (from Util.checkTokenKey):
     *   key = (YYYYMMDDHHMMSS as long) * 3593
     * must be within ±200 minutes of the server clock, so a fresh key is
     * generated for every request.
     */
    fun freshToken(): SecurityToken =
        SecurityToken(cpuId, keyFor(Calendar.getInstance()))

    companion object {
        fun keyFor(now: Calendar): String {
            // Locale.US تضمین می‌کند ارقام ASCII تولید شود (نه ارقام فارسی/عربی)
            val s = String.format(
                Locale.US,
                "%04d%02d%02d%02d%02d%02d",
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_MONTH),
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND),
            )
            return (s.toLong() * 3593L).toString()
        }

        /** انکود امن یک قطعه مسیر URL (بدون شکستن ساختار مسیر). */
        fun enc(seg: String): String =
            java.net.URLEncoder.encode(seg, "UTF-8").replace("+", "%20")

        fun isKeyFresh(key: String, toleranceMinutes: Long = 200, now: Calendar = Calendar.getInstance()): Boolean {
            val n = key.toLongOrNull() ?: return false
            if (n % 3593L != 0L) return false
            val dtNum = (n / 3593L).toString()
            if (dtNum.length != 14) return false
            val dtDate = try {
                SimpleDateFormat("yyyyMMddHHmmss", Locale.US).parse(dtNum)
            } catch (_: Exception) {
                return false
            } ?: return false
            val diffMinutes = abs((now.timeInMillis - dtDate.time) / 60000.0)
            return diffMinutes <= toleranceMinutes
        }
    }

    // ------------------------------------------------------------ transport
    private suspend fun postRaw(path: String, bodyJson: String?): String =
        withContext(Dispatchers.IO) {
            val url = base + "/" + path.trimStart('/')
            val builder = Request.Builder().url(url).post(
                (bodyJson ?: "").toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            http.newCall(builder.build()).execute().use { resp ->
                val text = resp.body?.string() ?: ""
                if (resp.code !in 200..299) {
                    throw AtiranException("خطای سرور (${resp.code}): ${text.take(400)}", resp.code)
                }
                text
            }
        }

    /** Parse the outer AtiranResult and unwrap `Result`. */
    private fun parseResult(raw: String): String? {
        val root = json.parseToJsonElement(raw).jsonObject
        val status = root["Status"]?.jsonPrimitive?.let { p ->
            p.contentOrNull?.trim()?.toIntOrNull()
        } ?: 0
        val type = root["Type"]?.jsonPrimitive?.let { p ->
            p.contentOrNull?.trim()?.toIntOrNull()
        } ?: 0
        val result = root["Result"]?.jsonPrimitive?.contentOrNull
        if (status != 1) {
            val msg = result?.take(300) ?: "Status=$status Type=$type"
            throw AtiranException(msg)
        }
        return result
    }

    private suspend fun unwrap(path: String, bodyJson: String?): JsonElement? {
        val raw = postRaw(path, bodyJson)
        val result = parseResult(raw) ?: return null
        return json.parseToJsonElement(result)
    }

    suspend fun <T> callSingle(path: String, bodyJson: String?, serializer: KSerializer<T>): T? {
        val el = unwrap(path, bodyJson) ?: return null
        return json.decodeFromJsonElement(serializer, el)
    }

    suspend fun <T> callList(path: String, bodyJson: String?, itemSerializer: KSerializer<T>): List<T> {
        val el = unwrap(path, bodyJson) ?: return emptyList()
        if (el is JsonArray) {
            return json.decodeFromJsonElement(ListSerializer(itemSerializer), el)
        }
        // tolerate a single object instead of an array
        return try {
            listOf(json.decodeFromJsonElement(itemSerializer, el))
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun callStringList(path: String, bodyJson: String?): List<String> {
        val el = unwrap(path, bodyJson) ?: return emptyList()
        if (el is JsonArray) {
            return el.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
        }
        return emptyList()
    }

    suspend fun callCount(path: String, bodyJson: String?): Int? {
        val el = unwrap(path, bodyJson) ?: return null
        return (el as? JsonPrimitive)?.let { p -> p.contentOrNull?.trim()?.toIntOrNull() }
    }

    suspend fun callString(path: String, bodyJson: String?): String? {
        val el = unwrap(path, bodyJson) ?: return null
        return (el as? JsonPrimitive)?.contentOrNull
    }

    fun <T> encode(serializer: KSerializer<T>, value: T): String =
        json.encodeToString(serializer, value)
}
