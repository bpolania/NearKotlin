package com.example.nearkotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearkotlin.data.*
import io.near.jsonrpc.client.NearRpcClient
import io.near.jsonrpc.types.generated.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.math.RoundingMode

class WalletViewModel : ViewModel() {
    private val walletManager = WalletManager()
    private var nearClient = NearRpcClient(NearNetwork.TESTNET.rpcUrl)
    
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()
    
    private val _accounts = MutableStateFlow<List<WalletAccount>>(emptyList())
    val accounts: StateFlow<List<WalletAccount>> = _accounts.asStateFlow()
    
    private val _tokens = MutableStateFlow<List<NEP141Token>>(emptyList())
    val tokens: StateFlow<List<NEP141Token>> = _tokens.asStateFlow()
    
    private val _accountBalances = MutableStateFlow<Map<String, String>>(emptyMap())
    val accountBalances: StateFlow<Map<String, String>> = _accountBalances.asStateFlow()
    
    private val _tokenBalances = MutableStateFlow<Map<String, Map<String, String>>>(emptyMap())
    val tokenBalances: StateFlow<Map<String, Map<String, String>>> = _tokenBalances.asStateFlow()
    
    fun createWallet() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val seedPhrase = walletManager.generateSeedPhrase()
                val keyPair = walletManager.deriveKeyFromSeed(seedPhrase, 0)
                val implicitAccountId = walletManager.getImplicitAccountId(keyPair.publicKey)
                
                val implicitAccount = WalletAccount(
                    accountId = implicitAccountId,
                    publicKey = keyPair.publicKey,
                    privateKey = keyPair.privateKey,
                    isImplicit = true,
                    network = _uiState.value.currentNetwork
                )
                
                _accounts.value = listOf(implicitAccount)
                _uiState.value = _uiState.value.copy(
                    seedPhrase = seedPhrase,
                    isWalletCreated = true,
                    isLoading = false
                )
                
                // Load initial balance
                refreshAccountBalance(implicitAccountId)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create wallet: ${e.message}"
                )
            }
        }
    }
    
    fun importWallet(seedPhrase: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val words = seedPhrase.trim().split(" ")
                if (words.size != 12 && words.size != 24) {
                    throw IllegalArgumentException("Seed phrase must be 12 or 24 words")
                }
                
                val keyPair = walletManager.deriveKeyFromSeed(words, 0)
                val implicitAccountId = walletManager.getImplicitAccountId(keyPair.publicKey)
                
                val implicitAccount = WalletAccount(
                    accountId = implicitAccountId,
                    publicKey = keyPair.publicKey,
                    privateKey = keyPair.privateKey,
                    isImplicit = true,
                    network = _uiState.value.currentNetwork
                )
                
                _accounts.value = listOf(implicitAccount)
                _uiState.value = _uiState.value.copy(
                    seedPhrase = words,
                    isWalletCreated = true,
                    isLoading = false
                )
                
                refreshAccountBalance(implicitAccountId)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to import wallet: ${e.message}"
                )
            }
        }
    }
    
    fun addNamedAccount(accountName: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                if (!walletManager.isValidAccountId(accountName)) {
                    throw IllegalArgumentException("Invalid account ID format")
                }
                
                val accountIndex = _accounts.value.size
                val keyPair = walletManager.deriveKeyFromSeed(_uiState.value.seedPhrase, accountIndex)
                
                val namedAccount = WalletAccount(
                    accountId = if (_uiState.value.currentNetwork == NearNetwork.TESTNET) {
                        "$accountName.testnet"
                    } else {
                        "$accountName.near"
                    },
                    publicKey = keyPair.publicKey,
                    privateKey = keyPair.privateKey,
                    isImplicit = false,
                    network = _uiState.value.currentNetwork
                )
                
                _accounts.value = _accounts.value + namedAccount
                _uiState.value = _uiState.value.copy(isLoading = false)
                
                refreshAccountBalance(namedAccount.accountId)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to add account: ${e.message}"
                )
            }
        }
    }
    
    fun addToken(token: NEP141Token) {
        _tokens.value = _tokens.value + token
        
        // Refresh token balances for all accounts
        _accounts.value.forEach { account ->
            refreshTokenBalance(account.accountId, token.contractId)
        }
    }
    
    fun refreshAccountBalance(accountId: String) {
        viewModelScope.launch {
            try {
                val response = nearClient.viewAccount(
                    accountId = accountId,
                    blockReference = BlockReference(finality = Finality.FINAL)
                )
                
                val balanceInNear = formatNearAmount(response.amount)
                _accountBalances.value = _accountBalances.value + (accountId to balanceInNear)
                
            } catch (e: Exception) {
                // Account might not exist yet (implicit accounts)
                _accountBalances.value = _accountBalances.value + (accountId to "0")
            }
        }
    }
    
    fun refreshTokenBalance(accountId: String, tokenContract: String) {
        viewModelScope.launch {
            try {
                val args = """{"account_id": "$accountId"}"""
                val argsBase64 = android.util.Base64.encodeToString(
                    args.toByteArray(), 
                    android.util.Base64.NO_WRAP
                )
                
                val response = nearClient.callFunction(
                    accountId = tokenContract,
                    methodName = "ft_balance_of",
                    argsBase64 = argsBase64,
                    blockReference = BlockReference(finality = Finality.FINAL)
                )
                
                val resultString = String(
                    android.util.Base64.decode(response.result, android.util.Base64.NO_WRAP)
                )
                val balance = resultString.trim('"')
                
                val currentTokenBalances = _tokenBalances.value[accountId] ?: emptyMap()
                val updatedTokenBalances = currentTokenBalances + (tokenContract to balance)
                
                _tokenBalances.value = _tokenBalances.value + (accountId to updatedTokenBalances)
                
            } catch (e: Exception) {
                // Token might not exist or account has no balance
                val currentTokenBalances = _tokenBalances.value[accountId] ?: emptyMap()
                val updatedTokenBalances = currentTokenBalances + (tokenContract to "0")
                _tokenBalances.value = _tokenBalances.value + (accountId to updatedTokenBalances)
            }
        }
    }
    
    fun switchNetwork(network: NearNetwork) {
        _uiState.value = _uiState.value.copy(currentNetwork = network)
        nearClient = NearRpcClient(network.rpcUrl)
        
        // Refresh all balances on network switch
        _accounts.value.forEach { account ->
            refreshAccountBalance(account.accountId)
            _tokens.value.forEach { token ->
                refreshTokenBalance(account.accountId, token.contractId)
            }
        }
    }
    
    private fun formatNearAmount(yoctoNear: String): String {
        val yocto = BigDecimal(yoctoNear)
        val near = yocto.divide(BigDecimal("1000000000000000000000000"), 4, RoundingMode.DOWN)
        return near.stripTrailingZeros().toPlainString()
    }
}

data class WalletUiState(
    val isWalletCreated: Boolean = false,
    val seedPhrase: List<String> = emptyList(),
    val currentNetwork: NearNetwork = NearNetwork.TESTNET,
    val isLoading: Boolean = false,
    val error: String? = null
)