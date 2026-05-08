package com.example.wisefox.model

data class LedgerSharedRequest(
    val name: String,
    val currency: String,
    val description: String?,
    val userId: Long,
    val memberUsernames: List<String>
)