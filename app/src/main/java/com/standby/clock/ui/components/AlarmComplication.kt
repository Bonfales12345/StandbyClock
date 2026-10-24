package com.standby.clock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standby.clock.ui.theme.SfNsRoundedFamily

@Composable
fun AlarmClockIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.54f)
        val radius = w * 0.37f
        val strokeW = w * 0.105f

        drawLine(
            color = color,
            start = Offset(center.x - radius * 0.55f, center.y + radius * 0.72f),
            end = Offset(center.x - radius * 0.82f, center.y + radius * 1.04f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(center.x + radius * 0.55f, center.y + radius * 0.72f),
            end = Offset(center.x + radius * 0.82f, center.y + radius * 1.04f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(center.x - radius * 0.96f, center.y - radius * 0.58f),
            end = Offset(center.x - radius * 0.48f, center.y - radius * 0.98f),
            strokeWidth = strokeW * 1.25f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(center.x + radius * 0.96f, center.y - radius * 0.58f),
            end = Offset(center.x + radius * 0.48f, center.y - radius * 0.98f),
            strokeWidth = strokeW * 1.25f,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = color,
            radius = radius,
            center = center
        )

        val handStroke = w * 0.085f
        drawLine(
            color = Color.Black,
            start = center,
            end = Offset(center.x, center.y - radius * 0.56f),
            strokeWidth = handStroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.Black,
            start = center,
            end = Offset(center.x + radius * 0.42f, center.y + radius * 0.28f),
            strokeWidth = handStroke,
            cap = StrokeCap.Round
        )
    }
}

private fun formatAlarmAnnotated(
    alarmText: String,
    mainFontSizeSp: Float,
    ampmFontSizeSp: Float,
    withSpace: Boolean
) = buildAnnotatedString {
    val clean = alarmText.trim()
    val amPmIndex = clean.indexOfFirst { it.isLetter() }
    if (amPmIndex > 0) {
        val timePart = clean.substring(0, amPmIndex).trim()
        val amPmPart = clean.substring(amPmIndex).trim().uppercase()
        withStyle(SpanStyle(fontSize = mainFontSizeSp.sp, fontWeight = FontWeight.SemiBold)) {
            append(timePart)
        }
        withStyle(SpanStyle(fontSize = ampmFontSizeSp.sp, fontWeight = FontWeight.SemiBold)) {
            append(if (withSpace) " $amPmPart" else amPmPart)
        }
    } else {
        withStyle(SpanStyle(fontSize = mainFontSizeSp.sp, fontWeight = FontWeight.SemiBold)) {
            append(clean)
        }
    }
}

@Composable
fun AlarmComplicationStacked(
    alarmText: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = 26.dp,
    textColor: Color = Color.White,
    fontSize: Float = 31f,
    onTap: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onTap
        ),
        horizontalAlignment = Alignment.Start
    ) {
        AlarmClockIcon(
            color = accentColor,
            size = iconSize
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatAlarmAnnotated(
                alarmText = alarmText,
                mainFontSizeSp = fontSize,
                ampmFontSizeSp = fontSize * 0.72f,
                withSpace = true
            ),
            color = textColor,
            fontFamily = SfNsRoundedFamily,
            letterSpacing = (-0.5).sp
        )
    }
}

@Composable
fun AlarmComplicationRow(
    alarmText: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp,
    textColor: Color = Color.White,
    fontSize: Int = 22,
    onTap: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onTap
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlarmClockIcon(
            color = accentColor,
            size = iconSize
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = formatAlarmAnnotated(
                alarmText = alarmText,
                mainFontSizeSp = fontSize.toFloat(),
                ampmFontSizeSp = fontSize * 0.76f,
                withSpace = false
            ),
            color = textColor,
            fontFamily = SfNsRoundedFamily,
            letterSpacing = (-0.4).sp
        )
    }
}
