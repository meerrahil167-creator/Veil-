package com.example.veil.engine

import com.example.veil.model.CommunitySite
import com.example.veil.model.HostedService
import com.example.veil.model.PrivateInvite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom
import java.util.Locale

class CommunityManager {
    private val random = SecureRandom()

    private val _directorySites = MutableStateFlow<List<CommunitySite>>(emptyList())
    val directorySites: StateFlow<List<CommunitySite>> = _directorySites.asStateFlow()

    private val _joinedInvites = MutableStateFlow<List<PrivateInvite>>(emptyList())
    val joinedInvites: StateFlow<List<PrivateInvite>> = _joinedInvites.asStateFlow()

    private val _hostedServices = MutableStateFlow<List<HostedService>>(emptyList())
    val hostedServices: StateFlow<List<HostedService>> = _hostedServices.asStateFlow()

    val isDirectoryVerified = true
    val directoryFingerprint = "7a1b94e82c5d6f10"

    init {
        loadDefaultDirectory()
    }

    private fun loadDefaultDirectory() {
        _directorySites.value = listOf(
            CommunitySite(
                name = "The Tor Project",
                description = "Official Tor Project website, served as an onion service.",
                url = "http://2gzyxa5ihm7nsggfxnu52rck2vv4rvmdlkiu3zzui5du4xyclen53wid.onion/"
            ),
            CommunitySite(
                name = "DuckDuckGo",
                description = "Private search engine — the onion version.",
                url = "https://duckduckgogg42xjoc72x3sjasowoarfbgcmvfimaftt6twagswzczad.onion/"
            ),
            CommunitySite(
                name = "BBC News",
                description = "BBC News, reachable without leaving Tor.",
                url = "https://www.bbcnewsd73hkzno2ini43t4gblxvycyac5aw4gnv7t2rccijh7745uqd.onion/"
            ),
            CommunitySite(
                name = "ProPublica",
                description = "Independent, non-profit investigative journalism newsroom.",
                url = "https://p53lf57qovyuvwsc6xnrppyply3vtqm7l6pcobkmyqsiofyeznfu5uqd.onion/"
            ),
            CommunitySite(
                name = "SecureDrop Directory",
                description = "Open-source whistleblower submission system directory.",
                url = "http://sdolvtfhatvsysc6l34d65ymdwxcujausv7k5jk4cy5ttzhjoi6fzvyd.onion/"
            ),
            CommunitySite(
                name = "Archive.today",
                description = "Digital time capsule preserving web pages snapshot onion.",
                url = "http://archivebay7n5wtuvmwfkuz6l7lyqrhgahsgx6wvgqy452mdf7hx6wid.onion/"
            )
        )
    }

    fun joinInvite(inviteString: String): Result<PrivateInvite> {
        val trimmed = inviteString.trim()
        val regex = Regex("^veil-invite:([a-z2-7]{56})\\.onion:([A-Za-z0-9+/=]{40,50})$")
        val match = regex.find(trimmed)
            ?: return Result.failure(IllegalArgumentException("Invalid Veil invite format. Expected: veil-invite:<56-char-onion>.onion:<client-key>"))

        val serviceId = match.groupValues[1]
        val clientKey = match.groupValues[2]
        val invite = PrivateInvite(
            serviceId = serviceId,
            clientKey = clientKey,
            url = "http://$serviceId.onion/"
        )

        _joinedInvites.value = _joinedInvites.value.filterNot { it.serviceId == serviceId } + invite
        return Result.success(invite)
    }

    fun removeInvite(serviceId: String) {
        _joinedInvites.value = _joinedInvites.value.filterNot { it.serviceId == serviceId }
    }

    fun createHostedService(
        title: String,
        content: String,
        inviteOnly: Boolean
    ): HostedService {
        val onionChars = "abcdefghijklmnopqrstuvwxyz234567"
        val serviceId = (1..56).map { onionChars[random.nextInt(onionChars.length)] }.joinToString("")
        val onionUrl = "http://$serviceId.onion/"

        val inviteKey = if (inviteOnly) {
            val keyChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
            val key = (1..43).map { keyChars[random.nextInt(keyChars.length)] }.joinToString("") + "="
            "veil-invite:$serviceId.onion:$key"
        } else null

        val service = HostedService(
            serviceId = serviceId,
            title = title.ifBlank { "Ephemeral Onion Service" },
            onionUrl = onionUrl,
            content = content.ifBlank { "Hello from Veil Onion Service!\nHosted securely through Tor." },
            isInviteOnly = inviteOnly,
            inviteKey = inviteKey
        )

        _hostedServices.value = _hostedServices.value + service
        return service
    }

    fun stopHostedService(serviceId: String) {
        _hostedServices.value = _hostedServices.value.filterNot { it.serviceId == serviceId }
    }

    fun clearAll() {
        _joinedInvites.value = emptyList()
        _hostedServices.value = emptyList()
    }
}
