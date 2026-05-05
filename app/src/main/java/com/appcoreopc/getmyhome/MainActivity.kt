package com.appcoreopc.getmyhome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoreopc.getmyhome.ui.theme.AccentBlue
import com.appcoreopc.getmyhome.ui.theme.AccentPink
import com.appcoreopc.getmyhome.ui.theme.BackgroundDark
import com.appcoreopc.getmyhome.ui.theme.CardGradientEnd
import com.appcoreopc.getmyhome.ui.theme.CardGradientStart
import com.appcoreopc.getmyhome.ui.theme.PrimaryPurple
import com.appcoreopc.getmyhome.ui.theme.SurfaceDark
import com.appcoreopc.getmyhome.ui.theme.TextPrimary
import com.appcoreopc.getmyhome.ui.theme.TextSecondary
import com.appcoreopc.getmyhome.ui.theme.GetMyHomeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import com.appcoreopc.getmyhome.data.const.API_BASE_URL
import com.appcoreopc.getmyhome.data.const.GRAPHQL_ENDPOINT
import com.appcoreopc.getmyhome.data.const.GRAPHQL_ENDPOINT_ENABLED

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GetMyHomeTheme {
                GetMyHomeApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun GetMyHomeApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var location by rememberSaveable { mutableStateOf("") }
    var propertyType by rememberSaveable { mutableStateOf("") }
    var useGraphQL by rememberSaveable { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label, color = TextSecondary) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        },
        containerColor = BackgroundDark,
        contentColor = TextPrimary
    ) {
        val gradientBrush = Brush.linearGradient(
            colors = listOf(CardGradientStart, CardGradientEnd),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(16.dp)
        ) {
            // Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(gradientBrush)
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "Search Properties",
                            color = TextPrimary.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Get My Home",
                            color = TextPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Find your dream home",
                            color = TextPrimary.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Search Form
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = TextSecondary,
                        cursorColor = PrimaryPurple,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    )
                )

                OutlinedTextField(
                    value = propertyType,
                    onValueChange = { propertyType = it },
                    label = { Text("Property Type", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = TextSecondary,
                        cursorColor = PrimaryPurple,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Use GraphQL", color = TextPrimary)
                    androidx.compose.material3.Switch(
                        checked = useGraphQL,
                        onCheckedChange = { useGraphQL = it }
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                if (useGraphQL) {
                                    val result = searchPropertyGraphQL(location, propertyType)
                                    // TODO: Handle GraphQL response: result
                                } else {
                                    val response = backendApi.searchProperty(PropertySearchRequest(location, propertyType))
                                    if (response.isSuccessful) {
                                        // TODO: Handle successful response: response.body()?.result
                                    } else {
                                        // TODO: Handle API error
                                    }
                                }
                            } catch (e: Exception) {
                                // TODO: Handle network error
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Search Now", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val reportContent = generateReportGraphQL()
                                // TODO: Handle report content (e.g., show in WebView or dialog)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Report", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    FAVORITES("Favorites", R.drawable.ic_favorite),
    PROFILE("Profile", R.drawable.ic_account_box),
}

data class PropertySearchRequest(val location: String, val propertyType: String)
data class PropertySearchResponse(val result: String?)

interface YourBackendApi {
    @POST("api/property-search")
    suspend fun searchProperty(@Body request: PropertySearchRequest): retrofit2.Response<PropertySearchResponse>
}

private val backendApi by lazy {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    Retrofit.Builder()
        .baseUrl(API_BASE_URL) // Android emulator localhost
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(YourBackendApi::class.java)
}

private val graphQLClient by lazy {
    OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}

suspend fun searchPropertyGraphQL(location: String, propertyType: String): String? = withContext(Dispatchers.IO) {
    val graphQLQuery = JSONObject().apply {
        put("query", """
            query AnalyzeQuery(${'$'}location: String!, ${'$'}propertyType: String!) {
                analyze(location: ${'$'}location, propertyType: ${'$'}propertyType) {
                    status
                    analysis
                }
            }
        """.trimIndent())
        put("variables", JSONObject().apply {
            put("location", location)
            put("propertyType", propertyType)
        })
    }

    val requestBody = graphQLQuery.toString().toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url(GRAPHQL_ENDPOINT)
        .post(requestBody)
        .build()

    val response = graphQLClient.newCall(request).execute()
    val responseBody = response.body?.string()

    if (response.isSuccessful && responseBody != null) {
        val jsonResponse = JSONObject(responseBody)
        jsonResponse.optJSONObject("data")?.optJSONObject("analyze")?.optString("analysis")
    } else {
        null
    }
}

suspend fun generateReportGraphQL(format: String = "html"): String? = withContext(Dispatchers.IO) {
    val graphQLQuery = JSONObject().apply {
        put("query", """
            query GenerateReportQuery(${'$'}format: String!) {
                generateReport(format: ${'$'}format) {
                    status
                    format
                    content
                }
            }
        """.trimIndent())
        put("variables", JSONObject().apply {
            put("format", format)
        })
    }

    val requestBody = graphQLQuery.toString().toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url(GRAPHQL_ENDPOINT)
        .post(requestBody)
        .build()

    val response = graphQLClient.newCall(request).execute()
    val responseBody = response.body?.string()

    if (response.isSuccessful && responseBody != null) {
        val jsonResponse = JSONObject(responseBody)
        jsonResponse.optJSONObject("data")?.optJSONObject("generateReport")?.optString("content")
    } else {
        null
    }
}