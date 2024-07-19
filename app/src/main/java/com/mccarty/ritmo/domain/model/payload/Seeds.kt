package com.mccarty.ritmo.domain.model.payload

data class Seeds(
    val seeds: List<Seed>,
    val tracks: List<Track>
)

data class Seed(
    val afterFilteringSize: Int,
    val afterRelinkingSize: Int,
    val href: String,
    val id: String,
    val initialPoolSize: Int,
    val type: String
)
