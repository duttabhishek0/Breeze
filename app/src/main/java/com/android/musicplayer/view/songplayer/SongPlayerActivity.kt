/**
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


package com.android.musicplayer.view.songplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.lifecycle.Observer
import coil.load
import coil.request.CachePolicy
import com.android.musicplayer.R
import com.android.musicplayer.data.model.Song
import com.android.player.BaseSongPlayerActivity
import com.android.player.model.ASong
import com.android.player.util.OnSwipeTouchListener
import com.android.player.util.formatTimeInMillisToString
import kotlinx.android.synthetic.main.activity_song_player.*
import java.io.File

class SongPlayerActivity : BaseSongPlayerActivity() {


    private var mSong: Song? = null
    private var mSongList: MutableList<ASong>? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.extras?.apply {
            if (containsKey(SONG_LIST_KEY)) {
                mSongList = getParcelableArrayList(SONG_LIST_KEY)
            }

            if (containsKey(ASong::class.java.name)) {
                mSong = getParcelable<ASong>(ASong::class.java.name) as Song
                mSong?.let {
                    mSongList?.let { it1 -> play(it1, it) }
                    loadInitialData(it)
                }
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_player)

        onNewIntent(intent)

        with(songPlayerViewModel) {

            songDurationData.observe(this@SongPlayerActivity, Observer {
                song_player_progress_seek_bar.max = it
            })

            songPositionTextData.observe(this@SongPlayerActivity,
                Observer { t -> song_player_passed_time_text_view.text = t })

            songPositionData.observe(this@SongPlayerActivity, {
                song_player_progress_seek_bar.progress = it
            })

            isRepeatData.observe(this@SongPlayerActivity, {
                song_player_repeat_image_view.setImageResource(
                    if (it) R.drawable.ic_repeat_one_color_primary_vector
                    else R.drawable.ic_repeat_one_black_vector
                )
            })

            isShuffleData.observe(this@SongPlayerActivity, {
                song_player_shuffle_image_view.setImageResource(
                    if (it) R.drawable.ic_shuffle_color_primary_vector
                    else R.drawable.ic_shuffle_black_vector
                )
            })

            isPlayData.observe(this@SongPlayerActivity, {
                song_player_toggle_image_view.setImageResource(if (it) R.drawable.ic_pause_vector else R.drawable.ic_play_vector)
            })

            playerData.observe(this@SongPlayerActivity, {
                loadInitialData(it)
            })
        }

        song_player_container.setOnTouchListener(object :
            OnSwipeTouchListener(this@SongPlayerActivity) {
            override fun onSwipeRight() {
                if (mSongList?.size ?: 0 > 1) previous()

            }

            override fun onSwipeLeft() {
                if (mSongList?.size ?: 0 > 1) next()
            }
        })

        song_player_progress_seek_bar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) seekTo(p1.toLong())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                //Nothing to do here
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                //Nothing to do here
            }

        })

        song_player_skip_next_image_view.setOnClickListener {
            next()
        }

        song_player_skip_back_image_view.setOnClickListener {
            previous()
        }

        song_player_toggle_image_view.setOnClickListener {
            toggle()
        }

        song_player_shuffle_image_view.setOnClickListener {
            shuffle()
        }

        song_player_repeat_image_view.setOnClickListener {
            repeat()
        }
    }

    /**
     * Load the metadata of the
     * Music
     *
     * @param aSong
     */
    private fun loadInitialData(aSong: ASong) {
        song_player_title_text_view.text = aSong.title
        song_player_singer_name_text_view.text = aSong.artist
        song_player_total_time_text_view.text =
            formatTimeInMillisToString(aSong.length?.toLong() ?: 0L)

        if (aSong.clipArt.isNullOrEmpty()) song_player_image_view.setImageResource(R.drawable.placeholde)
        aSong.clipArt?.let {
            song_player_image_view.load(File(it)) {
                CachePolicy.ENABLED
            }
        }
    }

    companion object {
        private val TAG = SongPlayerActivity::class.java.name

        /**
         * Starts playing the provided music.
         *
         * @param context
         * @param song
         * @param songList
         */
        fun start(context: Context, song: Song, songList: ArrayList<Song>) {
            val intent = Intent(context, SongPlayerActivity::class.java).apply {
                putExtra(ASong::class.java.name, song)
                putExtra(SONG_LIST_KEY, songList)
            }
            context.startActivity(intent)
        }
    }
}