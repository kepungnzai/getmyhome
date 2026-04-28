package com.appcoreopc.getmyhome

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoreopc.getmyhome.ui.theme.GetMyHomeTheme

class LoginActivity : ComponentActivity() {
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
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    private fun signInWithGoogle() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }
}

@Composable
fun LoginScreen(
    onCreateAccount: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            fontSize = 28.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onCreateAccount(username, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Account with Email")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onGoogleSignIn() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign in with Google")
        }
    }
}
