package com.development.forge.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.development.forge.core.designsystem.ForgeFonts

@Preview
@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    var email by rememberSaveable { mutableStateOf("") }
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
        /*TextField spec (email field)
        minHeight 64.dp,
        padding horizontal 18.dp,
        cornerRadius 16.dp
        background #1C180F (bg-raised)
        border 1.5.dp,
        color: #F5EDDE at 18% opacity (default) → #F58A00 (focused) → danger (error)
        leading icon mail,
        size 22.dp,
        color #7A7060 unfocused → #F58A00 focused,
        gap 14.dp from label
        floating label "Email": left 54.dp (because there's a leading icon),
        fontSize animates 16.sp → 11.sp when floated,
        fontWeight 500 → 600,
        letterSpacing 0.06em when floated
        input text: fontSize 16.sp, color #F5EDDE
        focus glow: boxShadow 0 0 0 4px rgba(245,138,0,0.14) + 0 8px 24px rgba(0,0,0,0.35) — in Compose, a 4dp amber ring (border or a second background layer) + elevation shadow
        helper/error text: marginTop 6.dp,
        paddingLeft 18.dp, fontSize 13.sp*/
        TextField(
            value = email,
            onValueChange = { value ->
                email = value
            },
            modifier = Modifier.heightIn(min = 64.dp)
                .padding(horizontal = 18.dp)
        )
    }
}