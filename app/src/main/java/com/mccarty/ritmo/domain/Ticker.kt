package com.mccarty.ritmo.domain

import kotlinx.coroutines.flow.Flow

interface Ticker {
    suspend fun getPlaybackPosition(position: Long, delay: Long, duration: Long): Flow<Long>
}