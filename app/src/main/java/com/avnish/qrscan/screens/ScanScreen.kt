package com.avnish.qrscan.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avnish.qrscan.component.BannerAdView
import com.avnish.qrscan.ads.AdManager
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
fun ScanScreen(
    onNavigateToGenerate: () -> Unit,
    onNavigateToInfo: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return
    var scanResult by remember { mutableStateOf<String?>(null) }
    var showRemoveAdsDialog by remember { mutableStateOf(false) }
    var showScanResult by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "QR Scanner",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (AdManager.areAdsRemoved()) {
                    startScanning(context) { result ->
                        scanResult = result
                        showScanResult = true
                        AdManager.showInterstitialAd(activity)
                    }
                } else {
                    showRemoveAdsDialog = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Scan QR Code")
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (showScanResult && scanResult != null) {
            ScanResultCard(scanResult!!) {
                showScanResult = false
            }
        }

        if (!AdManager.areAdsRemoved()) {
            Spacer(modifier = Modifier.height(16.dp))
            AdBanner()
        }
    }

    if (showRemoveAdsDialog) {
        RemoveAdsDialog(
            onDismiss = { showRemoveAdsDialog = false },
            onWatchVideo = {
                showRemoveAdsDialog = false
                AdManager.showRewardedInterstitialAd(
                    activity = activity,
                    onRewarded = {
                        AdManager.setAdsRemoved(true)
                        context.showToast("Ads removed for 24 hours!")
                    },
                    onAdDismissed = {
                        startScanning(context) { result ->
                            scanResult = result
                            showScanResult = true
                            AdManager.showInterstitialAd(activity)
                        }
                    }
                )
            },
            onSkip = {
                showRemoveAdsDialog = false
                startScanning(context) { result ->
                    scanResult = result
                    showScanResult = true
                    AdManager.showInterstitialAd(activity)
                }
            }
        )
    }
}

@Composable
private fun ScanResultCard(result: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Scan Result",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun AdBanner() {
    BannerAdView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        useLargeBanner = true
    )
}

@Composable
private fun RemoveAdsDialog(
    onDismiss: () -> Unit,
    onWatchVideo: () -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove Ads") },
        text = { Text("Watch a short video to remove ads for 24 hours.") },
        confirmButton = {
            TextButton(onClick = onWatchVideo) {
                Text("Watch Video")
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("Skip")
            }
        }
    )
}

private fun startScanning(context: Context, onResult: (String) -> Unit) {
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    val scanner = GmsBarcodeScanning.getClient(context, options)

    scanner.startScan()
        .addOnSuccessListener { barcode ->
            onResult(barcode.rawValue ?: "No data found")
        }
        .addOnFailureListener { e ->
            context.showToast("Scan failed: ${e.message}")
        }
}

private fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
