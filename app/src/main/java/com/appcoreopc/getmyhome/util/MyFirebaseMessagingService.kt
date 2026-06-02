package com.appcoreopc.getmyhome.util

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            val title = it.title ?: "Notification"
            val message = it.body ?: ""
            
            val notificationService = NotificationService(applicationContext)
            notificationService.showNotification(title, message)
        }

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "Notification"
            val message = remoteMessage.data["message"] ?: ""
            
            val notificationService = NotificationService(applicationContext)
            notificationService.showNotification(title, message)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Send token to your server
    }
}
