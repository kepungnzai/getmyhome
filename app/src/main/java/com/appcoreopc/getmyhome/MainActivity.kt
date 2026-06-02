package com.appcoreopc.getmyhome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.appcoreopc.getmyhome.ui.theme.GetMyHomeTheme

import android.content.Intent
import com.appcoreopc.getmyhome.data.const.AppDestinations
import com.appcoreopc.getmyhome.util.NotificationService

import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts

@dagger.hilt.android.AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkNotificationPermission()
        enableEdgeToEdge()
        setContent {
            GetMyHomeTheme {
                GetMyHomeApp(viewModel = viewModel)
            }
        }
        handleIntent(intent)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            if (it.getBooleanExtra(NotificationService.EXTRA_OPEN_NOTIFICATION, false)) {
                val title = it.getStringExtra(NotificationService.EXTRA_NOTIFICATION_TITLE)
                val message = it.getStringExtra(NotificationService.EXTRA_NOTIFICATION_MESSAGE)
                viewModel.setNotificationData(title, message)
                viewModel.navigateTo(AppDestinations.NOTIFICATION_VIEWER)
            }
        }
    }
}









