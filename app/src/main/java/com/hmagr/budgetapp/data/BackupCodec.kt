package com.hmagr.budgetapp.data

import com.hmagr.budgetapp.ui.EntryType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.time.YearMonth
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.util.Locale

data class SavedEntry(
    val month: YearMonth,
    val type: EntryType,
    val title: String,
    val amountCents: Long,
    val categoryId: Long? = null,
)

data class SavedCategory(val id: Long, val type: EntryType, val name: String)

data class SavedInvestmentValue(val month: YearMonth, val valueCents: Long)

data class BudgetSnapshot(
    val entries: List<SavedEntry>,
    val investmentValues: List<SavedInvestmentValue>,
    val categories: List<SavedCategory> = emptyList(),
)

object BackupCodec {
    const val MAX_BACKUP_BYTES = 8 * 1024 * 1024
    private const val MAX_RECORDS = 100_000
    private const val MAX_CATEGORIES = 10_000
    private const val ITERATIONS = 210_000
    private val magic = byteArrayOf('B'.code.toByte(), 'U'.code.toByte(), 'D'.code.toByte(), '1'.code.toByte())

    fun readLimited(input: InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            require(output.size() + count <= MAX_BACKUP_BYTES) { "Yedek dosyası çok büyük." }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    fun encode(snapshot: BudgetSnapshot, password: CharArray): ByteArray {
        require(password.size >= 8) { "Parola en az 8 karakter olmalı." }
        validate(snapshot)
        val plaintext = ByteArrayOutputStream().also { output ->
            DataOutputStream(output).use { data ->
                data.writeInt(2)
                data.writeInt(snapshot.categories.size)
                snapshot.categories.forEach { category ->
                    data.writeLong(category.id)
                    data.writeUTF(category.type.name)
                    data.writeUTF(category.name)
                }
                data.writeInt(snapshot.entries.size)
                snapshot.entries.forEach { entry ->
                    data.writeUTF(entry.month.toString())
                    data.writeUTF(entry.type.name)
                    data.writeUTF(entry.title)
                    data.writeLong(entry.amountCents)
                    data.writeLong(entry.categoryId ?: 0L)
                }
                data.writeInt(snapshot.investmentValues.size)
                snapshot.investmentValues.forEach { value ->
                    data.writeUTF(value.month.toString())
                    data.writeLong(value.valueCents)
                }
            }
        }.toByteArray()

        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        val nonce = ByteArray(12).also(SecureRandom()::nextBytes)
        val header = magic + salt + nonce
        val cipher = cipher(Cipher.ENCRYPT_MODE, password, salt, nonce, header)
        val result = header + cipher.doFinal(plaintext)
        require(result.size <= MAX_BACKUP_BYTES) { "Yedek dosyası boyut sınırını aşıyor." }
        return result
    }

    fun decode(file: ByteArray, password: CharArray): BudgetSnapshot {
        require(file.size in 49..MAX_BACKUP_BYTES) { "Yedek dosyası geçersiz veya çok büyük." }
        val header = file.copyOfRange(0, 32)
        require(header.copyOfRange(0, 4).contentEquals(magic)) { "Bu dosya BudgetApp yedeği değil." }
        val salt = header.copyOfRange(4, 20)
        val nonce = header.copyOfRange(20, 32)
        val plaintext = cipher(Cipher.DECRYPT_MODE, password, salt, nonce, header)
            .doFinal(file, 32, file.size - 32)
        val snapshot = DataInputStream(ByteArrayInputStream(plaintext)).use { data ->
            val version = data.readInt()
            require(version == 1 || version == 2) { "Desteklenmeyen yedek sürümü." }
            val categories = if (version == 2) {
                val categoryCount = data.readInt()
                require(categoryCount in 0..MAX_CATEGORIES) { "Geçersiz kategori sayısı." }
                List(categoryCount) {
                    SavedCategory(
                        id = data.readLong(),
                        type = EntryType.valueOf(data.readUTF()),
                        name = data.readUTF(),
                    )
                }
            } else {
                emptyList()
            }
            val entryCount = data.readInt()
            require(entryCount in 0..MAX_RECORDS) { "Geçersiz kayıt sayısı." }
            val entries = List(entryCount) {
                SavedEntry(
                    month = YearMonth.parse(data.readUTF()),
                    type = EntryType.valueOf(data.readUTF()),
                    title = data.readUTF(),
                    amountCents = data.readLong(),
                    categoryId = if (version == 2) data.readLong().takeUnless { it == 0L } else null,
                )
            }
            val valueCount = data.readInt()
            require(valueCount in 0..MAX_RECORDS) { "Geçersiz değer sayısı." }
            val values = List(valueCount) {
                SavedInvestmentValue(YearMonth.parse(data.readUTF()), data.readLong())
            }
            require(data.available() == 0) { "Yedek dosyasında fazladan veri var." }
            BudgetSnapshot(entries, values, categories)
        }
        validate(snapshot)
        return snapshot
    }

    fun validate(snapshot: BudgetSnapshot) {
        require(
            snapshot.entries.size <= MAX_RECORDS &&
                snapshot.investmentValues.size <= MAX_RECORDS &&
                snapshot.categories.size <= MAX_CATEGORIES,
        )
        val categoryById = snapshot.categories.associateBy { it.id }
        require(categoryById.size == snapshot.categories.size) { "Tekrarlanan kategori kimliği." }
        val categoryKeys = snapshot.categories.map {
            require(it.id > 0 && it.type != EntryType.INVESTMENT)
            require(it.name.isNotBlank() && it.name.length <= 80 && it.name == it.name.trim())
            it.type to it.name.lowercase(Locale.forLanguageTag("tr-TR"))
        }
        require(categoryKeys.toSet().size == categoryKeys.size) { "Tekrarlanan kategori adı." }
        snapshot.entries.forEach {
            require(it.title.isNotBlank() && it.title.length <= 200 && it.amountCents > 0)
            require(YearMonth.parse(it.month.toString()) == it.month)
            if (it.categoryId != null) {
                require(categoryById[it.categoryId]?.type == it.type) {
                    "Kayıt kategorisi eksik veya farklı türde."
                }
            }
        }
        snapshot.investmentValues.forEach { require(it.valueCents >= 0) }
        require(snapshot.investmentValues.map { it.month }.toSet().size == snapshot.investmentValues.size) {
            "Aynı ay için birden çok yatırım değeri var."
        }
    }

    private fun cipher(
        mode: Int,
        password: CharArray,
        salt: ByteArray,
        nonce: ByteArray,
        header: ByteArray,
    ): Cipher {
        val spec = PBEKeySpec(password, salt, ITERATIONS, 256)
        val key = try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
        return try {
            Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(mode, SecretKeySpec(key, "AES"), GCMParameterSpec(128, nonce))
                updateAAD(header)
            }
        } finally {
            key.fill(0)
        }
    }
}
