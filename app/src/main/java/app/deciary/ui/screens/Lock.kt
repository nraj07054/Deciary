package app.deciary.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import app.deciary.ui.components.drawCover
import app.deciary.ui.theme.Fraunces
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.deciary.Biometrics
import app.deciary.data.Leather
import app.deciary.ui.components.palette
import app.deciary.ui.theme.Type
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

const val PIN_LENGTH = 4

/** Four dots and a keypad on full-screen leather, as in the design. Shared by the lock screen and PIN setup. */
@Composable
fun PinPad(
    leather: Leather,
    title: String,
    subtitle: String,
    prompt: String,
    error: String?,
    onComplete: (String) -> Boolean,
    footer: @Composable () -> Unit = {},
) {
    val p = leather.palette()
    var pin by remember { mutableStateOf("") }
    val shake = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    fun press(d: String) {
        if (pin.length >= PIN_LENGTH) return
        pin += d
        if (pin.length == PIN_LENGTH) {
            val entered = pin
            if (!onComplete(entered)) {
                scope.launch {
                    shake.animateTo(0f, spring(dampingRatio = 0.2f, stiffness = 1200f), initialVelocity = 1500f)
                    pin = ""
                }
            } else {
                pin = ""
            }
        }
    }

    Box(Modifier.fillMaxSize().background(p.base)) {
        val strapY = with(LocalDensity.current) { (WindowInsets.statusBars.getTop(this) + 144.dp.roundToPx()).toFloat() }
        Canvas(Modifier.matchParentSize()) { drawCover(p, strap = true, mini = false, strapY = strapY) }
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(start = 24.dp, end = 24.dp, top = 96.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.fillMaxWidth().padding(start = 26.dp, end = 90.dp)) {
                Text(
                    title,
                    style = Type.display.copy(
                        fontSize = 54.sp, lineHeight = 54.sp, letterSpacing = (-1.08).sp,
                        shadow = Shadow(p.hi, offset = Offset(0f, 5f), blurRadius = 0f),
                    ),
                    color = p.ink,
                )
                Spacer(Modifier.height(8.dp))
                Text(subtitle, style = TextStyle(fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium, fontSize = 17.sp), color = p.ink.copy(alpha = 0.82f))
            }
            Spacer(Modifier.height(42.dp))
            Column(
                Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0x5907050F)).padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(prompt, style = Type.bodyStrong.copy(fontSize = 14.sp), color = p.ink)
                Row(
                    Modifier
                        .offset { IntOffset(shake.value.roundToInt(), 0) }
                        .semantics { contentDescription = "${pin.length} of $PIN_LENGTH digits entered" },
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    repeat(PIN_LENGTH) { i ->
                        val on = i < pin.length
                        Box(
                            Modifier
                                .size(if (on) 15.dp else 14.dp)
                                .clip(CircleShape)
                                .background(if (on) p.ink else Color.Transparent)
                                .border(2.dp, p.ink, CircleShape),
                        )
                    }
                }
            }
            Text(
                error ?: " ",
                style = Type.small,
                color = Color(0xFFFFC9D9),
                modifier = Modifier.padding(top = 8.dp).semantics { liveRegion = LiveRegionMode.Polite },
            )
            Spacer(Modifier.height(8.dp))
            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "del")
            Column(Modifier.width(276.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                keys.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        row.forEach { k ->
                            val m = Modifier.weight(1f).height(70.dp)
                            when (k) {
                                "" -> Spacer(m)
                                "del" -> Box(
                                    m.clip(CircleShape).semantics { contentDescription = "Delete digit" }.clickable { pin = pin.dropLast(1) },
                                    contentAlignment = Alignment.Center,
                                ) { Icon(Icons.AutoMirrored.Rounded.Backspace, null, tint = p.ink, modifier = Modifier.size(26.dp)) }
                                else -> Box(
                                    m.shadow(8.dp, CircleShape, ambientColor = Color.Black, spotColor = Color.Black)
                                        .clip(CircleShape)
                                        .background(Brush.verticalGradient(listOf(p.ink.copy(alpha = 0.16f), p.ink.copy(alpha = 0.08f))))
                                        .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.16f), Color.Transparent)), CircleShape)
                                        .clickable { press(k) },
                                    contentAlignment = Alignment.Center,
                                ) { Text(k, style = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 28.sp), color = p.ink) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            footer()
        }
    }
}

@Composable
fun LockScreen(
    name: String,
    leather: Leather,
    biometric: Boolean,
    onPin: (String) -> Boolean,
    onBiometric: () -> Unit,
) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    var tries by remember { mutableStateOf(0) }
    val canBio = biometric && Biometrics.available(context)
    LaunchedEffect(Unit) { if (canBio) Biometrics.prompt(context, onBiometric) }
    PinPad(
        leather = leather,
        title = name,
        subtitle = "Only you can open this",
        prompt = "Enter your PIN",
        error = error,
        onComplete = { pin ->
            onPin(pin).also { ok ->
                if (!ok) {
                    tries++
                    error = if (tries >= 3) "Not that one. Take your time." else "That PIN didn't match."
                }
            }
        },
        footer = {
            if (canBio) FingerprintPill { Biometrics.prompt(context, onBiometric) }
        },
    )
}

/** The light pill under the keypad that opens the fingerprint prompt. */
@Composable
fun FingerprintPill(onClick: () -> Unit) {
    Row(
        Modifier.heightIn(min = 52.dp).clip(CircleShape).background(Color(0xFFEEEAF8)).clickable(onClick = onClick).padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Fingerprint, null, tint = Color(0xFF1D1A33), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text("Use fingerprint", style = Type.bodyStrong.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1D1A33))
    }
}

/** Choose a PIN, then type it again. */
@Composable
fun PinSetupScreen(leather: Leather, onCancel: () -> Unit, onDone: (String) -> Unit) {
    var first by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val ink = leather.palette().ink
    PinPad(
        leather = leather,
        title = if (first == null) "Choose a PIN" else "Once more",
        subtitle = "You'll use it to open your diary",
        prompt = if (first == null) "Pick $PIN_LENGTH digits" else "Type the same $PIN_LENGTH digits",
        error = error,
        onComplete = { pin ->
            val f = first
            when {
                f == null -> { first = pin; error = null; true }
                f == pin -> { onDone(pin); true }
                else -> { first = null; error = "Those didn't match. Let's start again."; false }
            }
        },
        footer = {
            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Not now", style = Type.bodyStrong.copy(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline), color = ink)
            }
        },
    )
}
