package com.mccarty.ritmo.domain

import app.cash.turbine.test
import com.mccarty.ritmo.MainDispatcherRule
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SliderTickerTest {

    @JvmField
    @Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val ticker = SliderTicker(mainDispatcherRule.testDispatcher)

    @Test
    fun `assert playback position`() = runTest {
        val tickerFlow = ticker.getPlaybackPosition(
            position = 0,
            delay = 1L,
            duration = 3,
        ).flowOn(mainDispatcherRule.testDispatcher)

        tickerFlow.test {
            assertEquals(0, awaitItem())
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())

            awaitComplete()
        }
    }
}