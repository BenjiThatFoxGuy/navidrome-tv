package com.navidrome.tv.ui.screens

import android.graphics.drawable.BitmapDrawable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.palette.graphics.Palette
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.delay

@Composable
fun NowPlayingScreen(controller: MediaController?) {
    if (controller == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No player", color = Color.White.copy(alpha = 0.6f))
        }
        return
    }

    var isPlaying by remember { mutableStateOf(controller.isPlaying) }
    var position by remember { mutableLongStateOf(controller.currentPosition) }
    var duration by remember { mutableLongStateOf(controller.duration.coerceAtLeast(0)) }
    var currentItem by remember { mutableStateOf(controller.currentMediaItem) }

    LaunchedEffect(controller) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
                currentItem = item
            }
            override fun onPlaybackStateChanged(state: Int) {
                duration = controller.duration.coerceAtLeast(0)
            }
        }
        controller.addListener(listener)
        while (true) {
            position = controller.currentPosition
            duration = controller.duration.coerceAtLeast(0)
            delay(250)
        }
    }

    val meta = currentItem?.mediaMetadata
    val title = meta?.title?.toString() ?: "Nothing Playing"
    val artist = meta?.artist?.toString() ?: ""
    val album = meta?.albumTitle?.toString() ?: ""
    val artworkUri = meta?.artworkUri?.toString()

    // ---- Palette-based dynamic tint ----
    val context = LocalContext.current
    var dominantColor by remember { mutableStateOf(Color(0xFF0B0B0F)) }
    LaunchedEffect(artworkUri) {
        val url = artworkUri ?: return@LaunchedEffect
        runCatching {
            val req = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(req)
            val bmp = (result.drawable as? BitmapDrawable)?.bitmap
            if (bmp != null) {
                val palette = Palette.from(bmp).generate()
                val colorInt = palette.getVibrantColor(
                    palette.getDarkMutedColor(0xFF0B0B0F.toInt())
                )
                dominantColor = Color(colorInt)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Layer 1: blurred artwork
        if (artworkUri != null) {
            AsyncImage(
                model = artworkUri, contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(120.dp)
            )
        }
        // Layer 2: radial gradient from dominant color
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(dominantColor.copy(alpha = 0.55f), Color.Transparent),
                    radius = 1400f
                )
            )
        )
        // Layer 3: black vertical scrim
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.5f),
                        Color.Black.copy(alpha = 0.85f),
                        Color.Black
                    )
                )
            )
        )

        Row(
            Modifier.fillMaxSize().padding(top = 96.dp, start = 80.dp, end = 80.dp, bottom = 40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(460.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                if (artworkUri != null) {
                    AsyncImage(
                        model = artworkUri, contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.width(64.dp))

            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                Text(
                    album.uppercase(),
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 13.sp,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    title, color = Color.White, fontSize = 46.sp,
                    fontWeight = FontWeight.Bold, maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(artist, color = Color.White.copy(alpha = 0.75f), fontSize = 22.sp)
                Spacer(Modifier.height(36.dp))

                ProgressBar(position = position, duration = duration)
                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoundIconButton(Icons.Filled.Shuffle) {
                        controller.shuffleModeEnabled = !controller.shuffleModeEnabled
                    }
                    RoundIconButton(Icons.Filled.SkipPrevious) { controller.seekToPrevious() }
                    RoundIconButton(
                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        large = true
                    ) {
                        if (isPlaying) controller.pause() else controller.play()
                    }
                    RoundIconButton(Icons.Filled.SkipNext) { controller.seekToNext() }
                    RoundIconButton(Icons.Filled.Repeat) {
                        controller.repeatMode =
                            if (controller.repeatMode == Player.REPEAT_MODE_ALL) Player.REPEAT_MODE_OFF
                            else Player.REPEAT_MODE_ALL
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressBar(position: Long, duration: Long) {
    val progress = if (duration > 0) position.toFloat() / duration else 0f
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth().height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Box(
                Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(msToTime(position), color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            Text(msToTime(duration), color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
        }
    }
}

private fun msToTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "$m:${s.toString().padStart(2, '0')}"
}

@Composable
private fun RoundIconButton(
    icon: ImageVector,
    large: Boolean = false,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val size = if (large) 78.dp else 56.dp
    val bg = when {
        large -> Color.White
        focused -> Color.White.copy(alpha = 0.28f)
        else -> Color.White.copy(alpha = 0.12f)
    }
    val tint = if (large) Color.Black else Color.White

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(if (large) 34.dp else 24.dp))
    }
}
