package com.example.data.model

enum class DnsCategory(val title: String, val kanji: String) {
    GLOBAL("Global Anycast", "世"),
    PRIVACY("Privacy & Security", "密"),
    ADBLOCK("Ad & Tracker Blocking", "遮"),
    IRAN_UNBLOCK("Iran Anti-Sanction", "解")
}

data class DnsServer(
    val id: String,
    val name: String,
    val primaryIp: String,
    val secondaryIp: String = "",
    val dohUrl: String = "",
    val dotHostname: String = "",
    val region: String = "Global Anycast",
    val category: DnsCategory = DnsCategory.GLOBAL,
    val description: String = ""
)

object DefaultDnsList {
    val servers = listOf(
        DnsServer(
            id = "cf",
            name = "Cloudflare 1.1.1.1",
            primaryIp = "1.1.1.1",
            secondaryIp = "1.0.0.1",
            dohUrl = "https://cloudflare-dns.com/dns-query",
            dotHostname = "one.one.one.one",
            region = "Global Anycast / Fast",
            category = DnsCategory.GLOBAL,
            description = "Ultra-low latency privacy-first recursive resolver."
        ),
        DnsServer(
            id = "google",
            name = "Google Public DNS",
            primaryIp = "8.8.8.8",
            secondaryIp = "8.8.4.4",
            dohUrl = "https://dns.google/dns-query",
            dotHostname = "dns.google",
            region = "Global Anycast",
            category = DnsCategory.GLOBAL,
            description = "World's most resilient and widely distributed Anycast network."
        ),
        DnsServer(
            id = "quad9",
            name = "Quad9 Security",
            primaryIp = "9.9.9.9",
            secondaryIp = "149.112.112.112",
            dohUrl = "https://dns.quad9.net/dns-query",
            dotHostname = "dns.quad9.net",
            region = "Swiss / Global Anycast",
            category = DnsCategory.PRIVACY,
            description = "Non-profit DNS blocking malicious domains and phishing."
        ),
        DnsServer(
            id = "opendns",
            name = "OpenDNS (Cisco)",
            primaryIp = "208.67.222.222",
            secondaryIp = "208.67.220.220",
            dohUrl = "https://doh.opendns.com/dns-query",
            dotHostname = "dns.opendns.com",
            region = "Global Anycast",
            category = DnsCategory.GLOBAL,
            description = "High-availability enterprise and consumer DNS service."
        ),
        DnsServer(
            id = "adguard",
            name = "AdGuard DNS",
            primaryIp = "94.140.14.14",
            secondaryIp = "94.140.15.15",
            dohUrl = "https://dns.adguard.com/dns-query",
            dotHostname = "dns.adguard.com",
            region = "Global Anycast",
            category = DnsCategory.ADBLOCK,
            description = "Filters intrusive advertisements and tracking analytics."
        ),
        DnsServer(
            id = "dnssb",
            name = "DNS.SB",
            primaryIp = "185.222.222.222",
            secondaryIp = "45.11.45.11",
            dohUrl = "https://doh.dns.sb/dns-query",
            dotHostname = "dot.sb",
            region = "Europe / Asia Anycast",
            category = DnsCategory.PRIVACY,
            description = "Zero logging, DNSSEC enabled, high-privacy resolver."
        ),
        DnsServer(
            id = "cleanbrowsing",
            name = "CleanBrowsing",
            primaryIp = "185.228.168.9",
            secondaryIp = "185.228.169.9",
            dohUrl = "https://doh.cleanbrowsing.org/doh/security-filter/",
            dotHostname = "security-filter-dns.cleanbrowsing.org",
            region = "Global Anycast",
            category = DnsCategory.PRIVACY,
            description = "Security filter against malware, phishing and adult domains."
        ),
        DnsServer(
            id = "shecan",
            name = "Shecan (شکن)",
            primaryIp = "178.22.122.100",
            secondaryIp = "185.51.200.2",
            dohUrl = "https://free.shecan.ir/dns-query",
            dotHostname = "free.shecan.ir",
            region = "Iran Anti-Sanction",
            category = DnsCategory.IRAN_UNBLOCK,
            description = "Iran-specific unblocker for international sanction restrictions."
        ),
        DnsServer(
            id = "electro",
            name = "Electro (الکترو)",
            primaryIp = "78.157.42.101",
            secondaryIp = "78.157.42.100",
            dohUrl = "https://dns.electrotm.org/dns-query",
            dotHostname = "dns.electrotm.org",
            region = "Iran Anti-Sanction / Gaming",
            category = DnsCategory.IRAN_UNBLOCK,
            description = "Low ping gaming and developer sanction circumvention DNS."
        ),
        DnsServer(
            id = "online403",
            name = "403.online (۴۰۳)",
            primaryIp = "10.202.10.202",
            secondaryIp = "10.202.10.102",
            dohUrl = "https://403.online/dns-query",
            dotHostname = "dns403.online",
            region = "Iran Tech Unblock",
            category = DnsCategory.IRAN_UNBLOCK,
            description = "Unblocks GitHub, Docker, Google Cloud and dev services in Iran."
        ),
        DnsServer(
            id = "radargame",
            name = "Radar Game (رادار)",
            primaryIp = "10.202.10.10",
            secondaryIp = "10.202.10.11",
            dohUrl = "",
            dotHostname = "radar.game",
            region = "Iran Low-Ping Game",
            category = DnsCategory.IRAN_UNBLOCK,
            description = "Optimized routing and ping reduction for online gaming in Iran."
        ),
        DnsServer(
            id = "begzar",
            name = "Begzar (بگذر)",
            primaryIp = "185.55.226.26",
            secondaryIp = "185.55.225.25",
            dohUrl = "",
            dotHostname = "dns.begzar.ir",
            region = "Iran Anti-Sanction",
            category = DnsCategory.IRAN_UNBLOCK,
            description = "Free Iranian sanction circumvention DNS resolver."
        ),
        DnsServer(
            id = "alidns",
            name = "AliDNS (Alibaba)",
            primaryIp = "223.5.5.5",
            secondaryIp = "223.6.6.6",
            dohUrl = "https://dns.alidns.com/dns-query",
            dotHostname = "dns.alidns.com",
            region = "Asia Pacific / Global",
            category = DnsCategory.GLOBAL,
            description = "Major Asian high-speed Anycast DNS network."
        )
    )
}
