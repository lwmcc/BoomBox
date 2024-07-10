package com.mccarty.ritmo.domain

import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState

interface SliderPosition {
    fun resumePlayback(position: Long, playerState: PlayerState, remote: SpotifyAppRemote,)
}