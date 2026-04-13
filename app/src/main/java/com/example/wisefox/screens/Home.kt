package com.example.wisefox.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wisefox.R
import com.example.wisefox.ui.theme.*

private val expensesColor = Color(0xFFE06030)
private val earningsColor = Color(0xFF4A9E6A)

@Composable
fun HomeScreen(navController: NavController) {
    var isShared by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // ── Header ──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.welcome) + " " + "User!",
                fontSize = 30.sp,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = WiseFoxOrangeDark
                )
            )
            Image(
                painter = painterResource(id = R.drawable.ic_wisefox_icon),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Stats Row ────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatisticCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_earnings,
                label = stringResource(R.string.earnings),
                value = "1067€",
                valueColor = earningsColor
            )
            StatisticCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_expenses,
                label = stringResource(R.string.expenses),
                value = "67€",
                valueColor = expensesColor
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Ledgers Header ───────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_ledger),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.ledgers_capital),
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = WiseFoxOrangeDark
                    )
                )
            }

            SoloSharedSelector(
                isShared = isShared,
                onToggle = { isShared = it }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── expenses / earnings legend ─────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.bar),
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = expensesColor
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.expenses),
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = expensesColor
                    )
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.bar),
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = earningsColor
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.earnings),
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = earningsColor
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── Ledger Items ─────────────────────────────────────
        LedgerItem(label = "MY OWN LEDGER", expenses = 555f, earnings = 700f)
        Spacer(modifier = Modifier.height(10.dp))
        LedgerItem(label = "SCHOOL LEDGER", expenses = 23f, earnings = 33f)
        Spacer(modifier = Modifier.height(10.dp))
        LedgerItem(label = "FOOD LEDGER", expenses = 106f, earnings = 133f)

        Spacer(modifier = Modifier.height(24.dp))

        // ── Quick Advice ─────────────────────────────────────
        Text(
            text = stringResource(R.string.quick_advice),
            fontSize = 20.sp,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = WiseFoxOrangeDark
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        QuickAdviceCard(
            iconRes = R.drawable.ic_ai,
            message = "Great job! Set up an automatic 5% saving rule this week."
        )
    }
}


// ── Sub-components ────────────────────────────────────────────────

@Composable
fun StatisticCard(
    modifier: Modifier = Modifier,
    iconRes: Int,
    label: String,
    value: String,
    valueColor: Color = WiseFoxOrangeDark
) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 17.sp,
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                )
            }
            Text(
                text = value,
                fontSize = 25.sp,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
            )
        }
    }
}

@Composable
fun LedgerItem(
    label: String,
    expenses: Float,
    earnings: Float
) {
    // Both bars share the same scale — the larger value fills 100%
    val maxValue = maxOf(expenses, earnings)
    val expensesProgress = (expenses / maxValue).coerceIn(0f, 1f)
    val earningsProgress = (earnings / maxValue).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                text = label,
                fontSize = 14.sp,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // expenses bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { expensesProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = expensesColor,
                    trackColor = expensesColor.copy(alpha = 0.18f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${expenses.toInt()}€",
                    fontSize = 13.sp,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = expensesColor
                    ),
                    modifier = Modifier.width(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // earnings bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { earningsProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = earningsColor,
                    trackColor = earningsColor.copy(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${earnings.toInt()}€",
                    fontSize = 13.sp,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = earningsColor
                    ),
                    modifier = Modifier.width(44.dp)
                )
            }
        }
    }
}

@Composable
fun QuickAdviceCard(iconRes: Int, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = WiseFoxOrangeDark)) {
                        append(stringResource(R.string.smart_tip))
                    }
                    withStyle(SpanStyle(color = TextSecondary)) {
                        append(message)
                    }
                },
                fontSize = 15.sp,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )
        }
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