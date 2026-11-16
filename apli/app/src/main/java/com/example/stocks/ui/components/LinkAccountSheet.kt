package com.example.stocks.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkAccountSheet(
    currentIgn: String = "",
    onDismiss: () -> Unit,
    onLink: (ign: String, apiKey: String) -> Unit,
    onUnlink: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
) {
    var ignText by remember { mutableStateOf(currentIgn) }
    var apiKeyText by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                "Link Minecraft Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Link your Hypixel account to track live purse and bank balances directly in your net worth dashboard.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = ignText,
                onValueChange = { ignText = it },
                label = { Text("Minecraft Username (IGN)") },
                placeholder = { Text("e.g. Technoblade") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = apiKeyText,
                onValueChange = { apiKeyText = it },
                label = { Text("Hypixel API Key (Optional)") },
                placeholder = { Text("Leave blank for public stats") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (ignText.isNotBlank()) {
                        onLink(ignText.trim(), apiKeyText.trim())
                    }
                },
                enabled = ignText.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (currentIgn.isNotBlank()) "Update Account" else "Link Account")
                }
            }

            if (currentIgn.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onUnlink,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Unlink Account")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
