package com.sevis.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Receipt
import com.sevis.app.presentation.screens.BillingScreen
import com.sevis.app.presentation.screens.InventoryScreen
import com.sevis.app.presentation.screens.OrdersScreen
import com.sevis.app.presentation.screens.UsersScreen

enum class Screen(val label: String) {
    Users("Users"),
    Inventory("Inventory"),
    Orders("Orders"),
    Billing("Billing")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf(Screen.Users) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentScreen == Screen.Users,
                        onClick = { currentScreen = Screen.Users },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text(Screen.Users.label) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Inventory,
                        onClick = { currentScreen = Screen.Inventory },
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text(Screen.Inventory.label) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Orders,
                        onClick = { currentScreen = Screen.Orders },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                        label = { Text(Screen.Orders.label) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Billing,
                        onClick = { currentScreen = Screen.Billing },
                        icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                        label = { Text(Screen.Billing.label) }
                    )
                }
            }
        ) { innerPadding ->
            when (currentScreen) {
                Screen.Users -> UsersScreen()
                Screen.Inventory -> InventoryScreen()
                Screen.Orders -> OrdersScreen()
                Screen.Billing -> BillingScreen()
            }
        }
    }
}
