package com.standby.clock.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("standby_clock_prefs", Context.MODE_PRIVATE)

    var clockStyle: ClockStyle
        get() {
            val name = prefs.getString(KEY_CLOCK_STYLE, ClockStyle.DIGITAL.name)
            return try {
                ClockStyle.valueOf(name ?: ClockStyle.DIGITAL.name)
            } catch (e: Exception) {
                ClockStyle.DIGITAL
            }
        }
        set(value) {
            prefs.edit().putString(KEY_CLOCK_STYLE, value.name).apply()
        }

    var colorTheme: ColorTheme
        get() {
            val name = prefs.getString(KEY_COLOR_THEME, ColorTheme.MATERIAL_YOU.name)
            return try {
                ColorTheme.valueOf(name ?: ColorTheme.MATERIAL_YOU.name)
            } catch (e: Exception) {
                ColorTheme.MATERIAL_YOU
            }
        }
        set(value) {
            prefs.edit().putString(KEY_COLOR_THEME, value.name).apply()
        }

    var is24Hour: Boolean
        get() = prefs.getBoolean(KEY_IS_24_HOUR, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_24_HOUR, value).apply()

    var isCelsius: Boolean
        get() = prefs.getBoolean(KEY_IS_CELSIUS, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_CELSIUS, value).apply()

    var keepScreenOn: Boolean
        get() = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true)
        set(value) = prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, value).apply()

    var customCity: String
        get() = prefs.getString(KEY_CUSTOM_CITY, "Cupertino") ?: "Cupertino"
        set(value) = prefs.edit().putString(KEY_CUSTOM_CITY, value).apply()

    companion object {
        private const val KEY_CLOCK_STYLE = "key_clock_style"
        private const val KEY_COLOR_THEME = "key_color_theme"
        private const val KEY_IS_24_HOUR = "key_is_24_hour"
        private const val KEY_IS_CELSIUS = "key_is_celsius"
        private const val KEY_KEEP_SCREEN_ON = "key_keep_screen_on"
        private const val KEY_CUSTOM_CITY = "key_custom_city"
    }
}
