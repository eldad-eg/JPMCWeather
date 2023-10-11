package com.chase.weather.model

// data classes to match server responses
// initially created with the "JSON To Kotlin Class" plugin
// https://plugins.jetbrains.com/plugin/9960-json-to-kotlin-class-jsontokotlinclass-

data class Location(
    var lat: Double,
    var lon: Double,
    var country: String? = null,
    var local_names: HashMap<String, String?>? = null,
    var name: String? = null,
    var state: String? = null) {

    fun displayText(): String = when {
        name == null -> "($lat, $lon)"
        state == null && country == null -> "$name"
        state == null -> "$name, $country"
        else -> "$name, $state, $country"
    }
}

data class WeatherResponse(
    // this field does not exist in the server response, but it's a straightforward way
    // to associate the response with a timestamp, so we can tell when it's stale
    var timestamp: Long = 0,

    val base: String?,
    val clouds: Clouds?,
    val cod: Int?,
    val coord: Coord?,
    val dt: Int?,
    val id: Int?,
    val main: Main?,
    val name: String?,
    val sys: Sys?,
    val timezone: Int?,
    val visibility: Int?,
    val weather: List<Weather>?,
    val wind: Wind?
)

data class Clouds(
    val all: Int?
)

data class Coord(
    val lat: Double?,
    val lon: Double?
)

data class Main(
    val feels_like: Double?,
    val grnd_level: Int?,
    val humidity: Int?,
    val pressure: Int?,
    val sea_level: Int?,
    val temp: Double?,
    val temp_max: Double?,
    val temp_min: Double?
)

data class Sys(
    val country: String?,
    val id: Int?,
    val sunrise: Long?,
    val sunset: Long?,
    val type: Int?
)

data class Weather(
    val description: String?,
    val icon: String?,
    val id: Int?,
    val main: String?
)

data class Wind(
    val deg: Int?,
    val gust: Double?,
    val speed: Double?
)

/* sample weather response:
{
  "coord":{
    "lon":10.99,
    "lat":44.34
  },
  "weather":[
    {
      "id":800,
      "main":"Clear",
      "description":"clear sky",
      "icon":"01n"
    }
  ],
  "base":"stations",
  "main":{
    "temp":285.75,
    "feels_like":284.97,
    "temp_min":283.56,
    "temp_max":287.27,
    "pressure":1020,
    "humidity":73,
    "sea_level":1020,
    "grnd_level":934
  },
  "visibility":10000,
  "wind":{
    "speed":1.63,
    "deg":251,
    "gust":1.59
  },
  "clouds":{
    "all":3
  },
  "dt":1695872613,
  "sys":{
    "type":1,
    "id":6812,
    "country":"IT",
    "sunrise":1695877772,
    "sunset":1695920645
  },
  "timezone":7200,
  "id":3163858,
  "name":"Zocca",
  "cod":200
}
 */