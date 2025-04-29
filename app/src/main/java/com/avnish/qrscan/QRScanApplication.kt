package com.avnish.qrscan

import android.app.Application
import com.google.firebase.FirebaseApp

class QRScanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
} 