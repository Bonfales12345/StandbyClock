package com.standby.clock.ui

import android.app.Activity
import android.content.ContextWrapper
import android.os.Build
import android.service.dreams.DreamService
import android.view.HapticFeedbackConstants
import android.view.Window
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.standby.clock.data.ClockStyle
import com.standby.clock.data.ColorTheme
import com.standby.clock.ui.components.AnalogClock
import com.standby.clock.ui.components.DigitalClock
import com.standby.clock.ui.components.MinimalClock
import com.standby.clock.ui.components.RadialClock
import com.standby.clock.ui.physics.iosScrollGesture
import com.standby.clock.ui.physics.rememberIOSPagerState
import com.standby.clock.ui.theme.PureBlack
import com.standby.clock.ui.theme.StandByNightRed
import kotlin.math.abs

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val localDensity = LocalDensity.current

    val timeState by viewModel.timeState.collectAsState()
    val weatherData by viewModel.weatherData.collectAsState()
    val alarmData by viewModel.alarmData.collectAsState()
    val clockStyle by viewModel.clockStyle.collectAsState()
    val colorTheme by viewModel.colorTheme.collectAsState()
    val is24Hour by viewModel.is24Hour.collectAsState()
    val isNightRedMode by viewModel.isNightRedMode.collectAsState()
    val screenBrightness by viewModel.screenBrightness.collectAsState()
    val isReverseLandscape by viewModel.isReverseLandscape.collectAsState()

    val styles = ClockStyle.values()
    val iosPagerState = rememberIOSPagerState(
        pageCount = styles.size,
        initialPage = clockStyle.ordinal.coerceIn(0, styles.lastIndex)
    )

    LaunchedEffect(screenBrightness) {
        try {
            var ctx = view.context
            var targetWindow: Window? = null
            while (ctx != null) {
                if (ctx is Activity) {
                    targetWindow = ctx.window
                    break
                }
                if (ctx is DreamService) {
                    targetWindow = ctx.window
                    break
                }
                if (ctx is ContextWrapper) {
                    ctx = ctx.baseContext
                } else {
                    break
                }
            }
            if (targetWindow != null) {
                val lp = targetWindow.attributes
                if (lp.screenBrightness != screenBrightness) {
                    lp.screenBrightness = screenBrightness
                    targetWindow.attributes = lp
                }
            }
        } catch (e: Throwable) {}
    }

    LaunchedEffect(clockStyle) {
        if (iosPagerState.settledPage != clockStyle.ordinal && !iosPagerState.isScrollInProgress) {
            iosPagerState.scrollToPage(clockStyle.ordinal)
        }
    }

    LaunchedEffect(iosPagerState.settledPage) {
        val newStyle = styles[iosPagerState.settledPage]
        if (newStyle != clockStyle) {
            try {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            } catch (e: Throwable) {}
            viewModel.selectClockStyle(newStyle)
        }
    }

    val dynamicPrimary = MaterialTheme.colorScheme.primary
    val activeAccentColor = if (isNightRedMode) StandByNightRed else dynamicPrimary
    val effectiveColorTheme = if (isNightRedMode) ColorTheme.NIGHT_RED else colorTheme

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack),
        contentAlignment = Alignment.Center
    ) {
        val screenW = maxWidth
        val screenH = maxHeight

        val isForcedVertical = screenW < screenH
        val layoutW = if (isForcedVertical) screenH else screenW
        val layoutH = if (isForcedVertical) screenW else screenH
        val rotationAngle = if (isForcedVertical) {
            if (isReverseLandscape) -90f else 90f
        } else {
            0f
        }

        val containerHeightPx = with(localDensity) { layoutH.toPx() }
        LaunchedEffect(containerHeightPx) {
            iosPagerState.updatePageSize(containerHeightPx)
        }

        Box(
            modifier = Modifier
                .requiredSize(width = layoutW, height = layoutH)
                .graphicsLayer {
                    rotationZ = rotationAngle
                }
                .iosScrollGesture(
                    onDown = { iosPagerState.onPointerDown() },
                    onDragDelta = { deltaY -> iosPagerState.onDragDelta(deltaY) },
                    onUp = { velocity -> iosPagerState.onPointerUp(velocity) }
                ),
            contentAlignment = Alignment.Center
        ) {
            styles.forEachIndexed { pageIndex, pageStyle ->
                val pageOffset = iosPagerState.pageOffsetFraction(pageIndex)
                val absOffset = abs(pageOffset).coerceIn(0f, 1f)

                if (abs(pageOffset) <= 1.3f) {
                    val cardRadius = 24.dp * absOffset

                    val blurRadius = (18.dp * absOffset).coerceIn(0.dp, 20.dp)
                    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius.value > 0.5f) {
                        Modifier.blur(radius = blurRadius, edgeTreatment = BlurredEdgeTreatment.Rectangle)
                    } else {
                        Modifier
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {

                                translationY = (pageIndex.toFloat() * containerHeightPx) - iosPagerState.displayOffset

                                val scale = 1f - (absOffset * 0.12f)
                                scaleX = scale
                                scaleY = scale
                                alpha = (1f - (absOffset * 0.45f)).coerceIn(0f, 1f)
                                rotationX = (-pageOffset.coerceIn(-1.5f, 1.5f) * 16f)
                                cameraDistance = 16f * density
                            }
                            .clip(RoundedCornerShape(cardRadius))
                            .background(PureBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(blurModifier),
                            contentAlignment = Alignment.Center
                        ) {
                            when (pageStyle) {
                                ClockStyle.DIGITAL -> {
                                    DigitalClock(
                                        timeState = timeState,
                                        weatherData = weatherData,
                                        alarmData = alarmData,
                                        colorTheme = effectiveColorTheme,
                                        accentColor = activeAccentColor,
                                        is24Hour = is24Hour,
                                        onAlarmClick = { viewModel.openSystemClock() }
                                    )
                                }
                                ClockStyle.ANALOG -> {
                                    AnalogClock(
                                        timeState = timeState,
                                        weatherData = weatherData,
                                        colorTheme = effectiveColorTheme,
                                        accentColor = activeAccentColor
                                    )
                                }
                                ClockStyle.RADIAL -> {
                                    RadialClock(
                                        timeState = timeState,
                                        alarmData = alarmData,
                                        colorTheme = effectiveColorTheme,
                                        accentColor = activeAccentColor,
                                        onAlarmClick = { viewModel.openSystemClock() }
                                    )
                                }
                                ClockStyle.MINIMAL -> {
                                    MinimalClock(
                                        timeState = timeState,
                                        weatherData = weatherData,
                                        colorTheme = effectiveColorTheme,
                                        accentColor = activeAccentColor,
                                        is24Hour = is24Hour
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
