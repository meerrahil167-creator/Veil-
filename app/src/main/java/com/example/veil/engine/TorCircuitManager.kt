package com.example.veil.engine

import com.example.veil.model.BridgeType
import com.example.veil.model.Circuit
import com.example.veil.model.HopRole
import com.example.veil.model.RelayHop
import com.example.veil.model.TorState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom
import kotlin.random.Random

class TorCircuitManager {
    private val random = SecureRandom()
    private val circuitMap = mutableMapOf<String, Circuit>()

    private val _torState = MutableStateFlow<TorState>(TorState.Bootstrapping(10, "Connecting to directory mirror"))
    val torState: StateFlow<TorState> = _torState.asStateFlow()

    private val _activeBridge = MutableStateFlow(BridgeType.NONE)
    val activeBridge: StateFlow<BridgeType> = _activeBridge.asStateFlow()

    private val guardPool = listOf(
        RelayHop("SnowdenGuard", "94.23.151.78", "DE", "Germany", HopRole.GUARD, "24 MB/s"),
        RelayHop("NordicSentry", "185.220.101.5", "IS", "Iceland", HopRole.GUARD, "32 MB/s"),
        RelayHop("AlpenShield", "178.209.51.10", "CH", "Switzerland", HopRole.GUARD, "18 MB/s"),
        RelayHop("FjordNode", "193.11.114.42", "SE", "Sweden", HopRole.GUARD, "20 MB/s"),
        RelayHop("AmsterdamRelay", "194.109.206.212", "NL", "Netherlands", HopRole.GUARD, "28 MB/s"),
        RelayHop("HelsinkiPost", "95.216.144.18", "FI", "Finland", HopRole.GUARD, "22 MB/s")
    )

    private val middlePool = listOf(
        RelayHop("ViennaTransit", "193.170.187.3", "AT", "Austria", HopRole.MIDDLE, "19 MB/s"),
        RelayHop("PragueHop", "195.113.214.22", "CZ", "Czechia", HopRole.MIDDLE, "25 MB/s"),
        RelayHop("BucharestCore", "86.107.110.15", "RO", "Romania", HopRole.MIDDLE, "16 MB/s"),
        RelayHop("OsloLink", "158.39.77.100", "NO", "Norway", HopRole.MIDDLE, "21 MB/s"),
        RelayHop("BratislavaRelay", "147.213.1.5", "SK", "Slovakia", HopRole.MIDDLE, "17 MB/s"),
        RelayHop("TallinnHop", "194.126.115.80", "EE", "Estonia", HopRole.MIDDLE, "15 MB/s")
    )

    private val exitPool = listOf(
        RelayHop("LemanExit", "185.165.171.12", "CH", "Switzerland", HopRole.EXIT, "35 MB/s"),
        RelayHop("ReykjavikEgress", "185.220.102.242", "IS", "Iceland", HopRole.EXIT, "40 MB/s"),
        RelayHop("RotterdamExit", "185.220.100.252", "NL", "Netherlands", HopRole.EXIT, "30 MB/s"),
        RelayHop("PanamaTerminal", "190.14.36.10", "PA", "Panama", HopRole.EXIT, "18 MB/s"),
        RelayHop("VictoriaGateway", "102.130.112.5", "SC", "Seychelles", HopRole.EXIT, "14 MB/s")
    )

    suspend fun initializeBootstrap() {
        _torState.value = TorState.Bootstrapping(10, "Connecting to directory mirror")
        delay(400)
        _torState.value = TorState.Bootstrapping(25, "Handshaking with guard relay")
        delay(400)
        _torState.value = TorState.Bootstrapping(50, "Establishing 1-hop encrypted consensus circuit")
        delay(350)
        _torState.value = TorState.Bootstrapping(75, "Loading microdescriptor consensus")
        delay(350)
        _torState.value = TorState.Bootstrapping(90, "Building isolated site circuits")
        delay(300)
        val guard = guardPool.random()
        _torState.value = TorState.Connected(circuitsCount = 3, guardRelay = guard.nickname)
    }

    fun setBridge(bridge: BridgeType) {
        _activeBridge.value = bridge
        resetAllCircuits()
    }

    fun getCircuitForDomain(domain: String): Circuit {
        val cleanDomain = domain.lowercase().trim().removePrefix("www.")
        return circuitMap.getOrPut(cleanDomain) {
            buildFreshCircuit(cleanDomain)
        }
    }

    fun renewCircuitForDomain(domain: String): Circuit {
        val cleanDomain = domain.lowercase().trim().removePrefix("www.")
        val newCircuit = buildFreshCircuit(cleanDomain)
        circuitMap[cleanDomain] = newCircuit
        return newCircuit
    }

    fun resetAllCircuits() {
        circuitMap.clear()
        val guard = guardPool.random()
        _torState.value = TorState.Connected(circuitsCount = 1, guardRelay = guard.nickname)
    }

    private fun buildFreshCircuit(domain: String): Circuit {
        val guard = guardPool[Random.nextInt(guardPool.size)]
        val middle = middlePool[Random.nextInt(middlePool.size)]
        val exit = exitPool[Random.nextInt(exitPool.size)]
        return Circuit(
            domain = domain,
            hops = listOf(guard, middle, exit),
            createdAt = System.currentTimeMillis()
        )
    }
}
