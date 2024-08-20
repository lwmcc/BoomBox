package com.mccarty.ritmo.domain.playlists

sealed class PlaylistSelectAction {
    data class PlaylistSelect(val playlist: String?): PlaylistSelectAction()
}