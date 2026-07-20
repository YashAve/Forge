package com.development.forge.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme

/**
 * ForgeSectionLabel(text: String, action: String? = null, onAction: (() -> Unit)? = null)
 *
 * A Row with its children pushed to opposite ends (Arrangement.SpaceBetween),
 * aligned by their text BASELINE rather than centred vertically.
 *
 * LEFT — the label text:
 *   JetBrains Mono, 11sp, weight 500, letter spacing 0.14em, all uppercase,
 *   colour #7A7060 (tertiary).
 *
 * RIGHT — the action button (optional):
 *   Only rendered when action is not null.
 *   Space Grotesk, 13sp, weight 600, amber — your existing ForgeTextButton
 *   at a slightly smaller size than normal.
 *   Tapping it calls onAction.
 *
 * For "The only task": action = null, so nothing renders on the right.
 * For "Up next": action = "All tasks", onAction = { onSeeAll() }.
 */
@Preview
@Composable
fun ForgeSectionLabel(
    modifier: Modifier = Modifier,
    label: String = "The only task",
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            style = TextStyle(
                fontFamily = ForgeFonts.jetbrainsMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.W500,
                letterSpacing = (0.14).em,
                color = ForgeTheme.colors.fgTertiary
            )
        )
        if (action != null) {
            ForgeTextButton(text = action, fontSize = 13.sp, onClick = { onAction?.invoke() })
        }
    }
}
