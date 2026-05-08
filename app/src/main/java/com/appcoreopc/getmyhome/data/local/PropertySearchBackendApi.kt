package com.appcoreopc.getmyhome.data.local

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PropertySearchBackendApi {
    @POST("api/property-search")
    suspend fun searchProperty(@Body request: PropertySearchRequest): Response<PropertySearchResponse>
}