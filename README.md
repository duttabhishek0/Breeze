<p align="center"><img src="https://user-images.githubusercontent.com/56694152/142888334-0a99ad8e-427c-45e8-91f6-38237729c3fd.png"width="150"></p>
<h1 align="center"><b>Breeze</b></h1> 
<h4 align="center">A modern music-playing app, built using Modern-Architectural practices.</h4>
<p align="center">
    <a href="https://github.com/duttabhishek0/Breeze/releases/">
        <img alt="GitHub release" src="https://img.shields.io/static/v1?label=Tag&message=v1.0.0&color=0D5AF5">
    </a>
    <a href="https://www.gnu.org/licenses/gpl-3.0">
        <img src="https://img.shields.io/badge/License-GPL%20v3-blue.svg">
    </a>
    <img alt="Minimum SDK" src="https://img.shields.io/badge/API-21%2B-32B5ED">
</p>
<p align="center">
  <img src ="https://user-images.githubusercontent.com/56694152/141653023-a9a9c279-2e01-41ff-af0a-1e5957c1b6e7.jpeg" width="250" height="500" />
  <img src ="https://user-images.githubusercontent.com/56694152/141653024-43a6eab2-411b-4fea-a910-188ef99a42fa.jpeg" width="250" height="500" />
  <img src ="https://user-images.githubusercontent.com/56694152/141653026-cd097f59-1448-47d1-ae0b-8f36f405442d.jpeg" width="250" height="500" />
  
</p>

## How to build the app
- clone the repo
- open the app in android studio
- run the application

## What is used in this app

- [ExoPlayer](https://github.com/google/ExoPlayer)  (The Heart of this App)
- [Coil](https://github.com/coil-kt/coil)   - For Image Loading
- [Mockito-Kotlin](https://github.com/mockito/mockito-kotlin) - Testing
- [Koin](https://github.com/InsertKoinIO/koin) - For Dependency Injection
- [MVVM](https://developer.android.com/jetpack/docs/guide) 

## Structure
<h3>The project is divided into two parts : </h3>

1. Player
2. App

### Player
As the name suggests, the **Player** part is responsible for handling all the events:
1. Play/Pause the Music
2. Handle user input from Notification Bar
3. Manages Next/Previous

### App
This is the part of the application, that manages the overall performance of the application. 
The structure of this applilcation:
```kotlin
- data
   -- model
      - Song
   -- repository
      - PlaylistRepositoryImp
   -- source
      - dao
        - SongDao
      - AppDatabase
```
The above structure forms the lowest level of the architecture for **Breeze**. This is the level, where a song is stored and all modification(Insertion of a song/ Deletion of a song/Retriveal of song) happens.

To store a song, a 'Song' Entity is created(aka Table), with the following schema:
```kotlin
data class Song(
    @PrimaryKey var id: Int,
    var songName: String?,
    var path: String,
    var artistName: String?,
    var albumArt: String?,
    var duration: String?,
    var type: Int = 0
) : ASong(id, songName, albumArt, artistName, path, type, duration), Parcelable
```
The following operations can be performed on the `Song` entity using `SongDao` :
```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
fun insert(song: Song): Long

@Query("SELECT * FROM Song")
fun loadAll(): MutableList<Song>

@Delete
fun delete(song: Song)

@Query("DELETE FROM Song")
fun deleteAll()

@Query("SELECT * FROM Song where id = :songId")
fun loadOneBySongId(songId: Long): Song?

@Query("SELECT * FROM Song where title = :songTitle")
fun loadOneBySongTitle(songTitle: String): Song?

@Update
fun update(song: Song)
```

In this project, `Koin` is used for dependecny injection. 


## License

```
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
 ```
