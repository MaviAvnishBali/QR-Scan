package com.avnish.qrscan.ads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdView

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    useLargeBanner: Boolean = true
) {
    val context = LocalContext.current
    var adView by remember { mutableStateOf<AdView?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        adView = if (useLargeBanner) {
            AdManager.createLargeBannerAd(context)
        } else {
            AdManager.createBannerAd(context)
        }
        isLoading = false
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (adView != null) {
            AndroidView(
                factory = { adView!! },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            adView?.destroy()
        }
    }
}
