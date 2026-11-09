package br.com.ourtalk.app

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Small, encrypted local store. No fallback to plaintext or silent reset on authentication failure. */
class SecureStore(context: Context, private val namespace: String) {
    private val disk = context.applicationContext.getSharedPreferences("ourtalk-encrypted-v1", Context.MODE_PRIVATE)
    private val alias = "ourtalk.local.aes.v1"
    private fun key(): SecretKey = synchronized(SecureStore::class.java) {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey) ?: KeyGenerator.getInstance("AES", "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256).setRandomizedEncryptionRequired(true).build())
        }.generateKey()
    }
    private fun read(): JSONObject {
        val encoded = disk.getString(namespace, null) ?: return JSONObject()
        val bytes = Base64.decode(encoded, Base64.NO_WRAP)
        check(bytes.size >= 29 && bytes[0] == 1.toByte()) { "Invalid encrypted store" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(1, 13)))
        cipher.updateAAD(namespace.toByteArray(Charsets.UTF_8))
        return JSONObject(String(cipher.doFinal(bytes.copyOfRange(13, bytes.size)), Charsets.UTF_8))
    }
    private fun write(data: JSONObject) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        cipher.updateAAD(namespace.toByteArray(Charsets.UTF_8))
        check(cipher.iv.size == 12)
        val encrypted = byteArrayOf(1) + cipher.iv + cipher.doFinal(data.toString().toByteArray(Charsets.UTF_8))
        check(disk.edit().putString(namespace, Base64.encodeToString(encrypted, Base64.NO_WRAP)).commit()) { "Local storage unavailable" }
    }
    init {
        require(namespace in setOf("ourtalk-local-v1", "ourtalk-position-v1"))
        synchronized(SecureStore::class.java) {
            val legacy = context.applicationContext.getSharedPreferences(namespace, Context.MODE_PRIVATE)
            if (!disk.contains(namespace) && legacy.all.isNotEmpty()) {
                val migrated = JSONObject()
                legacy.all.forEach { (name, value) -> migrated.put(name, if (value is Set<*>) JSONArray(value.toList()) else value) }
                write(migrated)
                read() // Verify authenticated read before removing the plaintext source.
                check(legacy.edit().clear().commit()) { "Migration incomplete" }
            } else if (disk.contains(namespace) && legacy.all.isNotEmpty()) {
                read()
                check(legacy.edit().clear().commit())
            }
            read()
        }
    }
    fun contains(name: String) = synchronized(SecureStore::class.java) { read().has(name) }
    fun getString(name: String, fallback: String?): String? = synchronized(SecureStore::class.java) { val data=read(); if(data.has(name))data.getString(name) else fallback }
    fun getFloat(name: String, fallback: Float): Float = synchronized(SecureStore::class.java) { val data=read();if(data.has(name))data.getDouble(name).toFloat() else fallback }
    fun getLong(name: String, fallback: Long): Long = synchronized(SecureStore::class.java) { val data=read();if(data.has(name))data.getLong(name) else fallback }
    fun getBoolean(name: String, fallback: Boolean): Boolean = synchronized(SecureStore::class.java) { val data=read();if(data.has(name))data.getBoolean(name) else fallback }
    fun getStringSet(name: String, fallback: Set<String>): Set<String> = synchronized(SecureStore::class.java) { val array=read().optJSONArray(name);if(array==null)fallback else (0 until array.length()).map{array.getString(it)}.toSet() }
    fun edit() = Editor()
    inner class Editor {
        private val changes = mutableMapOf<String, Any?>()
        private var clear = false
        fun putString(name: String, value: String?) = apply { changes[name]=value }
        fun putFloat(name: String, value: Float) = apply { changes[name]=value }
        fun putLong(name: String, value: Long) = apply { changes[name]=value }
        fun putBoolean(name: String, value: Boolean) = apply { changes[name]=value }
        fun putStringSet(name: String, value: Set<String>) = apply { changes[name]=JSONArray(value.toList()) }
        fun clear() = apply { clear=true }
        fun apply() = synchronized(SecureStore::class.java) {
            val data=if(clear)JSONObject() else read()
            changes.forEach{(name,value)->if(value==null)data.remove(name) else data.put(name,value)}
            write(data)
        }
    }
}
