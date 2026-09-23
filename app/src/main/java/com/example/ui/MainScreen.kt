package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddCardDialog
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.GraphPaperBackground
import androidx.compose.material.icons.filled.CloudSync
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.CardsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SavingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.FinFlowNeonLime
import com.example.ui.theme.FinFlowPillDark
import com.example.ui.theme.FinFlowPillDarkElevated

enum class AppNavTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "nav_dashboard"),
    REPORTS("Reports", Icons.Default.DonutLarge, "nav_reports"),
    SAVINGS("Savings", Icons.Default.AutoAwesome, "nav_savings"),
    TRANSACTIONS("Expenses", Icons.Default.ReceiptLong, "nav_transactions"),
    CARDS("Cards", Icons.Default.CreditCard, "nav_cards"),
    ACCOUNT("Cloud", Icons.Default.CloudSync, "nav_account")
}

@Composable
fun MainScreen(viewModel: ExpenseViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(AppNavTab.DASHBOARD) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddCardDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 650.dp

        GraphPaperBackground {
            if (isWideScreen) {
                // Wide Screen Layout: Left Navigation Capsule Rail (exact match to Ronas IT sidebar!)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    DarkCapsuleNavRail(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it }
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        TabContent(
                            currentTab = currentTab,
                            uiState = uiState,
                            viewModel = viewModel,
                            isCloudConnected = currentUser != null,
                            onNavigateToTransactions = { currentTab = AppNavTab.TRANSACTIONS },
                            onNavigateToAccount = { currentTab = AppNavTab.ACCOUNT },
                            onOpenAddExpense = { showAddExpenseDialog = true },
                            onOpenAddCard = { showAddCardDialog = true }
                        )
                    }
                }
            } else {
                // Compact / Phone Layout: Content with floating bottom capsule nav bar
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    TabContent(
                        currentTab = currentTab,
                        uiState = uiState,
                        viewModel = viewModel,
                        isCloudConnected = currentUser != null,
                        onNavigateToTransactions = { currentTab = AppNavTab.TRANSACTIONS },
                        onNavigateToAccount = { currentTab = AppNavTab.ACCOUNT },
                        onOpenAddExpense = { showAddExpenseDialog = true },
                        onOpenAddCard = { showAddCardDialog = true }
                    )

                    // Floating Dark Capsule Navigation Bar at bottom
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 12.dp, start = 16.dp, end = 16.dp)
                    ) {
                        DarkCapsuleBottomBar(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it }
                        )
                    }
                }
            }
        }
    }

    if (showAddExpenseDialog) {
        val accountNames = uiState.cards.map { it.name }.ifEmpty { listOf("Universal Visa", "Platina", "Cash") }
        AddExpenseDialog(
            accounts = accountNames,
            onDismiss = { showAddExpenseDialog = false },
            onSave = { expense ->
                viewModel.addExpense(expense)
                showAddExpenseDialog = false
            }
        )
    }

    if (showAddCardDialog) {
        AddCardDialog(
            onDismiss = { showAddCardDialog = false },
            onSave = { card ->
                viewModel.addCard(card)
                showAddCardDialog = false
            }
        )
    }
}

@Composable
fun TabContent(
    currentTab: AppNavTab,
    uiState: ExpenseUiState,
    viewModel: ExpenseViewModel,
    isCloudConnected: Boolean,
    onNavigateToTransactions: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenAddCard: () -> Unit
) {
    Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
        when (tab) {
            AppNavTab.DASHBOARD -> DashboardScreen(
                monthlySummary = uiState.monthlySummary,
                recentExpenses = uiState.expenses,
                cards = uiState.cards,
                selectedCalendar = uiState.selectedCalendar,
                onMonthChange = { offset ->
                    if (offset == 0) viewModel.setTargetCalendar(java.util.Calendar.getInstance())
                    else viewModel.setMonthOffset(offset)
                },
                onViewAllTransactions = onNavigateToTransactions,
                onAddExpenseClick = onOpenAddExpense,
                onAddCardClick = onOpenAddCard,
                isCloudConnected = isCloudConnected,
                onAccountClick = onNavigateToAccount
            )

            AppNavTab.REPORTS -> ReportsScreen(
                monthlySummary = uiState.monthlySummary,
                onPrevMonth = { viewModel.setMonthOffset(-1) },
                onNextMonth = { viewModel.setMonthOffset(1) }
            )

            AppNavTab.SAVINGS -> SavingsScreen(
                savingsReport = uiState.savingsReport,
                savingsTargetPercent = uiState.savingsTargetPercent,
                onTargetPercentChange = { viewModel.setSavingsTargetPercent(it) }
            )

            AppNavTab.TRANSACTIONS -> TransactionsScreen(
                expenses = uiState.expenses,
                searchQuery = uiState.searchQuery,
                onSearchChange = { viewModel.setSearchQuery(it) },
                selectedCategoryFilter = uiState.selectedCategoryFilter,
                onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                onDeleteExpense = { viewModel.deleteExpense(it) },
                onAddExpenseClick = onOpenAddExpense
            )

            AppNavTab.CARDS -> CardsScreen(
                cards = uiState.cards,
                onAddCardClick = onOpenAddCard
            )

            AppNavTab.ACCOUNT -> AccountScreen(
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun DarkCapsuleBottomBar(
    currentTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(FinFlowPillDark)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("dark_capsule_bottom_bar"),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppNavTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isSelected) FinFlowPillDarkElevated else Color.Transparent)
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag(tab.tag),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) FinFlowNeonLime else Color(0xFF8C959F),
                        modifier = Modifier.size(20.dp)
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DarkCapsuleNavRail(
    currentTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(FinFlowPillDark)
            .padding(vertical = 20.dp, horizontal = 12.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Top circular logo with neon lime ring (matching screenshot top logo)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22262B)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(FinFlowNeonLime)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Navigation icons
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AppNavTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) FinFlowPillDarkElevated else Color.Transparent)
                            .clickable { onTabSelected(tab) }
                            .testTag(tab.tag),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) FinFlowNeonLime else Color(0xFF8C959F),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
