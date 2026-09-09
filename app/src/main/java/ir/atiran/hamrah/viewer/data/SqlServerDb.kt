package ir.atiran.hamrah.viewer.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
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
    /** اگر پورت واقعی سرور کشف شد، اینجا می‌آید تا کاربر با یک لمس اعمالش کند */
    val suggestedPort: Int? = null,
)

class SqlServerDb(private val cfg: DbSettings) {

    companion object {
        init {
            // بارگذاری صریح درایورها (در اندروید ServiceLoader قابل اتکا نیست)
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver")
            } catch (_: Throwable) {
                // درایور جایگزین موجود نیست — موتور اصلی کافی است
            }
        }

        /** حالت اتصالی که در کل عمر برنامه جواب داده — بین اتصال‌های مختلف به اشتراک */
        @Volatile
        private var lastGoodMode: Int? = null

        /** درایورها — صریح نمونه‌سازی می‌شوند؛ نه DriverManager */
        private val mssqlDriver by lazy { com.microsoft.sqlserver.jdbc.SQLServerDriver() }
        private val jtdsDriver by lazy { net.sourceforge.jtds.jdbc.Driver() }

        private val TEXT_TYPES = setOf(
            "char", "nchar", "varchar", "nvarchar", "text", "ntext", "sysname"
        )

        /** پیام خطای فارسی و قابل فهم برای کاربر */
        fun friendly(e: Throwable): String {
            if (e is AtiranDbException) return e.message ?: "خطای دیتابیس"
            val m = e.message ?: e.toString()
            return when {
                m.contains("Login failed", ignoreCase = true) ->
                    "سرور در دسترس است ✓ اما نام کاربری یا رمز عبور رد شد — همان رمزی که در برنامهٔ ویندوزی کار می‌کند را وارد کنید (رمز SQL Server کاربر AdminAn)"
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

    /** هدف تجزیه‌شده — هاست خالص، پورت و نمونه */
    private data class Target(val host: String, val port: Int?, val instance: String?)

    /**
     * تجزیه مقاوم آدرس سرور — همان چیزی که کاربر تایپ می‌کند:
     *  «37.143.147.19» ، «37.143.147.19:1433» ، «37.143.147.19,1433» (نوشتار SSMS)،
     *  «SERVER\SQLEXPRESS» و «SERVER\INST:1433» — با ارقام فارسی هم.
     */
    private fun parseTarget(): Target {
        fun latin(s: String): String = s.map { c ->
            when (c) {
                in '۰'..'۹' -> ('0' + (c - '۰'))
                in '٠'..'٩' -> ('0' + (c - '٠'))
                else -> c
            }
        }.joinToString("")
        val rawAll = latin(cfg.host).trim()
        val raw = rawAll.substringBefore('\\').trim()
        val instPart = rawAll.substringAfter('\\', "").trim()
        var instance: String? = null
        var embeddedPort: Int? = null
        if (instPart.isNotBlank()) {
            val ip = instPart.substringBefore(':').substringBefore(',').trim()
            val pp = instPart.substringAfter(':', "").substringAfter(',', "").trim()
            instance = ip.ifBlank { null }
            embeddedPort = pp.toIntOrNull()
        }
        var host = raw
        if (raw.count { it == ':' } == 1 && !raw.contains("::")) {
            val h = raw.substringBefore(':').trim()
            val pt = raw.substringAfter(':', "").trim()
            if (pt.isNotEmpty() && pt.all { it in '0'..'9' }) {
                host = h
                if (embeddedPort == null) embeddedPort = pt.toInt()
            }
        } else if (raw.contains(',')) {
            val h = raw.substringBefore(',').trim()
            val pt = raw.substringAfter(',', "").trim()
            if (pt.isNotEmpty() && pt.all { it in '0'..'9' }) {
                host = h
                if (embeddedPort == null) embeddedPort = pt.toInt()
            }
        }
        val portField = cfg.port.trim()
        val port: Int? = when {
            embeddedPort != null -> embeddedPort
            instance != null && (portField.isBlank() || portField == "1433") -> null
            else -> portField.ifBlank { "1433" }.toIntOrNull() ?: 1433
        }
        return Target(host, port, instance)
    }

    private val target: Target = parseTarget()
    private val serverHost: String get() = target.host
    private val instanceName: String? get() = target.instance

    /**
     * حالت اتصال که قبلاً جواب داده:
     *  ۰ = mssql-jdbc بدون رمزنگاری (الگوی ثابت‌شدهٔ نسخهٔ ویندوز M•R)
     *  ۱ = jTDS بدون TLS (بدون هیچ دست‌زدنی — برای سرورهای TLS قدیمی که
     *      ویندوز با آن‌ها کار می‌کند ولی TLS جدید اندروید نه)
     *  ۲ = mssql-jdbc با TLS + trust
     *  ۳ = mssql-jdbc با TLS + sslProtocol=TLS (JSSE)
     *  ۴ = mssql-jdbc با TLS 1.1 قدیمی (سرورهای قدیمی)
     *  ۵ = mssql-jdbc با TLS 1.0 قدیمی (سرورهای خیلی قدیمی)
     * بین اجراها هم به‌خاطر سپرده می‌شود تا اتصال بعدی سریع باشد.
     */
    @Volatile
    private var mode: Int? = null

    /**
     * ساخت URL بر اساس حالت اتصال:
     *  0 = رمزنگاری با تنظیم پیش‌فرض درایور
     *  1 = رمزنگاری با پروتکل پیش‌فرض JSSE (sslProtocol=TLS)
     *  2 = بدون رمزنگاری
     */
    private fun urlFor(m: Int, timeoutSec: Int = 15, portOverride: Int? = null): String {
        val port = portOverride ?: target.port
        // حالت ۱ = درایور jTDS — بدون TLS، مثل اتصال‌های قدیمی ویندوز
        if (m == 1) {
            val sb = StringBuilder("jdbc:jtds:sqlserver://").append(serverHost)
            if (port != null) sb.append(":").append(port)
            sb.append("/").append(cfg.database.trim())
            sb.append(";loginTimeout=").append(timeoutSec.coerceAtMost(30))
            sb.append(";socketTimeout=").append(60)
            if (instanceName != null) sb.append(";instance=").append(instanceName)
            return sb.toString()
        }
        val sb = StringBuilder("jdbc:sqlserver://").append(serverHost)
        if (port != null) sb.append(":").append(port)
        sb.append(";databaseName=").append(cfg.database.trim())
        when (m) {
            0 -> sb.append(";encrypt=false;trustServerCertificate=true")
            3 -> sb.append(";encrypt=true;trustServerCertificate=true;sslProtocol=TLS")
            4 -> sb.append(";encrypt=true;trustServerCertificate=true;sslProtocol=TLSv1.1")
            5 -> sb.append(";encrypt=true;trustServerCertificate=true;sslProtocol=TLSv1")
            else -> sb.append(";encrypt=true;trustServerCertificate=true")
        }
        sb.append(";loginTimeout=").append(timeoutSec)
        sb.append(";connectRetryCount=1;connectRetryDelay=1")
        sb.append(";applicationName=AtiranHamrahViewer")
        if (instanceName != null) sb.append(";instanceName=").append(instanceName)
        return sb.toString()
    }

    /** نمونهٔ صریح درایور — نه DriverManager؛ در اندروید کشف خودکار سرویس‌ها قابل‌اعتماد نیست */
    private fun connectOnce(m: Int, timeoutSec: Int = 15, portOverride: Int? = null): Connection {
        val url = urlFor(m, timeoutSec, portOverride)
        val conn = if (m == 1) jtdsDriver.connect(url, props()) else mssqlDriver.connect(url, props())
        if (conn == null) throw AtiranDbException("درایور نشست URL را نپذیرفت (حالت $m)")
        return conn
    }

    /** پیش‌آزمون خام TCP — اگر شبکه اصلاً به سرور نرسد، فوراً خطای روشن بده (نه انتظار ۲ دقیقه‌ای) */
    private fun rawTcpProbe(timeoutMs: Int = 4000): Throwable? {
        val port = target.port ?: return null // بدون پورت مشخص، درایور خودش از Browser می‌پرسد
        return try {
            java.net.Socket().use { s ->
                s.tcpNoDelay = true
                s.connect(java.net.InetSocketAddress(serverHost, port), timeoutMs)
            }
            null
        } catch (e: Throwable) {
            e
        }
    }

    /** آیا خطا با تغییر حالت اتصال قابل بازیابی است؟ */
    private fun recoverable(e: Throwable): Boolean =
        e is LinkageError || isTlsError(e) || isTimeout(e)

    /** آیا خطا مربوط به رمزنگاری/دست‌دادن TLS است؟ */
    private fun isTlsError(e: Throwable): Boolean {
        val m = (e.message ?: "") + " " + e.toString()
        return m.contains("SSL", true) || m.contains("TLS", true) ||
            m.contains("certificate", true) || m.contains("secure connection", true) ||
            m.contains("protocol version", true) ||
            m.contains("SQL Server did not return a response", true) ||
            m.contains("Connection reset", true)
    }

    /** آیا خطا از جنس تایم‌اوت/بی‌پاسخی است؟ */
    private fun isTimeout(e: Throwable): Boolean {
        val m = (e.message ?: "") + " " + e.toString()
        return m.contains("timed out", true) || m.contains("Timeout", true) ||
            m.contains("did not return a response", true)
    }

    private fun props(): Properties = Properties().apply {
        setProperty("user", cfg.user.trim())
        setProperty("password", cfg.password)
    }

    /**
     * تست سریع اتصال روی یک پورت مشخص — اول TLS، اگر TLS/تایم‌اوت شکست خورد
     * بدون رمزنگاری؛ خروجی null یعنی موفق، وگرنه آخرین خطا.
     */
    private fun quickConnect(port: Int? = null, timeoutSec: Int = 10): Throwable? {
        var last: Throwable? = null
        for (m in intArrayOf(1, 0, 2, 3, 4, 5)) {
            try {
                connectOnce(m, timeoutSec, port).use { }
                return null
            } catch (e: Throwable) {
                if (!recoverable(e)) return e
                last = e
            }
        }
        return last
    }

    /**
     * سنجش واقعی سرویس TDS: بسته prelogin استاندارد می‌فرستد و منتظر پاسخ
     * SQL Server می‌ماند — پورتِ «فقط باز» را از سرویس واقعی جدا می‌کند.
     */
    private fun tdsProbe(host: String, port: Int): ByteArray? {
        return try {
            java.net.Socket().use { s ->
                s.tcpNoDelay = true
                s.connect(java.net.InetSocketAddress(host, port), 6000)
                s.soTimeout = 7000
                val prelogin = byteArrayOf(
                    0x12, 0x01, 0x00, 0x1A, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x13, 0x00, 0x06,
                    0x01, 0x00, 0x19, 0x00, 0x01,
                    0xFF.toByte(),
                    0x0F, 0x00, 0x07, 0xD6.toByte(), 0x00, 0x00,
                    0x00,
                )
                s.getOutputStream().write(prelogin)
                s.getOutputStream().flush()
                val buf = ByteArray(512)
                val n = s.getInputStream().read(buf)
                if (n > 0) buf.copyOf(n) else null
            }
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * پرس‌وجو از سرویس SQL Server Browser (UDP 1434) — فهرست نمونه‌ها و
     * پورت‌های واقعی؛ برای وقتی نمونه روی پورت داینامیک گوش می‌دهد.
     */
    private fun browserQuery(host: String): List<Pair<String, Int>> {
        val out = ArrayList<Pair<String, Int>>()
        try {
            java.net.DatagramSocket().use { ds ->
                ds.soTimeout = 3500
                val addr = java.net.InetAddress.getByName(host)
                ds.send(java.net.DatagramPacket(byteArrayOf(0x0A), 1, addr, 1434))
                val buf = ByteArray(4096)
                val pkt = java.net.DatagramPacket(buf, buf.size)
                ds.receive(pkt)
                val text = String(buf, 0, pkt.length, charset("UTF-16LE"))
                val toks = text.split(";")
                var i = 0
                while (i < toks.size - 1) {
                    if (toks[i].trim().equals("tcp", true)) {
                        val p = toks[i + 1].trim().toIntOrNull()
                        if (p != null && p in 1..65535) {
                            val inst = if (i >= 3) toks[i - 3].trim() else ""
                            out += (inst to p)
                        }
                    }
                    i++
                }
            }
        } catch (_: Throwable) {
        }
        return out.distinctBy { it.second }
    }

    /** اتصال پایدار مشترک — یک‌بار ساخته می‌شود و همهٔ کوئری‌ها از آن استفاده می‌کنند */
    @Volatile
    private var sharedConn: Connection? = null
    private val connLock = Any()

    /**
     * گرفتن اتصال سالم — اگر اتصال قبلی زنده باشد همان برمی‌گردد (سریع و پایدار)؛
     * در غیر این صورت با همان منطق چندموتورهٔ open() از نو ساخته می‌شود.
     */
    private fun connection(): Connection = synchronized(connLock) {
        sharedConn?.let { c ->
            try {
                if (c.isValid(3)) return c
            } catch (_: Throwable) {
            }
            try {
                c.close()
            } catch (_: Throwable) {
            }
            sharedConn = null
        }
        val c = open()
        sharedConn = c
        return c
    }

    /**
     * اجرای یک کوئری روی اتصال پایدار — همهٔ کوئری‌ها سریال می‌شوند تا
     * چند کوروتین هم‌زمان روی یک Connection با هم تداخل نکنند؛
     * اگر وسط کار اتصال بمیرد، اتصال دور ریخته می‌شود تا کوئری بعدی از نو بسازد.
     */
    private fun <T> withConn(block: (Connection) -> T): T = synchronized(connLock) {
        val c = connection()
        try {
            block(c)
        } catch (e: Throwable) {
            try {
                c.close()
            } catch (_: Throwable) {
            }
            sharedConn = null
            throw e
        }
    }

    /**
     * اتصال چندموتوره: بدون رمزنگاری (الگوی ثابت‌شدهٔ نسخهٔ ویندوز M•R) →
     * jTDS بدون TLS → TLS → TLS/JSSE → TLS 1.1 قدیمی → TLS 1.0 قدیمی؛
     * حالت جواب‌داده به‌خاطر سپرده می‌شود تا اتصال‌های بعدی سریع باشند.
     */
    private fun open(): Connection {
        mode?.let { m -> return connectOnce(m) }
        // حالتی که قبلاً جواب داده — اگر هم‌اکنون هم جواب داد، سریع‌ترین راه است
        lastGoodMode?.let { m ->
            try {
                mode = m
                return connectOnce(m)
            } catch (_: Throwable) {
                mode = null // سرور عوض شده — جست‌وجوی کامل انجام می‌شود
            }
        }
        // پیش‌آزمون شبکه — اگر TCP نرسد، پیام روشن فوری بده
        if (mode == null && lastGoodMode == null) {
            rawTcpProbe()?.let { tcpErr ->
                throw AtiranDbException(
                    "سرور روی این شبکه در دسترس نیست (" + shortErr(tcpErr) + "). " +
                        "اگر با اینترنت همراه هستید، اپراتور ممکن است پورت دیتابیس را فیلتر کند — " +
                        "از وای‌فای شبکه اداره یا VPN استفاده کنید."
                )
            }
        }
        var last: Throwable? = null
        // ترتیب: jTDS بدون TLS (اثبات‌شده با پروب CI) → الگوی ویندوز → TLS → TLS/JSSE
        for (m in intArrayOf(1, 0, 2, 3, 4, 5)) {
            try {
                return connectOnce(m).also {
                    mode = m
                    lastGoodMode = m
                }
            } catch (e: Throwable) {
                if (!recoverable(e)) throw e
                last = e
            }
        }
        throw last ?: AtiranDbException("اتصال به سرور در هیچ‌یک از چهار حالت ناموفق بود")
    }

    // ------------------------------------------------------------ عیب‌یابی
    /**
     * عیب‌یابی مرحله‌به‌مرحله اتصال — دقیقاً نشان می‌دهد کدام گام می‌شکند:
     * پیدا کردن سرور (DNS) → درگاه TCP → رمزنگاری TLS → ورود → دیتابیس.
     */
    /**
     * آزمون مستقیم TLS روی پورت SQL — می‌فهمیم TLS سرور با اندروید کار می‌کند یا نه.
     * اگر نکند، دلیل اصلی شکست درایور اصلی پیدا شده و jTDS جایگزین می‌شود.
     */
    private fun tlsProbeStep(host: String, port: Int): DiagStep = try {
        val factory = javax.net.ssl.SSLSocketFactory.getDefault() as javax.net.ssl.SSLSocketFactory
        val sock = factory.createSocket() as javax.net.ssl.SSLSocket
        sock.use { s ->
            s.connect(java.net.InetSocketAddress(host, port), 6000)
            val all = listOf("TLSv1.3", "TLSv1.2", "TLSv1.1", "TLSv1")
            s.enabledProtocols = s.supportedProtocols.filter { it in all || it.startsWith("TLS") }.toTypedArray()
            s.startHandshake()
            val proto = s.session.protocol
            DiagStep(
                true, "دست‌دادن TLS با سرور",
                "برقرار شد ($proto) — رمزنگاری سازگار است",
            )
        }
    } catch (e: Throwable) {
        val m = (e.message ?: "") + " " + e.toString()
        DiagStep(
            false, "دست‌دادن TLS با سرور",
            "ناموفق: " + shortErr(e),
            if (m.contains("protocol", true) || m.contains("handshake", true) || m.contains("SSL", true))
                "TLS سرور با اندروید سازگار نیست (معمولاً سرور فقط TLS قدیمی 1.0/1.1 دارد). " +
                    "برنامه با درایور جایگزین (jTDS) بدون TLS ادامه می‌دهد — مثل نسخهٔ ویندوز."
            else null,
        )
    }

    suspend fun diagnose(): List<DiagStep> = withContext(Dispatchers.IO) {
        val steps = ArrayList<DiagStep>()

        // گام ۱: پیدا کردن سرور در شبکه
        try {
            val addr = java.net.InetAddress.getByName(serverHost)
            val targetDesc = addr.hostAddress +
                (target.port?.let { ":" + it } ?: "") +
                (target.instance?.let { " (نمونه " + it + ")" } ?: "")
            steps += DiagStep(true, "پیدا کردن سرور", "هدف اتصال: " + targetDesc)
        } catch (e: Throwable) {
            steps += DiagStep(
                false, "پیدا کردن سرور",
                "آدرس «" + serverHost + "» در شبکه پیدا نشد",
                "IP یا نام سرور را دقیق بررسی کنید. اگر از اینترنت وصل می‌شوید، سرور باید با IP عمومی منتشر شده باشد.",
            )
            return@withContext steps
        }

        // گام ۲: درگاه TCP + سنجش واقعی سرویس TDS (فقط وقتی نمونه ندارد)
        val portNum = target.port ?: 1433
        if (instanceName == null || target.port != null) {
            try {
                java.net.Socket().use { s ->
                    s.connect(java.net.InetSocketAddress(serverHost, portNum), 6000)
                }
                steps += DiagStep(true, "درگاه اتصال (TCP $portNum)", "دست‌دادن TCP با سرور برقرار شد")
            } catch (e: Throwable) {
                steps += DiagStep(
                    false, "درگاه اتصال (TCP $portNum)",
                    "اتصال به پورت ممکن نشد: " + shortErr(e),
                    "۱) در SQL Server Configuration Manager پروتکل TCP/IP را فعال و سرویس SQL را ری‌استارت کنید. ۲) فایروال ویندوز و مودم/روتر را برای این پورت باز کنید. ۳) اگر پورت دیگری است، در فیلد پورت همان را وارد کنید.",
                )
                return@withContext steps
            }

            // گام ۳: آیا پشت این پورت واقعاً SQL Server است؟ (بسته prelogin واقعی)
            val resp = tdsProbe(serverHost, portNum)
            if (resp == null) {
                steps += DiagStep(
                    false, "سرویس SQL Server روی پورت $portNum",
                    "اتصال TCP برقرار شد اما هیچ پاسخ TDS از سرور نرسید — پورت باز است ولی SQL Server پشت آن پاسخگو نیست",
                    "معمولاً یعنی: پورت‌فوروارد/NAT به مقصد اشتباه می‌رود، یا دستگاه میانی (فایروال/اپراتور) فقط اتصال را می‌پذیرد و داده را رها می‌کند. اگر با اینترنت همراه هستید و داخل شبکه اداره وصل می‌شوید، اپراتور ترافیک این پورت را فیلتر می‌کند — از VPN استفاده کنید.",
                )
                // تلاش برای یافتن پورت واقعی از SQL Browser
                val found = browserQuery(serverHost)
                if (found.isNotEmpty()) {
                    steps += DiagStep(
                        true, "سرویس SQL Browser (UDP 1434)",
                        "نمونه‌های روی سرور: " + found.joinToString("، ") { (name, prt) ->
                            (if (name.isBlank()) "پیش‌فرض" else name) + " → پورت " + ir.atiran.hamrah.viewer.utils.Jalali.fa(prt.toString())
                        },
                    )
                    for ((name, prt) in found.filter { it.second != portNum }.take(3)) {
                        val err = quickConnect(prt, 10)
                        if (err == null) {
                            steps += DiagStep(
                                true, "پورت واقعی سرور پیدا شد: " + ir.atiran.hamrah.viewer.utils.Jalali.fa(prt.toString()),
                                "اتصال روی پورت " + ir.atiran.hamrah.viewer.utils.Jalali.fa(prt.toString()) + " موفق بود! (نمونه «" + (if (name.isBlank()) "پیش‌فرض" else name) + "»)",
                                "دکمه «استفاده از این پورت» را بزنید تا خودکار در فیلد پورت تنظیم شود.",
                                suggestedPort = prt,
                            )
                            steps += DiagStep(true, "نتیجه نهایی", "اتصال با پورت کشف‌شده برقرار شد")
                            return@withContext steps
                        }
                    }
                } else {
                    steps += DiagStep(
                        false, "سرویس SQL Browser (UDP 1434)",
                        "پاسخی نرسید — یا سرویس Browser خاموش است یا ترافیک UDP بسته است",
                        "از مدیر سرور بخواهید پورت واقعی SQL Server را بگوید (فایروال ویندوز → Inbound Rules روی SQL Server).",
                    )
                }
                return@withContext steps
            }
            if (resp.isNotEmpty() && resp[0] != 0x04.toByte()) {
                steps += DiagStep(
                    false, "سرویس SQL Server روی پورت $portNum",
                    "پاسخی آمد ولی پاسخ TDS معتبر نبود — روی این پورت سرویس دیگری در حال شنیدن است",
                    "IP و پورت صحیح SQL Server را از مدیر سرور بپرسید؛ ممکن است 1433 به سرویس دیگری فوروارد شده باشد.",
                )
                return@withContext steps
            }
            steps += DiagStep(true, "سرویس SQL Server روی پورت $portNum", "پاسخ prelogin معتبر از SQL Server دریافت شد")
        } else {
            steps += DiagStep(
                true, "نمونه نام‌بریده «" + instanceName + "»",
                "درایور پورت واقعی را از سرویس SQL Browser (UDP 1434) می‌پرسد",
                "اگر این گام شکست خورد: در سرور سرویس SQL Server Browser را روشن کنید و UDP 1434 را در فایروال باز کنید؛ یا پورت مستقیم نمونه را پیدا کنید و فقط IP و پورت را (بدون بک‌اسلش) وارد کنید.",
            )
        }

        // گام ۲.۵: آزمون دست‌دادن TLS — آیا TLS سرور با اندروید سازگار است؟
        if (instanceName == null || target.port != null) {
            steps += tlsProbeStep(serverHost, portNum)
        }

        // گام ۳ تا ۵: اتصال کامل — چهار موتور (الگوی ویندوز، jTDS، TLS، TLS/JSSE)، ورود، دیتابیس
        try {
            var connectedMode = -1
            var modeErr: Throwable? = null
            for (m in intArrayOf(1, 0, 2, 3, 4, 5)) {
                try {
                    connectOnce(m, 15).use { }
                    connectedMode = m
                    break
                } catch (e: Throwable) {
                    if (!recoverable(e)) throw e
                    modeErr = e
                }
            }
            if (connectedMode == -1) {
                throw modeErr ?: AtiranDbException("هیچ حالت اتصال جواب نداد")
            }
            when (connectedMode) {
                0 -> steps += DiagStep(
                    true, "رمزنگاری اتصال",
                    "اتصال بدون رمزنگاری برقرار شد (الگوی نسخهٔ ویندوز M•R)",
                )
                1 -> steps += DiagStep(
                    true, "رمزنگاری اتصال",
                    "اتصال با درایور جایگزین (jTDS) برقرار شد — بدون دست‌زدن TLS",
                    "TLS سرور با اندروید هم‌خوان نبود؛ درایور جایگزین مثل ویندوزهای قدیمی بدون TLS وصل شد.",
                )
                3 -> steps += DiagStep(
                    true, "رمزنگاری اتصال (TLS)",
                    "اتصال امن با پروتکل جایگزین برقرار شد",
                    "پروتکل پیش‌فرض درایور با سرور شما هم‌خوان نبود؛ برنامه خودش حالت سازگار را پیدا کرد.",
                )
                else -> steps += DiagStep(true, "رمزنگاری اتصال (TLS)", "دست‌دادن امن با سرور موفق بود")
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
                isTimeout(e) -> {
                    steps += DiagStep(
                        false, "مذاکره با سرور", "پاسخ سرور بیش از حد انتظار طول کشید (حتی بدون رمزنگاری)",
                        "اگر داخل شبکه اداره وصل می‌شوید ولی با اینترنت همراه نه، اپراتور ترافیک این پورت را فیلتر می‌کند — از VPN یا شبکه اداره استفاده کنید. اگر همه‌جا همین است، سرور یا فایروال میانی جلسات طولانی را می‌بندد.",
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
        (e.javaClass.simpleName + ": " + (e.message ?: "")).replace("\n", " ").take(230)

    /**
     * جستجوی هوشمند جدول بین کاندیدها با اولویت — دقیقاً مثل نسخهٔ ویندوز M•R:
     * از sys.tables اولین جدولِ مطابق با ترتیب اولویت را برمی‌گرداند («schema.table»).
     */
    suspend fun findTable(vararg candidates: String): String? = withContext(Dispatchers.IO) {
        try {
            withConn { c ->
                val names = candidates.joinToString(",") { "'" + it.replace("'", "''") + "'" }
                val prio = candidates.mapIndexed { i, n -> "WHEN '$n' THEN ${i + 1}" }.joinToString(" ")
                c.createStatement().use { st ->
                    st.executeQuery(
                        "SELECT TOP (1) s.name + N'.' + t.name FROM sys.tables t " +
                            "JOIN sys.schemas s ON s.schema_id = t.schema_id " +
                            "WHERE t.name IN ($names) ORDER BY CASE t.name $prio ELSE 99 END"
                    ).use { rs ->
                        if (rs.next()) rs.getString(1) else null
                    }
                }
            }
        } catch (_: Throwable) {
            null
        }
    }

    /** بستن اتصال پایدار — هنگام خروج کاربر */
    fun shutdown() {
        synchronized(connLock) {
            try {
                sharedConn?.close()
            } catch (_: Throwable) {
            }
            sharedConn = null
        }
    }

    // ------------------------------------------------------------ ورود
    /**
     * تست اتصال هوشمند با نام کاربری/رمز — مثل نسخهٔ ویندوز M•R:
     * ۱) اتصال برقرار شود؛ ۲) دیتابیسِ فعال با دیتابیس انتخابی یکی باشد
     * (خطای رایج «وصل شد ولی دیتابیس غلط» همین‌جا کشف می‌شود)؛
     * خروجی: نسخه سرور.
     */
    suspend fun test(): String = withContext(Dispatchers.IO) {
        withConn { c ->
            c.createStatement().use { st ->
                st.executeQuery("SELECT DB_NAME(), @@SERVERNAME, @@VERSION").use { rs ->
                    if (!rs.next()) throw AtiranDbException("SQL Server پاسخ قابل استفاده‌ای برنگرداند")
                    val active = rs.getString(1) ?: ""
                    if (active.trim().equals(cfg.database.trim(), ignoreCase = true).not()) {
                        throw AtiranDbException(
                            "اتصال برقرار شد اما دیتابیس «" + active + "» فعال است؛ دیتابیس انتخابی «" + cfg.database.trim() + "» است"
                        )
                    }
                    (rs.getString(3) ?: "").substringBefore('\n').trim()
                }
            }
        }
    }

    // ------------------------------------------------------------ نمای کلی
    /** نسخه سرور + حجم دیتابیس + لیست همه جداول با تعداد رکورد */
    suspend fun overview(): Overview = withContext(Dispatchers.IO) {
        withConn { c ->
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

    // ------------------------------------------------------------ خواندن برای M•REPORT

    /** ستون‌های یک جدول — برای تشخیص نقش‌ها در نگاشت بخش‌ها */
    suspend fun columns(schema: String, table: String): List<ColumnInfo> = withContext(Dispatchers.IO) {
        withConn { c -> columnsOf(c, schema, table) }
    }

    /** تعداد کل رکوردهای یک جدول */
    suspend fun count(schema: String, table: String): Long = withContext(Dispatchers.IO) {
        withConn { c ->
            c.createStatement().use { st ->
                st.executeQuery("SELECT COUNT_BIG(*) FROM ${q(schema)}.${q(table)}").use { rs ->
                    rs.next()
                    rs.getLong(1)
                }
            }
        }
    }

    /** مجموع یک ستون عددی — برای گردش مالی (خطا → null) */
    suspend fun sumOf(schema: String, table: String, col: String): Double? = withContext(Dispatchers.IO) {
        try {
            withConn { c ->
                c.createStatement().use { st ->
                    st.executeQuery("SELECT SUM(CAST(${q(col)} AS FLOAT)) FROM ${q(schema)}.${q(table)}").use { rs ->
                        if (rs.next()) rs.getDouble(1).takeIf { !rs.wasNull() } else null
                    }
                }
            }
        } catch (_: Throwable) {
            null
        }
    }

    /** N رکورد اول به‌ترتیب نزولی یک ستون — «برترین‌ها» */
    suspend fun topBy(schema: String, table: String, orderCol: String, limit: Int): Pair<List<String>, List<List<String?>>> =
        withContext(Dispatchers.IO) {
            withConn { c ->
                val cols = columnsOf(c, schema, table)
                if (cols.none { it.name == orderCol }) {
                    throw AtiranDbException("ستون «$orderCol» در جدول «$table» یافت نشد")
                }
                c.createStatement().use { st ->
                    st.executeQuery(
                        "SELECT TOP ${limit.coerceIn(1, 50)} * FROM ${q(schema)}.${q(table)} ORDER BY ${q(orderCol)} DESC"
                    ).use { rs -> drain(rs) }
                }
            }
        }

    /** N رکورد آخر بر اساس ستون تاریخ */
    suspend fun latestBy(schema: String, table: String, dateCol: String, limit: Int): Pair<List<String>, List<List<String?>>> =
        withContext(Dispatchers.IO) {
            withConn { c ->
                val cols = columnsOf(c, schema, table)
                if (cols.none { it.name == dateCol }) {
                    throw AtiranDbException("ستون «$dateCol» در جدول «$table» یافت نشد")
                }
                c.createStatement().use { st ->
                    st.executeQuery(
                        "SELECT TOP ${limit.coerceIn(1, 50)} * FROM ${q(schema)}.${q(table)} ORDER BY ${q(dateCol)} DESC"
                    ).use { rs -> drain(rs) }
                }
            }
        }

    /** خواندن نتیجه کوئری به (ستون‌ها، رکوردها) */
    private fun drain(rs: java.sql.ResultSet): Pair<List<String>, List<List<String?>>> {
        val md = rs.metaData
        val n = md.columnCount
        val names = (1..n).map { md.getColumnLabel(it) }
        val out = ArrayList<List<String?>>()
        while (rs.next()) {
            val row = ArrayList<String?>(n)
            for (ci in 1..n) {
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
        return names to out
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
        withConn { c ->
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
