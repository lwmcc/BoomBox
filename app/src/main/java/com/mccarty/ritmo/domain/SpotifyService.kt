package com.mccarty.ritmo.domain

import com.mccarty.ritmo.viewmodel.TrackSelectAction
import com.spotify.android.appremote.api.SpotifyAppRemote
import javax.inject.Inject

class SpotifyRemoteService @Inject constructor() : SpotifyService {
    override fun remote(remote: SpotifyAppRemote?, action: TrackSelectAction.TrackSelect) {
        if (remote == null) {
            return
        }

        remote.let {
            it.playerApi
                .playerState
                .setResultCallback { playerState ->
                    if (playerState.isPaused) {
                        if (playerState.track.uri.equals(action)) {
                            it.playerApi.resume()
                        } else {
                            it.playerApi.play(action.uri)
                        }
                    } else {
                        if (playerState.track.uri.equals(action.uri)) {
                            it.playerApi.pause()
                        } else {
                            it.playerApi.play(action.uri)
                        }
                    }
                }
                .setErrorCallback { throwable ->

                }
        }
    }
}

// TODO: move this
interface SpotifyService {
    fun remote(remote: SpotifyAppRemote?, action: TrackSelectAction.TrackSelect)
}
