package com.appcoreopc.getmyhome

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.appcoreopc.getmyhome.ui.theme.BackgroundDark
import com.appcoreopc.getmyhome.ui.theme.TextPrimary
import com.appcoreopc.getmyhome.ui.theme.GetMyHomeTheme
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {

    companion object {
        const val SPLASH_DELAY_MS = 3000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GetMyHomeTheme {
                SplashScreen(
                    onNavigateToMain = {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    },
                    onNavigateToLogin = {
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Get My Home",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
    }

    LaunchedEffect(Unit) {
        delay(3000)
        val hasAccount = checkUserAccount()
        if (hasAccount) {
            onNavigateToMain()
        } else {
            onNavigateToLogin()
        }
    }
}

private fun checkUserAccount(): Boolean {
    return false
}
