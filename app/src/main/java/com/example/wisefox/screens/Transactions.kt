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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
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

// ── Screen ────────────────────────────────────────────────────────────────────

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

    // Close dialog on success / show error snackbar
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
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
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
    ledgerNames: Map<Long, String>,   // kept for API compatibility
    onTxClick: (TransactionResponse) -> Unit
) {
    // FIX: "All" is now localised. Explicit type annotation avoids the
    //      "Cannot infer type for this parameter" compiler error (error #48).
    val allLabel = stringResource(R.string.filter_all)

    var ledgerFilter   by remember(allLabel) { mutableStateOf<String>(allLabel) }
    var categoryFilter by remember(allLabel) { mutableStateOf<String>(allLabel) }
    var fromDate       by remember { mutableStateOf<LocalDate?>(null) }
    var toDate         by remember { mutableStateOf<LocalDate?>(null) }

    val uniqueLedgers    = remember(transactions) { transactions.mapNotNull { it.ledgerName }.distinct() }
    val uniqueCategories = remember(transactions) { transactions.mapNotNull { it.category?.name }.distinct() }

    val filtered = transactions.filter { tx ->
        val matchLedger   = ledgerFilter   == allLabel || tx.ledgerName == ledgerFilter
        val matchCategory = categoryFilter == allLabel || tx.category?.name == categoryFilter
        val txDate  = tx.date
        val matchFrom = fromDate == null || (txDate != null && !txDate.isBefore(fromDate))
        val matchTo   = toDate   == null || (txDate != null && !txDate.isAfter(toDate))
        matchLedger && matchCategory && matchFrom && matchTo
    }

    // FIX #5: date group labels now come from strings resources
    val unknownLabel   = stringResource(R.string.unknown_label).uppercase()
    val todayLabel     = stringResource(R.string.today_label).uppercase()
    val yesterdayLabel = stringResource(R.string.yesterday_label).uppercase()

    val grouped = filtered.groupBy { tx ->
        val today = LocalDate.now()
        when {
            tx.date == null               -> unknownLabel
            tx.date == today              -> todayLabel
            tx.date == today.minusDays(1) -> yesterdayLabel
            else -> tx.date.format(DateTimeFormatter.ofPattern("d MMM yyyy")).uppercase()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Filter card (FIX #3: ExposedDropdownMenuBox replaces chip rows) ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterDropdown(
                    label    = stringResource(R.string.filter_by_ledger),
                    values   = listOf(allLabel) + uniqueLedgers,
                    selected = ledgerFilter,
                    onSelect = { ledgerFilter = it }
                )
                FilterDropdown(
                    label    = stringResource(R.string.filter_by_category),
                    values   = listOf(allLabel) + uniqueCategories,
                    selected = categoryFilter,
                    onSelect = { categoryFilter = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = WiseFoxOrange)
            }
        } else if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = stringResource(R.string.no_transactions_found),
                    color = TextSecondary,
                    fontSize = 14.sp
                )
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

// ── Dropdown filter (FIX #3) ──────────────────────────────────────────────────

@Composable
private fun FilterDropdown(
    label: String,
    values: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier         = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value         = selected,
            onValueChange = {},
            readOnly      = true,
            label         = { Text(label) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier      = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape  = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = WiseFoxOrangeDark,
                unfocusedBorderColor = WiseFoxOrangeDark.copy(alpha = 0.4f),
                focusedLabelColor    = WiseFoxOrangeDark,
                unfocusedLabelColor  = WiseFoxOrangeDark.copy(alpha = 0.7f),
                cursorColor          = WiseFoxOrangeDark,
                focusedTextColor     = Color(0xFF3A2A00),
                unfocusedTextColor   = Color(0xFF3A2A00)
            )
        )
        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            values.forEach { value ->
                DropdownMenuItem(
                    text    = { Text(value, color = Color(0xFF1A1A1A)) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
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
            TabPill(
                text     = stringResource(R.string.transactions),
                selected = activeTab == TransactionTab.TRANSACTIONS,
                modifier = Modifier.weight(1f)
            ) { onTabSelected(TransactionTab.TRANSACTIONS) }

            TabPill(
                text     = stringResource(R.string.statistics),
                selected = activeTab == TransactionTab.STATISTICS,
                modifier = Modifier.weight(1f)
            ) { onTabSelected(TransactionTab.STATISTICS) }
        }
    }
}

@Composable
private fun TabPill(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
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
                color      = if (selected) Color.White else TextSecondary.copy(alpha = 0.6f)
            ),
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth().padding(vertical = 10.dp)
        )
    }
}

// ── Solo / Shared selector ────────────────────────────────────────────────────

@Composable
private fun SoloSharedSelectorTx(isShared: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        shape    = RoundedCornerShape(50),
        colors   = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectorOptionTx(stringResource(R.string.solo_capital),   !isShared) { onToggle(false) }
            SelectorOptionTx(stringResource(R.string.shared_capital),  isShared) { onToggle(true) }
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
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = text,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
                color      = if (isSelected) Color.White else Color.Black
            )
        }
    }
}

// ── Transaction card (public — reused by LedgerDetailScreen) ──────────────────

private val expenseColor = Color(0xFFE06030)
private val incomeColor  = Color(0xFF4A9E6A)

@Composable
fun TransactionCard(
    transaction: TransactionResponse,
    showLedger: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val isExpense = transaction.type?.name == "EXPENSE"
    val typeColor = if (isExpense) expenseColor else incomeColor
    val sign      = if (isExpense) "-" else "+"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it },
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category?.name
                        ?.lowercase()
                        ?.replaceFirstChar { it.uppercaseChar() }
                        ?: stringResource(R.string.category_other),
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary
                )
                if (showLedger && !transaction.ledgerName.isNullOrBlank()) {
                    Text(
                        text     = transaction.ledgerName,
                        fontSize = 12.sp,
                        color    = TextSecondary
                    )
                }
                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text     = transaction.note,
                        fontSize = 11.sp,
                        color    = TextSecondary.copy(alpha = 0.8f)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "$sign%.2f€".format(transaction.amount ?: 0.0),
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = typeColor
                )
                Text(
                    text       = transaction.type?.name ?: "",
                    fontSize   = 10.sp,
                    color      = typeColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Statistics content ────────────────────────────────────────────────────────

@Composable
private fun StatisticsContent(transactions: List<TransactionResponse>) {

    val expenses = transactions.filter { it.type?.name == "EXPENSE" }
    val incomes  = transactions.filter { it.type?.name == "INCOME" }

    val totalExpense = expenses.sumOf { it.amount ?: 0.0 }
    val totalIncome  = incomes .sumOf { it.amount ?: 0.0 }
    val net          = totalIncome - totalExpense

    val byCategory = expenses
        .groupBy { it.category?.name ?: "OTHER" }
        .map { (cat, list) -> cat to list.sumOf { it.amount ?: 0.0 } }
        .sortedByDescending { it.second }

    val today = LocalDate.now()
    val last7 = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val dailyExpense = last7.map { day ->
        expenses.filter { it.date == day }.sumOf { it.amount ?: 0.0 }.toFloat()
    }
    val dailyIncome = last7.map { day ->
        incomes.filter { it.date == day }.sumOf { it.amount ?: 0.0 }.toFloat()
    }
    val dayLabels = last7.map { it.dayOfMonth.toString() }
    val top3      = byCategory.take(3)

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

        // ── 1. Overview mini-cards ────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatMiniCard(
                modifier = Modifier.weight(1f),
                label    = stringResource(R.string.earnings),
                value    = "+%.2f€".format(totalIncome),
                color    = incomeColor
            )
            StatMiniCard(
                modifier = Modifier.weight(1f),
                label    = stringResource(R.string.expenses),
                value    = "-%.2f€".format(totalExpense),
                color    = expenseColor
            )
            StatMiniCard(
                modifier = Modifier.weight(1f),
                label    = "NET",
                value    = "%.2f€".format(net),
                color    = if (net >= 0) incomeColor else expenseColor
            )
        }

        // ── 2. Expenses by category – horizontal bars ─────────────────────────
        if (byCategory.isNotEmpty()) {
            StatSectionCard(
                title    = stringResource(R.string.filter_by_category),
                subtitle = stringResource(R.string.where_most_money_goes)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val maxVal = byCategory.maxOfOrNull { it.second } ?: 1.0
                    byCategory.forEachIndexed { index, (cat, amount) ->
                        val fraction = (amount / maxVal).toFloat().coerceIn(0f, 1f)
                        val color    = categoryColors[index % categoryColors.size]
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text       = cat.lowercase().replaceFirstChar { it.uppercaseChar() },
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = TextPrimary
                                )
                                Text(
                                    text       = "%.2f€".format(amount),
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = color
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            // FIX errors 61/62 & 535/536: clip() and background() belong on
                            // Modifier (Compose), NOT inside a Canvas DrawScope.
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color.copy(alpha = 0.15f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── 3. Last 7 days – grouped bar chart ────────────────────────────────
        val hasRecentData = (dailyExpense + dailyIncome).any { it > 0f }
        if (hasRecentData) {
            StatSectionCard(
                title    = stringResource(R.string.last_7_days),
                subtitle = stringResource(R.string.daily_income_expenses)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendDot(expenseColor, stringResource(R.string.expenses))
                    LegendDot(incomeColor,  stringResource(R.string.earnings))
                }
                Spacer(modifier = Modifier.height(8.dp))
                GroupedBarChart(
                    labels   = dayLabels,
                    series1  = dailyExpense,
                    series2  = dailyIncome,
                    color1   = expenseColor,
                    color2   = incomeColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )
            }
        }

        // ── 4. Top-3 spending categories ──────────────────────────────────────
        if (top3.isNotEmpty()) {
            StatSectionCard(
                title    = stringResource(R.string.top_spending_categories),
                subtitle = stringResource(R.string.where_most_money_goes)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                top3.forEachIndexed { i, (cat, amount) ->
                    val medals = listOf("🥇", "🥈", "🥉")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(medals[i], fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text       = cat.lowercase().replaceFirstChar { it.uppercaseChar() },
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = TextPrimary
                            )
                        }
                        Text(
                            text       = "%.2f€".format(amount),
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color      = categoryColors[i]
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
                Text(
                    text     = stringResource(R.string.no_transactions_to_display),
                    color    = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // room for FAB
    }
}

// ── Stat mini card ────────────────────────────────────────────────────────────

@Composable
private fun StatMiniCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// ── Shared stat card wrapper ──────────────────────────────────────────────────

@Composable
private fun StatSectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(title,    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WiseFoxOrangeDark)
            Text(subtitle, fontSize = 11.sp, color = TextSecondary)
            content()
        }
    }
}

// ── Legend dot ────────────────────────────────────────────────────────────────

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 11.sp, color = TextSecondary)
    }
}

// ── Grouped bar chart ─────────────────────────────────────────────────────────
// FIX errors 61/535: only Canvas drawing primitives (drawRect, drawText) are
// used inside DrawScope. Modifier.clip() and Modifier.background() are Compose
// Modifier extensions and must NOT be called inside a Canvas block.

@Composable
private fun GroupedBarChart(
    labels:   List<String>,
    series1:  List<Float>,   // expenses
    series2:  List<Float>,   // income
    color1:   Color,
    color2:   Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val maxVal = (series1 + series2).maxOrNull()?.takeIf { it > 0f } ?: 1f

    Canvas(modifier = modifier) {
        val n = labels.size
        if (n == 0) return@Canvas
        val groupW  = size.width / n
        val barW    = groupW / 3f
        val bottomY = size.height - 20f   // leave room for day label below

        labels.forEachIndexed { i, label ->
            val cx = groupW * i + groupW / 2f

            val h1 = (series1[i] / maxVal) * (bottomY - 4f)
            val h2 = (series2[i] / maxVal) * (bottomY - 4f)

            // Expense bar — left of centre
            if (h1 > 0f) {
                drawRect(
                    color   = color1,
                    topLeft = Offset(cx - barW, bottomY - h1),
                    size    = Size(barW - 1f, h1)
                )
            }
            // Income bar — right of centre
            if (h2 > 0f) {
                drawRect(
                    color   = color2,
                    topLeft = Offset(cx + 1f, bottomY - h2),
                    size    = Size(barW - 1f, h2)
                )
            }

            // Day label
            val layout = textMeasurer.measure(
                text  = AnnotatedString(label),
                style = TextStyle(fontSize = 9.sp, color = Color(0xFF7A6A4A))
            )
            drawText(
                textLayoutResult = layout,
                topLeft          = Offset(cx - layout.size.width / 2f, bottomY + 2f)
            )
        }
    }
}