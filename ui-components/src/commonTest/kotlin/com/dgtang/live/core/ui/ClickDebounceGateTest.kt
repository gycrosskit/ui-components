package com.dgtang.live.core.ui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

class ClickDebounceGateTest {
    @Test
    fun sameControlAllowsNextClickAfterQuietInterval() {
        val timeSource = TestTimeSource()
        val gate = ClickDebounceGate(intervalMillis = 500L, timeSource = timeSource)

        assertTrue(gate.tryAcquire())
        timeSource += 499.milliseconds
        assertFalse(gate.tryAcquire())
        timeSource += 500.milliseconds
        assertTrue(gate.tryAcquire())
    }

    @Test
    fun continuousClicksOnlyAllowFirstAction() {
        val timeSource = TestTimeSource()
        val gate = ClickDebounceGate(intervalMillis = 500L, timeSource = timeSource)

        assertTrue(gate.tryAcquire())
        repeat(10) {
            timeSource += 200.milliseconds
            assertFalse(gate.tryAcquire())
        }
        timeSource += 500.milliseconds
        assertTrue(gate.tryAcquire())
    }

    @Test
    fun differentControlsDoNotBlockEachOther() {
        val timeSource = TestTimeSource()
        val first = ClickDebounceGate(intervalMillis = 500L, timeSource = timeSource)
        val second = ClickDebounceGate(intervalMillis = 500L, timeSource = timeSource)

        assertTrue(first.tryAcquire())
        timeSource += 1.milliseconds
        assertTrue(second.tryAcquire())
    }
}
