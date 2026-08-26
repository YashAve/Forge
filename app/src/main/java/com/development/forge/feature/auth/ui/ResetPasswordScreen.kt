package com.development.forge.feature.auth.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.ForgeButton
import com.development.forge.core.components.ForgePasswordField
import com.development.forge.core.components.ForgeTextField
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme
import com.development.forge.feature.auth.contract.AuthError
import com.development.forge.feature.auth.contract.AuthState

/**
 * DESIGN SPEC — Reset Password screen (Screens.jsx → ResetPasswordScreen).
 *
 * ## Top bar
 * - Back button + wordmark. (ForgeTopBar with onBack.)
 *
 * ## Layout, top to bottom
 * 1. Spacer **48.dp**
 * 2. Headline: "Set a new\npassword."
 * 3. Spacer **14.dp**
 * 4. Subtext: "Make it strong. 8+ characters."
 * 5. Spacer **40.dp**
 * 6. Reset token field — NOT in the original design. Added because password-reset
 *    email delivery is out of scope: the server logs the token to its console
 *    instead of emailing a link, so the user enters it by hand.
 * 7. Spacer **16.dp**
 * 8. Password field #1 — label "New password". No helper text.
 * 9. Spacer **16.dp**
 * 10. Password field #2 — label "Confirm password". Helper/error is DYNAMIC:
 *    - confirm is not empty AND does not match  → error: "Passwords don't match."
 *    - both filled AND they match               → helper: "Passwords match."
 *    - new password has text, no match yet      → helper: "Keep going."
 *    - otherwise                                → nothing.
 * 11. Flexible spacer: weight(1f), minimum height **24.dp**
 * 12. Primary button — label **"Hammer it in"** (brand voice).
 * 13. No footer on this screen.
 *
 * ## Validation
 * - The button gates only on empty fields and matching passwords — the "8+
 *   characters" rule is enforced in the ViewModel so a failure surfaces as a
 *   readable error instead of a silently dead button.
 *
 * @param modifier from the nav host.
 * @param state the shared auth state.
 * @param onUpdateResetToken the token field changed.
 * @param onUpdatePassword the new-password field changed.
 * @param onUpdateConfirmPassword the confirm field changed.
 * @param onResetPassword tap "Hammer it in".
 */
@Composable
fun ResetPasswordScreen(
    modifier: Modifier = Modifier,
    state: AuthState = AuthState(),
    onUpdateResetToken: (String) -> Unit = { },
    onUpdatePassword: (String) -> Unit = { },
    onUpdateConfirmPassword: (String) -> Unit = { },
    onResetPassword: () -> Unit = { }
) {
    val passwordsMatch by remember(state.password, state.confirmPassword) {
        derivedStateOf { state.password == state.confirmPassword }
    }

    val helper by remember(state.password, state.confirmPassword) {
        derivedStateOf {
            when {
                state.confirmPassword.isNotEmpty() && passwordsMatch -> "Passwords match."
                state.password.isNotEmpty() && !passwordsMatch -> "Keep going."
                else -> ""
            }
        }
    }

    val confirmError by remember(state.password, state.confirmPassword) {
        derivedStateOf {
            if (state.confirmPassword.isNotEmpty() && !passwordsMatch) {
                "Passwords don't match."
            } else {
                ""
            }
        }
    }

    val isEnabled = !state.isLoading &&
            state.resetToken.isNotBlank() &&
            state.password.isNotEmpty() &&
            state.confirmPassword.isNotEmpty() &&
            passwordsMatch

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 28.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Set a new\npassword.",
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
            text = "Make it strong. 8+ characters.",
            style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                lineHeight = (1.45).em,
                color = ForgeTheme.colors.fgSecondary
            )
        )
        Spacer(modifier = Modifier.height(40.dp))

        ForgeTextField(
            value = state.resetToken,
            onValueChange = onUpdateResetToken,
            error = state.error.tokenMessage(),
            label = "Reset token",
            helper = "From the reset email.",
            leadingIcon = Icons.Default.Key
        )
        Spacer(modifier = Modifier.height(16.dp))

        ForgePasswordField(
            value = state.password,
            onValueChange = onUpdatePassword,
            error = state.error.passwordMessage(),
            label = "New password"
        )
        Spacer(modifier = Modifier.height(16.dp))

        ForgePasswordField(
            value = state.confirmPassword,
            onValueChange = onUpdateConfirmPassword,
            label = "Confirm password",
            helper = helper.ifEmpty { null },
            error = confirmError.ifEmpty { null }
        )

        Spacer(
            modifier = Modifier
                .heightIn(min = 24.dp)
                .weight(1f)
        )

        ForgeButton(
            label = "Hammer it in",
            isEnabled = isEnabled,
            isLoading = state.isLoading,
            icon = if (isEnabled) Icons.AutoMirrored.Default.ArrowForward else null,
            onClick = onResetPassword
        )
    }
}

@Preview
@Composable
private fun ResetPasswordScreenPreview() {
    ForgeTheme {
        ResetPasswordScreen(
            state = AuthState(error = AuthError.EXPIRED_TOKEN)
        )
    }
}

private fun AuthError?.tokenMessage(): String? = when (this) {
    AuthError.INVALID_TOKEN -> "That token isn't valid"
    AuthError.EXPIRED_TOKEN -> "That token has expired. Request a new one"
    else -> null
}

private fun AuthError?.passwordMessage(): String? = when (this) {
    AuthError.EMPTY_PASSWORD -> "Set a password"
    AuthError.WEAK_PASSWORD -> "At least 8 characters"
    AuthError.NETWORK_ERROR -> "Can't reach the server. Check your connection"
    else -> null
}