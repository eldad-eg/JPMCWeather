package com.chase.weather.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chase.weather.model.WeatherRepo
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    val weatherRepo: WeatherRepo by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "location") {
                composable("location") { LocationScreen(navController) }
                composable("weather") { WeatherScreen(navController) }
            }

            // if we already have a location, display weather for that location
            // TODO check if it would be best to stay on the first screen,
            // TODO and just populate the location's name in the search field?

            LaunchedEffect(Unit) {
                val savedLocation = weatherRepo.loadLocation()
                if (savedLocation != null) {
                    navController.navigate("weather")
                }
            }
        }


    }
}
