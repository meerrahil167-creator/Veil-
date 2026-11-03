package com.example.veil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.veil.model.BridgeType
import com.example.veil.model.CommunitySite
import com.example.veil.model.TorState
import com.example.veil.model.VeilScreen
import com.example.veil.ui.theme.CreepsterFontFamily
import com.example.veil.ui.theme.MonospaceFontFamily
import com.example.veil.ui.theme.VeilBackground
import com.example.veil.ui.theme.VeilBorder
import com.example.veil.ui.theme.VeilBorderBright
import com.example.veil.ui.theme.VeilGreen
import com.example.veil.ui.theme.VeilRedBright
import com.example.veil.ui.theme.VeilRedDark
import com.example.veil.ui.theme.VeilRedHighlight
import com.example.veil.ui.theme.VeilRedPrimary
import com.example.veil.ui.theme.VeilSurface
import com.example.veil.ui.theme.VeilSurfaceCard
import com.example.veil.ui.theme.VeilSurfaceElevated
import com.example.veil.ui.theme.VeilTextFaint
import com.example.veil.ui.theme.VeilTextPrimary
import com.example.veil.ui.theme.VeilTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HubScreen(
    torState: TorState,
    activeBridge: BridgeType,
    communitySites: List<CommunitySite>,
    onSearch: (String) -> Unit,
    onNavigate: (VeilScreen) -> Unit,
    onSelectSite: (String) -> Unit,
    onSetBridge: (BridgeType) -> Unit,
    onJoinInvite: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var inviteInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VeilBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Iconic VEIL Logo in Creepster font
        Text(
            text = buildAnnotatedString {
                append("VE")
                withStyle(SpanStyle(color = VeilRedBright)) {
                    append("I")
                }
                append("L")
            },
            fontFamily = CreepsterFontFamily,
            fontSize = 72.sp,
            color = VeilTextPrimary,
            letterSpacing = 0.08.sp
        )

        Text(
            text = "every site isolated · every request through tor · nothing kept",
            fontFamily = MonospaceFontFamily,
            fontSize = 11.sp,
            color = VeilTextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Status badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(VeilSurfaceElevated)
                .border(1.dp, VeilBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (torState is TorState.Connected) VeilGreen else VeilRedBright)
            )
            val statusText = when (torState) {
                is TorState.Connected -> "tor: ready (${torState.circuitsCount} circuits active)"
                is TorState.Bootstrapping -> "tor: bootstrapping ${torState.percent}% (${torState.step})"
                is TorState.Error -> "tor: disconnected"
            }
            Text(
                text = statusText,
                color = if (torState is TorState.Connected) VeilRedBright else VeilTextSecondary,
                fontSize = 11.sp,
                fontFamily = MonospaceFontFamily
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search Form (DuckDuckGo Onion)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("hub_search_input"),
                placeholder = {
                    Text(
                        "Search DuckDuckGo onion or type .onion address...",
                        fontSize = 12.sp,
                        color = VeilTextFaint
                    )
                },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = VeilTextPrimary,
                    fontSize = 13.sp,
                    fontFamily = MonospaceFontFamily
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VeilRedBright,
                    unfocusedBorderColor = VeilBorder,
                    focusedContainerColor = VeilSurfaceElevated,
                    unfocusedContainerColor = VeilSurface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(searchQuery) })
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { onSearch(searchQuery) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VeilRedDark,
                    contentColor = VeilTextPrimary
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("hub_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Feature Action Cards
        Text(
            text = "VEIL SECURITY SUITE",
            color = VeilTextFaint,
            fontSize = 10.sp,
            fontFamily = MonospaceFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 2
        ) {
            FeatureCard(
                title = "Onion Routing",
                desc = "Watch live multi-layer encryption peeling",
                icon = Icons.Default.Timeline,
                onClick = { onNavigate(VeilScreen.ROUTING_VISUALIZER) },
                modifier = Modifier.weight(1f)
            )

            FeatureCard(
                title = "Leak Test",
                desc = "Audit IP, DNS, and 13 fingerprint defenses",
                icon = Icons.Default.Shield,
                onClick = { onNavigate(VeilScreen.LEAK_TEST) },
                modifier = Modifier.weight(1f)
            )

            FeatureCard(
                title = "Community",
                desc = "Signed onion directory & invites",
                icon = Icons.Default.Language,
                onClick = { onNavigate(VeilScreen.COMMUNITY) },
                modifier = Modifier.weight(1f)
            )

            FeatureCard(
                title = "Ephemeral Host",
                desc = "One-click onion service publishing",
                icon = Icons.Default.CloudUpload,
                onClick = { onNavigate(VeilScreen.HOST_ONION) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tor Bridges Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(VeilSurface)
                .border(1.dp, VeilBorder, RoundedCornerShape(6.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOR BRIDGES",
                    color = VeilTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MonospaceFontFamily
                )
                Text(
                    text = "Bypass ISP Censorship",
                    color = VeilTextFaint,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BridgeType.entries.take(4).forEach { bridge ->
                    val isSelected = activeBridge == bridge
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) VeilRedDark else VeilSurfaceElevated)
                            .border(1.dp, if (isSelected) VeilRedBright else VeilBorder, RoundedCornerShape(4.dp))
                            .clickable { onSetBridge(bridge) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = bridge.label,
                            color = if (isSelected) VeilTextPrimary else VeilTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Verified Community Sites
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(VeilSurface)
                .border(1.dp, VeilBorder, RoundedCornerShape(6.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VeilSurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VERIFIED ONION SITES",
                    color = VeilTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MonospaceFontFamily
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = VeilRedBright,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ed25519 Signed",
                        color = VeilRedBright,
                        fontSize = 10.sp,
                        fontFamily = MonospaceFontFamily
                    )
                }
            }

            communitySites.forEach { site ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSite(site.url) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .border(width = 0.5.dp, color = VeilBorder.copy(alpha = 0.5f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = site.name,
                            color = VeilRedHighlight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = site.description,
                            color = VeilTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = site.url,
                            color = VeilRedPrimary,
                            fontSize = 10.sp,
                            fontFamily = MonospaceFontFamily
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Self-Test Quick Link
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(VeilSurfaceElevated)
                .border(1.dp, VeilBorderBright, RoundedCornerShape(6.dp))
                .clickable { onNavigate(VeilScreen.SELF_TEST) }
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = VeilRedBright,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Run Anonymity Self-Test (37 Checks)",
                        color = VeilTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mathematically prove isolation and anonymity guarantees against live Tor relays",
                        color = VeilTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FeatureCard(
    title: String,
    desc: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(VeilSurface)
            .border(1.dp, VeilBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VeilRedBright,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = VeilTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = desc,
            color = VeilTextSecondary,
            fontSize = 11.sp,
            lineHeight = 14.sp
        )
    }
}
