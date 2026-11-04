package com.navidrome.tv.data

import retrofit2.http.GET
import retrofit2.http.Query

interface SubsonicApi {

    @GET("rest/ping.view")
    suspend fun ping(): SubsonicResponseWrapper

    @GET("rest/getAlbumList2.view")
    suspend fun getAlbumList2(
        @Query("type") type: String,
        @Query("size") size: Int = 20,
        @Query("offset") offset: Int = 0
    ): SubsonicResponseWrapper

    @GET("rest/getAlbum.view")
    suspend fun getAlbum(@Query("id") id: String): SubsonicResponseWrapper

    @GET("rest/getPlaylists.view")
    suspend fun getPlaylists(): SubsonicResponseWrapper

    @GET("rest/getPlaylist.view")
    suspend fun getPlaylist(@Query("id") id: String): SubsonicResponseWrapper

    @GET("rest/getArtists.view")
    suspend fun getArtists(): SubsonicResponseWrapper

    @GET("rest/getArtist.view")
    suspend fun getArtist(@Query("id") id: String): SubsonicResponseWrapper

    @GET("rest/search3.view")
    suspend fun search3(
        @Query("query") query: String,
        @Query("artistCount") artistCount: Int = 5,
        @Query("albumCount") albumCount: Int = 20,
        @Query("songCount") songCount: Int = 30
    ): SubsonicResponseWrapper

    @GET("rest/getGenres.view")
    suspend fun getGenres(): SubsonicResponseWrapper

    @GET("rest/getStarred2.view")
    suspend fun getStarred2(): SubsonicResponseWrapper

    @GET("rest/star.view")
    suspend fun star(@Query("id") id: String): SubsonicResponseWrapper

    @GET("rest/unstar.view")
    suspend fun unstar(@Query("id") id: String): SubsonicResponseWrapper

    @GET("rest/scrobble.view")
    suspend fun scrobble(
        @Query("id") id: String,
        @Query("submission") submission: Boolean = true
    ): SubsonicResponseWrapper
}
