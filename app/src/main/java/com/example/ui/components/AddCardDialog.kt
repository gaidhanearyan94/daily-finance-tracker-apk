package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CardAccountEntity
import com.example.ui.theme.FinFlowPillDark
import com.example.ui.theme.FinFlowTextPrimary

@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    onSave: (CardAccountEntity) -> Unit
) {
    var cardName by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var limitText by remember { mutableStateOf("") }
    var last4Text by remember { mutableStateOf("") }
    var expiryText by remember { mutableStateOf("12/28") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_card_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Card / Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FinFlowTextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = { Text("Account Name") },
                    placeholder = { Text("e.g. Universal, Sapphire, Chase") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FinFlowPillDark,
                        unfocusedBorderColor = Color(0xFFD6DEC9)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = it },
                        label = { Text("Balance ($)") },
                        placeholder = { Text("1000.00") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FinFlowPillDark,
                            unfocusedBorderColor = Color(0xFFD6DEC9)
                        )
                    )
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { limitText = it },
                        label = { Text("Limit ($)") },
                        placeholder = { Text("5000.00") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FinFlowPillDark,
                            unfocusedBorderColor = Color(0xFFD6DEC9)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = last4Text,
                        onValueChange = { if (it.length <= 4) last4Text = it },
                        label = { Text("Last 4 digits") },
                        placeholder = { Text("1234") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FinFlowPillDark,
                            unfocusedBorderColor = Color(0xFFD6DEC9)
                        )
                    )
                    OutlinedTextField(
                        value = expiryText,
                        onValueChange = { expiryText = it },
                        label = { Text("Expiry (MM/YY)") },
                        placeholder = { Text("06/28") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FinFlowPillDark,
                            unfocusedBorderColor = Color(0xFFD6DEC9)
                        )
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val bal = balanceText.toDoubleOrNull() ?: 0.0
                        val lim = limitText.toDoubleOrNull() ?: 10000.0
                        if (cardName.isBlank()) {
                            errorMessage = "Please enter an account name."
                            return@Button
                        }
                        val last4 = if (last4Text.isNotBlank()) "*$last4Text" else "*9999"
                        onSave(
                            CardAccountEntity(
                                name = cardName.trim(),
                                cardNumberMasked = last4,
                                expiry = if (expiryText.isNotBlank()) expiryText else "12/28",
                                balance = bal,
                                creditLimit = lim,
                                creditUsed = 0.0,
                                currency = "USD",
                                cardType = "VISA",
                                isPrimary = false
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_card_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FinFlowPillDark,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Save Card", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
