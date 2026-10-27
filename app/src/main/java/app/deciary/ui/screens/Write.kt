package app.deciary.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import app.deciary.ui.components.CrystalBallArt
import app.deciary.ui.components.LabeledField
import app.deciary.ui.components.TwoToneTitle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.deciary.Draft
import app.deciary.data.Mood
import app.deciary.data.confidenceWord
import app.deciary.ui.components.Chip
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.PrimaryButton
import app.deciary.ui.components.QuietButton
import app.deciary.ui.friendly
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Type
import app.deciary.ui.theme.color
import app.deciary.ui.untilText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private const val STEPS = 4

/**
 * Writing a letter, in four short steps. Only the question and the choice are required;
 * from the second step on, "Skip the rest" seals it straight away.
 */
@Composable
fun WriteScreen(
    draft: Draft,
    step: Int,
    onDraft: (Draft) -> Unit,
    onStep: (Int) -> Unit,
    onClose: () -> Unit,
    onSeal: () -> Unit,
    onChallenge: () -> Unit,
) {
    val c = Deciary.colors
    BackHandler { if (step > 0) onStep(step - 1) else onClose() }
    Column(Modifier.fillMaxSize().background(c.bg).statusBarsPadding().imePadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(start = 4.dp, end = 10.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) { Icon(Icons.Rounded.Close, contentDescription = "Close", tint = c.ink, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.width(10.dp))
            // One continuous bar, filled to the current step.
            Box(
                Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(c.line)
                    .semantics { contentDescription = "Step ${step + 1} of $STEPS" },
            ) {
                Box(Modifier.fillMaxWidth((step + 1f) / STEPS).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(c.accent))
            }
            Spacer(Modifier.width(10.dp))
            if (step > 0) {
                Text(
                    "Skip the rest",
                    style = Type.small.copy(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline),
                    color = c.accentText,
                    modifier = Modifier.heightIn(min = 44.dp).clickable(role = Role.Button, onClick = onSeal).padding(vertical = 13.dp),
                )
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            when (step) {
                0 -> StepDecision(draft, onDraft)
                1 -> StepPrediction(draft, onDraft)
                2 -> StepWhen(draft, onDraft)
                else -> StepSure(draft, onDraft)
            }
            Spacer(Modifier.height(20.dp))
        }
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            when (step) {
                0 -> PrimaryButton("Next", onClick = { onStep(1) }, enabled = draft.question.isNotBlank() && draft.choice.isNotBlank())
                1, 2 -> PrimaryButton("Next", onClick = { onStep(step + 1) })
                else -> {
                    PrimaryButton("Seal it", onClick = onSeal)
                    QuietButton("Challenge me first", onClick = onChallenge, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}

@Composable
private fun StepTitle(a: String, b: String, sub: String) {
    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        TwoToneTitle(a, b, style = Type.display.copy(fontSize = 32.sp, lineHeight = 35.sp))
        Text(sub, style = Type.body, color = Deciary.colors.inkQuiet)
    }
}

@Composable
private fun StepDecision(d: Draft, onDraft: (Draft) -> Unit) {
    StepTitle("What are you", "deciding?", "Just the question and your choice. The rest can wait.")
    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        LabeledField("The decision", d.question, { onDraft(d.copy(question = it)) }, placeholder = "Accept the Pune job offer?", singleLine = false, minHeight = 84.dp)
        LabeledField("What you chose", d.choice, { onDraft(d.copy(choice = it)) }, placeholder = "Yes, I'm taking it", singleLine = false, minHeight = 84.dp)
    }
}

@Composable
private fun StepPrediction(d: Draft, onDraft: (Draft) -> Unit) {
    StepTitle("What do you", "expect?", "Write it to future you. Be specific, so you can tell later if it came true.")
    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        LabeledField(
            "Your prediction", d.prediction, { onDraft(d.copy(prediction = it)) },
            placeholder = "By April I'll be settled in, and glad I moved.", singleLine = false, minHeight = 120.dp,
            textStyle = Type.letter.copy(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 24.sp),
        )
        LabeledField("Why you chose it", d.why, { onDraft(d.copy(why = it)) }, suffix = "· optional", singleLine = false, minHeight = 110.dp)
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun StepWhen(d: Draft, onDraft: (Draft) -> Unit) {
    val c = Deciary.colors
    val today = LocalDate.now()
    StepTitle("How do you", "feel?", "And when should it open? Both optional. Feelings change how we decide, so it helps to note them.")
    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp)) {
    Kicker("Right now I feel")
    Spacer(Modifier.height(10.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Mood.entries.forEach { m ->
            Chip(m.label, d.mood == m, onClick = { onDraft(d.copy(mood = if (d.mood == m) null else m)) }, dot = m.color())
        }
    }
    Spacer(Modifier.height(26.dp))
    Kicker("Open it")
    Spacer(Modifier.height(10.dp))
    val presets = listOf(
        "In a week" to today.plusWeeks(1),
        "In a month" to today.plusMonths(1),
        "In 3 months" to today.plusMonths(3),
        "In 6 months" to today.plusMonths(6),
        "In a year" to today.plusYears(1),
    )
    var picking by remember { mutableStateOf(false) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        presets.forEach { (label, date) -> Chip(label, d.opensOn == date, onClick = { onDraft(d.copy(opensOn = date)) }) }
        Chip("Pick a date", presets.none { it.second == d.opensOn }, onClick = { picking = true })
    }
    Spacer(Modifier.height(14.dp))
    Text("Opens ${d.opensOn.friendly(today)}, ${untilText(d.opensOn, today)}.", style = Type.body, color = c.inkQuiet)

    if (picking) {
        val tomorrowUtc = today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = d.opensOn.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= tomorrowUtc
                override fun isSelectableYear(year: Int) = year >= today.year
            },
        )
        DatePickerDialog(
            onDismissRequest = { picking = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onDraft(d.copy(opensOn = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()))
                    }
                    picking = false
                }) { Text("Choose") }
            },
            dismissButton = { TextButton(onClick = { picking = false }) { Text("Cancel") } },
        ) { DatePicker(state) }
    }
    }
}

private val notches = (50..100 step 5).toList()

@Composable
private fun StepSure(d: Draft, onDraft: (Draft) -> Unit) {
    val c = Deciary.colors
    val conf = d.confidence ?: 70
    StepTitle("How sure", "are you?", "About your prediction. Once it's sealed, this is locked.")
    if (d.prediction.isNotBlank()) {
        val shape = RoundedCornerShape(22.dp)
        Row(
            Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp).fillMaxWidth()
                .shadow(10.dp, shape, ambientColor = Color(0x661D1A33), spotColor = Color(0x661D1A33)).clip(shape).background(c.surface)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CrystalBallArt(40.dp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("YOU PREDICTED", style = Type.label.copy(letterSpacing = 0.96.sp), color = c.inkQuiet)
                Text(d.prediction, style = Type.letter.copy(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 24.sp), color = c.ink)
            }
        }
    }
    Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 34.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.rotate(if (conf >= 95) -2f else 0f)) {
            Text("$conf", style = Type.display.copy(fontSize = 96.sp, lineHeight = 90.sp, letterSpacing = (-2.9).sp), color = c.ink)
            Spacer(Modifier.width(4.dp))
            Text("%", style = Type.display.copy(fontSize = 40.sp, lineHeight = 40.sp), color = c.ink, modifier = Modifier.padding(bottom = 8.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            confidenceWord(conf),
            style = Type.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
            color = if (c.isDark) c.accentText else Color(0xFF4A3A8C),
            modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(c.tint).padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
    // Notches you can tap or drag across, like the design's range slider.
    fun pick(x: Float, width: Int) {
        val i = ((x / width) * notches.size).toInt().coerceIn(0, notches.lastIndex)
        if (notches[i] != conf) onDraft(d.copy(confidence = notches[i]))
    }
    Row(
        Modifier
            .padding(start = 20.dp, end = 20.dp, top = 28.dp)
            .fillMaxWidth()
            .height(64.dp)
            .pointerInput(conf) {
                detectHorizontalDragGestures { change, _ -> pick(change.position.x, size.width) }
            }
            .semantics {
                contentDescription = "How sure are you"
                stateDescription = "$conf percent, ${confidenceWord(conf)}"
                progressBarRangeInfo = ProgressBarRangeInfo(conf.toFloat(), 50f..100f, steps = 9)
                setProgress { v -> onDraft(d.copy(confidence = (Math.round(v / 5f) * 5).coerceIn(50, 100))); true }
            },
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        notches.forEachIndexed { i, v ->
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .semantics { contentDescription = "$v percent, ${confidenceWord(v)}" }
                    .clickable { onDraft(d.copy(confidence = v)) },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height((18 + i * 4.6f).dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            when {
                                v == conf -> c.ink
                                v < conf -> c.accent
                                else -> c.line
                            },
                        ),
                )
            }
        }
    }
    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp)) {
        Text("Hunch", style = Type.small.copy(fontSize = 12.sp), color = c.inkQuiet, modifier = Modifier.weight(1f))
        Text("Certain", style = Type.small.copy(fontSize = 12.sp), color = c.inkQuiet)
    }
    Text(
        when {
            conf >= 95 -> "Bold call. How sure are you, really?"
            conf <= 60 -> "A hunch counts. Future you will see this."
            else -> "No wrong answer. Just be honest."
        },
        style = Type.body.copy(lineHeight = 22.sp), color = c.inkQuiet, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 22.dp),
    )
    if (d.confidence == null) {
        // Record the default the moment the step is seen, so sealing keeps what is on screen.
        androidx.compose.runtime.LaunchedEffect(Unit) { onDraft(d.copy(confidence = conf)) }
    }
}
