package com.development.forge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.createFontFamilyResolver
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme
import com.development.forge.navigation.ForgeNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val backgroundScope = CoroutineScope(Dispatchers.Default)
        val fontResolver = createFontFamilyResolver(applicationContext, backgroundScope.coroutineContext)
        backgroundScope.launch {
            fontResolver.apply {
                resolve(ForgeFonts.spaceGrotesk)
                resolve(ForgeFonts.jetbrainsMono)
            }
        }

        setContent {
            ForgeTheme {
                ForgeNavHost(modifier = Modifier)
            }
        }
    }
}