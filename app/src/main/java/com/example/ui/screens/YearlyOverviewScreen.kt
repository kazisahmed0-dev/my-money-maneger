package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DonutPieChart
import com.example.ui.components.HorizontalProgressBar
import com.example.ui.components.IncomeExpenseBarChart
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SavingsBlue
import com.example.ui.viewmodel.CategoryDistribution
import com.example.ui.viewmodel.MonthContribution
import com.example.ui.viewmodel.MonthSummary
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearlyOverviewScreen(
    selectedYear: Int,
    yearlyMonths: List<MonthSummary>,
    totalYearlyIncome: Double,
    totalYearlyExpense: Double,
    totalYearlySavings: Double,
    totalYearlyBalance: Double,
    yearlyIncomeDist: List<CategoryDistribution>,
    yearlyExpenseDist: List<CategoryDistribution>,
    monthlyContributions: List<MonthContribution>,
    currency: String,
    onYearChange: (Int) -> Unit,
    onDeleteMonthData: (year: Int, month1to12: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var monthToDeleteDataFor by remember { mutableStateOf<MonthSummary?>(null) }
    var showYearPicker by remember { mutableStateOf(false) }

    val availableYears = remember {
        val cur = DateUtils.currentYear()
        listOf(cur - 2, cur - 1, cur, cur + 1, cur + 2)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Yearly 12-Month Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Year selector dropdown button
                    OutlinedButton(
                        onClick = { showYearPicker = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp).testTag("btn_select_year")
                    ) {
                        Text("$selectedYear", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                    }

                    DropdownMenu(
                        expanded = showYearPicker,
                        onDismissRequest = { showYearPicker = false }
                    ) {
                        availableYears.forEach { yr ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "$yr",
                                        fontWeight = if (yr == selectedYear) FontWeight.Bold else FontWeight.Normal,
                                        color = if (yr == selectedYear) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onYearChange(yr)
                                    showYearPicker = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 52: 12-Month Canvas Bar Chart
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Annual Trend ($selectedYear)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        IncomeExpenseBarChart(
                            monthsData = yearlyMonths,
                            currency = currency
                        )
                    }
                }
            }

            // Section 47: 12 Month Table (January to December)
            item {
                Text(
                    text = "12 Months Breakdown ($selectedYear)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(yearlyMonths) { index, month ->
                val month1to12 = index + 1
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = month.monthName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Option to delete this month's data (Section 53)
                            if (month.income > 0 || month.expense > 0 || month.savings > 0) {
                                IconButton(
                                    onClick = { monthToDeleteDataFor = month },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete month data",
                                        tint = ExpenseRed.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = CurrencyFormatter.format(month.income, currency),
                                    fontWeight = FontWeight.Bold,
                                    color = if (month.income > 0) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                            Column {
                                Text("Expense", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = CurrencyFormatter.format(month.expense, currency),
                                    fontWeight = FontWeight.Bold,
                                    color = if (month.expense > 0) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = CurrencyFormatter.format(month.balance, currency),
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        month.balance > 0 -> IncomeGreen
                                        month.balance < 0 -> ExpenseRed
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Section 48: TOTAL YEARLY SUMMARY CARD
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "TOTAL YEARLY SUMMARY ($selectedYear)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        YearlySummaryLine("TOTAL YEARLY INCOME", totalYearlyIncome, currency, IncomeGreen)
                        YearlySummaryLine("TOTAL YEARLY EXPENSE", totalYearlyExpense, currency, ExpenseRed)
                        YearlySummaryLine("TOTAL YEARLY SAVINGS", totalYearlySavings, currency, SavingsBlue)

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        YearlySummaryLine(
                            "TOTAL YEARLY BALANCE",
                            totalYearlyBalance,
                            currency,
                            if (totalYearlyBalance >= 0) MaterialTheme.colorScheme.primary else ExpenseRed,
                            isLarge = true
                        )
                    }
                }
            }

            // Section 49: YEARLY 100% INCOME DISTRIBUTION
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Yearly 100% Income Distribution",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Category breakdown of all income earned in $selectedYear",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (yearlyIncomeDist.isEmpty()) {
                            Text("No income recorded for this year.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            DonutPieChart(
                                distributions = yearlyIncomeDist,
                                currency = currency,
                                centerTitle = "Income"
                            )
                        }
                    }
                }
            }

            // Section 50: YEARLY 100% EXPENSE DISTRIBUTION
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Yearly 100% Expense Distribution",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Category breakdown of all expenses incurred in $selectedYear",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (yearlyExpenseDist.isEmpty()) {
                            Text("No expenses recorded for this year.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            DonutPieChart(
                                distributions = yearlyExpenseDist,
                                currency = currency,
                                centerTitle = "Expenses"
                            )
                        }
                    }
                }
            }

            // Section 51: MONTHLY CONTRIBUTION TO YEARLY TOTAL
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Monthly Contribution to Yearly Total",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Percentage of annual income and expenses per month",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        monthlyContributions.forEach { mc ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(mc.monthName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "Income: ${String.format("%.1f", mc.incomePercentage)}% • Expense: ${String.format("%.1f", mc.expensePercentage)}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                // Income progress
                                HorizontalProgressBar(
                                    progressPercent = (mc.incomePercentage / 100.0).toFloat(),
                                    barColor = IncomeGreen
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                // Expense progress
                                HorizontalProgressBar(
                                    progressPercent = (mc.expensePercentage / 100.0).toFloat(),
                                    barColor = ExpenseRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Section 53: Month Data Deletion Confirmation Dialog
    if (monthToDeleteDataFor != null) {
        val m = monthToDeleteDataFor!!
        AlertDialog(
            onDismissRequest = { monthToDeleteDataFor = null },
            title = { Text("Delete Month Data") },
            text = {
                Text("Are you sure you want to delete all transactions for ${m.monthName} $selectedYear?\n\nThis will permanently remove all income, expense, and savings entries for this month.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMonthData(selectedYear, m.monthIndex + 1)
                        monthToDeleteDataFor = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete Month Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { monthToDeleteDataFor = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun YearlySummaryLine(label: String, amount: Double, currency: String, color: Color, isLarge: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isLarge) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isLarge) FontWeight.Bold else FontWeight.Medium
        )
        Text(
            text = CurrencyFormatter.format(amount, currency),
            style = if (isLarge) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
