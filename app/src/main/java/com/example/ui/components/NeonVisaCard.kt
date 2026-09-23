package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardAccountEntity
import com.example.domain.formatCurrency
import com.example.ui.theme.FinFlowNeonLime
import com.example.ui.theme.FinFlowPillDark
import com.example.ui.theme.FinFlowTextPrimary
import com.example.ui.theme.FinFlowTextSecondary

@Composable
fun NeonVisaCard(
    card: CardAccountEntity,
    secondaryCard: CardAccountEntity?,
    onAddCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("neon_visa_card_container"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Secondary card mini-tab header if available (matching "Platina $500.25" in design)
            if (secondaryCard != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5E9))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = secondaryCard.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = FinFlowTextSecondary
                    )
                    Text(
                        text = formatCurrency(secondaryCard.balance),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FinFlowTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Main Vibrant Neon Lime Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(FinFlowNeonLime)
                    .padding(20.dp)
            ) {
                Column {
                    // Top row: Universal name + EMV Chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = card.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinFlowTextPrimary
                        )

                        // Stylized EMV Microchip
                        EmvChipGraphic(modifier = Modifier.size(width = 38.dp, height = 28.dp))
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Center Balance
                    Text(
                        text = formatCurrency(card.balance),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = FinFlowTextPrimary
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Bottom Row: Masked Number + Expiry + VISA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = card.cardNumberMasked,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FinFlowTextPrimary
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = card.expiry,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = FinFlowTextPrimary.copy(alpha = 0.8f)
                            )
                        }

                        Text(
                            text = card.cardType,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.SansSerif,
                            color = FinFlowTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card statistics
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Credit limit",
                        fontSize = 13.sp,
                        color = FinFlowTextSecondary
                    )
                    Text(
                        text = formatCurrency(card.creditLimit),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FinFlowTextPrimary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Credit used",
                        fontSize = 13.sp,
                        color = FinFlowTextSecondary
                    )
                    Text(
                        text = formatCurrency(card.creditUsed),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FinFlowTextPrimary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Currency",
                        fontSize = 13.sp,
                        color = FinFlowTextSecondary
                    )
                    Text(
                        text = card.currency,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FinFlowTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dark "+ Add new card" Button matching design
            Button(
                onClick = onAddCardClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_card_button"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FinFlowPillDark,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add new card",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun EmvChipGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Chip metallic background
        drawRoundRect(
            color = Color(0xFFC9CDD4),
            size = Size(w, h),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )

        // Chip circuit etch lines
        val strokeColor = Color(0xFF9AA0A6)
        val strokeW = 1.5.dp.toPx()

        // Center oval
        drawRoundRect(
            color = strokeColor,
            topLeft = Offset(w * 0.25f, h * 0.25f),
            size = Size(w * 0.5f, h * 0.5f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
        )

        // Cross lines
        drawLine(
            color = strokeColor,
            start = Offset(0f, h * 0.5f),
            end = Offset(w * 0.25f, h * 0.5f),
            strokeWidth = strokeW
        )
        drawLine(
            color = strokeColor,
            start = Offset(w * 0.75f, h * 0.5f),
            end = Offset(w, h * 0.5f),
            strokeWidth = strokeW
        )
    }
}
