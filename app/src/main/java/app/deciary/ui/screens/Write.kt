package app.deciary.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import app.deciary.ui.components.Card
import app.deciary.ui.components.Chip
import app.deciary.ui.components.Field
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.PrimaryButton
import app.deciary.ui.components.QuietButton
import app.deciary.ui.components.SecondaryButton
import app.deciary.ui.components.TopBar
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
    Column(Modifier.fillMaxSize().background(c.bg).imePadding().navigationBarsPadding()) {
        TopBar(onBack = onClose, backIcon = Icons.Rounded.Close, backLabel = "Close") {
            if (step > 0) QuietButton("Skip the rest", onClick = onSeal, color = c.inkQuiet)
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(STEPS) { n ->
                Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(if (n <= step) c.accent else c.line))
            }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            when (step) {
                0 -> StepDecision(draft, onDraft)
                1 -> StepPrediction(draft, onDraft)
                2 -> StepWhen(draft, onDraft)
                else -> StepSure(draft, onDraft)
            }
        }
        Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            when (step) {
                0 -> PrimaryButton("Next", onClick = { onStep(1) }, enabled = draft.question.isNotBlank() && draft.choice.isNotBlank())
                1, 2 -> PrimaryButton("Next", onClick = { onStep(step + 1) })
                else -> {
                    PrimaryButton("Seal it", onClick = onSeal)
                    Spacer(Modifier.height(10.dp))
                    SecondaryButton("Challenge me first", onClick = onChallenge)
                }
            }
        }
    }
}

@Composable
private fun StepTitle(title: String, sub: String) {
    Text(title, style = Type.title, color = Deciary.colors.ink)
    Spacer(Modifier.height(6.dp))
    Text(sub, style = Type.body, color = Deciary.colors.inkQuiet)
    Spacer(Modifier.height(22.dp))
}

@Composable
private fun StepDecision(d: Draft, onDraft: (Draft) -> Unit) {
    StepTitle("What are you deciding?", "Just the question and your choice. The rest can wait.")
    Field(d.question, { onDraft(d.copy(question = it)) }, "The decision", placeholder = "Accept the Pune job offer?", minLines = 2)
    Spacer(Modifier.height(16.dp))
    Field(d.choice, { onDraft(d.copy(choice = it)) }, "What you chose", placeholder = "Yes, I'm taking it", minLines = 2)
}

@Composable
private fun StepPrediction(d: Draft, onDraft: (Draft) -> Unit) {
    StepTitle("What do you expect?", "Write it to future you. Be specific, so you can tell later if it came true.")
    Field(
        d.prediction, { onDraft(d.copy(prediction = it)) }, "Your prediction",
        placeholder = "By April I'll be settled in, and glad I moved.", minLines = 3, letterStyle = true,
    )
    Spacer(Modifier.height(16.dp))
    Field(d.why, { onDraft(d.copy(why = it)) }, "Why you chose it · optional", minLines = 3)
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun StepWhen(d: Draft, onDraft: (Draft) -> Unit) {
    val c = Deciary.colors
    val today = LocalDate.now()
    StepTitle("How do you feel, and when should it open?", "Both optional. Feelings change how we decide, so it helps to note them.")
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

private val notches = (50..100 step 5).toList()

@Composable
private fun StepSure(d: Draft, onDraft: (Draft) -> Unit) {
    val c = Deciary.colors
    val conf = d.confidence ?: 70
    StepTitle("How sure are you?", "About your prediction. Once it's sealed, this is locked.")
    if (d.prediction.isNotBlank()) {
        Card(color = c.tint, padding = 18.dp) {
            Kicker("You predicted")
            Spacer(Modifier.height(6.dp))
            Text(d.prediction, style = Type.letter, color = c.ink)
        }
        Spacer(Modifier.height(24.dp))
    }
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            "$conf",
            style = Type.display.copy(fontSize = 72.sp, lineHeight = 72.sp),
            color = c.ink,
            modifier = Modifier.rotate(if (conf >= 95) -2f else 0f),
        )
        Text("%", style = Type.title, color = c.ink, modifier = Modifier.padding(bottom = 10.dp))
        Spacer(Modifier.width(14.dp))
        Text(confidenceWord(conf), style = Type.heading.copy(fontWeight = FontWeight.Normal), color = c.accentText, modifier = Modifier.padding(bottom = 14.dp))
    }
    Spacer(Modifier.height(14.dp))
    Row(Modifier.fillMaxWidth().height(70.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        notches.forEachIndexed { i, v ->
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .semantics { contentDescription = "$v percent, ${confidenceWord(v)}" }
                    .clickable { onDraft(d.copy(confidence = v)) },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height((18 + i * 4.6f).dp)
                        .clip(RoundedCornerShape(5.dp))
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
    Slider(
        value = conf.toFloat(),
        onValueChange = { onDraft(d.copy(confidence = (Math.round(it / 5f) * 5).coerceIn(50, 100))) },
        valueRange = 50f..100f,
        steps = 9,
        colors = SliderDefaults.colors(thumbColor = c.ink, activeTrackColor = c.accent, inactiveTrackColor = c.line),
        modifier = Modifier.semantics { contentDescription = "How sure are you" },
    )
    Row(Modifier.fillMaxWidth()) {
        Text("Hunch", style = Type.small, color = c.inkQuiet, modifier = Modifier.weight(1f))
        Text("Certain", style = Type.small, color = c.inkQuiet)
    }
    Spacer(Modifier.height(18.dp))
    Text(
        when {
            conf >= 95 -> "Bold call. How sure are you, really?"
            conf <= 60 -> "A hunch counts. Future you will see this."
            else -> "No wrong answer. Just be honest."
        },
        style = Type.body, color = c.inkQuiet,
    )
    if (d.confidence == null) {
        // Record the default the moment the step is seen, so sealing keeps what is on screen.
        androidx.compose.runtime.LaunchedEffect(Unit) { onDraft(d.copy(confidence = conf)) }
    }
}
