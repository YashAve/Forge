package com.development.forge.core.components

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * DESIGN SPECIFICATION — ForgeCard
 * Source: Forge Design System, AppComponents.jsx → ForgeCard
 *
 * A reusable card container used throughout the app. It has two visual variants
 * controlled by the accent parameter. The plain variant is calm and subtle — it
 * sits in the background. The accent variant is warm and prominent — it demands
 * attention. Both share the same shape and corner radius.
 *
 * ═══════════════════════════════════════════════════════════════
 * SHAPE
 * ═══════════════════════════════════════════════════════════════
 * Corner radius: 16dp on all four corners. This applies to the clip, the
 * border, and the shadow shape — all three must use the same radius or the
 * card will look misaligned at its edges.
 *
 * ═══════════════════════════════════════════════════════════════
 * PLAIN VARIANT (accent = false)
 * ═══════════════════════════════════════════════════════════════
 *
 * BACKGROUND
 * A flat fill of #1C180F (bg-raised). No gradient wash. No tint.
 *
 * BORDER
 * 1dp, cream at 10% opacity — Color(0xFFF5EDDE).copy(alpha = 0.10f).
 * Barely visible. Just enough to separate the card from the background.
 *
 * SHADOW
 * Black at 28% opacity — Color.Black.copy(alpha = 0.28f).
 * Offset 2dp downward, x = 0dp (no horizontal lean).
 * Blur radius approximately 14dp.
 * Subtle — the card feels slightly lifted but not dramatic.
 *
 * ═══════════════════════════════════════════════════════════════
 * ACCENT VARIANT (accent = true)
 * ═══════════════════════════════════════════════════════════════
 *
 * BACKGROUND — two layers painted in sequence
 * Layer 1 (bottom): the same flat #1C180F base fill as the plain card.
 * Layer 2 (top): a diagonal gradient wash applied ON TOP of the base fill.
 *   The gradient runs from the top-left corner to the bottom-right corner
 *   (a top-left → bottom-right linearGradient is close enough to the design's
 *   160-degree angle).
 *   Color stops:
 *     0.0f → Amber at 10% opacity   (bright warm start at the top-left)
 *     0.6f → Amber at 2% opacity    (nearly gone by 60% of the way across)
 *     1.0f → Color.Transparent      (fully gone at the bottom-right)
 *   In Compose, apply this as a second .background(Brush.linearGradient(...))
 *   modifier AFTER the first solid .background(Color(0xFF1C180F)).
 *
 * BORDER
 * 1dp, amber at 28% opacity — Amber.copy(alpha = 0.28f).
 * Noticeably warmer and more present than the plain card's border. The amber
 * border is part of the accent identity — it frames the gradient wash.
 *
 * SHADOW
 * Black at 38% opacity — Color.Black.copy(alpha = 0.38f).
 * Offset 12dp downward, x = 0dp.
 * Blur radius approximately 32dp.
 * Large and dramatic. This shadow is what makes the focus card feel like it
 * is floating above the screen with real physical weight.
 *
 * ═══════════════════════════════════════════════════════════════
 * PRESS FEEDBACK (only when onClick is not null)
 * ═══════════════════════════════════════════════════════════════
 * When the card is tappable, pressing it scales the whole card down to 0.99
 * of its size over 120ms on the ease-out curve. This is the only press
 * feedback — there is NO ripple. Set indication = null on the clickable
 * modifier. The scale itself communicates "I responded to your tap."
 *
 * Track the pressed state using an interactionSource and
 * collectIsPressedAsState(), then drive the scale with animateFloatAsState:
 *   targetValue = if (isPressed) 0.99f else 1f
 *   animationSpec = tween(120, easing = EaseOut)
 * Apply the scale via graphicsLayer { scaleX = scale; scaleY = scale }.
 *
 * When onClick is null the card is not tappable and no scale animation is
 * needed. Use a conditional .then() to attach the clickable modifier only
 * when onClick is not null.
 *
 * ═══════════════════════════════════════════════════════════════
 * MODIFIER ORDER — this matters, do not rearrange
 * ═══════════════════════════════════════════════════════════════
 * 1. graphicsLayer { scale }     — outermost, scales the whole rendered card
 * 2. dropShadow(...)             — before clip, so shadow bleeds outside bounds
 * 3. clip(RoundedCornerShape)    — seals the shape; everything after is inside
 * 4. background(#1C180F)         — base fill, inside the clip
 * 5. background(gradient) if accent — wash on top of base fill
 * 6. border(...)                 — on top of both background layers
 * 7. clickable(...) if tappable  — last, defines the tap target
 *
 * ═══════════════════════════════════════════════════════════════
 * PARAMETERS
 * ═══════════════════════════════════════════════════════════════
 * @param modifier  applied to the root Box, allowing the caller to set size,
 *                  padding, and positioning from outside.
 * @param accent    true for the amber-tinted focus variant, false for the
 *                  plain subtle variant. Defaults to false.
 * @param onClick   optional tap handler. When null the card is not interactive
 *                  and no press animation plays. When provided the card scales
 *                  on press and calls this when released.
 * @param content   the composable content rendered inside the card. The card
 *                  itself has no internal padding — the caller is responsible
 *                  for padding the content as needed.
 */

@Preview
@Composable
fun ForgeCard(
    modifier: Modifier = Modifier,
    accent: Boolean = true,
    onClick: (() -> Unit)? = { },
    content: @Composable () -> Unit = { }
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.99f else 1f,
        tween(120, easing = EaseOut)
    )
    Box(
        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .dropShadow(
                shape = RoundedCornerShape(16.dp), shadow = Shadow(
                    offset = DpOffset(0.dp, if (accent) 12.dp else 2.dp),
                    radius = if (accent) 32.dp else 14.dp,
                    color = Color.Black.copy(alpha = if (accent) 0.38f else 0.28f)
                )
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C180F))
            .then(
                if (accent) Modifier.background(
                    Brush.linearGradient(
                        0.0f to Amber.copy(alpha = 0.10f),
                        0.6f to Amber.copy(alpha = 0.02f),
                        1f to Color.Transparent
                    )
                ) else Modifier
            )
            .border(
                width = 1.dp,
                color = if (accent) Amber.copy(alpha = 0.28f) else Color(0xFFF5EDDE).copy(alpha = 0.10f),
                shape = RoundedCornerShape(16.dp)
            )
            .then(
                onClick?.let {
                    Modifier.clickable(
                        enabled = true,
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } ?: Modifier
            )) {
        content()
    }
}
