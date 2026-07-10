package com.development.forge.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MailOutline
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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.development.forge.core.components.ForgeButton
import com.development.forge.core.components.ForgePasswordField
import com.development.forge.core.components.ForgeTextField
import com.development.forge.core.designsystem.ForgeFonts

val validEmailRegularExpression = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

@Composable
fun RegistrationScreen(modifier: Modifier = Modifier) {
    var emailInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val canSubmit by remember {
        derivedStateOf {
            emailInput.matches(validEmailRegularExpression) &&
                    passwordInput.length >= 8 &&
                    !isLoading
        }
    }

    Column(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 28.dp)
    ) {
        Spacer(Modifier.height(64.dp))

        // Headline block
        Text(
            text = "Forge an\naccount.",
            style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 36.sp,
                fontWeight = FontWeight.W600,
                lineHeight = 1.05.em,
                letterSpacing = (-0.03).em,
                color = Color(0xFFF5EDDE)
            )
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Stop putting it off.",
            style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                lineHeight = 1.45.em,
                color = Color(0xFFB5A990)
            )
        )

        Spacer(Modifier.height(40.dp))

        // Fields — 16dp gap
        ForgeTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = "Email",
            leadingIcon = Icons.Default.MailOutline
        )
        Spacer(Modifier.height(16.dp))
        ForgePasswordField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = "Password",
            helper = "8 characters minimum."          // ← was missing
        )

        // flexible spacer pushes the CTA stack to the bottom
        Spacer(
            Modifier
                .heightIn(min = 24.dp)
                .weight(1f)
        )

        // CTA stack — 16dp gap
        ForgeButton(
            label = "Continue",
            isEnabled = canSubmit,
            isLoading = isLoading,                     // plain Boolean now
            icon = if (canSubmit && !isLoading)        // ← arrow only when valid
                Icons.AutoMirrored.Default.ArrowForward else null,
            onClick = { isLoading = true }
        )
        Spacer(Modifier.height(16.dp))

        // Terms microcopy — 12sp, centered, marginTop -4 → net 12dp above
        TermsMicrocopy(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp),
            onTerms = { /* TODO open terms dialog */ },
            onPrivacy = { /* TODO open privacy dialog */ }
        )
    }
}

@Composable
private fun TermsMicrocopy(
    modifier: Modifier = Modifier,
    onTerms: () -> Unit,
    onPrivacy: () -> Unit
) {
    val linkStyle = SpanStyle(
        color = Color(0xFFB5A990),                    // fg-secondary
        textDecoration = TextDecoration.Underline
    )
    val annotated = buildAnnotatedString {
        append("By continuing you agree to our ")
        withLink(LinkAnnotation.Clickable("terms") { onTerms() }) {
            withStyle(linkStyle) { append("Terms") }
        }
        append(" and ")
        withLink(LinkAnnotation.Clickable("privacy") { onPrivacy() }) {
            withStyle(linkStyle) { append("Privacy") }
        }
        append(".")
    }
    Text(
        text = annotated,
        modifier = modifier,
        style = TextStyle(
            fontFamily = ForgeFonts.spaceGrotesk,
            fontSize = 12.sp,
            lineHeight = 1.5.em,
            color = Color(0xFF7A7060),                // fg-tertiary
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    )
}