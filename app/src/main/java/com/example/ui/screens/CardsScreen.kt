package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardAccountEntity
import com.example.domain.formatCurrency
import com.example.ui.components.NeonVisaCard
import com.example.ui.theme.FinFlowNeonLime
import com.example.ui.theme.FinFlowTextPrimary
import com.example.ui.theme.FinFlowTextSecondary

@Composable
fun CardsScreen(
    cards: List<CardAccountEntity>,
    onAddCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalBalance = cards.sumOf { it.balance }
    val totalCreditLimit = cards.sumOf { it.creditLimit }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cards_screen_column"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cards & Accounts",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = FinFlowTextPrimary,
                        modifier = Modifier.testTag("cards_screen_title")
                    )
                    Text(
                        text = "Total Liquidity: ${formatCurrency(totalBalance)} · Credit: ${formatCurrency(totalCreditLimit)}",
                        fontSize = 13.sp,
                        color = FinFlowTextSecondary
                    )
                }

                Button(
                    onClick = onAddCardClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FinFlowNeonLime,
                        contentColor = FinFlowTextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "New Card", fontWeight = FontWeight.Bold)
                }
            }
        }

        items(cards, key = { it.id }) { card ->
            NeonVisaCard(
                card = card,
                secondaryCard = null,
                onAddCardClick = onAddCardClick
            )
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
