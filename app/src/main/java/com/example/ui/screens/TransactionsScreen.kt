package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SavingsBlue
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.util.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    currency: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filterType: String,
    onFilterTypeChange: (String) -> Unit,
    filterPeriod: String,
    onFilterPeriodChange: (String) -> Unit,
    selectedCategory: String?,
    onCategoryChange: (String?) -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var detailsTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    // Summary calculations for currently filtered transactions
    val totalIncome = remember(transactions) { transactions.filter { it.type == "INCOME" }.sumOf { it.amount } }
    val totalExpense = remember(transactions) { transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount } }
    val totalSavings = remember(transactions) { transactions.filter { it.type == "SAVING" }.sumOf { it.amount } }
    val balance = remember(totalIncome, totalExpense) { totalIncome - totalExpense }
    val count = transactions.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // 1. Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(Strings.get("search_placeholder"), fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_tx_search"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Type Filter Chips (All, Income, Expense, Saving)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL" to "All", "INCOME" to "Income", "EXPENSE" to "Expense", "SAVING" to "Saving").forEach { (type, label) ->
                val isSelected = filterType == type
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterTypeChange(type) },
                    label = { Text(label, fontSize = 12.sp) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("filter_type_$type")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Time Period Chips (All, Today, This Week, This Month, This Year)
        ScrollableTabRow(
            selectedTabIndex = when (filterPeriod) {
                "TODAY" -> 1
                "THIS_WEEK" -> 2
                "THIS_MONTH" -> 3
                "THIS_YEAR" -> 4
                else -> 0
            },
            edgePadding = 0.dp,
            divider = {},
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                "ALL" to Strings.get("all"),
                "TODAY" to Strings.get("today"),
                "THIS_WEEK" to Strings.get("this_week"),
                "THIS_MONTH" to Strings.get("this_month"),
                "THIS_YEAR" to Strings.get("this_year")
            ).forEachIndexed { index, (period, label) ->
                val isSelected = filterPeriod == period
                Tab(
                    selected = isSelected,
                    onClick = { onFilterPeriodChange(period) },
                    text = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Financial Summary Strip for Active Filter
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.format(totalIncome, currency), fontWeight = FontWeight.Bold, color = IncomeGreen, fontSize = 12.sp)
                }
                Column {
                    Text("Expense", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.format(totalExpense, currency), fontWeight = FontWeight.Bold, color = ExpenseRed, fontSize = 12.sp)
                }
                Column {
                    Text("Balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.format(balance, currency), fontWeight = FontWeight.Bold, color = if (balance >= 0) MaterialTheme.colorScheme.primary else ExpenseRed, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Count", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$count", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Transaction List or Empty State
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = Strings.get("no_transactions"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try adjusting your search or filters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onAddIncome,
                            colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(Strings.get("add_income"))
                        }
                        Button(
                            onClick = onAddExpense,
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(Strings.get("add_expense"))
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions, key = { it.id }) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        currency = currency,
                        onViewDetails = { detailsTransaction = it },
                        onEdit = onEditTransaction,
                        onDelete = { transactionToDelete = it }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog (Section 6: "Are you sure you want to delete this transaction?")
    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction") },
            text = {
                Text("Are you sure you want to delete this transaction?\n\n\"${tx.title}\" (${CurrencyFormatter.format(tx.amount, currency)})")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(tx)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier.testTag("btn_confirm_delete_tx")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // View Details Dialog with Receipt
    if (detailsTransaction != null) {
        val tx = detailsTransaction!!
        Dialog(onDismissRequest = { detailsTransaction = null }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (tx.type) {
                                "INCOME" -> IncomeGreen.copy(alpha = 0.15f)
                                "EXPENSE" -> ExpenseRed.copy(alpha = 0.15f)
                                else -> SavingsBlue.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = tx.type,
                                fontWeight = FontWeight.Bold,
                                color = when (tx.type) {
                                    "INCOME" -> IncomeGreen
                                    "EXPENSE" -> ExpenseRed
                                    else -> SavingsBlue
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp
                            )
                        }

                        IconButton(onClick = { detailsTransaction = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = CurrencyFormatter.format(tx.amount, currency),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (tx.type) {
                            "INCOME" -> IncomeGreen
                            "EXPENSE" -> ExpenseRed
                            else -> SavingsBlue
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow("Category", tx.category)
                    DetailRow("Date", "${DateUtils.formatDisplayDate(tx.date)} (${tx.date})")
                    DetailRow("Day", tx.dayOfWeek)
                    DetailRow("Time", tx.time)
                    if (tx.note.isNotBlank()) {
                        DetailRow("Note", tx.note)
                    }

                    if (tx.receiptUri != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Attached Receipt:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        AsyncImage(
                            model = tx.receiptUri,
                            contentDescription = "Receipt Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                val current = tx
                                detailsTransaction = null
                                onEditTransaction(current)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { detailsTransaction = null },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
