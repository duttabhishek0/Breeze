package com.android.musicplayer.view.playlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.musicplayer.data.model.Song
import com.android.musicplayer.domain.usecase.DeleteSongUseCase
import com.android.musicplayer.domain.usecase.GetSongsUseCase
import com.android.musicplayer.domain.usecase.SaveSongDataUseCase

class PlaylistViewModel(
    private val saveSongDataUseCase: SaveSongDataUseCase,
    private val getSongsUseCase: GetSongsUseCase,
    private val deleteSongUseCase: DeleteSongUseCase
) : ViewModel() {


    val playlistData = MutableLiveData<List<Song>>()

    /**
     * Store the metadata of
     * the music inside ROOM
     *
     * @param song
     */
    fun saveSongData(song: Song) {
        saveSongDataUseCase.saveSongItem(song)
    }

    /**
     * Gets a list of music(s)
     * from ROOM
     *
     */
    fun getSongsFromDb() {
        playlistData.value = getSongsUseCase.getSongs()
    }

    /**
     * Removes passed music from
     * ROOM
     *
     * @param song
     */
    fun removeItemFromList(song: Song) {
        deleteSongUseCase.deleteSongItem(song)
        val list = playlistData.value as ArrayList<Song>
        list.remove(song)
        playlistData.value = list
    }
}