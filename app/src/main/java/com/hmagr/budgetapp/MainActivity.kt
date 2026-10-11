package com.hmagr.budgetapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.hmagr.budgetapp.data.BudgetDatabase
import com.hmagr.budgetapp.data.BackupCodec
import com.hmagr.budgetapp.data.BudgetSnapshot
import com.hmagr.budgetapp.data.calculateBudgetTotals
import com.hmagr.budgetapp.ui.BackupActionDialog
import com.hmagr.budgetapp.ui.BackupPasswordDialog
import com.hmagr.budgetapp.ui.BackupPasswordMode
import com.hmagr.budgetapp.ui.BudgetEntryUi
import com.hmagr.budgetapp.ui.BudgetHomeScreen
import com.hmagr.budgetapp.ui.BudgetHomeUiState
import com.hmagr.budgetapp.ui.BudgetTheme
import com.hmagr.budgetapp.ui.RestoreConfirmDialog
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class LoadedMonth(
    val month: YearMonth,
    val entries: List<BudgetEntryUi>,
    val investmentValueCents: Long?,
)

class MainActivity : ComponentActivity() {
    private val database = lazy { BudgetDatabase(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BudgetTheme {
                var monthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
                val month = YearMonth.parse(monthText)
                var revision by remember { mutableIntStateOf(0) }
                var loadedMonth by remember { mutableStateOf<LoadedMonth?>(null) }
                var backupActionOpen by remember { mutableStateOf(false) }
                var passwordMode by remember { mutableStateOf<BackupPasswordMode?>(null) }
                var pendingExportBytes by remember { mutableStateOf<ByteArray?>(null) }
                var pendingImportBytes by remember { mutableStateOf<ByteArray?>(null) }
                var pendingRestore by remember { mutableStateOf<BudgetSnapshot?>(null) }
                var restoreInProgress by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()
                val createDocument = rememberLauncherForActivityResult(
                    ActivityResultContracts.CreateDocument("application/octet-stream"),
                ) { uri ->
                    val bytes = pendingExportBytes
                    pendingExportBytes = null
                    if (uri != null && bytes != null) {
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    contentResolver.openOutputStream(uri, "w")?.use { it.write(bytes) }
                                        ?: error("Dosya açılamadı")
                                }
                                Toast.makeText(this@MainActivity, "Şifreli yedek oluşturuldu", Toast.LENGTH_SHORT).show()
                            } catch (error: Exception) {
                                Toast.makeText(this@MainActivity, "Yedek yazılamadı", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                val openDocument = rememberLauncherForActivityResult(
                    ActivityResultContracts.OpenDocument(),
                ) { uri ->
                    if (uri != null) {
                        scope.launch {
                            try {
                                pendingImportBytes = withContext(Dispatchers.IO) {
                                    contentResolver.openInputStream(uri)?.use(BackupCodec::readLimited)
                                        ?: error("Dosya açılamadı")
                                }
                                passwordMode = BackupPasswordMode.IMPORT
                            } catch (error: Exception) {
                                Toast.makeText(this@MainActivity, "Yedek okunamadı", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                LaunchedEffect(month, revision) {
                    loadedMonth = withContext(Dispatchers.IO) {
                        LoadedMonth(
                            month = month,
                            entries = database.value.entriesForMonth(month),
                            investmentValueCents = database.value.investmentValueForMonth(month),
                        )
                    }
                }

                val current = loadedMonth?.takeIf { it.month == month }
                val entries = current?.entries.orEmpty()
                val totals = calculateBudgetTotals(entries)
                BudgetHomeScreen(
                    state = BudgetHomeUiState(
                        month = month,
                        remainingCents = totals.remainingCents,
                        incomeCents = totals.incomeCents,
                        investmentCents = totals.investmentCents,
                        expenseCents = totals.expenseCents,
                        investmentValueCents = current?.investmentValueCents,
                        entries = entries,
                    ),
                    onMonthSelected = { monthText = it.toString() },
                    onAddEntry = { type, title, cents ->
                        val targetMonth = month
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    database.value.addEntry(targetMonth, type, title, cents)
                                }
                                revision++
                            } catch (error: Exception) {
                                Toast.makeText(this@MainActivity, "Kayıt yapılamadı", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onSaveInvestmentValue = { cents ->
                        val targetMonth = month
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    database.value.setInvestmentValue(targetMonth, cents)
                                }
                                revision++
                            } catch (error: Exception) {
                                Toast.makeText(this@MainActivity, "Değer kaydedilemedi", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onOpenBackup = { backupActionOpen = true },
                )

                if (backupActionOpen) {
                    BackupActionDialog(
                        onDismiss = { backupActionOpen = false },
                        onExport = {
                            backupActionOpen = false
                            passwordMode = BackupPasswordMode.EXPORT
                        },
                        onImport = {
                            backupActionOpen = false
                            openDocument.launch(arrayOf("*/*"))
                        },
                    )
                }
                passwordMode?.let { mode ->
                    BackupPasswordDialog(
                        mode = mode,
                        onDismiss = {
                            passwordMode = null
                            pendingImportBytes = null
                        },
                        onContinue = { password ->
                            passwordMode = null
                            val characters = password.toCharArray()
                            scope.launch {
                                try {
                                    if (mode == BackupPasswordMode.EXPORT) {
                                        pendingExportBytes = withContext(Dispatchers.IO) {
                                            BackupCodec.encode(database.value.snapshot(), characters)
                                        }
                                        createDocument.launch("BudgetApp-" + LocalDate.now() + ".budget")
                                    } else {
                                        val file = requireNotNull(pendingImportBytes)
                                        pendingRestore = withContext(Dispatchers.IO) {
                                            BackupCodec.decode(file, characters)
                                        }
                                        pendingImportBytes = null
                                    }
                                } catch (error: Exception) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        if (mode == BackupPasswordMode.EXPORT) "Yedek oluşturulamadı"
                                        else "Parola yanlış veya dosya geçersiz",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                    if (mode == BackupPasswordMode.IMPORT) {
                                        passwordMode = BackupPasswordMode.IMPORT
                                    }
                                } finally {
                                    characters.fill('\u0000')
                                }
                            }
                        },
                    )
                }
                pendingRestore?.let { snapshot ->
                    RestoreConfirmDialog(
                        snapshot = snapshot,
                        busy = restoreInProgress,
                        onDismiss = { pendingRestore = null },
                        onConfirm = {
                            restoreInProgress = true
                            scope.launch {
                                try {
                                    withContext(Dispatchers.IO) { database.value.replaceAll(snapshot) }
                                    pendingRestore = null
                                    revision++
                                    Toast.makeText(this@MainActivity, "Yedek geri yüklendi", Toast.LENGTH_SHORT).show()
                                } catch (error: Exception) {
                                    Toast.makeText(this@MainActivity, "Geri yükleme başarısız", Toast.LENGTH_SHORT).show()
                                } finally {
                                    restoreInProgress = false
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        if (database.isInitialized()) database.value.close()
        super.onDestroy()
    }
}
