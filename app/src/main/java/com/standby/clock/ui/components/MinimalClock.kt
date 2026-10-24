package com.standby.clock.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standby.clock.data.ColorTheme
import com.standby.clock.data.WeatherData
import com.standby.clock.services.TimeState
import com.standby.clock.ui.theme.SfNsRoundedFamily
import java.util.Locale

@Composable
fun MinimalClock(
    timeState: TimeState,
    weatherData: WeatherData,
    colorTheme: ColorTheme,
    accentColor: Color,
    is24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val primaryColor = colorTheme.getPrimaryColor(accentColor)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val h = maxHeight.value
        val clockFontSize = (h * 0.52f).sp
        val headerSize = (h * 0.082f).coerceIn(18f, 28f).sp

        val hourStr = if (is24Hour) timeState.hourString24 else timeState.hourString12
        val minStr = timeState.minuteString
        val secStr = String.format(Locale.US, "%02d", timeState.second)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                            append(timeState.dayOfWeekUpper)
                        }
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.SemiBold)) {
                            append(" ${timeState.monthUpper} ${timeState.dayOfMonth}")
                        }
                    },
                    fontFamily = SfNsRoundedFamily,
                    fontSize = headerSize,
                    letterSpacing = 0.6.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                if (weatherData.isAvailable) {
                    WeatherConditionIcon(
                        conditionType = weatherData.conditionType,
                        size = (headerSize.value * 1.05f).dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${weatherData.city} ${weatherData.tempString}",
                        color = Color(0xFFE5E5EA),
                        fontFamily = SfNsRoundedFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = headerSize
                    )
                } else {
                    Text(
                        text = "No Weather Information",
                        color = Color.White.copy(alpha = 0.6f),
                        fontFamily = SfNsRoundedFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = headerSize
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$hourStr:$minStr",
                    color = Color.White,
                    fontFamily = SfNsRoundedFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = clockFontSize,
                    letterSpacing = (-4).sp,
                    lineHeight = clockFontSize
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = secStr,
                    color = primaryColor,
                    fontFamily = SfNsRoundedFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (h * 0.22f).sp,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
    }
}
