@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.wisefox.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wisefox.R
import com.example.wisefox.navigation.Screen
import com.example.wisefox.ui.theme.*
import com.example.wisefox.viewmodels.HomeViewModel
import com.example.wisefox.viewmodels.LedgerCrudState
import com.example.wisefox.viewmodels.LedgerUiModel
import androidx.compose.foundation.background
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.wisefox.utils.SessionManager
import androidx.compose.runtime.DisposableEffect

private val expensesColor = Color(0xFFE06030)
private val earningsColor = Color(0xFF4A9E6A)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    navController: NavController,
    vm: HomeViewModel = viewModel()
) {
    var isShared      by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editTarget    by remember { mutableStateOf<LedgerUiModel?>(null) }
    var deleteTarget  by remember { mutableStateOf<LedgerUiModel?>(null) }

    val soloLedgers   by vm.soloLedgers.collectAsStateWithLifecycle()
    val sharedLedgers by vm.sharedLedgers.collectAsStateWithLifecycle()
    val isLoading     by vm.isLoading.collectAsStateWithLifecycle()
    val crudState     by vm.crudState.collectAsStateWithLifecycle()
    val snackbarHost  = remember { SnackbarHostState() }
// Refresh ledgers whenever the screen comes to the foreground
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                vm.loadLedgers()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val activeLedgers = if (isShared) sharedLedgers else soloLedgers
    val totalEarnings = activeLedgers.sumOf { it.totalIncome }
    val totalExpenses = activeLedgers.sumOf { it.totalExpense }
    val displayName = SessionManager.getName().ifBlank { SessionManager.getUsername() }


    // React to crud results
    LaunchedEffect(crudState) {
        when (crudState) {
            is LedgerCrudState.Success -> {
                showCreateDialog = false
                editTarget = null
                vm.resetCrudState()
            }
            is LedgerCrudState.Error -> {
                snackbarHost.showSnackbar((crudState as LedgerCrudState.Error).message)
                vm.resetCrudState()
            }
            else -> {}
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
                // ── Header ────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.welcome) + " $displayName!",
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

                // ── Stats Row ─────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatisticCard(
                        modifier   = Modifier.weight(1f),
                        iconRes    = R.drawable.ic_earnings,
                        label      = stringResource(R.string.earnings),
                        value      = "%.2f€".format(totalEarnings),
                        valueColor = earningsColor
                    )
                    StatisticCard(
                        modifier   = Modifier.weight(1f),
                        iconRes    = R.drawable.ic_expenses,
                        label      = stringResource(R.string.expenses),
                        value      = "%.2f€".format(totalExpenses),
                        valueColor = expensesColor
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Ledgers Header ────────────────────────────────────────
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
                    SoloSharedSelector(isShared = isShared, onToggle = { isShared = it })
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ── Legend ────────────────────────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(stringResource(R.string.bar), stringResource(R.string.expenses), expensesColor)
                    LegendItem(stringResource(R.string.bar), stringResource(R.string.earnings), earningsColor)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Ledger list ───────────────────────────────────────────
                if (isLoading) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = WiseFoxOrange)
                    }
                } else if (activeLedgers.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isShared) "No shared ledgers" else "No ledgers yet. Tap + to create one.",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(activeLedgers, key = { it.ledger.id }) { uiModel ->
                            LedgerCard(
                                uiModel   = uiModel,
                                isOwned   = !isShared,
                                onClick   = {
                                    navController.navigate(
                                        Screen.LedgerDetail.createRoute(uiModel.ledger.id)
                                    )
                                },
                                onEdit    = { editTarget = uiModel },
                                onDelete  = { deleteTarget = uiModel }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Tip card ──────────────────────────────────────────────
                TipCard(message = "Set up an automatic 5% saving rule this week.")
            }

            // ── FAB ───────────────────────────────────────────────────────
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                shape = CircleShape,
                containerColor = WiseFoxOrange,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 10.dp)
                    .size(64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(36.dp))
            }
        }
    }

    // ── Create dialog ──────────────────────────────────────────────────────
    if (showCreateDialog) {
        LedgerFormDialog(
            title     = "NEW LEDGER",
            initial   = null,
            isLoading = crudState is LedgerCrudState.Loading,
            onConfirm = { name, currency, description ->
                vm.createLedger(name, currency, description)
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    // ── Edit dialog ────────────────────────────────────────────────────────
    editTarget?.let { target ->
        LedgerFormDialog(
            title     = "EDIT LEDGER",
            initial   = target.ledger,
            isLoading = crudState is LedgerCrudState.Loading,
            onConfirm = { name, currency, description ->
                vm.updateLedger(target.ledger.id, name, currency, description)
            },
            onDismiss = { editTarget = null }
        )
    }

    // ── Delete confirmation ────────────────────────────────────────────────
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Ledger", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text  = { Text("Delete \"${target.ledger.name}\"? This cannot be undone.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteLedger(target.ledger.id)
                    deleteTarget = null
                }) {
                    Text("Delete", color = Color(0xFFE06030), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = WiseFoxSubCardBg
        )
    }
}

// ── Ledger card ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LedgerCard(
    uiModel: LedgerUiModel,
    isOwned: Boolean,
    onClick:  () -> Unit,
    onEdit:   () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val maxValue = maxOf(uiModel.totalExpense, uiModel.totalIncome, 1.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick      = onClick,
                onLongClick  = { if (isOwned) showMenu = true }
            ),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiModel.ledger.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = uiModel.ledger.currency ?: "EUR",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expenses bar
            LedgerProgressBar(
                label    = "%.2f€".format(uiModel.totalExpense),
                progress = (uiModel.totalExpense / maxValue).toFloat(),
                color    = expensesColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Earnings bar
            LedgerProgressBar(
                label    = "%.2f€".format(uiModel.totalIncome),
                progress = (uiModel.totalIncome / maxValue).toFloat(),
                color    = earningsColor
            )
        }

        // Long-press context menu (owners only)
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text    = { Text("Edit", color = TextPrimary) },
                onClick = { showMenu = false; onEdit() }
            )
            DropdownMenuItem(
                text    = { Text("Delete", color = Color(0xFFE06030)) },
                onClick = { showMenu = false; onDelete() }
            )
        }
    }
}

@Composable
private fun LedgerProgressBar(label: String, progress: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color            = color,
            trackColor       = color.copy(alpha = 0.15f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 12.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

// ── Ledger form dialog (create / edit) ────────────────────────────────────────

@Composable
fun LedgerFormDialog(
    title: String,
    initial: com.example.wisefox.model.LedgerResponse?,
    isLoading: Boolean,
    onConfirm: (name: String, currency: String, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name        by remember { mutableStateOf(initial?.name        ?: "") }
    var currency    by remember { mutableStateOf(initial?.currency    ?: "EUR") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var nameError   by remember { mutableStateOf(false) }

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
                shape  = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WiseFoxOrangeDark,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; nameError = false },
                        label = { Text("Name", color = TextPrimary) },
                        isError = nameError,
                        supportingText = if (nameError) {
                            { Text("Name is required", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ledgerDialogFieldColors()
                    )

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("Currency", color = TextPrimary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ledgerDialogFieldColors()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (optional)", color = TextPrimary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ledgerDialogFieldColors()
                    )

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
                                if (name.isBlank()) { nameError = true; return@Button }
                                onConfirm(name.trim(), currency.trim().ifBlank { "EUR" }, description.trim())
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
                            else Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ledgerDialogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = WiseFoxOrange,
    unfocusedBorderColor = WiseFoxOrangeDark.copy(alpha = 0.4f),
    focusedLabelColor    = WiseFoxOrange,
    unfocusedLabelColor  = TextPrimary.copy(alpha = 0.7f),
    focusedTextColor     = TextPrimary,
    unfocusedTextColor   = TextPrimary,
    cursorColor          = WiseFoxOrange
)

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
fun StatisticCard(
    modifier:   Modifier = Modifier,
    iconRes:    Int,
    label:      String,
    value:      String,
    valueColor: Color = WiseFoxOrangeDark
) {
    Card(
        modifier = modifier.height(80.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
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
                Text(text = label, fontSize = 17.sp,
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary))
            }
            Text(text = value, fontSize = 25.sp,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold, color = valueColor))
        }
    }
}

@Composable
private fun LegendItem(bar: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = bar, fontSize = 14.sp,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 14.sp,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, color = color))
    }
}

@Composable
private fun TipCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = WiseFoxOrange, fontWeight = FontWeight.Bold)) {
                        append("💡 TIP  ")
                    }
                    withStyle(SpanStyle(color = TextSecondary)) { append(message) }
                },
                fontSize = 15.sp,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SoloSharedSelector(isShared: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        shape    = RoundedCornerShape(50),
        colors   = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg),
        modifier = Modifier.height(32.dp)
    ) {
        Row(modifier = Modifier.padding(2.dp), verticalAlignment = Alignment.CenterVertically) {
            SelectorOption(stringResource(R.string.solo_capital),   !isShared) { onToggle(false) }
            SelectorOption(stringResource(R.string.shared_capital),  isShared) { onToggle(true) }
        }
    }
}

@Composable
private fun SelectorOption(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(50),
        color    = if (isSelected) WiseFoxOrange else Color.Transparent,
        modifier = Modifier.fillMaxHeight()
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.Black)
        }
    }
}