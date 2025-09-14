package com.example.nearkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearkotlin.data.NearNetwork
import com.example.nearkotlin.data.WalletAccount
import com.example.nearkotlin.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel,
    onAddAccount: () -> Unit,
    onAddToken: () -> Unit,
    onSendNear: (String) -> Unit,
    onViewTransactions: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val tokens by viewModel.tokens.collectAsState()
    val accountBalances by viewModel.accountBalances.collectAsState()
    val tokenBalances by viewModel.tokenBalances.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "NEAR Wallet",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Network switcher
                    AssistChip(
                        onClick = {
                            val newNetwork = if (uiState.currentNetwork == NearNetwork.TESTNET) {
                                NearNetwork.MAINNET
                            } else {
                                NearNetwork.TESTNET
                            }
                            viewModel.switchNetwork(newNetwork)
                        },
                        label = { Text(uiState.currentNetwork.name) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Public,
                                contentDescription = "Network",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    when (selectedTab) {
                        0 -> onAddAccount()
                        1 -> onAddToken()
                    }
                },
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { 
                    Text(
                        if (selectedTab == 0) "Add Account" else "Add Token"
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Accounts") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Tokens") }
                )
            }
            
            // Content
            when (selectedTab) {
                0 -> AccountsTab(
                    accounts = accounts,
                    balances = accountBalances,
                    onSendNear = onSendNear,
                    onViewTransactions = onViewTransactions,
                    onRefresh = { accountId ->
                        viewModel.refreshAccountBalance(accountId)
                    }
                )
                1 -> TokensTab(
                    accounts = accounts,
                    tokens = tokens,
                    tokenBalances = tokenBalances,
                    onRefresh = { accountId, tokenContract ->
                        viewModel.refreshTokenBalance(accountId, tokenContract)
                    }
                )
            }
        }
    }
}

@Composable
fun AccountsTab(
    accounts: List<WalletAccount>,
    balances: Map<String, String>,
    onSendNear: (String) -> Unit,
    onViewTransactions: (String) -> Unit,
    onRefresh: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(accounts) { account ->
            AccountCard(
                account = account,
                balance = balances[account.accountId] ?: "0",
                onSendNear = { onSendNear(account.accountId) },
                onViewTransactions = { onViewTransactions(account.accountId) },
                onRefresh = { onRefresh(account.accountId) }
            )
        }
        
        if (accounts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No accounts yet.\nTap + to add an account.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun AccountCard(
    account: WalletAccount,
    balance: String,
    onSendNear: () -> Unit,
    onViewTransactions: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (account.isImplicit) {
                            "${account.accountId.take(8)}...${account.accountId.takeLast(8)}"
                        } else {
                            account.accountId
                        },
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    
                    if (account.isImplicit) {
                        Text(
                            text = "Implicit Account",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "$balance NEAR",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSendNear,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send")
                }
                
                OutlinedButton(
                    onClick = onViewTransactions,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("History")
                }
            }
        }
    }
}

@Composable
fun TokensTab(
    accounts: List<WalletAccount>,
    tokens: List<com.example.nearkotlin.data.NEP141Token>,
    tokenBalances: Map<String, Map<String, String>>,
    onRefresh: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (tokens.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tokens added yet.\nTap + to add a token.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(tokens) { token ->
                TokenCard(
                    token = token,
                    accounts = accounts,
                    balances = tokenBalances,
                    onRefresh = onRefresh
                )
            }
        }
    }
}

@Composable
fun TokenCard(
    token: com.example.nearkotlin.data.NEP141Token,
    accounts: List<WalletAccount>,
    balances: Map<String, Map<String, String>>,
    onRefresh: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = token.symbol.take(2),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = token.name,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        text = token.symbol,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            accounts.forEach { account ->
                val balance = balances[account.accountId]?.get(token.contractId) ?: "0"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (account.isImplicit) {
                            "${account.accountId.take(6)}...${account.accountId.takeLast(4)}"
                        } else {
                            account.accountId
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatTokenBalance(balance, token.decimals),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        IconButton(
                            onClick = { onRefresh(account.accountId, token.contractId) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatTokenBalance(balance: String, decimals: Int): String {
    return try {
        val value = balance.toBigDecimal()
        val divisor = java.math.BigDecimal.TEN.pow(decimals)
        val formatted = value.divide(divisor, 4, java.math.RoundingMode.DOWN)
        formatted.stripTrailingZeros().toPlainString()
    } catch (e: Exception) {
        "0"
    }
}