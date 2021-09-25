package com.example.breeze.adapter

import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import androidx.recyclerview.widget.DiffUtil
import com.example.breeze.R
import javax.inject.Inject

class TrackAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseSongAdapter(R.layout.list_item){
    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val track = tracks[position]
        holder.itemView.apply {
            tvPrimary.text = track.title
            tvSecondary.text = track.subtitle
            glide.load(track.imageUrl).into(ivItemImage)

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(track)
                }
            }
        }
    }
}