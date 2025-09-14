package com.example.nearkotlin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "NEAR Wallet",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your gateway to the NEAR ecosystem",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onCreateWallet,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Create New Wallet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onImportWallet,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Import Existing Wallet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Features:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            FeatureItem("• Secure key generation with seed phrase")
            FeatureItem("• Multiple account management")
            FeatureItem("• NEP-141 token support")
            FeatureItem("• Send and receive NEAR")
            FeatureItem("• Transaction history")
            FeatureItem("• Testnet and Mainnet support")
        }
    }
}

@Composable
fun FeatureItem(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}