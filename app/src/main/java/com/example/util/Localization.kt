package com.example.util

/**
 * Supports English and Bangla translations for all major UI text in the app.
 */
enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    BANGLA("bn", "বাংলা")
}

object Strings {
    private var currentLang: AppLanguage = AppLanguage.ENGLISH

    fun setLanguage(lang: AppLanguage) {
        currentLang = lang
    }

    fun setLanguage(code: String) {
        currentLang = if (code.equals("bn", ignoreCase = true)) AppLanguage.BANGLA else AppLanguage.ENGLISH
    }

    fun getLanguage(): AppLanguage = currentLang

    // Translations mapping
    fun get(key: String): String {
        val isBn = currentLang == AppLanguage.BANGLA
        return when (key) {
            // App Title & Headers
            "app_name" -> if (isBn) "মাই মানি ম্যানেজার" else "My Money Manager"
            "good_morning" -> if (isBn) "শুভ সকাল" else "Good Morning"
            "good_afternoon" -> if (isBn) "শুভ দুপুর" else "Good Afternoon"
            "good_evening" -> if (isBn) "শুভ সন্ধ্যা" else "Good Evening"
            "welcome" -> if (isBn) "স্বাগতম" else "Welcome"

            // Dashboard Summary
            "total_income" -> if (isBn) "মোট আয়" else "TOTAL INCOME"
            "total_expense" -> if (isBn) "মোট ব্যয়" else "TOTAL EXPENSE"
            "current_balance" -> if (isBn) "বর্তমান ব্যালেন্স" else "CURRENT BALANCE"
            "total_savings" -> if (isBn) "মোট সঞ্চয়" else "TOTAL SAVINGS"
            "balance" -> if (isBn) "ব্যালেন্স" else "Balance"
            "income" -> if (isBn) "আয়" else "Income"
            "expense" -> if (isBn) "ব্যয়" else "Expense"
            "saving" -> if (isBn) "সঞ্চয়" else "Saving"
            "savings" -> if (isBn) "সঞ্চয়" else "Savings"

            // Quick Actions
            "quick_actions" -> if (isBn) "দ্রুত অ্যাকশন" else "Quick Actions"
            "add_income" -> if (isBn) "আয় যোগ করুন" else "Add Income"
            "add_expense" -> if (isBn) "ব্যয় যোগ করুন" else "Add Expense"
            "add_saving" -> if (isBn) "সঞ্চয় যোগ করুন" else "Add Saving"
            "add_goal" -> if (isBn) "লক্ষ্য যোগ করুন" else "Add Goal"

            // Navigation
            "nav_home" -> if (isBn) "হোম" else "Home"
            "nav_transactions" -> if (isBn) "লেনদেন" else "Transactions"
            "nav_reports" -> if (isBn) "রিপোর্ট" else "Reports"
            "nav_goals" -> if (isBn) "লক্ষ্য ও বাজেট" else "Goals & Budget"
            "nav_settings" -> if (isBn) "সেটিংস" else "Settings"

            // Forms & Fields
            "name" -> if (isBn) "নাম" else "Name"
            "amount" -> if (isBn) "পরিমাণ" else "Amount"
            "date" -> if (isBn) "তারিখ" else "Date"
            "time" -> if (isBn) "সময়" else "Time"
            "day" -> if (isBn) "বার / দিন" else "Day"
            "category" -> if (isBn) "ক্যাটাগরি" else "Category"
            "note" -> if (isBn) "নোট (ঐচ্ছিক)" else "Note (Optional)"
            "receipt_image" -> if (isBn) "রশিদ / ছবি" else "Receipt Image"
            "save" -> if (isBn) "সংরক্ষণ করুন" else "Save"
            "cancel" -> if (isBn) "বাতিল" else "Cancel"
            "delete" -> if (isBn) "মুছুন" else "Delete"
            "edit" -> if (isBn) "সম্পাদনা" else "Edit"
            "details" -> if (isBn) "বিস্তারিত" else "Details"
            "confirm" -> if (isBn) "নিশ্চিত করুন" else "Confirm"
            "warning" -> if (isBn) "সতর্কতা" else "Warning"

            // Time filters
            "all" -> if (isBn) "সকল" else "All"
            "today" -> if (isBn) "আজ" else "Today"
            "this_week" -> if (isBn) "এই সপ্তাহ" else "This Week"
            "this_month" -> if (isBn) "এই মাস" else "This Month"
            "this_year" -> if (isBn) "এই বছর" else "This Year"
            "custom" -> if (isBn) "কাস্টম রেঞ্জ" else "Custom Range"

            // Reports & Money Flow
            "money_flow" -> if (isBn) "অর্থের প্রবাহ" else "Money Flow"
            "available_balance" -> if (isBn) "ব্যবহারযোগ্য ব্যালেন্স" else "AVAILABLE BALANCE"
            "highest_income" -> if (isBn) "সর্বোচ্চ আয়" else "Highest Income"
            "highest_expense" -> if (isBn) "সর্বোচ্চ ব্যয়" else "Highest Expense"
            "largest_expense_category" -> if (isBn) "সবচেয়ে বড় ব্যয়ের খাত" else "Largest Expense Category"
            "most_used_income_source" -> if (isBn) "প্রধান আয়ের উৎস" else "Most Used Income Source"
            "most_used_expense_category" -> if (isBn) "সর্বাধিক ব্যবহৃত ব্যয়ের খাত" else "Most Used Expense Category"
            "number_of_transactions" -> if (isBn) "মোট লেনদেন সংখ্যা" else "Number of Transactions"
            "savings_rate" -> if (isBn) "সঞ্চয়ের হার" else "Savings Rate"
            "savings_this_month" -> if (isBn) "চলতি মাসের সঞ্চয়" else "Savings This Month"

            // Insights
            "money_insights" -> if (isBn) "আপনার আর্থিক পর্যবেক্ষণ" else "Your Money Insights"

            // Yearly Overview
            "yearly_overview" -> if (isBn) "বার্ষিক সার্বিক বিবরণ" else "Yearly Overview"
            "yearly_income" -> if (isBn) "মোট বার্ষিক আয়" else "TOTAL YEARLY INCOME"
            "yearly_expense" -> if (isBn) "মোট বার্ষিক ব্যয়" else "TOTAL YEARLY EXPENSE"
            "yearly_savings" -> if (isBn) "মোট বার্ষিক সঞ্চয়" else "TOTAL YEARLY SAVINGS"
            "yearly_balance" -> if (isBn) "মোট বার্ষিক ব্যালেন্স" else "TOTAL YEARLY BALANCE"
            "income_distribution" -> if (isBn) "বার্ষিক আয়ের উৎস বণ্টন (১০০%)" else "Yearly Income Distribution (100%)"
            "expense_distribution" -> if (isBn) "বার্ষিক ব্যয়ের খাত বণ্টন (১০০%)" else "Yearly Expense Distribution (100%)"
            "monthly_contribution" -> if (isBn) "বার্ষিক মোটে মাসের অবদান" else "Monthly Contribution to Yearly Total"
            "delete_month_data" -> if (isBn) "মাসের ডাটা মুছুন" else "Delete Month Data"

            // Goals & Budget
            "savings_goals" -> if (isBn) "সঞ্চয় লক্ষ্যসমূহ" else "Savings Goals"
            "target" -> if (isBn) "লক্ষ্য" else "Target"
            "saved" -> if (isBn) "সঞ্চিত" else "Saved"
            "remaining" -> if (isBn) "বাকি" else "Remaining"
            "add_money" -> if (isBn) "টাকা যোগ করুন" else "Add Money"
            "budget" -> if (isBn) "বাজেট" else "Budget"
            "monthly_budget" -> if (isBn) "মাসিক সার্বিক বাজেট" else "Monthly Overall Budget"
            "category_budgets" -> if (isBn) "ক্যাটাগরি অনুযায়ী বাজেট" else "Category Budgets"
            "budget_used" -> if (isBn) "ব্যবহৃত" else "Used"

            // Recurring
            "recurring_transactions" -> if (isBn) "নিয়মিত লেনদেন (রিক্যারিং)" else "Recurring Transactions"
            "frequency" -> if (isBn) "পৌনঃপুনিকতা" else "Frequency"
            "daily" -> if (isBn) "প্রতিদিন" else "Daily"
            "weekly" -> if (isBn) "সাপ্তাহিক" else "Weekly"
            "monthly" -> if (isBn) "মাসিক" else "Monthly"
            "yearly" -> if (isBn) "বার্ষিক" else "Yearly"

            // Settings & Security
            "profile" -> if (isBn) "প্রোফাইল" else "Profile"
            "change_name" -> if (isBn) "নাম পরিবর্তন" else "Change Name"
            "currency" -> if (isBn) "মুদ্রা" else "Currency"
            "language" -> if (isBn) "ভাষা" else "Language"
            "theme" -> if (isBn) "থিম" else "Theme"
            "light_mode" -> if (isBn) "লাইট মোড" else "Light Mode"
            "dark_mode" -> if (isBn) "ডার্ক মোড" else "Dark Mode"
            "system_default" -> if (isBn) "সিস্টেম ডিফল্ট" else "System Default"
            "pin_lock" -> if (isBn) "পিন লক সুরক্ষা" else "PIN Lock Protection"
            "enable_pin" -> if (isBn) "পিন লক সক্রিয় করুন" else "Enable PIN Lock"
            "notifications" -> if (isBn) "বিজ্ঞপ্তি ও অনুস্মারক" else "Notifications"
            "export_csv" -> if (isBn) "CSV হিসেবে এক্সপোর্ট" else "Export Data (CSV)"
            "backup_data" -> if (isBn) "ব্যাকআপ ডাটা (JSON)" else "Backup Data (JSON)"
            "restore_data" -> if (isBn) "ডাটা রিস্টোর করুন" else "Restore Data (JSON)"
            "manage_categories" -> if (isBn) "ক্যাটাগরি ব্যবস্থাপনা" else "Manage Categories"
            "clear_sample_data" -> if (isBn) "নমুনা ডাটা মুছুন" else "Clear Sample Data"
            "clear_all_data" -> if (isBn) "সব তথ্য মুছে ফেলুন" else "Clear All Data"

            // Placeholders & Empty
            "no_transactions" -> if (isBn) "এখনও কোনো লেনদেন নেই।" else "No transactions yet."
            "no_goals" -> if (isBn) "এখনও কোনো সঞ্চয় লক্ষ্য তৈরি করা হয়নি।" else "No savings goals yet."
            "no_budgets" -> if (isBn) "কোনো বাজেট নির্ধারণ করা হয়নি।" else "No budgets set yet."
            "search_placeholder" -> if (isBn) "নাম, ক্যাটাগরি, তারিখ বা নোট দিয়ে খুঁজুন..." else "Search by name, category, amount, note..."

            else -> key
        }
    }
}
