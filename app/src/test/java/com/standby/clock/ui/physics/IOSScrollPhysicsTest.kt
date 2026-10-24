package com.standby.clock.ui.physics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class IOSScrollPhysicsTest {

    @Test
    fun testRubberBandFormulaBoundaryProperties() {
        val d = 1000f

        assertEquals(0f, IOSScrollPhysics.rubberBandOffset(0f, d), 0.001f)

        assertEquals(0f, IOSScrollPhysics.rubberBandOffset(-50f, d), 0.001f)

        val offsetAtD = IOSScrollPhysics.rubberBandOffset(d, d)
        assertEquals(500f, offsetAtD, 0.001f)

        val offsetAt2D = IOSScrollPhysics.rubberBandOffset(2f * d, d)
        assertEquals(666.667f, offsetAt2D, 0.01f)

        val f100 = IOSScrollPhysics.rubberBandOffset(100f, d)
        val f200 = IOSScrollPhysics.rubberBandOffset(200f, d)
        val f300 = IOSScrollPhysics.rubberBandOffset(300f, d)
        assertTrue(f100 > 0f)
        assertTrue(f200 > f100)
        assertTrue(f300 > f200)

        val delta1 = f100 - 0f
        val delta2 = f200 - f100
        val delta3 = f300 - f200
        assertTrue("Resistance must increase as pull distance increases", delta1 > delta2)
        assertTrue("Resistance must increase as pull distance increases", delta2 > delta3)
    }

    @Test
    fun testInverseRubberBandFormulaExactRecovery() {
        val d = 1080f
        val testOvershoots = floatArrayOf(10f, 50f, 150f, 300f, 500f, 1000f, 2500f)

        for (x in testOvershoots) {
            val y = IOSScrollPhysics.rubberBandOffset(x, d)
            val recoveredX = IOSScrollPhysics.inverseRubberBandOffset(y, d)
            assertEquals("Recovered x must match original x for $x", x, recoveredX, 0.01f)
        }
    }

    @Test
    fun testExponentialDecayRateNormal() {
        val initialVelocity = 2000f
        val oneFrameSeconds = 1f / 60f

        val v1 = IOSScrollPhysics.decayVelocity(initialVelocity, oneFrameSeconds, IOSScrollPhysics.DECELERATION_RATE_NORMAL)
        assertEquals(initialVelocity * 0.998f, v1, 0.01f)

        val v60 = IOSScrollPhysics.decayVelocity(initialVelocity, 1.0f, IOSScrollPhysics.DECELERATION_RATE_NORMAL)
        val expected60 = initialVelocity * Math.pow(0.998, 60.0).toFloat()
        assertEquals(expected60, v60, 0.1f)

        assertTrue(v1 < initialVelocity)
        assertTrue(v60 < v1)
    }

    @Test
    fun testExponentialDecayRateFast() {
        val initialVelocity = 2000f
        val oneFrameSeconds = 1f / 60f

        val v1 = IOSScrollPhysics.decayVelocity(initialVelocity, oneFrameSeconds, IOSScrollPhysics.DECELERATION_RATE_FAST)
        assertEquals(initialVelocity * 0.99f, v1, 0.01f)
    }

    @Test
    fun testCriticallyDampedConstants() {

        assertEquals(1.0f, IOSScrollPhysics.DAMPING_RATIO_CRITICALLY_DAMPED, 0.0f)
        assertTrue(IOSScrollPhysics.RUBBER_BAND_SNAP_STIFFNESS >= 400f)
    }

    @Test
    fun testProjectedFlingDistance() {
        val v = 1000f
        val distance = IOSScrollPhysics.calculateProjectedFlingDistance(v, IOSScrollPhysics.DECELERATION_RATE_NORMAL)

        assertTrue("Projected distance must be positive for positive velocity", distance > 0f)
        assertTrue("Projected distance should be around 8325px", abs(distance - 8325f) < 50f)
    }
}
