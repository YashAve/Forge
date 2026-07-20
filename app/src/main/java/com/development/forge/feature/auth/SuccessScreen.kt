package com.development.forge.feature.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.ForgeButton
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme

/**
 * DESIGN SPEC — Success screen (Screens.jsx → SuccessScreen).
 *
 * ## Top bar
 * - NONE. No back button, no wordmark. Add this key to the "no top bar" branch
 *   in the nav host's `when`.
 *
 * ## Layout, top to bottom (shell: 24.dp sides, 24.dp top, 28.dp bottom)
 * 1. Badge, centered horizontally:
 *    - Circle **80.dp**, background **amber at 10% alpha**,
 *      border **1.5.dp amber at 30% alpha**.
 *    - Check icon inside: **40.dp**, amber.
 * 2. Spacer **28.dp**
 * 3. Headline "You're in." — centered.
 *    → Space Grotesk, **32.sp**, weight **600**, letterSpacing **(-0.025).em**,
 *      color **#F5EDDE**
 * 4. Spacer **14.dp**
 * 5. Two centered lines:
 *    - "Signed in as" → **16.sp**, color **#B5A990**
 *    - the email → **MONO font (JetBrains Mono)**, **14.sp**, color **#F5EDDE**
 * 6. Flexible spacer: weight(1f), minimum height **16.dp**
 * 7. CTA stack, gap **12.dp**:
 *    - Primary button "Continue" with arrow_forward.
 *    - Secondary button "Reset prototype" — NOTE: this is a PROTOTYPE-ONLY
 *      control (it restarts the design demo). In the real app, skip it or
 *      replace it with "Sign out". Your call.
 *
 * @param email shown under "Signed in as".
 * @param onContinue tap "Continue".
 */
@Preview
@Composable
fun SuccessScreen(
    modifier: Modifier = Modifier,
    email: String = "you@forge.app",
    onContinue: () -> Unit = { }
) {
    val amber = ForgeTheme.colors.amber   // captured for the DrawScope Canvas below
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp)
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    radius = size.minDimension / 2,
                    color = amber.copy(alpha = 0.10f)
                    // no center param → defaults to size.width/2, size.height/2 ✓
                )
                drawCircle(
                    radius = size.minDimension / 2,
                    color = amber.copy(alpha = 0.30f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            Icon(
                Icons.Default.Check,
                null,
                modifier = Modifier
                    .size(40.dp),
                tint = amber
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "You're in.", style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 32.sp,
                fontWeight = FontWeight.W600,
                letterSpacing = (-0.025).em,
                color = ForgeTheme.colors.fgPrimary
            )
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Signed in as", style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 16.sp,
                fontWeight = FontWeight.W600,
                letterSpacing = (-0.025).em,
                color = ForgeTheme.colors.fgSecondary
            )
        )
        Text(
            text = email, style = TextStyle(
                fontFamily = ForgeFonts.jetbrainsMono,
                fontSize = 14.sp,
                fontWeight = FontWeight.W600,
                color = ForgeTheme.colors.fgPrimary
            )
        )
        Spacer(
            modifier = Modifier
                .heightIn(min = 16.dp)
                .weight(1f)
        )
        ForgeButton(
            label = "Continue",
            isEnabled = true,
            icon = Icons.AutoMirrored.Default.ArrowForward
        ) {
            onContinue()
        }
    }
}