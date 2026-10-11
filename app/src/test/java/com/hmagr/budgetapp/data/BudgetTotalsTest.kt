package com.hmagr.budgetapp.data

import com.hmagr.budgetapp.ui.BudgetEntryUi
import com.hmagr.budgetapp.ui.EntryType
import com.hmagr.budgetapp.ui.parseMoney
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BudgetTotalsTest {
    @Test
    fun remainingSubtractsExpensesAndAllocatedInvestments() {
        val totals = calculateBudgetTotals(
            listOf(
                BudgetEntryUi("Maaş", 300_000, EntryType.INCOME),
                BudgetEntryUi("Ek gelir", 25_000, EntryType.INCOME),
                BudgetEntryUi("Market", 40_000, EntryType.EXPENSE),
                BudgetEntryUi("Birikim", 75_000, EntryType.INVESTMENT),
            ),
        )

        assertEquals(325_000, totals.incomeCents)
        assertEquals(40_000, totals.expenseCents)
        assertEquals(75_000, totals.investmentCents)
        assertEquals(210_000, totals.remainingCents)
    }

    @Test
    fun turkishMoneyInputKeepsExactCentsAndRejectsInvalidAmounts() {
        assertEquals(12_345L, parseMoney("123,45"))
        assertEquals(12_345L, parseMoney("123.45"))
        assertNull(parseMoney("12,345"))
        assertNull(parseMoney("-1"))
        assertNull(parseMoney("0"))
        assertEquals(0L, parseMoney("0", allowZero = true))
    }
}
