package com.example

import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.util.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyManagerLogicTest {

    @Test
    fun testDayOfWeekCalculation() {
        // 2026-09-07 is Monday
        val day = DateUtils.calculateDayOfWeek("2026-09-07")
        assertEquals("Monday", day)

        // 2026-09-08 is Tuesday
        val nextDay = DateUtils.calculateDayOfWeek("2026-09-08")
        assertEquals("Tuesday", nextDay)
    }

    @Test
    fun testCurrencyFormatting() {
        val formatted = CurrencyFormatter.format(5000.0, "৳")
        assertEquals("৳5,000", formatted)

        val formattedDec = CurrencyFormatter.format(1250.5, "৳")
        assertEquals("৳1,250.5", formattedDec)
    }

    @Test
    fun testBalanceAndDistribution() {
        val income = 45000.0
        val expense = 11000.0
        val balance = income - expense
        assertEquals(34000.0, balance, 0.001)

        val tuitionIncome = 10000.0
        val tuitionPct = (tuitionIncome / income) * 100.0
        assertTrue(tuitionPct > 22.0 && tuitionPct < 23.0)
    }

    @Test
    fun testLocalizationSwitch() {
        Strings.setLanguage("en")
        assertEquals("TOTAL INCOME", Strings.get("total_income"))

        Strings.setLanguage("bn")
        assertEquals("মোট আয়", Strings.get("total_income"))
    }
}
