package com.appcoreopc.getmyhome.data.local

data class UserProfile(
    val propertyPrice: Int = 0,
    val propertyPriceIncrease: Int = 0,
    val proximityAmenities: Int = 0,
    val proximitySchools: Int = 0,
    val proximityTrainStation: Int = 0,
    val naturalHazardRisk: Int = 0
)