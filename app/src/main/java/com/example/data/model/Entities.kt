package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME,
    EXPENSE,
    SAVING
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME", "EXPENSE", "SAVING"
    val category: String,
    val date: String, // "YYYY-MM-DD"
    val dayOfWeek: String, // "Monday", etc.
    val time: String, // "5:30 PM"
    val note: String = "",
    val receiptUri: String? = null,
    val isFavorite: Boolean = false,
    val isSample: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "INCOME" or "EXPENSE"
    val iconName: String = "category",
    val isCustom: Boolean = false
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val targetDate: String? = null, // "YYYY-MM-DD"
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "OVERALL" or category name
    val monthlyLimit: Double,
    val monthYear: String = "DEFAULT" // "DEFAULT" or "YYYY-MM"
)

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val frequency: String, // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val startDate: String,
    val nextDueDate: String,
    val active: Boolean = true
)

data class UserProfile(
    val name: String = "",
    val incomeTarget: Double = 0.0,
    val spendingBudget: Double = 0.0,
    val savingsGoal: Double = 0.0,
    val currency: String = "৳",
    val language: String = "en", // "en" or "bn"
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val isPinEnabled: Boolean = false,
    val pinCode: String = "",
    val notificationsEnabled: Boolean = true,
    val isOnboarded: Boolean = false
)
