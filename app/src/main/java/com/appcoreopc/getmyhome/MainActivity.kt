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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.appcoreopc.getmyhome.data.const.API_BASE_URL
import com.appcoreopc.getmyhome.data.const.GRAPHQL_ENDPOINT
import com.appcoreopc.getmyhome.data.const.GRAPHQL_WS_ENDPOINT
import com.appcoreopc.getmyhome.data.const.AppDestinations
import com.appcoreopc.getmyhome.data.local.PropertySearchRequest
import com.appcoreopc.getmyhome.data.local.ReportResponse
import com.appcoreopc.getmyhome.data.local.PropertySearchBackendApi
import androidx.compose.foundation.layout.size

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
    var useGraphQL by rememberSaveable { mutableStateOf(1) }
    var reportContent by rememberSaveable { mutableStateOf<String?>(null) }
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
        @Composable
        fun HomeUIComponentsBuild() {
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

                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    if (useGraphQL == 0) {
                                        val result = searchPropertyGraphQL(location, propertyType)
                                        // TODO: Handle GraphQL response: result
                                    } else if (useGraphQL == 1) {
                                        reportContent = ""
                                        analyzeStreamGraphQL(location, propertyType) { data ->
                                            reportContent = data
                                        }
                                    } else {

                                        val response = propertySearchBackendApi.searchProperty(
                                            PropertySearchRequest(
                                                location,
                                                propertyType
                                            )
                                        )
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

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val reportResponse = generateReportGraphQL()
                                    reportContent = reportResponse?.content
                                } catch (e: Exception) {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        )
                        {
                            Text("Report", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    if (reportContent != null) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(top = 16.dp)
                        ) {
                            items(reportContent!!.split("\n")) { line ->
                                Text(
                                    text = line,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        when (currentDestination) {
            AppDestinations.HOME -> {
                HomeUIComponentsBuild()
            }
            AppDestinations.INSIGHTS -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Insights", color = TextPrimary, fontSize = 24.sp)
                }
            }
            AppDestinations.INSIGHTS -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painterResource(R.drawable.ic_analytics),
                            contentDescription = "Analysis",
                            tint = PrimaryPurple,
                            modifier = Modifier
                                .size(64.dp)
                                .padding(bottom = 16.dp)
                        )
                        Text("Analysis Dashboard", color = TextPrimary, fontSize = 24.sp)
                    }
                }
            }
            AppDestinations.SETTINGS -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Settings", color = TextPrimary, fontSize = 24.sp)
                }
            }
        }
    }}

private val propertySearchBackendApi by lazy {
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
        .create(PropertySearchBackendApi::class.java)
}

private val graphQLClient by lazy {
    OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}

suspend fun searchPropertyGraphQL(location: String, propertyType: String): String? =
    withContext(Dispatchers.IO) {
        val graphQLQuery = JSONObject().apply {
            put(
                "query", """
            query AnalyzeQuery(${'$'}location: String!, ${'$'}propertyType: String!) {
                analyze(location: ${'$'}location, propertyType: ${'$'}propertyType) {
                    status
                    analysis
                }
            }
        """.trimIndent()
            )
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

suspend fun generateReportGraphQL(format: String = "html"): ReportResponse? = withContext(Dispatchers.IO) {
    val graphQLQuery = JSONObject().apply {
        put(
            "query", """
            query GenerateReportQuery(${'$'}format: String!) {
                generateReport(format: ${'$'}format) {
                    status
                    format
                    content
                }
            }
        """.trimIndent()
        )
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
        val generateReportJson = jsonResponse.optJSONObject("data")?.optJSONObject("generateReport")
        if (generateReportJson != null) {
            ReportResponse(
                status = generateReportJson.optString("status"),
                format = generateReportJson.optString("format"),
                content = generateReportJson.optString("content")
            )
        } else {
            null
        }
    } else {
        null
    }
}

suspend fun analyzeStreamGraphQL(location: String, propertyType: String, onData: (String) -> Unit) = withContext(Dispatchers.IO) {
    val wsClient = OkHttpClient.Builder().build()

    val request = Request.Builder().url(GRAPHQL_WS_ENDPOINT).addHeader("Sec-WebSocket-Protocol", "graphql-transport-ws").build()
    val stringBuilder = StringBuilder()

    wsClient.newWebSocket(request, object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            val initMessage = JSONObject().apply {
                put("type", "connection_init")
            }.toString()
            webSocket.send(initMessage)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val message = JSONObject(text)
                when (message.getString("type")) {
                    "connection_ack" -> {
                        val subscribeMessage = JSONObject().apply {
                            put("id", "1")
                            put("type", "subscribe")
                            put("payload", JSONObject().apply {
                                put("query", """
                                    subscription AnalyzeStream(${'$'}location: String!, ${'$'}propertyType: String!) {
                                        analyzeStream(location: ${'$'}location, propertyType: ${'$'}propertyType) {
                                            status
                                            analysis
                                        }
                                    }
                                """.trimIndent())
                                put("variables", JSONObject().apply {
                                    put("location", location)
                                    put("propertyType", propertyType)
                                })
                            })
                        }.toString()
                        webSocket.send(subscribeMessage)
                    }
                    "next" -> {
                        val payload = message.optJSONObject("payload")
                        val data = payload?.optJSONObject("data")
                        val analyzeStream = data?.optJSONObject("analyzeStream")
                        val analysis = analyzeStream?.optString("analysis")
                        if (!analysis.isNullOrEmpty()) {
                            stringBuilder.append(analysis).append("\n")
                            onData(stringBuilder.toString())
                        }
                    }
                    "complete" -> {
                        webSocket.close(1000, "Complete")
                    }
                    "error" -> {
                        webSocket.close(1000, "Error")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            t.printStackTrace()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        }
    })
}