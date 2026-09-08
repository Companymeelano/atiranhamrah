package ir.atiran.hamrah.viewer.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

class AtiranDbException(message: String) : Exception(message)

/**
 * اتصال مستقیم و فقط-خواندنی به Microsoft SQL Server از روی درایور JDBC.
 *
 * امنیت:
 *  - همه کوئری‌ها پارامتری هستند؛ نام جدول/ستون قبل از استفاده با
 *    INFORMATION_SCHEMA اعتبارسنجی و داخل براکت escape می‌شود (SQL Injection نداریم).
 *  - ترافیک با TLS رمز می‌شود (encrypt=true) و گواهی سرور تایید نمی‌شود
 *    (trustServerCertificate=true) تا اتصال داخلی بدون گواهی معتبر هم کار کند.
 */
/** یک گام عیب‌یابی اتصال — نتیجه، توضیح و راهنمای رفع */
data class DiagStep(
    val ok: Boolean,
    val title: String,
    val detail: String,
    val hint: String? = null,
)

class SqlServerDb(private val cfg: DbSettings) {

    companion object {
        init {
            // بارگذاری صریح درایور (در اندروید ServiceLoader قابل اتکا نیست)
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
        }

        private val TEXT_TYPES = setOf(
            "char", "nchar", "varchar", "nvarchar", "text", "ntext", "sysname"
        )

        /** پیام خطای فارسی و قابل فهم برای کاربر */
        fun friendly(e: Throwable): String {
            if (e is AtiranDbException) return e.message ?: "خطای دیتابیس"
            val m = e.message ?: e.toString()
            return when {
                m.contains("Login failed", ignoreCase = true) ->
                    "نام کاربری یا رمز عبور SQL Server اشتباه است"
                m.contains("Cannot open database", ignoreCase = true) ->
                    "دیتابیس مورد نظر باز نشد — نام دیتابیس یا دسترسی کاربر را بررسی کنید"
                m.contains("The TCP/IP connection to the host", ignoreCase = true) ||
                    m.contains("Connection refused", ignoreCase = true) ||
                    m.contains("timed out", ignoreCase = true) ||
                    m.contains("UnknownHost", ignoreCase = true) ||
                    m.contains("No route to host", ignoreCase = true) ->
                    "اتصال به سرور برقرار نشد — آدرس/پورت، فایروال یا فعال‌بودن TCP/IP در SQL Server را بررسی کنید"
                m.contains("SQL Server did not return a response", ignoreCase = true) ||
                    m.contains("Connection reset", ignoreCase = true) ||
                    m.contains("socket was closed", ignoreCase = true) ->
                    "سرور اتصال را پذیرفت ولی پاسخ نداد — معمولاً پروتکل TCP/IP در SQL Server فعال نیست یا TLS سرور قدیمی است. از دکمه «عیب‌یابی اتصال» استفاده کنید"
                m.contains("not associated with a trusted SQL Server connection", ignoreCase = true) ->
                    "این حساب، ویندوزی است — کاربر باید SQL Server Authentication باشد یا حالت Mixed Mode در سرور فعال شود"
                m.contains("SSL", ignoreCase = true) || m.contains("certificate", ignoreCase = true) ||
                    m.contains("secure connection", ignoreCase = true) ||
                    m.contains("protocol version", ignoreCase = true) ->
                    "خطای رمزنگاری در اتصال به سرور — برنامه اتصال بدون رمزنگاری را هم خودکار امتحان می‌کند؛ از «عیب‌یابی اتصال» برای جزئیات استفاده کنید"
                else -> m.take(300)
            }
        }

        /** escape یک شناسه داخل براکت */
        private fun q(ident: String): String = "[" + ident.replace("]", "]]") + "]"
    }

    /** نام سرور و نمونه — پشتیبانی از SERVER\INSTANCE (مثل SERVER\SQLEXPRESS) */
    private val serverHost: String = cfg.host.trim().substringBefore('\\')
    private val instanceName: String? =
        cfg.host.trim().substringAfter('\\', "").takeIf { it.isNotBlank() }

    /** حالت رمزنگاری که قبلاً جواب داده — تا اتصال‌های بعدی دوباره تست نشوند */
    @Volatile
    private var tlsWorked: Boolean? = null

    private fun url(encrypt: Boolean): String {
        val sb = StringBuilder("jdbc:sqlserver://").append(serverHost)
        // با نمونه (instance)، پورت نمی‌گذاریم تا درایور از SQL Browser پورت واقعی را بپرسد
        if (instanceName == null) {
            sb.append(":").append(cfg.port.trim().ifBlank { "1433" })
        }
        sb.append(";databaseName=").append(cfg.database.trim())
        sb.append(";encrypt=").append(if (encrypt) "true" else "false")
        if (encrypt) sb.append(";trustServerCertificate=true")
        sb.append(";loginTimeout=20;connectRetryCount=2;connectRetryDelay=1")
        sb.append(";applicationName=AtiranHamrahViewer")
        if (instanceName != null) sb.append(";instanceName=").append(instanceName)
        return sb.toString()
    }

    /** آیا خطا مربوط به رمزنگاری/دست‌دادن TLS است؟ */
    private fun isTlsError(e: Throwable): Boolean {
        val m = (e.message ?: "") + " " + e.toString()
        return m.contains("SSL", true) || m.contains("TLS", true) ||
            m.contains("certificate", true) || m.contains("secure connection", true) ||
            m.contains("protocol version", true) ||
            m.contains("SQL Server did not return a response", true) ||
            m.contains("Connection reset", true)
    }

    private fun props(): Properties = Properties().apply {
        setProperty("user", cfg.user.trim())
        setProperty("password", cfg.password)
    }

    /**
     * اتصال هوشمند: اول با رمزنگاری TLS؛ اگر سرور TLS قدیمی/خرابی داشت،
     * خودکار یک‌بار بدون رمزنگاری امتحان می‌کند و حالت جواب‌داده را نگه می‌دارد.
     */
    private fun open(): Connection {
        tlsWorked?.let { worked ->
            return DriverManager.getConnection(url(worked), props())
        }
        return try {
            DriverManager.getConnection(url(true), props()).also { tlsWorked = true }
        } catch (e: Throwable) {
            if (isTlsError(e)) {
                DriverManager.getConnection(url(false), props()).also { tlsWorked = false }
            } else {
                throw e
            }
        }
    }

    // ------------------------------------------------------------ عیب‌یابی
    /**
     * عیب‌یابی مرحله‌به‌مرحله اتصال — دقیقاً نشان می‌دهد کدام گام می‌شکند:
     * پیدا کردن سرور (DNS) → درگاه TCP → رمزنگاری TLS → ورود → دیتابیس.
     */
    suspend fun diagnose(): List<DiagStep> = withContext(Dispatchers.IO) {
        val steps = ArrayList<DiagStep>()

        // گام ۱: پیدا کردن سرور در شبکه
        try {
            val addr = java.net.InetAddress.getByName(serverHost)
            steps += DiagStep(true, "پیدا کردن سرور", "آدرس سرور: " + addr.hostAddress)
        } catch (e: Throwable) {
            steps += DiagStep(
                false, "پیدا کردن سرور",
                "آدرس «" + serverHost + "» در شبکه پیدا نشد",
                "IP یا نام سرور را دقیق بررسی کنید. اگر از اینترنت وصل می‌شوید، سرور باید با IP عمومی منتشر شده باشد.",
            )
            return@withContext steps
        }

        // گام ۲: درگاه TCP (فقط وقتی نمونه ندارد؛ با نمونه، درایور خودش از SQL Browser می‌پرسد)
        val portNum = cfg.port.trim().ifBlank { "1433" }.toIntOrNull() ?: 1433
        if (instanceName == null) {
            try {
                java.net.Socket().use { s ->
                    s.connect(java.net.InetSocketAddress(serverHost, portNum), 6000)
                }
                steps += DiagStep(true, "درگاه اتصال (TCP $portNum)", "پورت باز است — سرور در حال شنیدن است")
            } catch (e: Throwable) {
                steps += DiagStep(
                    false, "درگاه اتصال (TCP $portNum)",
                    "اتصال به پورت ممکن نشد: " + shortErr(e),
                    "۱) در SQL Server Configuration Manager پروتکل TCP/IP را فعال و سرویس SQL را ری‌استارت کنید. ۲) فایروال ویندوز و مودم/روتر را برای این پورت باز کنید. ۳) اگر پورت دیگری است، در فیلد پورت همان را وارد کنید.",
                )
                return@withContext steps
            }
        } else {
            steps += DiagStep(
                true, "نمونه نام‌بریده «" + instanceName + "»",
                "درایور پورت واقعی را از سرویس SQL Browser (UDP 1434) می‌پرسد",
                "اگر این گام شکست خورد: در سرور سرویس SQL Server Browser را روشن کنید و UDP 1434 را در فایروال باز کنید؛ یا پورت مستقیم نمونه را پیدا کنید و فقط IP و پورت را (بدون بک‌اسلش) وارد کنید.",
            )
        }

        // گام ۳ تا ۵: اتصال کامل — رمزنگاری، ورود، دیتابیس
        try {
            var tlsNote = "دست‌دادن امن با سرور موفق بود"
            try {
                DriverManager.getConnection(url(true), props()).use { }
            } catch (e: Throwable) {
                if (isTlsError(e)) {
                    DriverManager.getConnection(url(false), props()).use { }
                    tlsNote = "سرور رمزنگاری TLS را نپذیرفت — اتصال بدون رمزنگاری برقرار شد"
                    steps += DiagStep(
                        true, "رمزنگاری اتصال (TLS)", tlsNote,
                        "بهتر است TLS 1.2 در سرور فعال شود؛ تا آن زمان برنامه خودش بدون رمزنگاری وصل می‌شود.",
                    )
                } else {
                    throw e
                }
            }
            if (steps.none { it.title.startsWith("رمزنگاری") }) {
                steps += DiagStep(true, "رمزنگاری اتصال (TLS)", tlsNote)
            }
            steps += DiagStep(true, "ورود با نام کاربری و رمز", "اعتبارنامه پذیرفته شد")
            steps += DiagStep(true, "باز کردن دیتابیس «" + cfg.database.trim() + "»", "دیتابیس در دسترس است")
            steps += DiagStep(true, "نتیجه نهایی", "اتصال کامل برقرار شد — می‌توانید وارد شوید")
        } catch (e: Throwable) {
            val m = (e.message ?: "") + " " + e.toString()
            when {
                m.contains("Login failed", true) -> {
                    steps += DiagStep(true, "رمزنگاری اتصال (TLS)", "دست‌دادن با سرور موفق بود")
                    steps += DiagStep(
                        false, "ورود با نام کاربری و رمز", "سرور ورود را رد کرد",
                        "نام کاربری و رمز SQL Server (نه ویندوز) را بررسی کنید؛ کاربر باید SQL Server Authentication باشد.",
                    )
                }
                m.contains("Cannot open database", true) -> {
                    steps += DiagStep(true, "رمزنگاری اتصال (TLS)", "دست‌دادن با سرور موفق بود")
                    steps += DiagStep(true, "ورود با نام کاربری و رمز", "اعتبارنامه پذیرفته شد")
                    steps += DiagStep(
                        false, "باز کردن دیتابیس «" + cfg.database.trim() + "»", "دیتابیس باز نشد",
                        "نام دیتابیس یا دسترسی این کاربر به دیتابیس را بررسی کنید.",
                    )
                }
                m.contains("timed out", true) || m.contains("Timeout", true) -> {
                    steps += DiagStep(
                        false, "مذاکره با سرور", "پاسخ سرور بیش از حد انتظار طول کشید",
                        "شبکه یا VPN کند است / سرور بار زیادی دارد؛ دوباره تلاش کنید.",
                    )
                }
                else -> {
                    steps += DiagStep(
                        false, "مذاکره با سرور", shortErr(e),
                        "اگر مطمئن نیستید این متن را برای پشتیبانی بفرستید.",
                    )
                }
            }
        }
        steps
    }

    private fun shortErr(e: Throwable): String =
        (e.message ?: e.toString()).replace("\n", " ").take(200)

    // ------------------------------------------------------------ ورود
    /** تست اتصال با نام کاربری/رمز — موفقیت = اعتبارنامه درست است. نسخه سرور را برمی‌گرداند. */
    suspend fun test(): String = withContext(Dispatchers.IO) {
        open().use { c ->
            c.createStatement().use { st ->
                st.executeQuery("SELECT @@VERSION").use { rs ->
                    if (rs.next()) (rs.getString(1) ?: "").substringBefore('\n').trim() else ""
                }
            }
        }
    }

    // ------------------------------------------------------------ نمای کلی
    /** نسخه سرور + حجم دیتابیس + لیست همه جداول با تعداد رکورد */
    suspend fun overview(): Overview = withContext(Dispatchers.IO) {
        open().use { c ->
            val version = try {
                c.createStatement().use { st ->
                    st.executeQuery("SELECT @@VERSION").use { rs ->
                        if (rs.next()) (rs.getString(1) ?: "").substringBefore('\n').trim() else ""
                    }
                }
            } catch (_: Throwable) {
                ""
            }

            val sizeKb: Long? = try {
                c.createStatement().use { st ->
                    st.executeQuery(
                        "SELECT SUM(CAST(size AS BIGINT)) * 8 FROM sys.master_files WHERE database_id = DB_ID()"
                    ).use { rs ->
                        if (rs.next()) rs.getLong(1) else null
                    }
                }
            } catch (_: Throwable) {
                null
            }

            val tables = c.createStatement().use { st ->
                st.executeQuery(
                    """
                    SELECT s.name AS sch, t.name AS tbl,
                           ISNULL(SUM(CAST(CASE WHEN p.index_id IN (0,1) THEN p.rows END AS BIGINT)), 0) AS cnt
                    FROM sys.tables t
                    JOIN sys.schemas s ON s.schema_id = t.schema_id
                    LEFT JOIN sys.partitions p ON p.object_id = t.object_id AND p.index_id IN (0,1)
                    GROUP BY s.name, t.name
                    ORDER BY t.name
                    """.trimIndent()
                ).use { rs ->
                    val list = ArrayList<TableInfo>()
                    while (rs.next()) {
                        list += TableInfo(
                            schema = rs.getString(1) ?: "dbo",
                            name = rs.getString(2) ?: "?",
                            rows = rs.getLong(3),
                        )
                    }
                    list
                }
            }

            Overview(
                version = version,
                sizeMb = sizeKb?.let { it / 1024.0 },
                tables = tables,
            )
        }
    }

    // ------------------------------------------------------------ ستون‌ها
    /** ستون‌های یک جدول از INFORMATION_SCHEMA (همزمان اعتبارسنجی وجود جدول) */
    private fun columnsOf(c: Connection, schema: String, table: String): List<ColumnInfo> {
        c.prepareStatement(
            "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION"
        ).use { ps ->
            ps.setString(1, schema)
            ps.setString(2, table)
            ps.executeQuery().use { rs ->
                val list = ArrayList<ColumnInfo>()
                while (rs.next()) {
                    list += ColumnInfo(rs.getString(1) ?: "?", rs.getString(2) ?: "")
                }
                if (list.isEmpty()) {
                    throw AtiranDbException("جدول «$table» یافت نشد یا ستونی ندارد")
                }
                return list
            }
        }
    }

    // ------------------------------------------------------------ داده جدول
    /**
     * یک صفحه از رکوردهای یک جدول.
     *  - صفحه‌بندی با ROW_NUMBER (سازگار با SQL Server 2005+)
     *  - جستجوی سراسری سرور-محور روی ستون‌های متنی (کوئری پارامتری، امن)
     */
    suspend fun page(
        schema: String,
        table: String,
        offset: Long,
        limit: Long,
        searchQuery: String,
    ): PageData = withContext(Dispatchers.IO) {
        open().use { c ->
            val cols = columnsOf(c, schema, table)
            val tableRef = "${q(schema)}.${q(table)}"

            val textCols = cols
                .filter { it.type.lowercase() in TEXT_TYPES }
                .take(12)
                .map { it.name }

            val hasSearch = searchQuery.isNotBlank() && textCols.isNotEmpty()
            val whereSql = if (hasSearch) {
                " WHERE " + textCols.joinToString(" OR ") { q(it) + " LIKE ?" }
            } else ""

            // تعداد کل (با همان فیلتر جستجو)
            val total = c.prepareStatement("SELECT COUNT_BIG(*) FROM $tableRef$whereSql").use { ps ->
                if (hasSearch) {
                    textCols.forEachIndexed { i, _ -> ps.setString(i + 1, "%$searchQuery%") }
                }
                ps.executeQuery().use { rs -> rs.next(); rs.getLong(1) }
            }

            val pageSql = """
                SELECT * FROM (
                    SELECT *, ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS [_rn]
                    FROM $tableRef$whereSql
                ) AS [_paged]
                WHERE [_paged].[_rn] > ? AND [_paged].[_rn] <= ?
                ORDER BY [_paged].[_rn]
            """.trimIndent()

            val rows = c.prepareStatement(pageSql).use { ps ->
                var i = 1
                if (hasSearch) {
                    textCols.forEach { _ -> ps.setString(i++, "%$searchQuery%") }
                }
                ps.setLong(i++, offset)
                ps.setLong(i, offset + limit)

                ps.executeQuery().use { rs ->
                    val md = rs.metaData
                    val n = md.columnCount // ستون آخر = _rn
                    val colNames = (1 until n).map { md.getColumnLabel(it) }
                    val out = ArrayList<List<String?>>()
                    while (rs.next()) {
                        val row = ArrayList<String?>(n - 1)
                        for (ci in 1 until n) {
                            val v: Any? = try {
                                rs.getObject(ci)
                            } catch (_: Throwable) {
                                try {
                                    rs.getString(ci)
                                } catch (_: Throwable) {
                                    null
                                }
                            }
                            row += when (v) {
                                null -> null
                                is ByteArray -> "(داده باینری ${v.size} بایت)"
                                else -> v.toString()
                            }
                        }
                        out += row
                    }
                    PageData(colNames, out, total)
                }
            }
            rows
        }
    }
}
