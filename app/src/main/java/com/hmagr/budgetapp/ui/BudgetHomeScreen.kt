package com.hmagr.budgetapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch

enum class EntryType(val label: String) {
    INCOME("Gelir"), INVESTMENT("Yatırım"), EXPENSE("Gider"),
}

data class BudgetEntryUi(
    val title: String,
    val amountCents: Long,
    val type: EntryType,
)

data class BudgetHomeUiState(
    val month: YearMonth,
    val remainingCents: Long? = null,
    val incomeCents: Long = 0,
    val investmentCents: Long = 0,
    val expenseCents: Long = 0,
    val investmentValueCents: Long? = null,
    val entries: List<BudgetEntryUi> = emptyList(),
)

@Composable
fun BudgetHomeScreen(
    state: BudgetHomeUiState,
    onMonthSelected: (YearMonth) -> Unit = {},
    onAddEntry: (EntryType, String, Long) -> Unit = { _, _, _ -> },
    onSaveInvestmentValue: (Long) -> Unit = {},
    onOpenBackup: () -> Unit = {},
    initialPage: Int = 0,
) {
    val types = EntryType.entries
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { types.size })
    val scope = rememberCoroutineScope()
    var monthPickerOpen by remember { mutableStateOf(false) }
    var addType by remember { mutableStateOf<EntryType?>(null) }
    var valueDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    types.forEach { type ->
                        Button(
                            onClick = { addType = type },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                        ) { Text(type.label + " ekle", style = MaterialTheme.typography.labelMedium) }
                    }
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("BudgetApp", style = MaterialTheme.typography.headlineMedium)
                    TextButton(onClick = onOpenBackup) { Text("Yedek") }
                }
                TextButton(onClick = { monthPickerOpen = true }, contentPadding = PaddingValues(0.dp)) {
                    Text(formatMonth(state.month) + "  ▾", style = MaterialTheme.typography.titleMedium)
                }
                MonthlySummary(state)
            }
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                types.forEachIndexed { index, type ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(type.label) },
                    )
                }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                EntryPage(
                    types[page],
                    state.entries.filter { it.type == types[page] },
                    state.investmentCents,
                    state.investmentValueCents,
                    onEditInvestmentValue = { valueDialogOpen = true },
                )
            }
        }
    }
    if (monthPickerOpen) {
        MonthPickerDialog(
            selectedMonth = state.month,
            onDismiss = { monthPickerOpen = false },
            onSelect = {
                onMonthSelected(it)
                monthPickerOpen = false
            },
        )
    }
    addType?.let { type ->
        AddEntryDialog(
            type = type,
            month = state.month,
            onDismiss = { addType = null },
            onAdd = { title, cents ->
                onAddEntry(type, title, cents)
                addType = null
            },
        )
    }
    if (valueDialogOpen) {
        InvestmentValueDialog(
            month = state.month,
            currentValueCents = state.investmentValueCents,
            onDismiss = { valueDialogOpen = false },
            onSave = {
                onSaveInvestmentValue(it)
                valueDialogOpen = false
            },
        )
    }
}

@Composable
private fun MonthlySummary(state: BudgetHomeUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Aylık bütçe", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Kalan", style = MaterialTheme.typography.labelLarge)
                Text(
                    state.remainingCents?.let(::formatMoney) ?: "—",
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AmountLabel("Gelir", state.incomeCents)
                AmountLabel("Gider", state.expenseCents)
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
private fun EntryPage(
    type: EntryType,
    entries: List<BudgetEntryUi>,
    investmentCents: Long,
    investmentValueCents: Long?,
    onEditInvestmentValue: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (type == EntryType.INVESTMENT) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Bu ay yatırıma ayrılan", style = MaterialTheme.typography.labelLarge)
                        Text(formatMoney(investmentCents), style = MaterialTheme.typography.titleLarge)
                        Text("Yatırımların güncel değeri", style = MaterialTheme.typography.labelLarge)
                        Text(
                            investmentValueCents?.let(::formatMoney) ?: "Henüz girilmedi",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        TextButton(onClick = onEditInvestmentValue) {
                            Text(if (investmentValueCents == null) "Güncel değeri gir" else "Güncel değeri güncelle")
                        }
                    }
                }
            }
        }
        if (entries.isEmpty()) {
            item {
                Text(
                    "Bu ay için " + type.label.lowercase(turkishLocale) + " kaydı yok.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            items(entries) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(entry.title, style = MaterialTheme.typography.titleMedium)
                        Text(formatMoney(entry.amountCents), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun InvestmentValueDialog(
    month: YearMonth,
    currentValueCents: Long?,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit,
) {
    var amount by remember(currentValueCents) { mutableStateOf("") }
    val cents = parseMoney(amount, allowZero = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yatırımların güncel değeri") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(formatMonth(month) + " ayının toplam değeri")
                currentValueCents?.let { Text("Önceki değer: " + formatMoney(it)) }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Güncel değer (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = amount.isNotBlank() && cents == null,
                )
                Text(
                    "Bu değer Kalan hesabını değiştirmez; yatırıma ayırdığın tutar değiştirir.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { cents?.let(onSave) }, enabled = cents != null) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } },
    )
}

@Composable
private fun MonthPickerDialog(
    selectedMonth: YearMonth,
    onDismiss: () -> Unit,
    onSelect: (YearMonth) -> Unit,
) {
    var year by remember(selectedMonth) { mutableIntStateOf(selectedMonth.year) }
    val currentMonth = YearMonth.now()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ay seç") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { year-- }) { Text("‹") }
                    Text(year.toString(), style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = { year++ }, enabled = year < currentMonth.year) { Text("›") }
                }
                (1..12).chunked(3).forEach { months ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        months.forEach { number ->
                            val month = YearMonth.of(year, number)
                            TextButton(
                                onClick = { onSelect(month) },
                                enabled = !month.isAfter(currentMonth),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(2.dp),
                            ) { Text(month.month.getDisplayName(TextStyle.SHORT, turkishLocale)) }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Kapat") } },
    )
}

@Composable
private fun AddEntryDialog(
    type: EntryType,
    month: YearMonth,
    onDismiss: () -> Unit,
    onAdd: (String, Long) -> Unit,
) {
    var title by remember(type) { mutableStateOf("") }
    var amount by remember(type) { mutableStateOf("") }
    val cents = parseMoney(amount)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(type.label + " ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(formatMonth(month) + " kayıtları için", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(200) },
                    label = { Text("Açıklama") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Tutar (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = amount.isNotBlank() && cents == null,
                )
                if (amount.isNotBlank() && cents == null) {
                    Text("Pozitif tutarı en fazla iki ondalıkla yaz.", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { cents?.let { onAdd(title.trim(), it) } },
                enabled = title.isNotBlank() && cents != null,
            ) { Text("Ekle") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } },
    )
}

private val turkishLocale = Locale.forLanguageTag("tr-TR")

fun formatMonth(month: YearMonth): String = month
    .format(DateTimeFormatter.ofPattern("LLLL yyyy", turkishLocale))
    .replaceFirstChar { it.titlecase(turkishLocale) }

fun parseMoney(value: String, allowZero: Boolean = false): Long? {
    val normalized = value.trim().replace(',', '.')
    if (!Regex("[0-9]{1,10}(\\.[0-9]{1,2})?").matches(normalized)) return null
    return BigDecimal(normalized).movePointRight(2).longValueExact()
        .takeIf { it > 0 || (allowZero && it == 0L) }
}

private fun formatMoney(cents: Long): String =
    NumberFormat.getCurrencyInstance(turkishLocale).format(BigDecimal.valueOf(cents, 2))

internal fun sampleBudgetHomeUiState() = BudgetHomeUiState(
    month = YearMonth.of(2026, 10),
    remainingCents = 1_320_000,
    incomeCents = 3_000_000,
    investmentCents = 500_000,
    expenseCents = 1_180_000,
    investmentValueCents = 530_000,
    entries = listOf(
        BudgetEntryUi("Maaş", 3_000_000, EntryType.INCOME),
        BudgetEntryUi("Birikim", 500_000, EntryType.INVESTMENT),
        BudgetEntryUi("Market", 420_000, EntryType.EXPENSE),
        BudgetEntryUi("Faturalar", 510_000, EntryType.EXPENSE),
        BudgetEntryUi("Ulaşım", 250_000, EntryType.EXPENSE),
    ),
)

@Preview(name = "Örnek bütçe", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun BudgetHomeWithSampleDataPreview() {
    BudgetTheme { BudgetHomeScreen(sampleBudgetHomeUiState()) }
}

@Preview(name = "Boş bütçe", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun EmptyBudgetHomePreview() {
    BudgetTheme { BudgetHomeScreen(BudgetHomeUiState(month = YearMonth.of(2026, 10))) }
}
