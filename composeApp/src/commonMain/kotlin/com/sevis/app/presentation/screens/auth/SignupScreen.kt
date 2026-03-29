package com.sevis.app.presentation.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import com.sevis.app.presentation.components.SevisButton
import com.sevis.app.presentation.components.SevisDropdownField
import com.sevis.app.presentation.components.SevisLogo
import com.sevis.app.presentation.components.SevisPasswordField
import com.sevis.app.presentation.components.SevisTextField
import com.sevis.app.presentation.viewmodel.AuthViewModel

private val roles        = listOf("DEALER", "CUSTOMER")
private val accountTypes = listOf("INDIVIDUAL", "COMPANY")

private fun validateGstNo(v: String): String? = when {
    v.isBlank() -> "GST number is required for dealers"
    !v.matches(Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]\$")) ->
        "Invalid format (e.g. 22AAAAA0000A1Z5)"
    else -> null
}

private fun validatePinCode(v: String): String? = when {
    v.isBlank() -> "Pincode is required"
    !v.matches(Regex("^[1-9][0-9]{5}\$")) -> "Enter a valid 6-digit pincode"
    else -> null
}

private fun validateName(v: String): String? = when {
    v.isBlank()  -> "Full name is required"
    v.length < 2 -> "Name must be at least 2 characters"
    else         -> null
}

private fun validateEmail(v: String): String? = when {
    v.isBlank()                             -> "Email is required"
    !v.contains("@") || !v.contains(".")   -> "Enter a valid email address"
    else                                    -> null
}

private fun validatePhone(v: String): String? {
    val digits = v.filter { it.isDigit() }
    return when {
        v.isBlank()       -> "Phone number is required"
        digits.length < 10 -> "Enter a valid 10-digit phone number"
        else              -> null
    }
}

private fun validatePassword(v: String): String? = when {
    v.isBlank()                            -> "Password is required"
    v.length < 8                           -> "Minimum 8 characters"
    !v.any { it.isUpperCase() }            -> "Must contain at least one uppercase letter"
    !v.any { it.isDigit() }               -> "Must contain at least one number"
    else                                   -> null
}

private fun validateCompanyName(v: String, accountType: String): String? = when {
    accountType == "COMPANY" && v.isBlank() -> "Company name is required"
    else                                    -> null
}

@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Pre-filled defaults for faster signup during development
    var name        by remember { mutableStateOf("Admin") }
    var email       by remember { mutableStateOf("admin@sevis.com") }
    var phone       by remember { mutableStateOf("9876543210") }
    var password    by remember { mutableStateOf("Admin@1234") }
    var role        by remember { mutableStateOf(roles.first()) }
    var accountType by remember { mutableStateOf(accountTypes.first()) }
    var companyName by remember { mutableStateOf("") }
    var gstNo        by remember { mutableStateOf("") }
    var address      by remember { mutableStateOf("") }
    var city         by remember { mutableStateOf("") }
    var stateField   by remember { mutableStateOf("") }
    var pinCode      by remember { mutableStateOf("") }
    var dealerCode   by remember { mutableStateOf("") }

    // Touched flags — validate only after first edit
    var nameTouched        by remember { mutableStateOf(false) }
    var emailTouched       by remember { mutableStateOf(false) }
    var phoneTouched       by remember { mutableStateOf(false) }
    var passwordTouched    by remember { mutableStateOf(false) }
    var companyNameTouched by remember { mutableStateOf(false) }
    var gstNoTouched       by remember { mutableStateOf(false) }
    var pinCodeTouched     by remember { mutableStateOf(false) }

    val nameError        = if (nameTouched)        validateName(name)                            else null
    val emailError       = if (emailTouched)       validateEmail(email)                          else null
    val phoneError       = if (phoneTouched)       validatePhone(phone)                          else null
    val passwordError    = if (passwordTouched)    validatePassword(password)                    else null
    val companyNameError = if (companyNameTouched) validateCompanyName(companyName, accountType) else null
    val gstNoError       = if (gstNoTouched && role == "DEALER") validateGstNo(gstNo)            else null
    val pinCodeError     = if (pinCodeTouched && role == "DEALER" && pinCode.isNotBlank()) validatePinCode(pinCode) else null

    val formValid = validateName(name) == null &&
            validateEmail(email) == null &&
            validatePhone(phone) == null &&
            validatePassword(password) == null &&
            validateCompanyName(companyName, accountType) == null &&
            (role != "DEALER" || validateGstNo(gstNo) == null) &&
            (role != "DEALER" || pinCode.isBlank() || validatePinCode(pinCode) == null)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SevisLogo(textSize = 32.sp)

            Spacer(Modifier.height(24.dp))

            SevisTextField(
                value         = name,
                onValueChange = { name = it; nameTouched = true },
                label         = "Full Name",
                isError       = nameError != null,
                errorMessage  = nameError
            )
            Spacer(Modifier.height(12.dp))
            SevisTextField(
                value         = email,
                onValueChange = { email = it; emailTouched = true },
                label         = "Email",
                keyboardType  = KeyboardType.Email,
                isError       = emailError != null,
                errorMessage  = emailError
            )
            Spacer(Modifier.height(12.dp))
            SevisTextField(
                value         = phone,
                onValueChange = { phone = it; phoneTouched = true },
                label         = "Phone Number",
                keyboardType  = KeyboardType.Phone,
                isError       = phoneError != null,
                errorMessage  = phoneError
            )
            Spacer(Modifier.height(12.dp))
            SevisPasswordField(
                value         = password,
                onValueChange = { password = it; passwordTouched = true },
                imeAction     = ImeAction.Next,
                isError       = passwordError != null,
                errorMessage  = passwordError
            )
            Spacer(Modifier.height(12.dp))
            SevisDropdownField(
                value         = role,
                onValueChange = { role = it },
                label         = "Role",
                options       = roles
            )
            Spacer(Modifier.height(12.dp))
            SevisDropdownField(
                value         = accountType,
                onValueChange = { accountType = it; companyName = ""; companyNameTouched = false },
                label         = "Account Type",
                options       = accountTypes
            )
            if (accountType == "COMPANY") {
                Spacer(Modifier.height(12.dp))
                SevisTextField(
                    value         = companyName,
                    onValueChange = { companyName = it; companyNameTouched = true },
                    label         = "Company Name",
                    isError       = companyNameError != null,
                    errorMessage  = companyNameError
                )
            }

            if (role == "DEALER") {
                Spacer(Modifier.height(12.dp))
                SevisTextField(
                    value         = gstNo,
                    onValueChange = { gstNo = it.uppercase(); gstNoTouched = true },
                    label         = "GST Number *",
                    isError       = gstNoError != null,
                    errorMessage  = gstNoError
                )
                Spacer(Modifier.height(12.dp))
                SevisTextField(
                    value         = address,
                    onValueChange = { address = it },
                    label         = "Address"
                )
                Spacer(Modifier.height(12.dp))
                SevisTextField(
                    value         = city,
                    onValueChange = { city = it },
                    label         = "City"
                )
                Spacer(Modifier.height(12.dp))
                SevisTextField(
                    value         = stateField,
                    onValueChange = { stateField = it },
                    label         = "State"
                )
                Spacer(Modifier.height(12.dp))
                SevisTextField(
                    value         = pinCode,
                    onValueChange = { pinCode = it; pinCodeTouched = true },
                    label         = "Pincode",
                    keyboardType  = KeyboardType.Number,
                    isError       = pinCodeError != null,
                    errorMessage  = pinCodeError
                )
                Spacer(Modifier.height(12.dp))
                SevisTextField(
                    value         = dealerCode,
                    onValueChange = { dealerCode = it },
                    label         = "Dealer Code (optional)"
                )
            }

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
                text    = "Sign Up",
                onClick = {
                    // Mark all fields as touched to show any remaining errors
                    nameTouched = true; emailTouched = true
                    phoneTouched = true; passwordTouched = true
                    if (accountType == "COMPANY") companyNameTouched = true
                    if (role == "DEALER") { gstNoTouched = true; pinCodeTouched = true }
                    if (formValid) viewModel.signup(
                        name        = name,
                        email       = email,
                        phone       = phone,
                        password    = password,
                        role        = role,
                        accountType = accountType,
                        companyName = companyName.ifBlank { null },
                        gstNo       = if (role == "DEALER") gstNo.ifBlank { null } else null,
                        address     = if (role == "DEALER") address.ifBlank { null } else null,
                        city        = if (role == "DEALER") city.ifBlank { null } else null,
                        state       = if (role == "DEALER") stateField.ifBlank { null } else null,
                        pinCode     = if (role == "DEALER") pinCode.ifBlank { null } else null,
                        dealerCode  = if (role == "DEALER") dealerCode.ifBlank { null } else null,
                        onSuccess   = onSignupSuccess
                    )
                },
                isLoading = state.isLoading,
                enabled   = !state.isLoading
            )

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Login")
            }
        }
    }
}
