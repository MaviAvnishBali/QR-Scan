package com.avnish.qrscan.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.avnish.qrscan.BuildConfig
import com.avnish.qrscan.MainActivity
import com.avnish.qrscan.ui.theme.QRScanTheme
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import timber.log.Timber

class SplashActivity : ComponentActivity() {

    private val updateManager by lazy { AppUpdateManagerFactory.create(this) }
    private val remoteConfig: FirebaseRemoteConfig by lazy { Firebase.remoteConfig }

    private val updateResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Timber.w("Update flow failed or was canceled")
        }
        proceedToMain()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setupRemoteConfig()
        initializeLogging()

        setContent {
            QRScanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    SplashScreen(
                        onSplashFinished = ::checkForUpdates
                    )
                }
            }
        }
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun setupRemoteConfig() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600 // 1 hour in prod
        }

        remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(
                mapOf(
                    "force_update" to false, "min_version_code" to 1
                )
            )
        }
    }

    private fun checkForUpdates() {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val forceUpdate = remoteConfig.getBoolean("force_update")
                    val minVersionCode = remoteConfig.getLong("min_version_code").toInt()

                    // Proper way to access version code
                    if (BuildConfig.VERSION_CODE < minVersionCode) {
                        showCriticalUpdateDialog()
                    } else {
                        checkAppUpdateAvailability(forceUpdate)
                    }
                } else {
                    Timber.e(task.exception, "Remote config fetch failed")
                    proceedToMain()
                }
            }
    }

    private fun checkAppUpdateAvailability(forceUpdate: Boolean) {
        updateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                when {
                    appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> {
                        startUpdateFlow(appUpdateInfo, forceUpdate)
                    }

                    appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        // If an update is already in progress, resume it
                        startUpdateFlow(appUpdateInfo, forceUpdate)
                    }

                    else -> proceedToMain()
                }
            }.addOnFailureListener { e ->
                Timber.e(e, "Failed to check for updates")
                proceedToMain()
            }
    }

    private fun startUpdateFlow(appUpdateInfo: AppUpdateInfo, forceUpdate: Boolean) {
        val updateType = if (forceUpdate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE

        if (appUpdateInfo.isUpdateTypeAllowed(updateType)) {
            try {
                updateManager.startUpdateFlowForResult(
                    appUpdateInfo, updateType, this, REQUEST_CODE_UPDATE
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to start update flow")
                proceedToMain()
            }
        } else {
            proceedToMain()
        }
    }

    private fun showCriticalUpdateDialog() {
        // Implement a dialog that forces the user to update
        // and redirects them to Play Store
        proceedToMain() // Fallback if dialog implementation fails
    }

    private fun proceedToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (!isFinishing) {
            updateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    startUpdateFlow(appUpdateInfo, true)
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_UPDATE = 1234
        private const val MINIMUM_FETCH_INTERVAL_DEBUG = 0L // Debug: fetch every time
        private const val MINIMUM_FETCH_INTERVAL_PROD = 3600L // Prod: 1 hour
    }
}