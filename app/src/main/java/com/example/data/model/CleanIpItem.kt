package com.example.data.model

data class CleanIpItem(
    val ip: String,
    val port: Int = 443,
    val provider: String = "Cloudflare",
    val latencyMs: Long = -1,
    val packetLossPercent: Int = 0,
    val jitterMs: Long = 0,
    val isClean: Boolean = false,
    val isTesting: Boolean = false,
    val isFavorite: Boolean = false,
    val notes: String = "",
    val testedAt: Long = 0
)

data class CdnRange(
    val provider: String,
    val cidr: String,
    val description: String,
    val sampleIps: List<String>
)

object KnownCdnRanges {
    val providers = listOf(
        "All Providers",
        "Cloudflare",
        "Fastly",
        "Gcore",
        "CloudFront",
        "Akamai",
        "BunnyCDN",
        "ArvanCloud",
        "DerakCloud",
        "CDN77 / CacheFly"
    )

    val cdnRanges = listOf(
        // Cloudflare
        CdnRange(
            provider = "Cloudflare",
            cidr = "104.16.0.0/12",
            description = "Cloudflare CDN Anycast Edge (Most Popular)",
            sampleIps = listOf(
                "104.16.12.1", "104.16.45.89", "104.16.110.24", "104.16.180.5",
                "104.17.23.4", "104.17.89.12", "104.17.150.33", "104.17.200.70",
                "104.18.15.2", "104.18.32.90", "104.18.77.10", "104.19.12.5",
                "104.20.10.1", "104.21.32.4", "104.22.45.18", "104.24.112.5"
            )
        ),
        CdnRange(
            provider = "Cloudflare",
            cidr = "172.64.0.0/13",
            description = "Cloudflare High Performance Subnets",
            sampleIps = listOf(
                "172.64.32.1", "172.64.88.10", "172.64.120.5", "172.64.199.3",
                "172.67.70.1", "172.67.140.22", "172.67.180.99", "172.67.220.14",
                "172.67.12.5", "172.67.89.4", "172.64.150.2", "172.64.210.8"
            )
        ),
        CdnRange(
            provider = "Cloudflare",
            cidr = "162.159.0.0/16",
            description = "Cloudflare WARP & Enterprise Edge",
            sampleIps = listOf(
                "162.159.192.1", "162.159.193.5", "162.159.195.10", "162.159.36.1",
                "162.159.46.1", "162.159.138.1", "162.159.200.1", "162.159.24.1",
                "162.159.224.1", "162.159.225.10", "162.159.128.5", "162.159.150.2"
            )
        ),
        CdnRange(
            provider = "Cloudflare",
            cidr = "188.114.96.0/20",
            description = "Cloudflare European / Middle-East Optimized",
            sampleIps = listOf(
                "188.114.96.1", "188.114.97.2", "188.114.98.5", "188.114.99.12",
                "188.114.100.1", "188.114.105.7", "188.114.110.3", "188.114.96.15",
                "188.114.97.24", "188.114.108.9"
            )
        ),
        CdnRange(
            provider = "Cloudflare",
            cidr = "198.41.128.0/17",
            description = "Cloudflare Core Infrastructure",
            sampleIps = listOf(
                "198.41.128.1", "198.41.130.4", "198.41.140.7", "198.41.150.12",
                "198.41.200.5", "198.41.214.2", "198.41.135.8", "198.41.148.90"
            )
        ),
        CdnRange(
            provider = "Cloudflare",
            cidr = "141.101.64.0/18",
            description = "Cloudflare Transit Anycast",
            sampleIps = listOf(
                "141.101.64.1", "141.101.65.2", "141.101.90.15", "141.101.120.4",
                "141.101.75.10", "141.101.110.22"
            )
        ),
        CdnRange(
            provider = "Cloudflare",
            cidr = "108.162.192.0/18",
            description = "Cloudflare Global Low-Latency Tier",
            sampleIps = listOf(
                "108.162.192.1", "108.162.195.4", "108.162.200.10", "108.162.210.8",
                "108.162.220.15", "108.162.240.2"
            )
        ),

        // Fastly
        CdnRange(
            provider = "Fastly",
            cidr = "151.101.0.0/16",
            description = "Fastly Global Anycast Edge",
            sampleIps = listOf(
                "151.101.1.140", "151.101.65.140", "151.101.129.140", "151.101.193.140",
                "151.101.2.132", "151.101.66.132", "151.101.130.132", "151.101.194.132"
            )
        ),
        CdnRange(
            provider = "Fastly",
            cidr = "199.232.0.0/16",
            description = "Fastly Enterprise PoPs",
            sampleIps = listOf(
                "199.232.2.140", "199.232.66.140", "199.232.130.140", "199.232.194.140"
            )
        ),

        // Gcore
        CdnRange(
            provider = "Gcore",
            cidr = "92.223.0.0/16",
            description = "Gcore Global CDN / Middle East Friendly",
            sampleIps = listOf(
                "92.223.84.1", "92.223.88.2", "92.223.100.5", "92.223.112.4",
                "92.223.80.10", "92.223.95.4", "92.223.104.2"
            )
        ),
        CdnRange(
            provider = "Gcore",
            cidr = "92.38.0.0/16",
            description = "Gcore European & Eurasian PoPs",
            sampleIps = listOf(
                "92.38.128.1", "92.38.140.5", "92.38.150.12", "92.38.160.8"
            )
        ),

        // Amazon CloudFront
        CdnRange(
            provider = "CloudFront",
            cidr = "13.32.0.0/15",
            description = "AWS CloudFront Global Edge",
            sampleIps = listOf(
                "13.32.84.12", "13.32.110.45", "13.32.150.2", "13.32.200.70",
                "13.33.40.10", "13.33.95.22"
            )
        ),
        CdnRange(
            provider = "CloudFront",
            cidr = "143.204.0.0/16",
            description = "AWS CloudFront Anycast Tier",
            sampleIps = listOf(
                "143.204.50.12", "143.204.88.5", "143.204.140.22", "143.204.190.4"
            )
        ),

        // Akamai
        CdnRange(
            provider = "Akamai",
            cidr = "23.32.0.0/11",
            description = "Akamai EdgeSuite Worldwide",
            sampleIps = listOf(
                "23.32.10.15", "23.32.45.80", "23.33.120.4", "23.34.180.12",
                "23.35.50.2", "23.36.90.14"
            )
        ),

        // BunnyCDN
        CdnRange(
            provider = "BunnyCDN",
            cidr = "169.150.206.0/24",
            description = "Bunny.net Fast High-Throughput PoPs",
            sampleIps = listOf(
                "169.150.206.1", "169.150.206.15", "169.150.206.50", "169.150.206.100",
                "185.246.188.1", "185.246.188.24", "84.17.45.1", "84.17.45.40"
            )
        ),

        // ArvanCloud
        CdnRange(
            provider = "ArvanCloud",
            cidr = "185.143.232.0/22",
            description = "ArvanCloud Anycast (Iran / Middle East)",
            sampleIps = listOf(
                "185.143.232.1", "185.143.233.2", "185.143.234.3", "185.143.235.10",
                "185.231.112.5", "185.231.114.12", "94.182.152.1", "94.182.154.20"
            )
        ),

        // DerakCloud
        CdnRange(
            provider = "DerakCloud",
            cidr = "185.206.92.0/22",
            description = "DerakCloud Regional Anycast",
            sampleIps = listOf(
                "185.206.92.1", "185.206.93.5", "185.206.94.10", "185.165.116.2",
                "185.165.118.15"
            )
        ),

        // CDN77 / CacheFly
        CdnRange(
            provider = "CDN77 / CacheFly",
            cidr = "89.187.160.0/19",
            description = "DataPath / CDN77 Global Edge",
            sampleIps = listOf(
                "89.187.160.1", "89.187.165.4", "89.187.170.12", "205.234.175.1",
                "205.234.175.25", "185.229.226.5"
            )
        )
    )

    fun getAllSampleIps(): List<String> {
        return cdnRanges.flatMap { it.sampleIps }.distinct()
    }

    fun detectProviderForIp(ip: String): String {
        return cdnRanges.find { range ->
            range.sampleIps.contains(ip) || ipMatchesCidrPrefix(ip, range.cidr)
        }?.provider ?: "Anycast CDN"
    }

    private fun ipMatchesCidrPrefix(ip: String, cidr: String): Boolean {
        val baseIp = cidr.substringBefore("/")
        val ipParts = ip.split(".")
        val baseParts = baseIp.split(".")
        if (ipParts.size >= 2 && baseParts.size >= 2) {
            return ipParts[0] == baseParts[0] && ipParts[1] == baseParts[1]
        }
        return false
    }
}
