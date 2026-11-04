package com.navidrome.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.session.MediaController
import com.navidrome.tv.data.Credentials
import com.navidrome.tv.ui.components.TopNavBar
import com.navidrome.tv.ui.screens.AlbumScreen
import com.navidrome.tv.ui.screens.HomeScreen
import com.navidrome.tv.ui.screens.LibraryScreen
import com.navidrome.tv.ui.screens.NowPlayingScreen
import com.navidrome.tv.ui.screens.OnboardingScreen
import com.navidrome.tv.ui.screens.SearchScreen

sealed class Screen {
    data object Home : Screen()
    data object Search : Screen()
    data object Library : Screen()
    data object NowPlaying : Screen()
    data class Album(val id: String, val name: String) : Screen()
}

enum class TopTab { Home, Search, Library, NowPlaying }

@Composable
fun AppRoot(
    credentialsState: Credentials?,
    controller: MediaController?,
    onSaveCredentials: (Credentials) -> Unit
) {
    if (credentialsState == null) {
        OnboardingScreen(onSave = onSaveCredentials)
        return
    }

    var backStack by rememberSaveable(
        stateSaver = androidx.compose.runtime.saveable.listSaver(
            save = { list -> list.map { screenToKey(it) } },
            restore = { keys -> keys.map { keyToScreen(it) } }
        )
    ) { mutableStateOf(listOf<Screen>(Screen.Home)) }

    var topTab by rememberSaveable { mutableStateOf(TopTab.Home) }
    val current = backStack.last()

    fun navigate(screen: Screen) { backStack = backStack + screen }
    fun navigateTab(tab: TopTab) {
        topTab = tab
        backStack = listOf(
            when (tab) {
                TopTab.Home -> Screen.Home
                TopTab.Search -> Screen.Search
                TopTab.Library -> Screen.Library
                TopTab.NowPlaying -> Screen.NowPlaying
            }
        )
    }

    BackHandler(enabled = backStack.size > 1) {
        backStack = backStack.dropLast(1)
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AnimatedContent(
            targetState = current,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
            label = "screen"
        ) { screen ->
            when (screen) {
                Screen.Home -> HomeScreen(
                    onOpenAlbum = { id, name -> navigate(Screen.Album(id, name)) },
                    controller = controller
                )
                Screen.Search -> SearchScreen(
                    onOpenAlbum = { id, name -> navigate(Screen.Album(id, name)) },
                    controller = controller
                )
                Screen.Library -> LibraryScreen(
                    onOpenAlbum = { id, name -> navigate(Screen.Album(id, name)) },
                    controller = controller
                )
                Screen.NowPlaying -> NowPlayingScreen(controller)
                is Screen.Album -> AlbumScreen(
                    albumId = screen.id,
                    controller = controller,
                    onBack = { backStack = backStack.dropLast(1) }
                )
            }
        }

        TopNavBar(
            current = topTab,
            onSelect = { navigateTab(it) },
            modifier = Modifier
        )
    }
}

private fun screenToKey(s: Screen): String = when (s) {
    Screen.Home -> "home"
    Screen.Search -> "search"
    Screen.Library -> "library"
    Screen.NowPlaying -> "now"
    is Screen.Album -> "album:${s.id}:${s.name}"
}

private fun keyToScreen(k: String): Screen = when {
    k == "home" -> Screen.Home
    k == "search" -> Screen.Search
    k == "library" -> Screen.Library
    k == "now" -> Screen.NowPlaying
    k.startsWith("album:") -> {
        val parts = k.removePrefix("album:").split(":", limit = 2)
        Screen.Album(parts.getOrElse(0) { "" }, parts.getOrElse(1) { "" })
    }
    else -> Screen.Home
}
