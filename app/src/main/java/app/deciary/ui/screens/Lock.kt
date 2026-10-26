package app.deciary.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.deciary.Biometrics
import app.deciary.data.Leather
import app.deciary.ui.components.LeatherSurface
import app.deciary.ui.components.Snap
import app.deciary.ui.components.palette
import app.deciary.ui.theme.Type
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

const val PIN_LENGTH = 4

/** Four dots and a keypad, drawn on the leather. Shared by the lock screen and PIN setup. */
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

    LeatherSurface(leather, Modifier.fillMaxSize(), corner = 0.dp, stitchInset = 14.dp) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.6f))
            Snap(leather, Modifier.size(54.dp))
            Spacer(Modifier.height(18.dp))
            Text(title, style = Type.display, color = p.ink, textAlign = TextAlign.Center)
            Text(subtitle, style = Type.small, color = p.ink.copy(alpha = 0.75f))
            Spacer(Modifier.height(30.dp))
            Text(prompt, style = Type.bodyStrong, color = p.ink)
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier
                    .offset { IntOffset(shake.value.roundToInt(), 0) }
                    .semantics { contentDescription = "${pin.length} of $PIN_LENGTH digits entered" },
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                repeat(PIN_LENGTH) { i ->
                    Box(
                        Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (i < pin.length) p.ink else Color.Transparent)
                            .border(2.dp, p.ink, CircleShape),
                    )
                }
            }
            Text(
                error ?: " ",
                style = Type.small,
                color = Color(0xFFFFC9D9),
                modifier = Modifier.padding(top = 10.dp).semantics { liveRegion = LiveRegionMode.Polite },
            )
            Spacer(Modifier.weight(0.4f))
            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "del")
            Column(Modifier.widthIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                keys.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { k ->
                            val m = Modifier.weight(1f).aspectRatio(1.35f).clip(CircleShape)
                            when (k) {
                                "" -> Spacer(m)
                                "del" -> Box(
                                    m.semantics { contentDescription = "Delete digit" }.clickable { pin = pin.dropLast(1) },
                                    contentAlignment = Alignment.Center,
                                ) { Icon(Icons.AutoMirrored.Rounded.Backspace, null, tint = p.ink) }
                                else -> Box(
                                    m.background(p.ink.copy(alpha = 0.10f)).clickable { press(k) },
                                    contentAlignment = Alignment.Center,
                                ) { Text(k, style = Type.title, color = p.ink) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
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
            if (canBio) {
                TextButton(onClick = { Biometrics.prompt(context, onBiometric) }) {
                    Icon(Icons.Rounded.Fingerprint, null, tint = leather.palette().ink)
                    Spacer(Modifier.size(8.dp))
                    Text("Use fingerprint", style = Type.bodyStrong, color = leather.palette().ink)
                }
            }
        },
    )
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
                Text("Not now", style = Type.bodyStrong, color = ink)
            }
        },
    )
}
