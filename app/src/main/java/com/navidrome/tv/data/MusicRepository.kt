package com.navidrome.tv.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepository(
    private val apiProvider: () -> SubsonicApi,
    private val credentialsProvider: () -> Credentials?
) {

    private val api get() = apiProvider()

    suspend fun recentAlbums(size: Int = 20) = io {
        api.getAlbumList2("recent", size).unwrap().albumList2?.album ?: emptyList()
    }

    suspend fun newestAlbums(size: Int = 20) = io {
        api.getAlbumList2("newest", size).unwrap().albumList2?.album ?: emptyList()
    }

    suspend fun randomAlbums(size: Int = 20) = io {
        api.getAlbumList2("random", size).unwrap().albumList2?.album ?: emptyList()
    }

    suspend fun frequentAlbums(size: Int = 20) = io {
        api.getAlbumList2("frequent", size).unwrap().albumList2?.album ?: emptyList()
    }

    suspend fun alphabeticalAlbums(size: Int = 100) = io {
        api.getAlbumList2("alphabeticalByName", size).unwrap().albumList2?.album ?: emptyList()
    }

    suspend fun starredAlbums() = io {
        api.getStarred2().unwrap().starred2?.album ?: emptyList()
    }

    suspend fun album(id: String): AlbumWithSongsDto = io {
        api.getAlbum(id).unwrap().album ?: throw SubsonicException("Album missing")
    }

    suspend fun playlists(): List<PlaylistDto> = io {
        api.getPlaylists().unwrap().playlists?.playlist ?: emptyList()
    }

    suspend fun playlist(id: String): PlaylistDetailDto = io {
        api.getPlaylist(id).unwrap().playlist ?: throw SubsonicException("Playlist missing")
    }

    suspend fun artists(): List<ArtistDto> = io {
        api.getArtists().unwrap().artists?.index?.flatMap { it.artist } ?: emptyList()
    }

    suspend fun artist(id: String): ArtistDetailDto = io {
        api.getArtist(id).unwrap().artist ?: throw SubsonicException("Artist missing")
    }

    suspend fun genres(): List<GenreDto> = io {
        api.getGenres().unwrap().genres?.genre ?: emptyList()
    }

    suspend fun search(query: String): SearchResultDto = io {
        api.search3(query).unwrap().searchResult3 ?: SearchResultDto()
    }

    suspend fun star(id: String) = io { runCatching { api.star(id) } }
    suspend fun unstar(id: String) = io { runCatching { api.unstar(id) } }
    suspend fun scrobble(id: String) = io { runCatching { api.scrobble(id) } }

    fun streamUrl(songId: String): String {
        val creds = credentialsProvider() ?: error("No credentials")
        return buildStreamUrl(creds, songId)
    }

    fun coverUrl(coverArtId: String?, size: Int = 600): String? =
        coverArtUrl(credentialsProvider(), coverArtId, size)

    private suspend fun <T> io(block: suspend () -> T): T = withContext(Dispatchers.IO) { block() }
}
