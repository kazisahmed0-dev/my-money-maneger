package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.MoneyManagerRepository
import com.example.util.DateUtils
import com.example.util.NotificationHelper
import com.example.util.Strings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class MonthSummary(
    val monthIndex: Int, // 0-11
    val monthName: String,
    val income: Double,
    val expense: Double,
    val balance: Double,
    val savings: Double
)

data class CategoryDistribution(
    val category: String,
    val amount: Double,
    val percentage: Double
)

data class MonthContribution(
    val monthName: String,
    val incomeAmount: Double,
    val incomePercentage: Double,
    val expenseAmount: Double,
    val expensePercentage: Double
)

data class BudgetStatus(
    val category: String,
    val limit: Double,
    val used: Double,
    val remaining: Double,
    val percentageUsed: Double,
    val isWarning: Boolean
)

class MoneyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MoneyManagerRepository(application)

    val userProfile: StateFlow<UserProfile> = repository.userProfile

    private val _isPinLocked = MutableStateFlow(false)
    val isPinLocked: StateFlow<Boolean> = _isPinLocked.asStateFlow()

    // Current selected Year and Month (1-12)
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-indexed

    private val _selectedYear = MutableStateFlow(currentYear)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(currentMonth)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // Filter states
    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow("ALL") // "ALL", "INCOME", "EXPENSE", "SAVING"
    val filterPeriod = MutableStateFlow("ALL") // "ALL", "TODAY", "THIS_WEEK", "THIS_MONTH", "THIS_YEAR"
    val filterCategory = MutableStateFlow<String?>(null)

    // Room DB streams
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<SavingsGoalEntity>> = repository.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = repository.getAllBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringTransactions: StateFlow<List<RecurringTransactionEntity>> = repository.getAllRecurring()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentIncomeTitles: StateFlow<List<String>> = repository.getRecentTransactionTitles("INCOME")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentExpenseTitles: StateFlow<List<String>> = repository.getRecentTransactionTitles("EXPENSE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTransactions: StateFlow<List<TransactionEntity>> = repository.getFavoriteTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        NotificationHelper.initChannel(application)
        viewModelScope.launch {
            repository.initDefaultDataIfNeeded()
            repository.processRecurringTransactions()
            if (userProfile.value.isPinEnabled && userProfile.value.pinCode.isNotEmpty()) {
                _isPinLocked.value = true
            }
        }
    }

    fun unlockWithPin(enteredPin: String): Boolean {
        if (enteredPin == userProfile.value.pinCode) {
            _isPinLocked.value = false
            return true
        }
        return false
    }

    fun setSelectedMonth(month1to12: Int) {
        _selectedMonth.value = month1to12
    }

    fun setSelectedYear(year: Int) {
        _selectedYear.value = year
    }

    fun nextMonth() {
        if (_selectedMonth.value == 12) {
            _selectedMonth.value = 1
            _selectedYear.value += 1
        } else {
            _selectedMonth.value += 1
        }
    }

    fun previousMonth() {
        if (_selectedMonth.value == 1) {
            _selectedMonth.value = 12
            _selectedYear.value -= 1
        } else {
            _selectedMonth.value -= 1
        }
    }

    // Monthly transactions filtered by currently selected year and month
    val currentMonthTransactions: StateFlow<List<TransactionEntity>> =
        combine(allTransactions, selectedYear, selectedMonth) { list, yr, mo ->
            val prefix = String.format(Locale.US, "%04d-%02d", yr, mo)
            list.filter { it.date.startsWith(prefix) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered transactions for Transactions History tab
    val filteredTransactions: StateFlow<List<TransactionEntity>> =
        combine(allTransactions, searchQuery, filterType, filterPeriod, filterCategory) { list, query, type, period, cat ->
            var result = list
            if (type != "ALL") {
                result = result.filter { it.type.equals(type, ignoreCase = true) }
            }
            if (!cat.isNullOrBlank() && cat != "All") {
                result = result.filter { it.category.equals(cat, ignoreCase = true) }
            }
            if (query.isNotBlank()) {
                val q = query.trim().lowercase()
                result = result.filter {
                    it.title.lowercase().contains(q) ||
                    it.category.lowercase().contains(q) ||
                    it.note.lowercase().contains(q) ||
                    it.date.contains(q) ||
                    it.amount.toString().contains(q)
                }
            }
            // Period filter
            val today = DateUtils.todayDateString()
            val cal = Calendar.getInstance()
            when (period) {
                "TODAY" -> result = result.filter { it.date == today }
                "THIS_WEEK" -> {
                    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val startOfWeek = sdf.format(cal.time)
                    result = result.filter { it.date >= startOfWeek && it.date <= today }
                }
                "THIS_MONTH" -> {
                    val prefix = DateUtils.currentYearMonthKey()
                    result = result.filter { it.date.startsWith(prefix) }
                }
                "THIS_YEAR" -> {
                    val yearStr = DateUtils.currentYear().toString()
                    result = result.filter { it.date.startsWith(yearStr) }
                }
            }
            result
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month financial sums
    val monthTotalIncome: StateFlow<Double> = currentMonthTransactions.map { list ->
        list.filter { it.type == "INCOME" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthTotalExpense: StateFlow<Double> = currentMonthTransactions.map { list ->
        list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthTotalSavings: StateFlow<Double> = currentMonthTransactions.map { list ->
        list.filter { it.type == "SAVING" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthBalance: StateFlow<Double> = combine(monthTotalIncome, monthTotalExpense) { inc, exp ->
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Lifetime / Overall total savings
    val allTimeSavings: StateFlow<Double> = allTransactions.map { list ->
        list.filter { it.type == "SAVING" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Budget Status
    val budgetStatuses: StateFlow<List<BudgetStatus>> =
        combine(budgets, currentMonthTransactions) { bList, txList ->
            val expenseTx = txList.filter { it.type == "EXPENSE" }
            bList.map { b ->
                val used = if (b.category.equals("OVERALL", ignoreCase = true)) {
                    expenseTx.sumOf { it.amount }
                } else {
                    expenseTx.filter { it.category.equals(b.category, ignoreCase = true) }.sumOf { it.amount }
                }
                val remaining = (b.monthlyLimit - used).coerceAtLeast(0.0)
                val pct = if (b.monthlyLimit > 0) (used / b.monthlyLimit) * 100.0 else 0.0
                BudgetStatus(
                    category = b.category,
                    limit = b.monthlyLimit,
                    used = used,
                    remaining = remaining,
                    percentageUsed = pct,
                    isWarning = pct >= 80.0
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Money Flow: Grouped Income & Expense
    val moneyFlowIncome: StateFlow<Map<String, Double>> = currentMonthTransactions.map { list ->
        list.filter { it.type == "INCOME" }
            .groupBy { it.title.ifBlank { it.category } }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val moneyFlowExpense: StateFlow<Map<String, Double>> = currentMonthTransactions.map { list ->
        list.filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Reports stats for current selected month
    val highestIncomeTx = currentMonthTransactions.map { list ->
        list.filter { it.type == "INCOME" }.maxByOrNull { it.amount }
    }
    val highestExpenseTx = currentMonthTransactions.map { list ->
        list.filter { it.type == "EXPENSE" }.maxByOrNull { it.amount }
    }
    val largestExpenseCategory = currentMonthTransactions.map { list ->
        list.filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .maxByOrNull { it.value.sumOf { tx -> tx.amount } }
            ?.key ?: "N/A"
    }
    val mostUsedIncomeSource = currentMonthTransactions.map { list ->
        list.filter { it.type == "INCOME" }
            .groupBy { it.title }
            .maxByOrNull { it.value.size }
            ?.key ?: "N/A"
    }
    val mostUsedExpenseCategory = currentMonthTransactions.map { list ->
        list.filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .maxByOrNull { it.value.size }
            ?.key ?: "N/A"
    }

    // Smart Money Insights
    val smartInsights: StateFlow<List<String>> =
        combine(allTransactions, selectedYear, selectedMonth) { list, yr, mo ->
            val currentKey = String.format(Locale.US, "%04d-%02d", yr, mo)
            val prevMo = if (mo == 1) 12 else mo - 1
            val prevYr = if (mo == 1) yr - 1 else yr
            val prevKey = String.format(Locale.US, "%04d-%02d", prevYr, prevMo)

            val curTx = list.filter { it.date.startsWith(currentKey) }
            val prevTx = list.filter { it.date.startsWith(prevKey) }

            val curIncome = curTx.filter { it.type == "INCOME" }.sumOf { it.amount }
            val prevIncome = prevTx.filter { it.type == "INCOME" }.sumOf { it.amount }

            val curExpense = curTx.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val prevExpense = prevTx.filter { it.type == "EXPENSE" }.sumOf { it.amount }

            val curSavings = curTx.filter { it.type == "SAVING" }.sumOf { it.amount }

            val insights = mutableListOf<String>()

            // Income difference insight
            if (curIncome > 0 && prevIncome > 0) {
                val diff = curIncome - prevIncome
                if (diff > 0) {
                    insights.add("You earned ৳${String.format(Locale.US, "%,.0f", diff)} more this month than last month.")
                } else if (diff < 0) {
                    insights.add("Your income was ৳${String.format(Locale.US, "%,.0f", -diff)} less this month compared to last month.")
                }
            } else if (curIncome > 0) {
                insights.add("You earned ৳${String.format(Locale.US, "%,.0f", curIncome)} in ${DateUtils.getMonthName(mo - 1)}.")
            }

            // Expense difference insight
            if (curExpense > 0 && prevExpense > 0) {
                if (curExpense < prevExpense) {
                    val savedPct = ((prevExpense - curExpense) / prevExpense * 100).toInt()
                    insights.add("Your expenses decreased by $savedPct% compared with last month. Great job!")
                } else if (curExpense > prevExpense) {
                    val incPct = ((curExpense - prevExpense) / prevExpense * 100).toInt()
                    insights.add("Your expenses increased by $incPct% compared with last month.")
                }
            }

            // Largest expense category
            val largestCat = curTx.filter { it.type == "EXPENSE" }
                .groupBy { it.category }
                .maxByOrNull { it.value.sumOf { t -> t.amount } }

            if (largestCat != null) {
                val catSum = largestCat.value.sumOf { it.amount }
                insights.add("${largestCat.key} was your largest expense category (৳${String.format(Locale.US, "%,.0f", catSum)}).")
            }

            // Savings Rate
            if (curIncome > 0 && curSavings > 0) {
                val rate = ((curSavings / curIncome) * 100).toInt()
                insights.add("You saved $rate% of your income this month.")
            }

            // Highest spending day of week
            val daySpending = curTx.filter { it.type == "EXPENSE" }
                .groupBy { it.dayOfWeek }
                .maxByOrNull { it.value.sumOf { t -> t.amount } }

            if (daySpending != null && daySpending.key.isNotBlank()) {
                insights.add("Your highest spending day was ${daySpending.key}.")
            }

            if (insights.isEmpty()) {
                insights.add("Add your daily income and expenses to unlock helpful financial insights!")
            }

            insights
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================
    // YEARLY OVERVIEW CALCULATIONS (Sections 46-53)
    // ==========================================

    val yearly12MonthsSummary: StateFlow<List<MonthSummary>> =
        combine(allTransactions, selectedYear) { list, yr ->
            val yrPrefix = String.format(Locale.US, "%04d", yr)
            val yrTx = list.filter { it.date.startsWith(yrPrefix) }

            val result = mutableListOf<MonthSummary>()
            for (m in 1..12) {
                val mPrefix = String.format(Locale.US, "%04d-%02d", yr, m)
                val mTx = yrTx.filter { it.date.startsWith(mPrefix) }
                val inc = mTx.filter { it.type == "INCOME" }.sumOf { it.amount }
                val exp = mTx.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                val sav = mTx.filter { it.type == "SAVING" }.sumOf { it.amount }
                result.add(
                    MonthSummary(
                        monthIndex = m - 1,
                        monthName = DateUtils.getMonthName(m - 1),
                        income = inc,
                        expense = exp,
                        balance = inc - exp,
                        savings = sav
                    )
                )
            }
            result
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val yearlyTotalIncome: StateFlow<Double> = yearly12MonthsSummary.map { list ->
        list.sumOf { it.income }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val yearlyTotalExpense: StateFlow<Double> = yearly12MonthsSummary.map { list ->
        list.sumOf { it.expense }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val yearlyTotalSavings: StateFlow<Double> = yearly12MonthsSummary.map { list ->
        list.sumOf { it.savings }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val yearlyTotalBalance: StateFlow<Double> = combine(yearlyTotalIncome, yearlyTotalExpense) { inc, exp ->
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Yearly 100% Income Distribution
    val yearlyIncomeDistribution: StateFlow<List<CategoryDistribution>> =
        combine(allTransactions, selectedYear, yearlyTotalIncome) { list, yr, totalInc ->
            val yrPrefix = String.format(Locale.US, "%04d", yr)
            val incTx = list.filter { it.date.startsWith(yrPrefix) && it.type == "INCOME" }
            if (totalInc <= 0.0 || incTx.isEmpty()) {
                emptyList()
            } else {
                incTx.groupBy { it.category }
                    .map { entry ->
                        val catTotal = entry.value.sumOf { it.amount }
                        CategoryDistribution(
                            category = entry.key,
                            amount = catTotal,
                            percentage = (catTotal / totalInc) * 100.0
                        )
                    }
                    .sortedByDescending { it.amount }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Yearly 100% Expense Distribution
    val yearlyExpenseDistribution: StateFlow<List<CategoryDistribution>> =
        combine(allTransactions, selectedYear, yearlyTotalExpense) { list, yr, totalExp ->
            val yrPrefix = String.format(Locale.US, "%04d", yr)
            val expTx = list.filter { it.date.startsWith(yrPrefix) && it.type == "EXPENSE" }
            if (totalExp <= 0.0 || expTx.isEmpty()) {
                emptyList()
            } else {
                expTx.groupBy { it.category }
                    .map { entry ->
                        val catTotal = entry.value.sumOf { it.amount }
                        CategoryDistribution(
                            category = entry.key,
                            amount = catTotal,
                            percentage = (catTotal / totalExp) * 100.0
                        )
                    }
                    .sortedByDescending { it.amount }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monthly Contribution to Yearly Total
    val monthlyContributions: StateFlow<List<MonthContribution>> =
        combine(yearly12MonthsSummary, yearlyTotalIncome, yearlyTotalExpense) { months, totalInc, totalExp ->
            months.map { m ->
                MonthContribution(
                    monthName = m.monthName,
                    incomeAmount = m.income,
                    incomePercentage = if (totalInc > 0) (m.income / totalInc) * 100.0 else 0.0,
                    expenseAmount = m.expense,
                    expensePercentage = if (totalExp > 0) (m.expense / totalExp) * 100.0 else 0.0
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================
    // ACTIONS
    // ==========================================

    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        date: String,
        time: String,
        note: String = "",
        receiptUri: String? = null,
        isFavorite: Boolean = false
    ) {
        viewModelScope.launch {
            val dayOfWeek = DateUtils.calculateDayOfWeek(date)
            val entity = TransactionEntity(
                title = title.trim(),
                amount = amount,
                type = type.uppercase(),
                category = category.trim(),
                date = date,
                dayOfWeek = dayOfWeek,
                time = time,
                note = note.trim(),
                receiptUri = receiptUri,
                isFavorite = isFavorite,
                isSample = false
            )
            repository.insertTransaction(entity)

            // Check budget warnings if expense
            if (type.equals("EXPENSE", ignoreCase = true) && userProfile.value.notificationsEnabled) {
                checkBudgetWarnings(category)
            }
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            val dayOfWeek = DateUtils.calculateDayOfWeek(transaction.date)
            repository.updateTransaction(transaction.copy(dayOfWeek = dayOfWeek))
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun deleteTransactionById(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun deleteMonthData(year: Int, month1to12: Int) {
        viewModelScope.launch {
            val prefix = String.format(Locale.US, "%04d-%02d", year, month1to12)
            repository.deleteMonthData(prefix)
        }
    }

    fun addGoal(title: String, targetAmount: Double, initialSaved: Double = 0.0, targetDate: String? = null, note: String = "") {
        viewModelScope.launch {
            repository.insertGoal(
                SavingsGoalEntity(
                    title = title.trim(),
                    targetAmount = targetAmount,
                    savedAmount = initialSaved,
                    targetDate = targetDate,
                    note = note.trim()
                )
            )
        }
    }

    fun updateGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun deleteGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun addMoneyToGoal(goalId: Long, amount: Double) {
        viewModelScope.launch {
            repository.addMoneyToGoal(goalId, amount)
        }
    }

    fun addBudget(category: String, limit: Double) {
        viewModelScope.launch {
            repository.insertBudget(BudgetEntity(category = category, monthlyLimit = limit))
        }
    }

    fun updateBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.updateBudget(budget)
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    fun addRecurring(
        name: String,
        amount: Double,
        type: String,
        category: String,
        frequency: String,
        startDate: String
    ) {
        viewModelScope.launch {
            repository.insertRecurring(
                RecurringTransactionEntity(
                    name = name.trim(),
                    amount = amount,
                    type = type.uppercase(),
                    category = category.trim(),
                    frequency = frequency.uppercase(),
                    startDate = startDate,
                    nextDueDate = startDate,
                    active = true
                )
            )
        }
    }

    fun deleteRecurring(recurring: RecurringTransactionEntity) {
        viewModelScope.launch {
            repository.deleteRecurring(recurring)
        }
    }

    fun addCategory(name: String, type: String, icon: String = "category") {
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name.trim(),
                    type = type.uppercase(),
                    iconName = icon,
                    isCustom = true
                )
            )
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
            if (profile.isPinEnabled && profile.pinCode.isNotEmpty()) {
                // PIN is enabled
            } else {
                _isPinLocked.value = false
            }
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            repository.insertSampleData()
        }
    }

    fun deleteSampleData() {
        viewModelScope.launch {
            repository.deleteSampleTransactions()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    suspend fun exportCsv(): String {
        return repository.exportTransactionsToCsv(allTransactions.value)
    }

    suspend fun backupJson(): String {
        return repository.createJsonBackup(allTransactions.value)
    }

    suspend fun restoreJson(jsonString: String): Boolean {
        return repository.restoreFromJson(jsonString)
    }

    private fun checkBudgetWarnings(category: String) {
        val currentBudgets = budgets.value
        val catBudget = currentBudgets.find { it.category.equals(category, ignoreCase = true) }
        if (catBudget != null && catBudget.monthlyLimit > 0) {
            val used = currentMonthTransactions.value
                .filter { it.type == "EXPENSE" && it.category.equals(category, ignoreCase = true) }
                .sumOf { it.amount }
            val pct = ((used / catBudget.monthlyLimit) * 100).toInt()
            if (pct >= 80) {
                NotificationHelper.showBudgetWarning(getApplication(), category, pct)
            }
        }
    }
}
