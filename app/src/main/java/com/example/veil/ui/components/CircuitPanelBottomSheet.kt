package com.example.veil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.veil.model.Circuit
import com.example.veil.model.HopRole
import com.example.veil.model.RelayHop
import com.example.veil.ui.theme.MonospaceFontFamily
import com.example.veil.ui.theme.VeilBackground
import com.example.veil.ui.theme.VeilBorder
import com.example.veil.ui.theme.VeilBorderBright
import com.example.veil.ui.theme.VeilRedBright
import com.example.veil.ui.theme.VeilRedDark
import com.example.veil.ui.theme.VeilRedPrimary
import com.example.veil.ui.theme.VeilSurface
import com.example.veil.ui.theme.VeilSurfaceElevated
import com.example.veil.ui.theme.VeilTextFaint
import com.example.veil.ui.theme.VeilTextPrimary
import com.example.veil.ui.theme.VeilTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircuitPanelBottomSheet(
    circuit: Circuit?,
    onDismiss: () -> Unit,
    onSeeRouting: () -> Unit,
    onNewCircuit: () -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VeilSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tor Circuit for this site",
                    color = VeilTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = VeilTextSecondary
                    )
                }
            }

            Text(
                text = circuit?.domain ?: "Active Session",
                color = VeilRedBright,
                fontSize = 13.sp,
                fontFamily = MonospaceFontFamily,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
            )

            // 3-hop list
            val hops = circuit?.hops ?: emptyList()
            if (hops.isNotEmpty()) {
                hops.forEachIndexed { index, hop ->
                    HopRowItem(hop = hop, isFirst = index == 0, isLast = index == hops.size - 1)
                    if (index < hops.size - 1) {
                        Box(
                            modifier = Modifier
                                .padding(start = 17.dp)
                                .width(2.dp)
                                .height(14.dp)
                                .background(VeilBorderBright)
                        )
                    }
                }
            } else {
                Text(
                    text = "Internal browser page (circuit not required)",
                    color = VeilTextFaint,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Explanation box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(VeilSurfaceElevated)
                    .border(1.dp, VeilBorder, RoundedCornerShape(4.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "• Guard knows your IP, not where you're going.",
                    color = VeilTextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "• Middle knows neither.",
                    color = VeilTextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "• Exit knows the site, not who you are.",
                    color = VeilTextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "• Each site gets its own circuit, so sites can't link you.",
                    color = VeilRedBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSeeRouting,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VeilTextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(VeilBorder))
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("See Routing", fontSize = 12.sp)
                }

                Button(
                    onClick = onNewCircuit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VeilRedDark,
                        contentColor = VeilTextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Circuit", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun HopRowItem(hop: RelayHop, isFirst: Boolean, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(VeilSurfaceElevated)
            .border(1.dp, VeilBorder, RoundedCornerShape(6.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val badgeColor = when (hop.role) {
            HopRole.GUARD -> VeilRedBright
            HopRole.MIDDLE -> VeilTextSecondary
            HopRole.EXIT -> VeilRedPrimary
        }
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(VeilBackground)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${hop.role.name}: ",
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MonospaceFontFamily
                )
                Text(
                    text = hop.nickname,
                    color = VeilTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "${hop.countryName} (${hop.countryCode}) • IP: ${hop.ipFingerprint} • ${hop.bandwidth}",
                color = VeilTextSecondary,
                fontSize = 10.sp,
                fontFamily = MonospaceFontFamily
            )
        }
    }
}
