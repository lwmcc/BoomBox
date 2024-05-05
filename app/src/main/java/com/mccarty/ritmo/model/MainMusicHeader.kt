package com.mccarty.ritmo.model

import com.spotify.protocol.types.ImageUri

data class MainMusicHeader(
    var imageUrl: String? = "",
    var artistName: String = "",
    var albumName: String = "",
    var songName: String = "",
)

data class MusicHeader(
    var imageUrl: ImageUri? = null,
    var artistName: String = "",
    var albumName: String = "",
    var songName: String = "",
)