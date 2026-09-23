package com.example.data.firebase

import com.example.data.CardAccountEntity
import com.example.data.ExpenseEntity

data class FirestoreExpense(
    val id: Long = 0L,
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "Other",
    val timestamp: Long = 0L,
    val account: String = "Universal Visa",
    val note: String = "",
    val isRecurring: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toEntity(): ExpenseEntity = ExpenseEntity(
        id = id,
        title = title,
        amount = amount,
        category = category,
        timestamp = timestamp,
        account = account,
        note = note,
        isRecurring = isRecurring
    )

    companion object {
        fun fromEntity(entity: ExpenseEntity): FirestoreExpense = FirestoreExpense(
            id = entity.id,
            title = entity.title,
            amount = entity.amount,
            category = entity.category,
            timestamp = entity.timestamp,
            account = entity.account,
            note = entity.note,
            isRecurring = entity.isRecurring,
            updatedAt = System.currentTimeMillis()
        )
    }
}

data class FirestoreCard(
    val id: Long = 0L,
    val name: String = "",
    val cardNumberMasked: String = "****",
    val expiry: String = "",
    val balance: Double = 0.0,
    val creditLimit: Double = 0.0,
    val creditUsed: Double = 0.0,
    val currency: String = "USD",
    val cardType: String = "VISA",
    val isPrimary: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toEntity(): CardAccountEntity = CardAccountEntity(
        id = id,
        name = name,
        cardNumberMasked = cardNumberMasked,
        expiry = expiry,
        balance = balance,
        creditLimit = creditLimit,
        creditUsed = creditUsed,
        currency = currency,
        cardType = cardType,
        isPrimary = isPrimary
    )

    companion object {
        fun fromEntity(entity: CardAccountEntity): FirestoreCard = FirestoreCard(
            id = entity.id,
            name = entity.name,
            cardNumberMasked = entity.cardNumberMasked,
            expiry = entity.expiry,
            balance = entity.balance,
            creditLimit = entity.creditLimit,
            creditUsed = entity.creditUsed,
            currency = entity.currency,
            cardType = entity.cardType,
            isPrimary = entity.isPrimary,
            updatedAt = System.currentTimeMillis()
        )
    }
}

enum class SyncState {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE,
    ERROR
}
