package com.appcoreopc.getmyhome.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface VertexApiService {
    @POST("v1/projects/project-00e3e1b1-5433-464c-b5e/locations/us-central1/reasoningEngines/8531084881390731264:query")
    suspend fun queryReasoningEngine(
        @Header("Authorization") token: String,
        @Body request: VertexRequest
    ): VertexResponse
}

data class VertexRequest(
    val input: RequestInput
)

data class RequestInput(
    val query: String
)

data class VertexResponse(
    val output: String?
)
