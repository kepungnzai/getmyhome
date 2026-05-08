package com.appcoreopc.getmyhome.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoreopc.getmyhome.data.repository.HomeRepository
import com.appcoreopc.getmyhome.util.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val history = repository.getHistory()

    init {
        detectLocation()
    }

    private fun detectLocation() {
        viewModelScope.launch {
            val location = locationHelper.getCurrentStateOrCountry()
            _uiState.value = _uiState.value.copy(currentLocation = location)
        }
    }

    fun onSuburbChange(suburb: String) {
        _uiState.value = _uiState.value.copy(suburb = suburb)
    }

    fun onQuickAnalysis() {
        performAnalysis("Quick Analysis")
    }

    fun onDetailedAnalysis(
        bedrooms: String,
        bathrooms: String,
        kitchens: String,
        propertyType: String,
        propertyAge: String
    ) {
        val details = "Type: $propertyType, Age: $propertyAge, Config: $bedrooms bed, $bathrooms bath, $kitchens kitchen"
        performAnalysis(details)
    }

    private fun performAnalysis(details: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, analysisResult = null)
            try {
                val result = repository.analyzeProperty(_uiState.value.suburb, details)
                _uiState.value = _uiState.value.copy(isLoading = false, analysisResult = result)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, analysisResult = "Error: ${e.message}")
            }
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(analysisResult = null)
    }
}

data class UiState(
    val suburb: String = "",
    val currentLocation: String = "Detecting...",
    val isLoading: Boolean = false,
    val analysisResult: String? = null
)
