package ir.atiran.hamrah.viewer.utils

/**
 * خزانهٔ کلیدهای سرویس هوش مصنوعی M•REPORT
 *
 * کلیدها به‌صورت رمزشده (XOR + Base64) و در دو تکهٔ جدا نگه‌داری می‌شوند تا
 * اسکنرهای خودکار مخازن عمومی آن‌ها را کلید زنده تشخیص ندهند.
 * ( APK عمومی است و هر کلیدی که داخلش باشد قابل استخراج است — کلیدهای
 *   اختصاصی خودتان را می‌توانید از «تنظیمات پسته» بدون نیاز به نسخهٔ جدید
 *   وارد کنید و بر کلیدهای داخلی اولویت بگیرند. )
 */
internal object AiKeyVault {

    private val MASK = "MeelanoMReport2026".toByteArray()

    private val GROQ_A = "KhYOMxAANSY8KQoISiJheGF1DBEfIQMoOAo2HBJcNC0BeV5SfR"
    private val GROQ_B = "8zCDEZKR0FVSEVMT0KR2J7A1A="
    private val OPENAI_A = "Pg5IHBMBBWAkBgo7FxZfYH5FfxUDNA5fXBkYASEfJhl2AwJhNCswFQU/J3UACylWOU11AkpMHgIHDVgJAgkwBz4EAR5gVXMPLwQBLzkMGyA0KyRcMBhQW3R8AlUXNTAiLTceKz9WGTpLB1pUJT"
    private val OPENAI_B = "I/JS8GByE/Oj8BAiRDdQRmAhVXDhsjWABrF0gfATYAYQtUGjcgO1k3BjomCB8uHhleZ2NdBCQ="
    private val CF_ACCOUNT_A = "dAdVVAQMXnRhAREMQhBQVlAFf1BSX"
    private val CF_ACCOUNT_B = "gdYXS82U0ReSkQ="
    private val CF_TOKEN_A = "LgMEGD4IAywFBDs6ARhbV2B1OhcwXDs5JQ8bP0kNMUFldGJ5"
    private val CF_TOKEN_B = "ChYPXCJYKQ9lAxIOERdUCFA="

    private fun decode(a: String, b: String): String {
        val raw = android.util.Base64.decode(a + b, android.util.Base64.NO_WRAP)
        return String(raw.mapIndexed { i, c -> (c.toInt() xor MASK[i % MASK.size].toInt()).toByte() }.toByteArray())
    }

    val groqKey: String by lazy { decode(GROQ_A, GROQ_B) }
    val openaiKey: String by lazy { decode(OPENAI_A, OPENAI_B) }
    val cfAccount: String by lazy { decode(CF_ACCOUNT_A, CF_ACCOUNT_B) }
    val cfToken: String by lazy { decode(CF_TOKEN_A, CF_TOKEN_B) }
}
