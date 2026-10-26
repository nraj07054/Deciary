package app.deciary.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.deciary.R
import app.deciary.data.Letter
import app.deciary.data.Prefs
import app.deciary.data.Safety
import app.deciary.reminders.Reminders
import app.deciary.ui.components.Art
import app.deciary.ui.components.Card
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.PrimaryButton
import app.deciary.ui.components.QuietButton
import app.deciary.ui.components.SecondaryButton
import app.deciary.ui.components.TopBar
import app.deciary.ui.components.WaxSeal
import app.deciary.ui.components.initialOf
import app.deciary.ui.friendly
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Type

private data class Challenge(val art: Int, val kind: String, val line: String)

private fun challengesFor(confidence: Int?): List<Challenge> = listOf(
    Challenge(R.drawable.art_balance_scale, "The other side", "What would make the other option the right call?"),
    Challenge(
        R.drawable.art_eye, "Blind spot",
        if (confidence != null && confidence > 55) {
            "You said $confidence%. What would you need to see to drop to ${(confidence - 25).coerceAtLeast(50)}%?"
        } else {
            "What could you be missing here? Who would see it differently?"
        },
    ),
    Challenge(R.drawable.art_hourglass_done, "Past you", "How did your last big move like this feel six months in?"),
)

/** Three gentle questions before sealing. None of them stop you saving. */
@Composable
fun ChallengeScreen(confidence: Int?, onBack: () -> Unit, onEdit: () -> Unit, onSeal: () -> Unit) {
    val c = Deciary.colors
    val cards = remember(confidence) { challengesFor(confidence) }
    var i by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().background(c.bg).navigationBarsPadding()) {
        TopBar(onBack = { if (i > 0) i-- else onBack() }) {
            Text("Question ${i + 1} of 3", style = Type.bodyStrong, color = c.inkQuiet, modifier = Modifier.padding(end = 16.dp))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Text("Before you seal it", style = Type.title, color = c.ink)
            Spacer(Modifier.height(6.dp))
            Text("Three quick questions. None of them stop you saving.", style = Type.body, color = c.inkQuiet)
            Spacer(Modifier.height(24.dp))
            AnimatedContent(
                i,
                transitionSpec = { (slideInHorizontally { it / 3 } + fadeIn()) togetherWith fadeOut() },
                label = "challenge",
            ) { n ->
                val card = cards[n]
                Card(Modifier.fillMaxWidth(), padding = 24.dp) {
                    Art(card.art, 88.dp)
                    Spacer(Modifier.height(18.dp))
                    Kicker(card.kind, color = c.accentText)
                    Spacer(Modifier.height(8.dp))
                    Text(card.line, style = Type.title.copy(fontSize = Type.heading.fontSize * 1.15f), color = c.ink)
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.align(Alignment.CenterHorizontally), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { n ->
                    Box(Modifier.width(if (n == i) 22.dp else 8.dp).height(8.dp).clip(CircleShape).background(if (n == i) c.accent else c.line))
                }
            }
            Spacer(Modifier.height(18.dp))
        }
        Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            SecondaryButton("This changes things · edit my letter", onClick = onEdit, icon = Icons.Rounded.Edit)
            Spacer(Modifier.height(10.dp))
            if (i < 2) {
                PrimaryButton("Noted · next question", onClick = { i++ })
            } else {
                PrimaryButton("I've thought about this · Seal it", onClick = onSeal)
            }
        }
    }
}

/**
 * Shown when a letter's words suggest someone may be in crisis. Calm, plain, no playfulness.
 * Saving still works; challenge questions are switched off for this letter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetySheet(onSave: () -> Unit, onBack: () -> Unit) {
    val c = Deciary.colors
    val context = LocalContext.current
    ModalBottomSheet(onDismissRequest = onBack, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = c.surface) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Text("It sounds like a lot is going on right now.", style = Type.title, color = c.ink)
            Spacer(Modifier.height(12.dp))
            Text(
                "If you are thinking about hurting yourself, please talk to someone today. " +
                    "${Safety.HELPLINE_NAME} is a free government helpline, open 24 hours.",
                style = Type.body, color = c.ink,
            )
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                "Call ${Safety.HELPLINE_NAME} · ${Safety.HELPLINE_NUMBER}",
                onClick = {
                    // Opens the dialler with the number filled in; nothing is called without the person pressing call.
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Safety.HELPLINE_NUMBER}")))
                },
                icon = Icons.Rounded.Call,
            )
            Text(
                "Free · Hindi, English and regional languages",
                style = Type.small, color = c.inkQuiet,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "Your letter is safe either way. Challenge questions are switched off for this one.",
                style = Type.body, color = c.inkQuiet,
            )
            Spacer(Modifier.height(16.dp))
            SecondaryButton("Save without challenges", onClick = onSave)
            QuietButton("Back to my letter", onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun SealedScreen(letter: Letter?, prefs: Prefs, onDone: () -> Unit) {
    val c = Deciary.colors
    val context = LocalContext.current
    val drop = remember { Animatable(2.2f) }
    LaunchedEffect(Unit) { drop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }

    // The first sealed letter is a good moment to ask for notifications: the reason is obvious.
    val askNotify = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (prefs.reminders && Build.VERSION.SDK_INT >= 33 && !Reminders.canNotify(context)) {
            askNotify.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        Modifier.fillMaxSize().background(c.bg).navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Box(contentAlignment = Alignment.Center) {
            Art(R.drawable.art_envelope, 200.dp)
            WaxSeal(initialOf(prefs.ownerName.ifBlank { "Deciary" }), 64.dp, Modifier.padding(top = 46.dp).scale(drop.value))
        }
        Spacer(Modifier.height(28.dp))
        Text("Sealed.", style = Type.display, color = c.ink)
        Spacer(Modifier.height(8.dp))
        val who = prefs.ownerName.trim()
        Text(
            letter?.let { "See you on ${it.opensDate.friendly()}" + if (who.isNotEmpty()) ", $who." else "." } ?: "See you then.",
            style = Type.heading.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal),
            color = c.accentText,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            (if (prefs.reminders) "Reminder on" else "Reminders off") + " · Typo fix allowed for 10 minutes",
            style = Type.small, color = c.inkQuiet, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1.2f))
        PrimaryButton("Done", onClick = onDone)
    }
}
