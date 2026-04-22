package com.example.wisefox.model

/**
 * Mapea directamente el JSON que devuelve el backend:
 * GET /api/ledgers/user/{userId}  →  List<LedgerResponse>
 * GET /api/ledgers/{id}           →  LedgerResponse
 *
 * Campos según LedgerResponse.java del backend:
 *   id, name, currency, description, ownerId, ownerUsername
 */
data class LedgerResponse(
    val id: Long,
    val name: String,
    val currency: String?,
    val description: String?,
    val ownerId: Long?,
    val ownerUsername: String?
)