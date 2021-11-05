package com.android.musicplayer.view.playlist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.android.musicplayer.R
import com.android.musicplayer.data.model.Song
import com.android.musicplayer.view.songplayer.SongPlayerActivity
import com.android.player.BaseSongPlayerActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_playlist.*
import kotlinx.android.synthetic.main.content_main.*
import org.koin.android.viewmodel.ext.android.viewModel

class PlaylistActivity : BaseSongPlayerActivity(), OnPlaylistAdapterListener {

    private var adapter: PlaylistAdapter? = null
    private val viewModel: PlaylistViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)
        setSupportActionBar(toolbar)

        adapter = PlaylistAdapter(this)
        playlist_recycler_view.adapter = adapter


        fab.setOnClickListener { view ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (isReadPhoneStatePermissionGranted()) openMusicList()
                else requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION_READ_EXTERNAL_STORAGE_CODE
                )
            } else openMusicList()
        }

        viewModel.playlistData.observe(this, Observer {
            adapter?.songs = it
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.getSongsFromDb()
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_AUDIO_KEY) {
            data?.data?.let {
                addSong(it)
            }
        }
    }

    /**
     * Method to  Add song to the Playlist
     * Takes input the metadat of the song
     *
     * @param musicData
     */
    private fun addSong(musicData: Uri) {
        /*    val cursor = activity?.contentResolver?.query(musicData, null,null, null, null)*/
        val cursor = contentResolver?.query(
            musicData,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
            ), null, null, null
        )
        while (cursor?.moveToNext() == true) {
            val id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
            val path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
            val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
            val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
            val albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
            val duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

            val cursorAlbums = contentResolver?.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                MediaStore.Audio.Albums._ID + "=?",
                arrayOf<String>(albumId),
                null
            )
            var albumArt: String? = null
            if (cursorAlbums?.moveToFirst() == true) {
                albumArt =
                    cursorAlbums.getString(cursorAlbums.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
            }

            val song = Song(
                id.toInt(),
                title.toString(),
                path.toString(),
                artist,
                albumArt,
                duration,
                AUDIO_TYPE
            )
            viewModel.saveSongData(song)
        }
        cursor?.close()
    }

    /**
     * Method to check whether
     * storage READ access is
     * granted or not
     *
     * @return Boolean
     */
    private fun isReadPhoneStatePermissionGranted(): Boolean {
        val firstPermissionResult = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return firstPermissionResult == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Method to show an alert dialog box
     * before deleting a song.
     *
     * @param song
     */
    private fun showRemoveSongItemConfirmDialog(song: Song) {
        // setup the alert builder
        AlertDialog.Builder(this)
            .setMessage("Are you sure to remove this song?")
            // add a button
            .apply {
                setPositiveButton(R.string.yes) { _, _ ->
                    removeMusicFromList(song)
                }
                setNegativeButton(R.string.no) { _, _ ->
                    // User cancelled the dialog
                }
            }
            // create and show the alert dialog
            .show()
    }

    /**
     * Method invoked when user tries
     * to remove song from the playlist.
     *
     * @param song
     */
    override fun removeSongItem(song: Song) {
        showRemoveSongItemConfirmDialog(song)
    }

    /**
     * Method to remove music from the
     * playlist.
     *
     * @param song
     */
    private fun removeMusicFromList(song: Song) {
        songPlayerViewModel.stop()
        viewModel.removeItemFromList(song)
    }

    /**
     * Method invoked after storage
     * READ permission is asked.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_READ_EXTERNAL_STORAGE_CODE -> if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {// Permission Granted
                    openMusicList()
                } else {
                    // Permission Denied
                    Snackbar.make(
                        playlist_recycler_view,
                        getString(R.string.you_denied_permission),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Opens up the list including music
     *
     */
    private fun openMusicList() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_AUDIO_KEY)
    }

    /**
     * Method to start playing song.
     * Responsible for starting the
     * SongPlayerActivity
     *
     * @param song
     * @param songs
     */
    override fun playSong(song: Song, songs: ArrayList<Song>) {
        SongPlayerActivity.start(this, song, songs)
    }


    companion object {
        private val TAG = PlaylistActivity::class.java.name
        const val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE_CODE = 7031
        const val PICK_AUDIO_KEY = 2017
        const val AUDIO_TYPE = 3
    }
}
