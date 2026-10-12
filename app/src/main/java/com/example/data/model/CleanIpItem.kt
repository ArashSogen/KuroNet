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
    val cloudflareRanges = listOf(
        CdnRange(
            provider = "Cloudflare",
            cidr = "104.16.0.0/12",
            description = "Cloudflare CDN Anycast Edge (Most Popular)",
            sampleIps = listOf(
                "104.16.12.1", "104.16.45.89", "104.16.110.24", "104.16.180.5",
                "104.17.23.4", "104.17.89.12", "104.17.150.33", "104.17.200.70",
                "104.18.15.2", "104.18.32.90", "104.18.77.10", "104.19.12.5"
            )
        ),
        CdnRange(
            provider = "Cloudflare",
            cidr = "172.64.0.0/13",
            description = "Cloudflare High Performance Subnets",
            sampleIps = listOf(
                "172.64.32.1", "172.64.88.10", "172.64.120.5", "172.64.199.3",
                "172.67.70.1", "172.67.140.22", "172.67.180.99", "172.67.220.14"
            )
        ),
        CdnRange(
            provider = "Cloudflare",
            cidr = "162.159.0.0/16",
            description = "Cloudflare WARP & Enterprise Edge",
            sampleIps = listOf(
                "162.159.192.1", "162.159.193.5", "162.159.195.10", "162.159.36.1",
                "162.159.46.1", "162.159.138.1", "162.159.200.1", "162.159.24.1"
            )
        ),
        CdnRange(
            provider = "Cloudflare",
            cidr = "188.114.96.0/20",
            description = "Cloudflare European / Middle-East Optimized",
            sampleIps = listOf(
                "188.114.96.1", "188.114.97.2", "188.114.98.5", "188.114.99.12",
                "188.114.100.1", "188.114.105.7", "188.114.110.3"
            )
        ),
        CdnRange(
            provider = "Cloudflare",
            cidr = "198.41.128.0/17",
            description = "Cloudflare Core Infrastructure",
            sampleIps = listOf(
                "198.41.128.1", "198.41.130.4", "198.41.140.7", "198.41.150.12",
                "198.41.200.5", "198.41.214.2"
            )
        ),
        CdnRange(
            provider = "Cloudflare",
            cidr = "141.101.64.0/18",
            description = "Cloudflare Transit Anycast",
            sampleIps = listOf(
                "141.101.64.1", "141.101.65.2", "141.101.90.15", "141.101.120.4"
            )
        ),
        CdnRange(
            provider = "Gcore",
            cidr = "92.223.0.0/16",
            description = "Gcore Global CDN",
            sampleIps = listOf(
                "92.223.84.1", "92.223.88.2", "92.223.100.5", "92.223.112.4"
            )
        ),
        CdnRange(
            provider = "ArvanCloud",
            cidr = "185.143.232.0/22",
            description = "ArvanCloud Anycast (Iran / Middle East)",
            sampleIps = listOf(
                "185.143.232.1", "185.143.233.2", "185.143.234.3"
            )
        )
    )

    fun getAllSampleIps(): List<String> {
        return cloudflareRanges.flatMap { it.sampleIps }.distinct()
    }
}
