package com.appcoreopc.getmyhome.data.local

data class UserReport(
    val reportId: String,
    val propertyPrice: Int,
    val propertyPriceIncrease: Int,
    val proximityAmenities: Int,
    val proximitySchools: Int,
    val proximityTrainStation: Int,
    val floodBushfireRisk: Int
)
