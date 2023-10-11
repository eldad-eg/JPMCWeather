package com.chase.weather.model

import com.chase.weather.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OpenWeatherService {

    val api = buildApi()

    private fun buildApi(): OpenWeatherAPI {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(OpenWeatherAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            retrofitBuilder.client(
                OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build()
            )
        }
        return retrofitBuilder
            .build()
            .create(OpenWeatherAPI::class.java)
    }

}
