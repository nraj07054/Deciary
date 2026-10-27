package app.deciary.data

import kotlinx.serialization.Serializable
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class WrongPasswordException : Exception("That password doesn't open this backup.")
class NotABackupException : Exception("This file isn't a Deciary backup.")

/**
 * A backup file: the letters, encrypted with a password the person chooses.
 * Version 1 files have no [iterations] field and used 120,000 rounds; they still open.
 */
@Serializable
private data class BackupFile(
    val format: String = FORMAT,
    val version: Int = 1,
    val iterations: Int = V1_ITERATIONS,
    val salt: String,
    val iv: String,
    val data: String,
)

private const val FORMAT = "deciary-backup"
private const val V1_ITERATIONS = 120_000

/** PBKDF2-SHA256 rounds for new backups, as OWASP recommends. */
internal const val ITERATIONS = 600_000

/** Version 2 binds the header to the ciphertext, so its rounds or version can't be swapped. */
private fun header(version: Int, iterations: Int) = "$FORMAT:v$version:$iterations".toByteArray()

object Backup {
    private val b64e = Base64.getEncoder()
    private val b64d = Base64.getDecoder()

    fun encrypt(letters: List<Letter>, password: String, iterations: Int = ITERATIONS): String {
        val random = SecureRandom()
        val salt = ByteArray(16).also(random::nextBytes)
        val iv = ByteArray(12).also(random::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key(password, salt, iterations), GCMParameterSpec(128, iv))
        cipher.updateAAD(header(2, iterations))
        val plain = LetterStore.json.encodeToString(LetterStore.serializer, letters).toByteArray()
        val file = BackupFile(
            version = 2, iterations = iterations,
            salt = b64e.encodeToString(salt), iv = b64e.encodeToString(iv), data = b64e.encodeToString(cipher.doFinal(plain)),
        )
        return LetterStore.json.encodeToString(BackupFile.serializer(), file)
    }

    fun decrypt(text: String, password: String): List<Letter> {
        val file = try {
            LetterStore.json.decodeFromString(BackupFile.serializer(), text).also { require(it.format == FORMAT) }
        } catch (e: Exception) {
            throw NotABackupException()
        }
        // Refuse silly round counts, so a tampered file can't stall the phone or weaken the key.
        if (file.version !in 1..2 || file.iterations !in V1_ITERATIONS..5_000_000) throw NotABackupException()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(password, b64d.decode(file.salt), file.iterations), GCMParameterSpec(128, b64d.decode(file.iv)))
        if (file.version >= 2) cipher.updateAAD(header(file.version, file.iterations))
        val plain = try {
            cipher.doFinal(b64d.decode(file.data))
        } catch (e: AEADBadTagException) {
            throw WrongPasswordException()
        }
        return LetterStore.json.decodeFromString(LetterStore.serializer, String(plain))
    }

    private fun key(password: String, salt: ByteArray, iterations: Int): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return SecretKeySpec(bytes, "AES")
    }
}
