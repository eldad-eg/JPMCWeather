package com.chase.weather.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chase.weather.BuildConfig
import com.chase.weather.model.Location
import com.chase.weather.model.OpenWeatherService
import com.chase.weather.model.WeatherRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LocationViewModel : ViewModel(), KoinComponent {

    // this ViewModel makes network requests by itself rather than pass the job along to
    // the repository, since these are intermediate results only required by the screen.
    private val weatherService : OpenWeatherService by inject()
    private val weatherRepo: WeatherRepo by inject()

    private val _searchResults = mutableStateOf<List<Location>>(emptyList())
    val searchResults: State<List<Location>> = _searchResults

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun saveLocation(location: Location) {
        viewModelScope.launch {
            weatherRepo.saveLocation(location)
        }
    }

    fun search(query: String) {
        val trimmedQuery = query.trim()
        when {
            trimmedQuery.length < 2 -> return
            isZipCode(trimmedQuery) -> zipCodeSearch(trimmedQuery)
            else -> geocode(trimmedQuery)
        }
    }

    fun geocode(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val result = weatherService.api.geocode(query, 5, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
            _isLoading.value = false
            if (result.isSuccessful) {
                _searchResults.value = result.body() ?: emptyList()
            } else {
                _searchResults.value = emptyList()
            }
        }
    }

    fun zipCodeSearch(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val result = weatherService.api.zipCodeSearch(query,
                BuildConfig.OPEN_WEATHER_MAP_API_KEY
            )
            _isLoading.value = false
            if (result.isSuccessful && result.body() != null) {
                _searchResults.value = listOf(result.body()!!)
            } else {
                _searchResults.value = emptyList()
            }
        }
    }

    // probably not globally correct, but a good starting point
    // TODO should the user have two different search fields,
    // TODO or otherwise indicate manually whether query is zip code?
    fun isZipCode(query: String): Boolean = (query.count { it.isDigit() }) >= 2

}
