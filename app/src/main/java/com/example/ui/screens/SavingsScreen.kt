package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.SavingsAnalysisReport
import com.example.domain.SavingsRecommendation
import com.example.domain.formatCurrency
import com.example.ui.theme.FinFlowNeonLime
import com.example.ui.theme.FinFlowNeonLimeDark
import com.example.ui.theme.FinFlowPillDark
import com.example.ui.theme.FinFlowTextPrimary
import com.example.ui.theme.FinFlowTextSecondary

@Composable
fun SavingsScreen(
    savingsReport: SavingsAnalysisReport?,
    savingsTargetPercent: Float,
    onTargetPercentChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val appliedRecommendations = remember { mutableStateListOf<String>() }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("savings_screen_column"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FinFlowNeonLime),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = FinFlowPillDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Next Month Savings",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = FinFlowTextPrimary,
                        modifier = Modifier.testTag("savings_screen_title")
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Intelligent recommendations derived from your previous month spending analysis",
                    fontSize = 13.sp,
                    color = FinFlowTextSecondary
                )
            }
        }

        // 2. High Impact Next Month Target Simulator Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("savings_target_simulator_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = FinFlowPillDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Month Target",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFA0AAB5)
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(FinFlowNeonLime)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = FinFlowPillDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "-${savingsTargetPercent.toInt()}% Spend Target",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FinFlowPillDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = formatCurrency(savingsReport?.projectedSavingsAtTarget ?: 0.0),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = FinFlowNeonLime,
                        letterSpacing = (-1).sp,
                        modifier = Modifier.testTag("projected_savings_amount")
                    )

                    Text(
                        text = "Estimated cash to keep in your pocket next month",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Savings Target Slider
                    Slider(
                        value = savingsTargetPercent,
                        onValueChange = onTargetPercentChange,
                        valueRange = 5f..35f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = FinFlowNeonLime,
                            activeTrackColor = FinFlowNeonLime,
                            inactiveTrackColor = Color(0xFF2C3238)
                        ),
                        modifier = Modifier.testTag("savings_target_slider")
                    )

                    // Target Quick Pickers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(5f, 10f, 15f, 20f, 25f).forEach { target ->
                            val isSelected = savingsTargetPercent.toInt() == target.toInt()
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) FinFlowNeonLime else Color(0xFF23282E))
                                    .clickable { onTargetPercentChange(target) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${target.toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) FinFlowPillDark else Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Next Month Recommended Daily Cap
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1B1E22))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Next Month Daily Spend Cap",
                                fontSize = 11.sp,
                                color = Color(0xFFA0AAB5)
                            )
                            Text(
                                text = formatCurrency(savingsReport?.recommendedDailyCapNextMonth ?: 45.0) + " / day",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Annualized Impact",
                                fontSize = 11.sp,
                                color = Color(0xFFA0AAB5)
                            )
                            val annualGrowth = (savingsReport?.projectedSavingsAtTarget ?: 0.0) * 12.0
                            Text(
                                text = "+${formatCurrency(annualGrowth)}/yr",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = FinFlowNeonLime
                            )
                        }
                    }
                }
            }
        }

        // 3. Previous Month Analysis Highlight
        if (savingsReport?.highSpendCategoryAlert != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFB74D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = Color(0xFF6B4500),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "High Spend Category Alert",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B4500)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = savingsReport.highSpendCategoryAlert,
                                fontSize = 12.sp,
                                color = Color(0xFF4A3B18),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Subscriptions & Recurring Charges Audit Banner
        if ((savingsReport?.recurringCount ?: 0) > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active Recurring Services",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = FinFlowTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${savingsReport?.recurringCount} subscriptions detected totaling ${formatCurrency(savingsReport?.subscriptionsTotal ?: 0.0)}/mo",
                                fontSize = 12.sp,
                                color = FinFlowTextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Recurring",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }

        // 5. Actionable Recommendations Header
        item {
            Text(
                text = "Tailored Action Plan (${savingsReport?.recommendations?.size ?: 0} steps)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = FinFlowTextPrimary
            )
        }

        // Recommendations List
        val recommendations = savingsReport?.recommendations ?: emptyList()
        items(recommendations) { rec ->
            val isApplied = appliedRecommendations.contains(rec.id)
            RecommendationCardItem(
                recommendation = rec,
                isApplied = isApplied,
                onToggleApply = {
                    if (isApplied) {
                        appliedRecommendations.remove(rec.id)
                    } else {
                        appliedRecommendations.add(rec.id)
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun RecommendationCardItem(
    recommendation: SavingsRecommendation,
    isApplied: Boolean,
    onToggleApply: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rec_card_${recommendation.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isApplied) Color(0xFFF1F8E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority Tag
                val (tagBg, tagText) = when (recommendation.urgencyLevel) {
                    "High" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
                    "Medium" -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
                    else -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(tagBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${recommendation.urgencyLevel} Priority",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = tagText
                    )
                }

                // Potential Savings Badge
                Text(
                    text = "+${formatCurrency(recommendation.potentialMonthlySavings)} / mo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = recommendation.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = FinFlowTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = recommendation.description,
                fontSize = 13.sp,
                color = FinFlowTextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action / Apply Button
            Button(
                onClick = onToggleApply,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isApplied) Color(0xFF2E7D32) else FinFlowPillDark,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Icon(
                    imageVector = if (isApplied) Icons.Default.CheckCircle else Icons.Default.Savings,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isApplied) "Applied to Next Month Budget" else recommendation.actionLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
