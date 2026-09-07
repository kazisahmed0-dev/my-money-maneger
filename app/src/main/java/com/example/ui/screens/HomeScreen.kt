package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.UserProfile
import com.example.ui.components.QuickActionsRow
import com.example.ui.components.SummaryCardsGrid
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.EmeraldPrimaryLight
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SavingsBlue
import com.example.util.DateUtils
import com.example.util.Strings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    userProfile: UserProfile,
    selectedYear: Int,
    selectedMonth: Int,
    monthIncome: Double,
    monthExpense: Double,
    monthBalance: Double,
    monthSavings: Double,
    currentMonthTransactions: List<TransactionEntity>,
    favoriteTransactions: List<TransactionEntity>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddSaving: () -> Unit,
    onAddGoal: () -> Unit,
    onQuickAddFavorite: (TransactionEntity) -> Unit,
    onViewAllTransactions: () -> Unit,
    onViewTransactionDetails: (TransactionEntity) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onOpenYearlyOverview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBangla = userProfile.language == "bn"
    val greeting = remember(isBangla) { DateUtils.getGreeting(isBangla) }
    val monthName = remember(selectedYear, selectedMonth) {
        DateUtils.formatMonthYear(selectedYear, selectedMonth)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Greeting & User Name
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$greeting,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (userProfile.name.isNotBlank()) "${userProfile.name} 👋" else "Friend 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Yearly Overview Shortcut Button
                FilledTonalButton(
                    onClick = onOpenYearlyOverview,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_home_yearly_overview")
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Yearly", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 2. Month Selector Row
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
                    IconButton(onClick = onPreviousMonth, modifier = Modifier.size(36.dp).testTag("btn_prev_month")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                    }

                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(onClick = onNextMonth, modifier = Modifier.size(36.dp).testTag("btn_next_month")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                    }
                }
            }
        }

        // 3. Four Summary Cards
        item {
            SummaryCardsGrid(
                income = monthIncome,
                expense = monthExpense,
                balance = monthBalance,
                savings = monthSavings,
                currency = userProfile.currency
            )
        }

        // 4. Quick Action Buttons
        item {
            QuickActionsRow(
                onAddIncome = onAddIncome,
                onAddExpense = onAddExpense,
                onAddSaving = onAddSaving,
                onAddGoal = onAddGoal
            )
        }

        // 5. Frequently Used / Favorite Transactions (Quick Add)
        if (favoriteTransactions.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Quick Add Favorites",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        favoriteTransactions.distinctBy { it.title }.take(5).forEach { fav ->
                            ElevatedFilterChip(
                                selected = false,
                                onClick = { onQuickAddFavorite(fav) },
                                label = {
                                    Text("⭐ ${fav.title} (${userProfile.currency}${fav.amount.toInt()})", fontSize = 12.sp)
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Recent Transactions in Selected Month Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (currentMonthTransactions.isNotEmpty()) {
                    TextButton(onClick = onViewAllTransactions) {
                        Text("View All (${currentMonthTransactions.size})")
                    }
                }
            }
        }

        // 7. Recent Transactions List or Empty State
        if (currentMonthTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ReceiptLong,
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add an income or expense to start tracking this month.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onAddIncome,
                                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(Strings.get("add_income"))
                            }
                            Button(
                                onClick = onAddExpense,
                                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(Strings.get("add_expense"))
                            }
                        }
                    }
                }
            }
        } else {
            items(currentMonthTransactions.take(8), key = { it.id }) { transaction ->
                TransactionItemCard(
                    transaction = transaction,
                    currency = userProfile.currency,
                    onViewDetails = onViewTransactionDetails,
                    onEdit = onEditTransaction,
                    onDelete = onDeleteTransaction
                )
            }
        }
    }
}
