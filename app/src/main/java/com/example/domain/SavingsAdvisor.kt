package com.example.domain

import com.example.data.ExpenseEntity
import java.util.Calendar

data class SavingsAnalysisReport(
    val previousMonthTotal: Double,
    val currentMonthTotal: Double,
    val projectedSavingsAtTarget: Double,
    val recommendations: List<SavingsRecommendation>,
    val highSpendCategoryAlert: String?,
    val subscriptionsTotal: Double,
    val recurringCount: Int,
    val recommendedDailyCapNextMonth: Double
)

object SavingsAdvisor {

    fun generateRecommendations(
        allExpenses: List<ExpenseEntity>,
        targetMonthCal: Calendar,
        savingsTargetPercent: Float = 15f // 15% default target
    ): SavingsAnalysisReport {
        // Calculate previous month calendar
        val prevMonthCal = (targetMonthCal.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        val prevSummary = MonthlyReportCalculator.calculateSummary(allExpenses, prevMonthCal)
        val currentSummary = MonthlyReportCalculator.calculateSummary(allExpenses, targetMonthCal)

        val baseTotal = if (prevSummary.totalExpense > 0) prevSummary.totalExpense else currentSummary.totalExpense
        val projectedSavings = baseTotal * (savingsTargetPercent / 100.0)

        val recommendations = mutableListOf<SavingsRecommendation>()

        // 1. Check Subscriptions & Recurring Charges
        val recurringExpenses = allExpenses.filter { it.isRecurring }
        val uniqueRecurring = recurringExpenses.groupBy { it.title.lowercase() }
        val monthlyRecurringTotal = uniqueRecurring.values.sumOf { group -> group.first().amount }
        val recurringCount = uniqueRecurring.size

        if (monthlyRecurringTotal > 0) {
            val potentialSubSavings = monthlyRecurringTotal * 0.35 // 35% cutback assumption
            recommendations.add(
                SavingsRecommendation(
                    id = "rec_subscriptions",
                    title = "Audit Recurring Subscriptions",
                    description = "Detected $recurringCount recurring services (totaling ${formatCurrency(monthlyRecurringTotal)}/mo). Pausing or canceling 1-2 inactive services saves money immediately next month.",
                    potentialMonthlySavings = potentialSubSavings,
                    category = "Entertainment",
                    urgencyLevel = "High",
                    actionLabel = "Review Subscriptions"
                )
            )
        }

        // 2. High-spend category optimization from previous month
        val topCategory = prevSummary.categorySpends.firstOrNull()
        var highSpendAlert: String? = null

        if (topCategory != null && topCategory.totalAmount > 0) {
            val catSavings = topCategory.totalAmount * 0.20 // 20% trim
            highSpendAlert = "${topCategory.category} was your largest cost center at ${formatCurrency(topCategory.totalAmount)} (${String.format("%.1f", topCategory.percentage)}% of total spend)."
            
            recommendations.add(
                SavingsRecommendation(
                    id = "rec_top_category",
                    title = "Optimize ${topCategory.category} Budget",
                    description = "Previous month's ${topCategory.category} spending was high. Setting a 20% reduction cap will free up ${formatCurrency(catSavings)} next month.",
                    potentialMonthlySavings = catSavings,
                    category = topCategory.category,
                    urgencyLevel = "High",
                    actionLabel = "Cap ${topCategory.category}"
                )
            )
        }

        // 3. Food & Dining trim (common highest discretionary expense)
        val foodSpend = prevSummary.categorySpends.find { it.category.equals("Food", ignoreCase = true) }
        if (foodSpend != null && foodSpend.totalAmount > 100) {
            val mealPrepSavings = foodSpend.totalAmount * 0.15
            recommendations.add(
                SavingsRecommendation(
                    id = "rec_food_dining",
                    title = "Meal Planning & Batch Cooking",
                    description = "You spent ${formatCurrency(foodSpend.totalAmount)} on food last month. Swapping just 2 takeout meals per week for batch home cooking saves ~$140 next month.",
                    potentialMonthlySavings = mealPrepSavings,
                    category = "Food",
                    urgencyLevel = "Medium",
                    actionLabel = "Set Food Goal"
                )
            )
        }

        // 4. Shopping / Impulse purchase rule
        val shoppingSpend = prevSummary.categorySpends.find { 
            it.category.equals("Shopping", ignoreCase = true) || it.category.equals("Clothing", ignoreCase = true) 
        }
        if (shoppingSpend != null && shoppingSpend.totalAmount > 50) {
            val impulseSavings = shoppingSpend.totalAmount * 0.25
            recommendations.add(
                SavingsRecommendation(
                    id = "rec_impulse_cooling",
                    title = "48-Hour Impulse Purchase Delay",
                    description = "Apply a 48-hour delay for non-essential online checkout items. Historical users save over 25% on discretionary apparel and gear.",
                    potentialMonthlySavings = impulseSavings,
                    category = shoppingSpend.category,
                    urgencyLevel = "Tip",
                    actionLabel = "Enable Reminder"
                )
            )
        }

        // 5. Daily Spending Cap
        val baseDailyAverage = if (prevSummary.dailyAverage > 0) prevSummary.dailyAverage else currentSummary.dailyAverage
        val recommendedDailyCap = (baseDailyAverage * (1.0 - (savingsTargetPercent / 100.0))).coerceAtLeast(15.0)

        recommendations.add(
            SavingsRecommendation(
                id = "rec_daily_cap",
                title = "Enforce ${formatCurrency(recommendedDailyCap)} Daily Spending Cap",
                description = "Limiting variable daily expenses to ${formatCurrency(recommendedDailyCap)} allows you to automatically hit your $savingsTargetPercent% savings milestone.",
                potentialMonthlySavings = (baseDailyAverage - recommendedDailyCap) * 30.0,
                category = "General",
                urgencyLevel = "Medium",
                actionLabel = "Apply Daily Cap"
            )
        )

        return SavingsAnalysisReport(
            previousMonthTotal = prevSummary.totalExpense,
            currentMonthTotal = currentSummary.totalExpense,
            projectedSavingsAtTarget = projectedSavings,
            recommendations = recommendations,
            highSpendCategoryAlert = highSpendAlert,
            subscriptionsTotal = monthlyRecurringTotal,
            recurringCount = recurringCount,
            recommendedDailyCapNextMonth = recommendedDailyCap
        )
    }
}
