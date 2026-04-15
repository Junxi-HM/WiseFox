package com.example.wisefox.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.wisefox.model.TransactionResponse
import com.example.wisefox.repository.LedgerRepository
import com.example.wisefox.ui.theme.*
import com.example.wisefox.viewmodels.LedgerDetailUiState
import com.example.wisefox.viewmodels.LedgerDetailViewModel
import com.example.wisefox.viewmodels.LedgerDetailViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerDetailScreen(
    ledgerId: Long,
    ledgerName: String,
    navController: NavController
) {
    val viewModel: LedgerDetailViewModel = viewModel(
        factory = LedgerDetailViewModelFactory(ledgerId)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {

        // ── Header ────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WiseFoxOrangeDark)
            }
            Text(
                text = ledgerName,
                fontSize = 22.sp,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = WiseFoxOrangeDark
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (val state = uiState) {
            is LedgerDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = WiseFoxOrange)
                }
            }
            is LedgerDetailUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = Color.Red)
                }
            }
            is LedgerDetailUiState.Success -> {
                if (state.transactions.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No transactions yet", color = TextSecondary)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.transactions) { tx ->
                            TransactionCard(tx)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(tx: TransactionResponse) {
    val isIncome = tx.type.uppercase() == "INCOME"
    val amountColor = if (isIncome) Color(0xFF4A9E6A) else Color(0xFFE06030)
    val prefix = if (isIncome) "+" else "-"
    val amountText = tx.amount?.let { "$prefix${it.toInt()}€" } ?: "—"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = tx.category ?: tx.type,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 15.sp
                )
                Text(text = tx.date, fontSize = 12.sp, color = TextSecondary)
                tx.note?.let {
                    Text(text = it, fontSize = 11.sp, color = TextSecondary)
                }
            }
            Text(
                text = amountText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}