package com.example.wisefox.screens

import androidx.compose.foundation.Canvas
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
import androidx.navigation.NavController
import com.example.wisefox.R
import com.example.wisefox.ui.theme.*

// ── Enums & Models ────────────────────────────────────────────────────────────

enum class TransactionTab { TRANSACTIONS, STATISTICS }
enum class TransactionType { EXPENSE, INCOME }

data class Transaction(
    val id: Int,
    val category: String,
    val type: TransactionType,
    val amount: Float,
    val ledger: String,
    val date: String
)

// ── Sample data ───────────────────────────────────────────────────────────────

val sampleTransactions = listOf(
    Transaction(1, "Food", TransactionType.EXPENSE, 15f, "Personal Ledger", "TODAY"),
    Transaction(2, "Hygiene", TransactionType.EXPENSE, 9f, "Personal Ledger", "TODAY"),
    Transaction(3, "Electronic", TransactionType.EXPENSE, 67f, "Personal Ledger", "7TH APRIL"),
    Transaction(4, "Income", TransactionType.INCOME, 100f, "Personal Ledger", "7TH APRIL"),
)

private data class PieSlice(val label: String, val value: Float, val color: Color)
private data class BarEntry(val label: String, val value: Float)

private val pieSlices = listOf(
    PieSlice("Expenses", 90f, Color(0xFFE06030)),
    PieSlice("Income", 10f, Color(0xFFFFD97A))
)
private val barEntries = listOf(
    BarEntry("Food", 10f),
    BarEntry("Hygiene", 5f),
    BarEntry("Electronics", 12f),
    BarEntry("Other", 7f)
)

// ── Host: gestiona tabs ───────────────────────────────────────────────────────

@Composable
fun TransactionsScreen(navController: NavController) {
    var activeTab by remember { mutableStateOf(TransactionTab.TRANSACTIONS) }
    var isShared by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            TransactionTabBar(
                activeTab = activeTab,
                onTabSelected = { activeTab = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SoloSharedSelector(
                    isShared = isShared,
                    onToggle = { isShared = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (activeTab) {
                TransactionTab.TRANSACTIONS -> TransactionsContent()
                TransactionTab.STATISTICS -> StatisticsContent()
            }
        }

        if (activeTab == TransactionTab.TRANSACTIONS) {
            FloatingActionButton(
                onClick = { /* TO DO */ },
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
                    contentDescription = stringResource(R.string.add_transaction),
                    modifier = Modifier.size(36.dp)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TabPill(
                text = stringResource(R.string.transactions),
                selected = activeTab == TransactionTab.TRANSACTIONS,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(TransactionTab.TRANSACTIONS) }
            )
            TabPill(
                text = stringResource(R.string.statistics),
                selected = activeTab == TransactionTab.STATISTICS,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(TransactionTab.STATISTICS) }
            )
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
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) WiseFoxOrange else Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) Color.White else TextSecondary.copy(alpha = 0.6f)
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        )
    }
}

// ── Transactions tab content ──────────────────────────────────────────────────

@Composable
private fun TransactionsContent() {
    var fromDate by remember { mutableStateOf("01/04/2026") }
    var toDate by remember { mutableStateOf("09/04/2026") }
    var ledgerFilter by remember { mutableStateOf("--ALL--") }
    var categoryFilter by remember { mutableStateOf("--ALL--") }

    val grouped = sampleTransactions
        .filter {
            (ledgerFilter == "--ALL--" || it.ledger == ledgerFilter) &&
                    (categoryFilter == "--ALL--" || it.category == categoryFilter)
        }
        .groupBy { it.date }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Fila 1: Fechas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilterLabel(stringResource(R.string.from_capital))
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(fromDate) { }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterLabel(stringResource(R.string.to_capital))
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(toDate) { }
                }

                // Fila 2: Ledger
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterLabel(stringResource(R.string.to_capital))
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(ledgerFilter) {
                        ledgerFilter =
                            if (ledgerFilter == "--ALL--") "Personal Ledger" else "--ALL--"
                    }
                }


                // Fila 3: Categoría
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterLabel(stringResource(R.string.filter_by_category))
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(categoryFilter) {
                        categoryFilter = if (categoryFilter == "--ALL--") "Food" else "--ALL--"
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            grouped.forEach { (dateLabel, transactions) ->
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
                items(transactions) { tx ->
                    TransactionCard(transaction = tx)
                }
            }
        }
    }
}

// ── Statistics tab content ────────────────────────────────────────────────────

@Composable
private fun StatisticsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(title = "Statistic 1", subtitle = "Circle") {
            PieChart(
                slices = pieSlices,
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.CenterEnd)
            )
        }
        StatCard(title = "Statistic 2", subtitle = "Bars") {
            BarChart(
                entries = barEntries,
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth(0.6f)
                    .align(Alignment.CenterEnd)
            )
        }
    }
}

// ── Stat card wrapper ─────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    title: String,
    subtitle: String,
    chart: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle.uppercase(),
                    fontSize = 22.sp,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = WiseFoxOrangeDark
                    )
                )
            }
            chart()
        }
    }
}

// ── Pie chart ─────────────────────────────────────────────────────────────────

@Composable
private fun PieChart(slices: List<PieSlice>, modifier: Modifier = Modifier) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        var startAngle = -90f
        val diameter = size.minDimension * 0.85f
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)

        slices.forEach { slice ->
            val sweep = (slice.value / total) * 360f
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize
            )
            val midAngle = Math.toRadians((startAngle + sweep / 2).toDouble())
            val radius = diameter / 2f * 0.65f
            val cx = size.width / 2f + (radius * Math.cos(midAngle)).toFloat()
            val cy = size.height / 2f + (radius * Math.sin(midAngle)).toFloat()
            val pct = "${(slice.value / total * 100).toInt()}%"
            val measured = textMeasurer.measure(
                AnnotatedString(pct),
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(cx - measured.size.width / 2f, cy - measured.size.height / 2f)
            )
            startAngle += sweep
        }
    }
}

// ── Bar chart ─────────────────────────────────────────────────────────────────

private val barColors = listOf(
    Color(0xFFE06030), Color(0xFFFFD97A),
    Color(0xFFFFB347), Color(0xFFE8955A)
)

@Composable
private fun BarChart(entries: List<BarEntry>, modifier: Modifier = Modifier) {
    val maxValue = entries.maxOf { it.value }
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val barCount = entries.size
        val totalWidth = size.width
        val chartHeight = size.height * 0.80f
        val barWidth = (totalWidth / barCount) * 0.55f
        val gap = (totalWidth / barCount) * 0.45f
        val axisColor = Color(0xFF333333)

        drawLine(axisColor, Offset(0f, chartHeight), Offset(totalWidth, chartHeight), 2f)
        drawLine(axisColor, Offset(0f, 0f), Offset(0f, chartHeight), 2f)

        entries.forEachIndexed { index, entry ->
            val left = index * (barWidth + gap) + gap / 2f
            val barHeight = (entry.value / maxValue) * chartHeight
            val top = chartHeight - barHeight

            drawRect(
                color = barColors[index % barColors.size],
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight)
            )

            val measured = textMeasurer.measure(
                AnnotatedString(entry.value.toInt().toString()),
                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = axisColor)
            )
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    left + barWidth / 2f - measured.size.width / 2f,
                    top - measured.size.height - 2f
                )
            )
        }
    }
}

// ── Transaction card ──────────────────────────────────────────────────────────

private val expenseColor = Color(0xFFE06030)
private val incomeColor = Color(0xFF4A9E6A)

@Composable
fun TransactionCard(transaction: Transaction) {
    val typeColor = if (transaction.type == TransactionType.EXPENSE) expenseColor else incomeColor
    val typeLabel = if (transaction.type == TransactionType.EXPENSE) "Expense" else "Income"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = transaction.category,
                fontSize = 15.sp,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                ),
                modifier = Modifier.weight(1.5f)
            )
            Text(
                text = typeLabel.uppercase(),
                fontSize = 13.sp,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = typeColor
                ),
                modifier = Modifier.weight(1.2f)
            )
            Text(
                text = "${transaction.amount.toInt()}€",
                fontSize = 20.sp,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = WiseFoxOrangeDark
                ),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = transaction.ledger.uppercase(),
                fontSize = 11.sp,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary.copy(alpha = 0.7f)
                ),
                lineHeight = 13.sp,
                modifier = Modifier.weight(1.2f)
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun FilterLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
    )
}

@Composable
private fun FilterChip(value: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = WiseFoxOrangePale.copy(alpha = 0.5f),
        contentColor = WiseFoxOrangeDark
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
private fun SoloSharedSelector(
    isShared: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectorOption(
                text = stringResource(R.string.solo_capital),
                isSelected = !isShared,
                onClick = { onToggle(false) }
            )
            SelectorOption(
                text = stringResource(R.string.shared_capital),
                isSelected = isShared,
                onClick = { onToggle(true) }
            )
        }
    }
}

@Composable
private fun SelectorOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (isSelected) WiseFoxOrange else Color.Transparent,
        modifier = Modifier.fillMaxHeight()
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.Black
            )
        }
    }
}