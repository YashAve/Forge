package com.development.forge.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.ForgeCard
import com.development.forge.core.components.ForgeSectionLabel
import com.development.forge.core.components.ForgeToggle
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme
import com.development.forge.feature.settings.components.SettingRow

/**
 * DESIGN SPECIFICATION — Settings / "You" screen
 * Source: Forge Design System, AppScreens.jsx → SettingsView
 *
 * The profile + settings screen — a bottom-nav destination (the "You" tab). This
 * is the simplest remaining screen: pure layout, one new small component
 * (ForgeToggle), and it hosts the dark/light theme switch. All colours reference
 * ForgeTheme.colors so the screen itself flips correctly between themes.
 *
 * ═══════════════════════════════════════════════════════════════
 * 3. PREFERENCES GROUP (24dp top margin)
 * ═══════════════════════════════════════════════════════════════
 * A ForgeSectionLabel "Preferences", 6dp gap, then four SettingRows:
 *   a) icon notifications, title "Notifications", trailing = ForgeToggle
 *      (state hoisted — a remembered Boolean, default on)
 *   b) icon volume_up, title "Completion sound", trailing = ForgeToggle
 *      (remembered Boolean, default off)
 *   c) icon (light_mode when light / dark_mode when dark), title "Theme",
 *      value = "Light" or "Dark", onClick = onToggleTheme
 *      ← THIS is the theme switch. Tapping it flips the app theme.
 *   d) icon bolt, title "Focus preferences", value = "1 task", onClick = {}
 *
 * ═══════════════════════════════════════════════════════════════
 * 4. ABOUT GROUP (24dp top margin)
 * ═══════════════════════════════════════════════════════════════
 * A ForgeSectionLabel "About", 6dp gap, then two SettingRows:
 *   a) icon info, title "About Forge", onClick = {}
 *   b) icon shield, title "Privacy", onClick = {}
 *
 * ═══════════════════════════════════════════════════════════════
 * 5. SIGN OUT (24dp top margin)
 * ═══════════════════════════════════════════════════════════════
 * A single SettingRow: icon logout, title "Sign out", danger = true,
 * onClick = onSignOut. The danger flag colours the icon AND title
 * ForgeTheme.colors.danger instead of the normal colours.
 *
 * ═══════════════════════════════════════════════════════════════
 * 6. VERSION FOOTER (28dp top margin)
 * ═══════════════════════════════════════════════════════════════
 * Centred text "FORGE · V1.0.0" → JetBrains Mono, 11sp, letterSpacing 0.14em,
 * UPPERCASE, colour ForgeTheme.colors.fgTertiary.
 *
 * ═══════════════════════════════════════════════════════════════
 * NEW COMPONENTS
 * ═══════════════════════════════════════════════════════════════
 *
 * ── ForgeSettingRow(icon, title, value: String? = null,
 *                    danger: Boolean = false, trailing: @Composable (() -> Unit)? = null,
 *                    onClick: (() -> Unit)? = null) ──
 * A Row, vertically centred, 14dp gap, padding 14dp vertical / 16dp horizontal,
 * corner 12dp, clickable when onClick != null (no ripple; instead the background
 * animates to ForgeTheme.colors.bgPress while pressed, 120ms ease-out — use
 * collectIsPressedAsState + animateColorAsState).
 *   - LEFT icon: 22dp, colour = danger ? colors.danger : colors.fgSecondary
 *   - TITLE (weight 1f): Space Grotesk, 15sp, W500,
 *     colour = danger ? colors.danger : colors.fgPrimary
 *   - optional VALUE (right, before trailing): Space Grotesk, 14sp, colors.fgTertiary
 *   - optional TRAILING slot (the toggle) — rendered if provided
 *   - if onClick != null AND no trailing: a chevron_right icon, 20dp, colors.fgTertiary
 *
 * ═══════════════════════════════════════════════════════════════
 * THE THEME SWITCH — how it actually flips the app
 * ═══════════════════════════════════════════════════════════════
 * The screen does NOT own the theme state. It receives isDark + onToggleTheme.
 * onToggleTheme writes the new value to DataStore (via your ForgePreferences).
 * MainActivity collects that DataStore flow and passes darkTheme to ForgeTheme,
 * which re-provides LocalForgeColors — so the WHOLE app recomposes with the new
 * palette. The Settings screen just triggers the write; the theme system does
 * the rest. This is why every colour here reads from ForgeTheme.colors, not
 * hardcoded hex — so this screen flips along with everything else.
 *
 * ═══════════════════════════════════════════════════════════════
 * PARAMETERS
 * ═══════════════════════════════════════════════════════════════
 * @param email          the user's email (first char → avatar initial).
 * @param isDark         current theme, for the Theme row's icon + value label.
 * @param onToggleTheme  flips the theme (writes to DataStore).
 * @param onSignOut      signs out (clears back stack → Login).
 * @param modifier       applied to the root Column.
 */
@Preview
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    email: String = "karishma.kapoor@cosmiq.co.in",
    isDark: Boolean = true,
    onToggleTheme: () -> Unit = { },
    onSignOut: () -> Unit = { },
    /** True only in debug builds — gates the developer-tools section below. */
    showDebugTools: Boolean = false,
    onSeedData: () -> Unit = { }
) {
    val name = email.substringBefore(".")
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = name.replaceFirstChar { it.uppercase() }, style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 26.sp,
                fontWeight = FontWeight.W600,
                letterSpacing = (-0.025).em,
                color = ForgeTheme.colors.fgPrimary
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
        ForgeCard(
            modifier = Modifier
                .fillMaxWidth(),
            accent = false
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .dropShadow(
                            shape = RoundedCornerShape(percent = 100),
                            shadow = Shadow(
                                radius = 26.dp,
                                color = ForgeTheme.colors.amber.copy(alpha = 0.35f)
                            )
                        )
                        .clip(RoundedCornerShape(percent = 100))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ForgeTheme.colors.amber200, ForgeTheme.colors.amber, ForgeTheme.colors.amberA500)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = email.first().uppercase(), style = TextStyle(
                            fontFamily = ForgeFonts.spaceGrotesk,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.W700,
                            color = ForgeTheme.colors.fgOnAmber
                        )
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier) {
                    Text(
                        text = name.replaceFirstChar { it.uppercase() }, style = TextStyle(
                            fontFamily = ForgeFonts.spaceGrotesk,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W600,
                            color = ForgeTheme.colors.fgPrimary
                        )
                    )
                    Text(
                        text = email, style = TextStyle(
                            fontFamily = ForgeFonts.jetbrainsMono,
                            fontSize = (12.5).sp,
                            color = ForgeTheme.colors.fgTertiary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        var notificationsOn by remember { mutableStateOf(true) }
        var completionSoundOn by remember { mutableStateOf(false) }

        Spacer(modifier = Modifier.height(24.dp))
        ForgeSectionLabel(label = "Preferences")
        Spacer(modifier = Modifier.height(6.dp))
        SettingRow(
            icon = Icons.Outlined.Notifications,
            title = "Notifications",
            trailing = {
                ForgeToggle(checked = notificationsOn) { notificationsOn = it }
            })
        SettingRow(
            icon = Icons.AutoMirrored.Outlined.VolumeUp,
            title = "Completion sound",
            trailing = {
                ForgeToggle(checked = completionSoundOn) { completionSoundOn = it }
            })
        SettingRow(
            icon = if (isDark) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
            title = "Theme",
            value = if (isDark) "Dark" else "Light",
            onClick = onToggleTheme
        )
        SettingRow(
            icon = Icons.Outlined.Bolt,
            title = "Focus preferences",
            value = "1 task",
            onClick = { }
        )

        Spacer(modifier = Modifier.height(24.dp))
        ForgeSectionLabel(label = "About")
        Spacer(modifier = Modifier.height(6.dp))
        SettingRow(
            icon = Icons.Outlined.Info,
            title = "About Forge",
            onClick = { }
        )
        SettingRow(
            icon = Icons.Outlined.Shield,
            title = "Privacy",
            onClick = { }
        )

        // Developer tools. The caller passes BuildConfig.DEBUG, and the seeder it
        // reaches is guarded again on its own side, so this cannot ship as a live
        // button that quietly rewrites someone's task list.
        if (showDebugTools) {
            Spacer(modifier = Modifier.height(24.dp))
            ForgeSectionLabel(label = "Developer")
            Spacer(modifier = Modifier.height(6.dp))
            SettingRow(
                icon = Icons.Outlined.Science,
                title = "Seed sample tasks",
                value = "debug",
                onClick = onSeedData
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        SettingRow(
            icon = Icons.AutoMirrored.Filled.Logout,
            title = "Sign out",
            danger = true,
            onClick = onSignOut
        )

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "FORGE · V1.0.0",
            style = TextStyle(
                fontFamily = ForgeFonts.jetbrainsMono,
                fontSize = 11.sp,
                letterSpacing = (0.14).em,
                color = ForgeTheme.colors.fgTertiary,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
