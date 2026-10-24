package com.standby.clock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standby.clock.data.AlarmData
import com.standby.clock.data.ColorTheme
import com.standby.clock.data.WeatherData
import com.standby.clock.services.TimeState
import com.standby.clock.ui.theme.SfCompactRoundedFamily
import com.standby.clock.ui.theme.SfNsRoundedFamily

@Composable
fun DigitalColon(
    color: Color,
    heightDp: Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(width = heightDp * 0.17f, height = heightDp)) {
        val w = this.size.width
        val h = this.size.height
        val dotRadius = w * 0.42f
        val topCenter = Offset(w / 2f, h * 0.34f)
        val bottomCenter = Offset(w / 2f, h * 0.68f)

        drawCircle(
            color = color,
            radius = dotRadius,
            center = topCenter
        )
        drawCircle(
            color = color,
            radius = dotRadius,
            center = bottomCenter
        )
    }
}

@Composable
fun DigitalClock(
    timeState: TimeState,
    weatherData: WeatherData,
    alarmData: AlarmData,
    colorTheme: ColorTheme,
    accentColor: Color,
    is24Hour: Boolean = false,
    onAlarmClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primaryColor = colorTheme.getPrimaryColor(accentColor)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 42.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        val containerHeight = this@BoxWithConstraints.maxHeight
        val containerHVal = containerHeight.value
        val clockFontSize = (containerHVal * 0.88f).sp
        val sideColumnHeight = containerHeight * 0.72f
        val dateFontSize = (containerHVal * 0.088f).coerceIn(20f, 34f).sp
        val tempFontSize = (containerHVal * 0.105f).coerceIn(24f, 40f).sp
        val alarmFontSize = (containerHVal * 0.092f).coerceIn(22f, 34f)
        val alarmIconDp = (containerHVal * 0.076f).coerceIn(20f, 30f).dp

        val hourStr = if (is24Hour) timeState.hourString24 else timeState.hourString12
        val minStr = timeState.minuteString

        val clockTextStyle = TextStyle(
            fontFamily = SfCompactRoundedFamily,
            fontWeight = FontWeight.Bold,
            fontSize = clockFontSize,
            letterSpacing = (-3).sp,
            lineHeight = clockFontSize,
            textGeometricTransform = TextGeometricTransform(scaleX = 0.80f)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.offset(y = (-2).dp)
            ) {
                Text(
                    text = hourStr,
                    style = clockTextStyle,
                    color = primaryColor,
                    maxLines = 1,
                    softWrap = false
                )

                DigitalColon(
                    color = primaryColor,
                    heightDp = containerHeight * 0.72f,
                    modifier = Modifier.offset(x = (-6).dp)
                )

                Text(
                    text = minStr,
                    style = clockTextStyle,
                    color = primaryColor,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.offset(x = (-10).dp)
                )
            }

            Column(
                modifier = Modifier
                    .height(sideColumnHeight)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {

                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                                append(timeState.dayOfWeekUpper)
                            }
                            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.SemiBold)) {
                                append(" ${timeState.dayOfMonth}")
                            }
                        },
                        fontFamily = SfNsRoundedFamily,
                        fontSize = dateFontSize,
                        letterSpacing = 0.2.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    if (weatherData.isAvailable) {
                        Text(
                            text = "${weatherData.temperature}°",
                            color = Color.White,
                            fontFamily = SfNsRoundedFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = tempFontSize,
                            letterSpacing = (-0.5).sp
                        )
                    } else {
                        Text(
                            text = "No Weather Information",
                            color = Color.White.copy(alpha = 0.65f),
                            fontFamily = SfNsRoundedFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = (dateFontSize.value * 0.9f).sp,
                            letterSpacing = (-0.2).sp
                        )
                    }
                }

                if (alarmData.hasAlarm) {
                    AlarmComplicationStacked(
                        alarmText = alarmData.timeFormatted,
                        accentColor = primaryColor,
                        textColor = Color.White,
                        iconSize = alarmIconDp,
                        fontSize = alarmFontSize,
                        onTap = onAlarmClick
                    )
                }
            }
        }
    }
}
