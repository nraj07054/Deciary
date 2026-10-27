package app.deciary.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.deciary.ui.components.IconTile
import app.deciary.ui.components.ShieldArt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import app.deciary.Biometrics
import app.deciary.BuildConfigInfo
import app.deciary.data.Prefs
import app.deciary.data.ThemeChoice
import app.deciary.ui.components.CoverSwatch
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.TopBar
import app.deciary.ui.daysAgo
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Type

class SettingsActions(
    val onBack: () -> Unit,
    val onCover: () -> Unit,
    val onData: () -> Unit,
    val onLockOn: () -> Unit,
    val onLockOff: (pin: String) -> Boolean,
    val onReminders: (Boolean) -> Unit,
    val onReminderHour: (Int) -> Unit,
    val onReviewMonths: (Int) -> Unit,
    val onTheme: (ThemeChoice) -> Unit,
    val pinWaitMs: () -> Long = { 0L },
)

private enum class Dialog { None, LockOff, Hour, Months, Theme, Faq }

private val reviewChoices = listOf(1 to "1 month", 3 to "3 months", 6 to "6 months", 12 to "1 year")

private fun hourLabel(h: Int) = "%d:00".format(h)

@Composable
fun SettingsScreen(prefs: Prefs, a: SettingsActions) {
    val c = Deciary.colors
    val context = LocalContext.current
    var dialog by remember { mutableStateOf(Dialog.None) }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        TopBar(onBack = a.onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).navigationBarsPadding()) {
            Text("Settings", style = Type.display, color = c.ink, modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp))
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                val cardShape = RoundedCornerShape(24.dp)
                Row(
                    Modifier.fillMaxWidth().shadow(10.dp, cardShape, ambientColor = Color(0x731D1A33), spotColor = Color(0x731D1A33)).clip(cardShape).background(c.surface)
                        .clickable(role = Role.Button, onClick = a.onCover).padding(start = 14.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CoverSwatch(prefs.leather, Modifier.size(60.dp, 46.dp).shadow(6.dp, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 10.dp, bottomEnd = 10.dp)))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(prefs.coverName, style = Type.heading.copy(fontSize = 18.sp), color = c.ink)
                        Text("${prefs.leather.label} leather · tap to change", style = Type.small, color = c.inkQuiet)
                    }
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = c.inkQuiet, modifier = Modifier.size(20.dp))
                }

                // The privacy promise, on night ink with the shield.
                Box(Modifier.fillMaxWidth().clip(cardShape).background(Color(0xFF1D1A33))) {
                    ShieldArt(96.dp, Modifier.align(Alignment.CenterEnd).padding(end = 12.dp).rotate(8f))
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp).padding(end = 96.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF5FD0C6)))
                            Spacer(Modifier.width(6.dp))
                            Text("Offline by design", style = Type.small.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = Color(0xFFCFC9E6))
                        }
                        Text("0 bytes sent", style = Type.display.copy(fontSize = 34.sp, lineHeight = 36.sp), color = Color(0xFFF5F2FB))
                        Text(
                            "Deciary has no internet permission, so Android itself stops it from sending anything.",
                            style = Type.small, color = Color(0xFFC9B8FA),
                        )
                    }
                }

                Group("Privacy") {
                    ToggleRow(
                        Icons.Rounded.Lock, Color(0xFFFDF0F5), c.wax, "App lock",
                        if (prefs.lockOn) (if (Biometrics.available(context)) "PIN and fingerprint" else "PIN") else "Off",
                        prefs.lockOn,
                    ) { on -> if (on) a.onLockOn() else dialog = Dialog.LockOff }
                    Divider()
                    LinkRow(
                        Icons.Rounded.Inventory2, Color(0xFFEDE7FD), c.accent, "Your data",
                        if (prefs.lastBackupAt == 0L) "Not backed up yet" else "Backed up ${daysAgo(prefs.lastBackupAt)}",
                        a.onData,
                    )
                }

                Group("Letters") {
                    ToggleRow(
                        Icons.Rounded.Notifications, Color(0xFFFFF4DE), Color(0xFFD98A12), "Reminders",
                        if (prefs.reminders) "On the day, ${hourLabel(prefs.reminderHour)}" else "Off",
                        prefs.reminders, a.onReminders,
                    )
                    if (prefs.reminders) {
                        Divider()
                        LinkRow(Icons.Rounded.Schedule, Color(0xFFFFF4DE), Color(0xFFD98A12), "Reminder time", hourLabel(prefs.reminderHour)) { dialog = Dialog.Hour }
                    }
                    Divider()
                    LinkRow(
                        Icons.Rounded.CalendarMonth, Color(0xFFE8F1FD), Color(0xFF2F6FD0), "Default review date",
                        "${reviewChoices.firstOrNull { it.first == prefs.reviewMonths }?.second ?: "${prefs.reviewMonths} months"} after sealing",
                    ) { dialog = Dialog.Months }
                }

                Group("Look") {
                    LinkRow(Icons.Rounded.Palette, Color(0xFFFFF4DE), Color(0xFFD98A12), "Theme", prefs.theme.label) { dialog = Dialog.Theme }
                }

                Group("Help") {
                    LinkRow(Icons.AutoMirrored.Rounded.HelpOutline, Color(0xFFFDF0F5), c.wax, "Questions people ask", "Where is my data? Lost phone?") { dialog = Dialog.Faq }
                }

                Text(
                    "Deciary ${BuildConfigInfo.versionName(context)}",
                    style = Type.small, color = c.inkQuiet,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp),
                )
            }
        }
    }

    when (dialog) {
        Dialog.None -> Unit
        Dialog.LockOff -> PinConfirmDialog(
            title = "Turn off app lock?",
            body = "Anyone holding your phone could open your diary. Enter your PIN to turn it off.",
            confirm = "Turn off",
            onDismiss = { dialog = Dialog.None },
            onPin = { pin -> a.onLockOff(pin).also { if (it) dialog = Dialog.None } },
            pinWaitMs = a.pinWaitMs,
        )
        Dialog.Hour -> OptionsDialog(
            "Remind me at", (6..22).map { it to hourLabel(it) }, prefs.reminderHour,
            onDismiss = { dialog = Dialog.None }, onPick = { a.onReminderHour(it); dialog = Dialog.None },
        )
        Dialog.Months -> OptionsDialog(
            "Open letters after", reviewChoices, prefs.reviewMonths,
            onDismiss = { dialog = Dialog.None }, onPick = { a.onReviewMonths(it); dialog = Dialog.None },
        )
        Dialog.Theme -> OptionsDialog(
            "Theme", ThemeChoice.entries.map { it to it.label }, prefs.theme,
            onDismiss = { dialog = Dialog.None }, onPick = { a.onTheme(it); dialog = Dialog.None },
        )
        Dialog.Faq -> AlertDialog(
            onDismissRequest = { dialog = Dialog.None },
            title = { Text("Questions people ask", style = Type.heading) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Faq("Where is my data?", "Only in this app, on this phone. There's no account and no cloud. Deciary can't connect to the internet.")
                    Faq("What if I lose my phone?", "Your letters go with it, unless you've made a backup. Back up from Your data and keep the file somewhere safe, like a laptop or a USB stick.")
                    Faq("What if I forget my PIN?", "There's no way to reset it without losing your letters, because nobody else holds a key. If you have a backup, you can reinstall the app and restore it.")
                    Faq("Why can't I edit a sealed letter?", "So future you sees what you really thought. Small typos can be fixed for 10 minutes after sealing.")
                }
            },
            confirmButton = { TextButton(onClick = { dialog = Dialog.None }) { Text("Got it") } },
        )
    }
}

@Composable
private fun Faq(q: String, a: String) {
    Column {
        Text(q, style = Type.bodyStrong, color = Deciary.colors.ink)
        Text(a, style = Type.body, color = Deciary.colors.inkQuiet)
    }
}

@Composable
private fun Group(title: String, content: @Composable () -> Unit) {
    val c = Deciary.colors
    val shape = RoundedCornerShape(22.dp)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Kicker(title, modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 4.dp))
        Column(
            Modifier.fillMaxWidth().shadow(10.dp, shape, ambientColor = Color(0x731D1A33), spotColor = Color(0x731D1A33)).clip(shape).background(c.surface).padding(horizontal = 14.dp),
        ) { content() }
    }
}

@Composable
private fun Divider() = HorizontalDivider(color = Deciary.colors.line)

/** A Material icon on a soft colour tile; in dark mode the tiles all share the tint. */
@Composable
private fun Tile(icon: ImageVector, tile: Color, tint: Color) {
    val dark = Deciary.colors.isDark
    IconTile(icon, if (dark) Deciary.colors.tint else tile, if (dark) Deciary.colors.accentText else tint, size = 40.dp)
}

@Composable
private fun RowText(label: String, value: String, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = Type.body.copy(fontWeight = FontWeight.SemiBold), color = Deciary.colors.ink)
        Text(value, style = Type.small.copy(fontSize = 12.sp), color = Deciary.colors.inkQuiet)
    }
}

@Composable
private fun ToggleRow(icon: ImageVector, tile: Color, tint: Color, label: String, value: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 62.dp).toggleable(checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tile(icon, tile, tint)
        Spacer(Modifier.width(12.dp))
        RowText(label, value, Modifier.weight(1f))
        DesignSwitch(checked)
    }
}

/** The design's switch: a 48 × 30 track with a white knob that slides across. */
@Composable
private fun DesignSwitch(on: Boolean) {
    val c = Deciary.colors
    val x by animateDpAsState(if (on) 21.dp else 3.dp, label = "knob")
    Box(Modifier.size(48.dp, 30.dp).clip(CircleShape).background(if (on) c.accent else if (c.isDark) c.line else Color(0xFFCFC7E3))) {
        Box(Modifier.offset(x = x, y = 3.dp).size(24.dp).shadow(2.dp, CircleShape).clip(CircleShape).background(Color.White))
    }
}

@Composable
private fun LinkRow(icon: ImageVector, tile: Color, tint: Color, label: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 62.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tile(icon, tile, tint)
        Spacer(Modifier.width(12.dp))
        RowText(label, value, Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Deciary.colors.inkQuiet, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun <T> OptionsDialog(title: String, options: List<Pair<T, String>>, selected: T, onDismiss: () -> Unit, onPick: (T) -> Unit) {
    val c = Deciary.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = Type.heading) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                options.forEach { (value, label) ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(value == selected, role = Role.RadioButton) { onPick(value) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = value == selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = c.accent))
                        Spacer(Modifier.width(12.dp))
                        Text(label, style = Type.body, color = c.ink)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

/** Asks for the PIN before something sensitive. [onPin] returns whether it matched. */
@Composable
fun PinConfirmDialog(
    title: String,
    body: String,
    confirm: String,
    onDismiss: () -> Unit,
    onPin: (String) -> Boolean,
    extra: @Composable () -> Unit = {},
    canConfirm: Boolean = true,
    pinWaitMs: () -> Long = { 0L },
) {
    var pin by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }
    var attempts by remember { mutableStateOf(0) }
    var wait by remember { mutableStateOf(pinWaitMs()) }
    LaunchedEffect(attempts) {
        wait = pinWaitMs()
        while (wait > 0) {
            kotlinx.coroutines.delay(1000)
            wait = pinWaitMs()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = Type.heading) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(body, style = Type.body)
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= PIN_LENGTH && it.all(Char::isDigit)) { pin = it; wrong = false } },
                    label = { Text("PIN") },
                    singleLine = true,
                    isError = wrong || wait > 0,
                    supportingText = when {
                        wait > 0 -> ({ Text("Too many tries. Try again in ${waitLabel(wait)}.") })
                        wrong -> ({ Text("That PIN didn't match.") })
                        else -> null
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
                extra()
            }
        },
        confirmButton = {
            TextButton(onClick = { if (!onPin(pin)) { wrong = true; attempts++ } }, enabled = pin.length == PIN_LENGTH && canConfirm && wait <= 0) {
                Text(confirm, color = Deciary.colors.danger)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
