package br.com.ourtalk.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HelpRequestTest {
    @Test fun excludesOldFutureAndSimulatedCoordinates() {
        val now = 200_000L
        val real = PositionFix(-21.1767, -47.8208, 12f, now, false)
        val text = "Perto da praça."
        val noLocation = helpRequestText(text, null, now)
        assertTrue(helpRequestText(text, real, now).contains("-21.176700, -47.820800"))
        assertEquals(noLocation, helpRequestText(text, real.copy(time = now - 120_001), now))
        assertEquals(noLocation, helpRequestText(text, real.copy(time = now + 1), now))
        assertEquals(noLocation, helpRequestText(text, real.copy(mock = true), now))
        assertEquals(noLocation, helpRequestText(text, real.copy(accuracy = Float.NaN), now))
    }
}
