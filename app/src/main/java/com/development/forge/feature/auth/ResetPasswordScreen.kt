package com.development.forge.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.ForgeButton
import com.development.forge.core.components.ForgePasswordField
import com.development.forge.core.designsystem.ForgeFonts

/**
 * DESIGN SPEC — Reset Password screen (Screens.jsx → ResetPasswordScreen).
 *
 * ## Top bar
 * - Back button + wordmark. (ForgeTopBar with onBack.)
 *
 * ## Layout, top to bottom
 * 1. Spacer **48.dp** (same as Forgot Password).
 * 2. Headline: "Set a new\npassword."  (same headline style)
 * 3. Spacer **14.dp**
 * 4. Subtext: "Make it strong. 8+ characters."  (same subtext style)
 * 5. Spacer **40.dp**
 * 6. Password field #1 — label "New password". No helper text.
 * 7. Spacer **16.dp**
 * 8. Password field #2 — label "Confirm password". Helper/error is DYNAMIC:
 *    - confirm is not empty AND does not match  → error: "Passwords don't match."
 *    - both filled AND they match               → helper: "Passwords match."
 *    - new password has text, no match yet      → helper: "Keep going."
 *    - otherwise                                → nothing.
 * 9. Flexible spacer: weight(1f), minimum height **24.dp**
 * 10. Primary button — label is **"Hammer it in"** (yes, really — brand voice).
 *     Same rules: disabled until valid, spinner while loading,
 *     arrow only when valid and not loading.
 * 11. No footer on this screen.
 *
 * ## Validation
 * - canSubmit = new password length >= 8 AND both passwords match AND not loading.
 *
 * ## State to hoist (same pattern as Registration)
 * - password, confirm, isLoading → rememberSaveable
 * - the match / mismatch / canSubmit checks → derivedStateOf
 *
 * @param modifier from the nav host.
 * @param onSubmit tap "Hammer it in" with a valid form.
 */
@Preview
@Composable
fun ResetPasswordScreen(
    modifier: Modifier = Modifier,
    onResetPassword: () -> Unit = { }
) {
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var confirmPasswordInput by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    val isEnabled by remember {
        derivedStateOf {
            (passwordInput.isNotEmpty() && confirmPasswordInput.isNotEmpty()) && (passwordInput == confirmPasswordInput) && (passwordInput.length >= 8) && !isLoading
        }
    }
    val helper by remember {
        derivedStateOf {
            when {
                (confirmPasswordInput.isNotEmpty()) && passwordInput == confirmPasswordInput -> "Passwords match."
                passwordInput.isNotEmpty() && passwordInput != confirmPasswordInput -> "Keep going."
                else -> ""
            }
        }
    }
    val error by remember {
        derivedStateOf {
            when {
                (confirmPasswordInput.isNotEmpty()) && passwordInput != confirmPasswordInput -> "Passwords don't match."
                else -> ""
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 28.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Set a new\npassword.", style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 36.sp,
                fontWeight = FontWeight.W600,
                lineHeight = (1.05).em,
                letterSpacing = (-0.003).em
            ), color = Color(0xFFF5EDDE)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Make it strong. 8+ characters.", style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                lineHeight = (1.45).em,
                color = Color(0xFFB5A990)
            )
        )
        Spacer(modifier = Modifier.height(40.dp))
        ForgePasswordField(value = passwordInput, onValueChange = { value ->
            passwordInput = value
        }, label = "New password")
        Spacer(modifier = Modifier.height(16.dp))
        ForgePasswordField(
            value = confirmPasswordInput,
            onValueChange = { value ->
                confirmPasswordInput = value
            },
            label = "Confirm password",
            helper = helper.ifEmpty { null },
            error = error.ifEmpty { null })
        Spacer(
            modifier = Modifier
                .heightIn(min = 24.dp)
                .weight(1f)
        )
        ForgeButton(label = "Hammer it in", isEnabled = isEnabled, isLoading = isLoading) {
            isLoading = true
            onResetPassword()
        }
    }
}