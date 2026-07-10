package com.development.forge.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

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
fun ForgotPasswordScreen(modifier: Modifier = Modifier) {

}