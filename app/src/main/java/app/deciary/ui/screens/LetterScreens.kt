package app.deciary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.deciary.R
import app.deciary.ReviewDraft
import app.deciary.data.CameTrue
import app.deciary.data.Compared
import app.deciary.data.Letter
import app.deciary.data.SameAgain
import app.deciary.data.Verdict
import app.deciary.ui.components.Art
import app.deciary.ui.components.Card
import app.deciary.ui.components.Field
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.MoodTag
import app.deciary.ui.components.PrimaryButton
import app.deciary.ui.components.QuietButton
import app.deciary.ui.components.Segmented
import app.deciary.ui.components.TopBar
import app.deciary.ui.components.TwoToneTitle
import app.deciary.ui.components.WaxSeal
import app.deciary.ui.components.art
import app.deciary.ui.components.initialOf
import app.deciary.ui.friendly
import app.deciary.ui.short
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Type
import app.deciary.ui.toLocalDate
import app.deciary.ui.untilText
import kotlinx.coroutines.delay
import java.time.LocalDate

@Composable
private fun Meta(letter: Letter) {
    val c = Deciary.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        letter.mood?.let {
            MoodTag(it)
            Text("  ·  ", style = Type.small, color = c.inkQuiet)
        }
        Text("Sealed ${letter.sealedDate.short()}", style = Type.small, color = c.inkQuiet)
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Spacer(Modifier.height(18.dp))
    Kicker(title)
    Spacer(Modifier.height(6.dp))
    content()
}

@Composable
fun LetterScreen(
    letter: Letter,
    ownerName: String,
    onBack: () -> Unit,
    onOpen: () -> Unit,
    onFixTypo: () -> Unit,
) {
    val c = Deciary.colors
    val today = LocalDate.now()
    var askEarly by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(letter.id) {
        while (letter.canFixTypo(now)) {
            delay(15_000)
            now = System.currentTimeMillis()
        }
    }

    Column(Modifier.fillMaxSize().background(c.bg).navigationBarsPadding()) {
        TopBar(onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            val review = letter.review
            when {
                review != null -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Kicker("Opened ${review.openedAt.toLocalDate().short(today)}", color = c.wax, modifier = Modifier.weight(1f))
                        VerdictStamp(review.verdict)
                    }
                }
                letter.isDue(today) -> Kicker("Due today", color = c.wax)
                else -> Kicker("Opens ${letter.opensDate.friendly(today)} · ${untilText(letter.opensDate, today)}", color = c.accentText)
            }
            Spacer(Modifier.height(8.dp))
            Text(letter.title, style = Type.title, color = c.ink)
            if (letter.choice.isNotBlank() && letter.question.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(letter.question, style = Type.body, color = c.inkQuiet)
            }
            Spacer(Modifier.height(8.dp))
            Meta(letter)
            Spacer(Modifier.height(18.dp))

            if (!letter.isOpened && !letter.isDue(today)) {
                // Still in the post: the envelope stays closed.
                Card(Modifier.fillMaxWidth(), color = c.tint, padding = 24.dp) {
                    Box(Modifier.align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center) {
                        Art(R.drawable.art_envelope, 150.dp)
                        WaxSeal(initialOf(ownerName.ifBlank { "Deciary" }), 46.dp, Modifier.padding(top = 34.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Your prediction is sealed inside. You'll see it again on ${letter.opensDate.friendly(today)}.",
                        style = Type.body, color = c.inkQuiet, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (letter.canFixTypo(now)) {
                    val mins = ((Letter.TYPO_WINDOW_MS - (now - letter.sealedAt)) / 60_000 + 1).coerceAtLeast(1)
                    Spacer(Modifier.height(12.dp))
                    QuietButton("Fix a typo · $mins min left", onClick = onFixTypo, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            } else {
                Card(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Kicker("Your prediction", modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.Lock, null, tint = c.inkQuiet, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Sealed" + (letter.confidence?.let { " · $it%" } ?: ""), style = Type.small, color = c.inkQuiet)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(letter.prediction.ifBlank { "You didn't write a prediction for this one." }, style = Type.letter, color = c.ink)
                    if (letter.why.isNotBlank()) {
                        Section("Why you chose it") { Text(letter.why, style = Type.body, color = c.ink) }
                    }
                }
                if (review != null) {
                    Spacer(Modifier.height(16.dp))
                    Card(Modifier.fillMaxWidth()) {
                        Kicker("What happened")
                        Spacer(Modifier.height(6.dp))
                        Text(review.whatHappened.ifBlank { "Not written down." }, style = Type.letter, color = c.ink)
                        Section("Your answers") {
                            Text(
                                "${review.compared.label} than expected · Came true: ${review.cameTrue.label} · Same again: ${review.sameAgain.label}",
                                style = Type.body, color = c.ink,
                            )
                        }
                        if (review.lesson.isNotBlank()) {
                            Section("One line for next time") { Text(review.lesson, style = Type.body, color = c.ink) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        if (!letter.isOpened) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
                if (letter.isDue(today)) {
                    PrimaryButton("Open letter", onClick = onOpen)
                } else {
                    QuietButton("Open it early", onClick = { askEarly = true }, color = c.inkQuiet, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }

    if (askEarly) {
        AlertDialog(
            onDismissRequest = { askEarly = false },
            title = { Text("Open it early?", style = Type.heading) },
            text = { Text("Past you picked ${letter.opensDate.friendly(today)} for a reason. You can still open it now if things have already played out.", style = Type.body) },
            confirmButton = { TextButton(onClick = { askEarly = false; onOpen() }) { Text("Open now") } },
            dismissButton = { TextButton(onClick = { askEarly = false }) { Text("Keep it sealed") } },
        )
    }
}

@Composable
private fun VerdictStamp(verdict: Verdict) {
    val c = Deciary.colors
    Row(
        Modifier.rotate(-4f).border(2.dp, c.wax, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Art(verdict.art(), 22.dp)
        Spacer(Modifier.width(6.dp))
        Text(verdict.label.uppercase(), style = Type.label, color = c.wax)
    }
}

/** Fix a typo within ten minutes of sealing. */
@Composable
fun TypoFixScreen(letter: Letter, onBack: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    val c = Deciary.colors
    var question by rememberSaveable { mutableStateOf(letter.question) }
    var choice by rememberSaveable { mutableStateOf(letter.choice) }
    var prediction by rememberSaveable { mutableStateOf(letter.prediction) }
    var why by rememberSaveable { mutableStateOf(letter.why) }
    Column(Modifier.fillMaxSize().background(c.bg).imePadding().navigationBarsPadding()) {
        TopBar(title = "Fix a typo", onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("For small fixes only. How sure you were stays as it was.", style = Type.body, color = c.inkQuiet)
            Field(question, { question = it }, "The decision", minLines = 2)
            Field(choice, { choice = it }, "What you chose", minLines = 2)
            Field(prediction, { prediction = it }, "Your prediction", minLines = 3, letterStyle = true)
            Field(why, { why = it }, "Why you chose it", minLines = 2)
        }
        PrimaryButton(
            "Save fix",
            onClick = { onSave(question, choice, prediction, why) },
            enabled = question.isNotBlank() && choice.isNotBlank(),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
    }
}

/** "Then & now": what past you expected, next to what happened. */
@Composable
fun ReviewScreen(letter: Letter, draft: ReviewDraft, onDraft: (ReviewDraft) -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
    val c = Deciary.colors
    Column(Modifier.fillMaxSize().background(c.bg).imePadding().navigationBarsPadding()) {
        TopBar(title = "Then & now", onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Card(Modifier.fillMaxWidth(), color = c.tint) {
                Row {
                    Kicker("Then · ${letter.sealedDate.short()}", modifier = Modifier.weight(1f))
                    letter.confidence?.let { Text("$it% sure", style = Type.small.copy(fontWeight = FontWeight.Bold), color = c.accentText) }
                }
                Spacer(Modifier.height(8.dp))
                Text(letter.prediction.ifBlank { letter.title }, style = Type.letter, color = c.ink)
            }
            Spacer(Modifier.height(16.dp))
            Kicker("Now · ${LocalDate.now().short()}")
            Spacer(Modifier.height(8.dp))
            Field(draft.whatHappened, { onDraft(draft.copy(whatHappened = it)) }, "What happened?", minLines = 3, letterStyle = true)
            Spacer(Modifier.height(20.dp))
            Question("Compared to what you expected") {
                Segmented(Compared.entries, draft.compared, { it.label }, { onDraft(draft.copy(compared = it)) })
            }
            Question("Did your prediction come true?") {
                Segmented(CameTrue.entries, draft.cameTrue, { it.label }, { onDraft(draft.copy(cameTrue = it)) })
            }
            Question("Would you choose the same again?") {
                Segmented(SameAgain.entries, draft.sameAgain, { it.label }, { onDraft(draft.copy(sameAgain = it)) })
            }
            Spacer(Modifier.height(6.dp))
            Field(draft.lesson, { onDraft(draft.copy(lesson = it)) }, "One line for next time · optional", singleLine = true)
            Spacer(Modifier.height(20.dp))
        }
        PrimaryButton("See verdict", onClick = onNext, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
    }
}

@Composable
private fun Question(q: String, content: @Composable () -> Unit) {
    Text(q, style = Type.bodyStrong, color = Deciary.colors.ink)
    Spacer(Modifier.height(8.dp))
    content()
    Spacer(Modifier.height(18.dp))
}

/** The four squares: sound or shaky thinking, against how it went. */
@Composable
fun VerdictScreen(suggested: Verdict, worse: Boolean, onBack: () -> Unit, onStamp: (Verdict) -> Unit) {
    val c = Deciary.colors
    var pick by rememberSaveable { mutableStateOf(suggested) }
    Column(Modifier.fillMaxSize().background(c.bg).navigationBarsPadding()) {
        TopBar(title = "The verdict", onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            // Playfulness switches off when things went worse: plain heading, no flourish.
            if (worse) {
                Text("${pick.headA} ${pick.headB}", style = Type.display, color = c.ink)
            } else {
                TwoToneTitle(pick.headA, pick.headB)
            }
            Spacer(Modifier.height(18.dp))
            Row {
                Text("It went well", style = Type.label, color = c.inkQuiet, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Spacer(Modifier.width(12.dp))
                Text("It went badly", style = Type.label, color = c.inkQuiet, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(8.dp))
            val grid = listOf(listOf(Verdict.EarnedIt, Verdict.BadLuck), listOf(Verdict.LuckyBreak, Verdict.Lesson))
            grid.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { v -> VerdictCell(v, v == pick, Modifier.weight(1f)) { pick = v } }
                }
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(pick.body, style = Type.body, color = c.ink)
            Spacer(Modifier.height(8.dp))
            Text("Suggested from your answers. Tap another square to change it.", style = Type.small, color = c.inkQuiet)
            Spacer(Modifier.height(20.dp))
        }
        PrimaryButton("Stamp it: ${pick.label}", onClick = { onStamp(pick) }, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
    }
}

@Composable
private fun VerdictCell(v: Verdict, on: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = Deciary.colors
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier
            .aspectRatio(1f)
            .then(if (on) Modifier.shadow(10.dp, shape, spotColor = c.wax) else Modifier)
            .clip(shape)
            .background(if (on) c.surface else c.tint)
            .then(if (on) Modifier.border(2.dp, c.wax, shape) else Modifier)
            .semantics { selected = on }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(12.dp),
    ) {
        Column(Modifier.align(Alignment.Center).alpha(if (on) 1f else 0.55f), horizontalAlignment = Alignment.CenterHorizontally) {
            Art(v.art(), 56.dp)
            Spacer(Modifier.height(8.dp))
            Text(v.label, style = Type.bodyStrong.copy(fontWeight = if (on) FontWeight.ExtraBold else FontWeight.Medium), color = if (on) c.ink else c.inkQuiet)
            Text(v.sub, style = Type.small, color = c.inkQuiet)
        }
        if (on) {
            Box(
                Modifier.align(Alignment.TopEnd).size(22.dp).clip(CircleShape).background(c.wax),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
        }
    }
}
