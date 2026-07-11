package com.development.forge

import android.app.Application
import com.development.forge.core.designsystem.ForgeFonts
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class ForgeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        MainScope().launch {
            ForgeFonts.spaceGrotesk
            ForgeFonts.jetbrainsMono
        }
    }
}
