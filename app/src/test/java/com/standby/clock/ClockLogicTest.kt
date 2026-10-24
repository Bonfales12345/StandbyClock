package com.standby.clock

import com.standby.clock.data.ClockStyle
import com.standby.clock.data.ColorTheme
import com.standby.clock.data.WeatherConditionType
import com.standby.clock.data.WeatherData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockLogicTest {

    @Test
    fun testClockStylesExist() {
        val styles = ClockStyle.values()
        assertEquals(4, styles.size)
        assertEquals("DIGITAL", ClockStyle.DIGITAL.title)
        assertEquals("ANALOG", ClockStyle.ANALOG.title)
        assertEquals("SOLAR", ClockStyle.RADIAL.title)
        assertEquals("MINIMAL", ClockStyle.MINIMAL.title)
    }

    @Test
    fun testColorThemesExist() {
        val themes = ColorTheme.values()
        assertTrue(themes.isNotEmpty())
        assertNotNull(ColorTheme.MATERIAL_YOU)
        assertNotNull(ColorTheme.CORAL)
        assertNotNull(ColorTheme.BLUE)
    }

    @Test
    fun testWeatherStrings() {
        val weather = WeatherData(
            city = "Cupertino",
            temperature = 63,
            condition = "Sunny",
            highTemp = 91,
            lowTemp = 62,
            conditionType = WeatherConditionType.SUNNY
        )
        assertEquals("63°", weather.tempString)
        assertEquals("H:91° L:62°", weather.highLowString)
    }

    @Test
    fun testNoWeatherInformationWhenNotAvailable() {
        val weather = WeatherData(isAvailable = false)
        assertEquals("No Weather Information", weather.city)
        assertEquals("No Weather Information", weather.condition)
        assertEquals("No Weather Information", weather.tempString)
        assertEquals("", weather.highLowString)
    }
}
