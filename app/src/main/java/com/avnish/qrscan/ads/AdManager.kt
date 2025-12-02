package com.avnish.qrscan.ads

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.window.layout.WindowMetricsCalculator
import com.avnish.qrscan.BuildConfig
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import timber.log.Timber

object AdManager {

    private const val TAG = "AdManager"

    // Test IDs
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

    // Real IDs
    private const val PROD_BANNER_ID = "ca-app-pub-6792593559558279/7425557891"
    private const val PROD_INTERSTITIAL_ID = "ca-app-pub-6792593559558279/6017431208"

    // Switching based on build type
    private val BANNER_ID = if (BuildConfig.DEBUG) TEST_BANNER_ID else PROD_BANNER_ID
    private val INTERSTITIAL_ID = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID

    private const val PREFS = "qr_scan_prefs"
    private const val KEY_ADS_REMOVED = "ads_removed"
    private const val KEY_LAST_AD = "last_ad_shown"
    private const val COOLDOWN = 30000L // 30 sec

    private var interstitialAd: InterstitialAd? = null
    private lateinit var prefs: SharedPreferences

    private val adRequest = AdRequest.Builder().build()

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        MobileAds.initialize(context)
        loadInterstitialAd(context)
    }

    fun areAdsRemoved() = prefs.getBoolean(KEY_ADS_REMOVED, false)

    fun setAdsRemoved(removed: Boolean) {
        prefs.edit().putBoolean(KEY_ADS_REMOVED, removed).apply()
    }

    private fun canShowAd(): Boolean {
        if (areAdsRemoved()) return false
        val last = prefs.getLong(KEY_LAST_AD, 0)
        return (System.currentTimeMillis() - last) >= COOLDOWN
    }

    private fun updateLastAd() {
        prefs.edit().putLong(KEY_LAST_AD, System.currentTimeMillis()).apply()
    }



    fun createBannerAd(context: Context): AdView {

        val windowMetrics = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(context as Activity)

        val widthPixels = windowMetrics.bounds.width().toFloat()
        val density = context.resources.displayMetrics.density
        
        // Account for horizontal padding (16dp on each side = 32dp total)
        val horizontalPaddingDp = 32
        val adWidth = ((widthPixels / density) - horizontalPaddingDp).toInt()

        val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            context,
            adWidth
        )

        return AdView(context).apply {
            setAdSize(adaptiveSize)
            adUnitId = BANNER_ID
            // Set proper layout parameters to prevent cropping
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            adListener = object : AdListener() {
                override fun onAdLoaded(){
                    Timber.tag(TAG).d("Banner loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError){
                    Timber.tag(TAG).e("Banner Error: %s", error.message)
                }

            }
            loadAd(adRequest)
        }
    }

    fun showInterstitialAd(activity: Activity, onDismiss: () -> Unit = {}) {
        if (!canShowAd()) {
            onDismiss()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            loadInterstitialAd(activity)
            onDismiss()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial dismissed")
                interstitialAd = null
                loadInterstitialAd(activity) // preload next
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Interstitial failed: ${error.message}")
                interstitialAd = null
                onDismiss()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial showed")
            }
        }

        ad.show(activity)
        updateLastAd()
    }

    fun cleanup() {
        interstitialAd = null
    }

    private fun loadInterstitialAd(context: Context) {
        if (areAdsRemoved()) return

        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.e(TAG, "Interstitial Error: ${error.message}")
                }
            }
        )
    }
}
