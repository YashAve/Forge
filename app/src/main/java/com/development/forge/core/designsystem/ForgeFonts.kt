package com.development.forge.core.designsystem

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.development.forge.R

/**
 * Downloadable Google Fonts for Forge.
 *
 * Fonts are fetched at runtime from the Google Play Services font provider, so
 * no font files are bundled in the APK. Use the family directly in a text style:
 *
 *     TextStyle(fontFamily = ForgeFonts.spaceGrotesk, fontWeight = FontWeight.Bold)
 */
object ForgeFonts {

    /** Google Play Services font provider; certs live in res/values/font_certs.xml. */
    private val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )

    private val spaceGroteskName = GoogleFont("Space Grotesk")
    private val jetbrainsMonoName = GoogleFont("JetBrains Mono")

    /** Soft, rounded geometric sans — the Forge branding typeface. */
    val spaceGrotesk: FontFamily = FontFamily(
        Font(googleFont = spaceGroteskName, fontProvider = provider, weight = FontWeight.Medium),
        Font(googleFont = spaceGroteskName, fontProvider = provider, weight = FontWeight.W700)
    )
    val jetbrainsMono: FontFamily = FontFamily(
        Font(googleFont = jetbrainsMonoName, fontProvider = provider, weight = FontWeight.Medium),
        Font(googleFont = jetbrainsMonoName, fontProvider = provider, weight = FontWeight.W700)
    )
}
