package com.mccarty.ritmo.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield
import javax.inject.Inject

class SliderTicker @Inject constructor(): Ticker {

    override suspend fun getPlaybackPosition(
        position: Long,
        delay: Long,
        duration: Long,
    ): Flow<Long> = flow {
        delay(delay)
        var index = position
        while (index <= duration) {
            yield()
            emit(index)
            delay(delay)
            index++
        }
    }
}