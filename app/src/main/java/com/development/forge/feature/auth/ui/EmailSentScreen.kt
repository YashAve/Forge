package com.development.forge.feature.auth.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.ForgeButton
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme
import com.development.forge.feature.auth.contract.AuthState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * DESIGN SPEC — Email Sent screen (Screens.jsx → EmailSentScreen).
 *
 * ## Top bar
 * - Back button + wordmark (ForgeTopBar with onBack).
 *
 * ## Layout (shell: 24.dp sides, 24.dp top, 28.dp bottom)
 * The middle block is TRULY CENTERED — Column with weight(1f), centered both ways.
 *
 * 1. Badge: circle 96.dp, amber 10% fill, 1.5.dp amber 30% border, mark_email_read
 *    icon 44.dp amber, plus an 8.dp amber ember dot near the top-right with glow.
 * 2. Spacer 32.dp
 * 3. Headline "Check your email." — 32.sp, W600, (-0.025).em, #F5EDDE
 * 4. Spacer 14.dp
 * 5. "We sent a reset token to" — 15.sp, lineHeight 1.5.em, #B5A990, max 280.dp
 * 6. Spacer 4.dp
 * 7. The email — MONO, 14.sp, W500, #F5EDDE
 * 8. Spacer 14.dp
 * 9. "The token expires in 60 minutes." — 13.sp, #7A7060
 *
 * Bottom CTA stack (gap 14.dp):
 * 10. Primary button → Reset Password.
 * 11. Resend row — 48.dp, centered. Two states:
 *     - Cooling down: DISABLED, #7A7060, "Resend in m:ss".
 *     - Ready: ENABLED, amber, "Resend link".
 *
 * ## Countdown
 * - 45 second cooldown, LaunchedEffect keyed on the cooldown flag so a resend
 *   restarts the timer.
 *
 * ## Scope note
 * The design's "Open mail app" button assumed a real reset email. Email delivery
 * is out of scope for this build — the server logs the reset token to its console
 * instead of sending a link — so the primary action takes the user to the reset
 * screen to enter that token by hand.
 *
 * @param state shared auth state; the email is read from it.
 * @param onResend tap "Resend link".
 * @param onEnterToken tap the primary button.
 */
@Composable
fun EmailSentScreen(
    modifier: Modifier = Modifier,
    state: AuthState = AuthState(),
    onResend: () -> Unit = { },
    onEnterToken: () -> Unit = { }
) {
    val amber = ForgeTheme.colors.amber   // captured for the DrawScope Canvas below

    var timer by rememberSaveable { mutableIntStateOf(COOLDOWN_SECONDS) }
    var isCoolingDown by rememberSaveable { mutableStateOf(false) }

    val resendText by remember {
        derivedStateOf {
            if (!isCoolingDown) {
                "Resend link"
            } else {
                val seconds = if (timer > 9) "$timer" else "0$timer"
                "Resend in 0:$seconds"
            }
        }
    }

    LaunchedEffect(isCoolingDown) {
        if (isCoolingDown) {
            while (timer > 0) {
                delay(1000.milliseconds)
                timer--
            }
            isCoolingDown = false
            timer = COOLDOWN_SECONDS
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        radius = size.minDimension / 2,
                        color = amber.copy(alpha = 0.10f)
                    )
                    drawCircle(
                        radius = size.minDimension / 2,
                        color = amber.copy(alpha = 0.30f),
                        style = Stroke(1.5f)
                    )
                    drawCircle(
                        radius = 12f,
                        center = Offset(size.width - 22f, 14f),
                        brush = Brush.radialGradient(
                            center = Offset(size.width - 22f, 14f),
                            radius = 12f,
                            colors = listOf(amber.copy(alpha = 0.70f), Color.Transparent)
                        )
                    )
                }
                Icon(
                    Icons.Outlined.MarkEmailRead,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = amber
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Check your email.",
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.W600,
                    letterSpacing = (-0.025).em,
                    lineHeight = 1.1.em,
                    color = ForgeTheme.colors.fgPrimary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "We sent a reset token to",
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W400,
                    lineHeight = 1.5.em,
                    color = ForgeTheme.colors.fgSecondary
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.email,
                style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    color = ForgeTheme.colors.fgPrimary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "The token expires in 60 minutes.",
                style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W500,
                    color = ForgeTheme.colors.fgTertiary
                )
            )
        }

        // Email delivery is out of scope: the server logs the reset token to its
        // console instead of sending a link, so there is no mail to open. The
        // original design's "Open mail app" action is replaced by a direct route
        // to the reset screen, where the token is entered by hand.
        //
        // ForgeButton(
        //     label = "Open mail app",
        //     isEnabled = true,
        //     isLoading = false,
        //     icon = Icons.AutoMirrored.Default.ArrowForward,
        //     onClick = onOpenEmail
        // )
        ForgeButton(
            label = "Enter token",
            isEnabled = true,
            isLoading = false,
            icon = Icons.AutoMirrored.Default.ArrowForward,
            onClick = onEnterToken
        )

        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.height(48.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = resendText,
                style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 14.sp,
                    fontWeight = if (!isCoolingDown) FontWeight.W600 else FontWeight.W500,
                    color = if (!isCoolingDown) amber else ForgeTheme.colors.fgTertiary
                ),
                modifier = Modifier.clickable(
                    enabled = !isCoolingDown,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onResend()
                    isCoolingDown = true
                }
            )
        }
    }
}

private const val COOLDOWN_SECONDS = 45

@Preview
@Composable
private fun EmailSentScreenPreview() {
    ForgeTheme {
        EmailSentScreen(state = AuthState(email = "optimusprime@forge.dev"))
    }
}