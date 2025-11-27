package com.avnish.qrscan.updates

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun UpdateDialog(
    isVisible: Boolean,
    isImmediateUpdate: Boolean,
    isFlexibleUpdateReady: Boolean,
    onUpdateNow: () -> Unit,
    onUpdateLater: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isImmediateUpdate) "Update Required" else "Update Available",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isImmediateUpdate) {
                            "A critical update is available. Please update now to continue using the app."
                        } else {
                            "A new version of the app is available. Would you like to update now?"
                        },
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    if (isFlexibleUpdateReady) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Update downloaded and ready to install!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isImmediateUpdate) {
                            OutlinedButton(
                                onClick = onUpdateLater,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Later")
                            }
                        }
                        
                        Button(
                            onClick = onUpdateNow,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isFlexibleUpdateReady) "Install Now" else "Update Now")
                        }
                    }
                }
            }
        }
    }
}

