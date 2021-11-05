package com.android.musicplayer.view.playlist

import com.android.musicplayer.data.model.Song

/**
 * To make an interaction between [PlaylistActivity]
 * & [PlaylistAdapter]
 *
 * @author Abhishek
 * */
interface OnPlaylistAdapterListener {

    fun playSong(song: Song, songs: ArrayList<Song>)

    fun removeSongItem(song: Song)
}