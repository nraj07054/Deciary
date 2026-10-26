package app.deciary.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.deciary.Biometrics
import app.deciary.R
import app.deciary.ui.components.Art
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.PrimaryButton
import app.deciary.ui.components.QuietButton
import app.deciary.ui.components.SecondaryButton
import app.deciary.ui.components.TwoToneTitle
import app.deciary.ui.components.WaxSeal
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Type

private data class Slide(val kicker: String, val a: String, val b: String, val body: String, val stage: Color, val darkStage: Color)

private val slides = listOf(
    Slide(
        "Welcome to Deciary", "Letters to", "future you",
        "Write down a decision and what you expect. Seal it. Open it later and see how it really went.",
        Color(0xFFEDE7FD), Color(0xFF2C2547),
    ),
    Slide(
        "Offline by design", "It never leaves", "your phone",
        "Every letter is stored only in this app, on this phone. Nothing is sent anywhere, ever.",
        Color(0xFF1D1A33), Color(0xFF0C0A1A),
    ),
    Slide(
        "Optional", "Lock it,", "if you like",
        "Add a PIN or fingerprint now, or skip and turn it on later in Settings.",
        Color(0xFFFCE4EE), Color(0xFF3A2236),
    ),
)

/** Three slides. The last one offers a PIN; [onDone] gets whether the person asked for one, and fingerprint too. */
@Composable
fun WelcomeScreen(onDone: (wantsPin: Boolean, wantsBiometric: Boolean) -> Unit) {
    var i by rememberSaveable { mutableIntStateOf(0) }
    val c = Deciary.colors
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().background(c.bg).statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            slides.indices.forEach { n ->
                Box(
                    Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                        .heightIn(min = 24.dp)
                        .semantics { contentDescription = "Go to slide ${n + 1}" }
                        .clickable { i = n },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(if (n <= i) c.ink else c.line))
                }
            }
            QuietButton("Skip", onClick = { onDone(false, false) }, color = c.inkQuiet)
        }
        AnimatedContent(i, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "slide", modifier = Modifier.weight(1f)) { n ->
            val s = slides[n]
            Column(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (c.isDark) s.darkStage else s.stage),
                    contentAlignment = Alignment.Center,
                ) {
                    when (n) {
                        0 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.BottomCenter) {
                                Art(R.drawable.art_envelope, 150.dp)
                                WaxSeal("D", 48.dp, Modifier.padding(bottom = 24.dp))
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("Sealed · Opens in 3 months", style = Type.bodyStrong, color = c.ink)
                            Text("Reminder on · 9:00", style = Type.small, color = c.inkQuiet)
                        }
                        1 -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Art(R.drawable.art_mobile_phone, 110.dp)
                            listOf("No account", "No cloud", "No ads").forEach {
                                Text("✓  $it", style = Type.bodyStrong, color = Color(0xFFEEEAF8))
                            }
                            Text("0 bytes ever sent", style = Type.heading, color = Color(0xFFC9B8FA))
                        }
                        else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Art(R.drawable.art_locked_with_key, 140.dp)
                            Spacer(Modifier.height(10.dp))
                            Text("PIN or fingerprint", style = Type.bodyStrong, color = c.ink)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Kicker(s.kicker, color = c.accentText)
                Spacer(Modifier.height(8.dp))
                TwoToneTitle(s.a, s.b)
                Spacer(Modifier.height(10.dp))
                Text(s.body, style = Type.body, color = c.inkQuiet)
            }
        }
        Spacer(Modifier.height(20.dp))
        if (i < slides.lastIndex) {
            PrimaryButton("Continue", onClick = { i++ }, icon = Icons.AutoMirrored.Rounded.ArrowForward)
            Spacer(Modifier.height(60.dp))
        } else {
            val bio = Biometrics.available(context)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryButton("Set a PIN", onClick = { onDone(true, false) }, icon = Icons.Rounded.Lock, modifier = Modifier.weight(1f))
                if (bio) {
                    SecondaryButton("Fingerprint", onClick = { onDone(true, true) }, icon = Icons.Rounded.Fingerprint, modifier = Modifier.weight(1f))
                }
            }
            QuietButton("Not now", onClick = { onDone(false, false) }, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        Spacer(Modifier.height(12.dp))
    }
}

