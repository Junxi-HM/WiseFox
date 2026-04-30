@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.wisefox.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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

// ── Statistics content ────────────────────────────────────────────────────────

@Composable
private fun StatisticsContent(transactions: List<TransactionResponse>) {

    // ── Aggregations ──────────────────────────────────────────────────────────
    val expenses = transactions.filter { it.type?.name == "EXPENSE" }
    val incomes  = transactions.filter { it.type?.name == "INCOME" }

    val totalExpense = expenses.sumOf { it.amount ?: 0.0 }
    val totalIncome  = incomes .sumOf { it.amount ?: 0.0 }
    val net          = totalIncome - totalExpense

    // By category (expenses only, sorted desc)
    val byCategory = expenses
        .groupBy { it.category?.name ?: "OTHER" }
        .map { (cat, list) -> cat to list.sumOf { it.amount ?: 0.0 } }
        .sortedByDescending { it.second }

    // By date – last 7 days income vs expense
    val today = LocalDate.now()
    val last7 = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val dailyExpense = last7.map { day ->
        expenses.filter { it.date == day }.sumOf { it.amount ?: 0.0 }.toFloat()
    }
    val dailyIncome = last7.map { day ->
        incomes.filter { it.date == day }.sumOf { it.amount ?: 0.0 }.toFloat()
    }
    val dayLabels = last7.map { it.dayOfMonth.toString() }

    // Top-3 expense categories for the highlight row
    val top3 = byCategory.take(3)

    val categoryColors = listOf(
        Color(0xFFE06030), Color(0xFFFFD97A), Color(0xFF4A9E6A),
        Color(0xFF4A90D9), Color(0xFF9B59B6), Color(0xFF1ABC9C),
        Color(0xFFE91E8C), Color(0xFF607D8B)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ── 1. Summary totals ──────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryBlock("INCOME",   "+%.2f€".format(totalIncome),  Color(0xFF4A9E6A))
                VerticalDividerLine()
                SummaryBlock("EXPENSES", "-%.2f€".format(totalExpense), Color(0xFFE06030))
                VerticalDividerLine()
                SummaryBlock(
                    label      = "NET",
                    value      = "%+.2f€".format(net),
                    valueColor = if (net >= 0) Color(0xFF4A9E6A) else Color(0xFFE06030)
                )
            }
        }

        // ── 2. Pie chart – Income vs Expenses ─────────────────────────────
        if (totalExpense > 0 || totalIncome > 0) {
            val pieSlices = buildList {
                if (totalExpense > 0) add(PieSlice("Expenses", totalExpense.toFloat(), Color(0xFFE06030)))
                if (totalIncome  > 0) add(PieSlice("Income",   totalIncome .toFloat(), Color(0xFF4A9E6A)))
            }
            StatSectionCard(title = "Overview", subtitle = "Income vs Expenses") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PieChartFull(slices = pieSlices, modifier = Modifier.size(130.dp))
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pieSlices.forEach { slice ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(slice.color, shape = RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(slice.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    val pct = if ((totalExpense + totalIncome) > 0)
                                        (slice.value / (totalExpense + totalIncome).toFloat()) * 100f else 0f
                                    Text("%.1f%%".format(pct), fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── 3. Expenses by category – horizontal bar chart ────────────────
        if (byCategory.isNotEmpty()) {
            StatSectionCard(title = "Expenses by Category", subtitle = "All time breakdown") {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val maxVal = byCategory.maxOfOrNull { it.second } ?: 1.0
                    byCategory.forEachIndexed { index, (cat, amount) ->
                        val fraction = (amount / maxVal).toFloat().coerceIn(0f, 1f)
                        val color = categoryColors[index % categoryColors.size]
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = cat.lowercase().replaceFirstChar { it.uppercaseChar() },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "%.2f€".format(amount),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction)
                                        .fillMaxHeight()
                                        .background(color, RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── 4. Last 7 days – grouped bar chart ────────────────────────────
        val hasRecentData = (dailyExpense + dailyIncome).any { it > 0f }
        if (hasRecentData) {
            StatSectionCard(title = "Last 7 Days", subtitle = "Daily income & expenses") {
                Spacer(modifier = Modifier.height(8.dp))
                // Legend
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendDot(Color(0xFFE06030), "Expenses")
                    LegendDot(Color(0xFF4A9E6A), "Income")
                }
                Spacer(modifier = Modifier.height(8.dp))
                GroupedBarChart(
                    labels       = dayLabels,
                    series1      = dailyExpense,
                    series2      = dailyIncome,
                    color1       = Color(0xFFE06030),
                    color2       = Color(0xFF4A9E6A),
                    modifier     = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )
            }
        }

        // ── 5. Top spends highlight ───────────────────────────────────────
        if (top3.isNotEmpty()) {
            StatSectionCard(title = "Top Spending Categories", subtitle = "Where most money goes") {
                Spacer(modifier = Modifier.height(8.dp))
                top3.forEachIndexed { i, (cat, amount) ->
                    val medals = listOf("🥇", "🥈", "🥉")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(medals[i], fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cat.lowercase().replaceFirstChar { it.uppercaseChar() },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "%.2f€".format(amount),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColors[i]
                        )
                    }
                    if (i < top3.lastIndex) HorizontalDivider(color = TextSecondary.copy(alpha = 0.15f))
                }
            }
        }

        // Empty state
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No transactions to display", color = TextSecondary, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // room for FAB
    }
}

// ── Shared card wrapper ───────────────────────────────────────────────────────

@Composable
private fun StatSectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(title,    fontSize = 15.sp, fontWeight = FontWeight.Bold,    color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, fontWeight = FontWeight.Normal,  color = TextSecondary)
            content()
        }
    }
}

// ── Summary block ─────────────────────────────────────────────────────────────

@Composable
private fun SummaryBlock(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.8.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
    }
}

@Composable
private fun VerticalDividerLine() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(TextSecondary.copy(alpha = 0.2f))
    )
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
    }
}

// ── Pie chart ─────────────────────────────────────────────────────────────────

private data class PieSlice(val label: String, val value: Float, val color: Color)

@Composable
private fun PieChartFull(slices: List<PieSlice>, modifier: Modifier = Modifier) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f
    Canvas(modifier = modifier) {
        var startAngle = -90f
        val stroke = size.width * 0.20f
        val inset  = stroke / 2f
        slices.forEach { slice ->
            val sweep = (slice.value / total) * 360f
            drawArc(
                color      = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep - 2f,   // small gap between slices
                useCenter  = false,
                topLeft    = Offset(inset, inset),
                size       = Size(size.width - stroke, size.height - stroke),
                style      = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            startAngle += sweep
        }
    }
}

// ── Grouped bar chart (last 7 days) ──────────────────────────────────────────

@Composable
private fun GroupedBarChart(
    labels:   List<String>,
    series1:  List<Float>,   // expenses
    series2:  List<Float>,   // income
    color1:   Color,
    color2:   Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val maxVal = (series1 + series2).maxOrNull()?.takeIf { it > 0f } ?: 1f

    Canvas(modifier = modifier) {
        val n         = labels.size
        val labelH    = 22.dp.toPx()
        val chartH    = size.height - labelH
        val slotW     = size.width / n
        val barW      = slotW * 0.30f
        val gap       = slotW * 0.04f
        val axisColor = Color(0xFFCCCCCC)

        // Baseline
        drawLine(axisColor, Offset(0f, chartH), Offset(size.width, chartH), 1.5f)

        repeat(n) { i ->
            val slotLeft = i * slotW
            val centerX  = slotLeft + slotW / 2f

            // Bar 1 – expense
            val h1 = (series1[i] / maxVal) * chartH
            if (h1 > 0f) {
                drawRect(
                    color   = color1,
                    topLeft = Offset(centerX - barW - gap / 2f, chartH - h1),
                    size    = Size(barW, h1)
                )
            }

            // Bar 2 – income
            val h2 = (series2[i] / maxVal) * chartH
            if (h2 > 0f) {
                drawRect(
                    color   = color2,
                    topLeft = Offset(centerX + gap / 2f, chartH - h2),
                    size    = Size(barW, h2)
                )
            }

            // Day label
            val measured = textMeasurer.measure(
                androidx.compose.ui.text.AnnotatedString(labels[i]),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize   = 10.sp,
                    color      = Color(0xFF888888),
                    fontWeight = FontWeight.SemiBold
                )
            )
            drawText(
                measured,
                topLeft = Offset(
                    centerX - measured.size.width / 2f,
                    chartH + 4.dp.toPx()
                )
            )
        }
    }
}