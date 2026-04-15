@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.wisefox.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wisefox.R
import com.example.wisefox.model.LedgerUiModel
import com.example.wisefox.navigation.Screen
import com.example.wisefox.ui.theme.TextPrimary
import com.example.wisefox.ui.theme.TextSecondary
import com.example.wisefox.ui.theme.WiseFoxCardBg
import com.example.wisefox.ui.theme.WiseFoxOrange
import com.example.wisefox.ui.theme.WiseFoxOrangeDark
import com.example.wisefox.ui.theme.WiseFoxOrangePale
import com.example.wisefox.ui.theme.WiseFoxSubCardBg
import com.example.wisefox.utils.SessionManager
import com.example.wisefox.viewmodels.HomeUiState
import com.example.wisefox.viewmodels.HomeViewModel
import com.example.wisefox.viewmodels.LedgerCrudState

// ── Color aliases ─────────────────────────────────────────────────────────────
private val expensesColor = Color(0xFFE06030)
private val earningsColor = Color(0xFF4A9E6A)

// ─────────────────────────────────────────────────────────────────────────────
// HomeScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(navController: NavController) {

    val viewModel: HomeViewModel = viewModel()
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val isShared  by viewModel.isShared.collectAsStateWithLifecycle()
    val crudState by viewModel.crudState.collectAsStateWithLifecycle()

    val username  = SessionManager.getUsername().ifBlank { "User" }
    val isPremium = false   // TODO: SessionManager.isPremium()

    // ── Dialog visibility state ───────────────────────────────────────────────
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingLedger    by remember { mutableStateOf<LedgerUiModel?>(null) }
    var deletingLedger   by remember { mutableStateOf<LedgerUiModel?>(null) }

    // ── Snackbar ──────────────────────────────────────────────────────────────
    val snackbarHostState = remember { SnackbarHostState() }

    // Observa crudState para cerrar diálogos y mostrar feedback
    LaunchedEffect(crudState) {
        when (val state = crudState) {
            is LedgerCrudState.Success -> {
                showCreateDialog = false
                editingLedger    = null
                deletingLedger   = null
                viewModel.resetCrudState()
            }
            is LedgerCrudState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetCrudState()
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = WiseFoxOrangeDark,
                    contentColor = Color.White
                )
            }
        },
        floatingActionButton = {
            // Solo los propietarios pueden crear ledgers (Solo mode)
            if (!isShared) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    shape = CircleShape,
                    containerColor = WiseFoxOrangeDark,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create ledger")
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.welcome) + " $username!",
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

            // ── Stats Row ─────────────────────────────────────────────────────
            val successLedgers = (uiState as? HomeUiState.Success)?.ledgers ?: emptyList()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatisticCard(
                    modifier   = Modifier.weight(1f),
                    iconRes    = R.drawable.ic_earnings,
                    label      = stringResource(R.string.earnings),
                    value      = viewModel.getTotalEarnings(successLedgers),
                    valueColor = earningsColor
                )
                StatisticCard(
                    modifier   = Modifier.weight(1f),
                    iconRes    = R.drawable.ic_expenses,
                    label      = stringResource(R.string.expenses),
                    value      = viewModel.getTotalExpenses(successLedgers),
                    valueColor = expensesColor
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Ledgers Header ────────────────────────────────────────────────
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
                    onToggle = { viewModel.setSharedFilter(it) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Expenses / earnings legend ────────────────────────────────────
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

            // ── Ledger List ───────────────────────────────────────────────────
            when (val state = uiState) {

                is HomeUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = WiseFoxOrange)
                    }
                }

                is HomeUiState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                is HomeUiState.Success -> {
                    if (state.ledgers.isEmpty()) {
                        Text(
                            text = if (isShared) "No shared ledgers yet" else "No ledgers yet. Tap + to create one!",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        state.ledgers.forEach { ledger ->
                            LedgerItem(
                                label    = ledger.name.uppercase(),
                                expenses = ledger.totalExpenses,
                                earnings = ledger.totalEarnings,
                                // Solo el propietario puede editar/borrar
                                canEdit  = ledger.ownerId == SessionManager.getUserId(),
                                onClick  = {
                                    navController.navigate(
                                        Screen.LedgerDetail.createRoute(ledger.id, ledger.name)
                                    )
                                },
                                onEdit   = { editingLedger = ledger },
                                onDelete = { deletingLedger = ledger }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Quick Advice ──────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.quick_advice),
                fontSize = 20.sp,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = WiseFoxOrangeDark
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (isPremium) {
                QuickAdviceCard(
                    iconRes = R.drawable.ic_ai,
                    message = "Great job! Set up an automatic 5% saving rule this week."
                )
                Spacer(modifier = Modifier.height(10.dp))
                QuickAdviceCard(
                    iconRes = R.drawable.ic_ai,
                    message = "Your food spending is 20% higher than last month. Try a weekly budget."
                )
            } else {
                QuickAdviceCard(
                    iconRes = R.drawable.ic_ai,
                    message = "Unlock personalised AI tips about your spending habits."
                )
                Spacer(modifier = Modifier.height(10.dp))
                PremiumUpgradeCard()
            }

            Spacer(modifier = Modifier.height(80.dp)) // espacio para el FAB
        }
    }

    // ── Create Dialog ─────────────────────────────────────────────────────────
    if (showCreateDialog) {
        LedgerFormDialog(
            title       = "New Ledger",
            initialName = "",
            initialCurrency = "EUR",
            initialDescription = "",
            isSaving    = crudState is LedgerCrudState.Loading,
            onDismiss   = { showCreateDialog = false },
            onConfirm   = { name, currency, desc ->
                viewModel.createLedger(name, currency, desc)
            }
        )
    }

    // ── Edit Dialog ───────────────────────────────────────────────────────────
    editingLedger?.let { ledger ->
        LedgerFormDialog(
            title              = "Edit Ledger",
            initialName        = ledger.name,
            initialCurrency    = ledger.currency,
            initialDescription = ledger.description ?: "",
            isSaving           = crudState is LedgerCrudState.Loading,
            onDismiss          = { editingLedger = null },
            onConfirm          = { name, currency, desc ->
                viewModel.updateLedger(ledger.id, name, currency, desc)
            }
        )
    }

    // ── Delete Confirmation Dialog ────────────────────────────────────────────
    deletingLedger?.let { ledger ->
        AlertDialog(
            onDismissRequest = { deletingLedger = null },
            containerColor   = WiseFoxCardBg,
            title = {
                Text(
                    text = "Delete Ledger",
                    fontWeight = FontWeight.Bold,
                    color = WiseFoxOrangeDark
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${ledger.name}\"? This action cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteLedger(ledger.id) },
                    enabled = crudState !is LedgerCrudState.Loading
                ) {
                    if (crudState is LedgerCrudState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = WiseFoxOrangeDark)
                    } else {
                        Text("Delete", color = expensesColor, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingLedger = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LedgerFormDialog — used for both Create and Edit
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LedgerFormDialog(
    title: String,
    initialName: String,
    initialCurrency: String,
    initialDescription: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, currency: String, description: String?) -> Unit
) {
    var name        by remember { mutableStateOf(initialName) }
    var currency    by remember { mutableStateOf(initialCurrency) }
    var description by remember { mutableStateOf(initialDescription) }
    var nameError   by remember { mutableStateOf(false) }

    // Currency picker dropdown
    val currencies = listOf("EUR", "USD", "GBP", "JPY", "CHF", "CAD", "AUD", "CNY")
    var showCurrencyDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = WiseFoxCardBg)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = WiseFoxOrangeDark
                )

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Name *") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Name is required", color = expensesColor) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor     = TextPrimary,
                        unfocusedTextColor   = TextPrimary,
                        focusedLabelColor    = WiseFoxOrangeDark,
                        unfocusedLabelColor  = TextSecondary,
                        focusedBorderColor   = WiseFoxOrangeDark,
                        unfocusedBorderColor = WiseFoxOrangePale,
                        cursorColor          = WiseFoxOrangeDark
                    )
                )

                // Currency dropdown
                Box {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = {},
                        label = { Text("Currency *") },
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = {
                            Text(
                                text = "▼",
                                fontSize = 12.sp,
                                color = WiseFoxOrangeDark,
                                modifier = Modifier
                                    .clickable { showCurrencyDropdown = true }
                                    .padding(end = 8.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCurrencyDropdown = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor     = TextPrimary,
                            unfocusedTextColor   = TextPrimary,
                            focusedLabelColor    = WiseFoxOrangeDark,
                            unfocusedLabelColor  = TextSecondary,
                            focusedBorderColor   = WiseFoxOrangeDark,
                            unfocusedBorderColor = WiseFoxOrangePale,
                            cursorColor          = WiseFoxOrangeDark
                        )
                    )
                    DropdownMenu(
                        expanded = showCurrencyDropdown,
                        onDismissRequest = { showCurrencyDropdown = false }
                    ) {
                        currencies.forEach { curr ->
                            DropdownMenuItem(
                                text = { Text(curr) },
                                onClick = {
                                    currency = curr
                                    showCurrencyDropdown = false
                                }
                            )
                        }
                    }
                }

                // Description field (optional)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor     = TextPrimary,
                        unfocusedTextColor   = TextPrimary,
                        focusedLabelColor    = WiseFoxOrangeDark,
                        unfocusedLabelColor  = TextSecondary,
                        focusedBorderColor   = WiseFoxOrangeDark,
                        unfocusedBorderColor = WiseFoxOrangePale,
                        cursorColor          = WiseFoxOrangeDark
                    )
                )

                // Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@Surface
                            }
                            onConfirm(
                                name,
                                currency,
                                description.ifBlank { null }
                            )
                        },
                        shape = RoundedCornerShape(50),
                        color = if (isSaving) WiseFoxOrange.copy(alpha = 0.6f) else WiseFoxOrangeDark,
                        modifier = Modifier.clip(RoundedCornerShape(50))
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (title == "New Ledger") "Create" else "Save",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-components
// ─────────────────────────────────────────────────────────────────────────────

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
                androidx.compose.material3.Icon(
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
    earnings: Float,
    canEdit: Boolean = true,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val maxValue = maxOf(expenses, earnings, 1f)
    val expensesProgress = (expenses / maxValue).coerceIn(0f, 1f)
    val earningsProgress = (earnings / maxValue).coerceIn(0f, 1f)

    var showMenu by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { if (canEdit) showMenu = true }
                ),
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

                // Expenses bar
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
                        text = if (expenses > 0f) "${expenses.toInt()}€" else "—",
                        fontSize = 13.sp,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = expensesColor
                        ),
                        modifier = Modifier.width(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Earnings bar
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
                        text = if (earnings > 0f) "${earnings.toInt()}€" else "—",
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

        // Context menu (long-press)
        if (canEdit) {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit", color = WiseFoxOrangeDark, fontWeight = FontWeight.SemiBold) },
                    onClick = {
                        showMenu = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = expensesColor, fontWeight = FontWeight.SemiBold) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
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
fun PremiumUpgradeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3D6))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_wisefox_icon),
                contentDescription = null,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Upgrade to Premium",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = WiseFoxOrangeDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Get personalised AI tips, advanced statistics and more.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                onClick = { /* TODO: navigate to subscription screen */ },
                shape = RoundedCornerShape(50),
                color = WiseFoxOrangeDark
            ) {
                Text(
                    text = "Go Pro",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SoloSharedSelector
// ─────────────────────────────────────────────────────────────────────────────

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