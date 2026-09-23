package com.example.domain

import com.example.data.ExpenseEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object MonthlyReportCalculator {

    fun calculateSummary(expenses: List<ExpenseEntity>, targetCalendar: Calendar): MonthlySummary {
        val targetYear = targetCalendar.get(Calendar.YEAR)
        val targetMonth = targetCalendar.get(Calendar.MONTH)

        val monthNameFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        val monthLabel = monthNameFormat.format(targetCalendar.time)

        // Filter expenses for this specific month
        val monthExpenses = expenses.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.YEAR) == targetYear && cal.get(Calendar.MONTH) == targetMonth
        }

        val totalExpense = monthExpenses.sumOf { it.amount }
        val daysInMonth = targetCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = if (isCurrentMonth(targetCalendar)) {
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        } else {
            daysInMonth
        }
        val dailyAverage = if (currentDay > 0) totalExpense / currentDay else 0.0

        // Category breakdown
        val groupedByCategory = monthExpenses.groupBy { it.category }
        val categorySpends = groupedByCategory.map { (catName, items) ->
            val catTotal = items.sumOf { it.amount }
            val pct = if (totalExpense > 0) (catTotal / totalExpense).toFloat() * 100f else 0f
            val catMeta = ExpenseCategories.getCategory(catName)
            CategorySpendItem(
                category = catName,
                totalAmount = catTotal,
                percentage = pct,
                count = items.size,
                color = catMeta.color
            )
        }.sortedByDescending { it.totalAmount }

        val highestCategory = categorySpends.firstOrNull()?.category ?: "None"

        // Group by day for the budget/trend spline curve
        val dayGroups = monthExpenses.groupBy {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.DAY_OF_MONTH)
        }

        val dailyPoints = mutableListOf<DailySpendPoint>()
        val shortDateFormat = SimpleDateFormat("d MMM", Locale.US)

        for (day in 1..daysInMonth) {
            val dayCal = (targetCalendar.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, day)
            }
            val daySpend = dayGroups[day]?.sumOf { it.amount } ?: 0.0
            dailyPoints.add(
                DailySpendPoint(
                    dayNumber = day,
                    dateLabel = shortDateFormat.format(dayCal.time),
                    amount = daySpend
                )
            )
        }

        val highestDaySpend = dailyPoints.maxOfOrNull { it.amount } ?: 0.0

        return MonthlySummary(
            year = targetYear,
            month = targetMonth,
            monthLabel = monthLabel,
            totalExpense = totalExpense,
            dailyAverage = dailyAverage,
            transactionCount = monthExpenses.size,
            highestDaySpend = highestDaySpend,
            highestCategory = highestCategory,
            categorySpends = categorySpends,
            dailyPoints = dailyPoints
        )
    }

    private fun isCurrentMonth(cal: Calendar): Boolean {
        val now = Calendar.getInstance()
        return now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                now.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
    }
}
