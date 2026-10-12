package com.example.data.model

import com.example.data.local.EmergencyConfigEntity

object PresetConfigs {
    val presets = listOf(
        EmergencyConfigEntity(
            id = 1,
            title = "Cloudflare WARP Clean Endpoint",
            category = "WARP",
            description = "WireGuard / WARP endpoint template using verified Clean IPs to bypass UDP/IP blocking.",
            content = """
[Interface]
PrivateKey = YOUR_WARP_PRIVATE_KEY
Address = 172.16.0.2/32, 2606:4700:110:8b94:b8c9:e7a9:97e5:2408/128
DNS = 1.1.1.1, 1.0.0.1
MTU = 1280

[Peer]
PublicKey = bmXOC+F1FxEMF9dyiK2H5/1SUtzH0JuVo51h2wPfgyo=
AllowedIPs = 0.0.0.0/0, ::/0
# Replace with discovered Clean IP below:
Endpoint = 162.159.192.1:2408
PersistentKeepalive = 15
            """.trimIndent(),
            isPreset = true
        ),
        EmergencyConfigEntity(
            id = 2,
            title = "V2Ray / Xray CDN Clean IP Outbound",
            category = "V2RAY",
            description = "VLESS / VMess WebSocket TLS snippet routing to Cloudflare Clean IP with Host header preservation.",
            content = """
{
  "outbounds": [
    {
      "protocol": "vless",
      "settings": {
        "vnext": [
          {
            "address": "CLEAN_IP_HERE", 
            "port": 443,
            "users": [
              {
                "id": "YOUR_UUID_HERE",
                "encryption": "none"
              }
            ]
          }
        ]
      },
      "streamSettings": {
        "network": "ws",
        "security": "tls",
        "tlsSettings": {
          "serverName": "your-domain.example.com",
          "allowInsecure": false,
          "fingerprint": "chrome"
        },
        "wsSettings": {
          "path": "/your-path",
          "headers": {
            "Host": "your-domain.example.com"
          }
        },
        "sockopt": {
          "dialerProxy": "fragment"
        }
      }
    }
  ]
}
            """.trimIndent(),
            isPreset = true
        ),
        EmergencyConfigEntity(
            id = 3,
            title = "Android Private DNS (DoT) Setup",
            category = "DOT_DNS",
            description = "System-wide encrypted DNS setup to prevent DNS tampering, hijacking, and poisoning.",
            content = """
1. Open Android Settings -> Network & Internet -> Private DNS.
2. Select 'Private DNS provider hostname'.
3. Enter one of the optimal hostnames tested in KuroNet:
   - one.one.one.one (Cloudflare)
   - dns.google (Google)
   - dns.quad9.net (Quad9)
   - free.shecan.ir (Shecan Iran unblocker)
   - dns.electrotm.org (Electro Iran gaming/anti-sanction)
   - dns403.online (403.online dev services)
4. Tap Save. All DNS requests will be encrypted over TLS (Port 853).
            """.trimIndent(),
            isPreset = true
        ),
        EmergencyConfigEntity(
            id = 4,
            title = "Clash / Sing-box Clean Proxy Node",
            category = "CLASH",
            description = "Clash YAML proxy snippet mapped to low-latency Clean CDN IP with SNI camouflage.",
            content = """
proxies:
  - name: "Kuro-Clean-CF"
    type: vless
    server: 104.16.12.1 # Discovered Clean IP
    port: 443
    uuid: YOUR_UUID
    tls: true
    servername: your-worker.workers.dev
    network: ws
    ws-opts:
      path: "/live"
      headers:
        Host: your-worker.workers.dev
    udp: true
            """.trimIndent(),
            isPreset = true
        )
    )
}
