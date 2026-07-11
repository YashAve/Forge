package com.development.forge.feature.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.Amber
import com.development.forge.core.designsystem.ForgeFonts

/**
 * DESIGN SPEC — Email Sent screen (Screens.jsx → EmailSentScreen).
 *
 * ## Top bar
 * - Back button + wordmark (ForgeTopBar with onBack).
 *
 * ## Layout (shell: 24.dp sides, 24.dp top, 28.dp bottom)
 * The middle block is TRULY CENTERED — wrap it in a Column with weight(1f),
 * centered vertically and horizontally, all text centered:
 *
 * 1. Badge:
 *    - Circle **96.dp** (bigger than Success's 80), background **amber 10%**,
 *      border **1.5.dp amber 30%**.
 *    - Icon inside: mark_email_read (a "read mail" envelope), **44.dp**, amber, FILLED style.
 *    - PLUS a tiny ember: **8.dp** amber dot near the badge's top-right
 *      (offset ~14.dp down, ~22.dp in from the right edge of the 96.dp circle),
 *      with glow: radius 12, amber at 70%. Your ForgeWordmark dot pattern — reuse it.
 * 2. Spacer **32.dp**
 * 3. Headline "Check your email." → **32.sp**, W600, (-0.025).em, lineHeight 1.1.em, #F5EDDE
 * 4. Spacer **14.dp**
 * 5. "We sent a reset link to" → **15.sp**, lineHeight 1.5.em, #B5A990, max width **280.dp**
 * 6. Spacer **4.dp**
 * 7. The email → **MONO**, **14.sp**, weight 500, #F5EDDE
 * 8. Spacer **14.dp**
 * 9. "The link expires in 30 minutes." → **13.sp**, #7A7060
 *
 * Then, at the bottom (CTA stack, gap **14.dp**):
 * 10. Primary button "Open mail app" with arrow_forward. Always enabled.
 * 11. Resend row — height **48.dp**, centered. TWO states:
 *     - Cooling down (timer > 0): DISABLED. Color #7A7060. Shows two parts
 *       with an 8.dp gap: "Resend in" (14.sp W600) + the time "m:ss"
 *       (MONO, weight 500). Example: "Resend in 0:45".
 *     - Ready (timer == 0): ENABLED. Amber. Text "Resend link" (14.sp W600).
 *
 * ## The countdown logic — this is a LaunchedEffect exercise
 * - Start cooldown at **45** seconds.
 * - Every second, subtract 1, until 0.
 * - Tap "Resend link" → call onResend and reset cooldown to 45.
 * - In Compose: a `LaunchedEffect` with a while-loop and `delay(1000)`.
 *   Think about what the KEY should be so the timer restarts on resend —
 *   that's the whole lesson from the side-effects session, applied for real.
 *
 * @param email shown in the middle block.
 * @param onResend tap "Resend link".
 * @param onOpenMail tap "Open mail app".
 */
@Preview
@Composable
fun EmailSentScreen(
    modifier: Modifier = Modifier,
    email: String = "you@forge.app",
    onResend: () -> Unit = { },
    onOpenEmail: () -> Unit = { }
) {
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
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        radius = size.minDimension / 2,
                        color = Amber.copy(alpha = 0.10f)
                    )
                    drawCircle(
                        radius = size.minDimension / 2,
                        color = Amber.copy(alpha = 0.30f),
                        style = Stroke(1.5f)
                    )
                    drawCircle(
                        radius = 12f,
                        center = Offset(size.width - 22f, 14f),
                        brush = Brush.radialGradient(
                            center = Offset(size.width - 22f, 14f),
                            radius = 12f,
                            colors = listOf(Amber.copy(alpha = 0.70f), Color.Transparent)
                        )
                    )
                }
                Icon(
                    Icons.Outlined.MarkEmailRead,
                    null,
                    modifier = Modifier.size(44.dp),
                    tint = Amber
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Check your email.", style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.W600,
                    letterSpacing = (-0.025).em,
                    color = Color(0xFFF5EDDE)
                )
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "We sent a reset link to", style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W600,
                    lineHeight = 1.5.em,
                    color = Color(0xFFB5A990)
                ), modifier = Modifier.widthIn(max = 200.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = email, style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    color = Color(0xFFF5EDDE)
                )
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "The link expires in 30 minutes.", style = TextStyle(
                    fontFamily = ForgeFonts.jetbrainsMono,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W500,
                    color = Color(0xFF7A7060)
                )
            )
        }
        /** Then, at the bottom (CTA stack, gap **14.dp**):
         * 10. Primary button "Open mail app" with arrow_forward. Always enabled.
         * 11. Resend row — height **48.dp**, centered. TWO states:
         *     - Cooling down (timer > 0): DISABLED. Color #7A7060. Shows two parts
         *       with an 8.dp gap: "Resend in" (14.sp W600) + the time "m:ss"
         *       (MONO, weight 500). Example: "Resend in 0:45".
         *     - Ready (timer == 0): ENABLED. Amber. Text "Resend link" (14.sp W600).*/
    }
}