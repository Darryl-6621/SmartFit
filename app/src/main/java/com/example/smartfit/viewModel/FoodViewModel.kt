package com.example.smartfit.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartfit.SmartFitApplication
import com.example.smartfit.network.FoodDetailResponse
import com.example.smartfit.network.FoodSearchItem
import com.example.smartfit.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodViewModel(
    private val repository: ActivityRepository
) : ViewModel() {

    private val apiKey = "e98a6124e99d496ebaf0d5a4b58eeaeb" // Ideally keep this in local.properties

    private val _searchResults = MutableStateFlow<List<FoodSearchItem>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _selectedFoodDetail = MutableStateFlow<FoodDetailResponse?>(null)
    val selectedFoodDetail = _selectedFoodDetail.asStateFlow()

    // NEW: Track the image of the selected item
    private val _selectedFoodImage = MutableStateFlow<String?>(null)
    val selectedFoodImage = _selectedFoodImage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading = _isDetailLoading.asStateFlow()

    fun searchFood(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.searchFood(query, apiKey)
                _searchResults.value = response.results
            } catch (e: Exception) {
                Log.e("FoodViewModel", "Search Error", e)
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // NEW: Call this when clicking a search result item
    fun onFoodSelected(item: FoodSearchItem) {
        _selectedFoodImage.value = item.image // Capture the image here
        fetchFoodDetails(item.id)
    }

    private fun fetchFoodDetails(id: Int) {
        viewModelScope.launch {
            _isDetailLoading.value = true
            _selectedFoodDetail.value = null
            try {
                val detail = repository.getFoodDetails(id, apiKey)
                _selectedFoodDetail.value = detail
            } catch (e: Exception) {
                Log.e("FoodViewModel", "Detail Fetch Error", e)
            } finally {
                _isDetailLoading.value = false
            }
        }
    }

    fun getCaloriesPerUnit(): Double {
        val detail = _selectedFoodDetail.value ?: return 0.0
        return detail.nutrition.nutrients.find {
            it.name.equals("Calories", ignoreCase = true) ||
                    it.unit.equals("kcal", ignoreCase = true)
        }?.amount ?: 0.0
    }

    fun clearResults() {
        _searchResults.value = emptyList()
        _selectedFoodDetail.value = null
        _selectedFoodImage.value = null
        _isLoading.value = false
        _isDetailLoading.value = false
    }

    class Factory(private val app: SmartFitApplication) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FoodViewModel(app.repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}