package com.appcoreopc.getmyhome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoreopc.getmyhome.data.const.GRAPHQL_ENDPOINT
import com.appcoreopc.getmyhome.data.const.GRAPHQL_WS_ENDPOINT
import com.appcoreopc.getmyhome.data.local.PropertySearchBackendApi
import com.appcoreopc.getmyhome.data.local.PropertySearchRequest
import com.appcoreopc.getmyhome.data.local.ReportResponse
import com.appcoreopc.getmyhome.data.local.UserReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val propertySearchBackendApi: PropertySearchBackendApi,
    private val graphQLClient: OkHttpClient
) : ViewModel() {

    private val _reportContent = MutableStateFlow<String?>(null)
    val reportContent: StateFlow<String?> = _reportContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userReports = MutableStateFlow<List<UserReport>>(emptyList())
    val userReports: StateFlow<List<UserReport>> = _userReports.asStateFlow()

    fun searchPropertyREST(location: String, propertyType: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = withContext(Dispatchers.IO) {
                    propertySearchBackendApi.searchProperty(
                        PropertySearchRequest(location, propertyType)
                    )
                }
                if (response.isSuccessful) {
                    // TODO: Handle successful response: response.body()?.result
                } else {
                    // TODO: Handle API error
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchPropertyGraphQL(location: String, propertyType: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                withContext(Dispatchers.IO) {
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
                // TODO: Handle result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun analyzeStreamGraphQL(location: String, propertyType: String) {
        viewModelScope.launch {
            _reportContent.value = ""
            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(GRAPHQL_WS_ENDPOINT)
                    .addHeader("Sec-WebSocket-Protocol", "graphql-transport-ws")
                    .build()
                val stringBuilder = StringBuilder()

                graphQLClient.newWebSocket(request, object : WebSocketListener() {
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
                                        _reportContent.value = stringBuilder.toString()
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

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {}
                })
            }
        }
    }

    fun fetchReports(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val reports = withContext(Dispatchers.IO) {
                    val graphQLQuery = JSONObject().apply {
                        put("query", """
                            query GetUserReports(${'$'}userId: String!) {
                                getReportsByUserId(userId: ${'$'}userId) {
                                    status
                                    userId
                                    reports {
                                      id
                                      location
                                      propertyType
                                      currentAnalysis
                                    }
                                }
                            }
                        """.trimIndent())
                        put("variables", JSONObject().apply {
                            put("userId", userId)
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
                        val data = jsonResponse.optJSONObject("data")
                        val getReportsByUserId = data?.optJSONObject("getReportsByUserId")
                        val reportsArray = getReportsByUserId?.optJSONArray("reports")
                        if (reportsArray != null) {
                            (0 until reportsArray.length()).map { i ->
                                val report = reportsArray.getJSONObject(i)
                                UserReport(
                                    reportId = report.optString("id"),
                                    location = report.optString("location"),
                                    propertyType = report.optString("propertyType"),
                                    currentAnalysis = report.optString("currentAnalysis"),
                                    propertyPrice = 0,
                                    propertyPriceIncrease = 0,
                                    proximityAmenities = 0,
                                    proximitySchools = 0,
                                    proximityTrainStation = 0,
                                    floodBushfireRisk = 0
                                )
                            }
                        } else {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                }
                _userReports.value = reports
            } catch (e: Exception) {
                e.printStackTrace()
                _userReports.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateReportGraphQL(format: String = "html") {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val reportResponse = withContext(Dispatchers.IO) {
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
                _reportContent.value = reportResponse?.content
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val reports = withContext(Dispatchers.IO) {
                    val graphQLQuery = JSONObject().apply {
                        put("query", """
                            query GetUserReports(${'$'}userId: String!) {
                                getReportsByUserId(userId: ${'$'}userId) {
                                    status
                                    userId
                                    reports {
                                      id
                                      location
                                      propertyType
                                      currentAnalysis
                                    }
                                }
                            }
                        """.trimIndent())
                        put("variables", JSONObject().apply {
                            put("userId", userId)
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
                        val data = jsonResponse.optJSONObject("data")
                        val getReportsByUserId = data?.optJSONObject("getReportsByUserId")
                        val reportsArray = getReportsByUserId?.optJSONArray("reports")
                        if (reportsArray != null) {
                            (0 until reportsArray.length()).map { i ->
                                val report = reportsArray.getJSONObject(i)
                                UserReport(
                                    reportId = report.optString("id"),
                                    location = report.optString("location"),
                                    propertyType = report.optString("propertyType"),
                                    currentAnalysis = report.optString("currentAnalysis"),
                                    propertyPrice = 0,
                                    propertyPriceIncrease = 0,
                                    proximityAmenities = 0,
                                    proximitySchools = 0,
                                    proximityTrainStation = 0,
                                    floodBushfireRisk = 0
                                )
                            }
                        } else {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                }
                _userReports.value = reports
            } catch (e: Exception) {
                e.printStackTrace()
                _userReports.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}


