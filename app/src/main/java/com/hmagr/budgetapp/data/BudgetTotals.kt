package com.hmagr.budgetapp.data

import com.hmagr.budgetapp.ui.BudgetEntryUi
import com.hmagr.budgetapp.ui.EntryType

data class BudgetTotals(
    val incomeCents: Long,
    val investmentCents: Long,
    val expenseCents: Long,
) {
    val remainingCents: Long
        get() = Math.subtractExact(Math.subtractExact(incomeCents, expenseCents), investmentCents)
}

fun calculateBudgetTotals(entries: List<BudgetEntryUi>): BudgetTotals {
    var income = 0L
    var investment = 0L
    var expense = 0L
    entries.forEach { entry ->
        when (entry.type) {
            EntryType.INCOME -> income = Math.addExact(income, entry.amountCents)
            EntryType.INVESTMENT -> investment = Math.addExact(investment, entry.amountCents)
            EntryType.EXPENSE -> expense = Math.addExact(expense, entry.amountCents)
        }
    }
    return BudgetTotals(income, investment, expense)
}
