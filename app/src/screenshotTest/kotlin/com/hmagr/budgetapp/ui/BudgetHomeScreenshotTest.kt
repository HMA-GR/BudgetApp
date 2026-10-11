package com.hmagr.budgetapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import java.time.YearMonth

@PreviewTest
@Preview(name = "Örnek bütçe", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun BudgetHomeSampleScreenshot() {
    BudgetTheme {
        BudgetHomeScreen(sampleBudgetHomeUiState())
    }
}

@PreviewTest
@Preview(name = "Boş bütçe", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun BudgetHomeEmptyScreenshot() {
    BudgetTheme {
        BudgetHomeScreen(BudgetHomeUiState(month = YearMonth.of(2026, 10)))
    }
}

@PreviewTest
@Preview(name = "Örnek gider bölümü", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun BudgetHomeExpenseScreenshot() {
    BudgetTheme {
        BudgetHomeScreen(sampleBudgetHomeUiState(), initialPage = 2)
    }
}

@PreviewTest
@Preview(name = "Örnek yatırım bölümü", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun BudgetHomeInvestmentScreenshot() {
    BudgetTheme {
        BudgetHomeScreen(sampleBudgetHomeUiState(), initialPage = 1)
    }
}

@PreviewTest
@Preview(name = "Gider kategorileri", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun BudgetCategoryManagerScreenshot() {
    BudgetTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Card {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Gider kategorileri", style = MaterialTheme.typography.headlineSmall)
                        CategoryManagerContent(
                            type = EntryType.EXPENSE,
                            categories = sampleBudgetHomeUiState().categories.filter { it.type == EntryType.EXPENSE },
                            onAdd = {},
                            onRename = { _, _ -> },
                        )
                        TextButton(onClick = {}) { Text("Kapat") }
                    }
                }
            }
        }
    }
}
