package com.example.breeze.exoplayer.callbacks

import android.widget.Toast
import com.example.breeze.exoplayer.MusicService
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.Listener

class MusicPlayerEventListener(
    private val musicService: MusicService
) : Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if(playbackState == Player.STATE_READY){
            musicService.stopForeground(false)
        }
    }
    fun onPlayerErrorChanged(error: ExoPlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService, " An unknown error occured", Toast.LENGTH_LONG).show()
    }
}