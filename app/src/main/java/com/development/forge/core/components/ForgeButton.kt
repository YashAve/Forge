package com.development.forge.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.designsystem.ForgeFonts

/**
 * DESIGN SPEC — Forge Design System button (source: external Claude Design project).
 *
 * ## Base — shared by all button types
 * - **height**: 56.dp
 * - **cornerRadius**: 16.dp (rectangular, not pill)
 * - **font**: Space Grotesk, fontSize 16.sp, fontWeight 600, letterSpacing -0.005em
 * - **content**: centered, gap 10.dp between icon and text
 * - **padding**: horizontal 24.dp
 * - **press scale**: 0.985 — a subtle shrink when pressed (the button "presses in")
 * - **transition**: 120ms with easing `cubic-bezier(0.22, 1, 0.36, 1)`
 *   (the `--ease-out` token — same one from the splash breathe)
 *
 * ## PrimaryButton specifics
 * - **width**: full (fillMaxWidth) by default
 *
 * Background (by state):
 * - default → `#F58A00` (amber-400)
 * - pressed → `#D46E00` (amber-500, darker)
 * - disabled → `#3A3020` (bg-press)
 *
 * Text color (by state):
 * - enabled → `#1A1207` (fg-on-amber) — dark text on amber, NOT white.
 *   This is the one people get wrong.
 * - disabled → `#4A4338` (fg-disabled)
 *
 * Shadow — an amber-tinted glow (not black):
 * - default → `0 8px 24px rgba(245,138,0,0.22)` + `0 2px 4px rgba(245,138,0,0.18)`
 * - pressed / disabled → none
 *
 * Also:
 * - **opacity**: 0.6 when disabled
 * - **icons**: size 20.dp (left icon and/or right `iconRight` — Registration uses
 *   arrow_forward on the right when the form is valid)
 *
 * ## Loading state
 * When loading, the content is replaced by a spinner:
 * - 18.dp circle, 2.dp stroke
 * - track color `rgba(26,18,7,0.25)`, active/top color `#1A1207` (fg-on-amber)
 * - spins 0.7s linear, infinite
 *
 * ## Compose gotchas
 * 1. **Dark text on amber** — `#1A1207`, not white. Amber is a light-ish color;
 *    white text on it fails contrast and looks wrong.
 * 2. **Press feedback is three things at once** — darker amber (`#D46E00`) + shadow
 *    removed + scale to 0.985. All driven by the pressed state, all on the 120ms
 *    ease-out. Track pressed via `interactionSource.collectIsPressedAsState()`.
 * 3. **Amber-tinted shadow is not trivial in Compose.** `Modifier.shadow()` draws a
 *    black shadow by default. To get the amber glow, use
 *    `Modifier.shadow(elevation, shape, ambientColor = Amber, spotColor = Amber)`
 *    (the colored-shadow params, API 28+) — or draw it yourself with a `drawBehind`
 *    blurred round-rect. The colored-shadow route is simpler; just know the
 *    default black shadow won't match.
 * 4. **Enabled/disabled derives from the form, not an internal flag** — same
 *    hoisting principle as the text field. The screen passes `enabled = canSubmit`;
 *    the button just renders it.
 *
 * @param modifier Applied to the button root. Width is full ([fillMaxWidth]) by
 *   default; height is fixed at 56.dp with horizontal padding 24.dp.
 * @param label Button text — Space Grotesk, 16.sp, weight 600, letterSpacing
 *   -0.005em. Enabled color `#1A1207` (fg-on-amber, dark — not white), disabled
 *   `#4A4338` (fg-disabled).
 * @param leadingIcon Optional 20.dp icon before the text, 10.dp gap.
 * @param trailingIcon Optional 20.dp icon after the text, 10.dp gap — Registration
 *   uses `arrow_forward` here when the form is valid.
 * @param isEnabled Hoisted from the form (`enabled = canSubmit`) — never an
 *   internal flag. Disabled: background `#3A3020`, text `#4A4338`, opacity 0.6,
 *   no shadow.
 * @param onClick Invoked on tap. Press feedback while touched: background
 *   `#D46E00`, shadow removed, scale 0.985 — all on the 120ms ease-out.
 */
@Preview
@Composable
fun ForgeButton(
    modifier: Modifier = Modifier,
    label: String = "Continue",
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isEnabled: Boolean = true,
    isIcon: Boolean = true,
    onClick: () -> Unit = { }
) {

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var loading by rememberSaveable { mutableStateOf(false) }

    val buttonColor by animateColorAsState(
        targetValue = when {
            isEnabled -> Amber
            isPressed -> Amber500
            else -> DeepEarthBrown
        }
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            isEnabled -> DarkEspresso
            else -> AgedBrass
        }
    )

    val shape = RoundedCornerShape(corner = CornerSize(16.dp))
    ElevatedButton(
        onClick = {
            onClick()
        }, shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(height = 56.dp)
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    color = androidx.compose.ui.graphics.Color(0x2EF58A00),
                    offset = DpOffset(0.dp, 2.dp),
                    radius = 24.dp
                )
            ),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = buttonColor,
            contentColor = contentColor
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    letterSpacing = (-0.0005).em
                )
            )
            if (isIcon) {
                Spacer(modifier = Modifier.width(10.dp))
                Icon(Icons.AutoMirrored.Default.ArrowForward, null, modifier = Modifier.size(22.dp))
            }
        }
    }
}