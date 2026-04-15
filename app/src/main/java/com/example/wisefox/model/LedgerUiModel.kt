package com.example.wisefox.model

/**
 * UI model que envuelve los datos de un ledger junto con
 * los totales calculados a partir de sus transacciones.
 *
 * Se construye en HomeViewModel después de cargar tanto
 * los ledgers como sus respectivas transactions.
 */
data class LedgerUiModel(
    val id: Long,
    val name: String,
    val currency: String,
    val description: String?,
    val ownerId: Long,
    val ownerUsername: String,
    val totalExpenses: Float,   // suma de transactions con type == "EXPENSE"
    val totalEarnings: Float    // suma de transactions con type == "INCOME"
)