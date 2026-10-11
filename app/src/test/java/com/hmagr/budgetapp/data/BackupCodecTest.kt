package com.hmagr.budgetapp.data

import com.hmagr.budgetapp.ui.EntryType
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupCodecTest {
    private val month = YearMonth.of(2026, 10)
    private val snapshot = BudgetSnapshot(
        entries = listOf(
            SavedEntry(month, EntryType.INCOME, "Maaş", 250_000),
            SavedEntry(month, EntryType.INVESTMENT, "Birikim", 40_000),
        ),
        investmentValues = listOf(SavedInvestmentValue(month, 43_500)),
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
}
