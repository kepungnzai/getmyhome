package com.appcoreopc.getmyhome.data.local

data class UserReport(
    val reportId: String,
    val location: String,
    val propertyType: String,
    val currentAnalysis: String,
    val propertyPrice: Int = 0,
    val propertyPriceIncrease: Int = 0,
    val proximityAmenities: Int = 0,
    val proximitySchools: Int = 0,
    val proximityTrainStation: Int = 0,
    val floodBushfireRisk: Int = 0
)
