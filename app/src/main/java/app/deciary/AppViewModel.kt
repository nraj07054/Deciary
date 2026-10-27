package app.deciary

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.deciary.data.Backup
import app.deciary.data.CameTrue
import app.deciary.data.Compared
import app.deciary.data.Leather
import app.deciary.data.Letter
import app.deciary.data.Mood
import app.deciary.data.Review
import app.deciary.data.SameAgain
import app.deciary.data.ThemeChoice
import app.deciary.data.Verdict
import app.deciary.data.suggestVerdict
import app.deciary.reminders.Reminders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID

/** The letter being written, before it is sealed. */
data class Draft(
    val question: String = "",
    val choice: String = "",
    val prediction: String = "",
    val why: String = "",
    val mood: Mood? = null,
    val confidence: Int? = null,
    val opensOn: LocalDate = LocalDate.now().plusMonths(3),
)

/** Answers on the "Then & now" screen, carried to the verdict. */
data class ReviewDraft(
    val letterId: String,
    val whatHappened: String = "",
    val compared: Compared = Compared.Same,
    val cameTrue: CameTrue = CameTrue.Partly,
    val sameAgain: SameAgain = SameAgain.Yes,
    val lesson: String = "",
) {
    val suggested: Verdict get() = suggestVerdict(compared, cameTrue, sameAgain)
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val deciary = app as DeciaryApplication
    private val store = deciary.letters
    private val settings = deciary.settings

    val letters = store.letters
    val prefs = settings.prefs

    private val _locked = MutableStateFlow(settings.prefs.value.lockOn)
    val locked = _locked.asStateFlow()

    var draft by mutableStateOf(Draft())
    var writeStep by mutableIntStateOf(0)
    var reviewDraft by mutableStateOf<ReviewDraft?>(null)

    fun letter(id: String): Letter? = store.get(id)

    // Lock

    fun lock() {
        if (prefs.value.lockOn) _locked.value = true
    }

    fun tryUnlock(pin: String): Boolean = settings.checkPin(pin).also { if (it) _locked.value = false }

    fun unlockWithBiometric() {
        _locked.value = false
    }

    fun setPin(pin: String, biometric: Boolean) {
        settings.setPin(pin)
        settings.setBiometric(biometric)
    }

    fun checkPin(pin: String) = settings.checkPin(pin)

    /** How long until another PIN can be tried, after too many wrong ones. */
    fun pinWaitMs() = settings.pinWaitMs()

    fun turnLockOff() = settings.clearPin()

    fun setBiometric(on: Boolean) = settings.setBiometric(on)

    // Onboarding and settings

    fun finishOnboarding(name: String, leather: Leather) {
        settings.setCover(name, leather)
        settings.setOnboarded()
        val p = settings.prefs.value
        if (p.reminders) Reminders.schedule(getApplication(), p.reminderHour)
    }

    fun setCover(name: String, leather: Leather) = settings.setCover(name, leather)

    fun setReminders(on: Boolean) {
        settings.setReminders(on)
        if (on) Reminders.schedule(getApplication(), prefs.value.reminderHour) else Reminders.cancel(getApplication())
    }

    fun setReminderHour(hour: Int) {
        settings.setReminderHour(hour)
        if (prefs.value.reminders) Reminders.schedule(getApplication(), hour)
    }

    fun setReviewMonths(months: Int) = settings.setReviewMonths(months)

    fun setTheme(theme: ThemeChoice) = settings.setTheme(theme)

    // Writing

    fun startDraft() {
        draft = Draft(opensOn = LocalDate.now().plusMonths(prefs.value.reviewMonths.toLong()))
        writeStep = 0
    }

    /** Seals the draft. Once sealed, the prediction and how sure are locked. */
    fun seal(challenged: Boolean, onDone: (String) -> Unit) {
        val d = draft
        val letter = Letter(
            id = UUID.randomUUID().toString(),
            question = d.question.trim(),
            choice = d.choice.trim(),
            prediction = d.prediction.trim(),
            why = d.why.trim(),
            mood = d.mood,
            confidence = d.confidence,
            sealedAt = System.currentTimeMillis(),
            opensOn = d.opensOn.toEpochDay(),
            challenged = challenged,
        )
        viewModelScope.launch {
            store.upsert(letter)
            onDone(letter.id)
        }
    }

    /** A typo fix, allowed for ten minutes after sealing. */
    fun fixTypo(id: String, question: String, choice: String, prediction: String, why: String): Boolean {
        val l = store.get(id) ?: return false
        if (!l.canFixTypo()) return false
        viewModelScope.launch {
            store.upsert(l.copy(question = question.trim(), choice = choice.trim(), prediction = prediction.trim(), why = why.trim()))
        }
        return true
    }

    // Reviewing

    fun startReview(id: String) {
        if (reviewDraft?.letterId != id) reviewDraft = ReviewDraft(id)
    }

    fun stamp(verdict: Verdict, onDone: () -> Unit) {
        val r = reviewDraft ?: return
        val l = store.get(r.letterId) ?: return
        viewModelScope.launch {
            store.upsert(
                l.copy(
                    review = Review(
                        whatHappened = r.whatHappened.trim(), compared = r.compared, cameTrue = r.cameTrue,
                        sameAgain = r.sameAgain, lesson = r.lesson.trim(), verdict = verdict,
                        openedAt = System.currentTimeMillis(),
                    ),
                ),
            )
            reviewDraft = null
            onDone()
        }
    }

    // Your data

    suspend fun backup(uri: Uri, password: String): Result<Unit> = io {
        val text = Backup.encrypt(letters.value, password)
        write(uri, text)
        settings.setLastBackup(System.currentTimeMillis())
    }

    suspend fun restore(uri: Uri, password: String): Result<Int> = io {
        val text = read(uri)
        val restored = Backup.decrypt(text, password)
        // Keep letters already on this phone; the backup fills in the rest.
        val byId = letters.value.associateBy { it.id }.toMutableMap()
        restored.forEach { byId.putIfAbsent(it.id, it) }
        store.replaceAll(byId.values.sortedBy { it.sealedAt })
        restored.size
    }

    suspend fun exportJson(uri: Uri): Result<Unit> = io { write(uri, Backup.exportJson(letters.value)) }

    suspend fun exportCsv(uri: Uri): Result<Unit> = io { write(uri, Backup.exportCsv(letters.value)) }

    fun deleteEverything(onDone: () -> Unit) {
        viewModelScope.launch {
            store.clear()
            settings.clearAll()
            Reminders.cancel(getApplication())
            _locked.value = false
            draft = Draft()
            reviewDraft = null
            onDone()
        }
    }

    private suspend fun <T> io(block: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun write(uri: Uri, text: String) {
        val out = getApplication<Application>().contentResolver.openOutputStream(uri, "wt") ?: error("Couldn't open the file.")
        out.use { it.write(text.toByteArray()) }
    }

    private fun read(uri: Uri): String {
        val input = getApplication<Application>().contentResolver.openInputStream(uri) ?: error("Couldn't open the file.")
        return input.use { it.readBytes().toString(Charsets.UTF_8) }
    }
}
