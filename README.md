# KuroNet (黒) — Zen Network Benchmark & Clean IP Scanner
### بنچمارک دی‌ان‌اس و اسکنر آی‌پی تمیز (کلودفلر) با معماری مینیمال ژاپنی

[![Android](https://img.shields.io/badge/Platform-Android_7.0+-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0+-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Storage-Room_Offline_Cache-02569B.svg?style=flat)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 🌐 Language Navigation / تغییر زبان
- [English Documentation](#english)
- [راهنمای فارسی (Persian)](#فارسی)

---

<a name="english"></a>
## 🇬🇧 English

**KuroNet (黒)** is a high-contrast, minimalist Android application built with Japanese aesthetics (*Kanso* & *Shibui*). It is engineered for low-latency network optimization, real-time DNS diagnostic benchmarking, and discovering unblocked, low-latency **Clean Anycast IPs (White IPs)** for circumvention during severe internet disruptions and censorship (specifically in Iran and restricted networks).

### ⚡ Key Features

1. **Global DNS Latency Benchmark (測 - Soku)**
   - **Real Multi-Region Socket Probing:** Measures real-world TCP RTT latency, packet loss percentage, and jitter variance across major global and regional resolvers.
   - **Curated Resolvers:**
     - *Global Anycast:* Cloudflare (`1.1.1.1`), Google (`8.8.8.8`), Quad9 (`9.9.9.9`), OpenDNS Cisco (`208.67.222.222`), AliDNS (`223.5.5.5`), DNS.SB (`185.222.222.222`).
     - *Ad & Threat Filtering:* AdGuard DNS (`94.140.14.14`), CleanBrowsing.
     - *Iran Anti-Sanction & Gaming:* Shecan (`178.22.122.100`), Electro (`78.157.42.101`), 403.online (`10.202.10.202`), Radar Game (`10.202.10.10`), Begzar (`185.55.226.26`).
   - **Intelligent Stability Score:** Algorithms compute a 0–100 score considering latency, jitter, and packet loss, spotlighting the optimal DNS server with one-tap copy for IP and Private DNS (DoT) hostname.

2. **Multi-CDN Clean IP / White IP Scanner (境 - Kyou)**
   - **SenPai Anycast Pool (2,865 Embedded IPs):** Bundles a pre-generated, comprehensive database of **2,865 Anycast IPs** mirroring `MatinSenPai/SenPaiScanner` across Cloudflare (`104.16 - 104.27`, `172.64 - 172.71`, `162.158`, `162.159`, `198.41`, `188.114`, `141.101`, `108.162`, `173.245`, `103.21`), Fastly, Gcore, AWS CloudFront, Akamai, BunnyCDN, and ArvanCloud.
   - **Customizable Scan Batch Limits:** Flexible batch sweeping with selectable limits: `50`, `100`, `250`, `500`, `1,000`, or `All 2,865 IPs` with high-concurrency asynchronous coroutines.
   - **Custom `ips.txt` Input:** Import and paste your own custom IP lists or CIDRs directly into the app.
   - **All Providers Scanning:** One-tap scan across all CDN providers simultaneously ("Scan All Providers Together"), or filter by specific CDN.
   - **Port Inspection:** Tests TLS ports `443`, `80`, and `2408` (WARP/WireGuard).
   - **Zero Packet Loss Verification:** Filters out throttled/blackholed IPs, isolating verified clean endpoints with 0% loss.
   - **Instant Config Export:** Directly exports discovered IPs formatted for:
     - Cloudflare WARP (`IP:2408`)
     - V2Ray / Xray VLESS/VMess CDN addresses
     - Clash YAML proxy nodes
     - Raw clean IP lists

3. **Offline Vault & Emergency Configs (存 - Son)**
   - **Room Database Offline Storage:** Automatically persists all benchmark results and starred Clean IPs locally on-device.
   - **Pre-seeded Emergency Profiles:** Access connection guides, WARP configurations, V2Ray TLS fragmentation parameters, and DoT setups even when internet access is completely cut off.
   - **Custom Config Notepad:** Save your own backup keys and server snippets securely offline.

4. **Network Telemetry & DPI Bypass Guide (静 - Shizuka)**
   - **Live Diagnostics:** Gateway reachability, carrier connection type (Wi-Fi, Cellular LTE/5G, VPN).
   - **MTU Clamping Advice:** Recommendations for MTU adjustment (e.g., 1280 bytes) to prevent UDP drops on mobile operators (MCI, Irancell, Rightel).
   - **Circumvention Architecture:** In-depth guidelines on TLS ClientHello fragmentation, SNI camouflage, and Anycast routing.

5. **Aesthetics & Haptics**
   - **Sumi-e Monochromatic Theme:** Pure pitch obsidian `#090A0E` paired with Torii Red (`#FF334B`) and Bamboo Green (`#30D158`).
   - **Typography Pairing:** Bundled Google Fonts *Zen Kaku Gothic New* (headers) and *Space Mono* (telemetry & IPs).
   - **Tactile Haptic Feedback:** Physical vibration cues on scans, test completions, and copying.

---

### 📦 Building & Installation

#### Requirements
- Android 7.0+ (API level 24+)
- JDK 17+
- Android SDK 36

#### Build Universal APK via Command Line
```bash
# Clone the repository
git clone https://github.com/your-username/KuroNet.git
cd KuroNet

# Build the Universal Debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

#### Install on Device via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

<a name="فارسی"></a>
## 🇮🇷 فارسی (Persian)

**کورونت (KuroNet - 黒)** یک ابزار سبک، مینیمال و پرسرعت برای اندروید است که با الهام از زیبایی‌شناسی ژاپنی طراحی شده است. این برنامه به منظور سنجش دقیق پینگ و پکت‌لاس دی‌ان‌اس‌ها (DNS)، یافتن سریع **آی‌پی‌های تمیز و سفید (Clean IP)** کلودفلر، و دسترسی آفلاین به تنظیمات اضطراری برای دور زدن فیلترینگ و اختلالات شدید اینترنت در ایران توسعه یافته است.

### 🌟 ویژگی‌های کلیدی

1. **بنچمارک و تست زنده DNS (بخش 測)**
   - **تست واقعی با سوکت TCP:** اندازه‌گیری دقیق پینگ رفت‌وبرگشت (RTT)، جیتر (Jitter) و درصد پکت‌لاس بدون نیاز به روت.
   - **پشتیبانی از سرورهای جهانی و ضدتحریم ایران:**
     - سرورهای جهانی: کلودفلر (`1.1.1.1`)، گوگل (`8.8.8.8`)، کواد۹ (`9.9.9.9`)، اپن‌دی‌ان‌اس، علی‌دی‌ان‌اس و DNS.SB.
     - مسدودساز تبلیغات: AdGuard DNS و CleanBrowsing.
     - دی‌ان‌اس‌های ضدتحریم و گیمینگ ایران: **شکن (Shecan)**، **الکترو (Electro)**، **۴۰۳ (403.online)**، **رادار گیم (Radar)** و **بگذر (Begzar)**.
   - **امتیازدهی هوشمند (Stability Score):** انتخاب بهترین دی‌ان‌اس بر اساس پایداری واقعی و کپی سریع آدرس یا Private DNS (DoT) تنها با یک لمس.

2. **اسکنر آی‌پی تمیز چند شبکه‌ای (Multi-CDN Clean IP - بخش 境)**
   - **بانک جامع ۲٬۸۶۵ آی‌پی برگرفته از SenPaiScanner:** حاوی یک دیتابیس آماده و تعبیه‌شده از **۲٬۸۶۵ آی‌پی تمیز و Anycast** مشابه پروژه `MatinSenPai/SenPaiScanner` در سرتاسر رنج‌های کلودفلر (`104.16` تا `104.27`، `172.64` تا `172.71`، `162.158`، `162.159`، `198.41`، `188.114`، `141.101`، `108.162`، `173.245`، `103.21`)، فستلی، جی‌کور، کلودفرانت، آکامای و بانی‌سی‌دی‌ان.
   - **تنظیم تعداد اسکن (Batch Limits):** انتخاب حجم اسکن بر اساس نیاز شما: `۵۰`، `۱۰۰`، `۲۵۰`، `۵۰۰`، `۱٬۰۰۰` یا `کل ۲٬۸۶۵ آی‌پی` با پردازش موازی و سریع کوروتین‌ها.
   - **پشتیبانی از ورود دستی `ips.txt`:** امکان چسباندن (Paste) لیست یا ساب‌نت‌های دلخواه شما.
   - **اسکن یکپارچه تمام ارائه‌دهنده‌ها (Scan All Together):** امکان اسکن همزمان تمام CDNها با یک کلیک جهت یافتن سریع‌ترین آی‌پی‌های بدون اختلال.
   - **تست پورت‌های حساس:** سنجش هندشیک پورت‌های `443` (HTTPS)، `80` (HTTP) و `2408` (مخصوص وارپ / WireGuard).
   - **فیلتر پکت‌لاس صفر (۰٪):** جداسازی آی‌پی‌هایی که در اپراتورهای همراه اول، ایرانسل و رایتل مسدود یا تراتل (کند) نشده‌اند.
   - **خروجی گرفتن آسان کانفیگ:**
     - کپی فرمت اندپوینت کلودفلر وارپ (`IP:2408`)
     - کپی آدرس تمیز برای کانفیگ‌های V2Ray / Xray (VLESS, VMess, Trojan)
     - ساخت نود پروکسی برای کلاش (Clash) و سینگ‌باکس (Sing-box)

3. **گاوصندوق آفلاین کانفیگ‌ها و آی‌پی‌های ذخیره شده (بخش 存)**
   - **پایگاه داده آفلاین Room:** تمام آی‌پی‌های تمیز و نتایج بنچمارک به‌طور خودکار روی حافظه دستگاه ذخیره می‌شوند.
   - **پروفایل‌های آماده برای زمان قطعی کامل اینترنت:** دسترسی فوری به آموزش تنظیم DoT، تنظیمات وارپ با کلین آی‌پی، و فرگمنت TLS حتی هنگام قطعی اینترنت بین‌الملل.
   - **امکان افزودن کانفیگ شخصی:** یادداشت و ذخیره امن کانفیگ‌های بکاپ.

4. **اطلاعات شبکه و راهنمای دور زدن فیلترینگ (بخش 静)**
   - **تست گیت‌وی و اینترنت:** پینگ زنده به لبه‌های شبکه و تشخیص اتصال وای‌فای، همراه، یا وی‌پی‌ان.
   - **بهینه‌سازی MTU:** راهنمای کاهش MTU به مقدار ۱۲۸۰ برای جلوگیری از Drop شدن پکت‌های UDP وارپ روی شبکه اپراتورهای همراه ایران.
   - **تکنیک فرگمنتشن (TLS Fragmentation):** راهنمای تکه‌تکه کردن پکت ClientHello جهت عبور از سیستم‌های DPI و فیلترینگ SNI.

5. **طراحی مینیمال با لرزش هپتیک (Haptic Feedback)**
   - تم دارک عمیق با رنگ مشکی مرکب (Sumi Black)، خطوط ظریف و رنگ قرمز توری (Torii Red).
   - فونت‌های اختصاصی ژاپنی و تک‌فاصله (Space Mono) برای خوانایی فوق‌العاده داده‌های شبکه.
   - ویبره و فیدبک لمسی هنگام اتمام تست‌ها و لمس دکمه‌ها.

---

### 🚀 نحوه ساخت و خروجی APK

برای ساخت فایل APK یونیورسال که روی تمامی پردازنده‌های اندروید (ARM64, ARMv7, x86_64) اجرا می‌شود:

```bash
# کلون کردن ریپازیتوری
git clone https://github.com/your-username/KuroNet.git
cd KuroNet

# بیلد نسخه APK در لینوکس یا مک
./gradlew assembleDebug

# مسیر فایل خروجی:
# app/build/outputs/apk/debug/app-debug.apk
```

---

### 📱 پیش‌نیازها
- اندروید نسخه **7.0 (Nougat)** به بالا
- بدون نیاز به روت (No Root Required)
- حجم بسیار سبک (~23 مگابایت)

---

### 📄 مجوز (License)
این پروژه تحت مجوز **MIT** منتشر شده است. استفاده، ویرایش و بازنشر آن آزاد است.
