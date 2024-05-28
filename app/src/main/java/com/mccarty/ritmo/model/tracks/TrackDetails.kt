package com.mccarty.ritmo.model.tracks

data class TrackDetails(
    val imageUrl: String,
    val name: String,
    val albumName: String,
    val artists: List<String>,
    val explicit: Boolean,
)
