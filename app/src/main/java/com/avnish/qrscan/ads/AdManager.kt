package com.avnish.qrscan.ads

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
    
    // Test Ad Unit IDs for development
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    
    // Production Ad Unit IDs
    private const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-6792593559558279/7425557891"
    private const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-6792593559558279/6017431208"
    
    // Use test ads in debug mode, production ads in release mode
    private val BANNER_AD_UNIT_ID = if (BuildConfig.DEBUG) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID
    private val INTERSTITIAL_AD_UNIT_ID = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID
    
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
            // Use adaptive banner to avoid resizing warnings
            val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, AdSize.FULL_WIDTH)
            setAdSize(adSize)
            adUnitId = BANNER_AD_UNIT_ID
            
            // Add ad listener for better error handling
            adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdLoaded() {
                    Log.d(TAG, "Banner ad loaded successfully")
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Banner ad failed to load: ${loadAdError.code} - ${loadAdError.message}")
                }
                
                override fun onAdOpened() {
                    Log.d(TAG, "Banner ad opened")
                }
                
                override fun onAdClosed() {
                    Log.d(TAG, "Banner ad closed")
                }
            }
            
            loadAd(adRequest)
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
                    Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.code} - ${loadAdError.message}")
                }
            }
        )
    }
} 