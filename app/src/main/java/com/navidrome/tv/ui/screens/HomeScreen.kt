package com.navidrome.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.navidrome.tv.player.toMediaItem
import com.navidrome.tv.ui.components.MediaCard
import com.navidrome.tv.ui.components.PillButton
import com.navidrome.tv.ui.components.SectionHeader
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onOpenAlbum: (String, String) -> Unit,
    controller: MediaController?
) {
    val repo = ServiceLocator.repository
    val scope = rememberCoroutineScope()
    var recent by remember { mutableStateOf<List<AlbumDto>>(emptyList()) }
    var newest by remember { mutableStateOf<List<AlbumDto>>(emptyList()) }
    var random by remember { mutableStateOf<List<AlbumDto>>(emptyList()) }
    var frequent by remember { mutableStateOf<List<AlbumDto>>(emptyList()) }
    var starred by remember { mutableStateOf<List<AlbumDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        runCatching { recent = repo.recentAlbums(20) }
        runCatching { newest = repo.newestAlbums(20) }
        runCatching { random = repo.randomAlbums(20) }
        runCatching { frequent = repo.frequentAlbums(20) }
        runCatching { starred = repo.starredAlbums() }
    }

    val hero = recent.firstOrNull() ?: newest.firstOrNull()
    val heroUrl = ServiceLocator.repository.coverUrl(hero?.coverArt, 1200)

    Box(Modifier.fillMaxSize()) {
        if (heroUrl != null) {
            AsyncImage(
                model = heroUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black
                        )
                    )
                )
            )
        }

        Column(Modifier.fillMaxSize().padding(top = 96.dp)) {
            if (hero != null) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 48.dp, vertical = 24.dp)
                ) {
                    Text(
                        "TOP PICK FOR YOU",
                        color = Color(0xFFE50914),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        hero.name,
                        color = Color.White,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        listOfNotNull(hero.artist, hero.year?.toString(), "${hero.songCount ?: 0} songs").joinToString(" · "),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PillButton("Play") {
                            scope.launch {
                                runCatching {
                                    val album = repo.album(hero.id)
                                    playSongs(album.song, controller)
                                }
                            }
                        }
                        PillButton("Shuffle", primary = false) {
                            scope.launch {
                                runCatching {
                                    val album = repo.album(hero.id)
                                    playSongs(album.song.shuffled(), controller)
                                }
                            }
                        }
                        PillButton("Open", primary = false) { onOpenAlbum(hero.id, hero.name) }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            Carousel("Recently Played", frequent, onOpenAlbum)
            Carousel("Recently Added", newest, onOpenAlbum)
            Carousel("Made for You", random, onOpenAlbum)
            if (starred.isNotEmpty()) Carousel("Favorites", starred, onOpenAlbum)
        }
    }
}

internal fun playSongs(songs: List<SongDto>, controller: MediaController?) {
    if (controller == null || songs.isEmpty()) return
    val repo = ServiceLocator.repository
    val items = songs.map { it.toMediaItem(repo.streamUrl(it.id), repo.coverUrl(it.coverArt)) }
    controller.setMediaItems(items, 0, 0L)
    controller.prepare()
    controller.play()
}

@Composable
private fun Carousel(title: String, albums: List<AlbumDto>, onClick: (String, String) -> Unit) {
    if (albums.isEmpty()) return
    Column(Modifier.fillMaxWidth()) {
        SectionHeader(title)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp)
        ) {
            items(albums, key = { it.id }) { album ->
                MediaCard(
                    title = album.name,
                    subtitle = album.artist,
                    imageUrl = ServiceLocator.repository.coverUrl(album.coverArt),
                    onClick = { onClick(album.id, album.name) }
                )
            }
        }
    }
}
