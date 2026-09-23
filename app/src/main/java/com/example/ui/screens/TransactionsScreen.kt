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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseEntity
import com.example.domain.ExpenseCategories
import com.example.domain.formatCurrency
import com.example.ui.theme.FinFlowNeonLime
import com.example.ui.theme.FinFlowPillDark
import com.example.ui.theme.FinFlowTextPrimary
import com.example.ui.theme.FinFlowTextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TransactionsScreen(
    expenses: List<ExpenseEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategoryFilter: String?,
    onCategoryFilterChange: (String?) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    onAddExpenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter expenses
    val filtered = expenses.filter {
        val matchesQuery = searchQuery.isBlank() ||
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.note.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == null ||
                it.category.equals(selectedCategoryFilter, ignoreCase = true)
        matchesQuery && matchesCategory
    }

    val totalFilteredSpend = filtered.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transactions_screen_column"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title & Add Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "All Transactions",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = FinFlowTextPrimary,
                        modifier = Modifier.testTag("transactions_screen_title")
                    )
                    Text(
                        text = "${filtered.size} expenses · Total: ${formatCurrency(totalFilteredSpend)}",
                        fontSize = 13.sp,
                        color = FinFlowTextSecondary
                    )
                }

                Button(
                    onClick = onAddExpenseClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FinFlowNeonLime,
                        contentColor = FinFlowTextPrimary
                    ),
                    modifier = Modifier.testTag("add_transaction_page_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Add", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by merchant or note...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = FinFlowTextSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_search_field"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FinFlowPillDark,
                    unfocusedBorderColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        // Category filter chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isAll = selectedCategoryFilter == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isAll) FinFlowPillDark else Color.White)
                            .clickable { onCategoryFilterChange(null) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAll) Color.White else FinFlowTextPrimary
                        )
                    }
                }

                items(ExpenseCategories.all) { cat ->
                    val isSelected = selectedCategoryFilter.equals(cat.name, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) FinFlowNeonLime else Color.White)
                            .clickable { onCategoryFilterChange(cat.name) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(cat.color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FinFlowTextPrimary
                        )
                    }
                }
            }
        }

        // Transactions list
        if (filtered.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching expenses found.",
                            fontSize = 14.sp,
                            color = FinFlowTextSecondary
                        )
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { expense ->
                TransactionCardDetailed(
                    expense = expense,
                    onDelete = { onDeleteExpense(expense.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun TransactionCardDetailed(
    expense: ExpenseEntity,
    onDelete: () -> Unit
) {
    val dateTimeFormat = SimpleDateFormat("EEE, d MMM yyyy · h:mm a", Locale.US)
    val timeLabel = dateTimeFormat.format(expense.timestamp)
    val catMeta = ExpenseCategories.getCategory(expense.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("detailed_txn_${expense.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(catMeta.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = expense.title.take(1).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = catMeta.color
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = expense.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinFlowTextPrimary
                        )
                        if (expense.isRecurring) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Monthly",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${expense.category} · ${expense.account}",
                        fontSize = 12.sp,
                        color = FinFlowTextSecondary
                    )

                    Text(
                        text = timeLabel,
                        fontSize = 11.sp,
                        color = Color(0xFF9AA295)
                    )

                    if (expense.note.isNotBlank()) {
                        Text(
                            text = expense.note,
                            fontSize = 11.sp,
                            color = FinFlowTextSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "- ${formatCurrency(expense.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinFlowTextPrimary
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete expense",
                        tint = Color(0xFFB0BEC5),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
