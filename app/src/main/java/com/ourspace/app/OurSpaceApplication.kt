package com.ourspace.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class OurSpaceApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        runCatching {
            FirebaseApp.initializeApp(this)
            val appCheck = FirebaseAppCheck.getInstance()
            if (BuildConfig.USE_DEBUG_APP_CHECK) {
                appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
            } else {
                appCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
            }
        }
    }
}
