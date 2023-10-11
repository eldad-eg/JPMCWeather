package com.chase.weather.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.chase.weather.R
import com.chase.weather.kelvinToFahrenheitInt
import com.chase.weather.model.Location
import com.chase.weather.model.Weather
import com.chase.weather.model.WeatherResponse
import com.chase.weather.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class) // for TopAppBar, GlideImage
@Composable
fun WeatherScreen(navController: NavHostController) {
    val viewModel = remember { WeatherViewModel() }
    val location: State<Location?> =
        viewModel.locationFlow.collectAsState(initial = null)
    val weatherResponse: State<WeatherResponse?> =
        viewModel.weatherResponseFlow.collectAsState(initial = null)
    val isLoadingWeather: State<Boolean> =
        viewModel.isLoadingWeatherFlow.collectAsState(initial = false)
    viewModel.getWeather(forceRefresh = false)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                title = { Text(LocalContext.current.resources.getString(R.string.screen_title_weather)) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )

            Text(
                text = location.value?.displayText() ?: "",
                fontSize = 28.sp,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .padding(8.dp)

            )
            val weatherItemList: List<Weather>? = weatherResponse.value?.weather
            if (weatherItemList != null) {
                Box(contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        // it turns out these icons don't display correctly on a white background
                        // TODO find a better color (some sky blue?) and move to colors.xml
                        .background(Color(0xFFFFCC99))
                        .padding(bottom = 12.dp)
                ) {
                    // most calls will return one item, but I've seen some return two
                    // TODO check: is there a theoretical or practical maximum?
                    LazyRow {
                        items(weatherItemList) { weatherItem ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                GlideImage(
                                    model = "https://openweathermap.org/img/wn/${weatherItem.icon}@2x.png",
                                    contentDescription = weatherItem.description,
                                    modifier = Modifier.size(72.dp)
                                )
                                Text(
                                    text = weatherItem.main ?: "",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = getResponseDisplayText(weatherResponse.value),
                fontSize = 24.sp,
                style = MaterialTheme.typography.displayLarge,
                lineHeight = 28.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        if (isLoadingWeather.value) {
            Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        ButtonWithIcon(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize(Alignment.BottomStart)
                .padding(8.dp),
            enabled = !isLoadingWeather.value,
            onClick = {
                viewModel.getWeather(forceRefresh = true)
            },
            text = LocalContext.current.resources.getString(R.string.refresh_button),
            imageVector = Icons.Default.Refresh
        )

        ButtonWithIcon(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize(Alignment.BottomEnd)
                .padding(8.dp),
            enabled = true,
            onClick = {
                // go to the previous screen, which is always the location screen
                navController.popBackStack()
            },
            text = LocalContext.current.resources.getString(R.string.change_location_button),
            imageVector = Icons.Default.LocationOn
        )
    }

}

fun getResponseDisplayText(weatherResponse: WeatherResponse?): String {
    if (weatherResponse == null || weatherResponse.main == null) {
        return ""
    }

    // TODO: when finalized, move to Strings.xml, get resource and .format()
    return "Temp: ${weatherResponse.main.temp.kelvinToFahrenheitInt()}\n" +
            "Feels like: ${weatherResponse.main.feels_like.kelvinToFahrenheitInt()}\n\n" +
            "Min: ${weatherResponse.main.temp_min.kelvinToFahrenheitInt()}\n" +
            "Max: ${weatherResponse.main.temp_max.kelvinToFahrenheitInt()}\n\n" +
            "Humidity: ${weatherResponse.main.humidity}\n" +
            "Pressure: ${weatherResponse.main.pressure}\n\n" +
            sunriseSunsetData(weatherResponse)
}

val simpleDateFormat = SimpleDateFormat("HH:mm") // don't recreate object every time
fun sunriseSunsetData(weatherResponse: WeatherResponse): String {

    if (weatherResponse.sys?.sunrise != null && weatherResponse.sys.sunset != null && weatherResponse.timezone != null) {
        val sunrise: Long = (weatherResponse.sys.sunrise + weatherResponse.timezone) * 1000
        val sunset: Long = (weatherResponse.sys.sunset + weatherResponse.timezone) * 1000
        simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT")

        // TODO: when finalized, move to Strings.xml, get resource and .format()
        return "Sunrise: " + simpleDateFormat.format(Date(sunrise)) + "\n" +
                "Sunset: " + simpleDateFormat.format(Date(sunset)) + "\n\n"
    } else {
        return ""
    }
}
