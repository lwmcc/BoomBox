package com.mccarty.ritmo.domain

import com.mccarty.ritmo.MainActivity
import com.mccarty.ritmo.viewmodel.Playlist
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState

interface SliderPosition {
    fun resumePlayback(position: Long, playerState: PlayerState, remote: SpotifyAppRemote)

    fun newIndex(index: Int): Int

    fun getUri(index: Int): String

    fun cancelJob()

    fun setSliderPosition(
        position: Long,
        duration: Long,
        delay: Long,
        setPosition: Boolean = false,
    )

    fun setPlaylistData(playlist: Playlist?)

    fun setPlaybackPosition(position: Int)
}