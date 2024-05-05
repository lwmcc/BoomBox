package com.mccarty.ritmo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SpotifyBroadcastReceiver : BroadcastReceiver()  {
    override fun onReceive(context: Context?, intent: Intent?) {
        println("SpotifyBroadcastReceiver ***** BR ${intent?.action}")
        println("SpotifyBroadcastReceiver ***** BR ${intent?.toUri(Intent.URI_INTENT_SCHEME)}")
    }
}