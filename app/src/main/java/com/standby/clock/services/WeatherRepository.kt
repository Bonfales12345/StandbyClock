package com.standby.clock.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.standby.clock.data.WeatherConditionType
import com.standby.clock.data.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.roundToInt

class WeatherRepository(private val context: Context) {
    private val _weatherData = MutableStateFlow(
        WeatherData(
            isAvailable = false,
            city = "No Weather Information",
            condition = "No Weather Information"
        )
    )
    val weatherData: StateFlow<WeatherData> = _weatherData.asStateFlow()

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    suspend fun refreshWeather(isCelsius: Boolean? = null, customCity: String? = null) {
        withContext(Dispatchers.IO) {
            try {
                if (!hasLocationPermission()) {
                    _weatherData.value = WeatherData(
                        isAvailable = false,
                        city = "No Weather Information",
                        condition = "No Weather Information"
                    )
                    return@withContext
                }

                val loc = getLastKnownLocation()
                if (loc == null) {
                    _weatherData.value = WeatherData(
                        isAvailable = false,
                        city = "No Weather Information",
                        condition = "No Weather Information"
                    )
                    return@withContext
                }

                val lat = loc.latitude
                val lon = loc.longitude
                var resolvedCity: String? = null

                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val locality = addresses[0].locality
                            ?: addresses[0].subAdminArea
                            ?: addresses[0].adminArea
                        if (!locality.isNullOrBlank()) {
                            resolvedCity = locality
                        }
                    }
                } catch (e: Throwable) {}

                val finalCity = resolvedCity ?: customCity ?: "Cupertino"
                val useCelsius = isCelsius ?: isLocaleCelsius()
                val unitParam = if (useCelsius) "celsius" else "fahrenheit"
                val urlString = "https://api.open-meteo.com/v1/forecast?" +
                        "latitude=$lat&longitude=$lon" +
                        "&current=temperature_2m,weather_code" +
                        "&daily=temperature_2m_max,temperature_2m_min" +
                        "&temperature_unit=$unitParam&timezone=auto"

                var connection: HttpURLConnection? = null
                try {
                    val url = URL(urlString)
                    connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000
                    connection.requestMethod = "GET"

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        val response = reader.readText()
                        reader.close()

                        val json = JSONObject(response)
                        val current = json.optJSONObject("current")
                        val daily = json.optJSONObject("daily")

                        if (current != null && daily != null) {
                            val currentTemp = current.optDouble("temperature_2m", 63.0).roundToInt()
                            val weatherCode = current.optInt("weather_code", 0)

                            val maxTemps = daily.optJSONArray("temperature_2m_max")
                            val minTemps = daily.optJSONArray("temperature_2m_min")
                            val highTemp = if (maxTemps != null && maxTemps.length() > 0) {
                                maxTemps.optDouble(0, currentTemp + 10.0).roundToInt()
                            } else {
                                currentTemp + 10
                            }
                            val lowTemp = if (minTemps != null && minTemps.length() > 0) {
                                minTemps.optDouble(0, currentTemp - 10.0).roundToInt()
                            } else {
                                currentTemp - 10
                            }

                            val (conditionName, conditionType) = mapWeatherCode(weatherCode)

                            _weatherData.value = WeatherData(
                                isAvailable = true,
                                city = finalCity,
                                temperature = currentTemp,
                                condition = conditionName,
                                highTemp = highTemp,
                                lowTemp = lowTemp,
                                conditionType = conditionType,
                                isCelsius = useCelsius
                            )
                        }
                    }
                } finally {
                    connection?.disconnect()
                }
            } catch (e: Throwable) {}
        }
    }

    private fun isLocaleCelsius(): Boolean {
        return try {
            val country = Locale.getDefault().country.uppercase()
            country !in listOf("US", "BS", "KY", "LR", "PW", "FM", "MH")
        } catch (e: Throwable) {
            false
        }
    }

    private fun mapWeatherCode(code: Int): Pair<String, WeatherConditionType> {
        return when (code) {
            0 -> Pair("Sunny", WeatherConditionType.SUNNY)
            1 -> Pair("Mostly Sunny", WeatherConditionType.SUNNY)
            2 -> Pair("Partly Cloudy", WeatherConditionType.PARTLY_CLOUDY)
            3 -> Pair("Overcast", WeatherConditionType.CLOUDY)
            45, 48 -> Pair("Foggy", WeatherConditionType.CLOUDY)
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> Pair("Rainy", WeatherConditionType.RAINY)
            71, 73, 75, 77, 85, 86 -> Pair("Snow", WeatherConditionType.SNOWY)
            95, 96, 99 -> Pair("Thunderstorm", WeatherConditionType.THUNDERSTORM)
            else -> Pair("Sunny", WeatherConditionType.SUNNY)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val providers = locationManager?.getProviders(true) ?: return null
            var bestLocation: Location? = null
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }
            bestLocation
        } catch (e: Throwable) {
            null
        }
    }
}
