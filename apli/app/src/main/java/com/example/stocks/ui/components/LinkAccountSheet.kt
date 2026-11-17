package com.example.stocks.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stocks.ui.theme.EmeraldProfit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkAccountSheet(
    currentIgn: String = "",
    onDismiss: () -> Unit,
    onLink: (ign: String, apiKey: String, shiiyuUrl: String) -> Unit,
    onUnlink: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
) {
    var ignText by remember { mutableStateOf(currentIgn) }
    var apiKeyText by remember { mutableStateOf("") }
    var shiiyuUrlText by remember { mutableStateOf("") }
    var apiKeyError by remember { mutableStateOf<String?>(null) }
    var useShiiyu by remember { mutableStateOf(false) }

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

            // Method selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !useShiiyu,
                    onClick = { useShiiyu = false },
                    label = { Text("Hypixel API") }
                )
                FilterChip(
                    selected = useShiiyu,
                    onClick = { useShiiyu = true },
                    label = { Text("Shiiyu (No API Key)") }
                )
            }

            Spacer(Modifier.height(12.dp))

            if (!useShiiyu) {
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
                    onValueChange = { 
                        apiKeyText = it
                        apiKeyError = if (it.isNotBlank() && (it.contains("http") || it.contains("/") || it.contains("shiiyu"))) {
                            "Enter your API key, not a URL"
                        } else null
                    },
                    label = { Text("Hypixel API Key (Required for profile data)") },
                    placeholder = { Text("Paste your API key here") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = apiKeyError != null,
                    supportingText = apiKeyError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    "Required! Get your API key on Hypixel.net using /api new in-game",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            } else {
                OutlinedTextField(
                    value = shiiyuUrlText,
                    onValueChange = { shiiyuUrlText = it },
                    label = { Text("Shiiyu Profile URL") },
                    placeholder = { Text("https://sky.shiiyu.moe/stats/Blazecraft_edu/Papaya") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    "No API key required! Just paste your Shiiyu profile URL",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldProfit,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(Modifier.height(4.dp))
                Text(
                    "Example: https://sky.shiiyu.moe/stats/Blazecraft_edu/Papaya",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (useShiiyu) {
                        if (shiiyuUrlText.isNotBlank()) {
                            onLink("", "", shiiyuUrlText.trim())
                        }
                    } else {
                        if (ignText.isNotBlank() && apiKeyError == null) {
                            onLink(ignText.trim(), apiKeyText.trim(), "")
                        }
                    }
                },
                enabled = (!useShiiyu && ignText.isNotBlank() && !isLoading && apiKeyError == null) || (useShiiyu && shiiyuUrlText.isNotBlank() && !isLoading),
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
