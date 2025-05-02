package com.avnish.qrscan.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avnish.qrscan.ads.BannerAdView
import com.avnish.qrscan.ads.AdManager

@Composable
fun InfoScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToGenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "About QR Scan",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "QR Scan is a simple and easy-to-use QR code scanner and generator. " +
                   "You can scan QR codes with your camera or generate new QR codes with text or URLs.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (!AdManager.areAdsRemoved()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            BannerAdView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                useLargeBanner = true
            )
        }
    }
} 