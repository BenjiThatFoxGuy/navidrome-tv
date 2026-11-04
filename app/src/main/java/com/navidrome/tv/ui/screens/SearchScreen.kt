package com.navidrome.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.session.MediaController
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.navidrome.tv.data.AlbumDto
import com.navidrome.tv.data.SongDto
import com.navidrome.tv.di.ServiceLocator
import com.navidrome.tv.ui.components.MediaCard
import com.navidrome.tv.ui.components.SectionHeader
import kotlinx.coroutines.delay

private val ALPHABET: List<String> =
    (('A'..'Z').toList() + ('0'..'9').toList()).map { it.toString() } +
        listOf("SPACE", "DEL", "CLEAR")

@Composable
fun SearchScreen(
    onOpenAlbum: (String, String) -> Unit,
    controller: MediaController?
) {
    var query by remember { mutableStateOf("") }
    var albums by remember { mutableStateOf<List<AlbumDto>>(emptyList()) }
    var songs by remember { mutableStateOf<List<SongDto>>(emptyList()) }

    LaunchedEffect(query) {
        if (query.isBlank()) {
            albums = emptyList(); songs = emptyList(); return@LaunchedEffect
        }
        delay(280)
        runCatching {
            val result = ServiceLocator.repository.search(query)
            albums = result.album
            songs = result.song
        }
    }

    Column(Modifier.fillMaxSize().padding(top = 96.dp)) {
        Box(
            Modifier
                .padding(horizontal = 48.dp)
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                if (query.isBlank()) "Search albums, songs, artists…" else query,
                color = if (query.isBlank()) Color.White.copy(alpha = 0.4f) else Color.White,
                fontSize = 20.sp
            )
        }
        Spacer(Modifier.height(20.dp))

        LazyRow(
            modifier = Modifier.padding(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(ALPHABET) { key ->
                KeyButton(key) {
                    query = when (key) {
                        "SPACE" -> "$query "
                        "DEL" -> query.dropLast(1)
                        "CLEAR" -> ""
                        else -> query + key
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (songs.isNotEmpty()) {
            SectionHeader("Songs")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 48.dp, vertical = 4.dp)
            ) {
                items(songs, key = { it.id }) { song ->
                    SongResultCard(song, controller)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (albums.isNotEmpty()) {
            SectionHeader("Albums")
            LazyVerticalGrid(
                columns = GridCells.Adaptive(180.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxSize()
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
        } else if (query.isNotBlank() && songs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No results", color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun SongResultCard(song: SongDto, controller: MediaController?) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val bg = if (focused) Color.White.copy(alpha = 0.15f) else Color.Transparent

    Column(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(interactionSource = interaction, indication = null) {
                playSongs(listOf(song), controller)
            }
            .padding(6.dp)
    ) {
        AsyncImage(
            model = ServiceLocator.repository.coverUrl(song.coverArt, 300),
            contentDescription = song.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(168.dp).clip(RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.height(8.dp))
        Text(
            song.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            song.artist ?: "",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun KeyButton(label: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val bg = if (focused) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f)

    Box(
        Modifier
            .size(width = if (label.length > 1) 90.dp else 46.dp, height = 46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = Color.White,
            fontSize = if (label.length > 1) 13.sp else 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
