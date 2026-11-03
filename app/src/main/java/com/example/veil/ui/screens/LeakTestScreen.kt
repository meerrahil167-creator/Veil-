package com.example.veil.ui.screens

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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.veil.engine.LeakTestEngine
import com.example.veil.model.LeakTestItem
import com.example.veil.model.TestStatus
import com.example.veil.ui.theme.MonospaceFontFamily
import com.example.veil.ui.theme.VeilBackground
import com.example.veil.ui.theme.VeilBorder
import com.example.veil.ui.theme.VeilGreen
import com.example.veil.ui.theme.VeilRedBright
import com.example.veil.ui.theme.VeilRedDark
import com.example.veil.ui.theme.VeilRedPrimary
import com.example.veil.ui.theme.VeilSurface
import com.example.veil.ui.theme.VeilSurfaceElevated
import com.example.veil.ui.theme.VeilTextFaint
import com.example.veil.ui.theme.VeilTextPrimary
import com.example.veil.ui.theme.VeilTextSecondary
import kotlinx.coroutines.launch

@Composable
fun LeakTestScreen(
    leakTestEngine: LeakTestEngine,
    networkTests: List<LeakTestItem>,
    fingerprintTests: List<LeakTestItem>,
    isRunning: Boolean
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VeilBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Leak Test",
            color = VeilTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Checks what the network and websites can learn about you. Fingerprint values below are exactly what every website sees.",
            color = VeilTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    leakTestEngine.runNetworkTest()
                }
            },
            enabled = !isRunning,
            colors = ButtonDefaults.buttonColors(
                containerColor = VeilRedDark,
                contentColor = VeilTextPrimary,
                disabledContainerColor = VeilSurfaceElevated,
                disabledContentColor = VeilTextFaint
            ),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = VeilRedBright,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Testing via Tor relays...", fontSize = 12.sp)
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Run Network Isolation Test", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Network Section
        Text(
            text = "NETWORK ANONYMITY CHECKS",
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
            if (networkTests.isEmpty()) {
                Text(
                    text = "Tap 'Run Network Isolation Test' above to verify Tor exit nodes and DNS leakage.",
                    color = VeilTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(14.dp)
                )
            } else {
                networkTests.forEach { test ->
                    LeakTestRow(test = test)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fingerprint Section
        Text(
            text = "FINGERPRINT RESISTANCE (WHAT WEBSITES SEE)",
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
            fingerprintTests.forEach { test ->
                LeakTestRow(test = test)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LeakTestRow(test: LeakTestItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = VeilBorder.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (badgeText, badgeColor) = when (test.status) {
            TestStatus.PASS -> "[PASS]" to VeilGreen
            TestStatus.FAIL -> "[FAIL]" to VeilRedBright
            TestStatus.PENDING -> "[....]" to VeilTextFaint
        }

        Text(
            text = badgeText,
            color = badgeColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MonospaceFontFamily
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = test.title,
                color = VeilTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = test.detail,
                color = VeilTextSecondary,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = test.value,
            color = VeilRedBright,
            fontSize = 10.sp,
            fontFamily = MonospaceFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}
