package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CategorySpendItem
import com.example.domain.formatCurrency
import com.example.ui.theme.FinFlowNeonLime
import com.example.ui.theme.FinFlowTextPrimary
import com.example.ui.theme.FinFlowTextSecondary
import kotlin.math.atan2

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractivePieChart(
    categorySpends: List<CategorySpendItem>,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember { mutableStateOf<Int?>(null) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(categorySpends) {
        selectedCategoryIndex = null
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
    }

    val selectedCategory = selectedCategoryIndex?.let { idx ->
        if (idx in categorySpends.indices) categorySpends[idx] else null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_pie_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expense Breakdown",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinFlowTextPrimary
                )
                Text(
                    text = "Tap slice to inspect",
                    fontSize = 12.sp,
                    color = FinFlowTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Donut Pie Canvas with Center Information
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                DonutCanvas(
                    categorySpends = categorySpends,
                    selectedIndex = selectedCategoryIndex,
                    animProgress = animProgress.value,
                    onSliceSelected = { idx ->
                        selectedCategoryIndex = if (selectedCategoryIndex == idx) null else idx
                    },
                    modifier = Modifier.size(220.dp)
                )

                // Center info display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (selectedCategory != null) {
                        Text(
                            text = selectedCategory.category,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = FinFlowTextSecondary
                        )
                        Text(
                            text = formatCurrency(selectedCategory.totalAmount),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinFlowTextPrimary
                        )
                        Text(
                            text = "${String.format("%.1f", selectedCategory.percentage)}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = selectedCategory.color
                        )
                    } else {
                        Text(
                            text = "Total Spent",
                            fontSize = 12.sp,
                            color = FinFlowTextSecondary
                        )
                        Text(
                            text = formatCurrency(totalExpense),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinFlowTextPrimary
                        )
                        Text(
                            text = "${categorySpends.size} categories",
                            fontSize = 11.sp,
                            color = FinFlowTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categorySpends.forEachIndexed { index, item ->
                    val isSelected = selectedCategoryIndex == index
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) FinFlowNeonLime.copy(alpha = 0.35f) else Color(0xFFF4F6EE))
                            .clickable {
                                selectedCategoryIndex = if (isSelected) null else index
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(item.color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${item.category} ${String.format("%.0f", item.percentage)}%",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = FinFlowTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DonutCanvas(
    categorySpends: List<CategorySpendItem>,
    selectedIndex: Int?,
    animProgress: Float,
    onSliceSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalAmount = categorySpends.sumOf { it.totalAmount }

    Canvas(
        modifier = modifier.pointerInput(categorySpends) {
            detectTapGestures { tapOffset ->
                if (totalAmount <= 0) return@detectTapGestures
                val center = Offset(size.width / 2f, size.height / 2f)
                val dx = tapOffset.x - center.x
                val dy = tapOffset.y - center.y
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                val outerRadius = size.width / 2f
                val innerRadius = outerRadius * 0.58f

                if (distance in innerRadius..outerRadius) {
                    var angle = (Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 360f) % 360f
                    // Start angle offset is -90 deg (270)
                    angle = (angle - 270f + 360f) % 360f

                    var currentAngle = 0f
                    categorySpends.forEachIndexed { idx, item ->
                        val sweep = ((item.totalAmount / totalAmount).toFloat()) * 360f
                        if (angle >= currentAngle && angle <= currentAngle + sweep) {
                            onSliceSelected(idx)
                            return@detectTapGestures
                        }
                        currentAngle += sweep
                    }
                }
            }
        }
    ) {
        val strokeWidth = 36.dp.toPx()
        val diameter = size.width - strokeWidth
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        val arcSize = Size(diameter, diameter)

        if (totalAmount <= 0) {
            // Draw empty placeholder ring
            drawArc(
                color = Color(0xFFE2E7DB),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            return@Canvas
        }

        var startAngle = -90f
        categorySpends.forEachIndexed { index, item ->
            val sweep = ((item.totalAmount / totalAmount).toFloat()) * 360f * animProgress
            val isSelected = selectedIndex == index
            val currentStroke = if (isSelected) strokeWidth + 6.dp.toPx() else strokeWidth

            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweep - 1.5f, // slight gap
                useCenter = false,
                topLeft = if (isSelected) Offset(topLeft.x - 3.dp.toPx(), topLeft.y - 3.dp.toPx()) else topLeft,
                size = if (isSelected) Size(arcSize.width + 6.dp.toPx(), arcSize.height + 6.dp.toPx()) else arcSize,
                style = Stroke(width = currentStroke)
            )
            startAngle += sweep
        }
    }
}
