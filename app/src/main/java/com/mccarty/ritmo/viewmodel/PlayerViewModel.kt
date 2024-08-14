package com.mccarty.ritmo.viewmodel

import androidx.lifecycle.ViewModel
import com.mccarty.ritmo.domain.services.PlaybackService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class PlayerViewModel: ViewModel() {
    private var _playerState = MutableSharedFlow<PlaybackService.Player>()
    val playerState = _playerState.asSharedFlow()

    suspend fun setPlayerState(player: PlaybackService.Player) {
        _playerState.emit(player)
    }
}