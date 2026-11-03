package com.example.veil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.veil.model.CommunitySite
import com.example.veil.model.PrivateInvite
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
fun CommunityScreen(
    sites: List<CommunitySite>,
    joinedInvites: List<PrivateInvite>,
    directoryFingerprint: String,
    onSelectSite: (String) -> Unit,
    onJoinInvite: (String) -> Unit,
    onRemoveInvite: (String) -> Unit
) {
    var inviteInput by remember { mutableStateOf("") }
    var inviteError by remember { mutableStateOf<String?>(null) }

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
                    text = "Veil Community Network",
                    color = VeilTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cryptographically signed directory & invite-only Tor hidden services",
                    color = VeilTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Signature Status Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(VeilSurfaceElevated)
                .border(1.dp, VeilBorderBright, RoundedCornerShape(6.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Verified",
                tint = VeilGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Directory Signature Verified (Ed25519)",
                    color = VeilTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Key fingerprint: $directoryFingerprint • Tamper-proof",
                    color = VeilTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = MonospaceFontFamily
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Join Invite-Only Onion Section
        Text(
            text = "JOIN INVITE-ONLY ONION SERVICE",
            color = VeilTextFaint,
            fontSize = 11.sp,
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
                .padding(14.dp)
        ) {
            Text(
                text = "Enter a Veil invite key (x25519 client authorization) provided by a private onion operator:",
                color = VeilTextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = inviteInput,
                onValueChange = {
                    inviteInput = it
                    inviteError = null
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "veil-invite:<56-char-onion>.onion:<x25519-key>",
                        fontSize = 10.sp,
                        fontFamily = MonospaceFontFamily,
                        color = VeilTextFaint
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = VeilTextPrimary,
                    fontSize = 11.sp,
                    fontFamily = MonospaceFontFamily
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VeilRedBright,
                    unfocusedBorderColor = VeilBorder,
                    focusedContainerColor = VeilSurfaceElevated,
                    unfocusedContainerColor = VeilSurface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (inviteInput.isNotBlank()) {
                        onJoinInvite(inviteInput)
                        inviteInput = ""
                    }
                })
            )

            if (inviteError != null) {
                Text(
                    text = inviteError ?: "",
                    color = VeilRedBright,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (inviteInput.isNotBlank()) {
                        onJoinInvite(inviteInput)
                        inviteInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VeilRedDark,
                    contentColor = VeilTextPrimary
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Authorize & Unlock Service", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Joined Invites List
        if (joinedInvites.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "YOUR ACTIVE PRIVATE AUTHORIZATIONS",
                color = VeilTextFaint,
                fontSize = 11.sp,
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
                joinedInvites.forEach { invite ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = VeilRedBright, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = invite.url,
                                color = VeilTextPrimary,
                                fontSize = 12.sp,
                                fontFamily = MonospaceFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Client Auth: x25519:${invite.clientKey.take(16)}...",
                                color = VeilTextSecondary,
                                fontSize = 10.sp,
                                fontFamily = MonospaceFontFamily
                            )
                        }
                        IconButton(onClick = { onSelectSite(invite.url) }) {
                            Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = "Open", tint = VeilTextPrimary)
                        }
                        IconButton(onClick = { onRemoveInvite(invite.serviceId) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = VeilTextFaint)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Verified Onion Sites Directory
        Text(
            text = "VERIFIED COMMUNITY SITES (${sites.size})",
            color = VeilTextFaint,
            fontSize = 11.sp,
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
            sites.forEach { site ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSite(site.url) }
                        .border(width = 0.5.dp, color = VeilBorder.copy(alpha = 0.5f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = site.name,
                            color = VeilRedHighlight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = site.description,
                            color = VeilTextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = site.url,
                            color = VeilRedPrimary,
                            fontSize = 10.sp,
                            fontFamily = MonospaceFontFamily
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = "Visit Site",
                        tint = VeilTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
