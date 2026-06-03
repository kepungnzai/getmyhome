package com.appcoreopc.getmyhome.util

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)

    fun registerTokenIfNecessary() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FcmTokenManager", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.w("Token", token)

            val storedToken = prefs.getString("fcm_token", null)

            if (token != storedToken) {
                sendTokenToServer(token)
            } else {
                Log.d("FcmTokenManager", "Token already registered and hasn't changed.")
            }
        }
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Implement actual network call to your backend here
        Log.d("FcmTokenManager", "Registering token to server: $token")
        
        // On successful registration with your backend:
        saveTokenLocally(token)
    }

    private fun saveTokenLocally(token: String) {
        prefs.edit().putString("fcm_token", token).apply()
        Log.d("FcmTokenManager", "Token saved locally.")
    }
}
