package com.standby.clock.ui.physics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

object IOSScrollPhysics {

    const val DECELERATION_RATE_NORMAL = 0.998f

    const val DECELERATION_RATE_FAST = 0.99f

    const val DAMPING_RATIO_CRITICALLY_DAMPED = Spring.DampingRatioNoBouncy

    const val RUBBER_BAND_SNAP_STIFFNESS = 500f

    const val VELOCITY_STOP_THRESHOLD = 15f

    fun rubberBandOffset(overshootDistance: Float, containerDimension: Float): Float {
        if (overshootDistance <= 0f || containerDimension <= 0f) return 0f
        return (overshootDistance * containerDimension) / (overshootDistance + containerDimension)
    }

    fun inverseRubberBandOffset(rubberBandedOffset: Float, containerDimension: Float): Float {
        if (rubberBandedOffset <= 0f || containerDimension <= 0f) return 0f
        val clampedOffset = rubberBandedOffset.coerceAtMost(containerDimension * 0.999f)
        return (clampedOffset * containerDimension) / (containerDimension - clampedOffset)
    }

    fun decayVelocity(
        initialVelocity: Float,
        deltaTimeSeconds: Float,
        decelerationRate: Float = DECELERATION_RATE_NORMAL
    ): Float {
        val frames = deltaTimeSeconds * 60f
        return initialVelocity * decelerationRate.pow(frames)
    }

    fun calculateProjectedFlingDistance(
        initialVelocity: Float,
        decelerationRate: Float = DECELERATION_RATE_NORMAL
    ): Float {
        val coeff = -60f * ln(decelerationRate)
        return if (coeff > 0.0001f) initialVelocity / coeff else 0f
    }
}

@Stable
class IOSPagerState(
    val pageCount: Int,
    initialPage: Int = 0,
    private val coroutineScope: CoroutineScope
) {
    var pageSize: Float by mutableFloatStateOf(0f)

    val animatableOffset: Animatable<Float, AnimationVector1D> =
        Animatable(initialPage * 1000f)

    private var rawScrollOffset: Float = initialPage * 1000f

    var displayOffset: Float by mutableFloatStateOf(initialPage * 1000f)
        private set

    var isScrollInProgress: Boolean by mutableStateOf(false)
        private set

    var currentPage: Int by mutableIntStateOf(initialPage.coerceIn(0, pageCount - 1))
        private set

    var settledPage: Int by mutableIntStateOf(initialPage.coerceIn(0, pageCount - 1))
        private set

    private var animationJob: Job? = null
    private var isDragging: Boolean = false

    val maxScroll: Float
        get() = if (pageSize > 0f) (pageCount - 1) * pageSize else 0f

    fun pageOffsetFraction(page: Int): Float {
        if (pageSize <= 0f) return 0f
        val pageCenterOffset = page * pageSize
        return (displayOffset - pageCenterOffset) / pageSize
    }

    fun updatePageSize(newPageSize: Float) {
        if (pageSize != newPageSize && newPageSize > 0f) {
            val oldPageSize = pageSize
            pageSize = newPageSize
            if (oldPageSize == 0f) {
                val target = settledPage * newPageSize
                rawScrollOffset = target
                displayOffset = target
                coroutineScope.launch {
                    animatableOffset.snapTo(target)
                }
            } else {
                val ratio = newPageSize / oldPageSize
                rawScrollOffset *= ratio
                displayOffset *= ratio
                coroutineScope.launch {
                    animatableOffset.snapTo(displayOffset)
                }
            }
        }
    }

    fun onPointerDown() {
        if (animationJob?.isActive == true) {
            animationJob?.cancel()
            animationJob = null
        }
        isDragging = true
        isScrollInProgress = true

        val current = animatableOffset.value
        displayOffset = current

        if (pageSize > 0f) {
            if (current < 0f) {
                val y = -current
                val x = IOSScrollPhysics.inverseRubberBandOffset(y, pageSize)
                rawScrollOffset = -x
            } else if (current > maxScroll) {
                val y = current - maxScroll
                val x = IOSScrollPhysics.inverseRubberBandOffset(y, pageSize)
                rawScrollOffset = maxScroll + x
            } else {
                rawScrollOffset = current
            }
        } else {
            rawScrollOffset = current
        }
    }

    fun onDragDelta(deltaY: Float) {
        if (pageSize <= 0f) return

        rawScrollOffset -= deltaY

        val newDisplayOffset = if (rawScrollOffset < 0f) {
            val overshoot = -rawScrollOffset
            -IOSScrollPhysics.rubberBandOffset(overshoot, pageSize)
        } else if (rawScrollOffset > maxScroll) {
            val overshoot = rawScrollOffset - maxScroll
            maxScroll + IOSScrollPhysics.rubberBandOffset(overshoot, pageSize)
        } else {
            rawScrollOffset
        }

        displayOffset = newDisplayOffset
        updateCurrentPage(newDisplayOffset)
    }

    fun onPointerUp(velocity: Float) {
        isDragging = false
        if (pageSize <= 0f) {
            isScrollInProgress = false
            return
        }

        val scrollVelocity = -velocity

        animationJob = coroutineScope.launch {
            try {
                animatableOffset.snapTo(displayOffset)

                if (displayOffset < 0f) {

                    animatableOffset.animateTo(
                        targetValue = 0f,
                        initialVelocity = scrollVelocity,
                        animationSpec = spring(
                            dampingRatio = IOSScrollPhysics.DAMPING_RATIO_CRITICALLY_DAMPED,
                            stiffness = IOSScrollPhysics.RUBBER_BAND_SNAP_STIFFNESS
                        )
                    ) {
                        displayOffset = value
                        rawScrollOffset = value
                        updateCurrentPage(value)
                    }
                    settledPage = 0
                } else if (displayOffset > maxScroll) {

                    animatableOffset.animateTo(
                        targetValue = maxScroll,
                        initialVelocity = scrollVelocity,
                        animationSpec = spring(
                            dampingRatio = IOSScrollPhysics.DAMPING_RATIO_CRITICALLY_DAMPED,
                            stiffness = IOSScrollPhysics.RUBBER_BAND_SNAP_STIFFNESS
                        )
                    ) {
                        displayOffset = value
                        rawScrollOffset = value
                        updateCurrentPage(value)
                    }
                    settledPage = pageCount - 1
                } else {

                    val projectedDistance = IOSScrollPhysics.calculateProjectedFlingDistance(
                        scrollVelocity,
                        IOSScrollPhysics.DECELERATION_RATE_NORMAL
                    )
                    val projectedOffset = displayOffset + projectedDistance

                    val targetPage = if (abs(scrollVelocity) > 350f) {
                        if (scrollVelocity > 0) {
                            (currentPage + 1).coerceAtMost(pageCount - 1)
                        } else {
                            (currentPage - 1).coerceAtLeast(0)
                        }
                    } else {
                        (projectedOffset / pageSize).roundToInt().coerceIn(0, pageCount - 1)
                    }

                    val targetOffset = targetPage * pageSize

                    animatableOffset.animateTo(
                        targetValue = targetOffset,
                        initialVelocity = scrollVelocity,
                        animationSpec = spring(
                            dampingRatio = IOSScrollPhysics.DAMPING_RATIO_CRITICALLY_DAMPED,
                            stiffness = 450f
                        )
                    ) {
                        displayOffset = value
                        rawScrollOffset = value
                        updateCurrentPage(value)
                    }
                    settledPage = targetPage
                }
            } catch (e: CancellationException) {

            } finally {
                if (!isDragging) {
                    isScrollInProgress = false
                }
            }
        }
    }

    fun scrollToPage(page: Int) {
        val clamped = page.coerceIn(0, pageCount - 1)
        if (pageSize > 0f) {
            val target = clamped * pageSize
            animationJob?.cancel()
            animationJob = null
            coroutineScope.launch {
                animatableOffset.snapTo(target)
                displayOffset = target
                rawScrollOffset = target
                currentPage = clamped
                settledPage = clamped
                isScrollInProgress = false
            }
        } else {
            currentPage = clamped
            settledPage = clamped
        }
    }

    fun animateToPage(page: Int) {
        val clamped = page.coerceIn(0, pageCount - 1)
        if (pageSize > 0f) {
            val target = clamped * pageSize
            animationJob?.cancel()
            isScrollInProgress = true
            animationJob = coroutineScope.launch {
                try {
                    animatableOffset.animateTo(
                        targetValue = target,
                        animationSpec = spring(
                            dampingRatio = IOSScrollPhysics.DAMPING_RATIO_CRITICALLY_DAMPED,
                            stiffness = 450f
                        )
                    ) {
                        displayOffset = value
                        rawScrollOffset = value
                        updateCurrentPage(value)
                    }
                    settledPage = clamped
                } finally {
                    isScrollInProgress = false
                }
            }
        } else {
            currentPage = clamped
            settledPage = clamped
        }
    }

    private fun updateCurrentPage(offset: Float) {
        if (pageSize > 0f) {
            currentPage = (offset / pageSize).roundToInt().coerceIn(0, pageCount - 1)
        }
    }
}

@Stable
class IOSScrollState(
    initialScroll: Float = 0f,
    private val coroutineScope: CoroutineScope
) {
    var viewportDimension: Float by mutableFloatStateOf(0f)
    var contentDimension: Float by mutableFloatStateOf(0f)

    val maxScroll: Float
        get() = (contentDimension - viewportDimension).coerceAtLeast(0f)

    var displayOffset: Float by mutableFloatStateOf(initialScroll)
        private set

    val animatableOffset: Animatable<Float, AnimationVector1D> =
        Animatable(initialScroll)

    private var rawScrollOffset: Float = initialScroll
    var isScrollInProgress: Boolean by mutableStateOf(false)
        private set

    private var animationJob: Job? = null
    private var isDragging: Boolean = false

    fun onPointerDown() {
        if (animationJob?.isActive == true) {
            animationJob?.cancel()
            animationJob = null
        }
        isDragging = true
        isScrollInProgress = true

        val current = animatableOffset.value
        displayOffset = current

        val d = if (viewportDimension > 0f) viewportDimension else 1000f
        if (current < 0f) {
            val y = -current
            val x = IOSScrollPhysics.inverseRubberBandOffset(y, d)
            rawScrollOffset = -x
        } else if (current > maxScroll) {
            val y = current - maxScroll
            val x = IOSScrollPhysics.inverseRubberBandOffset(y, d)
            rawScrollOffset = maxScroll + x
        } else {
            rawScrollOffset = current
        }
    }

    fun onDragDelta(deltaPixels: Float) {
        rawScrollOffset -= deltaPixels
        val d = if (viewportDimension > 0f) viewportDimension else 1000f

        val newDisplayOffset = if (rawScrollOffset < 0f) {
            val overshoot = -rawScrollOffset
            -IOSScrollPhysics.rubberBandOffset(overshoot, d)
        } else if (rawScrollOffset > maxScroll) {
            val overshoot = rawScrollOffset - maxScroll
            maxScroll + IOSScrollPhysics.rubberBandOffset(overshoot, d)
        } else {
            rawScrollOffset
        }

        displayOffset = newDisplayOffset
    }

    fun onPointerUp(velocity: Float) {
        isDragging = false
        val d = if (viewportDimension > 0f) viewportDimension else 1000f
        val scrollVelocity = -velocity

        animationJob = coroutineScope.launch {
            try {
                animatableOffset.snapTo(displayOffset)

                if (displayOffset < 0f) {

                    animatableOffset.animateTo(
                        targetValue = 0f,
                        initialVelocity = scrollVelocity,
                        animationSpec = spring(
                            dampingRatio = IOSScrollPhysics.DAMPING_RATIO_CRITICALLY_DAMPED,
                            stiffness = IOSScrollPhysics.RUBBER_BAND_SNAP_STIFFNESS
                        )
                    ) {
                        displayOffset = value
                        rawScrollOffset = value
                    }
                } else if (displayOffset > maxScroll) {

                    animatableOffset.animateTo(
                        targetValue = maxScroll,
                        initialVelocity = scrollVelocity,
                        animationSpec = spring(
                            dampingRatio = IOSScrollPhysics.DAMPING_RATIO_CRITICALLY_DAMPED,
                            stiffness = IOSScrollPhysics.RUBBER_BAND_SNAP_STIFFNESS
                        )
                    ) {
                        displayOffset = value
                        rawScrollOffset = value
                    }
                } else if (abs(scrollVelocity) > IOSScrollPhysics.VELOCITY_STOP_THRESHOLD) {

                    var currentV = scrollVelocity
                    var lastTimeNanos = withFrameNanos { it }

                    while (abs(currentV) > IOSScrollPhysics.VELOCITY_STOP_THRESHOLD && isActive) {
                        withFrameNanos { frameTimeNanos ->
                            val dtSeconds = (frameTimeNanos - lastTimeNanos) / 1_000_000_000f
                            lastTimeNanos = frameTimeNanos

                            val decayedV = IOSScrollPhysics.decayVelocity(
                                currentV,
                                dtSeconds,
                                IOSScrollPhysics.DECELERATION_RATE_NORMAL
                            )
                            val delta = decayedV * dtSeconds
                            currentV = decayedV

                            rawScrollOffset += delta

                            if (rawScrollOffset < 0f) {
                                val overshoot = -rawScrollOffset
                                displayOffset = -IOSScrollPhysics.rubberBandOffset(overshoot, d)
                                currentV *= 0.85f
                                if (abs(currentV) < 50f) currentV = 0f
                            } else if (rawScrollOffset > maxScroll) {
                                val overshoot = rawScrollOffset - maxScroll
                                displayOffset = maxScroll + IOSScrollPhysics.rubberBandOffset(overshoot, d)
                                currentV *= 0.85f
                                if (abs(currentV) < 50f) currentV = 0f
                            } else {
                                displayOffset = rawScrollOffset
                            }
                        }
                    }

                    if (displayOffset < 0f) {
                        animatableOffset.snapTo(displayOffset)
                        animatableOffset.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = IOSScrollPhysics.DAMPING_RATIO_CRITICALLY_DAMPED,
                                stiffness = IOSScrollPhysics.RUBBER_BAND_SNAP_STIFFNESS
                            )
                        ) {
                            displayOffset = value
                            rawScrollOffset = value
                        }
                    } else if (displayOffset > maxScroll) {
                        animatableOffset.snapTo(displayOffset)
                        animatableOffset.animateTo(
                            targetValue = maxScroll,
                            animationSpec = spring(
                                dampingRatio = IOSScrollPhysics.DAMPING_RATIO_CRITICALLY_DAMPED,
                                stiffness = IOSScrollPhysics.RUBBER_BAND_SNAP_STIFFNESS
                            )
                        ) {
                            displayOffset = value
                            rawScrollOffset = value
                        }
                    }
                }
            } catch (e: CancellationException) {

            } finally {
                if (!isDragging) {
                    isScrollInProgress = false
                }
            }
        }
    }
}

fun Modifier.iosScrollGesture(
    onDown: () -> Unit,
    onDragDelta: (Float) -> Unit,
    onUp: (velocity: Float) -> Unit,
    onDoubleTap: (() -> Unit)? = null
): Modifier = this.pointerInput(Unit) {
    coroutineScope {
        var lastTapTime = 0L
        var lastTapPos = androidx.compose.ui.geometry.Offset.Zero

        while (isActive) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)

                onDown()

                val velocityTracker = VelocityTracker()
                velocityTracker.addPosition(down.uptimeMillis, down.position)

                val pointerId = down.id
                var isDragging = false
                val touchSlop = viewConfiguration.touchSlop

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == pointerId } ?: break

                    if (change.isConsumed) break

                    if (change.pressed) {
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        val deltaY = change.position.y - change.previousPosition.y
                        if (!isDragging) {
                            if (abs(change.position.y - down.position.y) > touchSlop) {
                                isDragging = true
                            }
                        }
                        if (isDragging) {
                            onDragDelta(deltaY)
                            change.consume()
                        }
                    } else {

                        val velocity = velocityTracker.calculateVelocity().y
                        onUp(velocity)

                        if (!isDragging && onDoubleTap != null) {
                            val upTime = change.uptimeMillis
                            val pos = change.position
                            if (upTime - lastTapTime < 320L && (pos - lastTapPos).getDistance() < 90f) {
                                onDoubleTap()
                                lastTapTime = 0L
                            } else {
                                lastTapTime = upTime
                                lastTapPos = pos
                            }
                        }
                        break
                    }
                }
            }
        }
    }
}

@Composable
fun rememberIOSPagerState(
    pageCount: Int,
    initialPage: Int = 0
): IOSPagerState {
    val coroutineScope = rememberCoroutineScope()
    return remember(pageCount) {
        IOSPagerState(
            pageCount = pageCount,
            initialPage = initialPage,
            coroutineScope = coroutineScope
        )
    }
}

@Composable
fun rememberIOSScrollState(
    initialScroll: Float = 0f
): IOSScrollState {
    val coroutineScope = rememberCoroutineScope()
    return remember {
        IOSScrollState(
            initialScroll = initialScroll,
            coroutineScope = coroutineScope
        )
    }
}
