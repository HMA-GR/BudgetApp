package com.hmagr.budgetapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

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
        BudgetHomeScreen(BudgetHomeUiState(monthLabel = "Ekim 2026"))
    }
}
