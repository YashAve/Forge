package com.development.forge.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.development.forge.core.components.ForgeTextField
import com.development.forge.core.designsystem.ForgeFonts

val validEmailRegularExpression = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

@Preview
@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    var emailInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }

    val canSubmit by rememberSaveable {
        derivedStateOf {
            emailInput.matches(validEmailRegularExpression) && passwordInput.length >= 8
        }
    }

    Column(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 28.dp)
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = "Forge an\naccount.",
            style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk,
                fontSize = 36.sp,
                fontWeight = FontWeight.W600,
                lineHeight = 37.8.sp,
                letterSpacing = (-0.03).em,
                color = Color(0xFFF5EDDE)
            )
        )
        Text(
            text = "Stop putting it off.",
            style = TextStyle(
                fontFamily = ForgeFonts.spaceGrotesk, fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                lineHeight = (1.45).em,
                color = Color(0xFFB5A990)
            )
        )
        Spacer(modifier = Modifier.height(40.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ForgeTextField(
                emailInput, onValueChange = { value ->
                    emailInput = value
                }, modifier = Modifier,
                label = "Email",
                leadingIcon = Icons.Default.MailOutline
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        ForgePasswordField(
            passwordInput, onValueChange = { value ->
                passwordInput = value
            }, modifier = Modifier,
            label = "Password"
        )
        Spacer(
            modifier = Modifier
                .heightIn(min = 24.dp)
                .weight(1f)
        )
        ForgeButton()
    }
}