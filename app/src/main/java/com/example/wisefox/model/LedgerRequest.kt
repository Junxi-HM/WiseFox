package com.example.wisefox.model

/**
 * Request body enviado al backend para crear o actualizar un ledger.
 * Campos según LedgerRequest.java:
 *   name (required), currency (required), description (optional), userId (required)
 */
data class LedgerRequest(
    val name: String,
    val currency: String,
    val description: String?,
    val userId: Long
)