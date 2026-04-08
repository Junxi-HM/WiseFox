package com.example.wisefox.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.wisefox.screens.common.WiseFoxLayout
import com.example.wisefox.ui.theme.*

private val GastosColor = Color(0xFFE06030)
private val IngresosColor = Color(0xFF4A9E6A)

@Composable
fun HomeScreen(navController: NavController) {
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
                text = "Welcome, User!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = WiseFoxOrangeDark
                )
            )
            Image(
                painter = painterResource(id = R.drawable.ic_wisefox_icon),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
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
                valueColor = IngresosColor
            )
            StatisticCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_expenses,
                label = stringResource(R.string.expenses),
                value = "67€",
                valueColor = GastosColor
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
                Icon(
                    painter = painterResource(id = R.drawable.ic_ledger),
                    contentDescription = null,
                    tint = WiseFoxOrange,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LEDGERS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = WiseFoxOrangeDark
                    )
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Solo/Shared",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                var isShared by remember { mutableStateOf(true) }
                Switch(
                    checked = isShared,
                    onCheckedChange = { isShared = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = WiseFoxOrange
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── Gastos / Ingresos legend ─────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GastosColor
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "gastos",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = GastosColor
                    )
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = IngresosColor
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ingresos",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = IngresosColor
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── Ledger Items ─────────────────────────────────────
        LedgerItem(label = "MY OWN LEDGER", gastos = 555f, ingresos = 700f)
        Spacer(modifier = Modifier.height(10.dp))
        LedgerItem(label = "SCHOOL LEDGER", gastos = 23f, ingresos = 33f)
        Spacer(modifier = Modifier.height(10.dp))
        LedgerItem(label = "FOOD LEDGER", gastos = 106f, ingresos = 133f)

        Spacer(modifier = Modifier.height(24.dp))

        // ── Quick Advice ─────────────────────────────────────
        Text(
            text = "Quick Advice",
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
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                )
            }
            Text(
                text = value,
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
    gastos: Float,
    ingresos: Float
) {
    // Both bars share the same scale — the larger value fills 100%
    val maxValue = maxOf(gastos, ingresos)
    val gastosProgress = (gastos / maxValue).coerceIn(0f, 1f)
    val ingresosProgress = (ingresos / maxValue).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Gastos bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { gastosProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = GastosColor,
                    trackColor = GastosColor.copy(alpha = 0.18f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${gastos.toInt()}€",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = GastosColor
                    ),
                    modifier = Modifier.width(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Ingresos bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { ingresosProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = IngresosColor,
                    trackColor = IngresosColor.copy(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${ingresos.toInt()}€",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = IngresosColor
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
                        append("Smart Tip: ")
                    }
                    withStyle(SpanStyle(color = TextSecondary)) {
                        append(message)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )
        }
    }
}