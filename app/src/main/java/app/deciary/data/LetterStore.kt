package app.deciary.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Encrypts and decrypts the letters file. */
interface Vault {
    fun seal(plain: ByteArray): ByteArray
    fun open(sealed: ByteArray): ByteArray
}

/**
 * AES-256-GCM with a key that lives in the Android Keystore: it never leaves the phone's secure
 * hardware, so a copy of the file is unreadable anywhere else. The key doesn't need the screen
 * unlocked, because the daily reminder check reads letters in the background; Android's own
 * storage encryption still keeps the file locked until the phone is first unlocked after a restart.
 */
class KeystoreVault(private val alias: String = "deciary-letters") : Vault {
    private fun key(): SecretKey {
        val store = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        val spec = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE).apply { init(spec) }.generateKey()
    }

    override fun seal(plain: ByteArray): ByteArray {
        // The Keystore picks a fresh random IV for every encryption.
        val cipher = Cipher.getInstance(TRANSFORM).apply { init(Cipher.ENCRYPT_MODE, key()) }
        return MAGIC + cipher.iv + cipher.doFinal(plain)
    }

    override fun open(sealed: ByteArray): ByteArray {
        require(sealed.size > MAGIC.size + IV && sealed.copyOfRange(0, MAGIC.size).contentEquals(MAGIC)) { "Not a sealed letters file" }
        val iv = sealed.copyOfRange(MAGIC.size, MAGIC.size + IV)
        val cipher = Cipher.getInstance(TRANSFORM).apply { init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv)) }
        return cipher.doFinal(sealed, MAGIC.size + IV, sealed.size - MAGIC.size - IV)
    }

    companion object {
        private const val KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORM = "AES/GCM/NoPadding"
        private const val IV = 12
        /** "DCY" and a format version, so a future format can tell old files apart. */
        val MAGIC = byteArrayOf(0x44, 0x43, 0x59, 0x01)
    }
}

/**
 * Keeps every letter in one encrypted file in the app's private storage. Nothing is sent anywhere.
 * A plain letters.json from an older version is encrypted on first load and then deleted.
 */
class LetterStore(dir: File, private val vault: Vault) {
    constructor(context: Context) : this(context.filesDir, KeystoreVault())

    private val file = File(dir, "letters.enc")
    private val legacy = File(dir, "letters.json")
    private val mutex = Mutex()
    private val _letters = MutableStateFlow(load())
    val letters: StateFlow<List<Letter>> = _letters.asStateFlow()

    private fun load(): List<Letter> {
        if (file.exists()) {
            return try {
                json.decodeFromString(serializer, vault.open(file.readBytes()).decodeToString())
            } catch (e: Exception) {
                // Never let a later save overwrite letters we couldn't read: set the file aside instead.
                file.renameTo(File(file.parentFile, "letters.enc.unreadable-${System.currentTimeMillis()}"))
                emptyList()
            }
        }
        if (!legacy.exists()) return emptyList()
        val old = try {
            json.decodeFromString(serializer, legacy.readText())
        } catch (e: Exception) {
            return emptyList()
        }
        write(old)
        legacy.delete()
        return old
    }

    suspend fun upsert(letter: Letter) = update { list ->
        val i = list.indexOfFirst { it.id == letter.id }
        if (i >= 0) list.toMutableList().also { it[i] = letter } else list + letter
    }

    suspend fun replaceAll(letters: List<Letter>) = update { letters }

    suspend fun clear() = update { emptyList() }

    fun get(id: String): Letter? = _letters.value.firstOrNull { it.id == id }

    private suspend fun update(change: (List<Letter>) -> List<Letter>) = mutex.withLock {
        val next = change(_letters.value)
        withContext(Dispatchers.IO) { write(next) }
        _letters.value = next
    }

    /** Writes to a temporary file first, so a crash mid-write never leaves a half-written file. */
    private fun write(letters: List<Letter>) {
        val tmp = File(file.parentFile, "letters.enc.tmp")
        tmp.writeBytes(vault.seal(json.encodeToString(serializer, letters).encodeToByteArray()))
        if (!tmp.renameTo(file)) {
            file.delete()
            tmp.renameTo(file)
        }
    }

    companion object {
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        val serializer = ListSerializer(Letter.serializer())
    }
}
