package br.com.ourtalk.app

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NearbyIdentityTest {
    @Test fun identityIsStableButProofCannotBeReplayed() {
        val identity = NearbyIdentity(ApplicationProvider.getApplicationContext())
        val firstToken = ByteArray(32) { 1 }
        val nextToken = ByteArray(32) { 2 }
        val proof = identity.proof(firstToken, incoming = false)
        val peer = identity.verify(proof, firstToken, incoming = true)
        val nextProof = identity.proof(nextToken, incoming = false)
        assertEquals(peer, identity.verify(nextProof, nextToken, incoming = true))
        assertTrue(runCatching { identity.verify(proof, nextToken, incoming = true) }.isFailure)
        assertTrue(runCatching { identity.verify(proof, firstToken, incoming = false) }.isFailure)
    }
}
