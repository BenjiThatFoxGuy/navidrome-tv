package com.navidrome.tv.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.navidrome.tv.data.Credentials
import com.navidrome.tv.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun OnboardingScreen(onSave: (Credentials) -> Unit) {
    var baseUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(Color(0xFF0B0B0F), Color(0xFF1A1A22), Color.Black)
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(720.dp)
        ) {
            Text("Navidrome TV", fontSize = 52.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(10.dp))
            Text("Connect to your music server", fontSize = 18.sp, color = Color.White.copy(alpha = 0.65f))
            Spacer(Modifier.height(48.dp))

            OnboardingField("Server URL", baseUrl, "http://192.168.1.10:4533") { baseUrl = it }
            Spacer(Modifier.height(18.dp))
            OnboardingField("Username", username, "user") { username = it }
            Spacer(Modifier.height(18.dp))
            OnboardingField("Password", password, "••••••••", isPassword = true) { password = it }

            if (error != null) {
                Spacer(Modifier.height(16.dp))
                Text(error!!, color = Color(0xFFFF6B6B), fontSize = 14.sp)
            }

            Spacer(Modifier.height(32.dp))

            val interaction = remember { MutableInteractionSource() }
            val focused by interaction.collectIsFocusedAsState()
            val scale by animateFloatAsState(if (focused) 1.06f else 1f, tween(150))

            Box(
                modifier = Modifier
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .clip(RoundedCornerShape(50))
                    .background(if (loading) Color.White.copy(alpha = 0.2f) else Color(0xFFE50914))
                    .clickable(interactionSource = interaction, indication = null, enabled = !loading) {
                        if (baseUrl.isBlank() || username.isBlank() || password.isBlank()) {
                            error = "All fields are required"
                            return@clickable
                        }
                        val creds = Credentials(baseUrl.trim(), username.trim(), password)
                        loading = true
                        error = null
                        scope.launch {
                            val ok = withContext(Dispatchers.IO) {
                                runCatching {
                                    val api = ServiceLocator.buildApi(creds.baseUrl)
                                    api.ping()
                                }.isSuccess
                            }
                            loading = false
                            if (ok) onSave(creds) else error = "Could not connect. Check URL & credentials."
                        }
                    }
                    .padding(horizontal = 48.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (loading) "Connecting…" else "Connect",
                    color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun OnboardingField(
    label: String,
    value: String,
    placeholder: String,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(placeholder, color = Color.White.copy(alpha = 0.35f), fontSize = 16.sp)
                    }
                    inner()
                }
            )
        }
    }
}
