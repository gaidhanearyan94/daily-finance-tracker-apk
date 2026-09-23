package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val cardAccountDao: CardAccountDao
) {
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val allCards: Flow<List<CardAccountEntity>> = cardAccountDao.getAllCards()

    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesBetween(startTime, endTime)
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    suspend fun insertAllExpenses(expenses: List<ExpenseEntity>) {
        expenseDao.insertAll(expenses)
    }

    suspend fun insertAllCards(cards: List<CardAccountEntity>) {
        cardAccountDao.insertAllCards(cards)
    }

    suspend fun insertCard(card: CardAccountEntity): Long {
        return cardAccountDao.insertCard(card)
    }

    suspend fun updateCard(card: CardAccountEntity) {
        cardAccountDao.updateCard(card)
    }

    suspend fun deleteCard(id: Long) {
        cardAccountDao.deleteCardById(id)
    }

    suspend fun seedInitialDataIfEmpty(currentExpensesCount: Int, currentCardsCount: Int) {
        if (currentCardsCount == 0) {
            val initialCards = listOf(
                CardAccountEntity(
                    name = "Universal",
                    cardNumberMasked = "*9423",
                    expiry = "06/28",
                    balance = 8523.20,
                    creditLimit = 22000.00,
                    creditUsed = 0.00,
                    currency = "USD",
                    cardType = "VISA",
                    isPrimary = true
                ),
                CardAccountEntity(
                    name = "Platina",
                    cardNumberMasked = "*1104",
                    expiry = "09/27",
                    balance = 500.25,
                    creditLimit = 5000.00,
                    creditUsed = 120.00,
                    currency = "USD",
                    cardType = "Mastercard",
                    isPrimary = false
                )
            )
            cardAccountDao.insertAllCards(initialCards)
        }

        if (currentExpensesCount == 0) {
            val now = Calendar.getInstance()
            val seedExpenses = mutableListOf<ExpenseEntity>()

            // We generate transactions matching the reference UI (Paypal, Twitch, Airbnb, Dribbble)
            // along with daily transactions for the current month and previous month so analytics,
            // charts, and monthly comparison work immediately!
            
            // Current month recent items (matching image)
            val calToday = Calendar.getInstance()
            calToday.set(Calendar.HOUR_OF_DAY, 11)
            calToday.set(Calendar.MINUTE, 55)

            seedExpenses.add(
                ExpenseEntity(
                    title = "Paypal",
                    amount = 10.67,
                    category = "Shopping",
                    timestamp = calToday.timeInMillis,
                    account = "Universal Visa",
                    note = "Online purchase",
                    isRecurring = false
                )
            )

            val calTwitch = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, -4); set(Calendar.MINUTE, 50) }
            seedExpenses.add(
                ExpenseEntity(
                    title = "Twitch",
                    amount = 12.01,
                    category = "Entertainment",
                    timestamp = calTwitch.timeInMillis,
                    account = "Universal Visa",
                    note = "Channel subscription",
                    isRecurring = true
                )
            )

            val calAirbnb = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1); set(Calendar.HOUR_OF_DAY, 11); set(Calendar.MINUTE, 17) }
            seedExpenses.add(
                ExpenseEntity(
                    title = "Airbnb",
                    amount = 112.43,
                    category = "Travel",
                    timestamp = calAirbnb.timeInMillis,
                    account = "Universal Visa",
                    note = "Weekend getaway lodge",
                    isRecurring = false
                )
            )

            val calDribbble = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1); set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 35) }
            seedExpenses.add(
                ExpenseEntity(
                    title = "Dribbble",
                    amount = 16.00,
                    category = "Tech",
                    timestamp = calDribbble.timeInMillis,
                    account = "Universal Visa",
                    note = "Pro designer subscription",
                    isRecurring = true
                )
            )

            val calAirbnb2 = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -2); set(Calendar.HOUR_OF_DAY, 15); set(Calendar.MINUTE, 20) }
            seedExpenses.add(
                ExpenseEntity(
                    title = "Airbnb Deposit",
                    amount = 112.43,
                    category = "Travel",
                    timestamp = calAirbnb2.timeInMillis,
                    account = "Universal Visa",
                    note = "Reservation reservation balance",
                    isRecurring = false
                )
            )

            // Additional realistic entries across Food, Home goods, Clothing, Other for radar chart & pie chart
            val extraEntries = listOf(
                Triple("Whole Foods Market", 145.80, "Food"),
                Triple("Zara Summer Collection", 89.90, "Clothing"),
                Triple("IKEA Home Decor", 134.50, "Home goods"),
                Triple("Spotify Premium", 11.99, "Entertainment"),
                Triple("Uber Ride", 24.30, "Transport"),
                Triple("Starbucks Coffee", 6.75, "Food"),
                Triple("Apple iCloud", 2.99, "Tech"),
                Triple("Pharmacy & Health", 38.20, "Other"),
                Triple("Trader Joe's", 78.40, "Food"),
                Triple("Netflix UHD", 22.99, "Entertainment"),
                Triple("Uniqlo Essentials", 54.00, "Clothing"),
                Triple("Home Depot Tools", 65.20, "Home goods")
            )

            for (i in extraEntries.indices) {
                val (title, amount, cat) = extraEntries[i]
                val itemCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -(i + 2))
                    set(Calendar.HOUR_OF_DAY, 12 + (i % 8))
                    set(Calendar.MINUTE, 10 * (i % 6))
                }
                seedExpenses.add(
                    ExpenseEntity(
                        title = title,
                        amount = amount,
                        category = cat,
                        timestamp = itemCal.timeInMillis,
                        account = if (i % 3 == 0) "Platina" else "Universal Visa",
                        note = "Sample monthly expense",
                        isRecurring = cat == "Tech" || cat == "Entertainment"
                    )
                )
            }

            // Previous month seed entries to power Month-over-Month comparison and Savings Recommendation!
            val prevMonthCal = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
            }
            val prevEntries = listOf(
                Triple("Supermarket Groceries", 420.00, "Food"),
                Triple("Restaurants & Dining", 280.50, "Food"),
                Triple("Boutique Clothing", 195.00, "Clothing"),
                Triple("Living Room Furniture", 310.00, "Home goods"),
                Triple("Gym Membership", 65.00, "Other"),
                Triple("Subway & Metro Pass", 90.00, "Transport"),
                Triple("Adobe Creative Cloud", 54.99, "Tech"),
                Triple("PlayStation Plus", 17.99, "Entertainment"),
                Triple("Hulu & Disney+", 19.99, "Entertainment"),
                Triple("Gasoline Station", 140.00, "Transport"),
                Triple("Target Sundries", 110.25, "Home goods"),
                Triple("Online Gadget Store", 185.00, "Tech")
            )

            for (i in prevEntries.indices) {
                val (title, amount, cat) = prevEntries[i]
                val pCal = (prevMonthCal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 2 + (i * 2))
                    set(Calendar.HOUR_OF_DAY, 14)
                }
                seedExpenses.add(
                    ExpenseEntity(
                        title = title,
                        amount = amount,
                        category = cat,
                        timestamp = pCal.timeInMillis,
                        account = "Universal Visa",
                        note = "Previous month baseline",
                        isRecurring = cat == "Entertainment" || title.contains("Membership") || title.contains("Cloud")
                    )
                )
            }

            expenseDao.insertAll(seedExpenses)
        }
    }
}
