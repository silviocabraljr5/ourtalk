package br.com.ourtalk.app

import android.content.Context
import android.net.Uri
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.security.SecureRandom
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.resume

internal class ContactInvites(context: Context) {
    private val app = context.applicationContext
    private val store = SecureStore(app, "ourtalk-invites-v1")
    val pending get() = store.getString("pending", "").orEmpty()
    val name get() = store.getString("name", "").orEmpty()
    private val identity get() = store.getString("device", "").orEmpty()
    val hasIdentity get() = identity.isNotEmpty()

    suspend fun readInstallInvite(): String {
        if (pending.isNotEmpty() || store.getString("referrer-read", "") == "yes") return pending
        val referrer = withTimeoutOrNull(5_000) {
            suspendCancellableCoroutine<String?> { continuation ->
                val client = InstallReferrerClient.newBuilder(app).build()
                fun finish(value: String?) {
                    if (continuation.isActive) continuation.resume(value)
                }
                continuation.invokeOnCancellation { client.endConnection() }
                try {
                    client.startConnection(object : InstallReferrerStateListener {
                        override fun onInstallReferrerSetupFinished(code: Int) {
                            val result = when (code) {
                                InstallReferrerClient.InstallReferrerResponse.OK ->
                                    runCatching { client.installReferrer.installReferrer }.getOrNull()
                                InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> ""
                                else -> null
                            }
                            client.endConnection()
                            finish(result)
                        }
                        override fun onInstallReferrerServiceDisconnected() {
                            client.endConnection()
                            finish(null)
                        }
                    })
                } catch (_: RuntimeException) {
                    client.endConnection()
                    finish(null)
                }
            }
        } ?: return ""
        val token = Uri.parse("https://ourtalk.com.br/?$referrer")
            .getQueryParameter("ourtalk_invite").orEmpty().takeIf(TOKEN::matches).orEmpty()
        store.edit().putString("pending", token).putString("referrer-read", "yes").apply()
        return token
    }

    fun discard() {
        store.edit().putString("pending", "").apply()
    }

    suspend fun create(displayName: String): String {
        val result = request("create", displayName)
        val token = result.getString("token")
        check(TOKEN.matches(token))
        return Uri.parse("https://play.google.com/store/apps/details").buildUpon()
            .appendQueryParameter("id", "br.com.ourtalk.app")
            .appendQueryParameter("referrer", "ourtalk_invite=$token")
            .build().toString()
    }

    suspend fun redeem(displayName: String, token: String) {
        require(TOKEN.matches(token))
        request("redeem", displayName, token)
        discard()
    }

    suspend fun contacts(): List<SavedContact> {
        if (!hasIdentity) return emptyList()
        val rows = request("list").getJSONArray("contacts")
        return List(rows.length()) { i ->
            val row = rows.getJSONObject(i)
            SavedContact("invite-" + row.getString("id"), row.getString("name"))
        }
    }

    suspend fun deleteData(clearLocalContacts: () -> Unit) {
        if (hasIdentity) request("delete")
        clearLocalContacts()
        store.edit().clear().putString("referrer-read", "yes").apply()
    }

    private suspend fun request(action: String, displayName: String = "", token: String = ""): JSONObject {
        val device = store.getString("device", "").orEmpty().ifEmpty {
            val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
            bytes.joinToString("") { "%02x".format(it) }.also {
                store.edit().putString("device", it).apply()
            }
        }
        if (displayName.isNotBlank()) store.edit().putString("name", displayName).apply()
        val body = JSONObject().put("action", action).put("device", device)
            .put("name", displayName).put("token", token).toString()
        return withContext(Dispatchers.IO) {
            val connection = URL("https://ourtalk.com.br/contact-invites.php")
                .openConnection() as HttpsURLConnection
            try {
                connection.requestMethod = "POST"
                connection.instanceFollowRedirects = false
                connection.connectTimeout = 6_000
                connection.readTimeout = 6_000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                if (connection.responseCode == 409) throw UnavailableInvite()
                if (connection.responseCode != 200) throw IOException("Invite service unavailable")
                JSONObject(connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() })
            } finally {
                connection.disconnect()
            }
        }
    }

    class UnavailableInvite : IOException()
    companion object {
        private val TOKEN = Regex("[0-9a-f]{48}")
    }
}
