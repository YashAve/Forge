package com.development.forge.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * DESIGN SPECIFICATION — ForgeCheckbox
 * Source: Forge Design System, AppComponents.jsx → ForgeCheckbox
 *
 * A custom checkbox with a satisfying spring-animated check mark. Used in task
 * rows to mark tasks done. The whole point of the design is the check mark's
 * spring overshoot — it should feel like a physical stamp landing, not a digital
 * toggle flipping.
 *
 * ═══════════════════════════════════════════════════════════════
 * SHAPE AND SIZE
 * ═══════════════════════════════════════════════════════════════
 * A rounded square, sized by the size parameter (default 26dp, but task rows
 * pass 24dp in compact mode). Corner radius 9dp. Border width 2dp.
 *
 * ═══════════════════════════════════════════════════════════════
 * THE BOX — border and background, both animated on checked state
 * ═══════════════════════════════════════════════════════════════
 * BORDER COLOR (animated, tween 160ms ease-out):
 *   unchecked → cream at 18% opacity — Color(0xFFF5EDDE).copy(alpha = 0.18f)
 *   checked   → solid Amber
 *
 * BACKGROUND COLOR (animated, tween 160ms ease-out):
 *   unchecked → Color.Transparent (empty box, only the border shows)
 *   checked   → solid Amber (filled box)
 *
 * GLOW (only when checked):
 *   A dropShadow with Amber at 40% opacity, offset (0dp, 0dp) — centered, no
 *   directional lean, because the box itself is glowing rather than casting a
 *   shadow beneath it — and radius about 12dp.
 *   Apply conditionally with .then(): present when checked, absent when not.
 *
 * ═══════════════════════════════════════════════════════════════
 * THE CHECK MARK — two separate animations
 * ═══════════════════════════════════════════════════════════════
 * The check icon sits centered inside the box. Its size is (boxSize - 8)dp — so
 * for a 26dp box the check is 18dp. Its color is #1A1207 (fg-on-amber, the dark
 * color that reads against the amber fill). Weight 700 (bold).
 *
 * It has TWO independent animations running at once:
 *
 * SCALE (the satisfying one — spring/overshoot):
 *   unchecked → 0f (invisible, collapsed to nothing)
 *   checked   → 1f (full size)
 *   animationSpec: tween(180, easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f))
 *   That curve OVERSHOOTS past 1.0 before settling — the check mark briefly
 *   grows larger than full size, then snaps back. This overshoot IS the
 *   satisfying feeling. It reads as physical momentum, like a stamp landing.
 *
 * ALPHA (fades in cleanly):
 *   unchecked → 0f (transparent)
 *   checked   → 1f (opaque)
 *   animationSpec: tween(120, easing = EaseOut)
 *   Faster than the scale, and no overshoot — alpha just ensures the check
 *   doesn't pop in hard. The scale does the character work; alpha smooths the
 *   entrance.
 *
 * Apply BOTH via a single graphicsLayer on the check icon:
 *   Modifier.graphicsLayer {
 *       scaleX = scale
 *       scaleY = scale
 *       this.alpha = alpha
 *   }
 *
 * ═══════════════════════════════════════════════════════════════
 * INTERACTION
 * ═══════════════════════════════════════════════════════════════
 * The whole box is clickable, with NO ripple (indication = null). Tapping it
 * calls onChange with the opposite of the current checked value — onChange(!checked).
 *
 * IMPORTANT: when this checkbox sits inside a clickable task row, tapping the
 * checkbox must NOT also trigger the row's tap. In the web prototype this needs
 * stopPropagation; in Compose, a clickable child normally consumes the tap
 * itself, so this should work automatically — but verify on device that tapping
 * the checkbox does not also open the task detail.
 *
 * ═══════════════════════════════════════════════════════════════
 * MODIFIER ORDER (on the box)
 * ═══════════════════════════════════════════════════════════════
 * 1. size(size)
 * 2. dropShadow(...) if checked   — glow, before clip so it bleeds outside
 * 3. clip(RoundedCornerShape(9.dp))
 * 4. background(backgroundColor)  — animated fill, inside the clip
 * 5. border(2.dp, borderColor, RoundedCornerShape(9.dp)) — on top of fill
 * 6. clickable(indication = null) — the tap target
 * Then the check Icon inside, centered, with the graphicsLayer applied to it.
 *
 * ═══════════════════════════════════════════════════════════════
 * PARAMETERS
 * ═══════════════════════════════════════════════════════════════
 * @param checked   whether the box is currently checked.
 * @param onChange  called with the NEW value (!checked) when tapped.
 * @param size      the box's side length. Defaults to 26dp; task rows pass 24dp
 *                  in compact mode.
 * @param modifier  applied to the root box for external positioning.
 */

@Preview
@Composable
fun ForgeCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean = true,
    onChange: (Boolean) -> Unit = { },
    size: Dp = 24.dp
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) Amber else Color.Transparent,
        animationSpec = tween(160, easing = EaseOut)
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) Amber else Color(0xFFF5EDDE).copy(alpha = 0.18f),
        animationSpec = tween(160, easing = EaseOut)
    )
    val checkBoxSpring by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(
            180, easing = CubicBezierEasing(
                0.34f, 1.56f, 0.64f, 1f
            )
        )
    )
    val checkBoxAlpha by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(120, easing = EaseOut)
    )

    Box(
        modifier
            .size(size)
            .then(
                if (checked) Modifier.dropShadow(
                    shape = RoundedCornerShape(9.dp),
                    shadow = Shadow(
                        radius = 12.dp,
                        offset = DpOffset(0.dp, 0.dp),
                        color = Amber.copy(alpha = 0.40f)
                    )
                )
                else Modifier
            )
            .clip(RoundedCornerShape(9.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(9.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = true,
                onClick = {
                    onChange(!checked)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Check,
            null,
            modifier = Modifier
                .size(size - 8.dp)
                .graphicsLayer {
                    scaleX = checkBoxSpring
                    scaleY = checkBoxSpring
                    alpha = checkBoxAlpha
                },
            tint = Color(0xFF1A1207)
        )
    }
}
