package com.example.pfsm.ui.theme.design



import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Category
import androidx.compose.ui.graphics.vector.ImageVector


object CategoryIcons {

    val catalog: Map<String, ImageVector> = mapOf(
        "food" to Icons.Default.Fastfood,
        "groceries" to Icons.Default.LocalGroceryStore,
        "transport" to Icons.Default.DirectionsBus,
        "shopping" to Icons.Default.ShoppingBag,
        "home" to Icons.Default.Home,
        "health" to Icons.Default.HealthAndSafety,
        "education" to Icons.Default.School,
        "entertainment" to Icons.Default.MovieFilter,
        "gaming" to Icons.Default.SportsEsports,
        "travel" to Icons.Default.Flight,
        "utilities" to Icons.Default.Bolt,
        "internet" to Icons.Default.Wifi,
        "subscriptions" to Icons.Default.Subscriptions,
        "gift" to Icons.Default.CardGiftcard,
        "salary" to Icons.Default.Work,
        "investment" to Icons.AutoMirrored.Filled.TrendingUp,
        "savings" to Icons.Default.Savings,
        "bonus" to Icons.Default.Redeem,
        "income_other" to Icons.Default.MonetizationOn,
    )

    fun resolve(key: String?): ImageVector =
        catalog[key] ?: Icons.Default.Category


    val incomeKeys = listOf("salary", "investment", "savings", "bonus", "gift", "income_other")


    val expenseKeys = listOf(
        "food", "groceries", "transport", "shopping", "home", "health",
        "education", "entertainment", "gaming", "travel", "utilities",
        "internet", "subscriptions", "gift"
    )
}