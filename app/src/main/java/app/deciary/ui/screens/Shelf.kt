package app.deciary.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import app.deciary.R
import app.deciary.data.Letter
import app.deciary.ui.components.Art
import app.deciary.ui.components.Card
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.MoodTag
import app.deciary.ui.components.Segmented
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
    val sorted = letters.sortedByDescending { it.sealedAt }
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
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text("Your shelf", style = Type.display, color = c.ink)
                val oldest = letters.minOfOrNull { it.sealedAt }
                Text(
                    when {
                        letters.isEmpty() -> "No letters yet"
                        else -> "${letters.size} letter${if (letters.size == 1) "" else "s"} since ${oldest!!.toLocalDate().monthLabel(today)}"
                    },
                    style = Type.body, color = c.inkQuiet,
                )
                Spacer(Modifier.height(16.dp))
                Segmented(ShelfTab.entries, ShelfTab.entries[tab], { "${it.label} ${counts[it]}" }, { tab = it.ordinal })
                Spacer(Modifier.height(6.dp))
            }
            var lastMonth = ""
            shown.forEach { l ->
                val month = l.sealedDate.monthLabel(today)
                if (month != lastMonth) {
                    lastMonth = month
                    item(key = "m-$month-${l.id}") { Kicker(month, modifier = Modifier.padding(top = 10.dp, start = 4.dp)) }
                }
                item(key = l.id) { ShelfRow(l, today) { onOpen(l.id) } }
            }
            if (shown.isEmpty() && letters.isNotEmpty()) {
                item { Text("Nothing here yet.", style = Type.body, color = c.inkQuiet, modifier = Modifier.padding(top = 20.dp)) }
            }
        }
    }
}

@Composable
private fun ShelfRow(l: Letter, today: LocalDate, onClick: () -> Unit) {
    val c = Deciary.colors
    val review = l.review
    Card(onClick = onClick, padding = 14.dp, corner = 20.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Art(review?.verdict?.art() ?: R.drawable.art_envelope, 44.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(l.title, style = Type.bodyStrong, color = c.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    l.mood?.let {
                        MoodTag(it)
                        Text("  ·  ", style = Type.small, color = c.inkQuiet)
                    }
                    Text(
                        if (review != null) "Opened ${review.openedAt.toLocalDate().short(today)}" else "Opens ${l.opensDate.short(today)}",
                        style = Type.small, color = c.inkQuiet,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            val (pill, bg, ink) = when {
                review != null -> Triple(review.verdict.label, c.tint, c.accentText)
                l.isDue(today) -> Triple("Due", c.wax, c.surface)
                else -> Triple("Sealed", c.line, c.inkQuiet)
            }
            Text(
                pill,
                style = Type.label,
                color = ink,
                modifier = Modifier.clip(CircleShape).background(bg).padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

