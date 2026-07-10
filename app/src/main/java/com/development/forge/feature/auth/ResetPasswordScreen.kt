package com.development.forge.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

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
fun ResetPasswordScreen(modifier: Modifier = Modifier) {
    
}