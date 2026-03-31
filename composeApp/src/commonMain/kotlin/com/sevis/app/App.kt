package com.sevis.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sevis.app.data.auth.TokenManager
import com.sevis.app.presentation.screens.AuditScreen
import com.sevis.app.presentation.screens.BillingScreen
import com.sevis.app.presentation.screens.InventoryScreen
import com.sevis.app.presentation.screens.OrdersScreen
import com.sevis.app.presentation.screens.UsersScreen
import com.sevis.app.presentation.screens.SplashScreen
import com.sevis.app.presentation.screens.auth.LoginScreen
import com.sevis.app.presentation.screens.auth.SignupScreen
import com.sevis.app.presentation.util.FilePicker
import com.sevis.app.presentation.viewmodel.AuthViewModel
import com.sevis.app.presentation.viewmodel.PartsViewModel
import org.koin.compose.viewmodel.koinViewModel

enum class MainScreen(val label: String) {
    Users("Accounts"),
    Inventory("Inventory"),
    Orders("Job Cards"),
    Billing("Billing"),
    Audit("Audit")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MaterialTheme {
        var showSplash  by remember { mutableStateOf(true) }
        var isLoggedIn  by remember { mutableStateOf(TokenManager.isLoggedIn) }
        var showSignup  by remember { mutableStateOf(false) }

        if (showSplash) {
            SplashScreen(onComplete = { showSplash = false })
            return@MaterialTheme
        }

        if (!isLoggedIn) {
            if (showSignup) {
                SignupScreen(
                    onSignupSuccess = { showSignup = false },
                    onNavigateToLogin = { showSignup = false }
                )
            } else {
                LoginScreen(
                    onLoginSuccess = { isLoggedIn = true },
                    onNavigateToSignup = { showSignup = true }
                )
            }
        } else {
            val authViewModel: AuthViewModel = koinViewModel()
            val partsViewModel: PartsViewModel = koinViewModel()
            var currentScreen by remember { mutableStateOf(MainScreen.Users) }
            var showFilePicker by remember { mutableStateOf(false) }

            FilePicker(
                show = showFilePicker,
                onFilePicked = { name, bytes ->
                    showFilePicker = false
                    partsViewModel.importCsv(bytes, name)
                },
                onDismiss = { showFilePicker = false }
            )

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(currentScreen.label) },
                        actions = {
                            if (currentScreen == MainScreen.Inventory && TokenManager.isAdmin) {
                                IconButton(
                                    onClick = { showFilePicker = true },
                                    enabled = !partsViewModel.state.value.isImporting
                                ) {
                                    Icon(Icons.Default.Upload, contentDescription = "Import CSV")
                                }
                            }
                            IconButton(onClick = {
                                authViewModel.logout { isLoggedIn = false }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.Users,
                            onClick = { currentScreen = MainScreen.Users },
                            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                            label = { Text(MainScreen.Users.label) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.Inventory,
                            onClick = { currentScreen = MainScreen.Inventory },
                            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                            label = { Text(MainScreen.Inventory.label) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.Orders,
                            onClick = { currentScreen = MainScreen.Orders },
                            icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) },
                            label = { Text(MainScreen.Orders.label) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.Billing,
                            onClick = { currentScreen = MainScreen.Billing },
                            icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                            label = { Text(MainScreen.Billing.label) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.Audit,
                            onClick = { currentScreen = MainScreen.Audit },
                            icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                            label = { Text(MainScreen.Audit.label) }
                        )
                    }
                }
            ) { innerPadding ->
                when (currentScreen) {
                    MainScreen.Users -> UsersScreen(modifier = Modifier.padding(innerPadding))
                    MainScreen.Inventory -> InventoryScreen(modifier = Modifier.padding(innerPadding))
                    MainScreen.Orders -> OrdersScreen(modifier = Modifier.padding(innerPadding))
                    MainScreen.Billing -> BillingScreen(modifier = Modifier.padding(innerPadding))
                    MainScreen.Audit -> AuditScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
