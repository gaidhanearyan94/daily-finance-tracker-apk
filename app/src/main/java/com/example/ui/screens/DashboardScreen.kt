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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.data.CardAccountEntity
import com.example.data.ExpenseEntity
import com.example.domain.ExpenseCategories
import com.example.domain.MonthlySummary
import com.example.domain.formatCurrency
import com.example.ui.components.NeonVisaCard
import com.example.ui.components.SpendingRadarChart
import com.example.ui.components.SpendingTrendCurve
import com.example.ui.theme.FinFlowNeonLime
import com.example.ui.theme.FinFlowPillDark
import com.example.ui.theme.FinFlowTextPrimary
import com.example.ui.theme.FinFlowTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardScreen(
    monthlySummary: MonthlySummary?,
    recentExpenses: List<ExpenseEntity>,
    cards: List<CardAccountEntity>,
    selectedCalendar: Calendar,
    onMonthChange: (Int) -> Unit,
    onViewAllTransactions: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddCardClick: () -> Unit,
    isCloudConnected: Boolean = false,
    onAccountClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primaryCard = cards.firstOrNull { it.isPrimary } ?: cards.firstOrNull() ?: CardAccountEntity(
        name = "Universal",
        cardNumberMasked = "*9423",
        expiry = "06/28",
        balance = 8523.20,
        creditLimit = 22000.00,
        creditUsed = 0.00,
        currency = "USD",
        cardType = "VISA",
        isPrimary = true
    )
    val secondaryCard = cards.firstOrNull { !it.isPrimary }

    var showMonthDropdown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_screen_column"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Dashboard Header (matching design: "Dashboard" + "Financial ▾" pill)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    color = FinFlowTextPrimary,
                    modifier = Modifier.testTag("dashboard_title")
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cloud Sync status badge pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(FinFlowPillDark)
                            .clickable { onAccountClick() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("dashboard_cloud_badge"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isCloudConnected) FinFlowNeonLime else Color(0xFFFFB020))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCloudConnected) "Cloud" else "Sign In",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Month / Perspective selector pill
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .clickable { showMonthDropdown = true }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("dashboard_month_selector"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val monthFormat = SimpleDateFormat("MMM yyyy", Locale.US)
                            Text(
                                text = monthlySummary?.monthLabel ?: monthFormat.format(selectedCalendar.time),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FinFlowTextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select month",
                                modifier = Modifier.size(16.dp),
                                tint = FinFlowTextSecondary
                            )
                        }

                        DropdownMenu(
                            expanded = showMonthDropdown,
                            onDismissRequest = { showMonthDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Current Month") },
                                onClick = {
                                    onMonthChange(0)
                                    showMonthDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Previous Month") },
                                onClick = {
                                    onMonthChange(-1)
                                    showMonthDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("2 Months Ago") },
                                onClick = {
                                    onMonthChange(-2)
                                    showMonthDropdown = false
                                }
                            )
                        }
                    }

                    // Quick "+ Expense" pill
                    Button(
                        onClick = onAddExpenseClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FinFlowNeonLime,
                            contentColor = FinFlowTextPrimary
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("quick_add_expense_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Recent Transactions Card (Directly from design mockup)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recent_transactions_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header: "Recent transactions" + "View all >"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent transactions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinFlowTextPrimary
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(FinFlowPillDark)
                                .clickable { onViewAllTransactions() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("view_all_transactions_btn"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View all",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val topTransactions = recentExpenses.take(5)
                    if (topTransactions.isEmpty()) {
                        Text(
                            text = "No transactions recorded for this month yet.",
                            fontSize = 13.sp,
                            color = FinFlowTextSecondary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            topTransactions.forEach { expense ->
                                TransactionRowItem(expense = expense)
                            }
                        }
                    }
                }
            }
        }

        // 3. Spending Radar Web Chart (Matching screenshot right card)
        item {
            SpendingRadarChart(
                categorySpends = monthlySummary?.categorySpends ?: emptyList()
            )
        }

        // 4. Universal Neon Lime VISA Card (Matching screenshot bottom-left)
        item {
            NeonVisaCard(
                card = primaryCard,
                secondaryCard = secondaryCard,
                onAddCardClick = onAddCardClick
            )
        }

        // 5. Budget / Trend Spline Curve (Matching screenshot bottom-right)
        item {
            SpendingTrendCurve(
                dailyPoints = monthlySummary?.dailyPoints ?: emptyList()
            )
        }

        // Extra bottom spacing for navigation bar
        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun TransactionRowItem(expense: ExpenseEntity) {
    val dateTimeFormat = SimpleDateFormat("d MMMM · h:mm a", Locale.US)
    val timeLabel = dateTimeFormat.format(expense.timestamp)
    val catMeta = ExpenseCategories.getCategory(expense.category)

    // Dynamic brand tint color for avatar icon
    val avatarBgColor = when (expense.title.lowercase()) {
        "paypal" -> Color(0xFFE3F2FD)
        "twitch" -> Color(0xFFEDE7F6)
        "airbnb", "airbnb deposit" -> Color(0xFFFFEBEE)
        "dribbble" -> Color(0xFFFCE4EC)
        else -> catMeta.color.copy(alpha = 0.2f)
    }

    val iconColor = when (expense.title.lowercase()) {
        "paypal" -> Color(0xFF0070BA)
        "twitch" -> Color(0xFF9146FF)
        "airbnb", "airbnb deposit" -> Color(0xFFFF5A5F)
        "dribbble" -> Color(0xFFEA4C89)
        else -> catMeta.color
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_item_${expense.id}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Rounded brand avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(avatarBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = expense.title.take(1).uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = expense.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinFlowTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeLabel,
                    fontSize = 12.sp,
                    color = FinFlowTextSecondary
                )
            }
        }

        // Amount: "- $10.67"
        Text(
            text = "- ${formatCurrency(expense.amount)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = FinFlowTextPrimary
        )
    }
}
