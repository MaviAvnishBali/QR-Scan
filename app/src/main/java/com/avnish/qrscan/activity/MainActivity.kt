package com.avnish.qrscan.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.avnish.qrscan.navigation.MainNavigation
import com.avnish.qrscan.ui.theme.QRScanTheme
import com.avnish.qrscan.ads.AdManager
import com.avnish.qrscan.updates.UpdateManager
import com.avnish.qrscan.updates.UpdateDialog
import com.avnish.qrscan.updates.UpdateSnackbar
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var showUpdateDialog by mutableStateOf(false)
    private var showUpdateSnackbar by mutableStateOf(false)
    private var isImmediateUpdate by mutableStateOf(false)
    private var isFlexibleUpdateReady by mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        var doubleBackPressed = false
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QRScanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        onShowImmediateUpdate = { showImmediateUpdateDialog() },
                        onShowFlexibleUpdate = { showFlexibleUpdateDialog() }
                    )
                    
                    // Update Dialog
                    UpdateDialog(
                        isVisible = showUpdateDialog,
                        isImmediateUpdate = isImmediateUpdate,
                        isFlexibleUpdateReady = isFlexibleUpdateReady,
                        onUpdateNow = {
                            if (isImmediateUpdate) {
                                UpdateManager.showImmediateUpdate(this@MainActivity)
                            } else {
                                UpdateManager.showFlexibleUpdate(this@MainActivity)
                            }
                            showUpdateDialog = false
                        },
                        onUpdateLater = {
                            showUpdateDialog = false
                            // Show snackbar for flexible updates
                            if (!isImmediateUpdate) {
                                showUpdateSnackbar = true
                            }
                        },
                        onDismiss = {
                            showUpdateDialog = false
                        }
                    )
                    
                    // Update Snackbar
                    UpdateSnackbar(
                        isVisible = showUpdateSnackbar,
                        isFlexibleUpdateReady = isFlexibleUpdateReady,
                        onInstallNow = {
                            UpdateManager.completeFlexibleUpdate(this@MainActivity)
                            showUpdateSnackbar = false
                        },
                        onDismiss = {
                            showUpdateSnackbar = false
                        }
                    )
                }
            }
        }

        // Initialize ads in background
        lifecycleScope.launch {
            MobileAds.initialize(this@MainActivity)
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(
                        "EA26CE6A96942E93B8F9032BDBFDA902",
                        "CD9DD1D29AFE18C21DB59071282EA6E1"
                    ))
                    .build()
            )
        }

        // Initialize AdMob
        AdManager.initialize(this)
        
        // Initialize Update Manager
        UpdateManager.initialize(this)

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
    
    override fun onResume() {
        super.onResume()
        UpdateManager.onResume(this)
        
        // Check if flexible update is ready
        isFlexibleUpdateReady = UpdateManager.isFlexibleUpdateReady()
        if (isFlexibleUpdateReady) {
            showUpdateSnackbar = true
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UpdateManager.onActivityResult(requestCode, resultCode, data)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        UpdateManager.cleanup()
    }
    
    // Public methods to trigger updates from other parts of the app
    fun showImmediateUpdateDialog() {
        isImmediateUpdate = true
        showUpdateDialog = true
    }
    
    fun showFlexibleUpdateDialog() {
        isImmediateUpdate = false
        showUpdateDialog = true
    }
}





