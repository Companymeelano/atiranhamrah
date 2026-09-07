package ir.atiran.hamrah.viewer.data

/** تنظیمات اتصال مستقیم به SQL Server */
data class DbSettings(
    val host: String = DEFAULT_HOST,
    val port: String = "1433",
    val database: String = DEFAULT_DB,
    val user: String = DEFAULT_USER,
    val password: String = "",
    val remember: Boolean = true,
) {
    companion object {
        /** مقادیر پیش‌فرض سازمانی — قابل ویرایش در صفحه ورود */
        const val DEFAULT_HOST = "37.143.147.19"
        const val DEFAULT_DB = "Atiran2"
        const val DEFAULT_USER = "AdminAn"
    }
}

/** یک جدول دیتابیس به همراه تعداد رکورد آن */
data class TableInfo(val schema: String, val name: String, val rows: Long)

/** یک ستون جدول */
data class ColumnInfo(val name: String, val type: String)

/** نمای کلی دیتابیس بعد از اتصال */
data class Overview(
    val version: String,
    val sizeMb: Double?,
    val tables: List<TableInfo>,
) {
    val totalRows: Long get() = tables.sumOf { it.rows }
}

/** یک صفحه داده از یک جدول */
data class PageData(
    val columns: List<String>,
    val rows: List<List<String?>>,
    val total: Long,
)
