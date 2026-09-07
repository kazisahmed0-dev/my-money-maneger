package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.UserProfile
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.Strings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfile,
    categories: List<CategoryEntity>,
    onSaveProfile: (UserProfile) -> Unit,
    onAddCategory: (String, String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onExportCsv: suspend () -> String,
    onBackupJson: suspend () -> String,
    onRestoreJson: suspend (String) -> Boolean,
    onLoadSampleData: () -> Unit,
    onDeleteSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Dialog states
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showManageCategoriesDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showClearDataConfirmDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = Strings.get("nav_settings"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Snackbar notification
        if (snackbarMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(snackbarMessage ?: "", style = MaterialTheme.typography.bodySmall)
                        IconButton(onClick = { snackbarMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                }
            }
        }

        // Section: Personal Profile
        item {
            SettingsGroupCard(title = "Profile & Targets") {
                SettingsActionRow(
                    icon = Icons.Default.Person,
                    title = "Personal Information",
                    subtitle = if (userProfile.name.isNotBlank()) userProfile.name else "Not set",
                    onClick = { showEditProfileDialog = true }
                )
            }
        }

        // Section: Preferences
        item {
            SettingsGroupCard(title = "Preferences") {
                SettingsActionRow(
                    icon = Icons.Default.Language,
                    title = Strings.get("language"),
                    subtitle = if (userProfile.language == "bn") "বাংলা (Bangla)" else "English",
                    onClick = { showLanguageDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsActionRow(
                    icon = Icons.Default.Paid,
                    title = Strings.get("currency"),
                    subtitle = "${userProfile.currency} (${when (userProfile.currency) { "৳" -> "Bangladeshi Taka"; "$" -> "US Dollar"; "€" -> "Euro"; "£" -> "British Pound"; else -> userProfile.currency }})",
                    onClick = { showCurrencyDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsActionRow(
                    icon = Icons.Default.DarkMode,
                    title = Strings.get("theme"),
                    subtitle = when (userProfile.themeMode) {
                        "LIGHT" -> "Light Theme"
                        "DARK" -> "Dark Theme"
                        else -> "System Default"
                    },
                    onClick = { showThemeDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsActionRow(
                    icon = Icons.Default.Category,
                    title = "Manage Categories",
                    subtitle = "${categories.size} categories available",
                    onClick = { showManageCategoriesDialog = true }
                )
            }
        }

        // Section: Security & Notifications
        item {
            SettingsGroupCard(title = "Security & Notifications") {
                SettingsActionRow(
                    icon = Icons.Default.Lock,
                    title = Strings.get("pin_lock"),
                    subtitle = if (userProfile.isPinEnabled) "PIN Protected" else "Disabled",
                    onClick = { showPinDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(Strings.get("notifications"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Budget & reminder alerts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = userProfile.notificationsEnabled,
                        onCheckedChange = { onSaveProfile(userProfile.copy(notificationsEnabled = it)) }
                    )
                }
            }
        }

        // Section: Data & Backup
        item {
            SettingsGroupCard(title = "Data Management & Export") {
                SettingsActionRow(
                    icon = Icons.Default.FileDownload,
                    title = Strings.get("export_csv"),
                    subtitle = "Export all transactions as CSV spreadsheet",
                    onClick = {
                        coroutineScope.launch {
                            val csvData = onExportCsv()
                            shareText(context, csvData, "My_Money_Manager_Transactions.csv", "text/csv")
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsActionRow(
                    icon = Icons.Default.Backup,
                    title = Strings.get("backup_data"),
                    subtitle = "Create offline JSON backup file",
                    onClick = {
                        coroutineScope.launch {
                            val json = onBackupJson()
                            shareText(context, json, "money_manager_backup.json", "application/json")
                            snackbarMessage = "Backup generated! Save or share it safely."
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsActionRow(
                    icon = Icons.Default.Restore,
                    title = Strings.get("restore_data"),
                    subtitle = "Restore transactions from JSON backup",
                    onClick = { showRestoreDialog = true }
                )
            }
        }

        // Section: Sample Data & Danger Zone
        item {
            SettingsGroupCard(title = "Sample Data & Reset") {
                SettingsActionRow(
                    icon = Icons.Default.Science,
                    title = Strings.get("load_sample_data"),
                    subtitle = "Insert demo income, expenses & savings fund",
                    onClick = {
                        onLoadSampleData()
                        snackbarMessage = "Sample data loaded successfully!"
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsActionRow(
                    icon = Icons.Default.CleaningServices,
                    title = Strings.get("delete_sample_data"),
                    subtitle = "Remove only sample demo transactions",
                    onClick = {
                        onDeleteSampleData()
                        snackbarMessage = "Sample data removed."
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsActionRow(
                    icon = Icons.Default.DeleteForever,
                    title = Strings.get("clear_all_data"),
                    subtitle = "Erase all transactions, goals, and budgets",
                    titleColor = ExpenseRed,
                    onClick = { showClearDataConfirmDialog = true }
                )
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var name by remember { mutableStateOf(userProfile.name) }
        var incomeTarget by remember { mutableStateOf(if (userProfile.incomeTarget > 0) userProfile.incomeTarget.toString() else "") }
        var spendingBudget by remember { mutableStateOf(if (userProfile.spendingBudget > 0) userProfile.spendingBudget.toString() else "") }
        var savingsGoal by remember { mutableStateOf(if (userProfile.savingsGoal > 0) userProfile.savingsGoal.toString() else "") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile & Targets") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Your Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = incomeTarget,
                        onValueChange = { incomeTarget = it },
                        label = { Text("Monthly Income Target (${userProfile.currency})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = spendingBudget,
                        onValueChange = { spendingBudget = it },
                        label = { Text("Monthly Spending Budget (${userProfile.currency})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = savingsGoal,
                        onValueChange = { savingsGoal = it },
                        label = { Text("Savings Goal (${userProfile.currency})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveProfile(
                            userProfile.copy(
                                name = name.trim(),
                                incomeTarget = incomeTarget.toDoubleOrNull() ?: 0.0,
                                spendingBudget = spendingBudget.toDoubleOrNull() ?: 0.0,
                                savingsGoal = savingsGoal.toDoubleOrNull() ?: 0.0
                            )
                        )
                        showEditProfileDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(Strings.get("language")) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            onSaveProfile(userProfile.copy(language = "en"))
                            showLanguageDialog = false
                        }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = userProfile.language == "en", onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("English", fontWeight = FontWeight.Medium)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            onSaveProfile(userProfile.copy(language = "bn"))
                            showLanguageDialog = false
                        }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = userProfile.language == "bn", onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("বাংলা (Bangla)", fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Currency Dialog
    if (showCurrencyDialog) {
        val currencies = listOf(
            "৳" to "Bangladeshi Taka (৳)",
            "$" to "US Dollar ($)",
            "€" to "Euro (€)",
            "£" to "British Pound (£)",
            "₹" to "Indian Rupee (₹)"
        )
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text(Strings.get("currency")) },
            text = {
                Column {
                    currencies.forEach { (symbol, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onSaveProfile(userProfile.copy(currency = symbol))
                                showCurrencyDialog = false
                            }.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = userProfile.currency == symbol, onClick = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        val themes = listOf(
            "SYSTEM" to "System Default",
            "LIGHT" to "Light Theme",
            "DARK" to "Dark Theme"
        )
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(Strings.get("theme")) },
            text = {
                Column {
                    themes.forEach { (mode, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onSaveProfile(userProfile.copy(themeMode = mode))
                                showThemeDialog = false
                            }.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = userProfile.themeMode == mode, onClick = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // PIN Lock Config Dialog
    if (showPinDialog) {
        var pinEnabled by remember { mutableStateOf(userProfile.isPinEnabled) }
        var pinCode by remember { mutableStateOf(userProfile.pinCode) }

        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("PIN Lock Protection") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable PIN Lock")
                        Switch(checked = pinEnabled, onCheckedChange = { pinEnabled = it })
                    }

                    if (pinEnabled) {
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinCode = it },
                            label = { Text("4-digit PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!pinEnabled || pinCode.length == 4) {
                            onSaveProfile(
                                userProfile.copy(
                                    isPinEnabled = pinEnabled,
                                    pinCode = if (pinEnabled) pinCode else ""
                                )
                            )
                            showPinDialog = false
                            snackbarMessage = if (pinEnabled) "PIN lock enabled." else "PIN lock disabled."
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Manage Categories Dialog
    if (showManageCategoriesDialog) {
        var newCatName by remember { mutableStateOf("") }
        var newCatType by remember { mutableStateOf("EXPENSE") }

        AlertDialog(
            onDismissRequest = { showManageCategoriesDialog = false },
            title = { Text("Manage Categories") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = newCatType == "EXPENSE",
                            onClick = { newCatType = "EXPENSE" },
                            label = { Text("Expense") }
                        )
                        FilterChip(
                            selected = newCatType == "INCOME",
                            onClick = { newCatType = "INCOME" },
                            label = { Text("Income") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCatName,
                            onValueChange = { newCatName = it },
                            label = { Text("New Category") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newCatName.isNotBlank()) {
                                    onAddCategory(newCatName.trim(), newCatType)
                                    newCatName = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Existing Categories:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(categories, key = { "${it.type}_${it.name}" }) { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${cat.name} (${cat.type})",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (cat.isCustom) {
                                    IconButton(
                                        onClick = { onDeleteCategory(cat) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showManageCategoriesDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Restore from JSON Dialog
    if (showRestoreDialog) {
        var jsonInput by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text(Strings.get("restore_data")) },
            text = {
                Column {
                    Text(
                        "Paste your JSON backup data below. This will restore your transactions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = {
                            jsonInput = it
                            isError = false
                        },
                        label = { Text("JSON Backup Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        maxLines = 8
                    )
                    if (isError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Invalid JSON backup structure. Please verify.", color = ExpenseRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = onRestoreJson(jsonInput.trim())
                            if (success) {
                                showRestoreDialog = false
                                snackbarMessage = "Transactions restored successfully!"
                            } else {
                                isError = true
                            }
                        }
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Danger Zone: Clear All Data Confirmation Dialog
    if (showClearDataConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirmDialog = false },
            title = { Text("Clear All Data") },
            text = {
                Text(
                    "Are you sure you want to delete all transactions, savings goals, and budgets?\n\nThis action is permanent and cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearDataConfirmDialog = false
                        snackbarMessage = "All application data has been cleared."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier.testTag("btn_confirm_clear_all_data")
                ) {
                    Text("Yes, Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsGroupCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = titleColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = titleColor)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun shareText(context: Context, text: String, title: String, mimeType: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_TITLE, title)
        type = mimeType
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share $title")
    context.startActivity(shareIntent)
}
