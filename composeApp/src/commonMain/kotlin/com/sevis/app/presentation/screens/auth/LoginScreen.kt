package com.sevis.app.presentation.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import com.sevis.app.data.config.Environment
import com.sevis.app.presentation.components.SevisButton
import com.sevis.app.presentation.components.SevisLogo
import com.sevis.app.presentation.components.SevisPasswordField
import com.sevis.app.presentation.components.SevisTextField
import com.sevis.app.presentation.viewmodel.AuthViewModel

private fun validateEmail(email: String): String? = when {
    email.isBlank()                              -> "Email is required"
    !email.contains("@") || !email.contains(".") -> "Enter a valid email address"
    else                                         -> null
}

private fun validatePassword(password: String): String? = when {
    password.isBlank()   -> "Password is required"
    password.length < 8  -> "Password must be at least 8 characters"
    else                 -> null
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Pre-filled defaults for faster login during development
    var email    by remember { mutableStateOf("admin@sevis.com") }
    var password by remember { mutableStateOf("Admin@1234") }

    // Hidden env switcher — tap logo 5 times
    var tapCount     by remember { mutableIntStateOf(0) }
    var showEnvDialog by remember { mutableStateOf(false) }
    var selectedEnv  by remember { mutableStateOf(Environment.current) }

    // Track touched state — show errors only after first edit
    var emailTouched    by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }

    val emailError    = if (emailTouched)    validateEmail(email)    else null
    val passwordError = if (passwordTouched) validatePassword(password) else null
    val formValid     = validateEmail(email) == null && validatePassword(password) == null

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SevisLogo(
                textSize = 32.sp,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null  // no ripple — stays hidden
                ) {
                    tapCount++
                    if (tapCount >= 5) { tapCount = 0; showEnvDialog = true }
                }
            )

            Spacer(Modifier.height(32.dp))

            if (showEnvDialog) {
                AlertDialog(
                    onDismissRequest = { showEnvDialog = false },
                    title = { Text("Environment", fontWeight = FontWeight.Bold) },
                    text  = {
                        Column {
                            Text(
                                text  = "Active: ${Environment.current.baseUrl}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            Environment.Env.entries.forEach { env ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedEnv = env }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedEnv == env,
                                        onClick  = { selectedEnv = env }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(env.label, fontWeight = FontWeight.Medium)
                                        Text(
                                            env.baseUrl,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            Environment.set(selectedEnv)
                            showEnvDialog = false
                        }) { Text("Apply") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEnvDialog = false }) { Text("Cancel") }
                    }
                )
            }

            SevisTextField(
                value         = email,
                onValueChange = { email = it; emailTouched = true },
                label         = "Email",
                keyboardType  = KeyboardType.Email,
                imeAction     = ImeAction.Next,
                isError       = emailError != null,
                errorMessage  = emailError
            )
            Spacer(Modifier.height(12.dp))
            SevisPasswordField(
                value         = password,
                onValueChange = { password = it; passwordTouched = true },
                imeAction     = ImeAction.Done,
                isError       = passwordError != null,
                errorMessage  = passwordError
            )

            Spacer(Modifier.height(16.dp))

            if (state.error != null) {
                Text(
                    text     = state.error!!,
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            SevisButton(
                text    = "Login",
                onClick = {
                    emailTouched    = true
                    passwordTouched = true
                    if (formValid) viewModel.login(email, password, onLoginSuccess)
                },
                isLoading = state.isLoading,
                enabled   = !state.isLoading
            )

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onNavigateToSignup) {
                Text("Don't have an account? Sign up")
            }
        }
    }
}
