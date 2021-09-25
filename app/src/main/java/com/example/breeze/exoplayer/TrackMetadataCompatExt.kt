package com.example.breeze.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.example.breeze.data.entities.Track

fun MediaMetadataCompat.toSong(): Track? {
    return description?.let {
        Track(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.mediaUri.toString(),
            it.iconUri.toString()
        )
    }
}
