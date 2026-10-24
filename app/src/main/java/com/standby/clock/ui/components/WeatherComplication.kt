package com.standby.clock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standby.clock.data.WeatherConditionType
import com.standby.clock.data.WeatherData
import com.standby.clock.ui.theme.SfNsRoundedFamily
import com.standby.clock.ui.theme.StandByYellow
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WeatherConditionIcon(
    conditionType: WeatherConditionType,
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
    sunColor: Color = StandByYellow
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)

        when (conditionType) {
            WeatherConditionType.SUNNY, WeatherConditionType.PARTLY_CLOUDY -> {
                val sunRadius = this.size.minDimension * 0.25f
                drawCircle(
                    color = sunColor,
                    radius = sunRadius,
                    center = center
                )
                val rayInner = sunRadius * 1.42f
                val rayOuter = sunRadius * 1.92f
                val strokeWidth = this.size.minDimension * 0.085f

                for (i in 0 until 8) {
                    val angle = Math.toRadians(i * 45.0).toFloat()
                    val start = Offset(
                        x = center.x + rayInner * cos(angle),
                        y = center.y + rayInner * sin(angle)
                    )
                    val end = Offset(
                        x = center.x + rayOuter * cos(angle),
                        y = center.y + rayOuter * sin(angle)
                    )
                    drawLine(
                        color = sunColor,
                        start = start,
                        end = end,
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                if (conditionType == WeatherConditionType.PARTLY_CLOUDY) {
                    val cloudColor = Color(0xFFE5E5EA)
                    drawRoundRect(
                        color = cloudColor,
                        topLeft = Offset(w * 0.28f, h * 0.50f),
                        size = Size(w * 0.58f, h * 0.32f),
                        cornerRadius = CornerRadius(h * 0.16f)
                    )
                    drawCircle(
                        color = cloudColor,
                        radius = w * 0.18f,
                        center = Offset(w * 0.48f, h * 0.50f)
                    )
                }
            }
            else -> {
                val cloudColor = Color(0xFFE5E5EA)
                drawRoundRect(
                    color = cloudColor,
                    topLeft = Offset(w * 0.14f, h * 0.38f),
                    size = Size(w * 0.72f, h * 0.38f),
                    cornerRadius = CornerRadius(h * 0.19f)
                )
                drawCircle(
                    color = cloudColor,
                    radius = w * 0.22f,
                    center = Offset(w * 0.42f, h * 0.38f)
                )
                drawCircle(
                    color = cloudColor,
                    radius = w * 0.17f,
                    center = Offset(w * 0.62f, h * 0.42f)
                )
            }
        }
    }
}

@Composable
fun WeatherWidgetLarge(
    weather: WeatherData,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxHeight()) {
        val h = maxHeight.value
        val citySize = (h * 0.105f).coerceIn(22f, 36f).sp
        val tempSize = (h * 0.36f).coerceIn(68f, 124f).sp
        val condSize = (h * 0.082f).coerceIn(18f, 28f).sp
        val hlSize = (h * 0.080f).coerceIn(18f, 27f).sp
        val iconDp = (h * 0.095f).coerceIn(22f, 34f).dp

        if (!weather.isAvailable) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "No Weather Information",
                    color = Color.White.copy(alpha = 0.75f),
                    fontFamily = SfNsRoundedFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (h * 0.095f).coerceIn(18f, 28f).sp,
                    letterSpacing = (-0.3).sp
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = weather.city,
                        color = Color.White,
                        fontFamily = SfNsRoundedFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = citySize,
                        letterSpacing = (-0.4).sp
                    )

                    Text(
                        text = weather.tempString,
                        color = Color.White,
                        fontFamily = SfNsRoundedFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = tempSize,
                        letterSpacing = (-2.5).sp,
                        lineHeight = tempSize,
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    WeatherConditionIcon(
                        conditionType = weather.conditionType,
                        size = iconDp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = weather.condition,
                        color = Color.White,
                        fontFamily = SfNsRoundedFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = condSize,
                        letterSpacing = (-0.2).sp
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = weather.highLowString,
                        color = Color.White,
                        fontFamily = SfNsRoundedFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = hlSize,
                        letterSpacing = (-0.2).sp
                    )
                }
            }
        }
    }
}
