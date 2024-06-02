package com.mccarty.ritmo.model.payload

data class PlaybackState(
    val actions: Actions,
    val context: Context,
    val currently_playing_type: String,
    val device: Device,
    val is_playing: Boolean,
    val item: Item,
    val progress_ms: Int,
    val repeat_state: String,
    val shuffle_state: Boolean,
    val timestamp: Long
)

data class Actions(
    val interrupting_playback: Boolean,
    val pausing: Boolean,
    val resuming: Boolean,
    val seeking: Boolean,
    val skipping_next: Boolean,
    val skipping_prev: Boolean,
    val toggling_repeat_context: Boolean,
    val toggling_repeat_track: Boolean,
    val toggling_shuffle: Boolean,
    val transferring_playback: Boolean
)

data class Device(
    val id: String,
    val is_active: Boolean,
    val is_private_session: Boolean,
    val is_restricted: Boolean,
    val name: String,
    val supports_volume: Boolean,
    val type: String,
    val volume_percent: Int
)