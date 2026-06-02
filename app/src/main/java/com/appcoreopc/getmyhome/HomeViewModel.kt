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
import com.appcoreopc.getmyhome.data.local.UserProfile
import com.appcoreopc.getmyhome.util.LocationHelper
import com.appcoreopc.getmyhome.data.const.AppDestinations

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val propertySearchBackendApi: PropertySearchBackendApi,
    private val graphQLClient: OkHttpClient,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _reportContent = MutableStateFlow<String?>(null)
    val reportContent: StateFlow<String?> = _reportContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _userReports = MutableStateFlow<List<UserReport>>(emptyList())
    val userReports: StateFlow<List<UserReport>> = _userReports.asStateFlow()

    private val _userProfile = MutableStateFlow<List<UserProfile>>(emptyList())
    val userProfile: StateFlow<List<UserProfile>> = _userProfile.asStateFlow()

    private val _notificationTitle = MutableStateFlow<String?>("GetMyHome Notification")
    val notificationTitle: StateFlow<String?> = _notificationTitle.asStateFlow()

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()
    private val _currentDestination = MutableStateFlow(AppDestinations.HOME)
    val currentDestination: StateFlow<AppDestinations> = _currentDestination.asStateFlow()

    private val _suburbSuggestions = MutableStateFlow<List<String>>(emptyList())
    val suburbSuggestions: StateFlow<List<String>> = _suburbSuggestions.asStateFlow()

    fun updateReportChecked(reportId: String, isChecked: Boolean) {
        _userReports.value = _userReports.value.map { report ->
            if (report.reportId == reportId) {
                report.copy(isChecked = isChecked)
            } else {
                report
            }
        }
    }

    fun detectLocation(onLocationDetected: (String) -> Unit) {
        viewModelScope.launch {
            val location = locationHelper.getCurrentStateOrCountry()
            onLocationDetected(location)
        }
    }

    fun fetchSuburbSuggestions(query: String) {
        viewModelScope.launch {
            _suburbSuggestions.value = locationHelper.getSuburbSuggestions(query)
        }
    }

    fun clearSuburbSuggestions() {
        _suburbSuggestions.value = emptyList()
    }

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
                                    currentAnalysis = report.optString("currentAnalysis")
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
                val profile = withContext(Dispatchers.IO) {
                    val graphQLQuery = JSONObject().apply {
                        put("query", """
                            query GetUserProfile(${'$'}userId: String!) {
                                getUserProfile(userId: ${'$'}userId) {
                                    status
                                    userId
                                    userProfileCriteria {
                                      propertyPrice
                                      propertyPriceIncrease
                                      proximityAmenities
                                      proximitySchools
                                      proximityTrainStation
                                      naturalHazardRisk
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
                        val getUserProfile = data?.optJSONObject("getUserProfile")
                        val criteriaArray = getUserProfile?.optJSONArray("userProfileCriteria")
                        if (criteriaArray != null && criteriaArray.length() > 0) {
                            (0 until criteriaArray.length()).map { i ->
                                val criteria = criteriaArray.getJSONObject(i)
                                UserProfile(
                                    propertyPrice = criteria.optInt("propertyPrice"),
                                    propertyPriceIncrease = criteria.optInt("propertyPriceIncrease"),
                                    proximityAmenities = criteria.optInt("proximityAmenities"),
                                    proximitySchools = criteria.optInt("proximitySchools"),
                                    proximityTrainStation = criteria.optInt("proximityTrainStation"),
                                    naturalHazardRisk = criteria.optInt("naturalHazardRisk")
                                )
                            }
                        } else {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                }
                _userProfile.value = profile
            } catch (e: Exception) {
                e.printStackTrace()
                _userProfile.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setUserId(userId: String) {
        _userId.value = userId
    }

    fun setNotificationData(title: String?, message: String?) {
        _notificationTitle.value = title
        _notificationMessage.value = message
    }

    fun navigateTo(destination: AppDestinations) {
        _currentDestination.value = destination
    }

    fun saveUserProfile(userId: String, profile: UserProfile) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                withContext(Dispatchers.IO) {
                    val graphQLQuery = JSONObject().apply {
                        put("query", """
                            mutation SaveUserProfile(${'$'}userId: String!, ${'$'}profile: UserProfileInput!) {
                                saveUserProfile(userId: ${'$'}userId, profile: ${'$'}profile) {
                                    status
                                    userId
                                    userProfileCriteria {
                                        propertyPrice
                                        propertyPriceIncrease
                                        proximityAmenities
                                        proximitySchools
                                        proximityTrainStation
                                        naturalHazardRisk
                                    }
                                }
                            }
                        """.trimIndent())
                        put("variables", JSONObject().apply {
                            put("userId", userId)
                            put("profile", JSONObject().apply {
                                put("propertyPrice", profile.propertyPrice)
                                put("propertyPriceIncrease", profile.propertyPriceIncrease)
                                put("proximityAmenities", profile.proximityAmenities)
                                put("proximitySchools", profile.proximitySchools)
                                put("proximityTrainStation", profile.proximityTrainStation)
                                put("naturalHazardRisk", profile.naturalHazardRisk)
                            })
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
                        val saveUserProfile = jsonResponse.optJSONObject("data")?.optJSONObject("saveUserProfile")
                        val criteriaArray = saveUserProfile?.optJSONArray("userProfileCriteria")
                        if (criteriaArray != null && criteriaArray.length() > 0) {
                            val updatedProfile = criteriaArray.getJSONObject(0)
                            val newProfile = UserProfile(
                                propertyPrice = updatedProfile.optInt("propertyPrice"),
                                propertyPriceIncrease = updatedProfile.optInt("propertyPriceIncrease"),
                                proximityAmenities = updatedProfile.optInt("proximityAmenities"),
                                proximitySchools = updatedProfile.optInt("proximitySchools"),
                                proximityTrainStation = updatedProfile.optInt("proximityTrainStation"),
                                naturalHazardRisk = updatedProfile.optInt("naturalHazardRisk")
                            )
                            _userProfile.value = listOf(newProfile)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}


