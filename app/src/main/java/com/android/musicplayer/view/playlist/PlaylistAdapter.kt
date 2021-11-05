/*
  Copyright (c) 2021 ABHISHEK (https://github.com/duttabhishek0)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package com.android.musicplayer.view.playlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.android.musicplayer.R
import com.android.musicplayer.data.model.Song
import com.android.musicplayer.view.playlist.PlaylistAdapter.SongViewHolder
import kotlinx.android.synthetic.main.holder_song.view.*
import java.io.File
import kotlin.properties.Delegates

/**
 * This class is responsible for converting each data entry [Song]
 * into [SongViewHolder] that can then be added to the AdapterView.
 *
 * Created by Abhishek.
 */
internal class PlaylistAdapter(val mListener: OnPlaylistAdapterListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var songs: List<Song> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }


    /**
     * This method is called right when adapter is created &
     * is used to initialize ViewHolders
     *
     * @param parent
     * @param viewType
     * @return  RecyclerView.ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewSongItemHolder =
            LayoutInflater.from(parent.context).inflate(R.layout.holder_song, parent, false)
        return SongViewHolder(viewSongItemHolder)
    }


    /**
     * It is called for each ViewHolder to bind it to the adapter &
     * This is where we pass data to ViewHolder
     *
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SongViewHolder).onBind(getItem(position))
    }

    private fun getItem(position: Int): Song {
        return songs[position]
    }

    /**
     * This method returns the size of collection that
     * contains the items we want to display.
     *
     * @return Int
     */
    override fun getItemCount(): Int {
        return songs.size
    }

    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        fun onBind(song: Song) {
            itemView.music_item_name_text_view.text = song.title ?: ""

            song.clipArt?.let { nonNullImage ->

                itemView.music_item_avatar_image_view.load(File(nonNullImage)) {
                    crossfade(true)
                    placeholder(R.drawable.placeholde)
                    error(R.drawable.placeholde)
                    CachePolicy.ENABLED
                }
            }

            itemView.setOnLongClickListener {
                mListener.removeSongItem(song)
                true
            }

            itemView.setOnClickListener {
                mListener.playSong(song, songs as ArrayList<Song>)
            }
        }
    }
}
