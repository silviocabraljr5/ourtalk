package br.com.ourtalk.app

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal data class VaultDocument(val id: UUID, val title: String, val mime: String?, val size: Int)

internal class DocumentVault(context: Context) {
    private val app = context.applicationContext
    private val directory = File(app.noBackupFilesDir, "document-vault")
    private val resolver = app.contentResolver

    fun documents(): List<VaultDocument> = synchronized(lock) {
        documentIds().map { id ->
            runCatching {
                val (document, data) = read(id)
                data.fill(0)
                document
            }.getOrElse { VaultDocument(id, "Documento indisponível", null, 0) }
        }.sortedBy { it.title.lowercase() }
    }

    fun importDocument(uri: Uri) = synchronized(lock) {
        require(uri.scheme == "content")
        require(documentIds().size < MAX_DOCUMENTS) { "O cofre comporta até 10 documentos." }
        val mime = resolver.getType(uri)
        require(mime in formats) { "Selecione um PDF, JPEG ou PNG." }
        val displayName = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { if (it.moveToFirst()) it.getString(0) else null }
        val extension = requireNotNull(formats[mime])
        val name = displayName.orEmpty().filter { !it.isISOControl() && it !in "/\\:" }.trim().take(100)
            .ifBlank { "Documento" }
        val title = if (name.endsWith(".$extension", ignoreCase = true)) name else "$name.$extension"
        val body = requireNotNull(resolver.openInputStream(uri)).use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                require(output.size() + count <= MAX_BYTES) { "O documento deve ter até 5 MB." }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
        require(body.isNotEmpty()) { "O documento está vazio." }
        val id = UUID.randomUUID()
        val titleBytes = title.toByteArray(Charsets.UTF_8)
        val mimeBytes = requireNotNull(mime).toByteArray(Charsets.UTF_8)
        val plain = ByteBuffer.allocate(8 + titleBytes.size + mimeBytes.size + body.size)
            .putInt(titleBytes.size).putInt(mimeBytes.size).put(titleBytes).put(mimeBytes).put(body).array()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        check(cipher.iv.size == 12)
        cipher.updateAAD(id.toString().toByteArray())
        val encrypted = cipher.doFinal(plain)
        check(directory.isDirectory || directory.mkdirs())
        val file = file(id)
        val stream = file.startWrite()
        try {
            stream.write(1)
            stream.write(cipher.iv)
            stream.write(encrypted)
            file.finishWrite(stream)
        } catch (failure: Exception) {
            file.failWrite(stream)
            throw failure
        } finally {
            plain.fill(0)
            body.fill(0)
        }
    }

    fun export(id: UUID, target: Uri) = synchronized(lock) {
        require(target.scheme == "content")
        val (_, data) = read(id)
        try {
            requireNotNull(resolver.openOutputStream(target, "wt")).use { it.write(data) }
        } finally {
            data.fill(0)
        }
    }

    fun delete(id: UUID) = synchronized(lock) {
        val file = file(id)
        file.delete()
        check(!file.baseFile.exists())
    }

    private fun read(id: UUID): Pair<VaultDocument, ByteArray> {
        val file = file(id)
        val encrypted = file.openRead().use { input ->
            val limit = MAX_BYTES + 1024
            val buffer = ByteArrayOutputStream()
            val chunk = ByteArray(8192)
            while (true) {
                val size = input.read(chunk)
                if (size < 0) break
                require(buffer.size() + size <= limit)
                buffer.write(chunk, 0, size)
            }
            buffer.toByteArray()
        }
        require(encrypted.size >= 37 && encrypted[0] == 1.toByte())
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, encrypted, 1, 12))
        cipher.updateAAD(id.toString().toByteArray())
        val plain = cipher.doFinal(encrypted, 13, encrypted.size - 13)
        try {
            val buffer = ByteBuffer.wrap(plain)
            val titleLength = buffer.int
            val mimeLength = buffer.int
            require(titleLength in 1..450 && mimeLength in 1..40 && buffer.remaining() > titleLength + mimeLength)
            val title = ByteArray(titleLength).also(buffer::get).toString(Charsets.UTF_8)
            val mime = ByteArray(mimeLength).also(buffer::get).toString(Charsets.UTF_8)
            require(mime in formats && buffer.remaining() in 1..MAX_BYTES)
            val size = buffer.remaining()
            return VaultDocument(id, title, mime, size) to ByteArray(size).also(buffer::get)
        } finally {
            plain.fill(0)
        }
    }

    private fun documentIds(): List<UUID> = directory.listFiles().orEmpty()
        .filter { it.name.endsWith(".vault") || it.name.endsWith(".vault.bak") }
        .mapNotNull { runCatching { UUID.fromString(it.name.substringBefore('.')) }.getOrNull() }.distinct()

    private fun file(id: UUID) = AtomicFile(File(directory, "$id.vault"))

    private fun key(): SecretKey {
        val keys = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val alias = "ourtalk.documents.aes.v1"
        (keys.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256).build())
        }.generateKey()
    }

    companion object {
        private val lock = Any()
        private val formats = mapOf("application/pdf" to "pdf", "image/jpeg" to "jpg", "image/png" to "png")
        private const val MAX_DOCUMENTS = 10
        private const val MAX_BYTES = 5_000_000
    }
}
