package com.development.forge.feature.onboarding.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.development.forge.core.components.ForgeButton
import com.development.forge.core.components.ForgePaginationDots
import com.development.forge.core.components.ForgeTopBar
import com.development.forge.core.components.OnForgeOnSkip

@Preview
@Composable
fun OnboardingFlowScreen(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit = { }
) {
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    val buttonLabel by remember {
        derivedStateOf {
            when (currentStep) {
                0 -> "Show me how"
                1 -> "Got it"
                else -> "Forge your first task"
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        when (currentStep) {
            0, 1 -> ForgeTopBar {
                OnForgeOnSkip {
                    onFinish()
                }
            }

            else -> ForgeTopBar { }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentStep) {
                0 -> OnboardingPromiseScreen()
                1 -> OnboardingMechanicScreen()
                2 -> OnboardingDisciplineScreen()
            }
        }

        ForgePaginationDots(
            totalSteps = 3,
            currentStep = currentStep,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 28.dp)
        )
        ForgeButton(
            label = buttonLabel,
            isEnabled = true,
            icon = if (currentStep < 2) Icons.AutoMirrored.Default.ArrowForward else null,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            when (currentStep) {
                0 -> currentStep = 1
                1 -> currentStep = 2
                2 -> {
                    onFinish()
                }
            }
        }
    }
}