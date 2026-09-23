package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val cardNumberMasked: String,
    val expiry: String,
    val balance: Double,
    val creditLimit: Double,
    val creditUsed: Double,
    val currency: String = "USD",
    val cardType: String = "VISA",
    val isPrimary: Boolean = false
)
