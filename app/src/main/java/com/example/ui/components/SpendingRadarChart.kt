package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CategorySpendItem
import com.example.ui.theme.FinFlowNeonLime
import com.example.ui.theme.FinFlowNeonLimeDark
import com.example.ui.theme.FinFlowTextPrimary
import com.example.ui.theme.FinFlowTextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpendingRadarChart(
    categorySpends: List<CategorySpendItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spending_radar_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: "Spending" + "• This month" indicator (exactly like image)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spending",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinFlowTextPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(FinFlowNeonLimeDark)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "This month",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = FinFlowTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The Radar Canvas
            RadarCanvas(
                categorySpends = categorySpends,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            )
        }
    }
}

@Composable
fun RadarCanvas(
    categorySpends: List<CategorySpendItem>,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(categorySpends) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    // Default categories if list is empty or small
    val displayCategories = if (categorySpends.isNotEmpty()) {
        categorySpends.take(6)
    } else {
        listOf(
            CategorySpendItem("Food", 350.0, 35f, 1, Color(0xFF91CE2C)),
            CategorySpendItem("Clothing", 140.0, 14f, 1, Color(0xFF5A9CE6)),
            CategorySpendItem("Home goods", 220.0, 22f, 1, Color(0xFFF2994A)),
            CategorySpendItem("Other", 80.0, 8f, 1, Color(0xFF78909C)),
            CategorySpendItem("Tech", 110.0, 11f, 1, Color(0xFF9B51E0)),
            CategorySpendItem("Transport", 100.0, 10f, 1, Color(0xFF00B4D8))
        )
    }

    val numAxes = displayCategories.size.coerceAtLeast(3)
    val maxSpend = (displayCategories.maxOfOrNull { it.totalAmount } ?: 100.0).coerceAtLeast(50.0)

    val gridWebColor = Color(0xFFE2E7DB)
    val axisLineColor = Color(0xFFD6DEC9)
    val neonLimeFill = FinFlowNeonLime.copy(alpha = 0.55f)
    val neonLimeStroke = Color(0xFF9DC417)

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = (size.width.coerceAtMost(size.height) / 2f) * 0.72f

        // Angle step for each category axis
        val angleStep = (2 * Math.PI / numAxes).toFloat()
        val startAngleOffset = (-Math.PI / 2).toFloat() // 12 o'clock

        // 1. Draw Concentric polygon rings (levels 0.25, 0.5, 0.75, 1.0)
        val ringLevels = listOf(0.33f, 0.66f, 1.0f)
        for (level in ringLevels) {
            val ringPath = Path()
            val ringR = radius * level
            for (i in 0 until numAxes) {
                val angle = startAngleOffset + i * angleStep
                val x = centerX + ringR * cos(angle)
                val y = centerY + ringR * sin(angle)
                if (i == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
            }
            ringPath.close()
            drawPath(
                path = ringPath,
                color = gridWebColor,
                style = Stroke(width = 1.2.dp.toPx())
            )
        }

        // 2. Draw Spokes / Axis lines from center to outer ring
        for (i in 0 until numAxes) {
            val angle = startAngleOffset + i * angleStep
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            drawLine(
                color = axisLineColor,
                start = Offset(centerX, centerY),
                end = Offset(x, y),
                strokeWidth = 1.2.dp.toPx()
            )

            // Draw Category Text Label using native Android canvas
            val labelRadius = radius + 18.dp.toPx()
            val labelX = centerX + labelRadius * cos(angle)
            val labelY = centerY + labelRadius * sin(angle)

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#33383F")
                textSize = 28f
                isAntiAlias = true
                textAlign = when {
                    cos(angle) > 0.25f -> android.graphics.Paint.Align.LEFT
                    cos(angle) < -0.25f -> android.graphics.Paint.Align.RIGHT
                    else -> android.graphics.Paint.Align.CENTER
                }
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }

            val categoryName = displayCategories[i].category
            drawContext.canvas.nativeCanvas.drawText(
                categoryName,
                labelX,
                labelY + 10f,
                textPaint
            )
        }

        // 3. Draw neon polygon of actual values
        val dataPath = Path()
        val dataPoints = mutableListOf<Offset>()

        for (i in 0 until numAxes) {
            val angle = startAngleOffset + i * angleStep
            val normalizedVal = ((displayCategories[i].totalAmount / maxSpend).toFloat()).coerceIn(0.2f, 1.0f)
            val currentRadius = radius * normalizedVal * animProgress.value

            val x = centerX + currentRadius * cos(angle)
            val y = centerY + currentRadius * sin(angle)
            dataPoints.add(Offset(x, y))

            if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
        }
        dataPath.close()

        // Fill polygon
        drawPath(
            path = dataPath,
            color = neonLimeFill,
            style = Fill
        )

        // Outline polygon
        drawPath(
            path = dataPath,
            color = neonLimeStroke,
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Draw small vertices
        for (point in dataPoints) {
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = neonLimeStroke,
                radius = 4.dp.toPx(),
                center = point,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
