package com.development.forge.ui.theme

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
 *     TextStyle(fontFamily = ForgeFonts.quicksand, fontWeight = FontWeight.Bold)
 */
object ForgeFonts {

    /** Google Play Services font provider; certs live in res/values/font_certs.xml. */
    private val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )

    private val quicksandName = GoogleFont("Quicksand")

    /** Soft, rounded geometric sans — the Forge branding typeface. */
    val quicksand: FontFamily = FontFamily(
        Font(googleFont = quicksandName, fontProvider = provider, weight = FontWeight.Medium),
        Font(googleFont = quicksandName, fontProvider = provider, weight = FontWeight.Bold)
    )
}
