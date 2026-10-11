package com.hmagr.budgetapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun CategoryManagerDialog(
    type: EntryType,
    categories: List<BudgetCategoryUi>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    onRename: (Long, String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(type.label + " kategorileri") },
        text = {
            CategoryManagerContent(type, categories, onAdd, onRename)
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Kapat") } },
    )
}

@Composable
internal fun CategoryManagerContent(
    type: EntryType,
    categories: List<BudgetCategoryUi>,
    onAdd: (String) -> Unit,
    onRename: (Long, String) -> Unit,
) {
    var newName by remember(type) { mutableStateOf("") }
    var editingId by remember(type) { mutableStateOf<Long?>(null) }
    var editedName by remember(type) { mutableStateOf("") }
    val locale = Locale.forLanguageTag("tr-TR")
    fun isValid(name: String, exceptId: Long? = null): Boolean {
        val cleaned = name.trim()
        return cleaned.isNotEmpty() &&
            cleaned.length <= 80 &&
            categories.none {
                it.id != exceptId && it.name.lowercase(locale) == cleaned.lowercase(locale)
            }
    }

    Column(
        modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Kategoriler tüm aylarda kullanılır. Ad değişikliği eski kayıtlarda da görünür.")
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it.take(80) },
                label = { Text("Yeni kategori") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = {
                    onAdd(newName.trim())
                    newName = ""
                },
                enabled = isValid(newName),
                modifier = Modifier.padding(top = 8.dp),
            ) { Text("Ekle") }
        }
        categories.forEach { category ->
            if (editingId == category.id) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it.take(80) },
                    label = { Text("Kategori adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row {
                    TextButton(
                        onClick = {
                            onRename(category.id, editedName.trim())
                            editingId = null
                        },
                        enabled = isValid(editedName, category.id),
                    ) { Text("Kaydet") }
                    TextButton(onClick = { editingId = null }) { Text("Vazgeç") }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(category.name, modifier = Modifier.weight(1f).padding(top = 12.dp))
                    TextButton(onClick = {
                        editingId = category.id
                        editedName = category.name
                    }) { Text("Düzenle") }
                }
            }
        }
        if (categories.isEmpty()) Text("Henüz kategori yok.")
    }
}
