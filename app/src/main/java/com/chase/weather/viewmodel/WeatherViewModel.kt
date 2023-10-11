package com.chase.weather.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chase.weather.model.Location
import com.chase.weather.model.WeatherRepo
import com.chase.weather.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WeatherViewModel : ViewModel(), KoinComponent {
    private val weatherRepo: WeatherRepo by inject()

    val locationFlow: Flow<Location?> = weatherRepo.locationFlow
    val weatherResponseFlow: Flow<WeatherResponse?> = weatherRepo.weatherResponseFlow
    val isLoadingWeatherFlow: Flow<Boolean> = weatherRepo.isLoadingWeatherFlow

    fun getWeather(forceRefresh: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepo.getWeather(forceRefresh)
        }
    }

}
