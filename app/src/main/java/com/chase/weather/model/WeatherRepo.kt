package com.chase.weather.model

import android.location.Location.distanceBetween
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.chase.weather.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WeatherRepo(val dataStore: DataStore<Preferences>) : KoinComponent {
    private val LOCATION_KEY = stringPreferencesKey("location")
    private val WEATHER_KEY = stringPreferencesKey("weather")
    private val weatherService: OpenWeatherService by inject()
    private val gson = Gson() // for cleaner de/serialization

    private val _locationFlow = MutableStateFlow<Location?>(null)
    val locationFlow: Flow<Location?> = _locationFlow

    private val _weatherResponseFlow = MutableStateFlow<WeatherResponse?>(null)
    val weatherResponseFlow: Flow<WeatherResponse?> = _weatherResponseFlow

    private val _isLoadingWeatherFlow = MutableStateFlow<Boolean>(false)
    val isLoadingWeatherFlow: Flow<Boolean> = _isLoadingWeatherFlow

    private var loadedWeatherResponse = false
    private var weatherResponse: WeatherResponse? = null

    suspend fun loadLocation(): Location? {
        return withContext(Dispatchers.IO) {
            val preferences = dataStore.data.first()
            val json = preferences[LOCATION_KEY]
            val location: Location? =
                if (json != null) gson.fromJson(json, Location::class.java) else null
            if (location != null && _locationFlow.value == null) {
                _locationFlow.value = location
            }
            location
        }
    }

    suspend fun saveLocation(location: Location) {
        _locationFlow.value = location
        dataStore.edit { preferences ->
            preferences[LOCATION_KEY] = gson.toJson(location)
        }
        if (location.name == null) {
            // location from device location provider, missing name. Try to reverse geocode
            val response = weatherService.api.reverseGeocode(
                location.lat,
                location.lon,
                limit = 1,
                BuildConfig.OPEN_WEATHER_MAP_API_KEY
            )
            if (response.isSuccessful) {
                // got a response! if it's successful, and we haven't changed locations yet,
                // replace the original location with the new one
                val responseBody = response.body()
                if (!responseBody.isNullOrEmpty()) {
                    // there should only be one, since limit = 1
                    val locationWithName = responseBody.first()
                    if (locationWithName.name != null && _locationFlow.value == location) {
                        // use the original lat/lon, not the center of the named location from the response
                        locationWithName.lat = location.lat
                        locationWithName.lon = location.lon
                        saveLocation(locationWithName)
                    }
                }
            }
        }
    }

    suspend fun getWeather(forceRefresh: Boolean) {
        val location = _locationFlow.value ?: return
        // null means getWeather was called before we have a location; should not be possible

        if (!forceRefresh) {
            if (!loadedWeatherResponse) {
                val preferences = dataStore.data.first()
                val json = preferences[WEATHER_KEY]
                weatherResponse =
                    if (json != null) gson.fromJson(json, WeatherResponse::class.java) else null
                loadedWeatherResponse = true
            }

            // if the old response is for this (or a nearby) location, post it
            val currentResponse = weatherResponse
            if (currentResponse?.coord != null &&
                areLocationsCloseEnough(
                    location.lat, location.lon,
                    currentResponse.coord.lat!!, currentResponse.coord.lon!!
                )
            ) {
                _weatherResponseFlow.value = currentResponse
                // if the old response is new enough, don't even refresh, just return it
                if (isResponseRecentEnough(currentResponse)) {
                    return
                }
            } else {
                // old response is invalid
                _weatherResponseFlow.value = null
            }
        }

        // it's time to make a network request
        _isLoadingWeatherFlow.value = true
        val networkResponse = weatherService.api.getWeatherByLatLon(
            location.lat, location.lon, BuildConfig.OPEN_WEATHER_MAP_API_KEY
        )
        if (networkResponse.isSuccessful && networkResponse.body() != null) {
            val newWeatherResponse: WeatherResponse = networkResponse.body()!!
            newWeatherResponse.timestamp = System.currentTimeMillis()
            weatherResponse = newWeatherResponse
            _weatherResponseFlow.value = newWeatherResponse
            dataStore.edit { preferences ->
                preferences[WEATHER_KEY] = gson.toJson(newWeatherResponse)
            }
        }
        _isLoadingWeatherFlow.value = false
    }

    val DISTANCE_THRESHOLD = 250.0f // in meters.
    private fun areLocationsCloseEnough(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Boolean {
        val results = floatArrayOf(0f)
        distanceBetween(lat1, lon1, lat2, lon2, results)
        return (results[0] < DISTANCE_THRESHOLD)
    }

    val STALENESS_THRESHOLD = 30 * 60 * 1000 // 30 minutes in milliseconds
    private fun isResponseRecentEnough(response: WeatherResponse): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - response.timestamp) < STALENESS_THRESHOLD

    }
}
