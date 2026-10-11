package com.hmagr.budgetapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.hmagr.budgetapp.data.BudgetSnapshot

enum class BackupPasswordMode { EXPORT, IMPORT }

@Composable
fun BackupActionDialog(
    onDismiss: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yedek") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Şifreli tek dosyayla kayıtlarını başka bir telefona taşıyabilirsin.")
                TextButton(onClick = onExport) { Text("Yedek dosyası oluştur") }
                TextButton(onClick = onImport) { Text("Yedek dosyasını geri yükle") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Kapat") } },
    )
}

@Composable
fun BackupPasswordDialog(
    mode: BackupPasswordMode,
    onDismiss: () -> Unit,
    onContinue: (String) -> Unit,
) {
    var password by remember(mode) { mutableStateOf("") }
    var repeat by remember(mode) { mutableStateOf("") }
    val exporting = mode == BackupPasswordMode.EXPORT
    val valid = password.length >= 8 && (!exporting || password == repeat)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (exporting) "Yedek parolası belirle" else "Yedek parolasını gir") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (exporting) "En az 8 karakter kullan. Parolayı unutursan dosya geri yüklenemez."
                    else "Dosyayı oluştururken belirlediğin parolayı gir.",
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Parola") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                )
                if (exporting) {
                    OutlinedTextField(
                        value = repeat,
                        onValueChange = { repeat = it },
                        label = { Text("Parolayı tekrar gir") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        isError = repeat.isNotEmpty() && repeat != password,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onContinue(password) }, enabled = valid) {
                Text(if (exporting) "Dosya seç" else "Dosyayı doğrula")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } },
    )
}

@Composable
fun RestoreConfirmDialog(
    snapshot: BudgetSnapshot,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Geri yüklemeyi onayla") },
        text = {
            Text(
                "Dosya doğrulandı: " + snapshot.entries.size + " gelir/yatırım/gider kaydı ve " +
                    snapshot.investmentValues.size + " aylık yatırım değeri var. " +
                    "Telefondaki mevcut kayıtların tamamı bu dosyadakilerle değiştirilecek.",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !busy) { Text("Değiştir ve geri yükle") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Vazgeç") }
        },
    )
}
