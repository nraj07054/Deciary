package app.deciary.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LogicTest {
    private fun letter(id: String = "a", opens: LocalDate = LocalDate.now(), review: Review? = null) = Letter(
        id = id, question = "Accept the job?", choice = "Yes, \"finally\"", prediction = "I'll be glad, mostly",
        why = "Line one\nline two", mood = Mood.Calm, confidence = 80, sealedAt = 0L, opensOn = opens.toEpochDay(), review = review,
    )

    @Test fun verdictSquares() {
        assertEquals(Verdict.EarnedIt, suggestVerdict(Compared.Better, CameTrue.Yes, SameAgain.Yes))
        assertEquals(Verdict.BadLuck, suggestVerdict(Compared.Worse, CameTrue.No, SameAgain.Yes))
        assertEquals(Verdict.LuckyBreak, suggestVerdict(Compared.Better, CameTrue.No, SameAgain.No))
        assertEquals(Verdict.Lesson, suggestVerdict(Compared.Worse, CameTrue.No, SameAgain.No))
        assertEquals(Verdict.Lesson, suggestVerdict(Compared.Worse, CameTrue.No, SameAgain.NotSure))
        assertEquals(Verdict.EarnedIt, suggestVerdict(Compared.Same, CameTrue.Partly, SameAgain.NotSure))
    }

    @Test fun confidenceWords() {
        assertEquals("Hunch", confidenceWord(60))
        assertEquals("Fairly sure", confidenceWord(75))
        assertEquals("Confident", confidenceWord(90))
        assertEquals("Certain", confidenceWord(95))
    }

    @Test fun dueAndTypoWindow() {
        val today = LocalDate.of(2026, 10, 26)
        assertTrue(letter(opens = today).isDue(today))
        assertFalse(letter(opens = today.plusDays(1)).isDue(today))
        assertFalse(letter(opens = today, review = Review(verdict = Verdict.Lesson, openedAt = 1)).isDue(today))
        val l = letter().copy(sealedAt = 1_000_000L)
        assertTrue(l.canFixTypo(1_000_000L + 9 * 60_000))
        assertFalse(l.canFixTypo(1_000_000L + 10 * 60_000))
    }

    @Test fun backupRoundTrip() {
        val letters = listOf(letter("a"), letter("b", review = Review(whatHappened = "Fine", verdict = Verdict.EarnedIt, openedAt = 5)))
        val file = Backup.encrypt(letters, "correct horse")
        assertFalse(file.contains("Accept the job"))
        assertEquals(letters, Backup.decrypt(file, "correct horse"))
    }

    @Test(expected = WrongPasswordException::class)
    fun backupWrongPassword() {
        Backup.decrypt(Backup.encrypt(listOf(letter()), "right one"), "wrong one")
    }

    @Test(expected = NotABackupException::class)
    fun notABackup() {
        Backup.decrypt("{\"hello\": 1}", "x")
    }

    @Test fun safetyCheck() {
        assertTrue(Safety.needsCare("Should I quit", "I don’t want to live like this"))
        assertTrue(Safety.needsCare("I keep thinking about suicide"))
        assertFalse(Safety.needsCare("Buy a second-hand car", "This commute is killing me"))
    }
}
