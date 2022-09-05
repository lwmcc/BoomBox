package com.mccarty.ritmo.model

data class Queue(
    val currently_playing: CurrentlyPlaying,
    val queue: List<QueueX>
)