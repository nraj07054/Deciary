package app.deciary.data

import android.content.Context
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

/** Keeps every letter in one JSON file in the app's private storage. Nothing is sent anywhere. */
class LetterStore(context: Context) {
    private val file = File(context.filesDir, "letters.json")
    private val mutex = Mutex()
    private val _letters = MutableStateFlow(load())
    val letters: StateFlow<List<Letter>> = _letters.asStateFlow()

    private fun load(): List<Letter> = try {
        if (file.exists()) json.decodeFromString(serializer, file.readText()) else emptyList()
    } catch (e: Exception) {
        emptyList()
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
        withContext(Dispatchers.IO) {
            val tmp = File(file.parentFile, "letters.json.tmp")
            tmp.writeText(json.encodeToString(serializer, next))
            if (!tmp.renameTo(file)) {
                file.delete()
                tmp.renameTo(file)
            }
        }
        _letters.value = next
    }

    companion object {
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        val serializer = ListSerializer(Letter.serializer())
    }
}
