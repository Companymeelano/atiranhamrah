import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * پروب واقعی سرور SQL — از رانر GitHub Actions اجرا می‌شود (اینترنت کامل).
 * هدف: کشف دقیق علت شکست اتصال اندروید — TCP/TLS/درایور/احراز هویت.
 * نتیجه‌ها به‌صورت annotation چاپ می‌شوند تا از API خوانده شوند.
 */
public class ServerProbe {
    public static void main(String[] args) throws Exception {
        String host = "37.143.147.19";
        int port = 1433;
        String db = "Atiran2";
        String user = args.length > 0 && !args[0].isBlank() ? args[0] : "AdminAn";
        String pass = args.length > 1 ? args[1] : "";

        System.out.println("::notice::PROBE host=" + host + " port=" + port + " db=" + db + " user=" + user + " pass=" + (pass.isEmpty() ? "(empty)" : "(set)"));

        // ۱) TCP خام
        try {
            java.net.Socket s = new java.net.Socket();
            s.connect(new java.net.InetSocketAddress(host, port), 8000);
            s.close();
            System.out.println("::notice::TCP " + host + ":" + port + " OPEN از اینترنت");
        } catch (Throwable e) {
            System.out.println("::error::TCP FAILED: " + e.getClass().getSimpleName() + ": " + clean(e) + " — سرور از اینترنت روی 1433 در دسترس نیست (پورت‌فوروارد/فایروال)؛ گوشی هم هرگز از اینترنت همراه نمی‌رسد");
            return;
        }

        // ۲) پروتکل TDS — پاسخ prelogin
        try {
            java.net.Socket s = new java.net.Socket();
            s.connect(new java.net.InetSocketAddress(host, port), 8000);
            s.setSoTimeout(7000);
            s.getOutputStream().write(new byte[]{
                0x12, 0x01, 0x00, 0x1A, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x13, 0x00, 0x06,
                0x01, 0x00, 0x19, 0x00, 0x01,
                (byte) 0xFF,
                0x0F, 0x00, 0x07, (byte) 0xD6, 0x00, 0x00,
                0x00,
            });
            s.getOutputStream().flush();
            byte[] buf = new byte[512];
            int n = s.getInputStream().read(buf);
            if (n > 0 && buf[0] == 0x04) {
                System.out.println("::notice::TDS PRELOGIN OK — SQL Server واقعی پاسخ داد (" + n + " بایت)");
            } else if (n > 0) {
                System.out.println("::error::TDS INVALID: بایت اول پاسخ=" + String.format("0x%02X", buf[0]) + " — سرویس دیگری روی این پورت است");
            } else {
                System.out.println("::error::TDS NO-RESPONSE: TCP وصل شد ولی SQL پاسخ نداد (NAT/فایروال میانی)");
            }
            s.close();
        } catch (Throwable e) {
            System.out.println("::error::TDS PROBE FAILED: " + clean(e));
        }

        // ۳) تلاش ورود با همهٔ حالت‌ها — دقیقاً همان موتورهای اپ اندروید
        String ms = "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + db +
            ";loginTimeout=12;user=" + user + ";password=" + pass;
        String[][] attempts = {
            {"1-mssql-encrypt=false", ms + ";encrypt=false;trustServerCertificate=true"},
            {"2-mssql-TLS", ms + ";encrypt=true;trustServerCertificate=true"},
            {"3-mssql-TLSv1.1", ms + ";encrypt=true;trustServerCertificate=true;sslProtocol=TLSv1.1"},
            {"4-mssql-TLSv1", ms + ";encrypt=true;trustServerCertificate=true;sslProtocol=TLSv1"},
            {"5-jtds-plain", "jdbc:jtds:sqlserver://" + host + ":" + port + "/" + db + ";user=" + user + ";password=" + pass + ";loginTimeout=12"},
        };
        for (String[] a : attempts) {
            try (Connection c = DriverManager.getConnection(a[1])) {
                System.out.println("::notice::LOGIN OK در حالت " + a[0] + " — اتصال کامل برقرار شد!");
                try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT DB_NAME(), @@SERVERNAME")) {
                    if (rs.next()) System.out.println("::notice::DB=" + rs.getString(1) + " SERVER=" + rs.getString(2));
                }
                return;
            } catch (Throwable e) {
                System.out.println("::error::MODE " + a[0] + " FAILED: " + e.getClass().getName() + ": " + clean(e));
            }
        }
        System.out.println("::error::ALL-MODES-FAILED — پیام‌های بالا علت دقیق را نشان می‌دهد (Login failed = رمز/کاربر؛ trusted = فقط ویندوز-احراز؛ TLS/timeout = شبکه)");
    }

    private static String clean(Throwable e) {
        String m = e.getMessage();
        if (m == null) m = e.toString();
        m = m.replace('\n', ' ').replace('\r', ' ');
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) root = root.getCause();
        if (root != e && root.getMessage() != null) m += " | root: " + root.getMessage().replace('\n', ' ');
        return m.length() > 400 ? m.substring(0, 400) : m;
    }
}
