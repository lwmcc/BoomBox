package com.mccarty.ritmo.domain

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MediaTicker @AssistedInject constructor(
    @Assisted(POSITION) val position: Long,
    @Assisted(DURATION) val duration: Long,
    @Assisted(DELAY) val delay: Long,
)  {
    fun mediaTicker(): Flow<Long> = flow {
        delay(delay)
        var index = position
        while (index <= duration) {
            emit(index)
            delay(delay)
            index++
        }
    }

    @AssistedFactory
    interface MediaTickerFactory {
        fun create(
            @Assisted(POSITION) position: Long,
            @Assisted(DURATION) duration: Long,
            @Assisted(DELAY) delay: Long,
        ): MediaTicker
    }

    companion object {
        const val POSITION = "position"
        const val DURATION = "duration"
        const val DELAY = "delay"
    }
}