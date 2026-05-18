package com.appcoreopc.getmyhome.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentStateOrCountry(): String {
        return try {
            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val adminArea = addresses[0].adminArea // State
                    val countryName = addresses[0].countryName
                    "$adminArea, $countryName"
                } else {
                    "Unknown Location"
                }
            } else {
                "Location not available"
            }
        } catch (e: Exception) {
            "Error detecting location"
        }
    }

    suspend fun getSuburbSuggestions(query: String): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (query.length < 3) return@withContext emptyList()
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(query, 5)
                addresses?.mapNotNull { it.locality ?: it.subLocality ?: it.featureName }?.distinct() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
