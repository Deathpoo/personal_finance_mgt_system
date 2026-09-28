package com.example.pfsm.ui.theme.navigation

sealed class Screen(val route: String) {
    // Bottom nav tabs
    data object Home : Screen("home")
    data object Budget : Screen("budget")
    data object Dashboard : Screen("dashboard")
    data object Transaction : Screen("transaction")

    // Pushed on top of the bottom-nav tabs (no bottom bar shown)
    data object Profile : Screen("profile")
    data object Category : Screen("category")
    data object AddTransaction : Screen("add_transaction?transactionId={transactionId}") {
        fun addRoute() = "add_transaction"
        fun editRoute(transactionId: Int) = "add_transaction?transactionId=$transactionId"
    }
    data object Calendar : Screen("calendar")
    data object AddBudget : Screen("add_budget")

    data object BudgetDetail : Screen("budget_detail/{budgetId}") {
        fun createRoute(budgetId: Int) = "budget_detail/$budgetId"
    }

    data object SignIn : Screen("sign_in")
    data object SignUp : Screen("sign_up")

    companion object {

        val bottomNavRoutes: List<String> by lazy {
            listOf(Home.route, Budget.route, Dashboard.route, Transaction.route)
        }
    }
}