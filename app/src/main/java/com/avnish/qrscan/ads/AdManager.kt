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
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"
    // Banner Ad Unit IDs
    private const val BANNER_AD_UNIT_ID = "ca-app-pub-6792593559558279/7821948023"
    private const val LARGE_BANNER_AD_UNIT_ID = "ca-app-pub-6792593559558279/7821948023"
    
    // Interstitial Ad Unit IDs
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-6792593559558279/6017431208"
    private const val REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-6792593559558279/8514993225"
    
    // Rewarded Ad Unit ID
    private const val REWARDED_AD_UNIT_ID = "ca-app-pub-6792593559558279/7820858657"
    
    private const val PREFS_NAME = "qr_scan_prefs"
    private const val KEY_ADS_REMOVED = "ads_removed"
    private const val KEY_LAST_AD_SHOWN = "last_ad_shown"
    private const val AD_COOLDOWN = 30000L // 30 seconds cooldown between ads
    
    private var interstitialAd: InterstitialAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private lateinit var prefs: SharedPreferences
    private const val TEST_DEVICE_ID = "ca-app-pub-6792593559558279/7821948023"

    private val adRequest = AdRequest.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
//                addTestDevice(TEST_DEVICE_ID)
            }
        }
        .build()

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        MobileAds.initialize(context)
        loadAllAds(context)
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
            setAdSize(AdSize.BANNER)
            adUnitId = BANNER_AD_UNIT_ID
            loadAd(AdRequest.Builder().build())
        }
    }

    fun createLargeBannerAd(context: Context): AdView {
        return AdView(context).apply {
            setAdSize(AdSize.LARGE_BANNER)
            adUnitId = LARGE_BANNER_AD_UNIT_ID
            loadAd(AdRequest.Builder().build())
        }
    }

    private fun loadAllAds(context: Context) {
        loadInterstitialAd(context)
        loadRewardedInterstitialAd(context)
        loadRewardedAd(context)
    }


    private fun loadRewardedInterstitialAd(context: Context) {
        if (areAdsRemoved()) return
        
        RewardedInterstitialAd.load(
            context,
            REWARDED_INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedInterstitialAd = null
                }
            }
        )
    }

    private fun loadRewardedAd(context: Context) {
        if (areAdsRemoved()) return
        
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
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

    fun showRewardedInterstitialAd(activity: Activity, onRewarded: () -> Unit, onAdDismissed: () -> Unit = {}) {
        if (!canShowAd()) {
            onAdDismissed()
            return
        }
        
        rewardedInterstitialAd?.show(activity) { rewardItem ->
            onRewarded()
        } ?: run {
            loadRewardedInterstitialAd(activity)
            onAdDismissed()
        }
        updateLastAdShown()
    }

    fun showRewardedAd(activity: Activity, onRewarded: () -> Unit, onAdDismissed: () -> Unit = {}) {
        if (!canShowAd()) {
            onAdDismissed()
            return
        }
        
        rewardedAd?.show(activity) { rewardItem ->
            onRewarded()
        } ?: run {
            loadRewardedAd(activity)
            onAdDismissed()
        }
        updateLastAdShown()
    }

    @Composable
    fun BannerAd(
        modifier: Modifier = Modifier,
        adUnitId: String = BANNER_AD_UNIT_ID // Use your actual ad unit ID
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val adView = remember {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId // Fixed: Actually use the adUnitId parameter
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
                adView.destroy() // Ensure proper cleanup
            }
        }

        AndroidView(
            factory = { adView },
            modifier = modifier
        )
    }

    // Add this cleanup function to your AdManager
    fun cleanup() {
        interstitialAd = null
        rewardedInterstitialAd = null
        rewardedAd = null
    }

    // Improved error handling in ad loading callbacks
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