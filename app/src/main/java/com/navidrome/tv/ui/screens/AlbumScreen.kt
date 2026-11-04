package com.navidrome.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.navidrome.tv.data.AlbumWithSongsDto
import com.navidrome.tv.data.SongDto
import com.navidrome.tv.di.ServiceLocator
import com.navidrome.tv.ui.components.PillButton
import kotlinx.coroutines.launch

@Composable
fun AlbumScreen(
    albumId: String,
    controller: MediaController?,
    onBack: () -> Unit
) {
    var album by remember { mutableStateOf<AlbumWithSongsDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(albumId) {
        runCatching { ServiceLocator.repository.album(albumId) }
            .onSuccess { album = it }
            .onFailure { error = it.message }
    }

    val url = album?.let { ServiceLocator.repository.coverUrl(it.coverArt, 1200) }

    Box(Modifier.fillMaxSize()) {
        if (url != null) {
            AsyncImage(
                model = url, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(
                        listOf(Color.Black.copy(alpha = 0.92f), Color.Black.copy(alpha = 0.7f))
                    )
                )
            )
        } else {
            Box(Modifier.fillMaxSize().background(Color.Black))
        }

        if (error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error!!, color = Color.White.copy(alpha = 0.7f))
            }
            return@Box
        }

        val current = album ?: return@Box

        Row(Modifier.fillMaxSize().padding(top = 96.dp, start = 48.dp, end = 48.dp, bottom = 24.dp)) {
            Column(Modifier.width(420.dp)) {
                AsyncImage(
                    model = url,
                    contentDescription = current.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(420.dp).clip(RoundedCornerShape(16.dp))
                )
            }
            Spacer(Modifier.width(40.dp))
            Column(Modifier.fillMaxHeight()) {
                Text(
                    current.name,
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    current.artist ?: "",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(4.dp))
                val meta = listOfNotNull(
                    current.year?.toString(),
                    current.songCount?.let { "$it songs" },
                    current.genre,
                    current.song.firstOrNull()?.suffix?.uppercase()?.let { "$it Lossless" }
                ).joinToString(" · ")
                Text(meta, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PillButton("Play") { playSongs(current.song, controller) }
                    PillButton("Shuffle", primary = false) {
                        playSongs(current.song.shuffled(), controller)
                    }
                }
                Spacer(Modifier.height(24.dp))
                LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                    items(current.song, key = { it.id }) { song ->
                        TrackRow(song, controller)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackRow(song: SongDto, controller: MediaController?) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val isPlaying = controller?.currentMediaItem?.mediaId == song.id

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (focused) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(interactionSource = interaction, indication = null) {
                playSongs(listOf(song), controller)
            }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            song.track?.toString() ?: "",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            modifier = Modifier.width(32.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                color = if (isPlaying) Color(0xFFE50914) else Color.White,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (song.artist != null) {
                Text(song.artist, color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp)
            }
        }
        Text(
            formatDuration(song.duration),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp
        )
    }
}

fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "$m:${s.toString().padStart(2, '0')}"
}
