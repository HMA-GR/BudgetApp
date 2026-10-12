package com.hmagr.budgetapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.hmagr.budgetapp.ui.BudgetEntryUi
import com.hmagr.budgetapp.ui.BudgetCategoryUi
import com.hmagr.budgetapp.ui.EntryType
import java.time.YearMonth
import java.util.Locale

/**
 * Kayıtlar yalnızca telefonun uygulamaya özel SQLite dosyasında tutulur.
 * Kişisel tutarlar hiçbir zaman örnek ekran görüntülerine veya Git deposuna yazılmaz.
 */
class BudgetDatabase(context: Context) : SQLiteOpenHelper(context, "budget.db", null, 2) {
    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

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
        addCategorySchema(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1 && newVersion == 2) {
            addCategorySchema(db)
        } else {
            error("Desteklenmeyen şema geçişi: $oldVersion -> $newVersion")
        }
    }

    private fun addCategorySchema(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE')),
                name TEXT NOT NULL,
                name_key TEXT NOT NULL,
                UNIQUE(type, name_key)
            )
            """.trimIndent(),
        )
        db.execSQL(
            "ALTER TABLE entries ADD COLUMN category_id INTEGER REFERENCES categories(id) ON DELETE SET NULL",
        )
    }

    fun addEntry(
        month: YearMonth,
        type: EntryType,
        title: String,
        amountCents: Long,
        categoryId: Long? = null,
    ) {
        require(title.isNotBlank() && title.length <= 200 && amountCents > 0)
        if (categoryId != null) require(categoryType(categoryId) == type)
        val values = ContentValues().apply {
            put("month", month.toString())
            put("type", type.name)
            put("title", title.trim())
            put("amount_cents", amountCents)
            if (categoryId != null) put("category_id", categoryId)
        }
        check(writableDatabase.insertOrThrow("entries", null, values) != -1L)
    }

    fun entriesForMonth(month: YearMonth): List<BudgetEntryUi> {
        val entries = mutableListOf<BudgetEntryUi>()
        readableDatabase.rawQuery(
            """
            SELECT e.id, e.title, e.amount_cents, e.type, c.name, e.category_id
            FROM entries e LEFT JOIN categories c ON c.id = e.category_id
            WHERE e.month = ? ORDER BY e.id DESC
            """.trimIndent(),
            arrayOf(month.toString()),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                entries += BudgetEntryUi(
                    title = cursor.getString(1),
                    amountCents = cursor.getLong(2),
                    type = EntryType.valueOf(cursor.getString(3)),
                    categoryName = if (cursor.isNull(4)) null else cursor.getString(4),
                    id = cursor.getLong(0),
                    categoryId = if (cursor.isNull(5)) null else cursor.getLong(5),
                )
            }
        }
        return entries
    }

    fun updateEntry(
        id: Long,
        month: YearMonth,
        type: EntryType,
        title: String,
        amountCents: Long,
        categoryId: Long?,
    ) {
        val cleaned = title.trim()
        require(id > 0 && cleaned.isNotEmpty() && cleaned.length <= 200 && amountCents > 0)
        if (categoryId != null) require(categoryType(categoryId) == type)
        val values = ContentValues().apply {
            put("title", cleaned)
            put("amount_cents", amountCents)
            if (categoryId == null) putNull("category_id") else put("category_id", categoryId)
        }
        check(writableDatabase.update(
            "entries", values, "id = ? AND month = ? AND type = ?",
            arrayOf(id.toString(), month.toString(), type.name),
        ) == 1)
    }

    fun deleteEntry(id: Long, month: YearMonth, type: EntryType) {
        require(id > 0)
        check(writableDatabase.delete(
            "entries", "id = ? AND month = ? AND type = ?",
            arrayOf(id.toString(), month.toString(), type.name),
        ) == 1)
    }

    fun categories(): List<BudgetCategoryUi> {
        val categories = mutableListOf<BudgetCategoryUi>()
        readableDatabase.query(
            "categories",
            arrayOf("id", "type", "name"),
            null, null, null, null, "type, name",
        ).use { cursor ->
            while (cursor.moveToNext()) {
                categories += BudgetCategoryUi(
                    id = cursor.getLong(0),
                    type = EntryType.valueOf(cursor.getString(1)),
                    name = cursor.getString(2),
                )
            }
        }
        return categories
    }

    fun addCategory(type: EntryType, name: String) {
        require(type != EntryType.INVESTMENT)
        val cleaned = name.trim()
        require(cleaned.isNotEmpty() && cleaned.length <= 80)
        writableDatabase.insertOrThrow("categories", null, ContentValues().apply {
            put("type", type.name)
            put("name", cleaned)
            put("name_key", categoryKey(cleaned))
        })
    }

    fun renameCategory(id: Long, name: String) {
        val cleaned = name.trim()
        require(cleaned.isNotEmpty() && cleaned.length <= 80)
        check(writableDatabase.update(
            "categories",
            ContentValues().apply {
                put("name", cleaned)
                put("name_key", categoryKey(cleaned))
            },
            "id = ?",
            arrayOf(id.toString()),
        ) == 1)
    }

    private fun categoryType(id: Long): EntryType? =
        readableDatabase.query(
            "categories", arrayOf("type"), "id = ?", arrayOf(id.toString()), null, null, null,
        ).use { cursor ->
            if (cursor.moveToFirst()) EntryType.valueOf(cursor.getString(0)) else null
        }

    private fun categoryKey(name: String): String =
        name.lowercase(Locale.forLanguageTag("tr-TR"))

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
            val categories = mutableListOf<SavedCategory>()
            db.query(
                "categories",
                arrayOf("id", "type", "name"),
                null, null, null, null, "id ASC",
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    categories += SavedCategory(
                        cursor.getLong(0),
                        EntryType.valueOf(cursor.getString(1)),
                        cursor.getString(2),
                    )
                }
            }
            val entries = mutableListOf<SavedEntry>()
            db.query(
                "entries",
                arrayOf("month", "type", "title", "amount_cents", "category_id"),
                null, null, null, null, "id ASC",
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    entries += SavedEntry(
                        YearMonth.parse(cursor.getString(0)),
                        EntryType.valueOf(cursor.getString(1)),
                        cursor.getString(2),
                        cursor.getLong(3),
                        if (cursor.isNull(4)) null else cursor.getLong(4),
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
            return BudgetSnapshot(entries, values, categories)
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
            db.delete("categories", null, null)
            snapshot.categories.forEach { category ->
                db.insertOrThrow("categories", null, ContentValues().apply {
                    put("id", category.id)
                    put("type", category.type.name)
                    put("name", category.name)
                    put("name_key", categoryKey(category.name))
                })
            }
            snapshot.entries.forEach { entry ->
                db.insertOrThrow("entries", null, ContentValues().apply {
                    put("month", entry.month.toString())
                    put("type", entry.type.name)
                    put("title", entry.title)
                    put("amount_cents", entry.amountCents)
                    if (entry.categoryId != null) put("category_id", entry.categoryId)
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
