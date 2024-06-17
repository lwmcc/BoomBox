package com.mccarty.ritmo.viewmodel

sealed class PlayerControlAction {
    data class Seek(val position: Float): PlayerControlAction() // TODO: Long
    data class Play(val pausedPosition: Long): PlayerControlAction()
    data class PlayWithUri(val uri: String): PlayerControlAction()
    data object Skip: PlayerControlAction()
    data object Back: PlayerControlAction()
}