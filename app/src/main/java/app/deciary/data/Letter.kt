package app.deciary.data

import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** How the person felt when they made the decision. Always shown with its word, never colour alone. */
enum class Mood(val label: String) {
    Calm("Calm"),
    Rushed("Rushed"),
    Anxious("Anxious"),
    Excited("Excited"),
    Tired("Tired"),
}

enum class Verdict(val label: String, val sub: String, val headA: String, val headB: String, val body: String) {
    EarnedIt(
        "Earned it", "Sound thinking", "You", "earned it.",
        "Your reasons held up, and it went well. Worth remembering what you checked before deciding.",
    ),
    BadLuck(
        "Bad luck", "Sound thinking", "Good thinking,", "rough luck.",
        "Keep the approach. Not every outcome is yours to control.",
    ),
    LuckyBreak(
        "Lucky break", "Shaky thinking", "A lucky", "break.",
        "It went well anyway. Worth knowing why it worked.",
    ),
    Lesson(
        "Lesson", "Shaky thinking", "A lesson,", "kept.",
        "This one taught you something. What would you check first next time?",
    ),
}

enum class Compared(val label: String) { Better("Better"), Same("Same"), Worse("Worse") }
enum class CameTrue(val label: String) { Yes("Yes"), Partly("Partly"), No("No") }
enum class SameAgain(val label: String) { Yes("Yes"), NotSure("Not sure"), No("No") }

@Serializable
data class Review(
    val whatHappened: String = "",
    val compared: Compared = Compared.Same,
    val cameTrue: CameTrue = CameTrue.Partly,
    val sameAgain: SameAgain = SameAgain.Yes,
    val lesson: String = "",
    val verdict: Verdict,
    val openedAt: Long,
)

@Serializable
data class Letter(
    val id: String,
    /** What the person was deciding, e.g. "Accept the Pune job offer?" */
    val question: String,
    /** What they chose. */
    val choice: String,
    val prediction: String = "",
    val why: String = "",
    val mood: Mood? = null,
    /** 50–100, or null when the person skipped it. */
    val confidence: Int? = null,
    val sealedAt: Long,
    /** The day the letter opens, as [LocalDate.toEpochDay]. */
    val opensOn: Long,
    val challenged: Boolean = false,
    val review: Review? = null,
) {
    val title: String get() = choice.ifBlank { question }
    val opensDate: LocalDate get() = LocalDate.ofEpochDay(opensOn)
    val sealedDate: LocalDate get() = Instant.ofEpochMilli(sealedAt).atZone(ZoneId.systemDefault()).toLocalDate()
    val isOpened: Boolean get() = review != null

    fun isDue(today: LocalDate = LocalDate.now()): Boolean = !isOpened && !opensDate.isAfter(today)

    /** The prediction and how sure are locked once sealed, apart from a short window to fix typos. */
    fun canFixTypo(now: Long = System.currentTimeMillis()): Boolean =
        !isOpened && now - sealedAt < TYPO_WINDOW_MS

    companion object {
        const val TYPO_WINDOW_MS = 10 * 60 * 1000L
    }
}

fun confidenceWord(value: Int): String = when {
    value <= 60 -> "Hunch"
    value <= 75 -> "Fairly sure"
    value <= 90 -> "Confident"
    else -> "Certain"
}

/** Suggests a verdict square from the review answers; the person can always pick another. */
fun suggestVerdict(compared: Compared, cameTrue: CameTrue, sameAgain: SameAgain): Verdict {
    val wentWell = compared != Compared.Worse
    val soundThinking = when (sameAgain) {
        SameAgain.Yes -> true
        SameAgain.NotSure -> cameTrue != CameTrue.No
        SameAgain.No -> false
    }
    return when {
        wentWell && soundThinking -> Verdict.EarnedIt
        !wentWell && soundThinking -> Verdict.BadLuck
        wentWell -> Verdict.LuckyBreak
        else -> Verdict.Lesson
    }
}
