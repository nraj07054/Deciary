package app.deciary.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.deciary.R
import app.deciary.data.Letter
import app.deciary.data.Prefs
import app.deciary.ui.components.Art
import app.deciary.ui.components.Card
import app.deciary.ui.components.DiaryCover
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.MoodTag
import app.deciary.ui.components.PrimaryButton
import app.deciary.ui.components.QuietButton
import app.deciary.ui.daysAgo
import app.deciary.ui.long
import app.deciary.ui.short
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Type
import java.time.LocalDate

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
) {
    val c = Deciary.colors
    val today = LocalDate.now()
    var blurred by rememberSaveable { mutableStateOf(false) }
    val due = letters.filter { it.isDue(today) }.sortedBy { it.opensOn }
    val inPost = letters.filter { !it.isOpened && !it.isDue(today) }.sortedBy { it.opensOn }

    Box(Modifier.fillMaxSize().background(c.bg)) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(today.long(), style = Type.bodyStrong, color = c.inkQuiet, modifier = Modifier.weight(1f))
                    IconButton(onClick = { blurred = !blurred }) {
                        Icon(
                            if (blurred) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (blurred) "Show titles" else "Blur titles",
                            tint = c.ink,
                        )
                    }
                    if (prefs.lockOn) {
                        IconButton(onClick = onLock) { Icon(Icons.Rounded.Lock, contentDescription = "Lock now", tint = c.ink) }
                    }
                    IconButton(onClick = onSettings) { Icon(Icons.Rounded.Tune, contentDescription = "Settings", tint = c.ink) }
                }
            }
            item {
                DiaryCover(prefs.coverName, prefs.leather, onSnap = if (prefs.lockOn) onLock else null)
            }
            if (due.isNotEmpty()) {
                item { DueCard(due, blurred, onOpen = { onOpen(due.first().id) }) }
            }
            if (letters.isEmpty()) {
                item { EmptyCard(onNew) }
            } else {
                item {
                    Card(padding = 0.dp) {
                        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("In the post", style = Type.heading, color = c.ink, modifier = Modifier.weight(1f))
                            QuietButton("See all", onClick = onShelf)
                        }
                        if (inPost.isEmpty()) {
                            Text(
                                "Nothing waiting. Every letter has been opened.",
                                style = Type.body, color = c.inkQuiet,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                            )
                        }
                        inPost.take(4).forEachIndexed { i, l ->
                            if (i > 0) HorizontalDivider(color = c.line, modifier = Modifier.padding(horizontal = 20.dp))
                            Row(
                                Modifier.fillMaxWidth().clickable { onOpen(l.id) }.padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Art(R.drawable.art_envelope, 36.dp)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    BlurText(l.title, blurred)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        l.mood?.let { MoodTag(it); Text("  ·  ", style = Type.small, color = c.inkQuiet) }
                                        Text("Opens ${l.opensDate.short(today)}", style = Type.small, color = c.inkQuiet)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (prefs.lastBackupAt == 0L) "No backup yet" else "Last backup ${daysAgo(prefs.lastBackupAt, today)}",
                        style = Type.small, color = c.inkQuiet, modifier = Modifier.weight(1f),
                    )
                    QuietButton("Back up", onClick = onData)
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onNew,
            icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
            text = { Text("New decision", style = Type.button) },
            containerColor = c.accent,
            contentColor = c.onAccent,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 20.dp).heightIn(min = 56.dp),
        )
    }
}

@Composable
private fun BlurText(text: String, blurred: Boolean) {
    val c = Deciary.colors
    // Blur needs Android 12; older phones get dots instead so the title still can't be read.
    val shown = if (blurred && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) "•".repeat(text.length.coerceAtMost(28)) else text
    Text(
        shown,
        style = Type.bodyStrong,
        color = c.ink,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = if (blurred) Modifier.blur(7.dp) else Modifier,
    )
}

@Composable
private fun DueCard(due: List<Letter>, blurred: Boolean, onOpen: () -> Unit) {
    val c = Deciary.colors
    val first = due.first()
    Card(color = c.tint) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Kicker(if (due.size == 1) "Due today" else "${due.size} letters ready", color = c.wax)
                Spacer(Modifier.height(4.dp))
                Text("A letter from past you", style = Type.heading, color = c.ink)
            }
            Art(R.drawable.art_incoming_envelope, 64.dp)
        }
        Spacer(Modifier.height(8.dp))
        BlurText(first.title, blurred)
        Spacer(Modifier.height(16.dp))
        PrimaryButton("Open letter", onClick = onOpen)
    }
}

@Composable
private fun EmptyCard(onNew: () -> Unit) {
    val c = Deciary.colors
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Your first letter", style = Type.heading, color = c.ink)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Pick something you're deciding this week, big or small. It takes about a minute.",
                    style = Type.body, color = c.inkQuiet,
                )
            }
            Spacer(Modifier.width(12.dp))
            Art(R.drawable.art_fountain_pen, 64.dp)
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.clip(CircleShape)) {
            QuietButton("Write it now", onClick = onNew)
        }
    }
}
