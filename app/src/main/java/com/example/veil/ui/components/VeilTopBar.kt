package com.example.veil.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.veil.model.BrowserTab
import com.example.veil.model.SecurityLevel
import com.example.veil.model.TorState
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

@Composable
fun VeilTopBar(
    tabs: List<BrowserTab>,
    activeTab: BrowserTab?,
    torState: TorState,
    securityLevel: SecurityLevel,
    isBookmarked: Boolean,
    onTabSelect: (String) -> Unit,
    onTabClose: (String) -> Unit,
    onNewTab: () -> Unit,
    onAddressSubmit: (String) -> Unit,
    onSecurityLevelChange: (SecurityLevel) -> Unit,
    onToggleBookmark: () -> Unit,
    onCircuitClick: () -> Unit,
    onNewIdentityClick: () -> Unit,
    onPanicClick: () -> Unit,
    onReload: () -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit = {},
    canGoBack: Boolean = true,
    canGoForward: Boolean = false
) {
    var addressInput by remember(activeTab?.url) {
        mutableStateOf(activeTab?.url ?: "")
    }
    var showSecurityMenu by remember { mutableStateOf(false) }

    Surface(
        color = VeilSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = VeilBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VeilBackground)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { tab ->
                        val isActive = tab.id == activeTab?.id
                        TabChip(
                            tab = tab,
                            isActive = isActive,
                            onClick = { onTabSelect(tab.id) },
                            onClose = { onTabClose(tab.id) }
                        )
                    }

                    IconButton(
                        onClick = onNewTab,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("new_tab_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Tab",
                            tint = VeilTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Tor Status Indicator
                TorStatusBadge(torState = torState)
            }

            // Navigation & Address Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    enabled = canGoBack,
                    modifier = Modifier.size(32.dp).testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (canGoBack) VeilTextPrimary else VeilTextFaint,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onForward,
                    enabled = canGoForward,
                    modifier = Modifier.size(32.dp).testTag("forward_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (canGoForward) VeilTextPrimary else VeilTextFaint,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onReload,
                    modifier = Modifier.size(32.dp).testTag("reload_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = VeilTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Address Bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(VeilSurfaceElevated)
                        .border(1.dp, VeilBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isHttps = activeTab?.url?.startsWith("https://") == true
                        val isOnion = activeTab?.url?.contains(".onion") == true
                        Icon(
                            imageVector = if (isOnion) Icons.Default.Shield else Icons.Default.Lock,
                            contentDescription = "Connection Security",
                            tint = if (isOnion) VeilRedBright else if (isHttps) VeilGreen else VeilTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        BasicTextField(
                            value = addressInput,
                            onValueChange = { addressInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("address_input"),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = VeilTextPrimary,
                                fontSize = 12.sp,
                                fontFamily = MonospaceFontFamily
                            ),
                            cursorBrush = SolidColor(VeilRedBright),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = { onAddressSubmit(addressInput) }),
                            decorationBox = { innerTextField ->
                                if (addressInput.isEmpty()) {
                                    Text(
                                        text = "Search privately or type an address",
                                        color = VeilTextFaint,
                                        fontSize = 11.sp
                                    )
                                }
                                innerTextField()
                            }
                        )

                        // .onion available badge
                        if (activeTab?.hasOnionAlternative == true && activeTab.onionAlternativeUrl != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(VeilRedDark)
                                    .clickable { onAddressSubmit(activeTab.onionAlternativeUrl) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = ".onion available",
                                    color = VeilRedBright,
                                    fontSize = 9.sp,
                                    fontFamily = MonospaceFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        // Bookmark star
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier.size(22.dp).testTag("bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) VeilRedBright else VeilTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Security Level Selector
                Box {
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(VeilSurfaceElevated)
                            .border(1.dp, VeilBorder, RoundedCornerShape(3.dp))
                            .clickable { showSecurityMenu = true }
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = securityLevel.label,
                            color = if (securityLevel == SecurityLevel.SAFEST) VeilRedBright else VeilTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    DropdownMenu(
                        expanded = showSecurityMenu,
                        onDismissRequest = { showSecurityMenu = false }
                    ) {
                        SecurityLevel.entries.forEach { level ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(level.label, fontWeight = FontWeight.Bold, color = VeilTextPrimary)
                                        Text(level.description, fontSize = 10.sp, color = VeilTextSecondary)
                                    }
                                },
                                onClick = {
                                    onSecurityLevelChange(level)
                                    showSecurityMenu = false
                                }
                            )
                        }
                    }
                }

                // Circuit button
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(VeilSurfaceElevated)
                        .border(1.dp, VeilBorder, RoundedCornerShape(3.dp))
                        .clickable(onClick = onCircuitClick)
                        .padding(horizontal = 6.dp)
                        .testTag("circuit_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Circuit",
                        color = VeilTextPrimary,
                        fontSize = 11.sp
                    )
                }

                // New Identity button
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(VeilSurfaceElevated)
                        .border(1.dp, VeilBorder, RoundedCornerShape(3.dp))
                        .clickable(onClick = onNewIdentityClick)
                        .padding(horizontal = 6.dp)
                        .testTag("new_identity_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "New ID",
                        color = VeilTextPrimary,
                        fontSize = 11.sp
                    )
                }

                // Panic button
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(VeilRedPrimary)
                        .clickable(onClick = onPanicClick)
                        .padding(horizontal = 8.dp)
                        .testTag("panic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PANIC",
                        color = VeilTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Loading Progress Bar
            if (activeTab?.isLoading == true) {
                LinearProgressIndicator(
                    progress = { activeTab.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = VeilRedBright,
                    trackColor = VeilBackground
                )
            }
        }
    }
}

@Composable
private fun TabChip(
    tab: BrowserTab,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    val bg = if (isActive) VeilSurfaceElevated else VeilSurface
    val borderCol = if (isActive) VeilRedPrimary else VeilBorder

    Row(
        modifier = Modifier
            .height(26.dp)
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (tab.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(10.dp),
                color = VeilRedBright,
                strokeWidth = 1.dp
            )
        }
        Text(
            text = tab.title,
            color = if (isActive) VeilTextPrimary else VeilTextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 110.dp)
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close tab",
                tint = VeilTextFaint,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

@Composable
private fun TorStatusBadge(torState: TorState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(VeilSurfaceElevated)
            .border(1.dp, VeilBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        when (torState) {
            is TorState.Bootstrapping -> {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(VeilRedHighlight)
                )
                Text(
                    text = "tor: ${torState.percent}%",
                    color = VeilTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = MonospaceFontFamily
                )
            }
            is TorState.Connected -> {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(VeilGreen)
                )
                Text(
                    text = "tor: ready",
                    color = VeilRedBright,
                    fontSize = 10.sp,
                    fontFamily = MonospaceFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
            is TorState.Error -> {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(VeilRedPrimary)
                )
                Text(
                    text = "tor: error",
                    color = VeilRedPrimary,
                    fontSize = 10.sp,
                    fontFamily = MonospaceFontFamily
                )
            }
        }
    }
}
