package com.development.forge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.development.forge.core.datastore.ForgePreferences
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme
import com.development.forge.navigation.ForgeNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var forgePreferences: ForgePreferences

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
            val isDark by remember { forgePreferences.isDarkTheme() }
                .collectAsStateWithLifecycle(initialValue = true)
            val coroutineScope = rememberCoroutineScope()
            ForgeTheme(darkTheme = isDark) {
                ForgeNavHost(
                    modifier = Modifier,
                    isDark = isDark,
                    onToggleTheme = {
                        coroutineScope.launch { forgePreferences.setDarkTheme(!isDark) }
                    }
                )
            }
        }
    }
}