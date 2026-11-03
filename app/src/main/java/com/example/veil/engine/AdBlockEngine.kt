package com.example.veil.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdBlockEngine {
    private val _enabled = MutableStateFlow(true)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _totalBlocked = MutableStateFlow(0)
    val totalBlocked: StateFlow<Int> = _totalBlocked.asStateFlow()

    private val blockedPatterns = listOf(
        "google-analytics.com",
        "googletagmanager.com",
        "doubleclick.net",
        "adservice.google.com",
        "facebook.net/tr",
        "connect.facebook.net",
        "analytics.twitter.com",
        "ads-twitter.com",
        "scorecardresearch.com",
        "criteo.com",
        "criteo.net",
        "outbrain.com",
        "taboola.com",
        "advertising.com",
        "adnxs.com",
        "rubiconproject.com",
        "quantserve.com",
        "hotjar.com",
        "clarity.ms",
        "amazon-adsystem.com",
        "adroll.com",
        "popads.net",
        "propellerads.com"
    )

    fun toggleAdBlock() {
        _enabled.value = !_enabled.value
    }

    fun shouldBlock(url: String): Boolean {
        if (!_enabled.value) return false
        val lower = url.lowercase()
        val match = blockedPatterns.any { lower.contains(it) }
        if (match) {
            _totalBlocked.value++
        }
        return match
    }

    fun resetStats() {
        _totalBlocked.value = 0
    }
}
