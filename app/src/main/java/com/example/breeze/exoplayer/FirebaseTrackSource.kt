package com.example.breeze.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.example.breeze.data.network.TrackDatabase
import com.example.breeze.exoplayer.State.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/*--------------ROLE OF THIS CLASS:------------*/
/*
    1. Get the tracks from the Firebase DB.
    2. Convert Song Format to the required formats.
* */


class FirebaseTrackSource @Inject constructor(
    private val trackDatabase: TrackDatabase
) {


    // To store the metadata of the tracks
    var tracks = emptyList<MediaMetadataCompat>()

    // Function responsible for fetching data from Firebase
    // Switches from Main thread to I/O thread
    suspend fun fetchMediaData() = withContext(Dispatchers.IO){
        // Primarily it should be in the edge of initializing
        state = STATE_INITIALIZING

        val allTracks = trackDatabase.getAllTrack()
        tracks = allTracks.map{track ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_MEDIA_ID, track.id)
                .putString(METADATA_KEY_MEDIA_URI,track.trackUrl)
                .putString(METADATA_KEY_ARTIST, track.artistName)
                .putString(METADATA_KEY_TITLE,track.name)
                .putString(METADATA_KEY_ALBUM_ART_URI,track.image)
                .putString(METADATA_KEY_ART_URI,track.artistImage)
                .build()
        }
        // After completion of fetching of data
        state = STATE_INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory) : ConcatenatingMediaSource{
        val concatenatingMediaSource = ConcatenatingMediaSource()
        tracks.forEach { track ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(track.getString(METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }


    fun asMediaItems() = tracks.map{ track->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(track.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(track.description.title)
            .setMediaId(track.description.mediaId)
            .setIconUri(track.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()


    /* List of Lambda Functions */
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    // Only access the same thread
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            }
            else{
                field = value
            }
        }
    // Perform an action when music list is ready
    fun whenReady(action: (Boolean) -> Unit ) : Boolean {
        if(state == STATE_CREATED || state == STATE_INITIALIZING){
            // Music Source is NOT ready!
            onReadyListeners += action
            return false;
        }
        else{
            action(state == STATE_INITIALIZED)
            return true;
        }
    }
}


// To define the states of the track source
enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}