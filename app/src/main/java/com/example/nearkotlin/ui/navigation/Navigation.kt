package com.example.nearkotlin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nearkotlin.ui.screens.*
import com.example.nearkotlin.viewmodel.WalletViewModel

@Composable
fun NearWalletNavigation(navController: NavHostController) {
    val walletViewModel: WalletViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(
                onCreateWallet = {
                    walletViewModel.createWallet()
                    navController.navigate("wallet")
                },
                onImportWallet = {
                    navController.navigate("import")
                }
            )
        }
        
        composable("import") {
            ImportWalletScreen(
                viewModel = walletViewModel,
                onWalletImported = {
                    navController.navigate("wallet")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("wallet") {
            WalletScreen(
                viewModel = walletViewModel,
                onAddAccount = {
                    navController.navigate("add_account")
                },
                onAddToken = {
                    navController.navigate("add_token")
                },
                onSendNear = { accountId ->
                    navController.navigate("send/$accountId")
                },
                onViewTransactions = { accountId ->
                    navController.navigate("transactions/$accountId")
                }
            )
        }
        
        composable("add_account") {
            AddAccountScreen(
                viewModel = walletViewModel,
                onAccountAdded = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("add_token") {
            AddTokenScreen(
                viewModel = walletViewModel,
                onTokenAdded = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("send/{accountId}") { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            SendNearScreen(
                accountId = accountId,
                viewModel = walletViewModel,
                onTransactionSent = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("transactions/{accountId}") { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
            TransactionHistoryScreen(
                accountId = accountId,
                viewModel = walletViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}