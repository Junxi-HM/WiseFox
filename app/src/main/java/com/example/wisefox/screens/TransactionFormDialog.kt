package com.example.wisefox.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.wisefox.model.LedgerResponse
import com.example.wisefox.ui.theme.*

// Categories that match the backend enum
val TRANSACTION_CATEGORIES = listOf(
    "FOOD", "TRANSPORT", "RENT", "ENTERTAINMENT", "HEALTH", "SHOPPING", "SALARY", "OTHER"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormDialog(
    // If ledger is fixed (LedgerDetail), pass it. If null, show a ledger selector.
    fixedLedger: LedgerResponse? = null,
    availableLedgers: List<LedgerResponse> = emptyList(),
    isLoading: Boolean = false,
    onConfirm: (ledgerId: Long, amount: Double, type: String, category: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType     by remember { mutableStateOf("EXPENSE") }
    var selectedCategory by remember { mutableStateOf("OTHER") }
    var amountText       by remember { mutableStateOf("") }
    var noteText         by remember { mutableStateOf("") }
    var selectedLedger   by remember { mutableStateOf(fixedLedger ?: availableLedgers.firstOrNull()) }
    var ledgerExpanded   by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var amountError      by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Title ─────────────────────────────────────────────
                    Text(
                        text = "NEW TRANSACTION",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WiseFoxOrangeDark,
                        letterSpacing = 1.sp
                    )

                    // ── Ledger selector (only when not fixed) ─────────────
                    if (fixedLedger == null && availableLedgers.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = ledgerExpanded,
                            onExpandedChange = { ledgerExpanded = !ledgerExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedLedger?.name ?: "Select ledger",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Ledger", color = TextPrimary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ledgerExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = dialogTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = ledgerExpanded,
                                onDismissRequest = { ledgerExpanded = false },
                                modifier = Modifier.background(WiseFoxSubCardBg)
                            ) {
                                availableLedgers.forEach { ledger ->
                                    DropdownMenuItem(
                                        text = { Text(ledger.name, color = Color(0xFF1A1A1A)) },
                                        onClick = { selectedLedger = ledger; ledgerExpanded = false }
                                    )
                                }
                            }
                        }
                    } else if (fixedLedger != null) {
                        Text(
                            text = "Ledger: ${fixedLedger.name}",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // ── Type toggle: EXPENSE / INCOME ─────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("EXPENSE", "INCOME").forEach { typeOpt ->
                            val isSelected = selectedType == typeOpt
                            val bgColor = when {
                                isSelected && typeOpt == "EXPENSE" -> Color(0xFFE06030)
                                isSelected && typeOpt == "INCOME"  -> Color(0xFF4A9E6A)
                                else -> Color(0xFFE0D8CC)
                            }
                            Button(
                                onClick = { selectedType = typeOpt },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = bgColor)
                            ) {
                                Text(
                                    text = typeOpt,
                                    color = if (isSelected) Color.White else Color(0xFF555555),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // ── Category dropdown ─────────────────────────────────
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category", color = TextPrimary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = dialogTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier.background(WiseFoxSubCardBg)
                        ) {
                            TRANSACTION_CATEGORIES.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = Color(0xFF1A1A1A)) },
                                    onClick = { selectedCategory = cat; categoryExpanded = false }
                                )
                            }
                        }
                    }

                    // ── Amount field ──────────────────────────────────────
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it; amountError = false },
                        label = { Text("Amount", color = TextPrimary) },
                        isError = amountError,
                        supportingText = if (amountError) {
                            { Text("Enter a valid amount", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = dialogTextFieldColors()
                    )

                    // ── Note field ────────────────────────────────────────
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Note (optional)", color = TextPrimary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = dialogTextFieldColors()
                    )

                    // ── Buttons ───────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = TextPrimary)
                        }
                        Button(
                            onClick = {
                                val amount = amountText.toDoubleOrNull()
                                if (amount == null || amount <= 0) { amountError = true; return@Button }
                                val ledgerId = (fixedLedger ?: selectedLedger)?.id
                                if (ledgerId == null) return@Button
                                onConfirm(ledgerId, amount, selectedType, selectedCategory, noteText.trim())
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WiseFoxOrange)
                        ) {
                            if (isLoading) CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            else Text("Add", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = WiseFoxOrange,
    unfocusedBorderColor = WiseFoxOrangeDark.copy(alpha = 0.4f),
    focusedLabelColor    = WiseFoxOrange,
    unfocusedLabelColor  = TextPrimary.copy(alpha = 0.7f),
    focusedTextColor     = Color(0xFF1A1A1A),
    unfocusedTextColor   = Color(0xFF1A1A1A),
    cursorColor          = WiseFoxOrange
)