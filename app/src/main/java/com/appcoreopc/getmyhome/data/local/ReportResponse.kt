package com.appcoreopc.getmyhome.data.local

data class ReportResponse(
    val status: String,
    val format: String,
    val content: String? = null
)