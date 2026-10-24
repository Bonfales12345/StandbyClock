package com.standby.clock.ui.components

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.standby.clock.data.ColorTheme
import com.standby.clock.data.WeatherData
import com.standby.clock.services.TimeState
import com.standby.clock.ui.theme.StandByOrange
import com.standby.clock.ui.theme.rememberRoundedTypeface
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

internal fun rayToRoundedRectDistance(
    cosA: Float,
    sinA: Float,
    halfW: Float,
    halfH: Float,
    cornerR: Float
): Float {
    val dx = abs(cosA).coerceAtLeast(1e-5f)
    val dy = abs(sinA).coerceAtLeast(1e-5f)
    val r = cornerR.coerceIn(0f, minOf(halfW, halfH))
    val cx = halfW - r
    val cy = halfH - r

    val tVert = halfW / dx
    if (tVert * dy <= cy) return tVert

    val tHoriz = halfH / dy
    if (tHoriz * dx <= cx) return tHoriz

    val b = dx * cx + dy * cy
    val c = cx * cx + cy * cy - r * r
    val disc = max(0f, b * b - c)
    return b + sqrt(disc)
}

@Composable
fun AnalogDialView(
    timeState: TimeState,
    secondHandColor: Color = StandByOrange,
    modifier: Modifier = Modifier
) {
    val roundedTypeface = rememberRoundedTypeface()

    Canvas(modifier = modifier.aspectRatio(1f)) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val halfS = w * 0.46f
        val cornerR = halfS * 0.38f

        for (i in 0 until 60) {
            val angleDeg = i * 6f
            val angleRad = Math.toRadians((angleDeg - 90).toDouble()).toFloat()
            val cosA = cos(angleRad)
            val sinA = sin(angleRad)

            val outerDist = rayToRoundedRectDistance(cosA, sinA, halfS, halfS, cornerR)
            val isCardinal = i % 15 == 0
            val isHourRay = i % 5 == 0

            val tickLen = when {
                isCardinal -> w * 0.085f
                isHourRay -> w * 0.165f
                else -> w * 0.056f
            }
            val innerDist = outerDist - tickLen
            val tickColor = if (isHourRay) Color.White else Color(0xFF48484A)
            val strokeW = if (isHourRay) w * 0.0115f else w * 0.0075f

            val start = Offset(
                center.x + innerDist * cosA,
                center.y + innerDist * sinA
            )
            val end = Offset(
                center.x + outerDist * cosA,
                center.y + outerDist * sinA
            )
            drawLine(
                color = tickColor,
                start = start,
                end = end,
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = w * 0.138f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = roundedTypeface
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                fontVariationSettings = "'wght' 700"
            }
            isAntiAlias = true
        }

        val fontMetrics = paint.fontMetrics
        val baselineOffset = -(fontMetrics.ascent + fontMetrics.descent) / 2f
        val cardinalOffset = halfS * 0.61f

        drawContext.canvas.nativeCanvas.drawText(
            "12",
            center.x,
            center.y - cardinalOffset + baselineOffset,
            paint
        )

        drawContext.canvas.nativeCanvas.drawText(
            "6",
            center.x,
            center.y + cardinalOffset + baselineOffset,
            paint
        )

        drawContext.canvas.nativeCanvas.drawText(
            "3",
            center.x + cardinalOffset,
            center.y + baselineOffset,
            paint
        )

        drawContext.canvas.nativeCanvas.drawText(
            "9",
            center.x - cardinalOffset,
            center.y + baselineOffset,
            paint
        )

        val hourRot = (timeState.hour12 % 12 + timeState.minute / 60f + timeState.second / 3600f) * 30f
        val minuteRot = (timeState.minute + timeState.second / 60f) * 6f
        val secondRot = timeState.secondSweepFraction * 360f

        rotate(hourRot, pivot = center) {
            drawAppleBatonHand(
                center = center,
                stemLength = halfS * 0.13f,
                totalLength = halfS * 0.54f,
                stemWidth = w * 0.015f,
                capsuleWidth = w * 0.044f,
                handColor = Color.White
            )
        }

        rotate(minuteRot, pivot = center) {
            drawAppleBatonHand(
                center = center,
                stemLength = halfS * 0.13f,
                totalLength = halfS * 0.85f,
                stemWidth = w * 0.015f,
                capsuleWidth = w * 0.040f,
                handColor = Color.White
            )
        }

        rotate(secondRot, pivot = center) {
            val secondLen = halfS * 0.94f
            val tailLen = halfS * 0.18f
            val secondWidth = w * 0.009f

            drawLine(
                color = secondHandColor,
                start = Offset(center.x, center.y + tailLen),
                end = Offset(center.x, center.y - secondLen),
                strokeWidth = secondWidth,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = Color.White,
                radius = w * 0.032f,
                center = center
            )

            drawCircle(
                color = secondHandColor,
                radius = w * 0.020f,
                center = center
            )

            drawCircle(
                color = Color.Black,
                radius = w * 0.008f,
                center = center
            )
        }
    }
}

internal fun DrawScope.drawAppleBatonHand(
    center: Offset,
    stemLength: Float,
    totalLength: Float,
    stemWidth: Float,
    capsuleWidth: Float,
    handColor: Color
) {

    drawLine(
        color = handColor,
        start = center,
        end = Offset(center.x, center.y - stemLength - capsuleWidth * 0.3f),
        strokeWidth = stemWidth,
        cap = StrokeCap.Round
    )

    val r = capsuleWidth / 2f
    val rect = RoundRect(
        left = center.x - r,
        top = center.y - totalLength,
        right = center.x + r,
        bottom = center.y - stemLength,
        cornerRadius = CornerRadius(r)
    )
    val path = Path().apply { addRoundRect(rect) }
    drawPath(path = path, color = handColor)
}

@Composable
fun AnalogClock(
    timeState: TimeState,
    weatherData: WeatherData,
    colorTheme: ColorTheme = ColorTheme.MATERIAL_YOU,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val secondColor = when (colorTheme) {
        ColorTheme.MATERIAL_YOU, ColorTheme.ORANGE -> StandByOrange
        else -> colorTheme.getPrimaryColor(accentColor)
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 42.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            AnalogDialView(
                timeState = timeState,
                secondHandColor = secondColor,
                modifier = Modifier.fillMaxHeight(0.92f)
            )
        }

        Spacer(modifier = Modifier.width(28.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(0.84f),
            contentAlignment = Alignment.CenterStart
        ) {
            WeatherWidgetLarge(weather = weatherData)
        }
    }
}
