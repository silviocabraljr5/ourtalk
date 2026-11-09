package br.com.ourtalk.app

import android.content.Context
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SecureStoreTest {
    private val context=ApplicationProvider.getApplicationContext<Context>()
    private val disk get()=context.getSharedPreferences("ourtalk-encrypted-v1",Context.MODE_PRIVATE)
    @Before fun clearOwnedTestData(){
        disk.edit().clear().commit()
        listOf("ourtalk-local-v1","ourtalk-position-v1").forEach{context.getSharedPreferences(it,Context.MODE_PRIVATE).edit().clear().commit()}
    }
    @Test fun migratesPlaintextOnlyAfterAuthenticatedWrite(){
        val legacy=context.getSharedPreferences("ourtalk-local-v1",Context.MODE_PRIVATE)
        legacy.edit().putString("draft-test","Mensagem particular 🌱").putStringSet("favorites",setOf("gps","battery")).commit()
        val secure=SecureStore(context,"ourtalk-local-v1")
        assertEquals("Mensagem particular 🌱",secure.getString("draft-test",null))
        assertEquals(setOf("gps","battery"),secure.getStringSet("favorites",emptySet()))
        assertTrue(legacy.all.isEmpty())
        assertFalse(disk.getString("ourtalk-local-v1","")!!.contains("Mensagem particular"))
        assertEquals("Mensagem particular 🌱",SecureStore(context,"ourtalk-local-v1").getString("draft-test",null))
    }
    @Test fun modifiedCiphertextFailsClosedAndRemainsAvailableForRecovery(){
        SecureStore(context,"ourtalk-local-v1").edit().putString("draft-test","Privado").apply()
        val bytes=Base64.decode(disk.getString("ourtalk-local-v1",null),Base64.NO_WRAP)
        bytes[bytes.lastIndex]=(bytes.last().toInt() xor 1).toByte()
        val corrupted=Base64.encodeToString(bytes,Base64.NO_WRAP)
        disk.edit().putString("ourtalk-local-v1",corrupted).commit()
        assertTrue(runCatching{SecureStore(context,"ourtalk-local-v1")}.isFailure)
        assertEquals(corrupted,disk.getString("ourtalk-local-v1",null))
    }
    @Test fun ciphertextCannotBeMovedToAnotherNamespace(){
        SecureStore(context,"ourtalk-local-v1").edit().putString("private","Value").apply()
        disk.edit().putString("ourtalk-position-v1",disk.getString("ourtalk-local-v1",null)).commit()
        assertTrue(runCatching{SecureStore(context,"ourtalk-position-v1")}.isFailure)
    }
    @Test fun repeatedWritesUseDifferentNoncesAndPreserveTypedData(){
        val secure=SecureStore(context,"ourtalk-position-v1")
        secure.edit().putString("latitude","-21.17").putFloat("accuracy",5f).putLong("time",12345L).putBoolean("mock",true).apply()
        val first=disk.getString("ourtalk-position-v1",null)
        secure.edit().putString("latitude","-21.17").apply()
        assertNotEquals(first,disk.getString("ourtalk-position-v1",null))
        val reopened=SecureStore(context,"ourtalk-position-v1")
        assertEquals(5f,reopened.getFloat("accuracy",0f),0f)
        assertEquals(12345L,reopened.getLong("time",0L))
        assertTrue(reopened.getBoolean("mock",false))
    }
}
