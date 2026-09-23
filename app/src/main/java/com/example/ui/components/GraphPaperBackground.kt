package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FinFlowGridLine

@Composable
fun GraphPaperBackground(
    modifier: Modifier = Modifier,
    gridSizeDp: Float = 28f,
    lineColor: Color = FinFlowGridLine,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stepPx = gridSizeDp.dp.toPx()
            val width = size.width
            val height = size.height

            // Vertical lines
            var x = 0f
            while (x <= width) {
                drawLine(
                    color = lineColor.copy(alpha = 0.5f),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += stepPx
            }

            // Horizontal lines
            var y = 0f
            while (y <= height) {
                drawLine(
                    color = lineColor.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += stepPx
            }
        }
        content()
    }
}
