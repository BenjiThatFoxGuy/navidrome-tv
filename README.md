# Navidrome TV

Native Android TV client for a [Navidrome](https://www.navidrome.org/) music server
(Subsonic API). Apple TV Music-inspired 10-foot UI built with Jetpack Compose for TV.

## Stack
- Kotlin · Jetpack Compose for TV
- AndroidX Media3 (ExoPlayer + MediaSession)
- Retrofit + OkHttp (Subsonic API)
- Coil (image loading)
- MVVM + Coroutines/Flow
- DataStore (credentials)

## Features
- Subsonic/Navidrome authentication
- Home discovery: Recently Played, Recently Added, Made for You, Favorites
- Library: Albums, Playlists, Artists, Recently Added
- Album detail with tracklist and Play / Shuffle
- Immersive Now Playing with blurred artwork background
- Search with on-screen alphabetical keyboard
- Background playback via MediaSessionService
- D-pad optimized focus states

## Build

```bash
./gradlew :app:assembleDebug

