package com.example.nearkotlin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nearkotlin.data.CommonTokens
import com.example.nearkotlin.data.NEP141Token
import com.example.nearkotlin.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportWalletScreen(
    viewModel: WalletViewModel,
    onWalletImported: () -> Unit,
    onBack: () -> Unit
) {
    var seedPhrase by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Wallet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Enter your seed phrase",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = seedPhrase,
                onValueChange = { seedPhrase = it },
                label = { Text("Seed phrase (12 or 24 words)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    viewModel.importWallet(seedPhrase)
                    if (uiState.error == null) {
                        onWalletImported()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = seedPhrase.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Import Wallet")
                }
            }
            
            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    viewModel: WalletViewModel,
    onAccountAdded: () -> Unit,
    onBack: () -> Unit
) {
    var accountName by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Create a named account",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it.lowercase() },
                label = { Text("Account name") },
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text(".testnet") },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Account name must be 2-64 characters, lowercase letters, digits, - and _",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    viewModel.addNamedAccount(accountName)
                    onAccountAdded()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = accountName.length >= 2
            ) {
                Text("Create Account")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTokenScreen(
    viewModel: WalletViewModel,
    onTokenAdded: () -> Unit,
    onBack: () -> Unit
) {
    var selectedToken by remember { mutableStateOf<NEP141Token?>(null) }
    var customContractId by remember { mutableStateOf("") }
    var customSymbol by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var customDecimals by remember { mutableStateOf("") }
    var isCustom by remember { mutableStateOf(false) }
    
    val commonTokens = listOf(
        CommonTokens.WRAP_NEAR,
        CommonTokens.USDT,
        CommonTokens.USDC,
        CommonTokens.DAI
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Token") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Select a token",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!isCustom) {
                commonTokens.forEach { token ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = { selectedToken = token }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(token.name, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                                Text(token.symbol, style = MaterialTheme.typography.bodySmall)
                            }
                            RadioButton(
                                selected = selectedToken == token,
                                onClick = { selectedToken = token }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = { isCustom = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Custom Token")
                }
            } else {
                OutlinedTextField(
                    value = customContractId,
                    onValueChange = { customContractId = it },
                    label = { Text("Contract ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = customSymbol,
                    onValueChange = { customSymbol = it },
                    label = { Text("Symbol") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = customDecimals,
                    onValueChange = { customDecimals = it },
                    label = { Text("Decimals") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = { isCustom = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Common Tokens")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    val tokenToAdd = if (isCustom) {
                        NEP141Token(
                            contractId = customContractId,
                            symbol = customSymbol,
                            name = customName,
                            decimals = customDecimals.toIntOrNull() ?: 18
                        )
                    } else {
                        selectedToken
                    }
                    
                    tokenToAdd?.let {
                        viewModel.addToken(it)
                        onTokenAdded()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = if (isCustom) {
                    customContractId.isNotEmpty() && customSymbol.isNotEmpty()
                } else {
                    selectedToken != null
                }
            ) {
                Text("Add Token")
            }
        }
    }
}

@Composable
fun SendNearScreen(
    accountId: String,
    viewModel: WalletViewModel,
    onTransactionSent: () -> Unit,
    onBack: () -> Unit
) {
    // TODO: Implement send functionality
    // This requires transaction signing which needs more setup
    BasicPlaceholderScreen(
        title = "Send NEAR",
        message = "Send functionality coming soon.\nFrom: $accountId",
        onBack = onBack
    )
}

@Composable
fun TransactionHistoryScreen(
    accountId: String,
    viewModel: WalletViewModel,
    onBack: () -> Unit
) {
    // TODO: Implement transaction history
    BasicPlaceholderScreen(
        title = "Transaction History",
        message = "Transaction history for:\n$accountId\n\nComing soon...",
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicPlaceholderScreen(
    title: String,
    message: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}