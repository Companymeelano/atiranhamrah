package ir.atiran.hamrah.viewer.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator

/**
 * صداهای UI فوق‌کوتاه و نرم (فقط برای Eventهای مهم — نه هر کلیک).
 */
object SoundFx {
    @Volatile
    var enabled = true

    private var tone: ToneGenerator? = null

    fun init(@Suppress("unused") context: Context) {
        try {
            if (tone == null) {
                tone = ToneGenerator(AudioManager.STREAM_MUSIC, 35)
            }
        } catch (_: Exception) {
            tone = null
        }
    }

    /** ورود/موفقیت — دو نُت کوتاه نرم */
    fun success() = play(ToneGenerator.TONE_PROP_ACK)

    /** تعامل ظریف (باز کردن پالت، اکشن) */
    fun soft() = play(ToneGenerator.TONE_PROP_BEEP)

    /** هشدار مهم */
    fun alert() = play(ToneGenerator.TONE_PROP_NACK)

    private fun play(type: Int) {
        if (!enabled) return
        try {
            tone?.startTone(type, 150)
        } catch (_: Exception) {
        }
    }

    fun release() {
        try {
            tone?.release()
        } catch (_: Exception) {
        }
        tone = null
    }
}
