package com.avnish.qrscan

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.avnish.qrscan.navigation.MainNavigation
import com.avnish.qrscan.ui.theme.QRScanTheme
import com.avnish.qrscan.ads.AdManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var doubleBackPressed = false
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ads in background
        lifecycleScope.launch {
            MobileAds.initialize(this@MainActivity)
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf("EA26CE6A96942E93B8F9032BDBFDA902"))
                    .build()
            )
        }

        // Initialize AdMob
        AdManager.initialize(this)

        setContent {
            QRScanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackPressed) {
                    finish()
                } else {
                    doubleBackPressed = true
                    Toast.makeText(
                        this@MainActivity,
                        "Press back again to exit",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }
}





