package com.appcoreopc.getmyhome.data.repository

import com.appcoreopc.getmyhome.data.local.AnalysisHistory
import com.appcoreopc.getmyhome.data.local.AnalysisHistoryDao
import com.appcoreopc.getmyhome.data.remote.RequestInput
import com.appcoreopc.getmyhome.data.remote.VertexApiService
import com.appcoreopc.getmyhome.data.remote.VertexRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val apiService: VertexApiService,
    private val historyDao: AnalysisHistoryDao
) {
    fun getHistory(): Flow<List<AnalysisHistory>> = historyDao.getAllHistory()

    suspend fun analyzeProperty(suburb: String, details: String): String {
        // In a real production app, you would manage tokens properly.
        // This is a placeholder for the Bearer token.
        val token = "Bearer YOUR_ACCESS_TOKEN" 
        val query = "Analyze property in $suburb. Details: $details"
        
        val response = apiService.queryReasoningEngine(
            token,
            VertexRequest(RequestInput(query))
        )
        
        val result = response.output ?: "No analysis returned."
        
        historyDao.insert(
            AnalysisHistory(
                timestamp = System.currentTimeMillis(),
                suburb = suburb,
                inputDetails = details,
                output = result
            )
        )
        
        return result
    }
}
