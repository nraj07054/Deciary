package app.deciary.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class SecurityTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun letter(id: String = "a", why: String = "Better team") = Letter(
        id = id, question = "Accept the Pune job offer?", choice = "Yes", prediction = "I'll be settled by April",
        why = why, sealedAt = 0L, opensOn = 20_000L,
    )

    /** The same AES-GCM as the Keystore vault, with an in-memory key, since unit tests have no Keystore. */
    private class SoftVault : Vault {
        private val key = SecretKeySpec(ByteArray(32).also { SecureRandom().nextBytes(it) }, "AES")
        override fun seal(plain: ByteArray): ByteArray {
            val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
            val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv)) }
            return iv + c.doFinal(plain)
        }
        override fun open(sealed: ByteArray): ByteArray {
            val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, sealed.copyOfRange(0, 12))) }
            return c.doFinal(sealed, 12, sealed.size - 12)
        }
    }

    @Test fun lettersAreEncryptedOnDisk() = runBlocking {
        val vault = SoftVault()
        val store = LetterStore(tmp.root, vault)
        store.upsert(letter())
        val raw = tmp.root.resolve("letters.enc").readBytes().decodeToString()
        assertFalse(raw.contains("Pune"))
        assertFalse(tmp.root.resolve("letters.json").exists())
        // A fresh store with the same key reads them back.
        assertEquals(listOf(letter()), LetterStore(tmp.root, vault).letters.value)
    }

    @Test fun plainLettersFromOlderVersionsAreEncryptedAndRemoved() {
        val legacy = tmp.root.resolve("letters.json")
        legacy.writeText(LetterStore.json.encodeToString(LetterStore.serializer, listOf(letter())))
        val store = LetterStore(tmp.root, SoftVault())
        assertEquals(listOf(letter()), store.letters.value)
        assertFalse(legacy.exists())
        assertFalse(tmp.root.resolve("letters.enc").readBytes().decodeToString().contains("Pune"))
    }

    @Test fun unreadableFileIsSetAsideNotOverwritten() = runBlocking {
        LetterStore(tmp.root, SoftVault()).upsert(letter())
        // A different key can't open it: the file is kept aside rather than replaced by an empty list.
        val other = LetterStore(tmp.root, SoftVault())
        assertTrue(other.letters.value.isEmpty())
        assertTrue(tmp.root.listFiles()!!.any { it.name.startsWith("letters.enc.unreadable-") })
    }

    @Test fun pinLockoutSchedule() {
        assertEquals(0L, PinLockout.delayAfter(4))
        assertEquals(30_000L, PinLockout.delayAfter(5))
        assertEquals(60_000L, PinLockout.delayAfter(6))
        assertEquals(120_000L, PinLockout.delayAfter(7))
        assertEquals(15 * 60_000L, PinLockout.delayAfter(12))
        assertEquals(15 * 60_000L, PinLockout.delayAfter(1_000))
    }

    @Test fun newBackupsUseStrongerKeys() {
        val file = Backup.encrypt(listOf(letter()), "correct horse")
        assertTrue(file.contains("\"version\":2"))
        assertTrue(file.contains("\"iterations\":$ITERATIONS"))
        assertEquals(listOf(letter()), Backup.decrypt(file, "correct horse"))
    }

    @Test(expected = WrongPasswordException::class)
    fun tamperedBackupHeaderIsRefused() {
        val file = Backup.encrypt(listOf(letter()), "correct horse", iterations = 200_000)
        Backup.decrypt(file.replace("\"iterations\":200000", "\"iterations\":150000"), "correct horse")
    }

    @Test fun olderBackupsStillOpen() {
        // A version 1 file as earlier releases wrote it: 120,000 rounds, no iterations field, no header binding.
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val key = SecretKeySpec(
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(PBEKeySpec("old pass".toCharArray(), salt, 120_000, 256)).encoded, "AES",
        )
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv)) }
        val data = c.doFinal(LetterStore.json.encodeToString(LetterStore.serializer, listOf(letter())).toByteArray())
        val b = Base64.getEncoder()
        val v1 = """{"format":"deciary-backup","version":1,"salt":"${b.encodeToString(salt)}","iv":"${b.encodeToString(iv)}","data":"${b.encodeToString(data)}"}"""
        assertEquals(listOf(letter()), Backup.decrypt(v1, "old pass"))
    }

    @Test fun csvCellsCantRunAsFormulas() {
        val csv = Backup.exportCsv(listOf(letter(why = "=HYPERLINK(\"http://x\")"), letter("b", why = "+1 more"), letter("c", why = "-ish")))
        assertTrue(csv.contains("\"'=HYPERLINK(\"\"http://x\"\")\""))
        assertTrue(csv.contains(",'+1 more,"))
        assertTrue(csv.contains(",'-ish,"))
    }
}
