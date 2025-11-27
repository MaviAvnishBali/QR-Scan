package com.avnish.qrscan.updates

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.InstallErrorCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object UpdateManager {
    private const val TAG = "UpdateManager"
    private const val REQUEST_CODE_IMMEDIATE_UPDATE = 1001
    private const val REQUEST_CODE_FLEXIBLE_UPDATE = 1002
    
    private var appUpdateManager: AppUpdateManager? = null
    private var updateInfo: AppUpdateInfo? = null
    
    // State for flexible updates
    private var isFlexibleUpdateDownloaded = false
    private var isImmediateUpdateInProgress = false
    
    fun initialize(activity: Activity) {
        // Check if app is installed from Play Store before initializing
        if (!isInstalledFromPlayStore(activity)) {
            Log.d(TAG, "App not installed from Play Store - skipping update manager initialization")
            return
        }
        
        appUpdateManager = AppUpdateManagerFactory.create(activity)
        checkForUpdates(activity)
    }
    
    private fun isInstalledFromPlayStore(activity: Activity): Boolean {
        return try {
            val packageManager = activity.packageManager
            val installerPackageName = packageManager.getInstallerPackageName(activity.packageName)
            installerPackageName == "com.android.vending" || installerPackageName == "com.google.android.feedback"
        } catch (e: Exception) {
            Log.d(TAG, "Could not determine installer package", e)
            false
        }
    }
    
    private fun checkForUpdates(activity: Activity) {
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                appUpdateManager?.appUpdateInfo?.addOnSuccessListener { info ->
                    updateInfo = info
                    when (info.updateAvailability()) {
                        UpdateAvailability.UPDATE_AVAILABLE -> {
                            Log.d(TAG, "Update available")
                            handleUpdateAvailable(activity, info)
                        }
                        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                            Log.d(TAG, "Developer triggered update in progress")
                            handleDeveloperTriggeredUpdate(activity, info)
                        }
                        else -> {
                            Log.d(TAG, "No update available")
                        }
                    }
                }?.addOnFailureListener { exception ->
                    // Handle specific error cases
                    when {
                        exception.message?.contains("ERROR_APP_NOT_OWNED") == true -> {
                            Log.d(TAG, "App not installed from Play Store - skipping update check")
                        }
                        exception.message?.contains("ERROR_INVALID_REQUEST") == true -> {
                            Log.d(TAG, "Invalid request - app may not be published on Play Store")
                        }
                        else -> {
                            Log.e(TAG, "Error checking for updates", exception)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for updates", e)
            }
        }
    }
    
    private fun handleUpdateAvailable(activity: Activity, updateInfo: AppUpdateInfo) {
        // Check if immediate update is available
        if (updateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
            Log.d(TAG, "Immediate update is available")
            // For critical updates, you might want to show immediate update
            // For now, we'll show flexible update by default
            showFlexibleUpdate(activity, updateInfo)
        } else if (updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
            Log.d(TAG, "Flexible update is available")
            showFlexibleUpdate(activity, updateInfo)
        }
    }
    
    private fun handleDeveloperTriggeredUpdate(activity: Activity, updateInfo: AppUpdateInfo) {
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                appUpdateManager?.completeUpdate()
            } catch (e: Exception) {
                Log.e(TAG, "Error completing developer triggered update", e)
            }
        }
    }
    
    fun showImmediateUpdate(activity: Activity) {
        if (appUpdateManager == null) {
            Log.d(TAG, "Update manager not initialized - cannot show immediate update")
            return
        }
        
        updateInfo?.let { info ->
            if (info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                isImmediateUpdateInProgress = true
                try {
                    appUpdateManager?.startUpdateFlowForResult(
                        info,
                        AppUpdateType.IMMEDIATE,
                        activity,
                        REQUEST_CODE_IMMEDIATE_UPDATE
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting immediate update", e)
                    isImmediateUpdateInProgress = false
                }
            }
        }
    }
    
    fun showFlexibleUpdate(activity: Activity, updateInfo: AppUpdateInfo? = null) {
        if (appUpdateManager == null) {
            Log.d(TAG, "Update manager not initialized - cannot show flexible update")
            return
        }
        
        val info = updateInfo ?: this.updateInfo
        info?.let { updateInfo ->
            if (updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    appUpdateManager?.startUpdateFlowForResult(
                        updateInfo,
                        AppUpdateType.FLEXIBLE,
                        activity,
                        REQUEST_CODE_FLEXIBLE_UPDATE
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting flexible update", e)
                }
            }
        }
    }
    
    fun completeFlexibleUpdate(activity: Activity) {
        if (appUpdateManager == null) {
            Log.d(TAG, "Update manager not initialized - cannot complete flexible update")
            return
        }
        
        if (isFlexibleUpdateDownloaded) {
            val scope = CoroutineScope(Dispatchers.Main)
            scope.launch {
                try {
                    appUpdateManager?.completeUpdate()
                } catch (e: Exception) {
                    Log.e(TAG, "Error completing flexible update", e)
                }
            }
        }
    }
    
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_IMMEDIATE_UPDATE -> {
                if (resultCode != Activity.RESULT_OK) {
                    Log.d(TAG, "Immediate update failed with result code: $resultCode")
                    isImmediateUpdateInProgress = false
                }
            }
            REQUEST_CODE_FLEXIBLE_UPDATE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Log.d(TAG, "Flexible update started successfully")
                    }
                    Activity.RESULT_CANCELED -> {
                        Log.d(TAG, "Flexible update cancelled by user")
                    }
                    else -> {
                        Log.d(TAG, "Flexible update failed with result code: $resultCode")
                    }
                }
            }
        }
    }
    
    fun onResume(activity: Activity) {
        appUpdateManager?.let { manager ->
            manager.appUpdateInfo.addOnSuccessListener { updateInfo ->
                when (updateInfo.updateAvailability()) {
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        // Complete the update
                        val scope = CoroutineScope(Dispatchers.Main)
                        scope.launch {
                            try {
                                manager.completeUpdate()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error completing update on resume", e)
                            }
                        }
                    }
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        // Check if flexible update is downloaded
                        if (updateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                            isFlexibleUpdateDownloaded = true
                            Log.d(TAG, "Flexible update downloaded and ready to install")
                        }
                    }
                    else -> {
                        // Update completed or not available
                        isImmediateUpdateInProgress = false
                        isFlexibleUpdateDownloaded = false
                    }
                }
            }
        }
    }
    
    fun isUpdateInProgress(): Boolean {
        return isImmediateUpdateInProgress || isFlexibleUpdateDownloaded
    }
    
    fun isFlexibleUpdateReady(): Boolean {
        return isFlexibleUpdateDownloaded
    }
    
    fun cleanup() {
        appUpdateManager = null
        updateInfo = null
        isFlexibleUpdateDownloaded = false
        isImmediateUpdateInProgress = false
    }
}

