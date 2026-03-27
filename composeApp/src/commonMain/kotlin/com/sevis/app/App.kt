package com.sevis.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sevis.app.data.auth.TokenManager
import com.sevis.app.presentation.screens.BillingScreen
import com.sevis.app.presentation.screens.InventoryScreen
import com.sevis.app.presentation.screens.OrdersScreen
import com.sevis.app.presentation.screens.UsersScreen
import com.sevis.app.presentation.screens.auth.LoginScreen
import com.sevis.app.presentation.screens.auth.SignupScreen
import com.sevis.app.presentation.viewmodel.AuthViewModel

enum class MainScreen(val label: String) {
    Users("Users"),
    Inventory("Inventory"),
    Orders("Orders"),
    Billing("Billing")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MaterialTheme {
        var isLoggedIn by remember { mutableStateOf(TokenManager.isLoggedIn) }
        var showSignup by remember { mutableStateOf(false) }

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
            val authViewModel: AuthViewModel = viewModel { AuthViewModel() }
            var currentScreen by remember { mutableStateOf(MainScreen.Users) }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(currentScreen.label) },
                        actions = {
                            IconButton(onClick = {
                                authViewModel.logout { isLoggedIn = false }
                            }) {
                                Icon(Icons.Default.Logout, contentDescription = "Logout")
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.Users,
                            onClick = { currentScreen = MainScreen.Users },
                            icon = { Icon(Icons.Default.Person, contentDescription = null) },
                            label = { Text(MainScreen.Users.label) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.Inventory,
                            onClick = { currentScreen = MainScreen.Inventory },
                            icon = { Icon(Icons.Default.List, contentDescription = null) },
                            label = { Text(MainScreen.Inventory.label) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.Orders,
                            onClick = { currentScreen = MainScreen.Orders },
                            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                            label = { Text(MainScreen.Orders.label) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.Billing,
                            onClick = { currentScreen = MainScreen.Billing },
                            icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                            label = { Text(MainScreen.Billing.label) }
                        )
                    }
                }
            ) { innerPadding ->
                when (currentScreen) {
                    MainScreen.Users -> UsersScreen(modifier = Modifier.padding(innerPadding))
                    MainScreen.Inventory -> InventoryScreen(modifier = Modifier.padding(innerPadding))
                    MainScreen.Orders -> OrdersScreen(modifier = Modifier.padding(innerPadding))
                    MainScreen.Billing -> BillingScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
