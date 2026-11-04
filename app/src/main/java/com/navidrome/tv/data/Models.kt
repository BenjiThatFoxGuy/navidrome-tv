package com.navidrome.tv.data

import com.google.gson.annotations.SerializedName

data class SubsonicResponseWrapper(
    @SerializedName("subsonic-response") val response: SubsonicResponse
)

data class SubsonicResponse(
    val status: String,
    val version: String = "1.16.1",
    val error: SubsonicError? = null,
    val albumList2: AlbumListDto? = null,
    val album: AlbumWithSongsDto? = null,
    val artists: ArtistsDto? = null,
    val artist: ArtistDetailDto? = null,
    val playlists: PlaylistsDto? = null,
    val playlist: PlaylistDetailDto? = null,
    val searchResult3: SearchResultDto? = null,
    val genres: GenresDto? = null,
    val starred2: AlbumListDto? = null
)

data class SubsonicError(val code: Int, val message: String)

data class AlbumListDto(val album: List<AlbumDto> = emptyList())

data class AlbumDto(
    val id: String,
    val name: String,
    val artist: String? = null,
    val artistId: String? = null,
    val coverArt: String? = null,
    val songCount: Int? = null,
    val duration: Int? = null,
    val year: Int? = null,
    val genre: String? = null,
    val created: String? = null
)

data class AlbumWithSongsDto(
    val id: String,
    val name: String,
    val artist: String? = null,
    val artistId: String? = null,
    val coverArt: String? = null,
    val songCount: Int? = null,
    val duration: Int? = null,
    val year: Int? = null,
    val genre: String? = null,
    val song: List<SongDto> = emptyList()
)

data class SongDto(
    val id: String,
    val parent: String? = null,
    val isDir: Boolean = false,
    val title: String,
    val album: String? = null,
    val artist: String? = null,
    val track: Int? = null,
    val year: Int? = null,
    val genre: String? = null,
    val coverArt: String? = null,
    val duration: Int = 0,
    val bitRate: Int? = null,
    val suffix: String? = null,
    val contentType: String? = null,
    val discNumber: Int? = null
)

data class ArtistsDto(val index: List<IndexDto> = emptyList())
data class IndexDto(val name: String, val artist: List<ArtistDto> = emptyList())
data class ArtistDto(
    val id: String,
    val name: String,
    val albumCount: Int? = null,
    val artistImageUrl: String? = null
)

data class ArtistDetailDto(
    val id: String,
    val name: String,
    val album: List<AlbumDto> = emptyList(),
    val artistImageUrl: String? = null
)

data class PlaylistsDto(val playlist: List<PlaylistDto> = emptyList())
data class PlaylistDto(
    val id: String,
    val name: String,
    val songCount: Int? = null,
    val coverArt: String? = null,
    val owner: String? = null
)

data class PlaylistDetailDto(
    val id: String,
    val name: String,
    val entry: List<SongDto> = emptyList(),
    val coverArt: String? = null
)

data class SearchResultDto(
    val artist: List<ArtistDto> = emptyList(),
    val album: List<AlbumDto> = emptyList(),
    val song: List<SongDto> = emptyList()
)

data class GenresDto(val genre: List<GenreDto> = emptyList())
data class GenreDto(val value: String, val songCount: Int? = null, val albumCount: Int? = null)

fun SubsonicResponseWrapper.unwrap(): SubsonicResponse {
    if (response.status != "ok") {
        throw SubsonicException(response.error?.message ?: "Subsonic error")
    }
    return response
}

class SubsonicException(message: String) : Exception(message)
