@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.wisefox.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wisefox.R
import com.example.wisefox.model.TransactionResponse
import com.example.wisefox.navigation.Screen
import com.example.wisefox.ui.theme.*
import com.example.wisefox.viewmodels.TransactionsViewModel
import com.example.wisefox.viewmodels.TxCrudState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── Enums ─────────────────────────────────────────────────────────────────────

enum class TransactionTab { TRANSACTIONS, STATISTICS }

// ── Host ──────────────────────────────────────────────────────────────────────

@Composable
fun TransactionsScreen(
    navController: NavController,
    vm: TransactionsViewModel = viewModel()
) {
    var activeTab by remember { mutableStateOf(TransactionTab.TRANSACTIONS) }
    var isShared  by remember { mutableStateOf(false) }
    var showForm  by remember { mutableStateOf(false) }

    val transactions by vm.transactions.collectAsStateWithLifecycle()
    val isLoading    by vm.isLoading.collectAsStateWithLifecycle()
    val crudState    by vm.crudState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    // Close dialog on success
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
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                TransactionTabBar(activeTab = activeTab, onTabSelected = { activeTab = it })
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SoloSharedSelectorTx(isShared = isShared, onToggle = {
                        isShared = it
                        vm.setShared(it)
                    })
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (activeTab) {
                    TransactionTab.TRANSACTIONS -> TransactionsContent(
                        transactions = transactions,
                        isLoading    = isLoading,
                        ledgerNames  = vm.currentLedgers().associate { it.id to it.name },
                        onTxClick    = { tx ->
                            tx.ledgerId?.let { lid ->
                                navController.navigate(Screen.LedgerDetail.createRoute(lid))
                            }
                        }
                    )
                    TransactionTab.STATISTICS -> StatisticsContent(transactions = transactions)
                }
            }

            if (activeTab == TransactionTab.TRANSACTIONS) {
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
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(36.dp))
                }
            }
        }
    }

    if (showForm) {
        val ledgers = vm.currentLedgers()
        TransactionFormDialog(
            fixedLedger      = null,
            availableLedgers = ledgers,
            isLoading        = crudState is TxCrudState.Loading,
            onConfirm        = { ledgerId, amount, type, category, note ->
                vm.createTransaction(ledgerId, amount, type, category, note)
            },
            onDismiss = { showForm = false }
        )
    }
}

// ── Transactions list content ─────────────────────────────────────────────────

@Composable
private fun TransactionsContent(
    transactions: List<TransactionResponse>,
    isLoading: Boolean,
    ledgerNames: Map<Long, String>,
    onTxClick: (TransactionResponse) -> Unit
) {
    var ledgerFilter   by remember { mutableStateOf("--ALL--") }
    var categoryFilter by remember { mutableStateOf("--ALL--") }
    var fromDate       by remember { mutableStateOf<LocalDate?>(null) }
    var toDate         by remember { mutableStateOf<LocalDate?>(null) }

    val uniqueLedgers   = remember(transactions) { transactions.mapNotNull { it.ledgerName }.distinct() }
    val uniqueCategories = remember(transactions) { transactions.mapNotNull { it.category?.name }.distinct() }

    val filtered = transactions.filter { tx ->
        val matchLedger   = ledgerFilter   == "--ALL--" || tx.ledgerName == ledgerFilter
        val matchCategory = categoryFilter == "--ALL--" || tx.category?.name == categoryFilter
        val txDate = tx.date
        val matchFrom = fromDate == null || (txDate != null && !txDate.isBefore(fromDate))
        val matchTo   = toDate   == null || (txDate != null && !txDate.isAfter(toDate))
        matchLedger && matchCategory && matchFrom && matchTo
    }

    val grouped = filtered.groupBy { tx ->
        val today = LocalDate.now()
        when {
            tx.date == null                    -> "UNKNOWN"
            tx.date == today                   -> "TODAY"
            tx.date == today.minusDays(1)      -> "YESTERDAY"
            else -> tx.date.format(DateTimeFormatter.ofPattern("d MMM yyyy")).uppercase()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Filter card ──────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Ledger filter
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterLabel("LEDGER")
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChipScroll(
                        values  = listOf("--ALL--") + uniqueLedgers,
                        selected = ledgerFilter,
                        onSelect = { ledgerFilter = it }
                    )
                }
                // Category filter
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterLabel("CATEGORY")
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChipScroll(
                        values   = listOf("--ALL--") + uniqueCategories,
                        selected  = categoryFilter,
                        onSelect  = { categoryFilter = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WiseFoxOrange)
            }
        } else if (filtered.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No transactions found", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                grouped.forEach { (dateLabel, txList) ->
                    item {
                        Text(
                            text = dateLabel,
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WiseFoxOrangeDark
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(txList) { tx ->
                        TransactionCard(
                            transaction = tx,
                            showLedger  = true,
                            onClick     = { onTxClick(tx) }
                        )
                    }
                }
            }
        }
    }
}

// ── Statistics content ────────────────────────────────────────────────────────

@Composable
private fun StatisticsContent(transactions: List<TransactionResponse>) {
    val totalExpense = transactions.filter { it.type?.name == "EXPENSE" }.sumOf { it.amount ?: 0.0 }.toFloat()
    val totalIncome  = transactions.filter { it.type?.name == "INCOME"  }.sumOf { it.amount ?: 0.0 }.toFloat()
    val total = totalExpense + totalIncome

    val pieSlices = if (total > 0) listOf(
        PieSlice("Expenses", totalExpense, Color(0xFFE06030)),
        PieSlice("Income",   totalIncome,  Color(0xFF4A9E6A))
    ) else listOf(PieSlice("No data", 1f, Color(0xFFCCCCCC)))

    // Bar chart by category
    val byCategory = transactions
        .filter { it.type?.name == "EXPENSE" }
        .groupBy { it.category?.name ?: "OTHER" }
        .map { (cat, list) -> BarEntry(cat, list.sumOf { it.amount ?: 0.0 }.toFloat()) }
        .sortedByDescending { it.value }
        .take(6)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary totals
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("INCOME", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    Text("+%.2f€".format(totalIncome), fontSize = 18.sp, color = Color(0xFF4A9E6A), fontWeight = FontWeight.Bold)
                }
                Divider(modifier = Modifier.height(40.dp).width(1.dp), color = TextSecondary.copy(alpha = 0.3f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EXPENSES", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    Text("-%.2f€".format(totalExpense), fontSize = 18.sp, color = Color(0xFFE06030), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Pie chart
        StatCard(title = "Overview", subtitle = "Income vs Expenses") {
            PieChart(slices = pieSlices, modifier = Modifier.size(140.dp).align(Alignment.CenterEnd))
        }

        // Bar chart
        if (byCategory.isNotEmpty()) {
            StatCard(title = "Expenses by Category", subtitle = "Top categories") {
                BarChart(
                    entries = byCategory,
                    modifier = Modifier.height(120.dp).fillMaxWidth(0.65f).align(Alignment.CenterEnd)
                )
            }
        }
    }
}

// ── Filter chip row ───────────────────────────────────────────────────────────

@Composable
private fun FilterChipScroll(
    values: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(values) { value ->
            FilterChip(
                value    = value,
                selected = value == selected,
                onClick  = { onSelect(value) }
            )
        }
    }
}

@Composable
private fun FilterChip(value: String, selected: Boolean = false, onClick: () -> Unit = {}) {
    Surface(
        onClick   = onClick,
        shape     = RoundedCornerShape(8.dp),
        color     = if (selected) WiseFoxOrange else WiseFoxOrangePale.copy(alpha = 0.5f),
        contentColor = if (selected) Color.White else WiseFoxOrangeDark
    ) {
        Text(
            text = value,
            fontSize = 12.sp,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun FilterLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary.copy(alpha = 0.8f),
        modifier = Modifier.width(68.dp)
    )
}

// ── Transaction card (shared by both screens) ─────────────────────────────────

private val expenseColor = Color(0xFFE06030)
private val incomeColor  = Color(0xFF4A9E6A)

@Composable
fun TransactionCard(
    transaction: TransactionResponse,
    showLedger: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val isExpense  = transaction.type?.name == "EXPENSE"
    val typeColor  = if (isExpense) expenseColor else incomeColor
    val amountSign = if (isExpense) "-" else "+"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
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
                if (showLedger && transaction.ledgerName != null) {
                    Text(
                        text = transaction.ledgerName,
                        fontSize = 11.sp,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }
                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text = transaction.note,
                        fontSize = 11.sp,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
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
        }
    }
}

// ── Tab bar ───────────────────────────────────────────────────────────────────

@Composable
private fun TransactionTabBar(
    activeTab: TransactionTab,
    onTabSelected: (TransactionTab) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TabPill(text = "Transactions", selected = activeTab == TransactionTab.TRANSACTIONS, modifier = Modifier.weight(1f)) { onTabSelected(TransactionTab.TRANSACTIONS) }
            TabPill(text = "Statistics",   selected = activeTab == TransactionTab.STATISTICS,   modifier = Modifier.weight(1f)) { onTabSelected(TransactionTab.STATISTICS) }
        }
    }
}

@Composable
private fun TabPill(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick  = onClick,
        modifier = modifier,
        shape    = RoundedCornerShape(14.dp),
        color    = if (selected) WiseFoxOrange else Color.Transparent
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) Color.White else TextSecondary.copy(alpha = 0.6f)
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
        )
    }
}

// ── Solo/Shared selector (local copy to avoid import conflict) ────────────────

@Composable
private fun SoloSharedSelectorTx(isShared: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        shape  = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg),
        modifier = Modifier.height(32.dp)
    ) {
        Row(modifier = Modifier.padding(2.dp), verticalAlignment = Alignment.CenterVertically) {
            SelectorOptionTx("SOLO",   !isShared) { onToggle(false) }
            SelectorOptionTx("SHARED", isShared)  { onToggle(true) }
        }
    }
}

@Composable
private fun SelectorOptionTx(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(50),
        color    = if (isSelected) WiseFoxOrange else Color.Transparent,
        modifier = Modifier.fillMaxHeight()
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Black)
        }
    }
}

// ── Chart helpers (keep existing implementations) ────────────────────────────

private data class PieSlice(val label: String, val value: Float, val color: Color)
private data class BarEntry(val label: String, val value: Float)

@Composable
private fun StatCard(title: String, subtitle: String, chart: @Composable BoxScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp).height(170.dp)) {
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
            }
            chart()
        }
    }
}

@Composable
private fun PieChart(slices: List<PieSlice>, modifier: Modifier = Modifier) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    Canvas(modifier = modifier) {
        var startAngle = -90f
        slices.forEach { slice ->
            val sweep = if (total > 0f) (slice.value / total) * 360f else 360f
            drawArc(color = slice.color, startAngle = startAngle, sweepAngle = sweep,
                useCenter = false, topLeft = Offset(size.width * 0.05f, size.height * 0.05f),
                size = Size(size.width * 0.9f, size.height * 0.9f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = size.width * 0.18f))
            startAngle += sweep
        }
    }
}

@Composable
private fun BarChart(entries: List<BarEntry>, modifier: Modifier = Modifier) {
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val barColors = listOf(Color(0xFFE06030), Color(0xFFFFD97A), Color(0xFF4A9E6A),
        Color(0xFF4A90D9), Color(0xFF9B59B6), Color(0xFF1ABC9C))
    val maxValue = entries.maxOfOrNull { it.value } ?: 1f
    val axisColor = TextSecondary.copy(alpha = 0.6f)

    Canvas(modifier = modifier) {
        val chartHeight = size.height * 0.75f
        val barCount = entries.size
        if (barCount == 0) return@Canvas
        val totalWidth = size.width
        val barWidth = totalWidth / (barCount * 1.8f)
        val gap = (totalWidth - barWidth * barCount) / (barCount + 1)

        drawLine(axisColor, Offset(0f, 0f), Offset(0f, chartHeight), 2f)
        entries.forEachIndexed { index, entry ->
            val left = index * (barWidth + gap) + gap / 2f
            val barHeight = (entry.value / maxValue) * chartHeight
            val top = chartHeight - barHeight
            drawRect(color = barColors[index % barColors.size], topLeft = Offset(left, top), size = Size(barWidth, barHeight))
            val measured = textMeasurer.measure(
                AnnotatedString(entry.value.toInt().toString()),
                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = axisColor)
            )
            drawText(measured, topLeft = Offset(left + barWidth / 2f - measured.size.width / 2f, top - measured.size.height - 2f))
        }
    }
}