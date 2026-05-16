package com.appcoreopc.getmyhome

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.appcoreopc.getmyhome.ui.theme.GetMyHomeTheme
import com.appcoreopc.getmyhome.ui.components.LoginScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            handleGoogleSignInSuccess(account.email, account.displayName)
        } catch (e: ApiException) {
            handleGoogleSignInFailure(e.statusCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

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
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    private fun signInWithGoogle() {
//        val signInIntent = googleSignInClient.signInIntent
//        signInLauncher.launch(signInIntent)
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    private fun handleGoogleSignInSuccess(email: String?, displayName: String?) {
        Toast.makeText(
            this,
            "Signed in as: ${displayName ?: email}",
            Toast.LENGTH_SHORT
        ).show()
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    private fun handleGoogleSignInFailure(statusCode: Int) {
        val message = when (statusCode) {
            10 -> "Developer error: Check your Google Cloud Console configuration"
            12500 -> "Sign in failed: Please update Google Play Services"
            12501 -> "Sign in cancelled by user"
            else -> "Sign in failed with error code: $statusCode"
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}






