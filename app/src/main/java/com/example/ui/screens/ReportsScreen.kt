package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CategorySpendItem
import com.example.domain.ExpenseCategories
import com.example.domain.MonthlySummary
import com.example.domain.formatCurrency
import com.example.ui.components.InteractivePieChart
import com.example.ui.components.SpendingRadarChart
import com.example.ui.theme.FinFlowNeonLime
import com.example.ui.theme.FinFlowPillDark
import com.example.ui.theme.FinFlowTextPrimary
import com.example.ui.theme.FinFlowTextSecondary

@Composable
fun ReportsScreen(
    monthlySummary: MonthlySummary?,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    var chartType by remember { mutableStateOf("pie") } // "pie" or "radar"

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("reports_screen_column"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title & Month Navigation bar
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Monthly Reports",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    color = FinFlowTextPrimary,
                    modifier = Modifier.testTag("reports_screen_title")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Month Navigator Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPrevMonth,
                        modifier = Modifier.testTag("report_prev_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            tint = FinFlowTextPrimary
                        )
                    }

                    Text(
                        text = monthlySummary?.monthLabel ?: "Current Month",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FinFlowTextPrimary
                    )

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.testTag("report_next_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            tint = FinFlowTextPrimary
                        )
                    }
                }
            }
        }

        // 2. Summary KPI Metrics Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("summary_metrics_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Total Monthly Expenses",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = FinFlowTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(monthlySummary?.totalExpense ?: 0.0),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = FinFlowTextPrimary,
                        modifier = Modifier.testTag("total_monthly_expense_text")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryMiniTile(
                            label = "Daily Average",
                            value = formatCurrency(monthlySummary?.dailyAverage ?: 0.0),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SummaryMiniTile(
                            label = "Transactions",
                            value = "${monthlySummary?.transactionCount ?: 0}",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SummaryMiniTile(
                            label = "Top Category",
                            value = monthlySummary?.highestCategory ?: "None",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Interactive Chart (Pie or Radar Toggle)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Chart toggle buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (chartType == "pie") FinFlowNeonLime else Color.Transparent)
                                .clickable { chartType = "pie" }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("toggle_pie_chart"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DonutLarge,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Pie Chart",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FinFlowTextPrimary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (chartType == "radar") FinFlowNeonLime else Color.Transparent)
                                .clickable { chartType = "radar" }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("toggle_radar_chart"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Radar View",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FinFlowTextPrimary
                                )
                            }
                        }
                    }
                }

                if (chartType == "pie") {
                    InteractivePieChart(
                        categorySpends = monthlySummary?.categorySpends ?: emptyList(),
                        totalExpense = monthlySummary?.totalExpense ?: 0.0
                    )
                } else {
                    SpendingRadarChart(
                        categorySpends = monthlySummary?.categorySpends ?: emptyList()
                    )
                }
            }
        }

        // 4. Category Breakdown Details List
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("category_breakdown_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Category Ranking",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FinFlowTextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val spends = monthlySummary?.categorySpends ?: emptyList()
                    if (spends.isEmpty()) {
                        Text(
                            text = "No category data recorded for this month.",
                            fontSize = 13.sp,
                            color = FinFlowTextSecondary,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            spends.forEach { item ->
                                CategoryRankingItem(item = item)
                            }
                        }
                    }
                }
            }
        }

        // 5. Automated Smart Insights Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_insights_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6FAF0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = FinFlowPillDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Monthly Spending Insights",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinFlowTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val topCategory = monthlySummary?.categorySpends?.firstOrNull()
                    if (topCategory != null) {
                        Text(
                            text = "• ${topCategory.category} represents the highest cost center at ${String.format("%.1f", topCategory.percentage)}% (${formatCurrency(topCategory.totalAmount)}).",
                            fontSize = 13.sp,
                            color = FinFlowTextPrimary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Text(
                        text = "• Average daily burn rate was ${formatCurrency(monthlySummary?.dailyAverage ?: 0.0)} over the month.",
                        fontSize = 13.sp,
                        color = FinFlowTextPrimary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Check the Next Month Recommendations tab for customized action steps to reduce unnecessary expenses.",
                        fontSize = 13.sp,
                        color = FinFlowTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun SummaryMiniTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF4F7EE))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = FinFlowTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = FinFlowTextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CategoryRankingItem(item: CategorySpendItem) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(item.color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.category,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinFlowTextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(${item.count})",
                    fontSize = 12.sp,
                    color = FinFlowTextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatCurrency(item.totalAmount),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinFlowTextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${String.format("%.1f", item.percentage)}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinFlowTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress meter bar
        LinearProgressIndicator(
            progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = item.color,
            trackColor = Color(0xFFF0F4E8)
        )
    }
}
