package com.chase.weather

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.chase.weather.model.OpenWeatherService
import com.chase.weather.model.WeatherRepo
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

private val Context.dataStore by preferencesDataStore("weather_preferences")

class WeatherApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@WeatherApplication)
            modules(networkModule, repoModule)
            if (BuildConfig.DEBUG) {
                androidLogger(Level.INFO)
            }
        }

    }
}

val networkModule = module {
    single { OpenWeatherService() }
}

val repoModule = module {
    single { androidContext().dataStore }
    single { WeatherRepo(get()) }
}
