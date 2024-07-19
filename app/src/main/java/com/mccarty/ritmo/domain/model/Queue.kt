package com.mccarty.ritmo.domain.model

data class Queue(
    val currently_playing: CurrentlyPlaying,
    val queue: List<QueueX>
)