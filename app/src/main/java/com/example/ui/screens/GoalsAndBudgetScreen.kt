package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.SavingsGoalEntity
import com.example.ui.components.HorizontalProgressBar
import com.example.ui.theme.BalanceAmber
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SavingsBlue
import com.example.ui.viewmodel.BudgetStatus
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.util.Strings
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsAndBudgetScreen(
    goals: List<SavingsGoalEntity>,
    budgetStatuses: List<BudgetStatus>,
    recurringList: List<RecurringTransactionEntity>,
    categories: List<CategoryEntity>,
    currency: String,
    onAddGoal: (title: String, target: Double, initialSaved: Double, targetDate: String?, note: String) -> Unit,
    onAddMoneyToGoal: (goalId: Long, amount: Double) -> Unit,
    onDeleteGoal: (SavingsGoalEntity) -> Unit,
    onSaveBudget: (category: String, limit: Double) -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit,
    onAddRecurring: (name: String, amount: Double, type: String, category: String, frequency: String, startDate: String) -> Unit,
    onDeleteRecurring: (RecurringTransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Goals, 1: Budgets, 2: Recurring

    // Dialog states
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddMoneyDialogForGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddRecurringDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = Strings.get("nav_goals"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Selector (Savings Goals, Budgets, Recurring)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Savings Goals", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("tab_savings_goals")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Budgets", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("tab_budgets")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Recurring", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("tab_recurring")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                // SAVINGS GOALS TAB
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Button(
                            onClick = { showAddGoalDialog = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_create_savings_goal"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SavingsBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Strings.get("add_goal"), fontWeight = FontWeight.Bold)
                        }
                    }

                    if (goals.isEmpty()) {
                        item {
                            EmptyCardView(
                                title = "No Savings Goals Yet",
                                message = "Create your first goal like 'Buy Laptop', 'Emergency Fund', or 'Vacation'."
                            )
                        }
                    } else {
                        items(goals, key = { it.id }) { goal ->
                            SavingsGoalItemCard(
                                goal = goal,
                                currency = currency,
                                onAddMoney = { showAddMoneyDialogForGoal = goal },
                                onDelete = { onDeleteGoal(goal) }
                            )
                        }
                    }
                }
            }
            1 -> {
                // BUDGETS TAB
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Button(
                            onClick = { showAddBudgetDialog = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_create_budget"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Strings.get("set_budget"), fontWeight = FontWeight.Bold)
                        }
                    }

                    if (budgetStatuses.isEmpty()) {
                        item {
                            EmptyCardView(
                                title = "No Budgets Configured",
                                message = "Set monthly spending limits for Food, Transport, Rent, or Overall spending."
                            )
                        }
                    } else {
                        items(budgetStatuses, key = { it.category }) { status ->
                            BudgetItemCard(
                                status = status,
                                currency = currency
                            )
                        }
                    }
                }
            }
            2 -> {
                // RECURRING TRANSACTIONS TAB
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Button(
                            onClick = { showAddRecurringDialog = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_create_recurring"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Recurring Transaction", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (recurringList.isEmpty()) {
                        item {
                            EmptyCardView(
                                title = "No Recurring Transactions",
                                message = "Schedule regular transactions like monthly Salary, Rent, Tuition, Internet bill or subscriptions."
                            )
                        }
                    } else {
                        items(recurringList, key = { it.id }) { rec ->
                            RecurringItemCard(
                                recurring = rec,
                                currency = currency,
                                onDelete = { onDeleteRecurring(rec) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        var title by remember { mutableStateOf("") }
        var targetText by remember { mutableStateOf("") }
        var initialSavedText by remember { mutableStateOf("") }
        var targetDate by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text(Strings.get("add_goal")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Goal Name (e.g. Buy Laptop)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text("Target Amount ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = initialSavedText,
                        onValueChange = { initialSavedText = it },
                        label = { Text("Already Saved ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = targetDate,
                        onValueChange = { targetDate = it },
                        label = { Text("Target Date (YYYY-MM-DD, Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = targetText.toDoubleOrNull() ?: 0.0
                        val initialSaved = initialSavedText.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && target > 0) {
                            onAddGoal(
                                title.trim(),
                                target,
                                initialSaved,
                                targetDate.ifBlank { null },
                                note.trim()
                            )
                            showAddGoalDialog = false
                        }
                    }
                ) {
                    Text("Create Goal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Money To Goal Dialog
    if (showAddMoneyDialogForGoal != null) {
        val goal = showAddMoneyDialogForGoal!!
        var addAmountText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddMoneyDialogForGoal = null },
            title = { Text("Add Money to ${goal.title}") },
            text = {
                Column {
                    Text(
                        "Current: ${CurrencyFormatter.format(goal.savedAmount, currency)} of ${CurrencyFormatter.format(goal.targetAmount, currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = addAmountText,
                        onValueChange = { addAmountText = it },
                        label = { Text("Amount to Add ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = addAmountText.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            onAddMoneyToGoal(goal.id, amt)
                            showAddMoneyDialogForGoal = null
                        }
                    }
                ) {
                    Text("Deposit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMoneyDialogForGoal = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Budget Dialog
    if (showAddBudgetDialog) {
        var selectedCat by remember { mutableStateOf("OVERALL") }
        var limitText by remember { mutableStateOf("") }

        val budgetCategories = remember(categories) {
            listOf("OVERALL") + categories.filter { it.type == "EXPENSE" }.map { it.name }
        }

        AlertDialog(
            onDismissRequest = { showAddBudgetDialog = false },
            title = { Text(Strings.get("set_budget")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Category or Overall:", style = MaterialTheme.typography.labelMedium)
                    ScrollableTabRow(
                        selectedTabIndex = budgetCategories.indexOf(selectedCat).coerceAtLeast(0),
                        edgePadding = 0.dp,
                        divider = {}
                    ) {
                        budgetCategories.forEach { cat ->
                            Tab(
                                selected = selectedCat == cat,
                                onClick = { selectedCat = cat },
                                text = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { limitText = it },
                        label = { Text("Monthly Limit ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = limitText.toDoubleOrNull()
                        if (limit != null && limit > 0) {
                            onSaveBudget(selectedCat, limit)
                            showAddBudgetDialog = false
                        }
                    }
                ) {
                    Text("Save Budget")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBudgetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Recurring Transaction Dialog
    if (showAddRecurringDialog) {
        var name by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("EXPENSE") }
        var frequency by remember { mutableStateOf("MONTHLY") }
        var category by remember { mutableStateOf("Rent") }

        AlertDialog(
            onDismissRequest = { showAddRecurringDialog = false },
            title = { Text("Add Recurring Transaction") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = type == "EXPENSE",
                            onClick = { type = "EXPENSE" },
                            label = { Text("Expense") }
                        )
                        FilterChip(
                            selected = type == "INCOME",
                            onClick = { type = "INCOME" },
                            label = { Text("Income") }
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name (e.g. House Rent, WiFi)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Frequency:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY").forEach { freq ->
                            FilterChip(
                                selected = frequency == freq,
                                onClick = { frequency = freq },
                                label = { Text(freq.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull()
                        if (name.isNotBlank() && amt != null && amt > 0) {
                            onAddRecurring(
                                name.trim(),
                                amt,
                                type,
                                category,
                                frequency,
                                DateUtils.todayDateString()
                            )
                            showAddRecurringDialog = false
                        }
                    }
                ) {
                    Text("Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRecurringDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SavingsGoalItemCard(
    goal: SavingsGoalEntity,
    currency: String,
    onAddMoney: () -> Unit,
    onDelete: () -> Unit
) {
    val remaining = (goal.targetAmount - goal.savedAmount).coerceAtLeast(0.0)
    val progress = if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount).toFloat() else 0f
    val percentText = String.format("%.1f", progress * 100f)

    // Calculate days remaining if target date is set
    val daysRemaining = remember(goal.targetDate) {
        if (goal.targetDate.isNullOrBlank()) null
        else {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val targetDate = sdf.parse(goal.targetDate)
                val today = sdf.parse(DateUtils.todayDateString())
                if (targetDate != null && today != null) {
                    val diff = targetDate.time - today.time
                    val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
                    days
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SavingsBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = SavingsBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (daysRemaining != null) {
                            Text(
                                text = if (daysRemaining >= 0) "$daysRemaining days left" else "Target date passed",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = if (daysRemaining >= 0) MaterialTheme.colorScheme.primary else ExpenseRed
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.format(goal.savedAmount, currency), fontWeight = FontWeight.Bold, color = SavingsBlue)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.format(remaining, currency), fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Target", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.format(goal.targetAmount, currency), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalProgressBar(
                progressPercent = progress,
                barColor = SavingsBlue
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$percentText% Achieved",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SavingsBlue
                )

                FilledTonalButton(
                    onClick = onAddMoney,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Money", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BudgetItemCard(
    status: BudgetStatus,
    currency: String
) {
    val barColor = when {
        status.percentageUsed > 100.0 -> ExpenseRed
        status.percentageUsed >= 80.0 -> BalanceAmber
        else -> IncomeGreen
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (status.category.equals("OVERALL", ignoreCase = true)) "Overall Monthly Budget" else "${status.category} Budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${String.format("%.0f", status.percentageUsed)}% used",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = barColor
                )
            }

            // Budget Warning Indicator (Section 12)
            if (status.isWarning) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (status.percentageUsed > 100.0) ExpenseRed.copy(alpha = 0.12f) else BalanceAmber.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (status.percentageUsed > 100.0)
                                "⚠️ Budget exceeded! You have overspent by ${CurrencyFormatter.format(status.used - status.limit, currency)}."
                            else
                                "⚠️ Warning: You have used most of your ${status.category} budget.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = if (status.percentageUsed > 100.0) ExpenseRed else BalanceAmber,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalProgressBar(
                progressPercent = (status.percentageUsed / 100.0).toFloat(),
                barColor = barColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ${CurrencyFormatter.format(status.used, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Limit: ${CurrencyFormatter.format(status.limit, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RecurringItemCard(
    recurring: RecurringTransactionEntity,
    currency: String,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = recurring.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${recurring.frequency.lowercase().replaceFirstChar { it.uppercase() }} • Next: ${recurring.nextDueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (recurring.type == "INCOME") "+" else "-"}${CurrencyFormatter.format(recurring.amount, currency)}",
                    fontWeight = FontWeight.Bold,
                    color = if (recurring.type == "INCOME") IncomeGreen else ExpenseRed
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ExpenseRed)
                }
            }
        }
    }
}

@Composable
private fun EmptyCardView(title: String, message: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
