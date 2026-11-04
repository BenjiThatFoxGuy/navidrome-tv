package com.navidrome.tv.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.navidrome.tv.di.ServiceLocator
import kotlinx.coroutines.delay

/**
 * Hooks a Player.Listener + polling loop that submits a Subsonic scrobble
 * once the current track has crossed 50% of its duration. Each track is
 * scrobbled at most once per playback session.
 */
@Composable
fun ObserveScrobbling(controller: MediaController?) {
    if (controller == null) return
    val repo = ServiceLocator.repository

    LaunchedEffect(controller) {
        var currentId: String? = controller.currentMediaItem?.mediaId
        var scrobbledId: String? = null

        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentId = mediaItem?.mediaId
                if (mediaItem?.mediaId != scrobbledId) scrobbledId = null
            }
        }
        controller.addListener(listener)

        while (true) {
            val id = currentId
            if (id != null && id != scrobbledId && controller.isPlaying) {
                val duration = controller.duration
                val position = controller.currentPosition
                if (duration > 0 && position > duration / 2) {
                    runCatching { repo.scrobble(id) }
                    scrobbledId = id
                }
            }
            delay(3_000)
        }
    }
}
