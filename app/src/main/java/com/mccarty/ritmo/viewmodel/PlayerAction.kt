package com.mccarty.ritmo.viewmodel

sealed class PlayerAction {
    data class Seek(val position: Float): PlayerAction()
    data object Play: PlayerAction()
    data object Pause: PlayerAction()
    data object Skip: PlayerAction()
    data object Back: PlayerAction()
}