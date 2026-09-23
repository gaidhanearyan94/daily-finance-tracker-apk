package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.DailySpendPoint
import com.example.domain.formatCurrency
import com.example.ui.theme.FinFlowNeonLime
import com.example.ui.theme.FinFlowPillDark
import com.example.ui.theme.FinFlowTextPrimary
import com.example.ui.theme.FinFlowTextSecondary

@Composable
fun SpendingTrendCurve(
    dailyPoints: List<DailySpendPoint>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember(dailyPoints) {
        val peakIdx = dailyPoints.indices.maxByOrNull { dailyPoints[it].amount } ?: (dailyPoints.size / 2)
        mutableStateOf(peakIdx)
    }

    val selectedPoint = dailyPoints.getOrNull(selectedIndex) ?: dailyPoints.lastOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("budget_trend_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: "Budget" + Selected Date Spend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budget",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinFlowTextPrimary
                )

                if (selectedPoint != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(FinFlowPillDark)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(FinFlowNeonLime)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${selectedPoint.dateLabel}: ${formatCurrency(selectedPoint.amount)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas with Y-axis grid & smooth curve
            TrendSplineCanvas(
                points = dailyPoints,
                selectedIndex = selectedIndex,
                onPointSelected = { selectedIndex = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom X-axis labels (5 sample dates matching reference UI: "18 dec", "25 dec", "1 jan", "8 jan", "15 jan")
            if (dailyPoints.isNotEmpty()) {
                val step = (dailyPoints.size / 4).coerceAtLeast(1)
                val keyPoints = listOf(
                    0,
                    step.coerceAtMost(dailyPoints.lastIndex),
                    (step * 2).coerceAtMost(dailyPoints.lastIndex),
                    (step * 3).coerceAtMost(dailyPoints.lastIndex),
                    dailyPoints.lastIndex
                ).distinct()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    keyPoints.forEach { idx ->
                        Text(
                            text = dailyPoints[idx].dateLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = FinFlowTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrendSplineCanvas(
    points: List<DailySpendPoint>,
    selectedIndex: Int,
    onPointSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val maxAmount = (points.maxOfOrNull { it.amount } ?: 100.0).coerceAtLeast(100.0)

    Canvas(
        modifier = modifier.pointerInput(points) {
            detectTapGestures { tapOffset ->
                val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                val tappedIdx = ((tapOffset.x + stepX / 2f) / stepX).toInt().coerceIn(0, points.lastIndex)
                onPointSelected(tappedIdx)
            }
        }
    ) {
        val width = size.width
        val height = size.height
        val paddingBottom = 16.dp.toPx()
        val usableHeight = height - paddingBottom

        // Draw horizontal grid guide lines (4 levels)
        val gridColor = Color(0xFFF0F4E8)
        val levels = listOf(0.25f, 0.5f, 0.75f, 1f)
        for (lvl in levels) {
            val y = usableHeight * (1f - lvl)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Compute coordinates of points
        val stepX = width / (points.size - 1).coerceAtLeast(1)
        val coords = points.mapIndexed { idx, pt ->
            val x = idx * stepX
            val normalized = (pt.amount / maxAmount).toFloat().coerceIn(0.08f, 0.95f)
            val y = usableHeight * (1f - normalized)
            Offset(x, y)
        }

        // Draw smooth cubic Bézier spline
        val curvePath = Path().apply {
            moveTo(coords.first().x, coords.first().y)
            for (i in 0 until coords.size - 1) {
                val p0 = coords[i]
                val p1 = coords[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                val controlY1 = p0.y
                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                val controlY2 = p1.y
                cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
            }
        }

        // Draw black spline line (just like in the Ronas IT mockup)
        drawPath(
            path = curvePath,
            color = Color(0xFF22262B),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw active highlighted dot on selected point
        if (selectedIndex in coords.indices) {
            val selectedCoord = coords[selectedIndex]

            // Vertical indicator guide
            drawLine(
                color = Color(0xFF22262B).copy(alpha = 0.3f),
                start = Offset(selectedCoord.x, 0f),
                end = Offset(selectedCoord.x, usableHeight),
                strokeWidth = 1.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Outer ring
            drawCircle(
                color = FinFlowNeonLime,
                radius = 8.dp.toPx(),
                center = selectedCoord
            )
            // Inner black dot
            drawCircle(
                color = Color(0xFF131518),
                radius = 4.5.dp.toPx(),
                center = selectedCoord
            )
        }
    }
}
