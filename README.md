# آتیران همراه (نمایشگر دیتابیس) — نسخه اندروید

اپلیکیشن اندرویدی **فقط-خواندنی** برای اتصال به سرویس «آتیران همراه»
(`AtiranLocalServices.svc` — WCF با `webHttpBinding` و JSON) و مشاهده داده‌های
پایگاه داده: مشتریان، کالاها و قیمت‌ها، موجودی انبارها، فاکتورها، چک‌ها، پیام‌ها و
گزارش‌ها. ثبت سفارش فروش/ویزیت در این نسخه وجود ندارد.

- زبان/UI: **Kotlin + Jetpack Compose** (Material 3، راست‌به‌چپ)
- HTTP: **OkHttp** + **kotlinx-serialization**
- ذخیره تنظیمات: **DataStore Preferences**
- حداقل اندروید: **API 24 (Android 7)**، target/compile SDK 35
- نسخه فعلی: **1.1.0** (versionCode 2)

## ساخت پروژه

```bash
cd AtiranHamrahViewer
gradle assembleDebug        # یا از Android Studio: Run ▶
```

خروجی: `app/build/outputs/apk/debug/app-debug.apk`

نیازمندی‌ها: JDK 17، Android SDK 35، Gradle 8.7+ (با AGP 8.6.1).

توجه: Gradle Wrapper در این بسته گنجانده نشده است؛ اگر Gradle نصب‌شده ندارید،
پروژه را در Android Studio باز کنید (ساختار Gradle را خودکار تشخیص می‌دهد) یا با
`gradle wrapper --gradle-version 8.9` Wrapper بسازید.

## راه‌اندازی روی سرور

پوشه منتشرشده سرویس (`LocalServices.svc` + `bin/`) باید روی وب‌سرور IIS
قابل دسترس باشد و آدرس آن (مثلاً `http://192.168.1.10/LocalServices.svc`)
در صفحه تنظیمات اپ وارد شود. چون روی `webHttpBinding` است، همه عملیات
با `POST` و بدنه JSON انجام می‌شوند؛ برای شبکه‌های داخلی HTTP ساده،
در `AndroidManifest.xml` مقدار `android:usesCleartextTraffic="true"` فعال است.

## فعال‌سازی دستگاه (CPUID)

سرویس، هر درخواست را با `SecurityToken {CPUID, Key}` اعتبارسنجی می‌کند:

- `Key = (YYYYMMDDHHMMSS) × 3593` (نمایش `long`)
- اختلاف زمان ورودی تا زمان سرور باید ≤ ۲۰۰ دقیقه باشد
- سپس رکورد دستگاه در جدول `Devices` بر اساس `CPUID` بررسی می‌شود
  (در غیر این صورت Status=2/3/4 برمی‌گردد)

این اپ کلید را **برای هر درخواست به‌صورت خودکار** از ساعت دستگاه تولید می‌کند
(`AtiranClient.freshToken()`). بنابراین:

1. ساعت دستگاه/تبلت باید با سرور هماهنگ باشد.
2. برای هر CPUID جدید، ابتدا باید تبلت در نسخه ویندوزی «آتیران همراه»
   فعال (Activate) شود؛ CPUID را از برنامه ویندوزی یا
   `GetDeviceInfo` دریافت کنید و در تنظیمات اپ وارد کنید.
3. اگر سرویس روی لوکال‌هوست/شبکه با آدرس IP تغییر کرده، فقط آدرس را عوض کنید.

## صفحه‌ها

| بخش | سرویس‌های استفاده‌شده |
|---|---|
| تنظیمات + تست اتصال | `CompanyInfo`, `Login`, `GetCustomerByLogin` |
| ورود | `Login`, `GetCustomerByLogin` |
| مشتریان | `Customers`, (فیلتر محلی) |
| کالاها و قیمت‌ها | `Kalas`, `ForoshPrices` |
| موجودی انبارها | `KalaAnbs`, `InventoryAnbars` |
| فاکتورها | `CustomerFactors`, `NotPaidFactors` |
| چک‌ها | `Checks`, `CustomerChecks` |
| پیام‌ها | `VisitorMessages`, `GetUnreadMessages` |
| گزارش‌ها | `CompanyInfo`, `CountKa`, `CountMo`, `MaxShMo`, `GetPeriods`, `CustGroups`, `KaGroups` |

همه این مسیرها و قالب‌های بدنه (Bare/Wrapped) دقیقاً از متادیتای
`AtiranLocalServices.dll` استخراج شده‌اند.

## ساختار کد

```
app/src/main/java/ir/atiran/hamrah/viewer/
├── MainActivity.kt
├── data/
│   ├── Models.kt           # DTOهای سرویس (kotlinx-serialization)
│   ├── AtiranClient.kt     # OkHttp + JSON + ساخت کلید امنیتی
│   ├── AtiranRepository.kt # متدهای تایپ‌شده هر endpoint
│   └── SettingsStore.kt    # DataStore
└── ui/
    ├── AppViewModel.kt     # وضعیت ناوبری (Section/Screen)
    ├── AtiranApp.kt        # مسیریابی صفحه‌ها + دکمه برگشت
    ├── theme/Theme.kt
    ├── components/Common.kt
    ├── screens/            # تنظیمات، ورود، خانه (منو)
    └── screens/browse/     # لیست‌ها + جستجو + جزئیات
```

## نکته‌ها

- پاسخ همه endpoint ها به شکل `AtiranResult {Status, Type, Result}` است؛
  `Result` یک **رشته JSON** است و اپ آن را باز می‌کند (`Status==1` یعنی موفق).
- بسیاری از endpoint ها صفحه‌بندی دارند (`startIndex/fetchNo`)؛ لیست‌ها
  به‌صورت اسکرول پیوسته صفحات بعدی را می‌گیرند. سرویس‌هایی که نتیجه را
  یک‌جا برمی‌گردانند (فاکتور/چک یک مشتری، پیام‌های خوانده‌نشده) با
  `paged=false` علامت خورده‌اند تا واکشی تکراری انجام نشود.
- برای جستجوی فیلترشده سمت سرور (`FilteredCustomers`, `FilteredKalas` و…)
  کلاس `SetInfo` و فیلدهای `OwnerNum/InventoryNum/...` در تنظیمات هست.

## گزارش تغییرات

### 1.1.0

**رفع باگ‌های بحرانی**
- کرش «Vertically scrollable component was measured with an infinity maximum
  height»: منوی خانه از حالت آکاردئونی (لیستِ Lazy داخل اسکرول بی‌نهایت) به
  **ناوبری واقعی بین صفحه‌ها** تبدیل شد؛ هر بخش داده در صفحه کامل خودش با
  نوار بالای اختصاصی و دکمه برگشت (هم آیکون، هم دکمه برگشت اندروید) باز می‌شود.
- حلقه/پرش ناوبری: تغییر هر تنظیماتی کاربر را دیگر از صفحه فعلی خارج نمی‌کند؛
  مقصد اولیه فقط یک‌بار در شروع برنامه تعیین می‌شود (رفع پرش Home→Login).
- کرش کلید تکراری در `LazyColumn`: کلید ردیف‌ها با شاخص ترکیب شد تا id های
  تکراری سمت سرور باعث `Key was already used` نشود.
- واکشی تکراری در سرویس‌های بدون صفحه‌بندی (فاکتور/چک مشتری، پیام‌های
  خوانده‌نشده) با پارامتر `paged=false` رفع شد.
- تولید توکن امنیتی اکنون با `Locale.US` ارقام ASCII می‌سازد (قبلاً روی
  دستگاه با locale فارسی/عربی می‌توانست رقم غیرASCII تولید کند).
- پارامترهای مسیر (ShMo، ShKa، VisitorID و…) قبل از قرارگیری در URL انکود
  می‌شوند.

**امنیت**
- `android:allowBackup="false"` — جلوگیری از استخراج تنظیمات/رمز از بکاپ.
- فیلدهای رمز عبور در صفحه ورود و تنظیمات ماسک شدند (با دکمه نمایش).

**بهبودها**
- آیکون اپلیکیشن (PNG در همه چگالی‌ها + آیکون تطبیقی برای اندروید ۸+).
- دکمه «تلاش دوباره» هنگام خطای بارگذاری لیست‌ها.
- دکمه خروج (پاک‌کردن تنظیمات) در صفحه خانه.
- صفحه تنظیمات هنگام باز شدن از خانه/ورود، دکمه بازگشت دارد.
- نمایش کاربر و ShMo فعلی در صفحه خانه.
- `loginVisitor` خروجی تایپ‌شده (`VisitorLoginResult?`) برمی‌گرداند و کلاس
  `kagroup` به `KaGroup` تغییر نام یافت.
