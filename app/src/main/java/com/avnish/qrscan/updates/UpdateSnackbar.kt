package com.avnish.qrscan.updates

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UpdateSnackbar(
    isVisible: Boolean,
    isFlexibleUpdateReady: Boolean,
    onInstallNow: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isFlexibleUpdateReady) "Update Ready!" else "Update Available",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = if (isFlexibleUpdateReady) {
                                "Tap to install the update"
                            } else {
                                "Download in progress..."
                            },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isFlexibleUpdateReady) {
                            TextButton(
                                onClick = onInstallNow
                            ) {
                                Text(
                                    text = "Install",
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = onDismiss
                        ) {
                            Text(
                                text = "Dismiss",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

