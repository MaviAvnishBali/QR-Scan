package com.avnish.qrscan.ads

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.datatransport.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"
    
    // Banner Ad Unit ID (using default banner size)
    private const val BANNER_AD_UNIT_ID = "ca-app-pub-6792593559558279/7821948023"
    
    // Interstitial Ad Unit ID
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-6792593559558279/6017431208"
    
    private const val PREFS_NAME = "qr_scan_prefs"
    private const val KEY_ADS_REMOVED = "ads_removed"
    private const val KEY_LAST_AD_SHOWN = "last_ad_shown"
    private const val AD_COOLDOWN = 30000L // 30 seconds cooldown between ads
    
    private var interstitialAd: InterstitialAd? = null
    private lateinit var prefs: SharedPreferences

    private val adRequest = AdRequest.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                // Add test device for debugging if needed
            }
        }
        .build()

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        MobileAds.initialize(context)
        loadInterstitialAd(context)
    }

    fun areAdsRemoved(): Boolean = prefs.getBoolean(KEY_ADS_REMOVED, false)

    fun setAdsRemoved(removed: Boolean) {
        prefs.edit().putBoolean(KEY_ADS_REMOVED, removed).apply()
    }

    private fun canShowAd(): Boolean {
        if (areAdsRemoved()) return false
        val lastAdShown = prefs.getLong(KEY_LAST_AD_SHOWN, 0)
        return System.currentTimeMillis() - lastAdShown >= AD_COOLDOWN
    }

    private fun updateLastAdShown() {
        prefs.edit().putLong(KEY_LAST_AD_SHOWN, System.currentTimeMillis()).apply()
    }

    fun createBannerAd(context: Context): AdView {
        return AdView(context).apply {
            setAdSize(AdSize.BANNER) // Using default banner size
            adUnitId = BANNER_AD_UNIT_ID
            loadAd(AdRequest.Builder().build())
        }
    }

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit = {}) {
        if (!canShowAd()) {
            onAdDismissed()
            return
        }
        
        interstitialAd?.show(activity) ?: run {
            loadInterstitialAd(activity)
            onAdDismissed()
        }
        updateLastAdShown()
    }

    @Composable
    fun BannerAd(
        modifier: Modifier = Modifier,
        adUnitId: String = BANNER_AD_UNIT_ID
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val adView = remember {
            AdView(context).apply {
                setAdSize(AdSize.BANNER) // Using default banner size
                this.adUnitId = adUnitId
                loadAd(adRequest)
            }
        }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> adView.pause()
                    Lifecycle.Event.ON_RESUME -> adView.resume()
                    Lifecycle.Event.ON_DESTROY -> adView.destroy()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                adView.destroy()
            }
        }

        AndroidView(
            factory = { adView },
            modifier = modifier
        )
    }

    fun cleanup() {
        interstitialAd = null
    }

    private fun loadInterstitialAd(context: Context) {
        if (areAdsRemoved()) return

        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial ad loaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }
} 