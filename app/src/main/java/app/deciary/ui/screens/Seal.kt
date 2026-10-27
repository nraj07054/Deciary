package app.deciary.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import app.deciary.ui.components.LightBarIcons
import app.deciary.ui.components.BalanceArt
import app.deciary.ui.components.EnvelopeArt
import app.deciary.ui.components.EyeArt
import app.deciary.ui.components.HourglassArt
import app.deciary.ui.components.TwoToneTitle
import app.deciary.ui.theme.Fraunces
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
import app.deciary.data.Letter
import app.deciary.data.Prefs
import app.deciary.data.Safety
import app.deciary.reminders.Reminders
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.PrimaryButton
import app.deciary.ui.components.initialOf
import app.deciary.ui.friendly
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Type

private data class Challenge(val art: @Composable (Dp) -> Unit, val kind: String, val line: String)

private fun challengesFor(confidence: Int?): List<Challenge> = listOf(
    Challenge({ BalanceArt(it) }, "The other side", "What would make the other option the right call?"),
    Challenge(
        { EyeArt(it) }, "Blind spot",
        if (confidence != null && confidence > 55) {
            "You said $confidence%. What would you need to see to drop to ${(confidence - 25).coerceAtLeast(50)}%?"
        } else {
            "What could you be missing here? Who would see it differently?"
        },
    ),
    Challenge({ HourglassArt(it) }, "Past you", "How did your last big move like this feel six months in?"),
)

/** Three gentle questions before sealing. None of them stop you saving. */
@Composable
fun ChallengeScreen(confidence: Int?, onBack: () -> Unit, onEdit: () -> Unit, onSeal: () -> Unit) {
    val c = Deciary.colors
    val cards = remember(confidence) { challengesFor(confidence) }
    var i by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().background(c.bg).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(start = 4.dp, end = 12.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (i > 0) i-- else onBack() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to your letter", tint = c.ink)
            }
            Spacer(Modifier.weight(1f))
            Text("Question ${i + 1} of 3", style = Type.small.copy(fontWeight = FontWeight.Bold), color = c.inkQuiet)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TwoToneTitle("Before you", "seal it", style = Type.display.copy(fontSize = 32.sp, lineHeight = 35.sp))
                Text("Three quick questions. None of them stop you saving.", style = Type.body, color = c.inkQuiet)
            }
            AnimatedContent(
                i,
                transitionSpec = { (slideInHorizontally { it / 3 } + fadeIn()) togetherWith fadeOut() },
                label = "challenge",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 28.dp),
            ) { n ->
                val card = cards[n]
                Box(Modifier.fillMaxWidth().heightIn(min = 380.dp)) {
                    // The next card waiting underneath.
                    Box(Modifier.matchParentSize().padding(start = 18.dp, end = 18.dp, top = 16.dp).offset(y = 14.dp).clip(RoundedCornerShape(30.dp)).background(c.line))
                    val shape = RoundedCornerShape(30.dp)
                    Column(
                        Modifier.fillMaxWidth().heightIn(min = 380.dp)
                            .shadow(18.dp, shape, ambientColor = Color(0x801D1A33), spotColor = Color(0x801D1A33)).clip(shape).background(c.surface)
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                    ) {
                        Box(Modifier.size(88.dp).clip(RoundedCornerShape(26.dp)).background(c.tint), contentAlignment = Alignment.Center) { card.art(64.dp) }
                        Spacer(Modifier.height(22.dp))
                        Kicker(card.kind, color = c.accentText)
                        Spacer(Modifier.height(8.dp))
                        Text(card.line, style = Type.title.copy(fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 33.sp), color = c.ink)
                        Spacer(Modifier.weight(1f).heightIn(min = 24.dp))
                        Row(
                            Modifier.heightIn(min = 44.dp).clickable(role = Role.Button, onClick = onEdit),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Edit, null, tint = c.ink, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("This changes things · edit my letter", style = Type.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline), color = c.ink)
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 34.dp), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
                repeat(3) { n ->
                    Box(Modifier.width(if (n == i) 22.dp else 8.dp).height(8.dp).clip(CircleShape).background(if (n == i) c.accent else Color(0xFFDCD3F5)))
                }
            }
            Spacer(Modifier.height(18.dp))
        }
        Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
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
    ModalBottomSheet(
        onDismissRequest = onBack,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = null,
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("It sounds like a lot is going on right now.", style = Type.body.copy(fontSize = 22.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold), color = c.ink)
                Text(
                    "If you are thinking about hurting yourself, please talk to someone today. " +
                        "${Safety.HELPLINE_NAME} is a free government helpline, open 24 hours.",
                    style = Type.body.copy(fontSize = 16.sp, lineHeight = 25.sp), color = c.ink,
                )
            }
            Row(
                Modifier.fillMaxWidth().heightIn(min = 64.dp).clip(RoundedCornerShape(18.dp)).border(1.5.dp, c.ink, RoundedCornerShape(18.dp))
                    .clickable(role = Role.Button) {
                        // Opens the dialler with the number filled in; nothing is called without the person pressing call.
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Safety.HELPLINE_NUMBER}")))
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(Icons.Rounded.Call, null, tint = c.ink, modifier = Modifier.size(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Call ${Safety.HELPLINE_NAME} · ${Safety.HELPLINE_NUMBER}", style = Type.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold), color = c.ink)
                    Text("Free · Hindi, English and regional languages", style = Type.small, color = c.inkQuiet)
                }
            }
            Text(
                "Your letter is safe either way. Challenge questions are switched off for this one.",
                style = Type.body.copy(fontSize = 14.sp, lineHeight = 21.sp), color = c.inkQuiet,
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PrimaryButton("Save without challenges", onClick = onSave, color = c.ink, contentColor = c.bg)
                Box(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onBack), contentAlignment = Alignment.Center) {
                    Text("Back to my letter", style = Type.body.copy(fontWeight = FontWeight.Bold), color = c.ink)
                }
            }
        }
    }
}

@Composable
fun SealedScreen(letter: Letter?, prefs: Prefs, onDone: () -> Unit) {
    val c = Deciary.colors
    val context = LocalContext.current
    val drop = remember { Animatable(2.2f) }
    LightBarIcons()
    LaunchedEffect(Unit) { drop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }

    // The first sealed letter is a good moment to ask for notifications: the reason is obvious.
    val askNotify = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (prefs.reminders && Build.VERSION.SDK_INT >= 33 && !Reminders.canNotify(context)) {
            askNotify.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Always the night colours: sealing is a quiet, final moment.
    Box(Modifier.fillMaxSize().background(Color(0xFF1D1A33))) {
        Box(
            Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 40.dp).size(420.dp)
                .background(Brush.radialGradient(listOf(Color(0x52A48BFF), Color(0x00A48BFF)))),
        )
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(280.dp, 220.dp), contentAlignment = Alignment.Center) {
                EnvelopeArt(
                    240.dp, Modifier.rotate(-5f),
                    initial = initialOf(prefs.ownerName.ifBlank { "Deciary" }),
                    sealModifier = Modifier.scale(drop.value).rotate(-12f),
                    sealSize = 64.dp,
                )
            }
            Spacer(Modifier.height(34.dp))
            Text(
                buildAnnotatedString {
                    append("Sealed")
                    withStyle(SpanStyle(color = Color(0xFFC9B8FA))) { append(".") }
                },
                style = Type.display.copy(fontSize = 60.sp, lineHeight = 60.sp, letterSpacing = (-1.2).sp),
                color = Color(0xFFF5F2FB),
            )
            Spacer(Modifier.height(10.dp))
            val who = prefs.ownerName.trim()
            Text(
                letter?.let { "See you on ${it.opensDate.friendly()}" + if (who.isNotEmpty()) ", $who." else "." } ?: "See you then.",
                style = TextStyle(fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium, fontSize = 20.sp),
                color = Color(0xFFDCD3F5),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                (if (prefs.reminders) "Reminder on" else "Reminders off") + " · Typo fix allowed for 10 minutes",
                style = Type.body.copy(fontSize = 14.sp), color = Color(0xFFCFC9E6), textAlign = TextAlign.Center,
            )
            Spacer(Modifier.weight(1f))
            PrimaryButton("Done", onClick = onDone, color = Color(0xFFF5F2FB), contentColor = Color(0xFF1D1A33))
        }
    }
}
