package com.example.wisefox.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wisefox.model.LedgerResponse
import com.example.wisefox.ui.theme.*
import com.example.wisefox.utils.SessionManager
import com.example.wisefox.viewmodels.ProfileViewModel
import com.example.wisefox.viewmodels.ProfileUiState
import com.example.wisefox.viewmodels.ShareLedgerState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.res.stringResource
import com.example.wisefox.model.UserLedgerResponse
import com.example.wisefox.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedLedgersScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState    by viewModel.uiState.collectAsStateWithLifecycle()
    val shareState by viewModel.shareLedgerState.collectAsStateWithLifecycle()

    var selectedLedger   by remember { mutableStateOf<LedgerResponse?>(null) }
    var showShareDialog  by remember { mutableStateOf(false) }

    val userId    = SessionManager.getUserId()
    val isPremium = SessionManager.isPremium()

    val ledgerMembersMap by viewModel.ledgerMembers.collectAsStateWithLifecycle()

    // 只展示当前用户是 owner 的共享账本（memberCount > 1）
    val sharedLedgers = when (val s = uiState) {
        is ProfileUiState.Success ->
            s.ledgers.filter { it.ownerId == userId && it.memberCount > 1 }
        else -> emptyList()
    }

    if (showShareDialog && selectedLedger != null) {
        ShareToUserDialog(
            ledger     = selectedLedger!!,
            shareState = shareState,
            onSend     = { username -> viewModel.shareLedgerByUsername(selectedLedger!!.id, username) },
            onDismiss  = {
                showShareDialog = false
                selectedLedger  = null
                viewModel.resetShareState()
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.shared_ledgers_title),
                        fontWeight  = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color       = WiseFoxOrangeDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back),
                            tint = WiseFoxOrangeDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        when (uiState) {
            is ProfileUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = WiseFoxOrangeDark)
                }
            }
            is ProfileUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center) {
                    Text((uiState as ProfileUiState.Error).message, color = Color(0xFFE53935))
                }
            }
            is ProfileUiState.Success -> {
                if (sharedLedgers.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.no_shared_ledgers_yet),
                            color     = TextSecondary,
                            fontSize  = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier  = Modifier.padding(horizontal = 40.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier            = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding      = PaddingValues(vertical = 16.dp)
                    ) {
                        items(sharedLedgers, key = { it.id }) { ledger ->
                            SharedLedgerManageRow(
                                ledger   = ledger,
                                members  = ledgerMembersMap[ledger.id] ?: emptyList(),
                                onExpand = { viewModel.loadMembersForLedger(ledger.id) },
                                onShare  = {
                                    selectedLedger  = ledger
                                    showShareDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Ledger row with share button ──────────────────────────────────────────────

@Composable
private fun SharedLedgerManageRow(
    ledger: LedgerResponse,
    members: List<UserLedgerResponse>,
    onShare: () -> Unit,
    onExpand: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CC))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Header row ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                        if (!expanded) onExpand()  // 展开时触发加载
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = ledger.name,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = WiseFoxOrangeDark
                    )
                    Text(
                        text     = ledger.currency ?: "",
                        fontSize = 12.sp,
                        color    = TextSecondary
                    )
                    Text(
                        text     = stringResource(R.string.members_count, ledger.memberCount),
                        fontSize = 11.sp,
                        color    = TextSecondary.copy(alpha = 0.7f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector        = Icons.Default.Share,
                            contentDescription = "Share",
                            tint               = WiseFoxOrangeDark
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint               = TextSecondary,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            // ── Members list (expanded) ───────────────────────────────────
            if (expanded) {
                HorizontalDivider(
                    color = WiseFoxOrangeDark.copy(alpha = 0.15f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                if (members.isEmpty()) {
                    Text(
                        text     = stringResource(R.string.loading_members),
                        fontSize = 12.sp,
                        color    = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical   = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        members.forEach { member ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier          = Modifier.fillMaxWidth()
                            ) {
                                // Avatar circle
                                Box(
                                    modifier         = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(WiseFoxOrangePale),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text       = member.username
                                            ?.firstOrNull()
                                            ?.uppercase() ?: "?",
                                        fontSize   = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = WiseFoxOrangeDark
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text       = "@${member.username ?: ""}",
                                        fontSize   = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = TextPrimary
                                    )
                                    Text(
                                        text     = member.permission ?: "",
                                        fontSize = 10.sp,
                                        color    = if (member.permission == "OWNER")
                                            WiseFoxOrangeDark
                                        else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Share to user dialog (by username, with real-time check) ──────────────────

@Composable
private fun ShareToUserDialog(
    ledger:     LedgerResponse,
    shareState: ShareLedgerState,
    onSend:     (String) -> Unit,
    onDismiss:  () -> Unit
) {
    var usernameInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { if (shareState !is ShareLedgerState.Sending) onDismiss() },
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape    = RoundedCornerShape(24.dp),
                colors   = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text          = stringResource(R.string.share_ledger_title).uppercase(),
                        fontSize      = 18.sp,
                        fontWeight    = FontWeight.ExtraBold,
                        color         = WiseFoxOrangeDark,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text     = stringResource(R.string.share_ledger_by_username, ledger.name),
                        fontSize = 13.sp,
                        color    = TextSecondary
                    )

                    OutlinedTextField(
                        value         = usernameInput,
                        onValueChange = { usernameInput = it },
                        label         = { Text(stringResource(R.string.hint_username), color = TextPrimary) },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        enabled       = shareState !is ShareLedgerState.Sending,
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = WiseFoxOrange,
                            unfocusedBorderColor = WiseFoxOrangeDark.copy(alpha = 0.4f),
                            focusedLabelColor    = WiseFoxOrange,
                            unfocusedLabelColor  = TextPrimary.copy(alpha = 0.7f),
                            focusedTextColor     = TextPrimary,
                            unfocusedTextColor   = TextPrimary,
                            errorTextColor       = Color(0xFF1A1A1A),
                            cursorColor          = WiseFoxOrange
                        )
                    )

                    // 状态反馈
                    when (shareState) {
                        is ShareLedgerState.Sending -> {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(20.dp),
                                    color       = WiseFoxOrangeDark,
                                    strokeWidth = 2.dp
                                )
                                Text("Sharing...", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                        is ShareLedgerState.Sent -> {
                            Text(
                                "✓ Successfully shared with @${usernameInput.trim()}",
                                color      = Color(0xFF4CAF50),
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 13.sp
                            )
                        }
                        is ShareLedgerState.Error -> {
                            // 区分已是成员 vs 用户不存在 vs 其他错误
                            val msg = when {
                                shareState.message.contains("already", ignoreCase = true) ->
                                    "\"${usernameInput.trim()}\" is already a member of this ledger."
                                shareState.message.contains("not found", ignoreCase = true) ||
                                        shareState.message.contains("404", ignoreCase = true) ->
                                    "User \"${usernameInput.trim()}\" does not exist."
                                else -> shareState.message
                            }
                            Text(msg, color = Color(0xFFE53935), fontSize = 13.sp)
                        }
                        else -> {}
                    }

                    // 按钮行
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick  = onDismiss,
                            modifier = Modifier.weight(1f),
                            enabled  = shareState !is ShareLedgerState.Sending,
                            shape    = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                if (shareState is ShareLedgerState.Sent)
                                    stringResource(R.string.close)
                                else
                                    stringResource(R.string.cancel),
                                color = TextPrimary
                            )
                        }
                        if (shareState !is ShareLedgerState.Sent) {
                            Button(
                                onClick  = { if (usernameInput.isNotBlank()) onSend(usernameInput.trim()) },
                                modifier = Modifier.weight(1f),
                                enabled  = usernameInput.isNotBlank()
                                        && shareState !is ShareLedgerState.Sending,
                                shape    = RoundedCornerShape(12.dp),
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor = WiseFoxOrange
                                )
                            ) {
                                Text(stringResource(R.string.share_capital), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}