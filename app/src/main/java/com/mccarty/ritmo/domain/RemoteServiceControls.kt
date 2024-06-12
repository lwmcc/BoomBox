package com.mccarty.ritmo.domain

import com.mccarty.ritmo.viewmodel.TrackSelectAction
import com.mccarty.ritmo.viewmodel.TrackSelectAction.TrackSelect as TrackSelect
import com.spotify.android.appremote.api.SpotifyAppRemote
import javax.inject.Inject

open class RemoteServiceControls @Inject constructor(): RemoteService {
    override fun onTrackSelected(remote: SpotifyAppRemote?, action: TrackSelect) {
        if (remote == null) {
            return
        }

        remote.let {
            it.playerApi
                .playerState
                .setResultCallback { playerState ->
                    if (playerState.isPaused) {
                        if (playerState.track.uri.equals(action.uri)) {
                            it.playerApi.resume()
                        } else {
                            it.playerApi.play(action.uri)
                        }
                    } else {
                        if (playerState.track.uri.equals(action.uri)) {
                            it.playerApi.skipPrevious()
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

interface RemoteService {
    fun onTrackSelected(remote: SpotifyAppRemote?, action: TrackSelect)
}
