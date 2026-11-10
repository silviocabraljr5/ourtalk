package br.com.ourtalk.app

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec

/** Installation identity disclosed only inside a mutually confirmed session. */
internal class NearbyIdentity(context: Context) {
    private val store = SecureStore(context, "ourtalk-nearby-safety-v1")
    private val alias = "ourtalk.nearby.identity.v1"
    private val signingContext = "OurTalk nearby identity v2\u0000".toByteArray()
    val acceptedTerms get() = store.getBoolean("terms-v1", false)
    fun acceptTerms() = store.edit().putBoolean("terms-v1", true).apply()
    fun isBlocked(peer: String) = peer in store.getStringSet("blocked", emptySet())
    fun block(peer: String) = store.edit()
        .putStringSet("blocked", store.getStringSet("blocked", emptySet()) + peer).apply()
    fun unblockAll() = store.edit().putStringSet("blocked", emptySet()).apply()
    fun blockedCount() = store.getStringSet("blocked", emptySet()).size

    fun proof(token: ByteArray, incoming: Boolean): ByteArray {
        val keys = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (!keys.containsAlias(alias)) {
            KeyPairGenerator.getInstance("EC", "AndroidKeyStore").apply {
                initialize(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
                    .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                    .setDigests(KeyProperties.DIGEST_SHA256).build())
            }.generateKeyPair()
        }
        val signature = Signature.getInstance("SHA256withECDSA").apply {
            initSign(keys.getKey(alias, null) as java.security.PrivateKey)
            update(signingContext)
            update(if (incoming) 1.toByte() else 0.toByte())
            update(token)
        }.sign()
        return JSONObject()
            .put("key", Base64.encodeToString(keys.getCertificate(alias).publicKey.encoded, Base64.NO_WRAP))
            .put("signature", Base64.encodeToString(signature, Base64.NO_WRAP))
            .toString().toByteArray()
    }

    fun verify(bytes: ByteArray, token: ByteArray, incoming: Boolean): String {
        require(bytes.size in 1..512)
        val proof = JSONObject(bytes.toString(Charsets.UTF_8))
        val key = KeyFactory.getInstance("EC").generatePublic(
            X509EncodedKeySpec(Base64.decode(proof.getString("key"), Base64.NO_WRAP))
        ) as java.security.interfaces.ECPublicKey
        require(key.params.curve.field.fieldSize == 256)
        val verifier = Signature.getInstance("SHA256withECDSA").apply {
            initVerify(key)
            update(signingContext)
            update(if (incoming) 0.toByte() else 1.toByte())
            update(token)
        }
        require(verifier.verify(Base64.decode(proof.getString("signature"), Base64.NO_WRAP)))
        return MessageDigest.getInstance("SHA-256").digest(key.encoded).joinToString("") { "%02x".format(it) }
    }
}
