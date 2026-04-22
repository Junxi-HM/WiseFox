@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.wisefox.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wisefox.model.LedgerResponse
import com.example.wisefox.model.TransactionResponse
import com.example.wisefox.ui.theme.*
import com.example.wisefox.viewmodels.LedgerDetailViewModel
import com.example.wisefox.viewmodels.TxCrudState

@Composable
fun LedgerDetailScreen(
    navController: NavController,
    ledger: LedgerResponse,
    vm: LedgerDetailViewModel = viewModel(factory = LedgerDetailViewModel.Factory(ledger.id))
) {
    val transactions by vm.transactions.collectAsStateWithLifecycle()
    val isLoading    by vm.isLoading.collectAsStateWithLifecycle()
    val crudState    by vm.crudState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    var showForm      by remember { mutableStateOf(false) }
    var txToDelete    by remember { mutableStateOf<TransactionResponse?>(null) }

    LaunchedEffect(crudState) {
        if (crudState is TxCrudState.Success) {
            showForm = false
            vm.resetCrudState()
        }
        if (crudState is TxCrudState.Error) {
            snackbarHost.showSnackbar((crudState as TxCrudState.Error).message)
            vm.resetCrudState()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // ── Header ─────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WiseFoxOrangeDark)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ledger.name.uppercase(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WiseFoxOrangeDark,
                            letterSpacing = 1.sp
                        )
                        if (!ledger.ownerUsername.isNullOrBlank()) {
                            Text(
                                text = "Owner: ${ledger.ownerUsername}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Summary ────────────────────────────────────────────
                val totalExpense = transactions.filter { it.type?.name == "EXPENSE" }.sumOf { it.amount ?: 0.0 }
                val totalIncome  = transactions.filter { it.type?.name == "INCOME"  }.sumOf { it.amount ?: 0.0 }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryChip(label = "INCOME", value = "+%.2f€".format(totalIncome), color = Color(0xFF4A9E6A), modifier = Modifier.weight(1f))
                    SummaryChip(label = "EXPENSES", value = "-%.2f€".format(totalExpense), color = Color(0xFFE06030), modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Transaction list ───────────────────────────────────
                if (isLoading) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = WiseFoxOrange)
                    }
                } else if (transactions.isEmpty()) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No transactions yet", color = TextSecondary, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(transactions, key = { it.id ?: it.hashCode() }) { tx ->
                            LedgerDetailTransactionCard(
                                transaction = tx,
                                onDelete    = { txToDelete = tx }
                            )
                        }
                    }
                }
            }

            // ── FAB ────────────────────────────────────────────────────
            FloatingActionButton(
                onClick = { showForm = true },
                shape = CircleShape,
                containerColor = WiseFoxOrange,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 10.dp)
                    .size(64.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add transaction", modifier = Modifier.size(36.dp))
            }
        }
    }

    // ── Add form dialog ────────────────────────────────────────────────
    if (showForm) {
        TransactionFormDialog(
            fixedLedger = ledger,
            isLoading   = crudState is TxCrudState.Loading,
            onConfirm   = { _, amount, type, category, note ->
                vm.createTransaction(amount, type, category, note)
            },
            onDismiss = { showForm = false }
        )
    }

    // ── Delete confirmation dialog ─────────────────────────────────────
    txToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("Delete Transaction", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text  = {
                Text(
                    "Delete ${tx.category?.name ?: "this transaction"} of ${"%.2f".format(tx.amount ?: 0.0)}€?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    tx.id?.let { vm.deleteTransaction(it) }
                    txToDelete = null
                }) {
                    Text("Delete", color = Color(0xFFE06030), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = WiseFoxSubCardBg
        )
    }
}

@Composable
private fun SummaryChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun LedgerDetailTransactionCard(
    transaction: TransactionResponse,
    onDelete: () -> Unit
) {
    val isExpense  = transaction.type?.name == "EXPENSE"
    val typeColor  = if (isExpense) Color(0xFFE06030) else Color(0xFF4A9E6A)
    val amountSign = if (isExpense) "-" else "+"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category?.name ?: "OTHER",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (!transaction.note.isNullOrBlank()) {
                    Text(transaction.note, fontSize = 11.sp, color = TextSecondary.copy(alpha = 0.7f))
                }
                transaction.date?.let {
                    Text(it.toString(), fontSize = 11.sp, color = TextSecondary.copy(alpha = 0.5f))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$amountSign${"%.2f".format(transaction.amount ?: 0.0)}€",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                    Text(
                        text = transaction.type?.name ?: "",
                        fontSize = 10.sp,
                        color = typeColor.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE06030).copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}