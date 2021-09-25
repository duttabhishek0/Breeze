package com.example.breeze.data.network

import com.example.breeze.data.entities.Track
import com.example.breeze.extras.Constants.TRACK_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TrackDatabase {
    private val firestore = FirebaseFirestore.getInstance()
    private val trackCollection = firestore.collection(TRACK_COLLECTION)

    suspend fun getAllTrack() : List<Track>{
        return try{
            trackCollection.get().await().toObjects(Track::class.java)
        }
        catch (e :Exception){
            emptyList()
        }
    }   
}