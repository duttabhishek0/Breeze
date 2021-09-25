package com.example.breeze.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.breeze.exoplayer.callbacks.MusicNotificationListener
import com.example.breeze.exoplayer.callbacks.MusicPlaybackPreparer
import com.example.breeze.exoplayer.callbacks.MusicPlayerEventListener
import com.example.breeze.extras.Constants.MEDIA_ROOT_ID
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val SERVICE_TAG = "MusicService"


@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var firebaseTrackSource : FirebaseTrackSource

    private  lateinit var  trackNotificationManager: TrackNotificationManager
    // Defines the lifetime of the co-routine
    private val serviceJob = Job()

    // Deals with the cancellation of the coroutine
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    // Current session about the media
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false

    private var isPlayerInitialized = false
    private lateinit var musicPlayerEventListener : MusicPlayerEventListener

    // Refers to the currently playing song
    private var currPlayingSong : MediaMetadataCompat? = null

    companion object{
        var currSongDuration = 0L
        private  set
    }
    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            firebaseTrackSource.fetchMediaData()
        }
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
        // Assignment of Session Token
        sessionToken = mediaSession.sessionToken


        trackNotificationManager = TrackNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicNotificationListener(this)
        ){
            currSongDuration = exoPlayer.duration
        }

        val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseTrackSource){
            currPlayingSong = it
            preparePlayer(
                firebaseTrackSource.tracks,
                it,
                true
            )
        }
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setPlayer(exoPlayer)  // To set the session player. Done internally by Dagger
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)
        trackNotificationManager.showNotification(exoPlayer)


    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession){
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseTrackSource.tracks[windowIndex].description
        }
    }

    private fun preparePlayer(
        tracks : List<MediaMetadataCompat>,
        itemToPlay : MediaMetadataCompat?,
        playNow : Boolean
    ){
        val currSongIndex = if(currPlayingSong == null) 0 else tracks.indexOf(itemToPlay)
        exoPlayer.setMediaSource(firebaseTrackSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(currSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return  BrowserRoot(MEDIA_ROOT_ID, null)
    }


    // For favourite tracks
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId){
            MEDIA_ROOT_ID ->{
                val resultSent = firebaseTrackSource.whenReady {isInitialized ->
                    if(isInitialized){
                        result.sendResult(firebaseTrackSource.asMediaItems())
                        if(!isPlayerInitialized && firebaseTrackSource.tracks.isNotEmpty()){
                            preparePlayer(firebaseTrackSource.tracks, firebaseTrackSource.tracks[0],false)
                            isPlayerInitialized = true;
                        }
                    }
                    else{
                        result.sendResult(null)
                    }
                }
                if(!resultSent){
                    result.detach()
                }
            }
        }
    }
}