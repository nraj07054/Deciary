package app.deciary.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import app.deciary.Biometrics
import app.deciary.R
import app.deciary.ui.components.EnvelopeArt
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.NotebookArt
import app.deciary.ui.components.PadlockArt
import app.deciary.ui.components.PrimaryButton
import app.deciary.ui.components.QuietButton
import app.deciary.ui.components.SecondaryButton
import app.deciary.ui.components.ShieldArt
import app.deciary.ui.components.SparklesArt
import app.deciary.ui.components.StampArt
import app.deciary.ui.components.TwoToneTitle
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Fraunces
import app.deciary.ui.theme.Type
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

/** The stage in the design is 358 × 392; its art is placed on that grid and scaled to the real panel. */
private const val StageW = 358f
private const val StageH = 392f

/** Three slides you can swipe or step through. The last one offers a PIN; [onDone] gets whether the person asked for one, and fingerprint too. */
@Composable
fun WelcomeScreen(onDone: (wantsPin: Boolean, wantsBiometric: Boolean) -> Unit) {
    val pager = rememberPagerState { slides.size }
    val scope = rememberCoroutineScope()
    val c = Deciary.colors
    val context = LocalContext.current
    val page = pager.currentPage
    Column(Modifier.fillMaxSize().background(c.bg).statusBarsPadding().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp)) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Logo()
            Spacer(Modifier.width(12.dp))
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                slides.indices.forEach { n ->
                    Box(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .semantics { contentDescription = "Go to slide ${n + 1}" }
                            .clickable { scope.launch { pager.animateScrollToPage(n) } },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(if (n <= page) c.ink else c.line))
                    }
                }
            }
            Spacer(Modifier.width(6.dp))
            QuietButton("Skip", onClick = { onDone(false, false) }, color = c.ink)
        }
        HorizontalPager(pager, Modifier.weight(1f), verticalAlignment = Alignment.Top) { n ->
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val stageH = min(maxWidth * (StageH / StageW), maxHeight * 0.62f)
                Column {
                    Spacer(Modifier.height(6.dp))
                    Stage(n, stageH)
                    Column(Modifier.padding(start = 6.dp, end = 6.dp, top = 22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Kicker(slides[n].kicker, color = c.accentText)
                        TwoToneTitle(slides[n].a, slides[n].b)
                        Text(slides[n].body, style = Type.lead, color = c.inkQuiet)
                    }
                }
            }
        }
        if (page < slides.lastIndex) {
            PrimaryButton("Continue", onClick = { scope.launch { pager.animateScrollToPage(page + 1) } }, trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryButton("Set a PIN", onClick = { onDone(true, false) }, modifier = Modifier.weight(1f))
                if (Biometrics.available(context)) {
                    SecondaryButton("Fingerprint", onClick = { onDone(true, true) }, icon = Icons.Rounded.Fingerprint, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
            QuietButton("Not now", onClick = { onDone(false, false) }, color = c.ink, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

/** The app icon, drawn from the launcher layers. */
@Composable
private fun Logo() {
    Box(Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(colorResource(R.color.icon_bg)), contentAlignment = Alignment.Center) {
        Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = "Deciary", modifier = Modifier.requiredSize(46.dp))
    }
}

@Composable
private fun Stage(n: Int, height: Dp) {
    val c = Deciary.colors
    val s = slides[n]
    val bg = if (c.isDark) s.darkStage else s.stage
    BoxWithConstraints(Modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(36.dp)).background(bg)) {
        when (n) {
            0 -> WelcomeArt(bg)
            1 -> OfflineArt()
            else -> LockArt()
        }
    }
}

/** Places a piece at the design's (x, y) on the 358 × 392 stage, scaled to this panel. */
private fun BoxWithConstraintsScope.place(x: Float, y: Float): Modifier =
    Modifier.offset(x = maxWidth * (x / StageW), y = maxHeight * (y / StageH))

/** How much bigger this panel is than the design's stage. */
private fun BoxWithConstraintsScope.k(): Float = minOf(maxWidth / StageW.dp, maxHeight / StageH.dp)

@Composable
private fun BoxWithConstraintsScope.WelcomeArt(bg: Color) {
    val k = k()
    val ring = Color(0xFFB9A6F5)
    // The dashed orbit the letter travels on.
    Box(
        place(29f, 26f).size(300.dp * k).drawBehind {
            drawCircle(ring, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))))
        },
    )
    EnvelopeArt(200.dp * k, place(79f, 104f).rotate(-8f), heart = true)
    StampArt(72.dp * k, edge = bg, modifier = place(34f, 44f).rotate(-12f))
    SparklesArt(58.dp * k, place(StageW - 40f - 58f, 50f))
    val opens = LocalDate.now().plusMonths(6).format(DateTimeFormatter.ofPattern("d MMM"))
    Chip(Modifier.align(Alignment.BottomCenter).padding(bottom = 26.dp * k).rotate(-2f)) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFC9B8FA)).border(3.dp, Color(0x1A000000), CircleShape), contentAlignment = Alignment.Center) {
            Text("D", style = Type.body.copy(fontFamily = Fraunces, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp), color = Color(0xFF1D1A33))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text("Sealed · Opens $opens", style = Type.small.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1D1A33))
            Text("Reminder on · 9:00", style = Type.small.copy(fontSize = 11.sp, lineHeight = 14.sp), color = Color(0xFF5E587A))
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.OfflineArt() {
    val k = k()
    // A violet glow behind the shield.
    Box(
        Modifier.align(Alignment.Center).offset(y = (-16).dp * k).size(300.dp * k)
            .background(Brush.radialGradient(listOf(Color(0x73A48BFF), Color(0x00A48BFF)))),
    )
    ShieldArt(172.dp * k, Modifier.align(Alignment.Center).offset(y = (-18).dp * k))
    GlassChip("No account", place(22f, 54f).rotate(-6f))
    GlassChip("No cloud", Modifier.align(Alignment.TopEnd).padding(end = 22.dp * k).offset(y = 92.dp * k).rotate(5f))
    GlassChip("No ads", place(34f, 236f).rotate(4f))
    Row(
        Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp * k).clip(RoundedCornerShape(20.dp)).background(Color(0xFFF5F2FB)).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2F9C95)))
        Spacer(Modifier.width(8.dp))
        Text("0 bytes", style = Type.body.copy(fontFamily = Fraunces, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp), color = Color(0xFF1D1A33))
        Spacer(Modifier.width(8.dp))
        Text("ever sent", style = Type.small, color = Color(0xFF5E587A))
    }
}

@Composable
private fun BoxWithConstraintsScope.LockArt() {
    val k = k()
    NotebookArt(150.dp * k, place(46f, 96f).rotate(-14f))
    PadlockArt(168.dp * k, place(StageW - 36f - 170f, 74f))
    SparklesArt(50.dp * k, place(40f, 46f))
    Chip(Modifier.align(Alignment.BottomCenter).padding(bottom = 26.dp * k).rotate(2f)) {
        repeat(4) { i ->
            Box(Modifier.size(10.dp).clip(CircleShape).background(if (i < 3) Color(0xFF1D1A33) else Color.Transparent).border(2.dp, Color(0xFF1D1A33), CircleShape))
            Spacer(Modifier.width(6.dp))
        }
        Spacer(Modifier.width(4.dp))
        Text("PIN or fingerprint", style = Type.small.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1D1A33))
    }
}

/** The white card that floats at the foot of a stage. */
@Composable
private fun Chip(modifier: Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier.shadow(12.dp, shape, ambientColor = Color(0x731D1A33), spotColor = Color(0x731D1A33)).clip(shape).background(Color.White)
            .padding(start = 10.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

/** A frosted label on the dark stage. */
@Composable
private fun GlassChip(text: String, modifier: Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Text(
        text,
        style = Type.small.copy(fontWeight = FontWeight.Bold),
        color = Color(0xFFF5F2FB),
        modifier = modifier.clip(shape).background(Color(0x1FF5F2FB)).border(1.dp, Color(0x38F5F2FB), shape).padding(horizontal = 14.dp, vertical = 8.dp),
    )
}
