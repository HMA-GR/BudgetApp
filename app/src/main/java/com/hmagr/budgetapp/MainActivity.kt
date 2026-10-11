package com.hmagr.budgetapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hmagr.budgetapp.ui.BudgetHomeScreen
import com.hmagr.budgetapp.ui.BudgetHomeUiState
import com.hmagr.budgetapp.ui.BudgetTheme
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val turkish = Locale.forLanguageTag("tr-TR")
        val monthLabel = YearMonth.now()
            .format(DateTimeFormatter.ofPattern("LLLL yyyy", turkish))
            .replaceFirstChar { it.titlecase(turkish) }

        setContent {
            BudgetTheme {
                BudgetHomeScreen(BudgetHomeUiState(monthLabel = monthLabel))
            }
        }
    }
}
