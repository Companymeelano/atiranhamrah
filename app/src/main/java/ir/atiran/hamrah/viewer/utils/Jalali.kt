package ir.atiran.hamrah.viewer.utils

/**
 * تقویم هجری شمسی M•REPORT — تبدیل میلادی→شمسی (الگوریتم استاندارد جلالی)،
 * طول ماه‌ها با کبیسه ۳۳ ساله و قالب‌بندی فارسی «۱۷ شهریور ۱۴۰۵».
 * فقط برای نمایش و یادآورها — هیچ داده‌ای را تغییر نمی‌دهد.
 */
object Jalali {

    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
    )

    /** میلادی → شمسی */
    fun fromGregorian(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var jy = if (gy > 1600) 979 else 0
        val gy2 = gy - (if (gy > 1600) 1600 else 621)
        val gy3 = if (gm > 2) gy2 + 1 else gy2
        var days = 365L * gy2 + (gy3 + 3) / 4 - (gy3 + 99) / 100 + (gy3 + 399) / 400 - 80 + gd + gdm[gm - 1]
        jy += (33 * (days / 12053L)).toInt()
        days %= 12053L
        jy += (4 * (days / 1461L)).toInt()
        days %= 1461L
        if (days > 365) {
            jy += ((days - 1) / 365).toInt()
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + (days / 31).toInt() else 7 + ((days - 186) / 30).toInt()
        val jd = 1 + if (days < 186) (days % 31).toInt() else ((days - 186) % 30).toInt()
        return Triple(jy, jm, jd)
    }

    /** امروز به شمسی */
    fun today(): Triple<Int, Int, Int> {
        val cal = java.util.Calendar.getInstance()
        return fromGregorian(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH),
        )
    }

    /** کبیسه شمسی — چرخه ۳۳ ساله */
    fun isLeap(jy: Int): Boolean {
        val m = ((jy % 33) + 33) % 33
        return m == 1 || m == 5 || m == 9 || m == 13 || m == 17 || m == 22 || m == 26 || m == 30
    }

    /** طول ماه شمسی: ۱-۶ سی‌ویک، ۷-۱۱ سی، اسفند ۲۹ (۳۰ در کبیسه) */
    fun monthLen(jy: Int, jm: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        isLeap(jy) -> 30
        else -> 29
    }

    /** تبدیل ارقام لاتین به فارسی */
    fun fa(v: String): String =
        v.map { if (it in '0'..'9') ('۰' + (it - '0')) else it }.joinToString("")

    /** شمسی → میلادی — با تصحیح تدریجی روی خروجی fromGregorian (همیشه دقیق) */
    fun toGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val cal = java.util.Calendar.getInstance()
        cal.set(jy + 621, jm - 1, jd, 12, 0, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        var guard = 0
        while (guard++ < 45) {
            val t = fromGregorian(
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1,
                cal.get(java.util.Calendar.DAY_OF_MONTH),
            )
            val cmp = compare(t, Triple(jy, jm, jd))
            when {
                cmp == 0 -> return Triple(
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH) + 1,
                    cal.get(java.util.Calendar.DAY_OF_MONTH),
                )
                cmp < 0 -> cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
                else -> cal.add(java.util.Calendar.DAY_OF_MONTH, -1)
            }
        }
        return Triple(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH),
        )
    }

    /** مقایسه دو تاریخ شمسی */
    private fun compare(a: Triple<Int, Int, Int>, b: Triple<Int, Int, Int>): Int = when {
        a.first != b.first -> a.first - b.first
        a.second != b.second -> a.second - b.second
        else -> a.third - b.third
    }

    /** «۱۷ شهریور ۱۴۰۵» */
    fun format(jy: Int, jm: Int, jd: Int): String =
        fa(jd.toString()) + " " + monthNames[(jm - 1).coerceIn(0, 11)] + " " + fa(jy.toString())
}
