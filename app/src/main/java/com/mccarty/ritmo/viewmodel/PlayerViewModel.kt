package com.mccarty.ritmo.viewmodel

import androidx.lifecycle.ViewModel
import com.mccarty.ritmo.domain.services.PlaybackService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlayerViewModel: ViewModel() {
    private var _playerState = MutableStateFlow<PlaybackService.Player?>(null)
    val playerState = _playerState.asStateFlow()

    private var _playbackDuration = MutableStateFlow(0L)
    val playbackDuration: StateFlow<Long> = _playbackDuration

    private var _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition

    private var _trackEnd = MutableSharedFlow<Boolean>(1)
    val trackEnd = _trackEnd

    //private var _currentUri = MutableStateFlow<String?>(null)
    //val currentUri = _currentUri.asStateFlow()

    fun setPlayerState(player: PlaybackService.Player)  {
        _playerState.value = player
    }

    //fun setCurrentUri(uri: String) = _currentUri.update { uri }

/*    private var job: Job? = null
    private fun setSliderPosition() {

        if (job != null) {
            job?.cancel()
            job = null
        }

        job = viewModelScope.launch {
            sliderTicker.getPlaybackPosition(
                position = playbackPosition.value,
                duration = playbackDuration.value,
                delay = MainActivity.TICKER_DELAY,
            ).collect { position ->
                _playbackPosition.update { position }
                if (position == playbackDuration.value) { // TODO: move to reuse
                    _playbackPosition.update { 0L }
                    _trackEnd.tryEmit(true)
                }
            }
        }
    }*/
/*
    fun setSliderPosition(
        position: Long,
        duration: Long,
        delay: Long,
        setPosition: Boolean,
    ) {
        playbackPosition(position.milliseconds.inWholeSeconds)
        playbackDuration(duration.milliseconds.inWholeSeconds)

        if (setPosition) {
            setSliderPosition()
        }
    }*/

/*    fun setPlaybackPosition(position: Int) {
        playbackPosition(position)
        setSliderPosition()
    }*/

    private inline fun <reified T : Number> playbackPosition(position: T) {
        _playbackPosition.value = position.toLong()
    }

    fun playbackDuration(duration: Long?) {
        _playbackDuration.update {
            duration ?: 0
        }
    }
}