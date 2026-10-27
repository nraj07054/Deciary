package app.deciary.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.runtime.remember
import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.deciary.data.Letter
import app.deciary.data.Prefs
import app.deciary.ui.components.DiaryCover
import app.deciary.ui.components.EnvelopeArt
import app.deciary.ui.components.HourglassArt
import app.deciary.ui.components.IncomingEnvelopeArt
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.PenArt
import app.deciary.ui.components.SeedlingArt
import app.deciary.ui.components.TwoToneTitle
import app.deciary.ui.daysAgo
import app.deciary.ui.long
import app.deciary.ui.short
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Fraunces
import app.deciary.ui.theme.Type
import app.deciary.ui.theme.color
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    prefs: Prefs,
    letters: List<Letter>,
    onNew: () -> Unit,
    onOpen: (String) -> Unit,
    onShelf: () -> Unit,
    onSettings: () -> Unit,
    onData: () -> Unit,
    onLock: () -> Unit,
    onReminders: (Boolean) -> Unit = {},
) {
    val c = Deciary.colors
    val today = LocalDate.now()
    var blurred by rememberSaveable { mutableStateOf(false) }
    val due = letters.filter { it.isDue(today) }.sortedBy { it.opensOn }
    val inPost = letters.filter { !it.isOpened && !it.isDue(today) }.sortedBy { it.opensOn }
    val empty = letters.isEmpty()

    Box(Modifier.fillMaxSize().background(c.bg)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().navigationBarsPadding().padding(bottom = 104.dp)) {
            if (empty) {
                Row(Modifier.fillMaxWidth().height(60.dp).padding(start = 20.dp, end = 12.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(today.format(DateTimeFormatter.ofPattern("EEE, d MMM")), style = Type.small.copy(fontWeight = FontWeight.SemiBold), color = c.inkQuiet, modifier = Modifier.weight(1f))
                    if (prefs.lockOn) RoundIcon(Icons.Outlined.Lock, "Lock now", onLock)
                    Spacer(Modifier.width(6.dp))
                    RoundIcon(Icons.Outlined.Tune, "Settings", onSettings)
                }
            } else {
                Row(Modifier.fillMaxWidth().height(56.dp).padding(start = 20.dp, end = 8.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(today.long(), style = Type.small.copy(fontWeight = FontWeight.SemiBold), color = c.inkQuiet, modifier = Modifier.weight(1f))
                    PlainIcon(if (blurred) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, if (blurred) "Show titles" else "Blur titles", on = blurred) { blurred = !blurred }
                    if (prefs.lockOn) PlainIcon(Icons.Outlined.Lock, "Lock now", onClick = onLock)
                    PlainIcon(Icons.Outlined.Search, "Search letters", onClick = onShelf)
                    PlainIcon(Icons.Outlined.Tune, "Settings", onClick = onSettings)
                }
            }
            DiaryCover(prefs.coverName, prefs.leather, Modifier.padding(start = 16.dp, end = 16.dp, top = if (empty) 8.dp else 6.dp), onLock = if (prefs.lockOn) onLock else null)

            if (empty) {
                EmptyCard()
            } else {
                if (!prefs.reminders) {
                    Row(
                        Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp).fillMaxWidth().heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(16.dp)).background(c.surface).border(1.5.dp, c.line, RoundedCornerShape(16.dp))
                            .clickable(role = Role.Button) { onReminders(true) }.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.NotificationsOff, null, tint = c.inkQuiet, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Reminders are off", style = Type.body.copy(fontSize = 14.sp), color = c.ink, modifier = Modifier.weight(1f))
                        Text("Turn on", style = Type.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = c.accentText)
                    }
                }
                if (due.isNotEmpty()) {
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = if (prefs.reminders) 24.dp else 22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Kicker(if (due.size == 1) "Due today" else "Waiting for you", Modifier.padding(horizontal = 4.dp))
                        DueCard(due, blurred, onOpen = { onOpen(due.first().id) })
                        if (due.size > 1) {
                            Text(
                                "No rush. Opening one a day works well, and they'll keep until you're ready.",
                                style = Type.small.copy(lineHeight = 19.sp), color = c.inkQuiet, modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }
                    }
                }
                // With letters waiting and nothing else in the post, the design leaves this section out.
                if (inPost.isNotEmpty() || due.isEmpty()) Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 26.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Kicker("In the post", Modifier.weight(1f))
                        Text(
                            "See all",
                            style = Type.small.copy(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline),
                            color = c.accentText,
                            modifier = Modifier.heightIn(min = 44.dp).clickable(role = Role.Button, onClick = onShelf).padding(vertical = 13.dp),
                        )
                    }
                    Column(
                        Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x661D1A33), spotColor = Color(0x661D1A33))
                            .clip(RoundedCornerShape(22.dp)).background(c.surface).padding(horizontal = 16.dp),
                    ) {
                        if (inPost.isEmpty()) {
                            Text("Nothing waiting. Every letter has been opened.", style = Type.body, color = c.inkQuiet, modifier = Modifier.padding(vertical = 18.dp))
                        }
                        inPost.take(3).forEachIndexed { i, l ->
                            if (i > 0) HorizontalDivider(color = c.line)
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = 60.dp).clickable { onOpen(l.id) },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.size(10.dp).clip(CircleShape).background(l.mood?.color() ?: c.line))
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f).padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    BlurText(l.title, blurred, Type.body.copy(fontWeight = FontWeight.SemiBold), maxLines = 1)
                                    Text(
                                        listOfNotNull(l.mood?.label, "Opens ${l.opensDate.short(today)}").joinToString(" · "),
                                        style = Type.small.copy(fontSize = 12.sp), color = c.inkQuiet,
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        buildAnnotatedString {
                            append(if (prefs.lastBackupAt == 0L) "No backup yet · " else "Last backup ${daysAgo(prefs.lastBackupAt, today)} · ")
                            withStyle(SpanStyle(color = c.accentText, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) { append("Back up") }
                        },
                        style = Type.small.copy(fontSize = 12.sp),
                        color = c.inkQuiet,
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp).clickable(role = Role.Button, onClick = onData),
                    )
                }
            }
        }
        // The main action, pinned to the foot of the screen.
        val main = if (empty) c.accent else c.ink
        Row(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 24.dp).fillMaxWidth().height(60.dp)
                .shadow(16.dp, CircleShape, ambientColor = main, spotColor = main).clip(CircleShape).background(main)
                .clickable(role = Role.Button, onClick = onNew),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Add, null, tint = if (empty) c.onAccent else c.bg, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(if (empty) "Write your first letter" else "New decision", style = Type.button.copy(fontSize = 17.sp), color = if (empty) c.onAccent else c.bg)
        }
    }
}

@Composable
private fun PlainIcon(icon: ImageVector, label: String, on: Boolean = false, onClick: () -> Unit) {
    val c = Deciary.colors
    Box(
        Modifier.size(44.dp).clip(CircleShape).background(if (on) c.tint else Color.Transparent).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = c.ink, modifier = Modifier.size(22.dp)) }
}

@Composable
private fun RoundIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    val c = Deciary.colors
    Box(
        Modifier.size(44.dp).shadow(8.dp, CircleShape, ambientColor = Color(0x4D1D1A33), spotColor = Color(0x4D1D1A33)).clip(CircleShape).background(c.surface)
            .clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = c.ink, modifier = Modifier.size(20.dp)) }
}

/**
 * A title that can be frosted over for privacy. The words melt into a soft violet haze, animating both
 * ways; Android 11 and older can't blur, so they get soft frosted bars over each line instead.
 */
@Composable
private fun BlurText(
    text: String,
    blurred: Boolean,
    style: androidx.compose.ui.text.TextStyle,
    maxLines: Int = 2,
    color: Color = Deciary.colors.ink,
) {
    val c = Deciary.colors
    val radius by animateDpAsState(if (blurred) 9.dp else 0.dp, tween(360), label = "blur")
    val ink by animateColorAsState(if (blurred) c.accent.copy(alpha = 0.6f) else color, tween(360), label = "haze")
    val bars = blurred && Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val frost = Brush.horizontalGradient(listOf(c.accent.copy(alpha = 0.28f), c.accent.copy(alpha = 0.12f)))
    Text(
        text,
        style = style,
        color = ink,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { layout = it },
        modifier = Modifier
            .then(if (radius > 0.dp) Modifier.blur(radius, BlurredEdgeTreatment.Unbounded) else Modifier)
            .then(
                if (bars) {
                    Modifier.drawWithContent {
                        val l = layout ?: return@drawWithContent
                        for (line in 0 until l.lineCount) {
                            val top = l.getLineTop(line)
                            val h = l.getLineBottom(line) - top
                            drawRoundRect(
                                frost,
                                topLeft = Offset(l.getLineLeft(line), top + h * 0.18f),
                                size = Size(l.getLineRight(line) - l.getLineLeft(line), h * 0.64f),
                                cornerRadius = CornerRadius(h * 0.32f),
                            )
                        }
                    }
                } else {
                    Modifier
                },
            ),
    )
}

@Composable
private fun DueCard(due: List<Letter>, blurred: Boolean, onOpen: () -> Unit) {
    val c = Deciary.colors
    val first = due.first()
    val many = due.size > 1
    Box(Modifier.padding(top = if (many) 14.dp else 0.dp)) {
        if (many) {
            // Two more letters peeking out behind the card.
            Box(Modifier.padding(horizontal = 28.dp).offset(y = (-14).dp).fillMaxWidth().height(40.dp).clip(RoundedCornerShape(22.dp)).background(Color(0xFFDCD3F5)))
            Box(Modifier.padding(horizontal = 14.dp).offset(y = (-7).dp).fillMaxWidth().height(40.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFFE9E3F5)))
        }
        val shape = RoundedCornerShape(26.dp)
        Column(
            Modifier.fillMaxWidth().shadow(14.dp, shape, ambientColor = c.accent, spotColor = c.accent).clip(shape).background(c.surface)
                .border(2.dp, c.accent, shape).clickable(role = Role.Button, onClick = onOpen).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    Box(Modifier.size(60.dp).clip(RoundedCornerShape(18.dp)).background(c.tint), contentAlignment = Alignment.Center) { IncomingEnvelopeArt(46.dp) }
                    if (many) {
                        Box(
                            Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-8).dp).size(26.dp).border(3.dp, c.surface, CircleShape).padding(3.dp).clip(CircleShape).background(c.ink),
                            contentAlignment = Alignment.Center,
                        ) { Text("${due.size}", style = Type.small.copy(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp), color = c.bg) }
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val heading = Type.heading.copy(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 25.sp)
                    if (many) {
                        Text("${due.size} letters have arrived", style = heading, color = c.ink)
                        BlurText("Oldest first: ${first.title}", blurred, Type.small, color = c.inkQuiet)
                    } else {
                        Text("A letter from past you", style = Type.small, color = c.inkQuiet)
                        BlurText(first.title, blurred, heading, maxLines = 3)
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(48.dp).clip(CircleShape).background(c.accent), contentAlignment = Alignment.Center) {
                Text(if (many) "Open the first one" else "Open letter", style = Type.button.copy(fontSize = 15.sp), color = c.onAccent)
            }
        }
    }
}

@Composable
private fun EmptyCard() {
    val c = Deciary.colors
    val shape = RoundedCornerShape(32.dp)
    Column(
        Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp).fillMaxWidth()
            .shadow(16.dp, shape, ambientColor = Color(0x661D1A33), spotColor = Color(0x661D1A33)).clip(shape).background(c.surface)
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(200.dp, 140.dp)) {
            Box(Modifier.offset(20.dp, 8.dp).size(160.dp, 126.dp).clip(RoundedCornerShape(63.dp)).background(c.tint))
            EnvelopeArt(124.dp, Modifier.offset(36.dp, 26.dp).rotate(-8f), heart = true)
            SeedlingArt(60.dp, Modifier.align(Alignment.TopEnd).offset(x = 0.dp, y = 68.dp))
        }
        Spacer(Modifier.height(14.dp))
        TwoToneTitle("No letters", "yet", style = Type.title)
        Spacer(Modifier.height(6.dp))
        Text("Your first one takes under a minute.", style = Type.body.copy(lineHeight = 22.sp), color = c.inkQuiet, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Step("1 · Write it", Modifier.weight(1f)) { PenArt(40.dp) }
            Step("2 · Seal it", Modifier.weight(1f)) { Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { EnvelopeArt(38.dp) } }
            Step("3 · Open later", Modifier.weight(1f)) { HourglassArt(40.dp) }
        }
    }
}

@Composable
private fun Step(label: String, modifier: Modifier, art: @Composable () -> Unit) {
    val c = Deciary.colors
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(c.bg).padding(horizontal = 6.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        art()
        Text(label, style = Type.small.copy(fontWeight = FontWeight.Bold), color = c.ink, maxLines = 2, textAlign = TextAlign.Center)
    }
}
