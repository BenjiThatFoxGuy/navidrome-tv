package com.navidrome.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.session.MediaController
import androidx.tv.material3.Text
import com.navidrome.tv.data.AlbumDto
import com.navidrome.tv.data.ArtistDto
import com.navidrome.tv.data.PlaylistDto
import com.navidrome.tv.di.ServiceLocator
import com.navidrome.tv.ui.components.MediaCard

private sealed class LibTab(val label: String) {
    data object Albums : LibTab("Albums")
    data object Playlists : LibTab("Playlists")
    data object Artists : LibTab("Artists")
    data object Recent : LibTab("Recently Added")
}

@Composable
fun LibraryScreen(
    onOpenAlbum: (String, String) -> Unit,
    controller: MediaController?
) {
    var tab by remember { mutableStateOf<LibTab>(LibTab.Albums) }
    var albums by remember { mutableStateOf<List<AlbumDto>>(emptyList()) }
    var recent by remember { mutableStateOf<List<AlbumDto>>(emptyList()) }
    var playlists by remember { mutableStateOf<List<PlaylistDto>>(emptyList()) }
    var artists by remember { mutableStateOf<List<ArtistDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        runCatching { albums = ServiceLocator.repository.alphabeticalAlbums(200) }
        runCatching { recent = ServiceLocator.repository.newestAlbums(60) }
        runCatching { playlists = ServiceLocator.repository.playlists() }
        runCatching { artists = ServiceLocator.repository.artists() }
    }

    Row(Modifier.fillMaxSize().padding(top = 96.dp)) {
        Column(
            Modifier
                .width(240.dp)
                .fillMaxHeight()
                .padding(horizontal = 32.dp)
        ) {
            Text(
                "Library",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            LibTabItem(LibTab.Recent, tab) { tab = it }
            LibTabItem(LibTab.Albums, tab) { tab = it }
            LibTabItem(LibTab.Playlists, tab) { tab = it }
            LibTabItem(LibTab.Artists, tab) { tab = it }
        }

        Box(Modifier.fillMaxSize().padding(end = 48.dp)) {
            when (tab) {
                LibTab.Albums -> AlbumGrid(albums, onOpenAlbum)
                LibTab.Recent -> AlbumGrid(recent, onOpenAlbum)
                LibTab.Playlists -> PlaylistGrid(playlists)
                LibTab.Artists -> ArtistGrid(artists)
            }
        }
    }
}

@Composable
private fun LibTabItem(item: LibTab, current: LibTab, onSelect: (LibTab) -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val bg = when {
        current == item -> Color.White.copy(alpha = 0.15f)
        focused -> Color.White.copy(alpha = 0.08f)
        else -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(interactionSource = interaction, indication = null) { onSelect(item) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            item.label,
            color = Color.White.copy(alpha = if (current == item) 1f else 0.7f),
            fontWeight = if (current == item) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun AlbumGrid(albums: List<AlbumDto>, onOpenAlbum: (String, String) -> Unit) {
    if (albums.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nothing here yet", color = Color.White.copy(alpha = 0.5f))
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(180.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(albums, key = { it.id }) { album ->
            MediaCard(
                title = album.name,
                subtitle = album.artist,
                imageUrl = ServiceLocator.repository.coverUrl(album.coverArt),
                onClick = { onOpenAlbum(album.id, album.name) }
            )
        }
    }
}

@Composable
private fun PlaylistGrid(playlists: List<PlaylistDto>) {
    if (playlists.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No playlists", color = Color.White.copy(alpha = 0.5f))
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(180.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(playlists, key = { it.id }) { pl ->
            MediaCard(
                title = pl.name,
                subtitle = pl.songCount?.let { "$it songs" },
                imageUrl = ServiceLocator.repository.coverUrl(pl.coverArt),
                onClick = { }
            )
        }
    }
}

@Composable
private fun ArtistGrid(artists: List<ArtistDto>) {
    if (artists.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No artists", color = Color.White.copy(alpha = 0.5f))
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(180.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(artists, key = { it.id }) { artist ->
            MediaCard(
                title = artist.name,
                subtitle = artist.albumCount?.let { "$it albums" },
                imageUrl = artist.artistImageUrl,
                onClick = { }
            )
        }
    }
}
