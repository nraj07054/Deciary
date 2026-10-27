package app.deciary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import app.deciary.ui.components.EnvelopeArt
import app.deciary.ui.components.LabeledField
import app.deciary.ui.theme.Fraunces
import app.deciary.ui.theme.color
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import app.deciary.ReviewDraft
import app.deciary.data.CameTrue
import app.deciary.data.Compared
import app.deciary.data.Letter
import app.deciary.data.SameAgain
import app.deciary.data.Verdict
import app.deciary.ui.components.Art
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.PrimaryButton
import app.deciary.ui.components.QuietButton
import app.deciary.ui.components.Segmented
import app.deciary.ui.components.TopBar
import app.deciary.ui.components.TwoToneTitle
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

/** "Calm · Sealed 23 Jul", with the mood's dot. */
@Composable
private fun Meta(letter: Letter) {
    val c = Deciary.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        letter.mood?.let {
            Box(Modifier.size(8.dp).clip(CircleShape).background(it.color()))
            Spacer(Modifier.width(6.dp))
        }
        Text(listOfNotNull(letter.mood?.label, "Sealed ${letter.sealedDate.short()}").joinToString(" · "), style = Type.small, color = c.inkQuiet)
    }
}

/** A white section card with a small heading, as on the letter screen. */
@Composable
private fun Section(title: String, badge: (@Composable () -> Unit)? = null, content: @Composable () -> Unit) {
    val c = Deciary.colors
    val shape = RoundedCornerShape(22.dp)
    Column(
        Modifier.fillMaxWidth().shadow(10.dp, shape, ambientColor = Color(0x661D1A33), spotColor = Color(0x661D1A33)).clip(shape).background(c.surface)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title.uppercase(), style = Type.label.copy(letterSpacing = 0.96.sp), color = c.inkQuiet, modifier = Modifier.weight(1f))
            badge?.invoke()
        }
        content()
    }
}

private val LetterVoice = TextStyle(fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 25.sp)

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
    val review = letter.review
    val inPost = !letter.isOpened && !letter.isDue(today)

    Column(Modifier.fillMaxSize().background(c.bg).navigationBarsPadding()) {
        TopBar(onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // The hero: the sealed envelope, or the verdict once it's opened.
            Box(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp).fillMaxWidth().height(196.dp).clip(RoundedCornerShape(32.dp)).background(c.tint),
                contentAlignment = Alignment.Center,
            ) {
                if (review != null) {
                    Art(review.verdict.art(), 120.dp, Modifier.rotate(-6f))
                } else {
                    EnvelopeArt(162.dp, Modifier.rotate(-5f), initial = initialOf(ownerName.ifBlank { "Deciary" }), sealModifier = Modifier.rotate(-12f), sealSize = 44.dp)
                }
                val chip = when {
                    review != null -> "Opened ${review.openedAt.toLocalDate().short(today)}"
                    letter.isDue(today) -> "Due today"
                    else -> "Opens ${letter.opensDate.short(today)} · ${untilText(letter.opensDate, today)}"
                }
                Text(
                    chip, style = Type.small.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = Color(0xFF1D1A33),
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(horizontal = 12.dp, vertical = 6.dp),
                )
                if (review != null) VerdictStamp(review.verdict, Modifier.align(Alignment.BottomEnd).padding(16.dp))
            }
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(letter.title, style = Type.display.copy(fontSize = 28.sp, lineHeight = 32.sp), color = c.ink)
                Meta(letter)
            }
            Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (inPost) {
                    Section("Your prediction", badge = { SealedBadge(letter.confidence) }) {
                        Text("Sealed inside. You'll see it again on ${letter.opensDate.friendly(today)}.", style = Type.body.copy(lineHeight = 22.sp), color = c.inkQuiet)
                    }
                    if (letter.canFixTypo(now)) {
                        val mins = ((Letter.TYPO_WINDOW_MS - (now - letter.sealedAt)) / 60_000 + 1).coerceAtLeast(1)
                        QuietButton("Fix a typo · $mins min left", onClick = onFixTypo, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                } else {
                    Section("Your prediction", badge = { SealedBadge(letter.confidence) }) {
                        Text(letter.prediction.ifBlank { "You didn't write a prediction for this one." }, style = LetterVoice, color = c.ink)
                    }
                    if (letter.why.isNotBlank()) {
                        Section("Why you chose it") { Text(letter.why, style = Type.body.copy(lineHeight = 22.sp), color = c.ink) }
                    }
                    if (review != null) {
                        Section("What happened") { Text(review.whatHappened.ifBlank { "Not written down." }, style = LetterVoice, color = c.ink) }
                        Section("Your answers") {
                            Text(
                                "${review.compared.label} than expected · Came true: ${review.cameTrue.label} · Same again: ${review.sameAgain.label}",
                                style = Type.body.copy(lineHeight = 22.sp), color = c.ink,
                            )
                        }
                        if (review.lesson.isNotBlank()) {
                            Section("One line for next time") { Text(review.lesson, style = Type.body.copy(lineHeight = 22.sp), color = c.ink) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        if (!letter.isOpened) {
            Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
                if (letter.isDue(today)) {
                    PrimaryButton("Open letter", onClick = onOpen)
                } else {
                    QuietButton("Open it early", onClick = { askEarly = true }, color = c.ink, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }

    if (askEarly) {
        AlertDialog(
            onDismissRequest = { askEarly = false },
            containerColor = c.surface,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Open it early?", style = Type.title, color = c.ink) },
            text = { Text("Past you picked ${letter.opensDate.friendly(today)} for a reason. You can still open it now if things have already played out.", style = Type.body.copy(lineHeight = 22.sp), color = c.inkQuiet) },
            confirmButton = { TextButton(onClick = { askEarly = false; onOpen() }) { Text("Open now", style = Type.button, color = c.accentText) } },
            dismissButton = { TextButton(onClick = { askEarly = false }) { Text("Keep it sealed", style = Type.button, color = c.ink) } },
        )
    }
}

/** The pink "Sealed · 70%" badge on the prediction card. */
@Composable
private fun SealedBadge(confidence: Int?) {
    val c = Deciary.colors
    Row(
        Modifier.clip(RoundedCornerShape(10.dp)).background(if (c.isDark) c.tint else Color(0xFFFDF0F5)).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Lock, null, tint = c.wax, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text("Sealed" + (confidence?.let { " · $it%" } ?: ""), style = Type.small.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = c.wax)
    }
}

@Composable
private fun VerdictStamp(verdict: Verdict, modifier: Modifier = Modifier) {
    val c = Deciary.colors
    Text(
        verdict.label.uppercase(),
        style = Type.label.copy(fontSize = 13.sp),
        color = c.wax,
        modifier = modifier.rotate(-6f).clip(RoundedCornerShape(10.dp)).background(Color.White).border(2.dp, c.wax, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
    )
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
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("For small fixes only. How sure you were stays as it was.", style = Type.body, color = c.inkQuiet, modifier = Modifier.padding(horizontal = 4.dp))
            LabeledField("The decision", question, { question = it }, singleLine = false, minHeight = 84.dp)
            LabeledField("What you chose", choice, { choice = it }, singleLine = false, minHeight = 84.dp)
            LabeledField("Your prediction", prediction, { prediction = it }, singleLine = false, minHeight = 110.dp, textStyle = LetterVoice.copy(fontSize = 17.sp))
            LabeledField("Why you chose it", why, { why = it }, singleLine = false, minHeight = 84.dp)
        }
        PrimaryButton(
            "Save fix",
            onClick = { onSave(question, choice, prediction, why) },
            enabled = question.isNotBlank() && choice.isNotBlank(),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        )
    }
}

/** "Then & now": what past you expected, next to what happened. */
@Composable
fun ReviewScreen(letter: Letter, draft: ReviewDraft, onDraft: (ReviewDraft) -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
    val c = Deciary.colors
    Column(Modifier.fillMaxSize().background(c.bg).imePadding().navigationBarsPadding()) {
        TopBar(onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            TwoToneTitle("Then", "& now", Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 18.dp))
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(c.tint).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("THEN · ${letter.sealedDate.short().uppercase()}", style = Type.label.copy(letterSpacing = 0.96.sp), color = if (c.isDark) c.accentText else Color(0xFF4A3A8C), modifier = Modifier.weight(1f))
                        letter.confidence?.let {
                            Text("$it% sure", style = Type.small.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = Color(0xFF1D1A33), modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White).padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                    Text(letter.prediction.ifBlank { letter.title }, style = LetterVoice, color = c.ink)
                }
                val shape = RoundedCornerShape(24.dp)
                Column(
                    Modifier.fillMaxWidth().shadow(12.dp, shape, ambientColor = Color(0x731D1A33), spotColor = Color(0x731D1A33)).clip(shape).background(c.surface)
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    Text("NOW · ${LocalDate.now().short().uppercase()}", style = Type.label.copy(letterSpacing = 0.96.sp), color = c.inkQuiet)
                    LabeledField(
                        "What happened?", draft.whatHappened, { onDraft(draft.copy(whatHappened = it)) },
                        singleLine = false, minHeight = 92.dp, labelSize = 16.sp, fieldColor = c.bg,
                        textStyle = Type.body.copy(lineHeight = 22.sp),
                    )
                    Question("Compared to what you expected") {
                        Segmented(Compared.entries, draft.compared, { it.label }, { onDraft(draft.copy(compared = it)) })
                    }
                    Question("Did your prediction come true?") {
                        Segmented(CameTrue.entries, draft.cameTrue, { it.label }, { onDraft(draft.copy(cameTrue = it)) })
                    }
                    Question("Would you choose the same again?") {
                        Segmented(SameAgain.entries, draft.sameAgain, { it.label }, { onDraft(draft.copy(sameAgain = it)) })
                    }
                    LabeledField(
                        "One line for next time", draft.lesson, { onDraft(draft.copy(lesson = it)) },
                        suffix = "· optional", placeholder = "e.g. get it checked by my own mechanic", labelSize = 16.sp, fieldColor = c.bg,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        PrimaryButton("See verdict", onClick = onNext, modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp))
    }
}

@Composable
private fun Question(q: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(q, style = Type.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold), color = Deciary.colors.ink)
        content()
    }
}

/** The four squares: sound or shaky thinking, against how it went. */
@Composable
fun VerdictScreen(suggested: Verdict, worse: Boolean, onBack: () -> Unit, onStamp: (Verdict) -> Unit) {
    val c = Deciary.colors
    var pick by rememberSaveable { mutableStateOf(suggested) }
    Column(Modifier.fillMaxSize().background(c.bg).navigationBarsPadding()) {
        TopBar(onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Kicker("The verdict")
                val head = Type.display.copy(fontSize = 34.sp, lineHeight = 36.sp)
                // Playfulness switches off when things went worse: plain heading, no flourish.
                if (worse) Text("${pick.headA} ${pick.headB}", style = head, color = c.ink)
                else TwoToneTitle(pick.headA, pick.headB, style = head, accent = c.wax)
            }
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("It went well", "It went badly").forEach {
                        Text(it, style = Type.small.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = c.inkQuiet, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
                listOf(listOf(Verdict.EarnedIt, Verdict.BadLuck), listOf(Verdict.LuckyBreak, Verdict.Lesson)).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { v -> VerdictCell(v, v == pick, Modifier.weight(1f)) { pick = v } }
                    }
                }
            }
            Text(pick.body, style = Type.body.copy(lineHeight = 22.sp), color = c.ink, modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp))
            Text("Suggested from your answers. Tap another square to change it.", style = Type.small.copy(fontSize = 12.sp), color = c.inkQuiet, modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp))
            Spacer(Modifier.height(20.dp))
        }
        PrimaryButton("Stamp it: ${pick.label}", onClick = { onStamp(pick) }, modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp))
    }
}

@Composable
private fun VerdictCell(v: Verdict, on: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = Deciary.colors
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier
            .height(158.dp)
            .then(if (on) Modifier.shadow(14.dp, shape, ambientColor = c.wax, spotColor = c.wax) else Modifier)
            .clip(shape)
            .background(if (on) c.surface else if (c.isDark) c.tint else Color(0xFFEEEAF7))
            .then(if (on) Modifier.border(2.dp, c.wax, shape) else Modifier)
            .semantics { selected = on }
            .clickable(role = Role.RadioButton, onClick = onClick),
    ) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Art(v.art(), 64.dp, Modifier.alpha(if (on) 1f else 0.55f))
            Text(v.label, style = Type.heading.copy(fontSize = 18.sp, fontWeight = if (on) FontWeight.ExtraBold else FontWeight.Medium), color = if (on) c.ink else c.inkQuiet)
            Text(v.sub, style = Type.small.copy(fontSize = 11.sp, lineHeight = 14.sp), color = c.inkQuiet)
        }
        if (on) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(10.dp).size(28.dp).clip(CircleShape).background(c.wax),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
        }
    }
}
