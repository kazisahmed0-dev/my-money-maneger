package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CurrencyFormatter {
    private val decimalFormat = DecimalFormat("#,##,##0.##")

    fun format(amount: Double, currency: String = "৳"): String {
        return "$currency${decimalFormat.format(amount)}"
    }

    fun formatPlain(amount: Double): String {
        return decimalFormat.format(amount)
    }
}

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
    private val displayDateFormat = SimpleDateFormat("d MMMM yyyy", Locale.US)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    private val yearMonthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    private val yearFormat = SimpleDateFormat("yyyy", Locale.US)

    fun todayDateString(): String = dateFormat.format(Date())

    fun currentTimeString(): String = timeFormat.format(Date())

    fun currentMonthYearString(): String = monthYearFormat.format(Date())

    fun currentYearMonthKey(): String = yearMonthKeyFormat.format(Date())

    fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun calculateDayOfWeek(dateString: String, isBangla: Boolean = false): String {
        return try {
            val date = dateFormat.parse(dateString) ?: Date()
            val cal = Calendar.getInstance().apply { time = date }
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> if (isBangla) "রবিবার" else "Sunday"
                Calendar.MONDAY -> if (isBangla) "সোমবার" else "Monday"
                Calendar.TUESDAY -> if (isBangla) "মঙ্গলবার" else "Tuesday"
                Calendar.WEDNESDAY -> if (isBangla) "বুধবার" else "Wednesday"
                Calendar.THURSDAY -> if (isBangla) "বৃহস্পতিবার" else "Thursday"
                Calendar.FRIDAY -> if (isBangla) "শুক্রবার" else "Friday"
                Calendar.SATURDAY -> if (isBangla) "শনিবার" else "Saturday"
                else -> if (isBangla) "সোমবার" else "Monday"
            }
        } catch (e: Exception) {
            if (isBangla) "সোমবার" else "Monday"
        }
    }

    fun formatDisplayDate(dateString: String): String {
        return try {
            val date = dateFormat.parse(dateString) ?: return dateString
            displayDateFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatMonthYear(year: Int, month1Indexed: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month1Indexed - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return monthYearFormat.format(cal.time)
    }

    fun getYearMonthKey(year: Int, month1Indexed: Int): String {
        return String.format(Locale.US, "%04d-%02d", year, month1Indexed)
    }

    fun getMonthName(monthIndex0to11: Int): String {
        val names = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return if (monthIndex0to11 in 0..11) names[monthIndex0to11] else ""
    }

    fun getGreeting(isBangla: Boolean = false): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> if (isBangla) "শুভ সকাল" else "Good Morning"
            hour < 17 -> if (isBangla) "শুভ দুপুর" else "Good Afternoon"
            else -> if (isBangla) "শুভ সন্ধ্যা" else "Good Evening"
        }
    }

    fun daysBetween(dateStr1: String, dateStr2: String): Long {
        return try {
            val d1 = dateFormat.parse(dateStr1) ?: return 0
            val d2 = dateFormat.parse(dateStr2) ?: return 0
            val diff = d2.time - d1.time
            diff / (1000 * 60 * 60 * 24)
        } catch (e: Exception) {
            0
        }
    }
}
