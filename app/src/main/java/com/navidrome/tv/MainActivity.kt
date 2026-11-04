package com.navidrome.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.navidrome.tv.di.ServiceLocator
import com.navidrome.tv.player.rememberMediaController
import com.navidrome.tv.ui.AppRoot
import com.navidrome.tv.ui.theme.NavidromeTvTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            NavidromeTvTheme {
                val creds by ServiceLocator.credentials.collectAsState()
                val controller = rememberMediaController(this)
                val scope = rememberCoroutineScope()

                AppRoot(
                    credentialsState = creds,
                    controller = controller,
                    onSaveCredentials = { c ->
                        scope.launch { ServiceLocator.credentialsStore.save(c) }
                    }
                )
            }
        }
    }
}
