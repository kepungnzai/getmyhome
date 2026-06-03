package com.appcoreopc.getmyhome

import android.app.Application
import com.appcoreopc.getmyhome.util.FcmTokenManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GetHomeApplication : Application() {

    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        fcmTokenManager.registerTokenIfNecessary()
    }
}
