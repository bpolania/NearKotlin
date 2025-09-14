package com.example.nearkotlin.data

import android.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDKeyDerivation
import java.security.MessageDigest

/**
 * Manages NEAR wallet functionality including key generation and account management
 */
class WalletManager {
    
    /**
     * Generates a new seed phrase (12 words)
     */
    fun generateSeedPhrase(): List<String> {
        val entropy = ByteArray(16) // 128 bits for 12 words
        SecureRandom().nextBytes(entropy)
        return MnemonicCode.INSTANCE.toMnemonic(entropy)
    }
    
    /**
     * Derives a NEAR key pair from seed phrase
     * Uses BIP44 path: m/44'/397'/0' (397 is NEAR's coin type)
     */
    fun deriveKeyFromSeed(seedPhrase: List<String>, accountIndex: Int = 0): NearKeyPair {
        val seed = MnemonicCode.toSeed(seedPhrase, "")
        val masterKey = HDKeyDerivation.createMasterPrivateKey(seed)
        
        // BIP44 path for NEAR: m/44'/397'/0'/0'/accountIndex'
        val purpose = HDKeyDerivation.deriveChildKey(masterKey, 44 or HDKeyDerivation.HARDENED_BIT)
        val coinType = HDKeyDerivation.deriveChildKey(purpose, 397 or HDKeyDerivation.HARDENED_BIT)
        val account = HDKeyDerivation.deriveChildKey(coinType, 0 or HDKeyDerivation.HARDENED_BIT)
        val change = HDKeyDerivation.deriveChildKey(account, 0 or HDKeyDerivation.HARDENED_BIT)
        val addressKey = HDKeyDerivation.deriveChildKey(change, accountIndex)
        
        val privateKey = addressKey.privKeyBytes
        val publicKey = addressKey.pubKey
        
        return NearKeyPair(
            privateKey = Base64.encodeToString(privateKey, Base64.NO_WRAP),
            publicKey = Base64.encodeToString(publicKey, Base64.NO_WRAP)
        )
    }
    
    /**
     * Creates implicit account ID from public key
     * In NEAR, implicit accounts are hex(publicKey)
     */
    fun getImplicitAccountId(publicKey: String): String {
        val publicKeyBytes = Base64.decode(publicKey, Base64.NO_WRAP)
        return publicKeyBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Validates a NEAR account ID
     */
    fun isValidAccountId(accountId: String): Boolean {
        // NEAR account ID rules:
        // - 2-64 characters
        // - Lowercase letters, digits, - and _
        // - Cannot start/end with - or _
        // - No consecutive - or _
        val regex = "^(?=.{2,64}$)(?!.*\\.\\.)[a-z0-9]+([._\\-][a-z0-9]+)*$".toRegex()
        return regex.matches(accountId)
    }
}

@Serializable
data class NearKeyPair(
    val privateKey: String,
    val publicKey: String
)

@Serializable
data class WalletAccount(
    val accountId: String,
    val publicKey: String,
    val privateKey: String,
    val isImplicit: Boolean = false,
    val network: NearNetwork = NearNetwork.TESTNET
)

@Serializable
enum class NearNetwork(val rpcUrl: String) {
    MAINNET("https://rpc.mainnet.near.org"),
    TESTNET("https://rpc.testnet.near.org")
}

@Serializable
data class NEP141Token(
    val contractId: String,
    val symbol: String,
    val name: String,
    val decimals: Int,
    val icon: String? = null
)

// Common NEP-141 tokens
object CommonTokens {
    val WRAP_NEAR = NEP141Token(
        contractId = "wrap.near",
        symbol = "wNEAR",
        name = "Wrapped NEAR",
        decimals = 24
    )
    
    val USDT = NEP141Token(
        contractId = "usdt.tether-token.near",
        symbol = "USDT",
        name = "Tether USD",
        decimals = 6
    )
    
    val USDC = NEP141Token(
        contractId = "17208628f84f5d6ad33f0da3bbbeb27ffcb398eac501a31bd6ad2011e36133a1",
        symbol = "USDC",
        name = "USD Coin",
        decimals = 6
    )
    
    val DAI = NEP141Token(
        contractId = "6b175474e89094c44da98b954eedeac495271d0f.factory.bridge.near",
        symbol = "DAI",
        name = "Dai Stablecoin",
        decimals = 18
    )
}