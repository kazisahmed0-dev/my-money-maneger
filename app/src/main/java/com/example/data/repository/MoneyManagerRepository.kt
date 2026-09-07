package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MoneyManagerRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val transactionDao = db.transactionDao()
    private val categoryDao = db.categoryDao()
    private val savingsGoalDao = db.savingsGoalDao()
    private val budgetDao = db.budgetDao()
    private val recurringDao = db.recurringDao()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("money_manager_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    fun getTransactionsByMonth(yearMonth: String): Flow<List<TransactionEntity>> = transactionDao.getTransactionsByMonth(yearMonth)
    fun getTransactionsByYear(year: String): Flow<List<TransactionEntity>> = transactionDao.getTransactionsByYear(year)
    fun getRecentTransactionTitles(type: String): Flow<List<String>> = transactionDao.getRecentTransactionTitles(type)
    fun getFavoriteTransactions(): Flow<List<TransactionEntity>> = transactionDao.getFavoriteTransactions()

    suspend fun getTransactionById(id: Long) = transactionDao.getTransactionById(id)
    suspend fun insertTransaction(transaction: TransactionEntity) = transactionDao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: TransactionEntity) = transactionDao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: TransactionEntity) = transactionDao.deleteTransaction(transaction)
    suspend fun deleteTransactionById(id: Long) = transactionDao.deleteTransactionById(id)
    suspend fun deleteMonthData(yearMonth: String) = transactionDao.deleteTransactionsByMonth(yearMonth)
    suspend fun deleteSampleTransactions() = transactionDao.deleteSampleTransactions()

    // Categories
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>> = categoryDao.getCategoriesByType(type)
    suspend fun insertCategory(category: CategoryEntity) = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)
    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    // Goals
    fun getAllGoals(): Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoals()
    suspend fun insertGoal(goal: SavingsGoalEntity) = savingsGoalDao.insertGoal(goal)
    suspend fun updateGoal(goal: SavingsGoalEntity) = savingsGoalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: SavingsGoalEntity) = savingsGoalDao.deleteGoal(goal)
    suspend fun addMoneyToGoal(goalId: Long, amountToAdd: Double) {
        val goal = savingsGoalDao.getGoalById(goalId) ?: return
        val updated = goal.copy(savedAmount = goal.savedAmount + amountToAdd)
        savingsGoalDao.updateGoal(updated)
    }

    // Budgets
    fun getAllBudgets(): Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    fun getBudgetsForMonth(yearMonth: String): Flow<List<BudgetEntity>> = budgetDao.getBudgetsForMonth(yearMonth)
    suspend fun insertBudget(budget: BudgetEntity) = budgetDao.insertBudget(budget)
    suspend fun updateBudget(budget: BudgetEntity) = budgetDao.updateBudget(budget)
    suspend fun deleteBudget(budget: BudgetEntity) = budgetDao.deleteBudget(budget)

    // Recurring
    fun getAllRecurring(): Flow<List<RecurringTransactionEntity>> = recurringDao.getAllRecurring()
    suspend fun insertRecurring(recurring: RecurringTransactionEntity) = recurringDao.insertRecurring(recurring)
    suspend fun updateRecurring(recurring: RecurringTransactionEntity) = recurringDao.updateRecurring(recurring)
    suspend fun deleteRecurring(recurring: RecurringTransactionEntity) = recurringDao.deleteRecurring(recurring)

    // User Profile
    private fun loadUserProfile(): UserProfile {
        return UserProfile(
            name = prefs.getString("user_name", "") ?: "",
            incomeTarget = prefs.getFloat("income_target", 0f).toDouble(),
            spendingBudget = prefs.getFloat("spending_budget", 0f).toDouble(),
            savingsGoal = prefs.getFloat("savings_goal", 0f).toDouble(),
            currency = prefs.getString("currency", "৳") ?: "৳",
            language = prefs.getString("language", "en") ?: "en",
            themeMode = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM",
            isPinEnabled = prefs.getBoolean("pin_enabled", false),
            pinCode = prefs.getString("pin_code", "") ?: "",
            notificationsEnabled = prefs.getBoolean("notifications_enabled", true),
            isOnboarded = prefs.getBoolean("is_onboarded", false)
        )
    }

    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        prefs.edit()
            .putString("user_name", profile.name)
            .putFloat("income_target", profile.incomeTarget.toFloat())
            .putFloat("spending_budget", profile.spendingBudget.toFloat())
            .putFloat("savings_goal", profile.savingsGoal.toFloat())
            .putString("currency", profile.currency)
            .putString("language", profile.language)
            .putString("theme_mode", profile.themeMode)
            .putBoolean("pin_enabled", profile.isPinEnabled)
            .putString("pin_code", profile.pinCode)
            .putBoolean("notifications_enabled", profile.notificationsEnabled)
            .putBoolean("is_onboarded", profile.isOnboarded)
            .apply()
        _userProfile.value = profile
    }

    suspend fun initDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
        if (categoryDao.getCategoryCount() == 0) {
            val defaults = listOf(
                // Income Categories
                CategoryEntity(name = "Salary", type = "INCOME", iconName = "work"),
                CategoryEntity(name = "Tuition", type = "INCOME", iconName = "school"),
                CategoryEntity(name = "Freelancing", type = "INCOME", iconName = "laptop"),
                CategoryEntity(name = "Business", type = "INCOME", iconName = "store"),
                CategoryEntity(name = "Bonus", type = "INCOME", iconName = "redeem"),
                CategoryEntity(name = "Gift", type = "INCOME", iconName = "card_giftcard"),
                CategoryEntity(name = "Investments", type = "INCOME", iconName = "trending_up"),
                CategoryEntity(name = "Other Income", type = "INCOME", iconName = "attach_money"),

                // Expense Categories
                CategoryEntity(name = "Food", type = "EXPENSE", iconName = "restaurant"),
                CategoryEntity(name = "Transport", type = "EXPENSE", iconName = "directions_bus"),
                CategoryEntity(name = "Shopping", type = "EXPENSE", iconName = "shopping_bag"),
                CategoryEntity(name = "Education", type = "EXPENSE", iconName = "menu_book"),
                CategoryEntity(name = "Internet", type = "EXPENSE", iconName = "wifi"),
                CategoryEntity(name = "Mobile Recharge", type = "EXPENSE", iconName = "phone_android"),
                CategoryEntity(name = "Rent", type = "EXPENSE", iconName = "home"),
                CategoryEntity(name = "Entertainment", type = "EXPENSE", iconName = "movie"),
                CategoryEntity(name = "Bills", type = "EXPENSE", iconName = "receipt_long"),
                CategoryEntity(name = "Health", type = "EXPENSE", iconName = "local_hospital"),
                CategoryEntity(name = "Other Expense", type = "EXPENSE", iconName = "more_horiz")
            )
            categoryDao.insertAll(defaults)
        }
    }

    suspend fun insertSampleData() = withContext(Dispatchers.IO) {
        val today = DateUtils.todayDateString()
        val day = DateUtils.calculateDayOfWeek(today)
        val time = DateUtils.currentTimeString()

        // Income: Tuition ৳5,000, Freelancing ৳8,000
        transactionDao.insertTransaction(
            TransactionEntity(
                title = "Tuition",
                amount = 5000.0,
                type = "INCOME",
                category = "Tuition",
                date = today,
                dayOfWeek = day,
                time = time,
                note = "September tuition payment",
                isSample = true,
                isFavorite = true
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                title = "Freelancing",
                amount = 8000.0,
                type = "INCOME",
                category = "Freelancing",
                date = today,
                dayOfWeek = day,
                time = time,
                note = "Mobile app project milestone",
                isSample = true,
                isFavorite = true
            )
        )

        // Expense: Food ৳500, Transport ৳300
        transactionDao.insertTransaction(
            TransactionEntity(
                title = "Food",
                amount = 500.0,
                type = "EXPENSE",
                category = "Food",
                date = today,
                dayOfWeek = day,
                time = time,
                note = "Lunch with team",
                isSample = true,
                isFavorite = true
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                title = "Transport",
                amount = 300.0,
                type = "EXPENSE",
                category = "Transport",
                date = today,
                dayOfWeek = day,
                time = time,
                note = "Bus & rickshaw fare",
                isSample = true,
                isFavorite = true
            )
        )

        // Savings: Laptop Fund ৳3,000
        savingsGoalDao.insertGoal(
            SavingsGoalEntity(
                title = "Buy Laptop",
                targetAmount = 80000.0,
                savedAmount = 30000.0,
                targetDate = "2026-12-31",
                note = "Upgrading work laptop"
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                title = "Laptop Fund",
                amount = 3000.0,
                type = "SAVING",
                category = "Savings",
                date = today,
                dayOfWeek = day,
                time = time,
                note = "Monthly contribution for laptop",
                isSample = true
            )
        )

        // Sample budgets
        budgetDao.insertBudget(BudgetEntity(category = "Food", monthlyLimit = 5000.0))
        budgetDao.insertBudget(BudgetEntity(category = "Transport", monthlyLimit = 3000.0))
        budgetDao.insertBudget(BudgetEntity(category = "OVERALL", monthlyLimit = 25000.0))
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        transactionDao.deleteAllTransactions()
        savingsGoalDao.deleteAllGoals()
        budgetDao.deleteAllBudgets()
        recurringDao.deleteAllRecurring()
    }

    // Process recurring transactions
    suspend fun processRecurringTransactions() = withContext(Dispatchers.IO) {
        val activeList = recurringDao.getActiveRecurring()
        val today = DateUtils.todayDateString()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayDate = sdf.parse(today) ?: return@withContext

        for (recurring in activeList) {
            val nextDate = sdf.parse(recurring.nextDueDate) ?: continue
            if (!nextDate.after(todayDate)) {
                // Time to insert transaction
                val day = DateUtils.calculateDayOfWeek(recurring.nextDueDate)
                val time = DateUtils.currentTimeString()
                transactionDao.insertTransaction(
                    TransactionEntity(
                        title = recurring.name,
                        amount = recurring.amount,
                        type = recurring.type,
                        category = recurring.category,
                        date = recurring.nextDueDate,
                        dayOfWeek = day,
                        time = time,
                        note = "Recurring payment (${recurring.frequency.lowercase()})"
                    )
                )

                // Advance next due date
                val cal = Calendar.getInstance().apply { this.time = nextDate }
                when (recurring.frequency.uppercase()) {
                    "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                    "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                    "MONTHLY" -> cal.add(Calendar.MONTH, 1)
                    "YEARLY" -> cal.add(Calendar.YEAR, 1)
                }
                val updated = recurring.copy(nextDueDate = sdf.format(cal.time))
                recurringDao.updateRecurring(updated)
            }
        }
    }

    // CSV Export
    suspend fun exportTransactionsToCsv(transactions: List<TransactionEntity>): String = withContext(Dispatchers.Default) {
        val sb = StringBuilder()
        sb.append("Type,Name,Amount,Category,Date,Day,Time,Note\n")
        for (t in transactions) {
            val cleanTitle = t.title.replace("\"", "\"\"")
            val cleanNote = t.note.replace("\"", "\"\"")
            val cleanCat = t.category.replace("\"", "\"\"")
            sb.append("\"${t.type}\",\"$cleanTitle\",${t.amount},\"$cleanCat\",\"${t.date}\",\"${t.dayOfWeek}\",\"${t.time}\",\"$cleanNote\"\n")
        }
        sb.toString()
    }

    // JSON Backup
    suspend fun createJsonBackup(transactions: List<TransactionEntity>): String = withContext(Dispatchers.Default) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val txArray = JSONArray()
        for (t in transactions) {
            val obj = JSONObject()
            obj.put("title", t.title)
            obj.put("amount", t.amount)
            obj.put("type", t.type)
            obj.put("category", t.category)
            obj.put("date", t.date)
            obj.put("dayOfWeek", t.dayOfWeek)
            obj.put("time", t.time)
            obj.put("note", t.note)
            obj.put("isFavorite", t.isFavorite)
            txArray.put(obj)
        }
        root.put("transactions", txArray)
        root.toString(2)
    }

    // JSON Restore
    suspend fun restoreFromJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val txArray = root.getJSONArray("transactions")
            transactionDao.deleteAllTransactions()
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                transactionDao.insertTransaction(
                    TransactionEntity(
                        title = obj.getString("title"),
                        amount = obj.getDouble("amount"),
                        type = obj.getString("type"),
                        category = obj.getString("category"),
                        date = obj.getString("date"),
                        dayOfWeek = obj.optString("dayOfWeek", DateUtils.calculateDayOfWeek(obj.getString("date"))),
                        time = obj.optString("time", "12:00 PM"),
                        note = obj.optString("note", ""),
                        isFavorite = obj.optBoolean("isFavorite", false),
                        isSample = false
                    )
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
