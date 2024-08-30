package com.mccarty.ritmo.domain

import android.content.Context
import com.mccarty.ritmo.KeyConstants
import com.mccarty.ritmo.MainActivity
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SpotifyRemoteWrapper @Inject constructor(private val context: Context) {
    suspend fun spotifyConnection(): SpotifyAppRemote = suspendCoroutine { cont ->
        val connectionParams = ConnectionParams.Builder(KeyConstants.CLIENT_ID)
            .setRedirectUri(MainActivity.REDIRECT_URI)
            .showAuthView(true)
            .build()
        SpotifyAppRemote.connect(
            context,
            connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    cont.resume(appRemote)
                }

                override fun onFailure(throwable: Throwable) {
                    Timber.e(throwable.message)
                    cont.resumeWithException(throwable)
                }
            }
        )
    }
}