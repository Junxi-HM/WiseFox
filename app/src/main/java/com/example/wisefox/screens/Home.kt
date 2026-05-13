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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import com.example.wisefox.viewmodels.HomeViewModel.UsernameCheckState

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
    var showTypePicker       by remember { mutableStateOf(false) }
    var showCreateSharedDialog by remember { mutableStateOf(false) }
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
                showCreateSharedDialog = false
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
                    .padding(horizontal = 20.dp, vertical = 8.dp)
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
                        painter = painterResource(id = R.drawable.ic_fox_home),
                        contentDescription = null,
                        modifier = Modifier.size(90.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(18.dp))

                // ── Ledger list ───────────────────────────────────────────
                if (isLoading) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = WiseFoxOrange)
                    }
                } else if (activeLedgers.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        val noLedgersText = if (isShared) {
                            stringResource(R.string.no_shared_ledgers)
                        } else {
                            stringResource(R.string.no_ledgers_yet)
                        }

                        Text(
                            text = noLedgersText,
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    if (!isShared) {
                        // ── Solo tab：原有逻辑不变 ────────────────────────────────
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(activeLedgers, key = { it.ledger.id }) { uiModel ->
                                val currentUserId = SessionManager.getUserId()
                                LedgerCard(
                                    uiModel  = uiModel,
                                    isOwned  = uiModel.ledger.ownerId == currentUserId,
                                    onClick  = {
                                        navController.navigate(
                                            Screen.LedgerDetail.createRoute(uiModel.ledger.id)
                                        )
                                    },
                                    onEdit   = { editTarget = uiModel },
                                    onDelete = { deleteTarget = uiModel }
                                )
                            }
                        }
                    } else {
                        // ── Shared tab：分 owner / member 两个区域 ────────────────
                        val currentUserId = SessionManager.getUserId()
                        val ownedShared  = activeLedgers.filter { it.ledger.ownerId == currentUserId }
                        val joinedShared = activeLedgers.filter { it.ledger.ownerId != currentUserId }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // ── My Shared Ledgers ─────────────────────────────────
                            if (ownedShared.isNotEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(R.string.my_shared_ledgers),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WiseFoxOrangeDark,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                items(ownedShared, key = { it.ledger.id }) { uiModel ->
                                    LedgerCard(
                                        uiModel  = uiModel,
                                        isOwned  = true,
                                        onClick  = {
                                            navController.navigate(
                                                Screen.LedgerDetail.createRoute(uiModel.ledger.id)
                                            )
                                        },
                                        onEdit   = { editTarget = uiModel },
                                        onDelete = { deleteTarget = uiModel }
                                    )
                                }
                            }

                            // ── Joined Ledgers ────────────────────────────────────
                            if (joinedShared.isNotEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(R.string.joined_ledgers),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(top = if (ownedShared.isNotEmpty()) 12.dp else 4.dp, bottom = 4.dp)
                                    )
                                }
                                items(joinedShared, key = { it.ledger.id }) { uiModel ->
                                    LedgerCard(
                                        uiModel  = uiModel,
                                        isOwned  = false,
                                        onClick  = {
                                            navController.navigate(
                                                Screen.LedgerDetail.createRoute(uiModel.ledger.id)
                                            )
                                        },
                                        onEdit   = { editTarget = uiModel },
                                        onDelete = { deleteTarget = uiModel }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Tip card ──────────────────────────────────────────────
                TipCard(message = stringResource(R.string.default_tip))
            }

            // ── FAB ───────────────────────────────────────────────────────
            FloatingActionButton(
                onClick = { showTypePicker = true },
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
// ── Ledger type picker ─────────────────────────────────────────────────────
    if (showTypePicker) {
        val isPremium = SessionManager.isPremium()
        LedgerTypePickerDialog(
            isPremium     = isPremium,
            onSelectPersonal = { showTypePicker = false; showCreateDialog = true },
            onSelectShared   = { showTypePicker = false; showCreateSharedDialog = true },
            onDismiss        = { showTypePicker = false }
        )
    }

// ── Create shared dialog ───────────────────────────────────────────────────
    if (showCreateSharedDialog) {
        val usernameStates by vm.usernameCheckState.collectAsStateWithLifecycle()
        SharedLedgerFormDialog(
            isLoading            = crudState is LedgerCrudState.Loading,
            usernameCheckState   = usernameStates,
            onCheckUsername      = { vm.checkUsername(it) },
            onClearUsernameStates = { vm.clearUsernameCheckState() },
            onDismiss            = { showCreateSharedDialog = false },
            onConfirm            = { name, currency, description, members ->
                vm.createSharedLedger(name, currency, description, members)
            }
        )
    }
    // ── Create dialog ──────────────────────────────────────────────────────
    if (showCreateDialog) {
        LedgerFormDialog(
            title = stringResource(R.string.new_ledger_title),
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
            title = stringResource(R.string.edit_ledger_title),
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
            title = { Text(stringResource(R.string.delete_ledger_title), fontWeight = FontWeight.Bold, color = TextPrimary) },
            text  = { Text(stringResource(R.string.delete_ledger_confirm, target.ledger.name), color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteLedger(target.ledger.id)
                    deleteTarget = null
                }) {
                    Text(stringResource(R.string.delete), color = Color(0xFFE06030), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.cancel), color = TextSecondary)
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
                            Text(stringResource(R.string.cancel),color = TextPrimary)
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

// ── Ledger type picker dialog ─────────────────────────────────────────────

@Composable
private fun LedgerTypePickerDialog(
    isPremium: Boolean,
    onSelectPersonal: () -> Unit,
    onSelectShared: () -> Unit,
    onDismiss: () -> Unit
) {
    var showPremiumWarning by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
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
                        text = stringResource(R.string.new_ledger),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WiseFoxOrangeDark,
                        letterSpacing = 1.sp
                    )
                    Text(stringResource(R.string.choose_ledger_type), color = TextSecondary, fontSize = 14.sp)

                    // Personal
                    Button(
                        onClick = onSelectPersonal,
                        modifier = Modifier.fillMaxWidth(),
                        shape  = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WiseFoxOrange)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.personal_ledger),  color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Shared
                    Button(
                        onClick = {
                            if (isPremium) onSelectShared()
                            else showPremiumWarning = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape  = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPremium) WiseFoxOrange else Color(0xFFBBBBBB)
                        )
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.shared_ledger), color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    if (showPremiumWarning) {
                        Text(
                            text = stringResource(R.string.premium_only_shared),
                            color = WiseFoxOrangeDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.cancel), color = TextPrimary)

                    }
                }
            }
        }
    }
}

// ── Shared ledger form dialog ─────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SharedLedgerFormDialog(
    isLoading: Boolean,
    usernameCheckState: Map<String, UsernameCheckState>,
    onCheckUsername: (String) -> Unit,
    onClearUsernameStates: () -> Unit,
    onConfirm: (name: String, currency: String, description: String?, members: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var name          by remember { mutableStateOf("") }
    var currency      by remember { mutableStateOf("EUR") }
    var description   by remember { mutableStateOf("") }
    var memberInput   by remember { mutableStateOf("") }
    var members       by remember { mutableStateOf(listOf<String>()) }
    var memberError   by remember { mutableStateOf<String?>(null) }

    val enterUsernameErr = stringResource(R.string.enter_username_error)
    val alreadyAddedErr = stringResource(R.string.already_added_error)
    val verifyUsernameErr = stringResource(R.string.verify_username_first)
    val addAtLeastOneMemberErr = stringResource(R.string.add_at_least_one_member)

    val currentInputState = usernameCheckState[memberInput.trim()]

    DisposableEffect(Unit) { onDispose { onClearUsernameStates() } }

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
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.new_shared_ledger),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WiseFoxOrangeDark,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.ledger_name), color = TextPrimary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ledgerDialogFieldColors()
                    )

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text(stringResource(R.string.currency_label), color = TextPrimary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ledgerDialogFieldColors()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.description_optional), color = TextPrimary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ledgerDialogFieldColors()
                    )

                    // ── Member input ──────────────────────────────────────
                    Text(
                        stringResource(R.string.members_label),
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = memberInput,
                            onValueChange = { input ->
                                memberInput = input
                                memberError = null
                                if (input.trim().isNotEmpty()) onCheckUsername(input.trim())
                            },
                            label = { Text("Username", color = TextPrimary) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            isError = currentInputState is UsernameCheckState.NotFound || memberError != null,
                            trailingIcon = {
                                when (currentInputState) {
                                    is UsernameCheckState.Loading ->
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = WiseFoxOrange,
                                            strokeWidth = 2.dp
                                        )
                                    is UsernameCheckState.Found ->
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    is UsernameCheckState.NotFound ->
                                        Icon(
                                            Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = Color(0xFFE06030),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    null -> {}
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = when (currentInputState) {
                                    is UsernameCheckState.Found    -> Color(0xFF4CAF50)
                                    is UsernameCheckState.NotFound -> Color(0xFFE06030)
                                    else -> WiseFoxOrange
                                },
                                unfocusedBorderColor = when (currentInputState) {
                                    is UsernameCheckState.Found    -> Color(0xFF4CAF50)
                                    is UsernameCheckState.NotFound -> Color(0xFFE06030)
                                    else -> WiseFoxOrangeDark.copy(alpha = 0.4f)
                                },
                                focusedLabelColor    = WiseFoxOrange,
                                unfocusedLabelColor  = TextPrimary.copy(alpha = 0.7f),
                                focusedTextColor     = TextPrimary,
                                unfocusedTextColor   = TextPrimary,
                                errorTextColor       = Color(0xFF1A1A1A),
                                cursorColor          = WiseFoxOrange,
                                errorBorderColor     = Color(0xFFE06030),
                                errorLabelColor      = Color(0xFFE06030)
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                val trimmed = memberInput.trim()
                                when {
                                    trimmed.isEmpty()                          -> memberError = enterUsernameErr
                                    members.contains(trimmed)                  -> memberError = alreadyAddedErr
                                    currentInputState !is UsernameCheckState.Found -> memberError = verifyUsernameErr
                                    else -> {
                                        members = members + trimmed
                                        memberInput = ""
                                        memberError = null
                                    }
                                }
                            },
                            enabled = currentInputState is UsernameCheckState.Found
                        ) {
                            Icon(
                                Icons.Default.AddCircle,
                                contentDescription = "Add member",
                                tint = if (currentInputState is UsernameCheckState.Found)
                                    WiseFoxOrange else Color(0xFFBBBBBB),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // 状态提示文字
                    when {
                        memberError != null ->
                            Text(memberError!!, color = Color(0xFFE06030), fontSize = 12.sp)
                        currentInputState is UsernameCheckState.NotFound ->
                            Text(
                                stringResource(R.string.user_not_found_fmt, memberInput.trim()),
                                color = Color(0xFFE06030),
                                fontSize = 12.sp
                            )
                        currentInputState is UsernameCheckState.Found
                                && !members.contains(memberInput.trim()) ->
                            Text(
                                stringResource(R.string.user_found_add),
                                color = Color(0xFF4CAF50),
                                fontSize = 12.sp
                            )
                    }

                    // 已添加成员 chips
                    if (members.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement   = Arrangement.spacedBy(4.dp)
                        ) {
                            members.forEach { m ->
                                InputChip(
                                    selected = false,
                                    onClick  = { members = members - m },
                                    label    = { Text(m, color = TextPrimary, fontSize = 13.sp) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Cancel,
                                            contentDescription = "Remove $m",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        containerColor = WiseFoxOrange.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }

                    // 按钮行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick  = onDismiss,
                            modifier = Modifier.weight(1f),
                            enabled  = !isLoading,
                            shape    = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.cancel), color = TextPrimary)
                        }
                        Button(
                            onClick = {
                                if (members.isEmpty()) {
                                    memberError = addAtLeastOneMemberErr
                                    return@Button
                                }
                                if (name.isBlank()) return@Button
                                onConfirm(
                                    name.trim(),
                                    currency.trim().ifBlank { "EUR" },
                                    description.trim().ifBlank { null },
                                    members
                                )
                            },
                            modifier = Modifier.weight(1f),
                            enabled  = !isLoading && name.isNotBlank() && currency.isNotBlank(),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = WiseFoxOrange)
                        ) {
                            if (isLoading) CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            else Text(stringResource(R.string.create),color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}