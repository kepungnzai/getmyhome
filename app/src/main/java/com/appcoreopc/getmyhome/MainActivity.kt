package com.appcoreopc.getmyhome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import com.appcoreopc.getmyhome.data.local.PropertySearchBackendApi
import com.appcoreopc.getmyhome.ui.theme.GetMyHomeTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.OkHttpClient
import javax.inject.Inject

@dagger.hilt.android.AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GetMyHomeTheme {
                GetMyHomeApp(viewModel = viewModel)
            }
        }
    }
}









