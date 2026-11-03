package com.example.veil.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.veil.model.Circuit
import com.example.veil.ui.theme.MonospaceFontFamily
import com.example.veil.ui.theme.VeilBackground
import com.example.veil.ui.theme.VeilBorder
import com.example.veil.ui.theme.VeilBorderBright
import com.example.veil.ui.theme.VeilGreen
import com.example.veil.ui.theme.VeilRedBright
import com.example.veil.ui.theme.VeilRedDark
import com.example.veil.ui.theme.VeilRedHighlight
import com.example.veil.ui.theme.VeilRedPrimary
import com.example.veil.ui.theme.VeilRedSecondary
import com.example.veil.ui.theme.VeilSurface
import com.example.veil.ui.theme.VeilSurfaceElevated
import com.example.veil.ui.theme.VeilTextFaint
import com.example.veil.ui.theme.VeilTextPrimary
import com.example.veil.ui.theme.VeilTextSecondary
import kotlinx.coroutines.delay

@Composable
fun RoutingVisualizerScreen(
    circuit: Circuit?,
    onRenewCircuit: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var isAutoPlaying by remember { mutableStateOf(false) }

    val liveLogs = remember {
        mutableStateListOf(
            "[CIRCUIT] 3-hop isolation circuit negotiated",
            "[CELL] CREATE2 ntor handshake completed with Guard",
            "[CELL] EXTEND2 payload forwarded to Middle",
            "[CELL] EXTEND2 payload forwarded to Exit",
            "[RELAY] Circuit active with 3 symmetric AES-CTR keys"
        )
    }

    LaunchedEffect(isAutoPlaying) {
        while (isAutoPlaying) {
            delay(1600)
            currentStep = (currentStep + 1) % 5
            liveLogs.add(0, "[CELL 0x${(1000..9999).random()}] RELAY_DATA 512 bytes routed through hop $currentStep")
            if (liveLogs.size > 20) liveLogs.removeLast()
        }
    }

    val hops = circuit?.hops ?: emptyList()
    val guard = hops.getOrNull(0)?.nickname ?: "SnowdenGuard (DE)"
    val middle = hops.getOrNull(1)?.nickname ?: "ViennaTransit (AT)"
    val exit = hops.getOrNull(2)?.nickname ?: "LemanExit (CH)"
    val destination = circuit?.domain ?: "duckduckgo.onion"

    val captions = listOf(
        "Client (Your Device): Encrypts packet with 3 nested layers of symmetric AES keys (Exit key, Middle key, Guard key)." to "3 layers of encryption present",
        "Guard Relay ($guard): Peels off outermost layer (Layer 3). Sees your real IP, but only knows to forward to Middle relay." to "Layer 3 stripped; Guard has no idea where packet is going",
        "Middle Relay ($middle): Peels off middle layer (Layer 2). Knows only Guard and Exit relays; knows neither your IP nor destination site." to "Layer 2 stripped; Middle knows neither origin nor destination",
        "Exit Relay ($exit): Peels off innermost layer (Layer 1). Delivers decrypted payload to target site; has zero knowledge of your IP." to "Layer 1 stripped; Exit sees site but not who you are",
        "Destination Website ($destination): Receives standard connection from Exit relay. Only Exit IP is visible in server logs." to "Payload delivered anonymously"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VeilBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Onion Routing Visualizer",
                    color = VeilTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Observe real-time 3-layer cryptographic peeling across Tor relays",
                    color = VeilTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { isAutoPlaying = !isAutoPlaying },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAutoPlaying) VeilRedDark else VeilSurfaceElevated,
                    contentColor = VeilTextPrimary
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isAutoPlaying) "Pause" else "Auto Play", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = {
                    currentStep = (currentStep + 1) % 5
                },
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = VeilTextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Step Hop", fontSize = 11.sp, color = VeilTextPrimary)
            }

            OutlinedButton(
                onClick = {
                    currentStep = 0
                    onRenewCircuit()
                },
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = VeilTextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Circuit", fontSize = 11.sp, color = VeilTextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // The Stage: 5 Relay Nodes with animated packet
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(VeilSurface)
                .border(1.dp, VeilBorder, RoundedCornerShape(6.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StageNode(
                    name = "You",
                    sub = "Local SOCKS5",
                    role = "ORIGIN",
                    isActive = currentStep == 0,
                    onClick = { currentStep = 0 }
                )
                WireConnector(currentStep >= 1)

                StageNode(
                    name = "Guard",
                    sub = guard,
                    role = "RELAY 1",
                    isActive = currentStep == 1,
                    onClick = { currentStep = 1 }
                )
                WireConnector(currentStep >= 2)

                StageNode(
                    name = "Middle",
                    sub = middle,
                    role = "RELAY 2",
                    isActive = currentStep == 2,
                    onClick = { currentStep = 2 }
                )
                WireConnector(currentStep >= 3)

                StageNode(
                    name = "Exit",
                    sub = exit,
                    role = "RELAY 3",
                    isActive = currentStep == 3,
                    onClick = { currentStep = 3 }
                )
                WireConnector(currentStep >= 4)

                StageNode(
                    name = "Site",
                    sub = destination,
                    role = "TARGET",
                    isActive = currentStep == 4,
                    onClick = { currentStep = 4 }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Multi-Layer Onion Packet Representation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(VeilSurfaceElevated)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Onion Packet Rings
                Box(
                    modifier = Modifier.size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentStep <= 0) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(VeilRedPrimary)
                        )
                    }
                    if (currentStep <= 1) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(VeilRedSecondary)
                        )
                    }
                    if (currentStep <= 2) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(VeilRedHighlight)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(VeilTextPrimary)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ENCRYPTION STATUS (Hop $currentStep of 4)",
                        color = VeilRedBright,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MonospaceFontFamily
                    )
                    Text(
                        text = captions[currentStep].first,
                        color = VeilTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = captions[currentStep].second,
                        color = VeilTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Who Sees What Breakdown Matrix
        Text(
            text = "WHO SEES WHAT (GUARANTEES)",
            color = VeilTextFaint,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MonospaceFontFamily
        )

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(VeilSurface)
                .border(1.dp, VeilBorder, RoundedCornerShape(6.dp))
        ) {
            MatrixHeader()
            MatrixRow("Guard Relay", yourIp = true, intermediate = false, targetSite = false, payload = false)
            MatrixRow("Middle Relay", yourIp = false, intermediate = false, targetSite = false, payload = false)
            MatrixRow("Exit Relay", yourIp = false, intermediate = false, targetSite = true, payload = true)
            MatrixRow("Target Website", yourIp = false, intermediate = false, targetSite = true, payload = true)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Live Simulated Cell Traffic Stream
        Text(
            text = "LIVE TOR RELAY TRAFFIC LOG",
            color = VeilTextFaint,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MonospaceFontFamily
        )

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(VeilSurface)
                .border(1.dp, VeilBorder, RoundedCornerShape(6.dp))
                .padding(10.dp)
        ) {
            liveLogs.take(5).forEach { log ->
                Text(
                    text = log,
                    color = if (log.contains("CREATE") || log.contains("negotiated")) VeilRedBright else VeilTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = MonospaceFontFamily,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StageNode(
    name: String,
    sub: String,
    role: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = role,
            color = if (isActive) VeilRedBright else VeilTextFaint,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MonospaceFontFamily
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (isActive) VeilRedDark else VeilSurfaceElevated)
                .border(2.dp, if (isActive) VeilRedBright else VeilBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                color = if (isActive) VeilTextPrimary else VeilTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = sub.take(12),
            color = VeilTextFaint,
            fontSize = 9.sp,
            fontFamily = MonospaceFontFamily
        )
    }
}

@Composable
private fun WireConnector(isLit: Boolean) {
    Box(
        modifier = Modifier
            .width(20.dp)
            .height(2.dp)
            .background(if (isLit) VeilRedBright else VeilBorderBright)
    )
}

@Composable
private fun MatrixHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VeilSurfaceElevated)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Observer", color = VeilTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.4f))
        Text("Your IP", color = VeilTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("Hops", color = VeilTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("Site", color = VeilTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("Payload", color = VeilTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MatrixRow(
    name: String,
    yourIp: Boolean,
    intermediate: Boolean,
    targetSite: Boolean,
    payload: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = VeilBorder.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = VeilTextPrimary, fontSize = 11.sp, modifier = Modifier.weight(1.4f))
        MatrixBadge(hasAccess = yourIp, modifier = Modifier.weight(1f))
        MatrixBadge(hasAccess = intermediate, modifier = Modifier.weight(1f))
        MatrixBadge(hasAccess = targetSite, modifier = Modifier.weight(1f))
        MatrixBadge(hasAccess = payload, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MatrixBadge(hasAccess: Boolean, modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        if (hasAccess) {
            Text("SEES", color = VeilRedBright, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = MonospaceFontFamily)
        } else {
            Text("HIDDEN", color = VeilTextFaint, fontSize = 10.sp, fontFamily = MonospaceFontFamily)
        }
    }
}
