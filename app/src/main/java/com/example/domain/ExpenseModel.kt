package com.example.domain

import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.util.Locale

data class ExpenseCategory(
    val name: String,
    val color: Color,
    val iconName: String
)

object ExpenseCategories {
    val FOOD = ExpenseCategory("Food", Color(0xFF8BC34A), "restaurant")
    val CLOTHING = ExpenseCategory("Clothing", Color(0xFF64B5F6), "checkroom")
    val HOME_GOODS = ExpenseCategory("Home goods", Color(0xFFFFB74D), "home")
    val TECH = ExpenseCategory("Tech", Color(0xFFBA68C8), "devices")
    val ENTERTAINMENT = ExpenseCategory("Entertainment", Color(0xFFFF8A65), "videogame_asset")
    val SHOPPING = ExpenseCategory("Shopping", Color(0xFF4DD0E1), "shopping_bag")
    val TRAVEL = ExpenseCategory("Travel", Color(0xFFFFD54F), "flight")
    val TRANSPORT = ExpenseCategory("Transport", Color(0xFF81C784), "directions_car")
    val OTHER = ExpenseCategory("Other", Color(0xFFA1887F), "more_horiz")

    val all = listOf(FOOD, CLOTHING, HOME_GOODS, TECH, ENTERTAINMENT, SHOPPING, TRAVEL, TRANSPORT, OTHER)

    fun getCategory(name: String): ExpenseCategory {
        return all.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: OTHER
    }
}

data class CategorySpendItem(
    val category: String,
    val totalAmount: Double,
    val percentage: Float,
    val count: Int,
    val color: Color
)

data class DailySpendPoint(
    val dayNumber: Int,
    val dateLabel: String,
    val amount: Double
)

data class MonthlySummary(
    val year: Int,
    val month: Int, // 0-based
    val monthLabel: String,
    val totalExpense: Double,
    val dailyAverage: Double,
    val transactionCount: Int,
    val highestDaySpend: Double,
    val highestCategory: String,
    val categorySpends: List<CategorySpendItem>,
    val dailyPoints: List<DailySpendPoint>
)

data class SavingsRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val potentialMonthlySavings: Double,
    val category: String,
    val urgencyLevel: String = "Medium", // High, Medium, Tip
    val actionLabel: String = "Set Budget Limit"
)

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}
