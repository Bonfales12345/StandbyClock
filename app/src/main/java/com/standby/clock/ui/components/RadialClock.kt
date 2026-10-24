package com.standby.clock.ui.components

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standby.clock.data.AlarmData
import com.standby.clock.data.ColorTheme
import com.standby.clock.services.TimeState
import com.standby.clock.ui.theme.SfNsRoundedFamily
import com.standby.clock.ui.theme.rememberRoundedTypeface
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadialClock(
    timeState: TimeState,
    alarmData: AlarmData,
    colorTheme: ColorTheme,
    accentColor: Color,
    onAlarmClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primaryColor = colorTheme.getPrimaryColor(accentColor)
    val roundedTypeface = rememberRoundedTypeface()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        val boxW = this@BoxWithConstraints.maxWidth
        val boxH = this@BoxWithConstraints.maxHeight
        val compFontSize = (boxH.value * 0.068f).coerceIn(17f, 24f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val halfW = size.width * 0.47f
            val halfH = size.height * 0.44f
            val cornerR = halfH * 0.36f

            for (i in 0 until 60) {
                val angleDeg = i * 6f
                val angleRad = Math.toRadians((angleDeg - 90).toDouble()).toFloat()
                val cosA = cos(angleRad)
                val sinA = sin(angleRad)

                val outerDist = rayToRoundedRectDistance(cosA, sinA, halfW, halfH, cornerR)
                val isTopOrBottomCardinal = (i == 0 || i == 30)
                val isLeftOrRightCardinal = (i == 15 || i == 45)
                val isDiagonalHourRay = (i % 5 == 0 && i % 15 != 0)

                val innerDist = when {
                    isTopOrBottomCardinal -> outerDist - size.height * 0.088f
                    isLeftOrRightCardinal -> outerDist - size.width * 0.092f
                    isDiagonalHourRay -> rayToRoundedRectDistance(
                        cosA,
                        sinA,
                        halfW * 0.56f,
                        halfH * 0.62f,
                        halfH * 0.48f
                    )
                    else -> outerDist - size.height * 0.085f
                }

                val tickColor = if (i % 5 == 0) Color(0xFF98989D) else Color(0xFF3A3A3C)
                val strokeW = if (i % 5 == 0) size.height * 0.0105f else size.height * 0.0055f

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
                textSize = size.height * 0.148f
                textAlign = android.graphics.Paint.Align.CENTER
                textScaleX = 1.18f
                typeface = roundedTypeface
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    fontVariationSettings = "'wght' 700"
                }
                isAntiAlias = true
            }

            val fontMetrics = paint.fontMetrics
            val baselineOffset = -(fontMetrics.ascent + fontMetrics.descent) / 2f

            drawContext.canvas.nativeCanvas.drawText(
                "12",
                center.x,
                center.y - halfH * 0.60f + baselineOffset,
                paint
            )

            drawContext.canvas.nativeCanvas.drawText(
                "6",
                center.x,
                center.y + halfH * 0.62f + baselineOffset,
                paint
            )

            drawContext.canvas.nativeCanvas.drawText(
                "3",
                center.x + halfW * 0.59f,
                center.y + baselineOffset,
                paint
            )

            drawContext.canvas.nativeCanvas.drawText(
                "9",
                center.x - halfW * 0.59f,
                center.y + baselineOffset,
                paint
            )

            val hourRot = (timeState.hour12 % 12 + timeState.minute / 60f + timeState.second / 3600f) * 30f
            val minuteRot = (timeState.minute + timeState.second / 60f) * 6f
            val secondRot = timeState.secondSweepFraction * 360f

            rotate(hourRot, pivot = center) {
                drawAppleBatonHand(
                    center = center,
                    stemLength = halfH * 0.13f,
                    totalLength = halfH * 0.52f,
                    stemWidth = size.height * 0.015f,
                    capsuleWidth = size.height * 0.044f,
                    handColor = Color.White
                )
            }

            rotate(minuteRot, pivot = center) {
                drawAppleBatonHand(
                    center = center,
                    stemLength = halfH * 0.13f,
                    totalLength = halfH * 0.88f,
                    stemWidth = size.height * 0.015f,
                    capsuleWidth = size.height * 0.040f,
                    handColor = Color.White
                )
            }

            rotate(secondRot, pivot = center) {
                val secondLen = halfH * 0.94f
                val tailLen = halfH * 0.19f
                val secondStroke = size.height * 0.0085f

                drawLine(
                    color = primaryColor,
                    start = Offset(center.x, center.y + tailLen),
                    end = Offset(center.x, center.y - secondLen),
                    strokeWidth = secondStroke,
                    cap = StrokeCap.Round
                )

                drawCircle(
                    color = Color.White,
                    radius = size.height * 0.028f,
                    center = center
                )

                drawCircle(
                    color = primaryColor,
                    radius = size.height * 0.018f,
                    center = center
                )

                drawCircle(
                    color = Color.Black,
                    radius = size.height * 0.008f,
                    center = center
                )
            }
        }

        if (alarmData.hasAlarm) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = -(boxW * 0.165f)),
                contentAlignment = Alignment.Center
            ) {
                AlarmComplicationRow(
                    alarmText = alarmData.timeCompact,
                    accentColor = primaryColor,
                    textColor = Color.White,
                    iconSize = (compFontSize * 0.95f).dp,
                    fontSize = compFontSize.toInt(),
                    onTap = onAlarmClick
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (boxW * 0.165f)),
            contentAlignment = Alignment.Center
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
                fontSize = compFontSize.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}
