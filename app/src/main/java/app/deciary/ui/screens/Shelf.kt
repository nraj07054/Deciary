package app.deciary.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.deciary.ui.components.EnvelopeArt
import app.deciary.ui.components.MailboxArt
import app.deciary.ui.components.TwoToneTitle
import app.deciary.ui.theme.color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.deciary.data.Letter
import app.deciary.ui.components.Art
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.TopBar
import app.deciary.ui.components.art
import app.deciary.ui.monthLabel
import app.deciary.ui.short
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Type
import app.deciary.ui.toLocalDate
import java.time.LocalDate

private enum class ShelfTab(val label: String) { All("All"), Sealed("Sealed"), Opened("Opened") }

@Composable
fun ShelfScreen(letters: List<Letter>, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val c = Deciary.colors
    val today = LocalDate.now()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    // Newest activity first: when a letter was opened, or else when it was sealed.
    fun activity(l: Letter) = l.review?.openedAt ?: l.sealedAt
    val sorted = letters.sortedByDescending { activity(it) }
    val shown = when (ShelfTab.entries[tab]) {
        ShelfTab.All -> sorted
        ShelfTab.Sealed -> sorted.filter { !it.isOpened }
        ShelfTab.Opened -> sorted.filter { it.isOpened }
    }
    val counts = mapOf(
        ShelfTab.All to letters.size,
        ShelfTab.Sealed to letters.count { !it.isOpened },
        ShelfTab.Opened to letters.count { it.isOpened },
    )
    Column(Modifier.fillMaxSize().background(c.bg)) {
        TopBar(onBack = onBack)
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Row(Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        TwoToneTitle("Your", "shelf")
                        val oldest = letters.minOfOrNull { it.sealedAt }
                        Text(
                            if (letters.isEmpty()) "No letters yet"
                            else "${letters.size} letter${if (letters.size == 1) "" else "s"} since ${oldest!!.toLocalDate().monthLabel(today)}",
                            style = Type.body.copy(fontSize = 14.sp), color = c.inkQuiet,
                        )
                    }
                    MailboxArt(72.dp, Modifier.rotate(6f))
                }
                Spacer(Modifier.height(18.dp))
                // Tabs: a white tray with the chosen one filled in ink.
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(c.surface).border(1.dp, c.line, RoundedCornerShape(20.dp)).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ShelfTab.entries.forEach { t ->
                        val on = t.ordinal == tab
                        Row(
                            Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(16.dp)).background(if (on) c.ink else Color.Transparent)
                                .semantics { selected = on }.clickable(role = Role.Tab) { tab = t.ordinal },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(t.label, style = Type.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = if (on) c.bg else c.ink)
                            Spacer(Modifier.width(6.dp))
                            Box(
                                Modifier.heightIn(min = 20.dp).widthIn(min = 20.dp).clip(CircleShape).background(if (on) Color.White.copy(alpha = 0.18f) else c.line).padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) { Text("${counts[t]}", style = Type.small.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold), color = if (on) c.bg else c.ink) }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
            var lastMonth = ""
            shown.forEach { l ->
                val month = activity(l).toLocalDate().monthLabel(today)
                if (month != lastMonth) {
                    lastMonth = month
                    item(key = "m-$month-${l.id}") { Kicker(month, modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 14.dp, bottom = 2.dp)) }
                }
                item(key = l.id) { ShelfRow(l, today) { onOpen(l.id) } }
            }
            if (shown.isEmpty() && letters.isNotEmpty()) {
                item { Text("Nothing here yet.", style = Type.body, color = c.inkQuiet, modifier = Modifier.padding(top = 20.dp, start = 4.dp)) }
            }
        }
    }
}

@Composable
private fun ShelfRow(l: Letter, today: LocalDate, onClick: () -> Unit) {
    val c = Deciary.colors
    val review = l.review
    val pink = if (c.isDark) c.tint else Color(0xFFFDF0F5)
    val shape = RoundedCornerShape(22.dp)
    Row(
        Modifier.fillMaxWidth().heightIn(min = 76.dp).shadow(10.dp, shape, ambientColor = Color(0x731D1A33), spotColor = Color(0x731D1A33)).clip(shape).background(c.surface)
            .clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(if (review != null) pink else c.tint), contentAlignment = Alignment.Center) {
            if (review != null) Art(review.verdict.art(), 38.dp) else EnvelopeArt(38.dp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(l.title, style = Type.heading.copy(fontSize = 16.sp, lineHeight = 21.sp), color = c.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                l.mood?.let {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(it.color()))
                    Spacer(Modifier.width(5.dp))
                    Text(it.label, style = Type.small.copy(fontSize = 12.sp), color = c.inkQuiet)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (review != null) "Opened ${review.openedAt.toLocalDate().short(today)}" else "Opens ${l.opensDate.short(today)}",
                    style = Type.small.copy(fontSize = 12.sp), color = c.inkQuiet,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        val (pill, bg, ink) = when {
            review != null -> Triple(review.verdict.label, pink, c.wax)
            l.isDue(today) -> Triple("Due", c.wax, Color.White)
            else -> Triple("Sealed", if (c.isDark) c.tint else Color(0xFFEEEAF7), if (c.isDark) c.accentText else Color(0xFF4A3A8C))
        }
        Text(
            pill,
            style = Type.small.copy(fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.22.sp),
            color = ink,
            modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(bg).padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}
