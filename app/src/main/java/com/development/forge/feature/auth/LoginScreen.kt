package com.development.forge.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.R
import com.development.forge.core.components.ForgeButton
import com.development.forge.core.components.ForgePasswordField
import com.development.forge.core.components.ForgeSecondaryButton
import com.development.forge.core.components.ForgeTextButton
import com.development.forge.core.components.ForgeTextField
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme

/**
 * DESIGN SPEC — Login screen (source: Forge Design System, Screens.jsx → LoginScreen).
 *
 * ## Top bar
 * - Wordmark only. NO back button. (Handled by ForgeTopBar in the nav host.)
 *
 * ## Layout, top to bottom
 * 1.  Spacer **64.dp**
 * 2.  Headline: "Welcome\nback."
 *     → Space Grotesk, **36.sp**, weight **600**, lineHeight **1.05.em**,
 *       letterSpacing **(-0.03).em**, color **#F5EDDE**
 * 3.  Spacer **14.dp**
 * 4.  Subtext: "Pick up where you left off."
 *     → **16.sp**, weight **400**, lineHeight **1.45.em**, color **#B5A990**
 * 5.  Spacer **40.dp**
 * 6.  Email field (ForgeTextField, mail icon)
 * 7.  Spacer **16.dp**
 * 8.  Password field (ForgePasswordField, label "Password", no helper)
 * 9.  Spacer **2.dp**, then a "Forgot password?" text button.
 *     Align it to the END (right side). Tap → go to ForgotPassword.
 * 10. Flexible spacer: weight(1f), minimum height **24.dp**
 * 11. Primary button "Continue"
 * 12. Spacer **16.dp**
 * 13. Divider with the word "or"
 * 14. Spacer **16.dp**
 * 15. Secondary button "Continue with Google"
 * 16. Spacer **24.dp** (16 gap + 8 extra margin in the design)
 * 17. Footer, centered: "New here? " + text button "Forge an account"
 *     → "New here?" is **14.sp**, weight 400, color **#B5A990**
 *     → tap "Forge an account" → go to Registration
 *
 * ## Validation (different from Registration!)
 * - canSubmit = email matches the regex AND password is NOT empty AND not loading.
 * - Login does NOT check the 8-character rule. Any non-empty password is fine.
 * - Arrow icon on "Continue": show ONLY when canSubmit is true and not loading.
 *
 * ## New components this screen needs
 *
 * ### ForgeTextButton
 * - Text only. No background. No border.
 * - Color: amber **#F58A00**. Space Grotesk, **14.sp**, weight **600**,
 *   letterSpacing **(-0.005).em**.
 * - Padding: **6.dp** top/bottom, **4.dp** left/right.
 * - While pressed: opacity **0.7**, animated 120ms ease-out.
 *
 * ### ForgeDivider
 * - A Row, centered vertically. Gap **12.dp** between line and text.
 * - Left line: height **1.dp**, weight(1f), color **#F5EDDE at 10% alpha**.
 * - Text "or": **13.sp**, weight **500**, color **#7A7060**.
 * - Right line: same as the left line.
 *
 * ### ForgeSecondaryButton
 * - Same base as the primary button: height **56.dp**, corner **16.dp**,
 *   full width, text **16.sp** weight 600 letterSpacing (-0.005).em,
 *   icon-text gap **10.dp**, press scale **0.985**, 120ms ease-out.
 * - Background: **#1C180F**. While pressed: **#3A3020**.
 * - Text color: cream **#F5EDDE** (always).
 * - Border: **1.5.dp**, color **#F5EDDE at 18% alpha**.
 * - Shadow: small BLACK shadow (0 1px 2px, black 35%). None while pressed.
 *   Note: only the PRIMARY button gets the amber glow. This one is black.
 * - Disabled: opacity **0.4** (the primary uses 0.6 — they differ).
 * - Google icon: **20.dp**. This is an image (the colored G), not a vector icon.
 *   Add it as a drawable resource.
 *
 * @param modifier from the nav host.
 * @param onForgotPassword tap "Forgot password?".
 * @param onRegister tap "Forge an account".
 * @param onGoogle tap the Google button.
 * @param onSubmit tap "Continue" with a valid form.
 */
@Preview
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onForgotPassword: () -> Unit = { },
    onForgeAccount: () -> Unit = { },
    onLogin: () -> Unit = { }
) {

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var emailInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val canSubmit by remember {
        derivedStateOf {
            emailInput.matches(validEmailRegularExpression) && passwordInput.length >= 8 && !isLoading
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 28.dp)
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = "Welcome\nback.",
            style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 36.sp,
                fontWeight = FontWeight.W600,
                lineHeight = (1.05).em,
                letterSpacing = (-0.003).em
            ),
            color = ForgeTheme.colors.fgPrimary
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Pick up where you left off.", style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                lineHeight = (1.45).em,
                color = ForgeTheme.colors.fgSecondary
            )
        )
        Spacer(modifier = Modifier.height(40.dp))
        ForgeTextField(
            value = emailInput, onValueChange = { value ->
                emailInput = value
            }, label = "Email",
            leadingIcon = Icons.Default.MailOutline
        )
        Spacer(modifier = Modifier.height(16.dp))
        ForgePasswordField(
            value = passwordInput, onValueChange = { value ->
                passwordInput = value
            }, label = "Password"
        )
        Spacer(modifier = Modifier.height(2.dp))
        ForgeTextButton(
            text = "Forgot password?", onClick = onForgotPassword, modifier = Modifier.align(
                Alignment.End
            )
        )
        Spacer(
            modifier = Modifier
                .defaultMinSize(minHeight = 24.dp)
                .weight(1f)
        )
        ForgeButton(
            label = "Continue",
            isEnabled = canSubmit,
            isLoading = isLoading,                     // plain Boolean now
            icon = if (canSubmit && !isLoading)        // ← arrow only when valid
                Icons.AutoMirrored.Default.ArrowForward else null,
            onClick = {
                isLoading = true
                onLogin()
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .background(color = ForgeTheme.colors.borderDefault)
                    .weight(1f)
            )
            Text(
                text = "or",
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W500,
                    color = ForgeTheme.colors.fgTertiary
                ),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .background(color = ForgeTheme.colors.borderDefault)
                    .weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        ForgeSecondaryButton(leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.google_icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),          // design: 20.dp
                tint = Color.Unspecified                  // keep the 4 brand colors
            )
        }) {
            onLogin()
        }
        Spacer(modifier = Modifier.height(24.dp))
        /*Footer, centered: "New here? " + text button "Forge an account" → "New here?" is 14.sp, weight 400, color #B5A990 →
         tap "Forge an account" → go to Registration*/
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New here?",
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = ForgeTheme.colors.fgSecondary
                )
            )
            ForgeTextButton(text = "Forge an account") {
                onForgeAccount()
            }
        }
    }
}