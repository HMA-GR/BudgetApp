package com.hmagr.budgetapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.hmagr.budgetapp.ui.BudgetEntryUi
import com.hmagr.budgetapp.ui.EntryType
import java.time.YearMonth

/**
 * Kayıtlar yalnızca telefonun uygulamaya özel SQLite dosyasında tutulur.
 * Kişisel tutarlar hiçbir zaman örnek ekran görüntülerine veya Git deposuna yazılmaz.
 */
class BudgetDatabase(context: Context) : SQLiteOpenHelper(context, "budget.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                month TEXT NOT NULL,
                type TEXT NOT NULL CHECK(type IN ('INCOME', 'INVESTMENT', 'EXPENSE')),
                title TEXT NOT NULL,
                amount_cents INTEGER NOT NULL CHECK(amount_cents > 0)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX entries_month_idx ON entries(month, id)")
        db.execSQL(
            """
            CREATE TABLE investment_values (
                month TEXT PRIMARY KEY NOT NULL,
                value_cents INTEGER NOT NULL CHECK(value_cents >= 0)
            )
            """.trimIndent(),
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        error("Henüz şema geçişi yok: $oldVersion -> $newVersion")
    }

    fun addEntry(month: YearMonth, type: EntryType, title: String, amountCents: Long) {
        require(title.isNotBlank() && title.length <= 200 && amountCents > 0)
        val values = ContentValues().apply {
            put("month", month.toString())
            put("type", type.name)
            put("title", title.trim())
            put("amount_cents", amountCents)
        }
        check(writableDatabase.insertOrThrow("entries", null, values) != -1L)
    }

    fun entriesForMonth(month: YearMonth): List<BudgetEntryUi> {
        val entries = mutableListOf<BudgetEntryUi>()
        readableDatabase.query(
            "entries",
            arrayOf("title", "amount_cents", "type"),
            "month = ?",
            arrayOf(month.toString()),
            null,
            null,
            "id DESC",
        ).use { cursor ->
            while (cursor.moveToNext()) {
                entries += BudgetEntryUi(
                    title = cursor.getString(0),
                    amountCents = cursor.getLong(1),
                    type = EntryType.valueOf(cursor.getString(2)),
                )
            }
        }
        return entries
    }

    fun investmentValueForMonth(month: YearMonth): Long? =
        readableDatabase.query(
            "investment_values",
            arrayOf("value_cents"),
            "month = ?",
            arrayOf(month.toString()),
            null,
            null,
            null,
        ).use { cursor -> if (cursor.moveToFirst()) cursor.getLong(0) else null }

    fun setInvestmentValue(month: YearMonth, valueCents: Long) {
        require(valueCents >= 0)
        val values = ContentValues().apply {
            put("month", month.toString())
            put("value_cents", valueCents)
        }
        check(writableDatabase.insertWithOnConflict(
            "investment_values",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        ) != -1L)
    }

    fun snapshot(): BudgetSnapshot {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val entries = mutableListOf<SavedEntry>()
            db.query(
                "entries",
                arrayOf("month", "type", "title", "amount_cents"),
                null, null, null, null, "id ASC",
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    entries += SavedEntry(
                        YearMonth.parse(cursor.getString(0)),
                        EntryType.valueOf(cursor.getString(1)),
                        cursor.getString(2),
                        cursor.getLong(3),
                    )
                }
            }
            val values = mutableListOf<SavedInvestmentValue>()
            db.query(
                "investment_values",
                arrayOf("month", "value_cents"),
                null, null, null, null, "month ASC",
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    values += SavedInvestmentValue(
                        YearMonth.parse(cursor.getString(0)),
                        cursor.getLong(1),
                    )
                }
            }
            db.setTransactionSuccessful()
            return BudgetSnapshot(entries, values)
        } finally {
            db.endTransaction()
        }
    }

    fun replaceAll(snapshot: BudgetSnapshot) {
        BackupCodec.validate(snapshot)
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("entries", null, null)
            db.delete("investment_values", null, null)
            snapshot.entries.forEach { entry ->
                db.insertOrThrow("entries", null, ContentValues().apply {
                    put("month", entry.month.toString())
                    put("type", entry.type.name)
                    put("title", entry.title)
                    put("amount_cents", entry.amountCents)
                })
            }
            snapshot.investmentValues.forEach { value ->
                db.insertOrThrow("investment_values", null, ContentValues().apply {
                    put("month", value.month.toString())
                    put("value_cents", value.valueCents)
                })
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
