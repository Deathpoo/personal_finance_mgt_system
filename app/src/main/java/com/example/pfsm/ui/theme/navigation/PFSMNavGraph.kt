package com.example.pfsm.ui.theme.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pfsm.ui.theme.pages.AddBudgetScreen
import com.example.pfsm.ui.theme.pages.AddTransactionScreen
import com.example.pfsm.ui.theme.pages.AppTopBar
import com.example.pfsm.ui.theme.pages.BottomAppBar
import com.example.pfsm.ui.theme.pages.BudgetDetailScreen
import com.example.pfsm.ui.theme.pages.BudgetScreen
import com.example.pfsm.ui.theme.pages.CalendarScreen
import com.example.pfsm.ui.theme.pages.CategoryScreen
import com.example.pfsm.ui.theme.pages.DashboardScreen
import com.example.pfsm.ui.theme.pages.HomeContentPage
import com.example.pfsm.ui.theme.pages.NavigationRailBar
import com.example.pfsm.ui.theme.pages.ProfileScreen
import com.example.pfsm.ui.theme.pages.SignInScreen
import com.example.pfsm.ui.theme.pages.SignUpScreen
import com.example.pfsm.ui.theme.pages.TransactionScreen
import com.example.pfsm.ui.theme.pages.rememberProfileBitmap
import com.example.pfsm.viewModels.AppViewModelProvider
import com.example.pfsm.viewModels.ProfileViewModel
import kotlinx.coroutines.flow.first

private val bottomTabRoutes = listOf(
    Screen.Home.route,
    Screen.Budget.route,
    Screen.Dashboard.route,
    Screen.Transaction.route
)

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun PFSMNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.hierarchy?.firstOrNull()?.route

    val showBars = currentRoute in Screen.bottomNavRoutes
    val selectedTabIndex = bottomTabRoutes.indexOf(currentRoute).let { if (it == -1) 0 else it }

    val profileViewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val profileUiState by profileViewModel.uiState.collectAsState()
    val profileBitmap = rememberProfileBitmap(profileUiState.user?.profilePicUri)

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val useNavigationRail = screenWidthDp >= 600

    fun navigateToTabRoute(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToTab(index: Int) = navigateToTabRoute(bottomTabRoutes[index])

    val context = LocalContext.current
    val container = (context.applicationContext as com.example.pfsm.PFSMApplication).container
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val userId = container.sessionManager.currentUserId.first()
        val isValidSession = userId?.let { container.userRepository.getUser(it).first() } != null
        startDestination = if (isValidSession) Screen.Home.route else Screen.SignIn.route
    }

    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            if (showBars && !useNavigationRail) {
                AppTopBar(
                    profileImage = profileBitmap,
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }
        },
        bottomBar = {
            if (showBars && !useNavigationRail) {
                BottomAppBar(
                    selectedItem = selectedTabIndex,
                    onItemSelected = { index -> navigateToTab(index) }
                )
            }
        },
    ) { scaffoldPadding ->
        Row(modifier =if(useNavigationRail) Modifier.fillMaxSize() else Modifier.padding(scaffoldPadding).fillMaxSize()) {
            if (showBars && useNavigationRail) {
                NavigationRailBar(
                    selectedItem = selectedTabIndex,
                    profileImage = profileBitmap,
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onItemSelected = { index -> navigateToTab(index) }
                )
            }

            NavHost(
                navController = navController,
                startDestination = startDestination!!,
                modifier = Modifier.weight(1f)
            ) {
                composable(Screen.Home.route) {
                    HomeContentPage(
                        onSeeAllTransactions = { navigateToTabRoute(Screen.Transaction.route) },
                        onOpenCalendar = { navController.navigate(Screen.Calendar.route) },
                        onAddTransaction = {  navController.navigate(Screen.AddTransaction.addRoute()) }
                    )
                }

                composable(Screen.Budget.route) {
                    BudgetScreen(
                        onAddBudget = { navController.navigate(Screen.AddBudget.route) },
                        onBudgetClick = { budgetId ->
                            navController.navigate(Screen.BudgetDetail.createRoute(budgetId))
                        }
                    )
                }

                composable(Screen.Dashboard.route) {
                    DashboardScreen()
                }

                composable(Screen.Transaction.route) {
                    TransactionScreen(
                        onTransactionClick = { txnId ->
                            navController.navigate(Screen.AddTransaction.editRoute(txnId))
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        padding = PaddingValues(),
                        profileViewModel = profileViewModel,
                        onManageCategories = { navController.navigate(Screen.Category.route) },
                        onBack = { navController.popBackStack() },
                        onSignedOut = {
                            navController.navigate(Screen.SignIn.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Category.route) {
                    CategoryScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.AddTransaction.route,
                    arguments = listOf(navArgument("transactionId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) {
                    AddTransactionScreen(
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() },
                        onDeleted = { navController.popBackStack() }
                    )
                }

                composable(Screen.Calendar.route) {
                    CalendarScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AddBudget.route) {
                    AddBudgetScreen(
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.BudgetDetail.route,
                    arguments = listOf(navArgument("budgetId") { type = NavType.IntType })
                ) {
                    BudgetDetailScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.SignIn.route) {
                    SignInScreen(
                        onSignedIn = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onGoToSignUp = { navController.navigate(Screen.SignUp.route) }
                    )
                }

                composable(Screen.SignUp.route) {
                    SignUpScreen(
                        onSignedUp = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onGoToSignIn = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}