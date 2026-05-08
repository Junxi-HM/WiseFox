package com.example.wisefox.model

data class UserLedgerResponse(
    val id: Long,
    val userId: Long?,
    val username: String?,
    val userEmail: String?,
    val ledgerId: Long?,
    val ledgerName: String?,
    val permission: String?
)