package app.deciary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.deciary.ui.components.BalanceArt
import app.deciary.ui.components.CrystalBallArt
import app.deciary.ui.components.EnvelopeArt
import app.deciary.ui.components.EyeArt
import app.deciary.ui.components.FileCabinetArt
import app.deciary.ui.components.HourglassArt
import app.deciary.ui.components.IncomingEnvelopeArt
import app.deciary.ui.components.MailboxArt
import app.deciary.ui.components.NotebookArt
import app.deciary.ui.components.PadlockArt
import app.deciary.ui.components.PenArt
import app.deciary.ui.components.SeedlingArt
import app.deciary.ui.components.ShieldArt
import app.deciary.ui.components.SparklesArt
import app.deciary.ui.components.StampArt
import app.deciary.ui.theme.DeciaryTheme
import app.deciary.ui.theme.Type

/** Debug only: shows every drawn illustration at a large and a small size. Launch with `am start -n app.deciary/.ArtGalleryActivity`. */
class ArtGalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Same bars as the app's light theme, so previews show what the app shows.
        val bars = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(bars, bars)
        super.onCreate(savedInstanceState)
        val tint = Color(0xFFEDE7FD)
        val pieces: List<Pair<String, @Composable () -> Unit>> = listOf(
            "Envelope" to { EnvelopeArt(150.dp, initial = "P") },
            "Love letter" to { EnvelopeArt(150.dp, heart = true) },
            "Incoming" to { IncomingEnvelopeArt(150.dp) },
            "Sparkles" to { SparklesArt(130.dp) },
            "Stamp" to { StampArt(130.dp, edge = tint) },
            "Shield" to { ShieldArt(140.dp) },
            "Notebook" to { NotebookArt(140.dp) },
            "Padlock" to { PadlockArt(140.dp) },
            "Seedling" to { SeedlingArt(130.dp) },
            "Pen" to { PenArt(130.dp) },
            "Hourglass" to { HourglassArt(130.dp) },
            "Crystal ball" to { CrystalBallArt(130.dp) },
            "Balance" to { BalanceArt(130.dp) },
            "Eye" to { EyeArt(130.dp) },
            "Mailbox" to { MailboxArt(140.dp) },
            "File cabinet" to { FileCabinetArt(140.dp) },
        )
        val screen = intent.getStringExtra("screen")
        setContent {
            if (screen != null) {
                DeciaryTheme { ScreenPreview(screen) }
                return@setContent
            }
            DeciaryTheme {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    Modifier.fillMaxSize().background(Color(0xFFF5F2FB)).statusBarsPadding().verticalScroll(rememberScrollState()).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    pieces.forEach { (name, art) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(172.dp).clip(RoundedCornerShape(20.dp)).background(tint), contentAlignment = Alignment.Center) { art() }
                            Text(name, style = Type.small)
                        }
                    }
                }
            }
        }
    }
}

/** Debug only: real screens with sample data, for screenshots (the app itself blocks them while the lock is on). */
@Composable
private fun ScreenPreview(screen: String) {
    when (screen) {
        "lock" -> app.deciary.ui.screens.PinPad(
            leather = app.deciary.data.Leather.Ink, title = "Priya", subtitle = "Only you can open this",
            prompt = "Enter your PIN", error = null, onComplete = { false },
            footer = { app.deciary.ui.screens.FingerprintPill {} },
        )
        "pin-setup" -> app.deciary.ui.screens.PinSetupScreen(app.deciary.data.Leather.Ink, onCancel = {}, onDone = {})
        "home" -> app.deciary.ui.screens.HomeScreen(Samples.prefs, Samples.letters.filter { it.id != "busy" }, {}, {}, {}, {}, {}, {})
        "home-busy" -> app.deciary.ui.screens.HomeScreen(Samples.prefs.copy(reminders = false), Samples.busy, {}, {}, {}, {}, {}, {})
        "home-empty" -> app.deciary.ui.screens.HomeScreen(Samples.prefs, emptyList(), {}, {}, {}, {}, {}, {})
        "write-0", "write-1", "write-2", "write-3" -> {
            var d by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(Samples.draft) }
            app.deciary.ui.screens.WriteScreen(d, screen.last().digitToInt(), { d = it }, {}, {}, {}, {})
        }
        "letter" -> app.deciary.ui.screens.LetterScreen(Samples.car, "Priya", {}, {}, {})
        "letter-sealed" -> app.deciary.ui.screens.LetterScreen(Samples.letters[4], "Priya", {}, {}, {})
        "letter-opened" -> app.deciary.ui.screens.LetterScreen(Samples.letters.last(), "Priya", {}, {}, {})
        "review" -> {
            var r by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(app.deciary.ReviewDraft("car", whatHappened = "One repair, the clutch, ₹18,000. Still saved about ₹1.8 lakh.", compared = app.deciary.data.Compared.Better)) }
            app.deciary.ui.screens.ReviewScreen(Samples.car, r, { r = it }, {}, {})
        }
        "verdict" -> app.deciary.ui.screens.VerdictScreen(app.deciary.data.Verdict.EarnedIt, false, {}, {})
        "shelf" -> app.deciary.ui.screens.ShelfScreen(Samples.letters, {}, {})
        "settings" -> app.deciary.ui.screens.SettingsScreen(
            Samples.prefs,
            app.deciary.ui.screens.SettingsActions({}, {}, {}, {}, { false }, {}, {}, {}, {}, {}),
        )
        "data" -> app.deciary.ui.screens.DataScreen(
            Samples.prefs, 8,
            app.deciary.ui.screens.DataActions({}, { _, _ -> Result.success(Unit) }, { _, _ -> Result.success(0) }, { false }, {}),
        )
        "safety" -> androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.fillMaxSize().background(app.deciary.ui.theme.Deciary.colors.bg)) { app.deciary.ui.screens.SafetySheet({}, {}) }
        "challenge" -> app.deciary.ui.screens.ChallengeScreen(95, {}, {}, {})
        "sealed" -> app.deciary.ui.screens.SealedScreen(Samples.letters[4], Samples.prefs.copy(reminders = false), {})
    }
}

/** Sample letters matching the design mockups. */
object Samples {
    private val today = java.time.LocalDate.now()
    private fun day(offset: Long) = today.plusDays(offset).toEpochDay()
    private fun ago(days: Long) = System.currentTimeMillis() - days * 86_400_000L
    val prefs = app.deciary.data.Prefs(onboarded = true, ownerName = "Priya", hasPin = true, lastBackupAt = ago(12))
    private fun review(v: app.deciary.data.Verdict, daysAgo: Long) = app.deciary.data.Review(
        whatHappened = "One repair, the clutch, ₹18,000. Still saved about ₹1.8 lakh.",
        compared = app.deciary.data.Compared.Better, verdict = v, openedAt = ago(daysAgo),
    )
    val car = app.deciary.data.Letter(
        "car", "Buy a used car?", "Buy a second-hand car instead of a new one",
        prediction = "Fewer than two repairs in three months, and I'll save about ₹2 lakh.",
        why = "One owner, full service record. The EMI on a new hatchback would eat the travel fund.",
        mood = app.deciary.data.Mood.Calm, confidence = 70, sealedAt = ago(92), opensOn = day(0),
    )
    val letters = listOf(
        car,
        app.deciary.data.Letter("gym", "Gym plan?", "Switch to the annual gym plan", mood = app.deciary.data.Mood.Calm, confidence = 80, sealedAt = ago(30), opensOn = day(11)),
        app.deciary.data.Letter("robot", "Robotics club?", "Let Riya join the robotics club", mood = app.deciary.data.Mood.Excited, confidence = 75, sealedAt = ago(20), opensOn = day(22)),
        app.deciary.data.Letter("loan", "Refinance?", "Refinance the home loan", mood = app.deciary.data.Mood.Tired, confidence = 60, sealedAt = ago(10), opensOn = day(71)),
        app.deciary.data.Letter("pune", "Accept the Pune job offer?", "Accept the Pune job offer?", prediction = "By April I'll be settled in, and glad I moved.", mood = app.deciary.data.Mood.Excited, confidence = 95, sealedAt = ago(2), opensOn = day(180)),
        app.deciary.data.Letter("wed", "Skip the wedding?", "Skip my cousin's wedding to study for the exam", mood = app.deciary.data.Mood.Anxious, sealedAt = ago(110), opensOn = day(-25), review = review(app.deciary.data.Verdict.Lesson, 25)),
        app.deciary.data.Letter("acct", "Hire help?", "Hire a part-time accountant", mood = app.deciary.data.Mood.Rushed, sealedAt = ago(120), opensOn = day(-39), review = review(app.deciary.data.Verdict.LuckyBreak, 39)),
        car.copy(id = "car2", opensOn = day(-4), review = review(app.deciary.data.Verdict.EarnedIt, 4)),
    )
    val draft = app.deciary.Draft(
        question = "Accept the Pune job offer?", choice = "Yes, I'm taking it",
        prediction = "By April I'll be settled in, and glad I moved.", why = "Better team, and Pune is closer to family.",
        mood = app.deciary.data.Mood.Excited, confidence = 95,
    )
    val busy = (1..5).map { i -> app.deciary.data.Letter("b$i", "Q$i", listOf("Switch to the annual gym plan", "Let Riya join the robotics club", "Refinance the home loan", "Buy a standing desk", "Learn to swim")[i - 1], mood = app.deciary.data.Mood.Calm, sealedAt = ago(100L - i), opensOn = day(-6L + i)) }
}
