package com.mccarty.ritmo.viewmodel

import androidx.lifecycle.ViewModel
import com.mccarty.ritmo.domain.services.PlaybackService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlayerViewModel: ViewModel() {
    private var _playerState = MutableStateFlow<PlaybackService.Player?>(null)
    val playerState = _playerState.asStateFlow()

    private var _currentUri = MutableStateFlow<String?>(null)
    val currentUri = _currentUri.asStateFlow()

    fun setPlayerState(player: PlaybackService.Player)  {
        _playerState.value = player
    }

    fun setCurrentUri(uri: String) = _currentUri.update { uri }
}