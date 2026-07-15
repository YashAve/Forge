package com.development.forge.feature.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.Amber
import com.development.forge.core.components.FgTertiary
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.Oil
import com.development.forge.navigation.ForgeNavKey

/**
 * DESIGN SPECIFICATION — Bottom navigation bar
 * Source: Forge Design System, AppComponents.jsx → BottomNav
 *
 * ═══════════════════════════════════════════════════════════════
 * THE FOUR ITEMS
 * ═══════════════════════════════════════════════════════════════
 * In order, left to right:
 *
 *   1. "Focus"  — icon: bolt      — this is the Home screen
 *   2. "Add"    — icon: add       — the special centre button (see below)
 *   3. "Stats"  — icon: insights  — the progress screen
 *   4. "You"    — icon: person    — settings and profile
 *
 * Note the labels do not match the screen names — the Home screen's tab reads
 * "Focus", and the Settings screen's tab reads "You". That is deliberate voice,
 * not an error.
 *
 * ═══════════════════════════════════════════════════════════════
 * THE BAR ITSELF
 * ═══════════════════════════════════════════════════════════════
 * A Row whose four children each take an equal share of the width (weight 1f
 * each), stretched to fill the full width of the screen.
 *
 * Background: #1C180F, the raised surface colour.
 *
 * A 1dp border along the TOP edge only — cream at 10% opacity (border-default).
 * Not a full border, just the top hairline separating the bar from the content
 * above it. In Compose this is easiest as a drawBehind that draws a single line
 * across the top edge, or a thin Box above the Row.
 *
 * Padding: 4dp at the bottom of the bar as a whole. Each individual item is
 * padded 10dp at the top and 8dp at the bottom.
 *
 * ═══════════════════════════════════════════════════════════════
 * EACH ITEM
 * ═══════════════════════════════════════════════════════════════
 * A Column, horizontally centred, with a 4dp gap between the icon and its label.
 * The whole item is tappable, with no ripple — the colour change is the feedback.
 *
 * THE ICON, for the three ordinary tabs (Focus, Stats, You):
 *   Sized 24dp, no background behind it.
 *   When that tab is ACTIVE, the icon is amber and drawn in its FILLED style.
 *   When inactive, it is #7A7060 (tertiary) and drawn in its OUTLINE style.
 *   The colour animates over 160ms on the ease-out curve.
 *
 *   A note on the fill: the design uses Material Symbols' variable FILL axis to
 *   morph smoothly between the outline and filled forms. Compose's Material Icons
 *   do not support that variable axis — you have two separate icon assets
 *   instead, e.g. Icons.Outlined.Person and Icons.Filled.Person. So you swap the
 *   icon rather than animate the fill. The colour still animates; the shape just
 *   switches instantly. That is an acceptable and normal compromise.
 *
 * THE ICON, for the Add button (the special one):
 *   It sits inside an amber pill: 44dp wide, 32dp tall, 12dp corner radius,
 *   filled with solid amber #F58A00.
 *   The pill carries a glow: amber at 35% opacity, offset 4dp downward, reaching
 *   about 14dp outward.
 *   The icon inside is 24dp and coloured #1A1207 (the dark on-amber colour), NOT
 *   amber and NOT tertiary.
 *   Critically, the Add button NEVER shows an active state. It is always drawn
 *   the same way, because it does not represent a destination — it opens a sheet.
 *
 * THE LABEL, for every item including Add:
 *   Space Grotesk, 10.5sp, weight 600, letter spacing 0.02em.
 *   Amber when that tab is active, #7A7060 (tertiary) when it is not.
 *   The colour animates over 160ms on the ease-out curve.
 *   The Add button's label is always tertiary, since it is never active.
 *
 * ═══════════════════════════════════════════════════════════════
 * BEHAVIOUR — this is where the design and your architecture diverge
 * ═══════════════════════════════════════════════════════════════
 * The design treats all four items identically: every tap calls onSelect with an
 * id, including Add. That works for a prototype but is wrong for your app,
 * because Add is not a destination — it opens a bottom sheet over whatever
 * screen is currently showing.
 *
 * So your component needs TWO separate callbacks rather than one:
 *
 *   onSelect(key: ForgeNavKey) — for Focus, Stats and You. The host responds by
 *   clearing the back stack and adding the new key, since tabs are siblings that
 *   replace rather than push.
 *
 *   onAddClick() — for the Add button alone. The host responds by flipping the
 *   sheet's visibility state. The back stack is never touched.
 *
 * Keeping these separate also keeps the active-state logic honest: with a single
 * callback you would have to special-case Add everywhere to stop it being
 * highlighted, since it can never be the "current" destination.
 *
 * ═══════════════════════════════════════════════════════════════
 * WHERE IT LIVES
 * ═══════════════════════════════════════════════════════════════
 * In the Scaffold's bottomBar slot in ForgeNavHost, gated by the same kind of
 * `when` on the current destination that already gates the top bar. It shows for
 * Home, Tasks, Stats and Settings, and is absent everywhere else — splash, all
 * the auth screens, onboarding, and the task detail screen.
 *
 * ═══════════════════════════════════════════════════════════════
 * PARAMETERS
 * ═══════════════════════════════════════════════════════════════
 * @param current     the currently displayed destination, used to decide which
 *                    tab is highlighted.
 * @param onSelect    called when Focus, Stats or You is tapped.
 * @param onAddClick  called when the centre Add button is tapped.
 */

/*"Focus" — icon: bolt — this is the Home screen
"Add" — icon: add — the special centre button (see below)
"Stats" — icon: insights — the progress screen
"You" — icon: person — settings and profile*/

private val navigations = listOf(
    listOf(0, ForgeNavKey.Home, "Focus", Icons.Outlined.Bolt),
    listOf(1, ForgeNavKey.Add, "Add", Icons.Default.Add),
    listOf(2, ForgeNavKey.Statistics, "Stats", Icons.Default.Insights),
    listOf(3, ForgeNavKey.Settings, "You", Icons.Outlined.Person)
)

@Preview
@Composable
fun ForgeBottomNavigationBar(
    modifier: Modifier = Modifier,
    current: ForgeNavKey = ForgeNavKey.Home,
    onSelect: () -> Unit = { },
    onAddClick: () -> Unit = { }
) {
    var selected by rememberSaveable { mutableStateOf(ForgeNavKey.Home.toString()) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .background(color = Oil)
            .drawBehind {

            }) {
        /** A Column, horizontally centred, with a 4dp gap between the icon and its label.
         * The whole item is tappable, with no ripple — the colour change is the feedback.
         *
         * THE ICON, for the three ordinary tabs (Focus, Stats, You):
         *   Sized 24dp, no background behind it.
         *   When that tab is ACTIVE, the icon is amber and drawn in its FILLED style.
         *   When inactive, it is #7A7060 (tertiary) and drawn in its OUTLINE style.
         *   The colour animates over 160ms on the ease-out curve.
         *
         *   A note on the fill: the design uses Material Symbols' variable FILL axis to
         *   morph smoothly between the outline and filled forms. Compose's Material Icons
         *   do not support that variable axis — you have two separate icon assets
         *   instead, e.g. Icons.Outlined.Person and Icons.Filled.Person. So you swap the
         *   icon rather than animate the fill. The colour still animates; the shape just
         *   switches instantly. That is an acceptable and normal compromise.
         *
         * THE ICON, for the Add button (the special one):
         *   It sits inside an amber pill: 44dp wide, 32dp tall, 12dp corner radius,
         *   filled with solid amber #F58A00.
         *   The pill carries a glow: amber at 35% opacity, offset 4dp downward, reaching
         *   about 14dp outward.
         *   The icon inside is 24dp and coloured #1A1207 (the dark on-amber colour), NOT
         *   amber and NOT tertiary.
         *   Critically, the Add button NEVER shows an active state. It is always drawn
         *   the same way, because it does not represent a destination — it opens a sheet.
         *
         * THE LABEL, for every item including Add:
         *   Space Grotesk, 10.5sp, weight 600, letter spacing 0.02em.
         *   Amber when that tab is active, #7A7060 (tertiary) when it is not.
         *   The colour animates over 160ms on the ease-out curve.
         *   The Add button's label is always tertiary, since it is never active.*/
        val color by animateColorAsState(
            targetValue = if (current == navigations[2]) Amber else FgTertiary,
            animationSpec = tween(160, easing = EaseOut),
            label = "label"
        )

        navigations.forEachIndexed { index, navigationItem ->
            Column(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 8.dp)
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            selected = (navigationItem[1] as ForgeNavKey).toString()
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(navigationItem[3] as ImageVector, null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = navigationItem[2] as String,
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = (10.5).sp,
                    fontWeight = FontWeight.W600,
                    letterSpacing = (0.02).em,
                    color = color
                )
            }
        }
    }
}