package com.standby.clock.data

enum class WeatherConditionType {
    SUNNY,
    PARTLY_CLOUDY,
    CLOUDY,
    RAINY,
    SNOWY,
    THUNDERSTORM
}

data class WeatherData(
    val isAvailable: Boolean = true,
    val city: String = "No Weather Information",
    val temperature: Int = 63,
    val condition: String = "No Weather Information",
    val highTemp: Int = 91,
    val lowTemp: Int = 62,
    val conditionType: WeatherConditionType = WeatherConditionType.SUNNY,
    val isCelsius: Boolean = false
) {
    val tempString: String get() = if (isAvailable) "$temperature°" else "No Weather Information"
    val highLowString: String get() = if (isAvailable) "H:$highTemp° L:$lowTemp°" else ""
}
