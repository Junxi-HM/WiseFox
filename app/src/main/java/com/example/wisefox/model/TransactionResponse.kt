package com.example.wisefox.model
import java.time.LocalDate
/**
 * Mapea directamente el JSON que devuelve el backend:
 * GET /api/transactions/{ledgerId}  →  List<TransactionResponse>
 *
 * Campos según TransactionResponse.java del backend:
 *   id, amount, type (INCOME/EXPENSE), category, date, note, ledgerId, ledgerName
 *
 * Nota: el backend serializa TransactionType y Category como Strings en JSON,
 * así que los recibimos como String aquí para no depender de los enums Java.
 */
data class TransactionResponse(
    val id: Long?,
    val amount: Double?,
    val type: TransactionTypeEnum?,
    val category: CategoryEnum?,
    val date: LocalDate?,
    val note: String?,
    val ledgerId: Long?,
    val ledgerName: String?
)

enum class TransactionTypeEnum { INCOME, EXPENSE }
enum class CategoryEnum { FOOD, TRANSPORT, RENT, ENTERTAINMENT, HEALTH, SHOPPING, SALARY, OTHER }