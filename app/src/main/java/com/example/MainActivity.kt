package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MoneyViewModel
import com.example.util.Strings

sealed class Screen(val route: String, val icon: ImageVector, val labelKey: String) {
    data object Home : Screen("home", Icons.Default.Home, "nav_home")
    data object Transactions : Screen("transactions", Icons.Default.ReceiptLong, "nav_transactions")
    data object Reports : Screen("reports", Icons.Default.BarChart, "nav_reports")
    data object Goals : Screen("goals", Icons.Default.Flag, "nav_goals")
    data object Settings : Screen("settings", Icons.Default.Settings, "nav_settings")
    data object YearlyOverview : Screen("yearly_overview", Icons.Default.CalendarMonth, "nav_yearly")
}

class MainActivity : ComponentActivity() {
    private val viewModel: MoneyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
            val isPinLocked by viewModel.isPinLocked.collectAsStateWithLifecycle()

            // Update language configuration
            LaunchedEffect(userProfile.language) {
                Strings.setLanguage(userProfile.language)
            }

            MyApplicationTheme(themeMode = userProfile.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        // 1. First-time Onboarding flow (Section 1)
                        !userProfile.isOnboarded -> {
                            OnboardingScreen(
                                currentProfile = userProfile,
                                onComplete = { updated ->
                                    viewModel.saveUserProfile(updated)
                                }
                            )
                        }

                        // 2. PIN Lock Protection (Section 25)
                        isPinLocked -> {
                            PinLockScreen(
                                onUnlock = { enteredPin ->
                                    viewModel.unlockWithPin(enteredPin)
                                }
                            )
                        }

                        // 3. Main App Navigation Container
                        else -> {
                            MainAppContent(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MoneyViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // Transaction Sheet state (Add / Edit)
    var showTransactionSheet by remember { mutableStateOf(false) }
    var transactionSheetType by remember { mutableStateOf("INCOME") }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    // ViewModel State flows
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()

    val monthIncome by viewModel.monthTotalIncome.collectAsStateWithLifecycle()
    val monthExpense by viewModel.monthTotalExpense.collectAsStateWithLifecycle()
    val monthBalance by viewModel.monthBalance.collectAsStateWithLifecycle()
    val monthSavings by viewModel.monthTotalSavings.collectAsStateWithLifecycle()

    val currentMonthTransactions by viewModel.currentMonthTransactions.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val budgetStatuses by viewModel.budgetStatuses.collectAsStateWithLifecycle()
    val recurringList by viewModel.recurringTransactions.collectAsStateWithLifecycle()
    val favoriteTransactions by viewModel.favoriteTransactions.collectAsStateWithLifecycle()

    val recentIncomeTitles by viewModel.recentIncomeTitles.collectAsStateWithLifecycle()
    val recentExpenseTitles by viewModel.recentExpenseTitles.collectAsStateWithLifecycle()

    val moneyFlowIncome by viewModel.moneyFlowIncome.collectAsStateWithLifecycle()
    val moneyFlowExpense by viewModel.moneyFlowExpense.collectAsStateWithLifecycle()
    val smartInsights by viewModel.smartInsights.collectAsStateWithLifecycle()

    val highestIncomeTx by viewModel.highestIncomeTx.collectAsStateWithLifecycle(initialValue = null)
    val highestExpenseTx by viewModel.highestExpenseTx.collectAsStateWithLifecycle(initialValue = null)
    val largestExpenseCategory by viewModel.largestExpenseCategory.collectAsStateWithLifecycle(initialValue = "N/A")
    val mostUsedIncomeSource by viewModel.mostUsedIncomeSource.collectAsStateWithLifecycle(initialValue = "N/A")
    val mostUsedExpenseCategory by viewModel.mostUsedExpenseCategory.collectAsStateWithLifecycle(initialValue = "N/A")

    // Yearly calculations
    val yearly12MonthsSummary by viewModel.yearly12MonthsSummary.collectAsStateWithLifecycle()
    val totalYearlyIncome by viewModel.yearlyTotalIncome.collectAsStateWithLifecycle()
    val totalYearlyExpense by viewModel.yearlyTotalExpense.collectAsStateWithLifecycle()
    val totalYearlySavings by viewModel.yearlyTotalSavings.collectAsStateWithLifecycle()
    val totalYearlyBalance by viewModel.yearlyTotalBalance.collectAsStateWithLifecycle()
    val yearlyIncomeDist by viewModel.yearlyIncomeDistribution.collectAsStateWithLifecycle()
    val yearlyExpenseDist by viewModel.yearlyExpenseDistribution.collectAsStateWithLifecycle()
    val monthlyContributions by viewModel.monthlyContributions.collectAsStateWithLifecycle()

    // Filter controls
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    val filterPeriod by viewModel.filterPeriod.collectAsStateWithLifecycle()
    val filterCategory by viewModel.filterCategory.collectAsStateWithLifecycle()

    val navItems = listOf(
        Screen.Home,
        Screen.Transactions,
        Screen.Reports,
        Screen.Goals,
        Screen.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentScreen != Screen.YearlyOverview) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    navItems.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = { Icon(screen.icon, contentDescription = Strings.get(screen.labelKey)) },
                            label = { Text(Strings.get(screen.labelKey), maxLines = 1) },
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Home -> {
                    HomeScreen(
                        userProfile = userProfile,
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        monthIncome = monthIncome,
                        monthExpense = monthExpense,
                        monthBalance = monthBalance,
                        monthSavings = monthSavings,
                        currentMonthTransactions = currentMonthTransactions,
                        favoriteTransactions = favoriteTransactions,
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onAddIncome = {
                            transactionSheetType = "INCOME"
                            transactionToEdit = null
                            showTransactionSheet = true
                        },
                        onAddExpense = {
                            transactionSheetType = "EXPENSE"
                            transactionToEdit = null
                            showTransactionSheet = true
                        },
                        onAddSaving = {
                            transactionSheetType = "SAVING"
                            transactionToEdit = null
                            showTransactionSheet = true
                        },
                        onAddGoal = {
                            currentScreen = Screen.Goals
                        },
                        onQuickAddFavorite = { fav ->
                            transactionSheetType = fav.type
                            transactionToEdit = fav.copy(id = 0) // New transaction prefilled
                            showTransactionSheet = true
                        },
                        onViewAllTransactions = {
                            currentScreen = Screen.Transactions
                        },
                        onViewTransactionDetails = { tx ->
                            // Transaction details can be viewed from transactions screen
                            currentScreen = Screen.Transactions
                        },
                        onEditTransaction = { tx ->
                            transactionSheetType = tx.type
                            transactionToEdit = tx
                            showTransactionSheet = true
                        },
                        onDeleteTransaction = { tx ->
                            viewModel.deleteTransaction(tx)
                        },
                        onOpenYearlyOverview = {
                            currentScreen = Screen.YearlyOverview
                        }
                    )
                }

                Screen.Transactions -> {
                    TransactionsScreen(
                        transactions = filteredTransactions,
                        categories = categories,
                        currency = userProfile.currency,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.searchQuery.value = it },
                        filterType = filterType,
                        onFilterTypeChange = { viewModel.filterType.value = it },
                        filterPeriod = filterPeriod,
                        onFilterPeriodChange = { viewModel.filterPeriod.value = it },
                        selectedCategory = filterCategory,
                        onCategoryChange = { viewModel.filterCategory.value = it },
                        onAddIncome = {
                            transactionSheetType = "INCOME"
                            transactionToEdit = null
                            showTransactionSheet = true
                        },
                        onAddExpense = {
                            transactionSheetType = "EXPENSE"
                            transactionToEdit = null
                            showTransactionSheet = true
                        },
                        onEditTransaction = { tx ->
                            transactionSheetType = tx.type
                            transactionToEdit = tx
                            showTransactionSheet = true
                        },
                        onDeleteTransaction = { tx ->
                            viewModel.deleteTransaction(tx)
                        }
                    )
                }

                Screen.Reports -> {
                    ReportsScreen(
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        monthIncome = monthIncome,
                        monthExpense = monthExpense,
                        monthBalance = monthBalance,
                        monthSavings = monthSavings,
                        monthlyTransactions = currentMonthTransactions,
                        highestIncomeTx = highestIncomeTx,
                        highestExpenseTx = highestExpenseTx,
                        largestExpenseCategory = largestExpenseCategory,
                        mostUsedIncomeSource = mostUsedIncomeSource,
                        mostUsedExpenseCategory = mostUsedExpenseCategory,
                        moneyFlowIncome = moneyFlowIncome,
                        moneyFlowExpense = moneyFlowExpense,
                        smartInsights = smartInsights,
                        yearly12MonthsSummary = yearly12MonthsSummary,
                        currency = userProfile.currency,
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onOpenYearlyOverview = {
                            currentScreen = Screen.YearlyOverview
                        }
                    )
                }

                Screen.YearlyOverview -> {
                    YearlyOverviewScreen(
                        selectedYear = selectedYear,
                        yearlyMonths = yearly12MonthsSummary,
                        totalYearlyIncome = totalYearlyIncome,
                        totalYearlyExpense = totalYearlyExpense,
                        totalYearlySavings = totalYearlySavings,
                        totalYearlyBalance = totalYearlyBalance,
                        yearlyIncomeDist = yearlyIncomeDist,
                        yearlyExpenseDist = yearlyExpenseDist,
                        monthlyContributions = monthlyContributions,
                        currency = userProfile.currency,
                        onYearChange = { viewModel.setSelectedYear(it) },
                        onDeleteMonthData = { yr, mo ->
                            viewModel.deleteMonthData(yr, mo)
                        },
                        onBack = {
                            currentScreen = Screen.Reports
                        }
                    )
                }

                Screen.Goals -> {
                    GoalsAndBudgetScreen(
                        goals = goals,
                        budgetStatuses = budgetStatuses,
                        recurringList = recurringList,
                        categories = categories,
                        currency = userProfile.currency,
                        onAddGoal = { title, target, initialSaved, targetDate, note ->
                            viewModel.addGoal(title, target, initialSaved, targetDate, note)
                        },
                        onAddMoneyToGoal = { goalId, amount ->
                            viewModel.addMoneyToGoal(goalId, amount)
                        },
                        onDeleteGoal = { goal ->
                            viewModel.deleteGoal(goal)
                        },
                        onSaveBudget = { category, limit ->
                            viewModel.addBudget(category, limit)
                        },
                        onDeleteBudget = { budget ->
                            viewModel.deleteBudget(budget)
                        },
                        onAddRecurring = { name, amount, type, category, frequency, startDate ->
                            viewModel.addRecurring(name, amount, type, category, frequency, startDate)
                        },
                        onDeleteRecurring = { rec ->
                            viewModel.deleteRecurring(rec)
                        }
                    )
                }

                Screen.Settings -> {
                    SettingsScreen(
                        userProfile = userProfile,
                        categories = categories,
                        onSaveProfile = { viewModel.saveUserProfile(it) },
                        onAddCategory = { name, type -> viewModel.addCategory(name, type) },
                        onDeleteCategory = { viewModel.deleteCategory(it) },
                        onExportCsv = { viewModel.exportCsv() },
                        onBackupJson = { viewModel.backupJson() },
                        onRestoreJson = { viewModel.restoreJson(it) },
                        onLoadSampleData = { viewModel.loadSampleData() },
                        onDeleteSampleData = { viewModel.deleteSampleData() },
                        onClearAllData = { viewModel.clearAllData() }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Adding / Editing Transactions
    if (showTransactionSheet) {
        val currentRecentTitles = when (transactionSheetType) {
            "INCOME" -> recentIncomeTitles
            "EXPENSE" -> recentExpenseTitles
            else -> emptyList()
        }

        AddEditTransactionSheet(
            initialType = transactionSheetType,
            transactionToEdit = transactionToEdit,
            categories = categories,
            recentTitles = currentRecentTitles,
            currency = userProfile.currency,
            onDismiss = {
                showTransactionSheet = false
                transactionToEdit = null
            },
            onSave = { title, amount, type, category, date, time, note, receiptUri, isFavorite ->
                if (transactionToEdit == null) {
                    viewModel.addTransaction(
                        title = title,
                        amount = amount,
                        type = type,
                        category = category,
                        date = date,
                        time = time,
                        note = note,
                        receiptUri = receiptUri,
                        isFavorite = isFavorite
                    )
                } else {
                    viewModel.updateTransaction(
                        transactionToEdit!!.copy(
                            title = title,
                            amount = amount,
                            type = type,
                            category = category,
                            date = date,
                            time = time,
                            note = note,
                            receiptUri = receiptUri,
                            isFavorite = isFavorite
                        )
                    )
                }
                showTransactionSheet = false
                transactionToEdit = null
            },
            onAddNewCategory = { name, type ->
                viewModel.addCategory(name, type)
            }
        )
    }
}
