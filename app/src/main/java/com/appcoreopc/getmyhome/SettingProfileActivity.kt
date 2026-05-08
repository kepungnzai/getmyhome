package com.appcoreopc.getmyhome

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.appcoreopc.getmyhome.ui.theme.GetMyHomeTheme
import com.appcoreopc.getmyhome.ui.components.LoginScreen

class SettingProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GetMyHomeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onCreateAccount = { username, password ->
                            createAccount(username, password)
                        },
                        onGoogleSignIn = {
                            signInWithGoogle()
                        }
                    )
                }
            }
        }
    }

    private fun createAccount(username: String, password: String) {
        startActivity(Intent(this@SettingProfileActivity, MainActivity::class.java))
        finish()
    }

    private fun signInWithGoogle() {
        startActivity(Intent(this@SettingProfileActivity, MainActivity::class.java))
        finish()
    }
}
