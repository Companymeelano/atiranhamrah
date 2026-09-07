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
                m.contains("SSL", ignoreCase = true) || m.contains("certificate", ignoreCase = true) ->
                    "خطای رمزنگاری در اتصال به سرور"
                else -> m.take(300)
            }
        }

        /** escape یک شناسه داخل براکت */
        private fun q(ident: String): String = "[" + ident.replace("]", "]]") + "]"
    }

    private fun url(): String =
        "jdbc:sqlserver://${cfg.host.trim()}:${cfg.port.trim()};" +
            "databaseName=${cfg.database.trim()};" +
            "encrypt=true;trustServerCertificate=true;" +
            "loginTimeout=15;applicationName=AtiranHamrahViewer"

    private fun open(): Connection {
        val props = Properties()
        props.setProperty("user", cfg.user.trim())
        props.setProperty("password", cfg.password)
        return DriverManager.getConnection(url(), props)
    }

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
            } catch (_: Exception) {
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
            } catch (_: Exception) {
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
                    val out = ArrayList<List<String?>()
                    while (rs.next()) {
                        val row = ArrayList<String?>(n - 1)
                        for (ci in 1 until n) {
                            val v: Any? = try {
                                rs.getObject(ci)
                            } catch (_: Exception) {
                                try {
                                    rs.getString(ci)
                                } catch (_: Exception) {
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
