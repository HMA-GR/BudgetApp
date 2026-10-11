package com.hmagr.budgetapp.data

import com.hmagr.budgetapp.ui.EntryType
import java.time.YearMonth
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupCodecTest {
    private val month = YearMonth.of(2026, 10)
    private val snapshot = BudgetSnapshot(
        entries = listOf(
            SavedEntry(month, EntryType.INCOME, "Maaş", 250_000, 1),
            SavedEntry(month, EntryType.INVESTMENT, "Birikim", 40_000),
        ),
        investmentValues = listOf(SavedInvestmentValue(month, 43_500)),
        categories = listOf(SavedCategory(1, EntryType.INCOME, "İş")),
    )

    @Test
    fun encryptedFileRestoresEntriesAndInvestmentValue() {
        val bytes = BackupCodec.encode(snapshot, "güçlüParola123".toCharArray())

        assertFalse(bytes.toString(Charsets.UTF_8).contains("Maaş"))
        assertEquals(snapshot, BackupCodec.decode(bytes, "güçlüParola123".toCharArray()))
    }

    @Test
    fun wrongPasswordAndChangedCiphertextAreRejected() {
        val bytes = BackupCodec.encode(snapshot, "güçlüParola123".toCharArray())
        assertThrows(Exception::class.java) {
            BackupCodec.decode(bytes, "yanlışParola".toCharArray())
        }

        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
        assertThrows(Exception::class.java) {
            BackupCodec.decode(bytes, "güçlüParola123".toCharArray())
        }
    }

    @Test
    fun categoryMustExistAndMatchTheEntryType() {
        val mismatched = snapshot.copy(
            entries = listOf(SavedEntry(month, EntryType.EXPENSE, "Market", 25_000, 1)),
        )
        assertThrows(IllegalArgumentException::class.java) {
            BackupCodec.validate(mismatched)
        }
    }

    @Test
    fun oldBackupWithoutCategoriesStillImports() {
        // Önceki sürümün sabit örnek dosyası; test verisi gerçek kişisel bilgi içermez.
        val oldFile = Base64.getDecoder().decode(
            "QlVEMQECAwQFBgcICQoLDA0ODxAVFhcYGRobHB0eHyBqyUoywA+1V69vS2HVGfbpW0QlEX2f3UnbxPF8bDfMNkUjn3Vd9tkMHykURSejDQHfYSuiaFDdZOsZgXbMJ38SxpA6gUn9JVt0DSOcWi49",
        )
        assertEquals(
            BudgetSnapshot(
                entries = listOf(SavedEntry(month, EntryType.EXPENSE, "Market", 420_000)),
                investmentValues = listOf(SavedInvestmentValue(month, 530_000)),
            ),
            BackupCodec.decode(oldFile, "fixture-pass123".toCharArray()),
        )
    }
}
