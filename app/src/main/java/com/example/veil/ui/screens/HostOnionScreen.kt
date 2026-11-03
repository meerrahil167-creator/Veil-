package com.example.veil.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.veil.engine.CommunityManager
import com.example.veil.model.HostedService
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
import com.example.veil.ui.theme.VeilSurfaceElevated
import com.example.veil.ui.theme.VeilTextFaint
import com.example.veil.ui.theme.VeilTextPrimary
import com.example.veil.ui.theme.VeilTextSecondary

@Composable
fun HostOnionScreen(
    communityManager: CommunityManager,
    hostedServices: List<HostedService>,
    onPreviewOnion: (String) -> Unit
) {
    val context = LocalContext.current
    var titleInput by remember { mutableStateOf("Veil Onion Site") }
    var contentInput by remember {
        mutableStateOf(
            "<h1>You made it.</h1>\n" +
            "<p>This page is hosted as a Tor onion service, published with one click from Veil.</p>\n" +
            "<p>Nobody on the path knows where it's hosted — and the host doesn't know who you are.</p>\n" +
            "<code>No servers. No domains. No IP addresses.</code>"
        )
    }
    var isInviteOnly by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VeilBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "One-Click Onion Hosting",
            color = VeilTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Publish web pages or encrypted notes as real v3 Tor onion services directly from your device.",
            color = VeilTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Creator Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(VeilSurface)
                .border(1.dp, VeilBorder, RoundedCornerShape(6.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "SERVICE CONFIGURATION",
                color = VeilTextFaint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MonospaceFontFamily
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                label = { Text("Service Title", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VeilRedBright,
                    unfocusedBorderColor = VeilBorder,
                    focusedContainerColor = VeilSurfaceElevated,
                    unfocusedContainerColor = VeilSurface
                ),
                textStyle = androidx.compose.ui.text.TextStyle(color = VeilTextPrimary, fontSize = 13.sp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = contentInput,
                onValueChange = { contentInput = it },
                label = { Text("HTML / Markdown Content", fontSize = 11.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VeilRedBright,
                    unfocusedBorderColor = VeilBorder,
                    focusedContainerColor = VeilSurfaceElevated,
                    unfocusedContainerColor = VeilSurface
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = VeilTextPrimary,
                    fontSize = 11.sp,
                    fontFamily = MonospaceFontFamily
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isInviteOnly,
                    onCheckedChange = { isInviteOnly = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = VeilRedBright,
                        uncheckedColor = VeilTextSecondary
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Invite-only (Tor v3 Client Authorization)",
                        color = VeilTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Generates an x25519 authorization key required to access this service",
                        color = VeilTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    communityManager.createHostedService(
                        title = titleInput,
                        content = contentInput,
                        inviteOnly = isInviteOnly
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VeilRedDark,
                    contentColor = VeilTextPrimary
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Publish,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publish Onion Service", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Active Hosted Services List
        Text(
            text = "ACTIVE ONION SERVICES (${hostedServices.size})",
            color = VeilTextFaint,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MonospaceFontFamily
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (hostedServices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(VeilSurface)
                    .border(1.dp, VeilBorder, RoundedCornerShape(6.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active onion services hosted. Create one above!",
                    color = VeilTextSecondary,
                    fontSize = 12.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                hostedServices.forEach { service ->
                    HostedServiceCard(
                        service = service,
                        onPreview = { onPreviewOnion(service.onionUrl) },
                        onStop = { communityManager.stopHostedService(service.serviceId) },
                        onCopy = { text ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Veil Onion", text))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HostedServiceCard(
    service: HostedService,
    onPreview: () -> Unit,
    onStop: () -> Unit,
    onCopy: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(VeilSurface)
            .border(1.dp, VeilBorderBright, RoundedCornerShape(6.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(VeilGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = service.title,
                    color = VeilTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onStop, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Stop",
                    tint = VeilTextFaint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = service.onionUrl,
            color = VeilRedBright,
            fontSize = 11.sp,
            fontFamily = MonospaceFontFamily
        )

        if (service.isInviteOnly && service.inviteKey != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(VeilSurfaceElevated)
                    .border(1.dp, VeilRedDark, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRIVATE INVITE KEY (x25519):",
                            color = VeilRedHighlight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MonospaceFontFamily
                        )
                        IconButton(
                            onClick = { onCopy(service.inviteKey) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = VeilTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Text(
                        text = service.inviteKey,
                        color = VeilTextSecondary,
                        fontSize = 9.sp,
                        fontFamily = MonospaceFontFamily
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onPreview,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VeilRedDark,
                    contentColor = VeilTextPrimary
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open in Browser", fontSize = 11.sp)
            }

            Button(
                onClick = { onCopy(service.onionUrl) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VeilSurfaceElevated,
                    contentColor = VeilTextPrimary
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Address", fontSize = 11.sp)
            }
        }
    }
}
