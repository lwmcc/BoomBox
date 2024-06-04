package com.mccarty.ritmo.viewmodel

sealed class PlayerAction {
    data class Seek(val position: Float): PlayerAction()
    data object Play: PlayerAction()
    data class PlayWithUri(val uri: String): PlayerAction()
    data object Skip: PlayerAction()
    data object Back: PlayerAction()
}