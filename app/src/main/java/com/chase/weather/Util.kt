package com.chase.weather

import kotlin.math.roundToInt

fun Double?.kelvinToFahrenheitInt(): Int? =
    if (this == null) null else ((this-273.0) * 1.8 + 32.0).roundToInt()
