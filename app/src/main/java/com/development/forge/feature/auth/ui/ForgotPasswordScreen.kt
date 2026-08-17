package com.development.forge.feature.auth.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.ForgeButton
import com.development.forge.core.components.ForgeTextButton
import com.development.forge.core.components.ForgeTextField
import com.development.forge.core.designsystem.ForgeFonts
import com.development.forge.core.designsystem.ForgeTheme

/**
 * DESIGN SPEC — Forgot Password screen (Screens.jsx → ForgotPasswordScreen).
 *
 * ## Top bar
 * - Back button + wordmark. (ForgeTopBar with onBack.)
 *
 * ## Layout, top to bottom
 * 1. Spacer **48.dp** — NOTE: smaller than Login/Registration (they use 64).
 * 2. Headline: "Cool down,\nreset it."  (same headline style as Login)
 * 3. Spacer **14.dp**
 * 4. Subtext: "Enter your email. We'll send a reset link." (same subtext style)
 * 5. Spacer **40.dp**
 * 6. Email field (ForgeTextField, mail icon). Only one field on this screen.
 * 7. Flexible spacer: weight(1f), minimum height **24.dp**
 * 8. Primary button "Send reset link"
 *    → same rules: disabled until valid, spinner while loading,
 *      arrow only when valid and not loading.
 * 9. Spacer **16.dp**
 * 10. Footer, centered: "Remembered it? " + text button "Log in"
 *     → "Remembered it?" is **14.sp**, color **#B5A990**
 *     → tap "Log in" → go back (same as the back arrow)
 *
 * ## Validation
 * - canSubmit = email matches the regex AND not loading. Nothing else.
 *
 * @param modifier from the nav host.
 * @param onLogin tap "Log in" (navigate back).
 * @param onSubmit tap "Send reset link" with a valid email.
 */
@Preview
@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    onSendLink: () -> Unit = { },
    onRemembered: () -> Unit = { }
) {
    var emailInput by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 28.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Cool down,\nreset it.",
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
            text = "Enter your email. We'll send a reset link.", style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                lineHeight = (1.45).em,
                color = ForgeTheme.colors.fgSecondary
            )
        )
        Spacer(modifier = Modifier.height(40.dp))
        ForgeTextField(value = emailInput, onValueChange = { value ->
            emailInput = value
        }, label = "Email", leadingIcon = Icons.Default.MailOutline) {

        }
        Spacer(
            modifier = Modifier
                .heightIn(min = 24.dp)
                .weight(1f)
        )
        ForgeButton(
            label = "Send reset link",
            isEnabled = emailInput.matches(validEmailRegularExpression),
            isLoading = isLoading
        ) {
            onSendLink()
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Remembered it? ",
                style = TextStyle(
                    fontFamily = ForgeFonts.spaceGrotesk,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = ForgeTheme.colors.fgSecondary
                )
            )
            ForgeTextButton(text = "Log in") {
                onRemembered()
            }
        }
    }
}