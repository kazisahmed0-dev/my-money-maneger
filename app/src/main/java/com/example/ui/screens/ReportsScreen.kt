package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.data.model.TransactionEntity
import com.example.ui.components.DonutPieChart
import com.example.ui.components.IncomeExpenseBarChart
import com.example.ui.components.SummaryCardsGrid
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SavingsBlue
import com.example.ui.viewmodel.CategoryDistribution
import com.example.ui.viewmodel.MonthSummary
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.util.Strings

@Composable
fun ReportsScreen(
    selectedYear: Int,
    selectedMonth: Int,
    monthIncome: Double,
    monthExpense: Double,
    monthBalance: Double,
    monthSavings: Double,
    monthlyTransactions: List<TransactionEntity>,
    highestIncomeTx: TransactionEntity?,
    highestExpenseTx: TransactionEntity?,
    largestExpenseCategory: String,
    mostUsedIncomeSource: String,
    mostUsedExpenseCategory: String,
    moneyFlowIncome: Map<String, Double>,
    moneyFlowExpense: Map<String, Double>,
    smartInsights: List<String>,
    yearly12MonthsSummary: List<MonthSummary>,
    currency: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenYearlyOverview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthName = remember(selectedYear, selectedMonth) {
        DateUtils.formatMonthYear(selectedYear, selectedMonth)
    }

    // Expense Category Distribution for Donut Chart
    val expenseDistribution = remember(moneyFlowExpense, monthExpense) {
        if (monthExpense <= 0.0) emptyList()
        else {
            moneyFlowExpense.map { (cat, amt) ->
                CategoryDistribution(
                    category = cat,
                    amount = amt,
                    percentage = (amt / monthExpense) * 100.0
                )
            }.sortedByDescending { it.amount }
        }
    }

    // Income Category Distribution for Donut Chart
    val incomeDistribution = remember(moneyFlowIncome, monthIncome) {
        if (monthIncome <= 0.0) emptyList()
        else {
            moneyFlowIncome.map { (src, amt) ->
                CategoryDistribution(
                    category = src,
                    amount = amt,
                    percentage = (amt / monthIncome) * 100.0
                )
            }.sortedByDescending { it.amount }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector Header & Yearly Navigation
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.get("nav_reports"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onOpenYearlyOverview,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("btn_reports_yearly_overview")
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("12-Month Yearly", fontSize = 12.sp)
                }
            }
        }

        // Month Picker Bar
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousMonth) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                    }

                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(onClick = onNextMonth) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                    }
                }
            }
        }

        // Monthly Summary 4 Cards
        item {
            SummaryCardsGrid(
                income = monthIncome,
                expense = monthExpense,
                balance = monthBalance,
                savings = monthSavings,
                currency = currency
            )
        }

        // Section 15: SMART MONEY INSIGHTS
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.get("money_insights"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    smartInsights.forEach { insight ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = insight,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Section 8: MONTHLY STAT HIGHLIGHTS
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Monthly Financial Highlights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    StatRow(
                        label = Strings.get("highest_income"),
                        value = if (highestIncomeTx != null) "${highestIncomeTx.title} (${CurrencyFormatter.format(highestIncomeTx.amount, currency)})" else "None",
                        color = IncomeGreen
                    )
                    StatRow(
                        label = Strings.get("highest_expense"),
                        value = if (highestExpenseTx != null) "${highestExpenseTx.title} (${CurrencyFormatter.format(highestExpenseTx.amount, currency)})" else "None",
                        color = ExpenseRed
                    )
                    StatRow(
                        label = Strings.get("largest_expense_category"),
                        value = largestExpenseCategory,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatRow(
                        label = Strings.get("most_used_income_source"),
                        value = mostUsedIncomeSource,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatRow(
                        label = Strings.get("most_used_expense_category"),
                        value = mostUsedExpenseCategory,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatRow(
                        label = Strings.get("number_of_transactions"),
                        value = "${monthlyTransactions.size}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Section 9: DEDICATED MONEY FLOW SECTION
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.get("money_flow"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // INCOME BREAKDOWN
                    Text(
                        text = "INCOME",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (moneyFlowIncome.isEmpty()) {
                        Text("No income recorded for this month.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        moneyFlowIncome.forEach { (source, amt) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(source, style = MaterialTheme.typography.bodyMedium)
                                Text(CurrencyFormatter.format(amt, currency), fontWeight = FontWeight.SemiBold, color = IncomeGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(Strings.get("total_income"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(CurrencyFormatter.format(monthIncome, currency), fontWeight = FontWeight.Bold, color = IncomeGreen)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // EXPENSE BREAKDOWN
                    Text(
                        text = "EXPENSE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (moneyFlowExpense.isEmpty()) {
                        Text("No expenses recorded for this month.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        moneyFlowExpense.forEach { (cat, amt) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cat, style = MaterialTheme.typography.bodyMedium)
                                Text(CurrencyFormatter.format(amt, currency), fontWeight = FontWeight.SemiBold, color = ExpenseRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(Strings.get("total_expense"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(CurrencyFormatter.format(monthExpense, currency), fontWeight = FontWeight.Bold, color = ExpenseRed)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // AVAILABLE BALANCE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.get("available_balance"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = CurrencyFormatter.format(monthBalance, currency),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (monthBalance >= 0) MaterialTheme.colorScheme.primary else ExpenseRed
                        )
                    }
                }
            }
        }

        // Section 14: VISUAL CHARTS (Income vs Expense & Category Breakdown)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Expense by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    DonutPieChart(
                        distributions = expenseDistribution,
                        currency = currency,
                        centerTitle = "Expenses"
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "12-Month Trend ($selectedYear)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    IncomeExpenseBarChart(
                        monthsData = yearly12MonthsSummary,
                        currency = currency
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}
