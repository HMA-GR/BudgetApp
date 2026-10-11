package com.hmagr.budgetapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

data class CategorySpendingUi(
    val name: String,
    val spentCents: Long,
    val limitCents: Long,
)

data class BudgetHomeUiState(
    val monthLabel: String,
    val budgetCents: Long? = null,
    val spentCents: Long = 0,
    val categories: List<CategorySpendingUi> = emptyList(),
)

@Composable
fun BudgetHomeScreen(state: BudgetHomeUiState) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("BudgetApp", style = MaterialTheme.typography.headlineMedium)
                Text(state.monthLabel, style = MaterialTheme.typography.titleMedium)
            }

            if (state.budgetCents == null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Bu ay için henüz bütçe yok", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Bütçe eklediğinde kalan tutarı ve kategori harcamalarını burada göreceksin.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                MonthlySummary(state)
                if (state.categories.isNotEmpty()) {
                    Text("Kategoriler", style = MaterialTheme.typography.titleLarge)
                    state.categories.forEach { CategoryRow(it) }
                }
            }
        }
    }
}

@Composable
private fun MonthlySummary(state: BudgetHomeUiState) {
    val budget = requireNotNull(state.budgetCents)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Aylık bütçe", style = MaterialTheme.typography.titleMedium)
            Text(formatMoney(budget), style = MaterialTheme.typography.headlineLarge)
            LinearProgressIndicator(
                progress = { progress(state.spentCents, budget) },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AmountLabel("Harcanan", state.spentCents)
                AmountLabel("Kalan", budget - state.spentCents)
            }
        }
    }
}

@Composable
private fun AmountLabel(label: String, cents: Long) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(formatMoney(cents), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun CategoryRow(category: CategorySpendingUi) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(category.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${formatMoney(category.spentCents)} / ${formatMoney(category.limitCents)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            LinearProgressIndicator(
                progress = { progress(category.spentCents, category.limitCents) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun progress(spentCents: Long, limitCents: Long): Float =
    if (limitCents <= 0) 0f else (spentCents.toFloat() / limitCents).coerceIn(0f, 1f)

private fun formatMoney(cents: Long): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("tr-TR"))
        .format(BigDecimal.valueOf(cents, 2))

@Preview(name = "Örnek bütçe", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun BudgetHomeWithSampleDataPreview() {
    BudgetTheme {
        BudgetHomeScreen(
            BudgetHomeUiState(
                monthLabel = "Ekim 2026",
                budgetCents = 3_000_000,
                spentCents = 1_180_000,
                categories = listOf(
                    CategorySpendingUi("Market", 420_000, 800_000),
                    CategorySpendingUi("Faturalar", 510_000, 650_000),
                    CategorySpendingUi("Ulaşım", 250_000, 400_000),
                ),
            ),
        )
    }
}

@Preview(name = "Boş bütçe", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun EmptyBudgetHomePreview() {
    BudgetTheme {
        BudgetHomeScreen(BudgetHomeUiState(monthLabel = "Ekim 2026"))
    }
}
