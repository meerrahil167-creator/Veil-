package com.example.veil.model

enum class HopRole {
    GUARD,
    MIDDLE,
    EXIT
}

data class RelayHop(
    val nickname: String,
    val ipFingerprint: String,
    val countryCode: String,
    val countryName: String,
    val role: HopRole,
    val bandwidth: String = "15 MB/s"
)

data class Circuit(
    val domain: String,
    val hops: List<RelayHop>,
    val createdAt: Long = System.currentTimeMillis(),
    val isEncrypted: Boolean = true
)

enum class SecurityLevel(val label: String, val description: String) {
    STANDARD("Standard", "All browser features and JavaScript enabled."),
    SAFER("Safer", "WebGL disabled, performance APIs restricted, some media click-to-play."),
    SAFEST("Safest", "JavaScript disabled completely. Maximum fingerprint protection.")
}

enum class BridgeType(val label: String) {
    NONE("None"),
    SNOWFLAKE("Snowflake"),
    OBFS4("obfs4"),
    MEEK("meek-azure"),
    CUSTOM("Custom Bridge")
}

sealed class TorState {
    data class Bootstrapping(val percent: Int, val step: String) : TorState()
    data class Connected(val circuitsCount: Int, val guardRelay: String) : TorState()
    data class Error(val message: String) : TorState()
}

data class CommunitySite(
    val name: String,
    val description: String,
    val url: String,
    val isVerified: Boolean = true
)

data class PrivateInvite(
    val serviceId: String,
    val clientKey: String,
    val url: String,
    val addedAt: Long = System.currentTimeMillis()
)

data class HostedService(
    val serviceId: String,
    val title: String,
    val onionUrl: String,
    val content: String,
    val isInviteOnly: Boolean,
    val inviteKey: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class BrowserTab(
    val id: String,
    val title: String,
    val url: String,
    val isLoading: Boolean = false,
    val progress: Int = 100,
    val circuit: Circuit? = null,
    val adsBlockedCount: Int = 0,
    val hasOnionAlternative: Boolean = false,
    val onionAlternativeUrl: String? = null
)

enum class TestStatus {
    PASS,
    FAIL,
    PENDING
}

data class LeakTestItem(
    val category: String,
    val title: String,
    val detail: String,
    val value: String,
    val status: TestStatus
)

data class SelfTestResult(
    val checkName: String,
    val passed: Boolean,
    val detail: String
)

enum class VeilScreen {
    HUB,
    BROWSER,
    ROUTING_VISUALIZER,
    LEAK_TEST,
    COMMUNITY,
    HOST_ONION,
    SELF_TEST
}
