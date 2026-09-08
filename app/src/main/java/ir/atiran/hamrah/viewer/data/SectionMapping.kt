package ir.atiran.hamrah.viewer.data

import org.json.JSONObject

/**
 * نگاشت بخش‌های M•REPORT به جداول واقعی سرور SQL.
 *
 * هر بخش رابط (مشتریان، کالاها، فروش، چک‌ها، بانک‌ها) به یک جدول واقعی
 * «schema.table» وصل می‌شود؛ نقش ستون‌ها (نام، مبلغ، تاریخ، کد) به‌صورت
 * هوشمند از نام ستون‌ها تشخیص داده می‌شود. همه‌چیز فقط-خواندنی است.
 */

/** نگاشت بخش‌ها به جداول — null یعنی هنوز وصل نشده */
data class SectionMap(
    val customers: String? = null,
    val products: String? = null,
    val invoices: String? = null,
    val checks: String? = null,
    val banks: String? = null,
) {
    fun tableFor(section: String): String? = when (section) {
        "customers" -> customers
        "products" -> products
        "invoices" -> invoices
        "checks" -> checks
        "banks" -> banks
        else -> null
    }

    fun withSection(section: String, ref: String?): SectionMap = when (section) {
        "customers" -> copy(customers = ref)
        "products" -> copy(products = ref)
        "invoices" -> copy(invoices = ref)
        "checks" -> copy(checks = ref)
        "banks" -> copy(banks = ref)
        else -> this
    }

    val allConnected: Boolean
        get() = customers != null && products != null && invoices != null && checks != null && banks != null

    val anyConnected: Boolean
        get() = customers != null || products != null || invoices != null || checks != null || banks != null
}

fun parseSectionMap(json: String?): SectionMap {
    if (json.isNullOrBlank()) return SectionMap()
    return try {
        val o = JSONObject(json)
        SectionMap(
            customers = o.optString("customers").takeIf { it.isNotBlank() },
            products = o.optString("products").takeIf { it.isNotBlank() },
            invoices = o.optString("invoices").takeIf { it.isNotBlank() },
            checks = o.optString("checks").takeIf { it.isNotBlank() },
            banks = o.optString("banks").takeIf { it.isNotBlank() },
        )
    } catch (_: Throwable) {
        SectionMap()
    }
}

fun serializeSectionMap(m: SectionMap): String = JSONObject().apply {
    put("customers", m.customers ?: "")
    put("products", m.products ?: "")
    put("invoices", m.invoices ?: "")
    put("checks", m.checks ?: "")
    put("banks", m.banks ?: "")
}.toString()

/** نقش ستون‌های یک جدول در نمایش M•REPORT */
data class TableRoles(
    val titleCol: String?,
    val amountCol: String?,
    val dateCol: String?,
    val codeCol: String?,
)

/** یک رکورد واقعی از سرور — نام ستون‌ها + مقدارها */
data class RealRow(val colNames: List<String>, val values: List<String?>) {
    fun value(col: String?): String? {
        if (col == null) return null
        val i = colNames.indexOf(col)
        return if (i >= 0) values.getOrNull(i) else null
    }
}

/** نمونه‌ای از یک جدول واقعی — ستون‌ها + رکوردها + تعداد کل */
data class RealTable(
    val cols: List<ColumnInfo>,
    val rows: List<RealRow>,
    val total: Long,
)

/** «dbo.MyTable» → ("dbo", "MyTable") */
fun splitRef(ref: String): Pair<String, String> {
    val s = ref.trim()
    return if (s.contains('.')) s.substringBefore('.') to s.substringAfter('.') else "dbo" to s
}

/**
 * تشخیص هوشمند — کدام جدول برای کدام بخش؛ نقش ستون‌ها از نامشان.
 * برای نرم‌افزارهای حسابداری ایرانی (کالا/مشتری/فاکتور/چک/بانک) و
 * انگلیسی (Product/Customer/Invoice/Check/Bank) هر دو جواب می‌دهد.
 */
object TableHeuristics {

    private val SECTION_STEMS = mapOf(
        "customers" to listOf("customer", "moshtar", "مشتری", "ashkh", "اشخاص", "person", "client", "partner", "طرف"),
        "products" to listOf("kala", "کالا", "product", "item", "goods", "جنس", "merch"),
        "invoices" to listOf("factor", "faktor", "فاکتور", "invoice", "sale", "forosh", "فروش", "order"),
        "checks" to listOf("check", "cheque", "چک"),
        "banks" to listOf("bank", "بانک"),
    )

    /** بهترین جدول کاندید برای یک بخش — «schema.table» یا null */
    fun detectTable(section: String, tables: List<TableInfo>): String? {
        val stems = SECTION_STEMS[section] ?: return null
        var bestScore = 0
        var bestRef: String? = null
        for (t in tables) {
            val full = (t.schema + "." + t.name).lowercase()
            var score = 0
            for (s in stems) {
                if (full.contains(s)) {
                    score += if (full.substringAfter('.').startsWith(s)) 3 else 1
                }
            }
            if (score > 0) score += (24 - t.name.length.coerceAtMost(24)) / 8
            if (score > bestScore) {
                bestScore = score
                bestRef = t.schema + "." + t.name
            }
        }
        return bestRef
    }

    /** تشخیص خودکار نگاشت همه بخش‌ها از فهرست جداول سرور */
    fun detectMap(tables: List<TableInfo>): SectionMap = SectionMap(
        customers = detectTable("customers", tables),
        products = detectTable("products", tables),
        invoices = detectTable("invoices", tables),
        checks = detectTable("checks", tables),
        banks = detectTable("banks", tables),
    )

    private val NUMERIC_TYPES = setOf(
        "int", "bigint", "smallint", "tinyint", "bit", "decimal", "numeric",
        "float", "real", "money", "smallmoney",
    )
    private val TEXTY_TYPES = setOf("char", "nchar", "varchar", "nvarchar", "text", "ntext", "sysname")

    private val TITLE_STEMS = listOf("fullname", "customername", "kalaname", "name", "title", "onvan", "نام", "شرح")
    private val AMOUNT_STEMS = listOf(
        "mablagh", "amount", "مبلغ", "price", "ghimat", "قیمت",
        "bedehkar", "bestankar", "mandeh", "mande", "مانده", "balance", "total", "jam", "جمع",
    )
    private val DATE_STEMS = listOf("date", "tarikh", "تاریخ")
    private val CODE_STEMS = listOf("code", "shomare", "کد", "id")

    private fun findCol(cols: List<ColumnInfo>, stems: List<String>, numericOnly: Boolean = false): String? {
        val pool = if (numericOnly) cols.filter { it.type.lowercase() in NUMERIC_TYPES } else cols
        if (pool.isEmpty()) return null
        for (s in stems) for (c in pool) if (c.name.lowercase() == s) return c.name
        for (s in stems) for (c in pool) if (c.name.lowercase().startsWith(s)) return c.name
        for (s in stems) for (c in pool) if (c.name.lowercase().contains(s)) return c.name
        return null
    }

    /** نقش ستون‌های جدول: نام/مبلغ/تاریخ/کد */
    fun detectRoles(cols: List<ColumnInfo>): TableRoles = TableRoles(
        titleCol = findCol(cols, TITLE_STEMS)
            ?: cols.firstOrNull { it.type.lowercase() in TEXTY_TYPES && !it.name.lowercase().endsWith("id") }?.name,
        amountCol = findCol(cols, AMOUNT_STEMS, numericOnly = true)
            ?: cols.firstOrNull { it.type.lowercase() in NUMERIC_TYPES }?.name,
        dateCol = findCol(cols, DATE_STEMS),
        codeCol = findCol(cols, CODE_STEMS),
    )

    /** «1,234.50» یا «۱۲۳۴» → عدد */
    fun parseNum(v: String?): Double? {
        if (v.isNullOrBlank()) return null
        val cleaned = v.replace(",", "").replace("٬", "").replace("،", "").trim()
        return cleaned.toDoubleOrNull()
    }

    /** عدد → «۱٬۲۳۴٬۵۶۷» با ارقام فارسی و جداکننده هزارگان */
    fun faMoney(d: Double): String {
        var v = kotlin.math.abs(d).toLong()
        if (v == 0L) return "۰"
        val parts = ArrayList<String>()
        while (v > 0) {
            parts.add((v % 1000).toString())
            v /= 1000
        }
        val grouped = parts.reversed().joinToString("٬")
        return (if (d < 0) "−" else "") + faDigits(grouped)
    }

    /** تبدیل ارقام لاتین به فارسی */
    fun faDigits(s: String): String =
        s.map { if (it in '0'..'9') ('۰' + (it - '0')) else it }.joinToString("")
}

// ------------------------------------------------------------ خواندن داده واقعی

/** نمونه رکوردهای یک جدول + ستون‌ها + تعداد کل */
suspend fun SqlServerDb.realTable(ref: String, sample: Int = 200): RealTable {
    val (schema, table) = splitRef(ref)
    val cols = columns(schema, table)
    val p = page(schema, table, 0, sample.toLong(), "")
    val rows = p.rows.map { RealRow(p.columns, it) }
    return RealTable(cols, rows, p.total)
}

/** برترین رکوردها بر اساس یک ستون (نزولی) */
suspend fun SqlServerDb.realTop(ref: String, orderCol: String, limit: Int = 5): List<RealRow> {
    val (schema, table) = splitRef(ref)
    val (names, rows) = topBy(schema, table, orderCol, limit)
    return rows.map { RealRow(names, it) }
}

/** آخرین رکوردها بر اساس ستون تاریخ (نزولی) */
suspend fun SqlServerDb.realLatest(ref: String, dateCol: String, limit: Int = 8): List<RealRow> {
    val (schema, table) = splitRef(ref)
    val (names, rows) = latestBy(schema, table, dateCol, limit)
    return rows.map { RealRow(names, it) }
}
