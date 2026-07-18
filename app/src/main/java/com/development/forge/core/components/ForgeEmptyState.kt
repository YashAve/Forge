package com.development.forge.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts

/**
 * DESIGN SPECIFICATION — ForgeEmptyState
 * Source: Forge Design System, AppScreens.jsx → EmptyState
 *
 * A centred, friendly placeholder shown when a screen has no content to display.
 * On the Home screen it appears when there are no open tasks ("Nothing left to
 * forge."). Reused on the Task List screen and anywhere else a list can be empty.
 *
 * ═══════════════════════════════════════════════════════════════
 * THE CONTAINER
 * ═══════════════════════════════════════════════════════════════
 * A Column, horizontally centred, with its text centred too. Padded 64dp
 * vertically and 24dp horizontally. Everything stacks vertically down the middle.
 *
 * ═══════════════════════════════════════════════════════════════
 * 1. THE BADGE — reuse your Success screen pattern
 * ═══════════════════════════════════════════════════════════════
 * A circle, 88dp across:
 *   - background: amber at 10% opacity — Amber.copy(alpha = 0.10f)
 *   - border: 1.5dp, amber at 30% opacity — Amber.copy(alpha = 0.30f)
 * Inside it, centred: the icon passed in (default local_fire_department /
 * Icons.Filled.LocalFireDepartment), 40dp, tinted Amber, FILLED style.
 *
 * This is exactly the badge from your Success screen. If you built that as a
 * standalone piece you can reuse it; otherwise it's a Box with a CircleShape
 * clip, the amber fill, the amber border, and the icon centred inside.
 *
 * ═══════════════════════════════════════════════════════════════
 * 2. Spacer 24dp
 * ═══════════════════════════════════════════════════════════════
 *
 * 3. THE TITLE
 *   Space Grotesk, 24sp, weight 600, letter spacing -0.02em, colour #F5EDDE.
 *   Centred. e.g. "Nothing left to forge."
 *
 * ═══════════════════════════════════════════════════════════════
 * 4. Spacer 12dp
 * ═══════════════════════════════════════════════════════════════
 *
 * 5. THE BODY
 *   Space Grotesk, 15sp, line height 1.5em, colour #B5A990, centred.
 *   Constrained to a maximum width of 280dp so it wraps into a tidy block
 *   rather than stretching edge to edge — use Modifier.widthIn(max = 280.dp).
 *   e.g. "You've cleared today. Rest, or line up what's next."
 *
 * ═══════════════════════════════════════════════════════════════
 * 6. Spacer (before the CTA — the design uses roughly 24dp)
 * ═══════════════════════════════════════════════════════════════
 *
 * 7. THE CTA BUTTON
 *   Your existing ForgeButton (primary), label passed in (e.g. "Add a task"),
 *   calls onCta when tapped. No icon needed unless you want one.
 *   Note: the button shouldn't stretch full-width here since the whole block is
 *   centred — let it size to its content, or wrap it so it stays centred rather
 *   than filling the 280dp width.
 *
 * ═══════════════════════════════════════════════════════════════
 * PARAMETERS
 * ═══════════════════════════════════════════════════════════════
 * @param icon      the badge icon (default Icons.Filled.LocalFireDepartment).
 * @param title     the headline text.
 * @param body      the supporting text (wraps at max 280dp).
 * @param cta       the button label.
 * @param onCta     called when the CTA button is tapped.
 * @param modifier  applied to the root Column.
 */
@Preview
@Composable
fun ForgeEmptyState(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.LocalFireDepartment,
    title: String = "Nothing left to forge.",
    body: String = "You have cleared today. Rest, or line up\nwhat's next.",
    cta: String = "Add a task",
    onCta: () -> Unit = { }
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(percent = 100))
                .background(Amber.copy(alpha = 0.10f))
                .border(
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = Amber.copy(alpha = 0.30f)
                    ),
                    shape = RoundedCornerShape(percent = 100)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(40.dp), Amber)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title, style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 24.sp,
                fontWeight = FontWeight.W600,
                letterSpacing = (-0.02).em,
                color = Color(0xFFF5EDDE)
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = body, style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 15.sp,
                lineHeight = 1.5.em,
                color = Color(0xFFB5A990),
                textAlign = TextAlign.Center
            ), modifier = Modifier.widthIn(max = 280.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        ForgeButton(
            modifier = Modifier.widthIn(max = 280.dp),
            label = cta,
            isEnabled = true,
            isLoading = false,
            onClick = onCta
        )
    }
}
