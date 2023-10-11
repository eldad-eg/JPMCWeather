package com.chase.weather.model

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// see https://openweathermap.org/api/

interface OpenWeatherAPI {
    companion object {
        val BASE_URL = "https://api.openweathermap.org/"
    }

    // https://api.openweathermap.org/data/2.5/weather?q={city name}&appid={API key}
    // https://api.openweathermap.org/data/2.5/weather?q={city name},{country code}&appid={API key}
    // https://api.openweathermap.org/data/2.5/weather?q={city name},{state code},{country code}&appid={API key}
    @GET("data/2.5/weather")
    suspend fun getWeatherByQuery(
        @Query("q") query: String,
        @Query("appid") apiKey: String
    ): Response<WeatherResponse>

    // https://api.openweathermap.org/data/2.5/weather?lat=44.34&lon=10.99&appid={API key}
    @GET("data/2.5/weather")
    suspend fun getWeatherByLatLon(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String
    ): Response<WeatherResponse>

    // http://api.openweathermap.org/geo/1.0/direct?q=London&limit=5&appid={API key}
    @GET("geo/1.0/direct")
    suspend fun geocode(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("appid") apiKey: String
    ): Response<List<Location>>

    // http://api.openweathermap.org/geo/1.0/reverse?lat={lat}&lon={lon}&limit={limit}&appid={API key}
    @GET("geo/1.0/reverse")
    suspend fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("limit") limit: Int,
        @Query("appid") apiKey: String
    ): Response<List<Location>>

    // http://api.openweathermap.org/geo/1.0/zip?zip=E14,GB&appid={API key}
    @GET("geo/1.0/zip")
    suspend fun zipCodeSearch(
        @Query("zip") zip: String,
        @Query("appid") apiKey: String
    ): Response<Location>
}
