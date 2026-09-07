package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SavingsBlue
import com.example.util.CurrencyFormatter
import com.example.util.Strings

@Composable
fun SummaryCardsGrid(
    income: Double,
    expense: Double,
    balance: Double,
    savings: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // TOTAL INCOME CARD
            SummaryMetricCard(
                title = Strings.get("total_income"),
                amount = income,
                currency = currency,
                icon = Icons.Default.ArrowDownward,
                accentColor = IncomeGreen,
                modifier = Modifier.weight(1f).testTag("card_total_income")
            )

            // TOTAL EXPENSE CARD
            SummaryMetricCard(
                title = Strings.get("total_expense"),
                amount = expense,
                currency = currency,
                icon = Icons.Default.ArrowUpward,
                accentColor = ExpenseRed,
                modifier = Modifier.weight(1f).testTag("card_total_expense")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // CURRENT BALANCE CARD
            SummaryMetricCard(
                title = Strings.get("current_balance"),
                amount = balance,
                currency = currency,
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = if (balance >= 0) MaterialTheme.colorScheme.primary else ExpenseRed,
                subtitle = "Income - Expense",
                modifier = Modifier.weight(1f).testTag("card_current_balance")
            )

            // TOTAL SAVINGS CARD
            SummaryMetricCard(
                title = Strings.get("total_savings"),
                amount = savings,
                currency = currency,
                icon = Icons.Default.Savings,
                accentColor = SavingsBlue,
                modifier = Modifier.weight(1f).testTag("card_total_savings")
            )
        }
    }
}

@Composable
fun SummaryMetricCard(
    title: String,
    amount: Double,
    currency: String,
    icon: ImageVector,
    accentColor: Color,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = CurrencyFormatter.format(amount, currency),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddSaving: () -> Unit,
    onAddGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = Strings.get("quick_actions"),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                label = Strings.get("add_income"),
                icon = Icons.Default.AddCircle,
                accentColor = IncomeGreen,
                onClick = onAddIncome,
                modifier = Modifier.weight(1f).testTag("action_add_income")
            )
            QuickActionButton(
                label = Strings.get("add_expense"),
                icon = Icons.Default.RemoveCircle,
                accentColor = ExpenseRed,
                onClick = onAddExpense,
                modifier = Modifier.weight(1f).testTag("action_add_expense")
            )
            QuickActionButton(
                label = Strings.get("add_saving"),
                icon = Icons.Default.Savings,
                accentColor = SavingsBlue,
                onClick = onAddSaving,
                modifier = Modifier.weight(1f).testTag("action_add_saving")
            )
            QuickActionButton(
                label = Strings.get("add_goal"),
                icon = Icons.Default.Flag,
                accentColor = MaterialTheme.colorScheme.primary,
                onClick = onAddGoal,
                modifier = Modifier.weight(1f).testTag("action_add_goal")
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.09f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
