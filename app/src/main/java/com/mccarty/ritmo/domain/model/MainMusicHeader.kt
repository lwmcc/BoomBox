package com.mccarty.ritmo.domain.model


data class MusicHeader(
    var imageUrl: String? = null,
    var artistName: String = "",
    var albumName: String = "",
    var songName: String = "",
    var dataSet: Boolean = false,
)