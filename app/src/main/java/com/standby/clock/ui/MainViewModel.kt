package com.standby.clock.ui

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.OrientationEventListener
import android.view.WindowManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.standby.clock.data.ClockStyle
import com.standby.clock.data.ColorTheme
import com.standby.clock.data.PreferencesManager
import com.standby.clock.services.AlarmRepository
import com.standby.clock.services.TimeManager
import com.standby.clock.services.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferencesManager(application)
    private val timeManager = TimeManager(viewModelScope)
    private val weatherRepository = WeatherRepository(application)
    private val alarmRepository = AlarmRepository(application)

    val timeState = timeManager.timeState
    val weatherData = weatherRepository.weatherData
    val alarmData = alarmRepository.alarmData

    private val _clockStyle = MutableStateFlow(prefs.clockStyle)
    val clockStyle: StateFlow<ClockStyle> = _clockStyle.asStateFlow()

    val colorTheme: StateFlow<ColorTheme> = MutableStateFlow(ColorTheme.MATERIAL_YOU).asStateFlow()

    private val _is24Hour = MutableStateFlow(prefs.is24Hour)
    val is24Hour: StateFlow<Boolean> = _is24Hour.asStateFlow()

    private val _isNightRedMode = MutableStateFlow(false)
    val isNightRedMode: StateFlow<Boolean> = _isNightRedMode.asStateFlow()

    private val _screenBrightness = MutableStateFlow(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
    val screenBrightness: StateFlow<Float> = _screenBrightness.asStateFlow()

    private val _isReverseLandscape = MutableStateFlow(false)
    val isReverseLandscape: StateFlow<Boolean> = _isReverseLandscape.asStateFlow()

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val lightSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null || event.sensor.type != Sensor.TYPE_LIGHT) return
            val lux = event.values[0]
            if (lux < 4.0f) {
                _isNightRedMode.value = true
                _screenBrightness.value = 0.01f
            } else if (lux > 8.0f) {
                _isNightRedMode.value = false
                _screenBrightness.value = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val orientationListener = object : OrientationEventListener(application) {
        override fun onOrientationChanged(orientation: Int) {
            if (orientation == ORIENTATION_UNKNOWN) return
            if (orientation in 60..120) {
                _isReverseLandscape.value = true
            } else if (orientation in 240..300) {
                _isReverseLandscape.value = false
            }
        }
    }

    init {
        refreshData()
        try {
            if (orientationListener.canDetectOrientation()) {
                orientationListener.enable()
            }
        } catch (e: Throwable) {}
        try {
            if (lightSensor != null) {
                sensorManager?.registerListener(
                    lightSensorListener,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        } catch (e: Throwable) {}
    }

    fun refreshData() {
        viewModelScope.launch {
            weatherRepository.refreshWeather(
                isCelsius = if (prefs.isCelsius) true else null,
                customCity = if (prefs.customCity != "Cupertino") prefs.customCity else null
            )
            alarmRepository.refreshAlarm()
        }
    }

    fun selectClockStyle(style: ClockStyle) {
        _clockStyle.value = style
        prefs.clockStyle = style
    }

    fun toggleNightRedMode() {
        val next = !_isNightRedMode.value
        _isNightRedMode.value = next
        _screenBrightness.value = if (next) 0.01f else WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    }

    fun toggle24HourMode() {
        val newVal = !_is24Hour.value
        _is24Hour.value = newVal
        prefs.is24Hour = newVal
    }

    fun openSystemClock() {
        alarmRepository.openSystemClock()
    }

    override fun onCleared() {
        super.onCleared()
        try {
            orientationListener.disable()
        } catch (e: Throwable) {}
        try {
            sensorManager?.unregisterListener(lightSensorListener)
        } catch (e: Throwable) {}
    }
}
