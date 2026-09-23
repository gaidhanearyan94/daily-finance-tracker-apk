package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CardAccountEntity
import com.example.data.ExpenseEntity
import com.example.data.ExpenseRepository
import com.example.data.firebase.FirebaseManager
import com.example.data.firebase.SyncState
import com.example.domain.MonthlyReportCalculator
import com.example.domain.MonthlySummary
import com.example.domain.SavingsAdvisor
import com.example.domain.SavingsAnalysisReport
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class ExpenseUiState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val cards: List<CardAccountEntity> = emptyList(),
    val selectedCalendar: Calendar = Calendar.getInstance(),
    val monthlySummary: MonthlySummary? = null,
    val savingsReport: SavingsAnalysisReport? = null,
    val savingsTargetPercent: Float = 15f,
    val searchQuery: String = "",
    val selectedCategoryFilter: String? = null,
    val isLoading: Boolean = true
)

private data class FilterSettings(
    val cal: Calendar,
    val targetPct: Float,
    val query: String,
    val catFilter: String?
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    val firebaseManager = FirebaseManager(application)

    val currentUser: StateFlow<FirebaseUser?> = firebaseManager.currentUser
    val syncState: StateFlow<SyncState> = firebaseManager.syncState
    val syncMessage: StateFlow<String> = firebaseManager.syncMessage
    val lastSyncTime: StateFlow<Long?> = firebaseManager.lastSyncTime

    private val _selectedCalendar = MutableStateFlow(Calendar.getInstance())
    val selectedCalendar: StateFlow<Calendar> = _selectedCalendar

    private val _savingsTargetPercent = MutableStateFlow(15f)
    val savingsTargetPercent: StateFlow<Float> = _savingsTargetPercent

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter

    val uiState: StateFlow<ExpenseUiState>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(db.expenseDao(), db.cardAccountDao())

        val dataFlow = combine(repository.allExpenses, repository.allCards) { expenses, cards ->
            expenses to cards
        }

        val settingsFlow = combine(
            _selectedCalendar,
            _savingsTargetPercent,
            _searchQuery,
            _selectedCategoryFilter
        ) { cal, targetPct, query, catFilter ->
            FilterSettings(cal, targetPct, query, catFilter)
        }

        uiState = combine(dataFlow, settingsFlow) { (expenses, cards), settings ->
            val summary = MonthlyReportCalculator.calculateSummary(expenses, settings.cal)
            val savingsReport = SavingsAdvisor.generateRecommendations(expenses, settings.cal, settings.targetPct)

            ExpenseUiState(
                expenses = expenses,
                cards = cards,
                selectedCalendar = settings.cal,
                monthlySummary = summary,
                savingsReport = savingsReport,
                savingsTargetPercent = settings.targetPct,
                searchQuery = settings.query,
                selectedCategoryFilter = settings.catFilter,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExpenseUiState()
        )

        // Seed initial mock data if empty
        viewModelScope.launch {
            repository.allExpenses.collect { expenses ->
                if (expenses.isEmpty()) {
                    repository.seedInitialDataIfEmpty(0, 0)
                }
            }
        }

        // Monitor auth changes to set up cloud sync
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    // Sync local data to Firestore and attach real-time listeners
                    syncLocalAndCloud()
                    firebaseManager.attachCloudListeners(
                        onExpensesChanged = { cloudExpenses ->
                            viewModelScope.launch {
                                repository.insertAllExpenses(cloudExpenses)
                            }
                        },
                        onCardsChanged = { cloudCards ->
                            viewModelScope.launch {
                                repository.insertAllCards(cloudCards)
                            }
                        }
                    )
                } else {
                    firebaseManager.detachCloudListeners()
                }
            }
        }
    }

    fun syncLocalAndCloud() {
        val currentExpenses = uiState.value.expenses
        val currentCards = uiState.value.cards
        firebaseManager.syncAllToCloud(currentExpenses, currentCards)
    }

    fun pullCloudData(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        firebaseManager.fetchCloudData(
            onSuccess = { cloudExpenses, cloudCards ->
                viewModelScope.launch {
                    if (cloudExpenses.isNotEmpty()) {
                        repository.insertAllExpenses(cloudExpenses)
                    }
                    if (cloudCards.isNotEmpty()) {
                        repository.insertAllCards(cloudCards)
                    }
                    onComplete(true, "Pulled ${cloudExpenses.size} expenses & ${cloudCards.size} cards")
                }
            },
            onError = { err ->
                onComplete(false, err)
            }
        )
    }

    fun addExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            val generatedId = repository.insertExpense(expense)
            val savedExpense = if (expense.id == 0L) expense.copy(id = generatedId) else expense
            
            // Sync to Firestore if authenticated
            if (currentUser.value != null) {
                firebaseManager.syncSingleExpense(savedExpense)
            }

            // Update card balance if tied to a card
            val currentCards = uiState.value.cards
            val card = currentCards.find { it.name.equals(expense.account, ignoreCase = true) }
            if (card != null) {
                val updatedCard = card.copy(
                    balance = (card.balance - expense.amount).coerceAtLeast(0.0),
                    creditUsed = card.creditUsed + expense.amount
                )
                repository.updateCard(updatedCard)
                if (currentUser.value != null) {
                    firebaseManager.syncSingleCard(updatedCard)
                }
            }
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpense(id)
            if (currentUser.value != null) {
                firebaseManager.deleteExpenseFromCloud(id)
            }
        }
    }

    fun addCard(card: CardAccountEntity) {
        viewModelScope.launch {
            val generatedId = repository.insertCard(card)
            val savedCard = if (card.id == 0L) card.copy(id = generatedId) else card
            if (currentUser.value != null) {
                firebaseManager.syncSingleCard(savedCard)
            }
        }
    }

    // ----------------------------------------------------
    // AUTH ACTIONS
    // ----------------------------------------------------

    fun signInWithEmail(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseManager.signInWithEmail(email, pass, onSuccess, onError)
    }

    fun signUpWithEmail(email: String, pass: String, displayName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseManager.signUpWithEmail(email, pass, displayName, onSuccess, onError)
    }

    fun sendPhoneVerification(activity: Activity, phone: String, onCodeSent: () -> Unit, onError: (String) -> Unit) {
        firebaseManager.startPhoneNumberVerification(activity, phone, onCodeSent, onError)
    }

    fun verifyPhoneCode(code: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseManager.verifyPhoneCode(code, onSuccess, onError)
    }

    fun signInWithGoogle(activity: Activity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = firebaseManager.signInWithGoogleCredentialManager(activity)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.localizedMessage ?: "Google sign-in was canceled or failed")
            }
        }
    }

    fun signOut() {
        firebaseManager.signOut()
    }

    // ----------------------------------------------------
    // NAVIGATION & FILTERS
    // ----------------------------------------------------

    fun setMonthOffset(offset: Int) {
        val newCal = (_selectedCalendar.value.clone() as Calendar).apply {
            add(Calendar.MONTH, offset)
        }
        _selectedCalendar.value = newCal
    }

    fun setTargetCalendar(cal: Calendar) {
        _selectedCalendar.value = cal
    }

    fun setSavingsTargetPercent(percent: Float) {
        _savingsTargetPercent.value = percent
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }
}
